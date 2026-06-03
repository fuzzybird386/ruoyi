package com.ruoyi.transmit.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.transmit.entity.CleanupResult;
import com.ruoyi.transmit.entity.CompletedDataStats;
import com.ruoyi.transmit.handler.QrCodeWebSocketHandler;
import com.ruoyi.transmit.mapper.QrCodeQueueManager;
import com.ruoyi.transmit.signal.SuccessSignalHandler;
import com.ruoyi.transmit.signal.gpio.GpioSignalMonitor;
import com.ruoyi.transmit.service.DataCleanupService;
import com.ruoyi.transmit.service.QrCodeQueueService;
import com.ruoyi.transmit.service.StatisticsService;
import com.ruoyi.transmit.utils.QRCodeUtil;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.StringUtils;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 短信二维码生成控制器
 */

@RestController
@RequestMapping("/smsQrCode")
public class SmsController extends BaseController {

    private static final Map<String, Object> latestQrData = new ConcurrentHashMap<>();
    private static String latestQrCodeBase64 = "";


    @Autowired
    private SuccessSignalHandler successSignalHandler;

    @Autowired
    private GpioSignalMonitor gpioSignalMonitor;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private QrCodeWebSocketHandler qrCodeWebSocketHandler;

    @Autowired
    private QrCodeQueueService qrCodeQueueService;

    @Autowired
    private QrCodeQueueManager qrCodeQueueManager;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private DataCleanupService dataCleanupService;


    // 内存存储最新生成的二维码数据
    private Map<String, Object> latestQrCodeData = new ConcurrentHashMap<>();

    private AtomicLong lastUpdateTime = new AtomicLong(0);
    private final Object lock = new Object();


    /**
     * 接收JSON数据并生成二维码
     */
    /**
     * 生成二维码API
     * @param data 接收的JSON数据
     * @param response HTTP响应
     */
    @PostMapping("/generate")
    public void generateQrCode(@RequestBody Map<String, Object> data, HttpServletResponse response) {
        try {
            // 将JSON数据转换为字符串
            String content = StringUtils.format("{}", data);

            // 设置响应类型为图片
            response.setContentType("image/png");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Pragma", "no-cache");

            // 生成二维码并输出到响应流
            QRCodeUtil.createCodeToStream(content,
                    300, // 宽度
                    300, // 高度
                    "png", // 格式
                    response.getOutputStream());

        } catch (Exception e) {
            logger.error("生成二维码失败", e);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "生成二维码失败");
            } catch (Exception ex) {
                logger.error("发送错误响应失败", ex);
            }
        }
    }

    /**
     * 生成二维码并返回Base64字符串
     * @param data 接收的JSON数据
     * @return 包含Base64二维码图片的结果
     */
    @PostMapping("/generateBase64")
    public AjaxResult generateQrCodeBase64(@RequestBody Map<String, Object> data) {
        try {
            // 将JSON数据转换为字符串
            String content = StringUtils.format("{}", data);

            // 生成二维码Base64字符串
            String base64QrCode = QRCodeUtil.createCodeToBase64(content,
                    300, // 宽度
                    300, // 高度
                    "png"); // 格式

            // 移除Base64前缀（如果需要）
            if (base64QrCode.startsWith("data:image/png;base64,")) {
                base64QrCode = base64QrCode.substring("data:image/png;base64,".length());
            }

            return AjaxResult.success("二维码生成成功", base64QrCode);

        } catch (Exception e) {
            logger.error("生成二维码失败", e);
            return AjaxResult.error("生成二维码失败: " + e.getMessage());
        }
    }

    /**
     * 自动生成二维码API
     * 只要调用这个接口并传入数据，就自动生成二维码返回
     *
     * @param data 接收的任意JSON数据
     * @param response HTTP响应
     */
//    @PostMapping("/autoGenerate")
//    public void autoGenerateQrCode(@RequestBody(required = false) Map<String, Object> data,
//                                   HttpServletResponse response) {
//        try {
//            // 记录接收到的数据
//            logger.info("【自动生成】接收到二维码生成请求，数据: {}", data);
//
//            String content;
//            if (data == null || data.isEmpty()) {
//                content = "暂无数据";
//                logger.warn("【自动生成】接收到空数据，使用默认内容");
//            } else {
//                // 将JSON数据转换为字符串作为二维码内容
//                content = StringUtils.format("{}", data);
//            }
//
//            // 通知所有连接的客户端
////            messagingTemplate.convertAndSend("/topic/qrCodeUpdates", notification);
//
//            logger.info("【自动生成】二维码内容: {}", content);
//
//            // 设置响应类型为图片
//            response.setContentType("image/png");
//            response.setHeader("Cache-Control", "no-cache");
//            response.setHeader("Pragma", "no-cache");
//            response.setHeader("X-QR-Content", java.net.URLEncoder.encode(content, "UTF-8"));
//
//            // 自动生成二维码并输出到响应流
//            QRCodeUtil.createCodeToStream(content,
//                    300, // 宽度
//                    300, // 高度
//                    "png", // 格式
//                    response.getOutputStream());
//
//            logger.info("【自动生成】二维码生成成功，内容长度: {}", content.length());
//
//        } catch (Exception e) {
//            logger.error("【自动生成】生成二维码失败", e);
//            try {
//                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "生成二维码失败: " + e.getMessage());
//            } catch (Exception ex) {
//                logger.error("【自动生成】发送错误响应失败", ex);
//            }
//        }
//    }
//    /**
//     * 自动生成二维码API
//     * 只要调用这个接口并传入数据，就自动生成二维码返回
//     * 添加WebSocket实时通知*******************************
//     *
//     * @param data 接收的任意JSON数据
//     * @param response HTTP响应
//     */
//    @PostMapping("/autoGenerate")
//    public void autoGenerateQrCode(@RequestBody(required = false) Map<String, Object> data,
//                                   HttpServletResponse response) {
//        try {
//            // 记录接收到的数据
//            logger.info("【自动生成】接收到二维码生成请求，数据: {}", data);
//
//            String content;
//            if (data == null || data.isEmpty()) {
//                content = "暂无数据";
//                logger.warn("【自动生成】接收到空数据，使用默认内容");
//            } else {
//                // 将JSON数据转换为字符串作为二维码内容
//                content = StringUtils.format("{}", data);
//            }
//
//            logger.info("【自动生成】二维码内容: {}", content);
//
//            // 设置响应类型为图片
//            response.setContentType("image/png");
//            response.setHeader("Cache-Control", "no-cache");
//            response.setHeader("Pragma", "no-cache");
//            response.setHeader("X-QR-Content", java.net.URLEncoder.encode(content, "UTF-8"));
//
//            // 自动生成二维码并输出到响应流
//            QRCodeUtil.createCodeToStream(content,
//                    300, // 宽度
//                    300, // 高度
//                    "png", // 格式
//                    response.getOutputStream());
//
//            logger.info("【自动生成】二维码生成成功，内容长度: {}", content.length());
//
//            // 🔥 新增：通知前端有新的二维码生成
//            notifyFrontendQrCodeGenerated(data, content);
//
//        } catch (Exception e) {
//            logger.error("【自动生成】生成二维码失败", e);
//            try {
//                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "生成二维码失败: " + e.getMessage());
//            } catch (Exception ex) {
//                logger.error("【自动生成】发送错误响应失败", ex);
//            }
//        }
//    }
//
//    /**
//     * 通知前端二维码已生成
//     */
//    private void notifyFrontendQrCodeGenerated(Map<String, Object> data, String content) {
//        try {
//            Map<String, Object> notification = new HashMap<>();
//            notification.put("type", "QR_CODE_GENERATED");
//            notification.put("timestamp", System.currentTimeMillis());
//            notification.put("data", data);
//            notification.put("content", content);
//            notification.put("message", "新的二维码已生成");
//
//            // 发送给所有订阅了/topic/qrCodeUpdates的客户端
//            messagingTemplate.convertAndSend("/topic/qrCodeUpdates", notification);
//
//            logger.info("【自动生成】已通知前端更新二维码，数据: {}", data);
//        } catch (Exception e) {
//            logger.error("【自动生成】通知前端失败", e);
//        }
//    }
    /***
     * 添加轮询检查接口2222222222222222222
     */


    /**
     * 自动生成二维码API
     */
//    @PostMapping("/autoGenerate")
//    public void autoGenerateQrCode(@RequestBody(required = false) Map<String, Object> data,
//                                   HttpServletResponse response) {
//        try {
//            logger.info("【自动生成】接收到二维码生成请求，数据: {}", data);
//
//            String content;
//            if (data == null || data.isEmpty()) {
//                content = "暂无数据";
//                logger.warn("【自动生成】接收到空数据，使用默认内容");
//            } else {
//                content = StringUtils.format("{}", data);
//
//                // 存储最新数据供前端轮询
//                synchronized (lock) {
//                    this.latestQrCodeData = new ConcurrentHashMap<>(data);
//                    this.lastUpdateTime.set(System.currentTimeMillis());
//                    logger.info("【自动生成】已存储最新二维码数据，时间戳: {}", lastUpdateTime.get());
//                }
//            }
//
//            logger.info("【自动生成】二维码内容: {}", content);
//
//            // 设置响应类型为图片
//            response.setContentType("image/png");
//            response.setHeader("Cache-Control", "no-cache");
//            response.setHeader("Pragma", "no-cache");
//            response.setHeader("X-QR-Content", java.net.URLEncoder.encode(content, "UTF-8"));
//
//            // 自动生成二维码并输出到响应流
//            QRCodeUtil.createCodeToStream(content, 300, 300, "png", response.getOutputStream());
//
//            logger.info("【自动生成】二维码生成成功，内容长度: {}", content.length());
//
//        } catch (Exception e) {
//            logger.error("【自动生成】生成二维码失败", e);
//            try {
//                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "生成二维码失败: " + e.getMessage());
//            } catch (Exception ex) {
//                logger.error("【自动生成】发送错误响应失败", ex);
//            }
//        }
//    }
//    @PostMapping("/autoGenerate")
//    public void autoGenerateQrCode(@RequestBody(required = false) Map<String, Object> data,
//                                   HttpServletResponse response) {
//        try {
//            logger.info("【自动生成】接收到二维码生成请求，数据: {}", data);
//
//            String content;
//            if (data == null || data.isEmpty()) {
//                content = "暂无数据";
//            } else {
//                content = StringUtils.format("{}", data);
//            }
//
//            // 🔥 新增：通知WebSocket客户端
//            qrCodeWebSocketHandler.notifyNewQrCode(data);
//
//            logger.info("【自动生成】二维码内容: {}", content);
//
//            // 设置响应类型为图片
//            response.setContentType("image/png");
//            response.setHeader("Cache-Control", "no-cache");
//
//            // 生成二维码
//            QRCodeUtil.createCodeToStream(content, 300, 300, "png", response.getOutputStream());
//
//            logger.info("【自动生成】二维码生成成功，内容长度: {}", content.length());
//
//        } catch (Exception e) {
//            logger.error("【自动生成】生成二维码失败", e);
//            try {
//                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "生成二维码失败: " + e.getMessage());
//            } catch (Exception ex) {
//                logger.error("【自动生成】发送错误响应失败", ex);
//            }
//        }
//    }


//    @PostMapping("/autoGenerate")
//    public void autoGenerateQrCode(@RequestBody(required = false) Map<String, Object> data,
//                                   HttpServletResponse response) {
//        try {
//            logger.info("【自动生成】接收到二维码生成请求，数据: {}", data);
//
//            String content;
//            if (data == null || data.isEmpty()) {
//                content = "暂无数据";
//            } else {
//                content = StringUtils.format("{}", data);
//            }
//
//            // 先存储数据到内存中，供轮询接口使用
//            synchronized (lock) {
//                latestQrCodeData.clear();
//                latestQrCodeData.putAll(data != null ? data : new HashMap<>());
//                lastUpdateTime.set(System.currentTimeMillis());
//                logger.info("【自动生成】已存储数据到内存，数据大小: {}", latestQrCodeData.size());
//            }
//
//            // 然后通知WebSocket客户端
//            qrCodeWebSocketHandler.notifyNewQrCode(data);
//            logger.info("【自动生成】已发送WebSocket通知");
//
//            logger.info("【自动生成】二维码内容: {}", content);
//
//            // 设置响应类型为图片
//            response.setContentType("image/png");
//            response.setHeader("Cache-Control", "no-cache");
//
//            // 生成二维码
//            QRCodeUtil.createCodeToStream(content, 300, 300, "png", response.getOutputStream());
//
//            logger.info("【自动生成】二维码生成成功，内容长度: {}", content.length());
//
//        } catch (Exception e) {
//            logger.error("【自动生成】生成二维码失败", e);
//            try {
//                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "生成二维码失败: " + e.getMessage());
//            } catch (Exception ex) {
//                logger.error("【自动生成】发送错误响应失败", ex);
//            }
//        }
//    }
//    @PostMapping("/autoGenerate")
//    public void autoGenerateQrCode(@RequestBody(required = false) Map<String, Object> data,
//                                   HttpServletResponse response) {
//        try {
//            logger.info("【自动生成】接收到二维码生成请求，数据: {}", data);
//
//            String content;
//            if (data == null || data.isEmpty()) {
//                content = "暂无数据";
//            } else {
//                content = StringUtils.format("{}", data);
//            }
//
//            // 先通知WebSocket客户端，再生成二维码
//            qrCodeWebSocketHandler.notifyNewQrCode(data);
//            logger.info("【自动生成】已发送WebSocket通知");
//
//            logger.info("【自动生成】二维码内容: {}", content);
//
//            // 设置响应类型为图片
//            response.setContentType("image/png");
//            response.setHeader("Cache-Control", "no-cache");
//            response.setHeader("Pragma", "no-cache");
//
//            // 生成二维码
//            QRCodeUtil.createCodeToStream(content, 300, 300, "png", response.getOutputStream());
//
//            logger.info("【自动生成】二维码生成成功，内容长度: {}", content.length());
//
//        } catch (Exception e) {
//            logger.error("【自动生成】生成二维码失败", e);
//            try {
//                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "生成二维码失败: " + e.getMessage());
//            } catch (Exception ex) {
//                logger.error("【自动生成】发送错误响应失败", ex);
//            }
//        }
//    }

    /**
     * 自动生成二维码API - 修改后版本
     * 从消息队列获取数据，实现二维码刷新机制
     */
//    @PostMapping("/autoGenerate")
//    public void autoGenerateQrCode(@RequestBody(required = false) Map<String, Object> data,
//                                   HttpServletResponse response) {
//        try {
//            // 优先从消息队列获取数据
//            Map<String, String> queueData = qrCodeQueueService.pollFromQueue();
//
//            Map<String, Object> convertedData = new HashMap<>(queueData);
//
//            if (queueData != null) {
//                String dataId = queueData.get("_dataId");
//                logger.info("【自动生成】从队列获取数据，ID: {}, 数据: {}", dataId, queueData);
//
//                // 开始二维码刷新任务
//                qrCodeQueueService.startRefreshTask(dataId, queueData);
//
//                // 使用队列数据生成二维码
//                String content = StringUtils.format("{}", queueData);
//
//                // 先通知WebSocket客户端
//                qrCodeWebSocketHandler.notifyNewQrCode(convertedData);
//                logger.info("【自动生成】已发送WebSocket通知，数据ID: {}", dataId);
//
//                logger.info("【自动生成】二维码内容: {}", content);
//
//                // 设置响应类型为图片
//                response.setContentType("image/png");
//                response.setHeader("Cache-Control", "no-cache");
//                response.setHeader("Pragma", "no-cache");
//                response.setHeader("X-Data-Id", dataId); // 在header中返回数据ID
//
//                // 生成二维码
//                QRCodeUtil.createCodeToStream(content, 300, 300, "png", response.getOutputStream());
//
//                logger.info("【自动生成】二维码生成成功，数据ID: {}", dataId);
//
//            } else if (data != null && !data.isEmpty()) {
//                // 如果没有队列数据，但传入了数据，则使用传入的数据
//                logger.info("【自动生成】使用直接传入的数据: {}", data);
//
//                String content = StringUtils.format("{}", data);
//                qrCodeWebSocketHandler.notifyNewQrCode(data);
//
//                response.setContentType("image/png");
//                response.setHeader("Cache-Control", "no-cache");
//                response.setHeader("Pragma", "no-cache");
//
//                QRCodeUtil.createCodeToStream(content, 300, 300, "png", response.getOutputStream());
//
//            } else {
//                // 既没有队列数据也没有传入数据，生成默认二维码
//                logger.info("【自动生成】无数据，生成默认二维码");
//
//                String content = "暂无数据";
//                response.setContentType("image/png");
//                response.setHeader("Cache-Control", "no-cache");
//                response.setHeader("Pragma", "no-cache");
//
//                QRCodeUtil.createCodeToStream(content, 300, 300, "png", response.getOutputStream());
//            }
//
//        } catch (Exception e) {
//            logger.error("【自动生成】生成二维码失败", e);
//            try {
//                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "生成二维码失败: " + e.getMessage());
//            } catch (Exception ex) {
//                logger.error("【自动生成】发送错误响应失败", ex);
//            }
//        }
//    }


//    @PostMapping("/autoGenerate")
//    public void autoGenerateQrCode(@RequestBody(required = false) Map<String, Object> data,
//                                   HttpServletResponse response) {
//        try {
//            // 从队列管理器获取当前处理的数据
//            Map<String, Object> currentData = qrCodeQueueManager.getCurrentProcessingData();
//            String content;
//
//            // 创建 ObjectMapper 用于 JSON 序列化
//            ObjectMapper objectMapper = new ObjectMapper();
//
//            if (currentData != null) {
//                // 创建不包含系统字段的纯净数据
//                Map<String, Object> cleanData = new HashMap<>(currentData);
//                // 移除系统字段
//                cleanData.remove("_dataId");
//                cleanData.remove("_refreshCount");
//                cleanData.remove("_failReason");
//
////                content = StringUtils.format("{}", currentData);
//                // 使用 ObjectMapper 正确序列化为 JSON 字符串
//                content = objectMapper.writeValueAsString(cleanData);
//                logger.info("【自动生成】使用队列当前数据生成二维码，ID: {}", currentData.get("_dataId"));
//            } else if (data != null && !data.isEmpty()) {
//                // 同样清理传入的数据
//                Map<String, Object> cleanData = new HashMap<>(data);
//                cleanData.remove("_dataId");
//                cleanData.remove("_refreshCount");
//                cleanData.remove("_failReason");
//
////                content = StringUtils.format("{}", data);
//                // 使用 ObjectMapper 正确序列化为 JSON 字符串
//                content = objectMapper.writeValueAsString(cleanData);
//                logger.info("【自动生成】使用传入数据生成二维码");
//
//            } else {
//                content = "暂无处理数据";
//                logger.info("【自动生成】无数据，生成默认二维码");
//            }
//
//            logger.info("【自动生成】二维码内容: {}", content);
//
//            // 设置响应类型为图片
//            response.setContentType("image/png");
//            response.setHeader("Cache-Control", "no-cache");
//            response.setHeader("Pragma", "no-cache");
//
//            // 生成二维码
//            QRCodeUtil.createCodeToStream(content, 300, 300, "png", response.getOutputStream());
//
//            logger.info("【自动生成】二维码生成成功");
//
//        } catch (Exception e) {
//            logger.error("【自动生成】生成二维码失败", e);
//            try {
//                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "生成二维码失败: " + e.getMessage());
//            } catch (Exception ex) {
//                logger.error("【自动生成】发送错误响应失败", ex);
//            }
//        }
//    }
    @PreAuthorize("@ss.hasPermi('smsQrCode:autoGenerate')")
    @PostMapping("/autoGenerate")
    public void autoGenerateQrCode(@RequestBody(required = false) Map<String, Object> data,
                                   HttpServletResponse response) {
        try {
            // 从队列管理器获取当前处理的数据
            Map<String, Object> currentData = qrCodeQueueManager.getCurrentProcessingData();
            String content;

            // 创建 ObjectMapper 用于 JSON 序列化
            ObjectMapper objectMapper = new ObjectMapper();

            // 详细的调试日志
            logger.info("【自动生成】接收到的数据: {}", data);
            logger.info("【自动生成】队列当前数据: {}", currentData);

            if (currentData != null) {
                // 详细的类型检查
                logger.info("【自动生成】currentData 类型: {}", currentData.getClass().getName());
                logger.info("【自动生成】currentData 键值: {}", currentData.keySet());

                // 确保是 Map 类型
                if (currentData instanceof Map) {
                    // 清理数据
                    Map<String, Object> cleanData = new HashMap<>((Map<String, Object>) currentData);
                    cleanData.keySet().removeIf(key -> key.startsWith("_"));

                    // 使用 ObjectMapper 序列化
                    content = objectMapper.writeValueAsString(cleanData);
                    logger.info("【自动生成】使用队列当前数据生成二维码");
                } else {
                    // 如果不是 Map，直接使用 toString()
                    content = currentData.toString();
                    logger.warn("【自动生成】currentData 不是 Map 类型，使用 toString()");
                }
            } else if (data != null && !data.isEmpty()) {
                logger.info("【自动生成】data 类型: {}", data.getClass().getName());

                // 清理传入的数据
                Map<String, Object> cleanData = new HashMap<>(data);
                cleanData.keySet().removeIf(key -> key.startsWith("_"));
                content = objectMapper.writeValueAsString(cleanData);
                logger.info("【自动生成】使用传入数据生成二维码");
            } else {
                content = "{\"message\":\"暂无处理数据\"}";
                logger.info("【自动生成】无数据，生成默认二维码");
            }

            // 详细的二维码内容检查
            logger.info("【自动生成】最终二维码内容: {}", content);
            logger.info("【自动生成】内容长度: {}", content.length());
            logger.info("【自动生成】是否包含{{}: {}", content.contains("{"));
            logger.info("【自动生成】是否包含}}: {}", content.contains("}"));
            logger.info("【自动生成】内容前100字符: {}", content.substring(0, Math.min(content.length(), 100)));

            // 设置响应类型为图片
            response.setContentType("image/png");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Pragma", "no-cache");

            // 生成二维码
            QRCodeUtil.createCodeToStream(content, 300, 300, "png", response.getOutputStream());

            logger.info("【自动生成】二维码生成成功");

            // 记录二维码生成统计
            statisticsService.recordQrCodeGenerate();

        } catch (Exception e) {
            logger.error("【自动生成】生成二维码失败", e);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "生成二维码失败: " + e.getMessage());
            } catch (Exception ex) {
                logger.error("【自动生成】发送错误响应失败", ex);
            }
        }
    }

    /**
     * 检查最新二维码数据的接口
     */
    @GetMapping("/check-latest-qrcode")
    public ResponseEntity<Map<String, Object>> checkLatestQrCode() {
        Map<String, Object> response = new HashMap<>();

        synchronized (lock) {
            if (latestQrCodeData.isEmpty() || lastUpdateTime.get() == 0) {
                response.put("success", true);
                response.put("hasNew", false);
                response.put("message", "暂无新的二维码数据");
                logger.info("【轮询检查】暂无新数据");
            } else {
                response.put("success", true);
                response.put("hasNew", true);
                response.put("qrCodeData", new HashMap<>(latestQrCodeData));
                response.put("lastUpdateTime", lastUpdateTime.get());
                response.put("message", "发现新的二维码数据");

                logger.info("【轮询检查】返回新数据: {}", latestQrCodeData);

                // 读取后清空
                latestQrCodeData.clear();
                lastUpdateTime.set(0);
            }
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 自动生成二维码并返回Base64（推荐使用）
     * 接收到数据就自动生成Base64格式的二维码
     *
     * @param data 接收的任意JSON数据
     * @return 包含Base64二维码图片的结果
     */
    @PostMapping("/autoGenerateBase64")
    public AjaxResult autoGenerateQrCodeBase64(@RequestBody(required = false) Map<String, Object> data) {

//        Map<String, Object> result = new HashMap<>();
        try {
            // 记录接收到的数据
            logger.info("【自动生成Base64】接收到二维码生成请求，数据: {}", data);

            String content;
            if (data == null || data.isEmpty()) {
                content = "暂无数据";
                logger.warn("【自动生成Base64】接收到空数据，使用默认内容");
            } else {
                // 将JSON数据转换为字符串作为二维码内容
                content = StringUtils.format("{}", data);
            }

            logger.info("【自动生成Base64】二维码内容: {}", content);

            // 自动生成二维码Base64字符串
            String base64QrCode = QRCodeUtil.createCodeToBase64(content, 300, 300, "png");

            // 记录生成结果
            logger.info("【自动生成Base64】二维码生成成功，Base64长度: {}",
                    base64QrCode != null ? base64QrCode.length() : 0);

            // 返回结果
            Map<String, Object> result = new ConcurrentHashMap<>();
            result.put("qrCodeImage", base64QrCode);
            result.put("content", content);
            result.put("timestamp", System.currentTimeMillis());
            result.put("contentLength", content.length());

            return AjaxResult.success("二维码自动生成成功", result);

        } catch (Exception e) {
            logger.error("【自动生成Base64】生成二维码失败", e);
            return AjaxResult.error("自动生成二维码失败: " + e.getMessage());
        }
    }

    /**
     * 返回值自定义 0：成功
     * @param data
     * @return
     */
    @PostMapping("/autoGenerateBase64B")
    public Map<String, Object> autoGenerateQrCodeBase64B(@RequestBody(required = false) Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 从队列管理器获取当前处理的数据
            Map<String, Object> currentData = qrCodeQueueManager.getCurrentProcessingData();
            String content;

            if (currentData != null) {
                content = StringUtils.format("{}", currentData);
                logger.info("【自动生成】使用队列当前数据生成二维码，ID: {}", currentData.get("_dataId"));
            } else if (data != null && !data.isEmpty()) {
                content = StringUtils.format("{}", data);
                logger.info("【自动生成】使用传入数据生成二维码");
            } else {
                content = "暂无处理数据";
                logger.info("【自动生成】无数据，生成默认二维码");
            }

            logger.info("【自动生成】二维码内容: {}", content);

            // 生成二维码到字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            QRCodeUtil.createCodeToStream(content, 300, 300, "png", outputStream);

            // 转换为Base64
            String base64Image = Base64.getEncoder().encodeToString(outputStream.toByteArray());

            // 构建返回结果
            result.put("success", true);
            result.put("code", "0");
            result.put("message", "二维码生成成功");
            result.put("qrCodeImage", "data:image/png;base64," + base64Image);
            result.put("content", content);

            logger.info("【自动生成】二维码生成成功");

        } catch (Exception e) {
            logger.error("【自动生成】生成二维码失败", e);
            result.put("success", false);
            result.put("code", "500");
            result.put("message", "生成二维码失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * GET方式自动生成二维码 - 支持URL参数
     * 方便直接通过浏览器访问生成二维码
     *
     * @param content 二维码内容
     * @param width 宽度
     * @param height 高度
     * @param response HTTP响应
     */
    @GetMapping("/autoGenerate")
    public void autoGenerateQrCodeByGet(
            @RequestParam(value = "content", required = false, defaultValue = "自动生成的二维码") String content,
            @RequestParam(value = "width", defaultValue = "300") int width,
            @RequestParam(value = "height", defaultValue = "300") int height,
            HttpServletResponse response) {
        try {
            logger.info("【GET自动生成】接收到请求，内容: {}, 尺寸: {}x{}", content, width, height);

            // 设置响应类型为图片
            response.setContentType("image/png");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Pragma", "no-cache");

            // 自动生成二维码
            QRCodeUtil.createCodeToStream(content, width, height, "png", response.getOutputStream());

            logger.info("【GET自动生成】二维码生成成功");

        } catch (Exception e) {
            logger.error("【GET自动生成】生成二维码失败", e);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "生成二维码失败");
            } catch (Exception ex) {
                logger.error("【GET自动生成】发送错误响应失败", ex);
            }
        }
    }

    /**
     * 批量自动生成二维码
     * 接收数组数据，为每个元素生成二维码
     *
//     * @param dataList 数据列表
     * @return 包含多个二维码的结果
     */
    @PostMapping("/batchAutoGenerate")
    public AjaxResult batchAutoGenerateQrCode(@RequestBody Map<String, Object> requestData) {
        try {
            logger.info("【批量自动生成】接收到批量生成请求，数据: {}", requestData);

            Map<String, Object> result = new ConcurrentHashMap<>();
            result.put("timestamp", System.currentTimeMillis());
            result.put("total", 0);

            // 检查是否包含数据列表
            if (requestData.containsKey("dataList") && requestData.get("dataList") instanceof java.util.List) {
                java.util.List<Object> dataList = (java.util.List<Object>) requestData.get("dataList");
                java.util.List<Map<String, Object>> qrCodes = new java.util.ArrayList<>();

                for (int i = 0; i < dataList.size(); i++) {
                    Object item = dataList.get(i);
                    String content = StringUtils.format("{}", item);

                    try {
                        String base64QrCode = QRCodeUtil.createCodeToBase64(content, 200, 200, "png");

                        Map<String, Object> qrItem = new ConcurrentHashMap<>();
                        qrItem.put("index", i);
                        qrItem.put("content", content);
                        qrItem.put("qrCodeImage", base64QrCode);
                        qrCodes.add(qrItem);
                    } catch (Exception e) {
                        logger.error("【批量自动生成】生成第{}个二维码失败", i, e);
                    }
                }

                result.put("qrCodes", qrCodes);
                result.put("total", qrCodes.size());
                result.put("successCount", qrCodes.size());
            } else {
                // 如果不是列表，就生成单个二维码
                String content = StringUtils.format("{}", requestData);
                String base64QrCode = QRCodeUtil.createCodeToBase64(content, 300, 300, "png");

                result.put("singleQrCode", base64QrCode);
                result.put("content", content);
                result.put("total", 1);
            }

            logger.info("【批量自动生成】批量生成完成，总数: {}", result.get("total"));
            return AjaxResult.success("批量二维码自动生成成功", result);

        } catch (Exception e) {
            logger.error("【批量自动生成】批量生成二维码失败", e);
            return AjaxResult.error("批量自动生成二维码失败: " + e.getMessage());
        }
    }



    /**
     * 存储数据并生成二维码
     */
    @PostMapping("/generateAndStore")
    public AjaxResult generateAndStore(@RequestBody Map<String, Object> data) {
        try {
            String content = StringUtils.format("{}", data);
            String base64QrCode = QRCodeUtil.createCodeToBase64(content, 300, 300, "png");

            // 存储数据
            latestQrData.put("data", data);
            latestQrData.put("qrCode", base64QrCode);
            latestQrData.put("timestamp", System.currentTimeMillis());

            return AjaxResult.success("二维码生成并存储成功", base64QrCode);
        } catch (Exception e) {
            return AjaxResult.error("生成失败: " + e.getMessage());
        }
    }

    /**
     * 获取最新生成的二维码
     */
    @GetMapping("/getLatestQrCode")
    public AjaxResult getLatestQrCode() {
        if (latestQrData.isEmpty()) {
            return AjaxResult.error("暂无二维码数据");
        }
        return AjaxResult.success(latestQrData);
    }

    /**
     * 设置数据队列 - 供外部API调用
     */
    @PostMapping("/setDataQueue")
    public AjaxResult setDataQueue(@RequestBody List<Map<String, Object>> dataList) {
        try {
            if (dataList == null) {
                return AjaxResult.error("数据列表不能为null");
            }

            for (Map<String, Object> item : dataList)
            {
                qrCodeQueueManager.addToQueue1(item);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("queueSize", dataList.size());
            result.put("message", "数据队列设置成功");
            result.put("timestamp", System.currentTimeMillis());

            logger.info("【设置数据队列】成功，数据量: {}", dataList.size());
            return AjaxResult.success("数据队列设置成功", result);

        } catch (Exception e) {
            logger.error("【设置数据队列】失败", e);
            return AjaxResult.error("设置数据队列失败: " + e.getMessage());
        }
    }


    /**
     * 获取当前二维码（TCP信号控制）
     */
//    @GetMapping("/getCurrentQrCode")
//    public AjaxResult getCurrentQrCode() {
//
//        try {
//            Map<String, Object> currentData = tcpSignalReceiver.getCurrentData();
//
//            Map<String, Object> result = new HashMap<>();
//            result.put("timestamp", System.currentTimeMillis());
//
//            if (currentData != null) {
//                // 创建 ObjectMapper 用于 JSON 序列化
//                ObjectMapper objectMapper = new ObjectMapper();
//
//                // 创建纯净数据
//                Map<String, Object> cleanData = new HashMap<>(currentData);
//                cleanData.remove("_dataId");
//                cleanData.remove("_refreshCount");
//                cleanData.remove("_failReason");
//
//                // 有数据，生成二维码
////                String content = StringUtils.format("{}", currentData);
//                // 使用 ObjectMapper 正确序列化为 JSON 字符串
//                String content = objectMapper.writeValueAsString(cleanData);
//                String base64QrCode = QRCodeUtil.createCodeToBase64(content, 300, 300, "png");
//
//                result.put("hasData", true);
//                result.put("qrCodeImage", base64QrCode);
//                result.put("content", content);
//                result.put("currentData", currentData);
//
//                logger.debug("返回当前数据二维码");
//            } else {
//                // 没有数据，返回空白状态
//                result.put("hasData", false);
//                result.put("qrCodeImage", "");
//                result.put("content", "");
//                result.put("currentData", null);
//
////                logger.debug("返回空白状态");
//            }
//
//            return AjaxResult.success("获取成功", result);
//
//        } catch (Exception e) {
//            logger.error("获取二维码失败", e);
//            return AjaxResult.error("获取二维码失败: " + e.getMessage());
//        }
//    }
    /**
     * 获取当前二维码（队列当前处理数据）
     */
    @GetMapping("/getCurrentQrCode")
    public AjaxResult getCurrentQrCode() {
        try {
            Map<String, Object> currentData = qrCodeQueueManager.getCurrentProcessingData();

            Map<String, Object> result = new HashMap<>();
            result.put("timestamp", System.currentTimeMillis());

            if (currentData != null) {
                // 创建 ObjectMapper 用于 JSON 序列化
                ObjectMapper objectMapper = new ObjectMapper();

                // 创建纯净数据
                Map<String, Object> cleanData = new HashMap<>(currentData);
                cleanData.remove("_dataId");
                cleanData.remove("_refreshCount");
                cleanData.remove("_failReason");

                // 使用 ObjectMapper 正确序列化为 JSON 字符串
                String content = objectMapper.writeValueAsString(cleanData);
                String base64QrCode = QRCodeUtil.createCodeToBase64(content, 300, 300, "png");

                result.put("hasData", true);
                result.put("qrCodeImage", base64QrCode);
                result.put("content", content);
                result.put("currentData", currentData);

                logger.debug("返回当前数据二维码");
            } else {
                // 没有数据，返回空白状态
                result.put("hasData", false);
                result.put("qrCodeImage", "");
                result.put("content", "");
                result.put("currentData", null);
            }

            return AjaxResult.success("获取成功", result);

        } catch (Exception e) {
            logger.error("获取二维码失败", e);
            return AjaxResult.error("获取二维码失败: " + e.getMessage());
        }
    }

    /**
     * 获取TCP服务器状态
     */
//    @GetMapping("/getTcpStatus")
//    public AjaxResult getTcpStatus() {
//        try {
//            Map<String, Object> status = tcpSignalReceiver.getStatus();
//            status.put("timestamp", System.currentTimeMillis());
//            return AjaxResult.success("状态获取成功", status);
//        } catch (Exception e) {
//            logger.error("【获取TCP状态】失败", e);
//            return AjaxResult.error("获取状态失败: " + e.getMessage());
//        }
//    }

    /**
     * 手动触发扫描成功（用于测试）
     */
    @PostMapping("/manualNext")
    public AjaxResult manualNext() {
        try {
            successSignalHandler.manualTrigger();
            return AjaxResult.success("手动成功信号已触发");
        } catch (Exception e) {
            logger.error("【手动触发】失败", e);
            return AjaxResult.error("手动触发失败: " + e.getMessage());
        }
    }

    /**
     * 清空数据队列
     */
    @PreAuthorize("@ss.hasPermi('smsQrCode:clearQueue')")
    @PostMapping("/clearQueue")
    public AjaxResult clearQueue() {
        try {
            qrCodeQueueManager.clearAllQueues();
            return AjaxResult.success("数据队列已清空");
        } catch (Exception e) {
            logger.error("【清空队列】失败", e);
            return AjaxResult.error("清空队列失败: " + e.getMessage());
        }
    }

    /**
     * 重启 GPIO 信号监控
     */
    @PostMapping("/restartSignalMonitor")
    public AjaxResult restartSignalMonitor() {
        try {
            gpioSignalMonitor.restartMonitor();

            Map<String, Object> result = new HashMap<>(gpioSignalMonitor.getStatus());
            result.put("message", "GPIO 信号监控重启成功");
            result.put("timestamp", System.currentTimeMillis());

            return AjaxResult.success("GPIO 信号监控重启成功", result);
        } catch (Exception e) {
            logger.error("【重启 GPIO 监控】失败", e);
            return AjaxResult.error("重启 GPIO 监控失败: " + e.getMessage());
        }
    }

    /**
     * @deprecated 请改用 {@link #restartSignalMonitor()}
     */
    @Deprecated
    @PostMapping("/restartTcpServer")
    public AjaxResult restartTcpServer(@RequestParam(defaultValue = "1919") int port) {
        logger.warn("restartTcpServer 已废弃，port={} 将被忽略，请改用 restartSignalMonitor", port);
        return restartSignalMonitor();
    }

    /**
     * 清空存储的数据
     */
    @PostMapping("/clearQrCode")
    public AjaxResult clearQrCode() {
        latestQrData.clear();
        return AjaxResult.success("数据已清空");
    }



    /**
     * 检查系统状态
     */
    @GetMapping("/checkStatus")
    public AjaxResult checkStatus() {
        try {
            Map<String, Object> status = new HashMap<>(gpioSignalMonitor.getStatus());
            status.put("hasCurrentData", qrCodeQueueManager.getCurrentProcessingData() != null);
            status.put("timestamp", System.currentTimeMillis());

            return AjaxResult.success("状态检查成功", status);
        } catch (Exception e) {
            return AjaxResult.error("状态检查失败: " + e.getMessage());
        }
    }
    /**
     * 接收数据接口
     */
//    @PostMapping("/receiveData")
//    public Map<String, Object> receiveData(@RequestBody Map<String, String> params) {
//        try {
//            Map<String, Object> result = new HashMap<>();
//
//            // 验证必需字段
//            if (params == null || !params.containsKey("content") || !params.containsKey("receivePhone")) {
//                result.put("code", 1);
//                result.put("msg", "缺少必需参数: content 或 receivePhone");
//                return result;
//            }
//
//            String content = params.get("content");
//            String receivePhone = params.get("receivePhone");
//
//            // 打印接收到的数据
//            logger.info("【接收数据】接收到运维平台数据 - 内容: {}, 手机号: {}", content, receivePhone);
//
//            // 构建处理数据
//            Map<String, Object> processData = new HashMap<>(params);
////            processData.put("content", content);
////            processData.put("receivePhone", receivePhone);
////            processData.put("receiveTime", System.currentTimeMillis());
////            processData.put("source", "运维管理平台");
//
//            // 添加到队列一
//            qrCodeQueueManager.addToQueue1(processData);
//
//            // 确保队列处理已启动
//            qrCodeQueueManager.startProcessing();
//
//            logger.info("✅ 数据已存入队列一，当前队列大小: {}", qrCodeQueueManager.getQueue1Size());
//            logger.info("队列一数据：{}",processData);
//
//            // 返回成功响应
//            result.put("code", 0);
//            result.put("msg", "成功");
////            result.put("timestamp", System.currentTimeMillis());
//
//            return result;
//
//        } catch (Exception e) {
//            logger.error("【接收数据】处理失败", e);
//            Map<String, Object> errorResult = new HashMap<>();
//            errorResult.put("code", 500);
//            errorResult.put("msg", "数据接收失败: " + e.getMessage());
//            return errorResult;
//        }
//    }
    /**
     * 接收数据接口 - 确保自动启动
     */
    @PostMapping("/receiveData")
//    @Anonymous //允许匿名访问
    public Map<String, Object> receiveData(@RequestBody Map<String, String> params) {
        try {
            Map<String, Object> result = new HashMap<>();

            // 验证必需字段
            if (params == null || !params.containsKey("content") || !params.containsKey("receivePhone")) {
                result.put("code", 1);
                result.put("msg", "缺少必需参数: content 或 receivePhone");
                return result;
            }

            String content = params.get("content");
            String receivePhone = params.get("receivePhone");

            logger.info("【接收数据】接收到运维平台数据 - 内容: {}, 手机号: {}", content, receivePhone);

            // 构建处理数据
            Map<String, Object> processData = new HashMap<>(params);

            // 强制启动队列处理（双重保障）
            qrCodeQueueManager.startProcessing();

            // 添加到队列一
            qrCodeQueueManager.addToQueue1(processData);

            logger.info("✅ 数据已存入队列一，当前队列大小: {}", qrCodeQueueManager.getQueue1Size());

            // 返回成功响应
            result.put("code", 0);
            result.put("msg", "成功");

            // 记录数据接收统计
            statisticsService.recordDataReceive();

            return result;

        } catch (Exception e) {
            logger.error("【接收数据】处理失败", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("code", 500);
            errorResult.put("msg", "数据接收失败: " + e.getMessage());
            return errorResult;
        }
    }

    /**
     * 导出队列二数据（未成功数据）为JSON文件
     */
//    @PostMapping("/exportFailedData")
//    public ResponseEntity<Resource> exportFailedData() {
//        try {
//            // ✅ 直接获取原始数据
//            List<Map<String, Object>> failedData = qrCodeQueueManager.exportQueue2Data();
//
//            logger.info("【导出】导出未成功数据为JSON文件，数量: {}", failedData.size());
//
//            // ✅ 直接序列化原始数据，不做任何修改
//            ObjectMapper objectMapper = new ObjectMapper();
//            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
//            String jsonContent = objectMapper.writeValueAsString(failedData);
//
//            // 创建临时文件
//            String fileName = "failed_data_" + System.currentTimeMillis() + ".json";
//            File tempFile = File.createTempFile("failed_data_", ".json");
//            Files.write(tempFile.toPath(), jsonContent.getBytes(StandardCharsets.UTF_8));
//
//            // 创建Resource
//            Resource resource = new InputStreamResource(Files.newInputStream(tempFile.toPath()));
//
//            // 设置响应头
//            HttpHeaders headers = new HttpHeaders();
//            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
//            headers.add(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
//
//            // 添加文件删除钩子
//            tempFile.deleteOnExit();
//
//            logger.info("【导出】JSON文件生成成功: {}, 数据条数: {}", fileName, failedData.size());
//
//            return ResponseEntity.ok()
//                    .headers(headers)
//                    .contentLength(Files.size(tempFile.toPath()))
//                    .body(resource);
//
//        } catch (Exception e) {
//            logger.error("【导出】导出失败数据为JSON文件失败", e);
//            throw new RuntimeException("导出失败: " + e.getMessage());
//        }
//    }
    @PreAuthorize("@ss.hasPermi('smsQrCode:exportFailedData')")
    @PostMapping("/exportFailedData")
    public ResponseEntity<Resource> exportFailedData() {
        try {
            List<Map<String, Object>> failedData = qrCodeQueueManager.exportQueue2Data();

            logger.info("【导出】导出未成功数据为JSON文件，数量: {}", failedData.size());

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

            // ✅ 直接序列化为JSON数组（自动处理逗号分隔）
            String jsonContent = objectMapper.writeValueAsString(failedData);

            // 创建临时文件
            String fileName = "failed_data_" + System.currentTimeMillis() + ".json";
            File tempFile = File.createTempFile("failed_data_", ".json");
            Files.write(tempFile.toPath(), jsonContent.getBytes(StandardCharsets.UTF_8));

            // 创建Resource
            Resource resource = new InputStreamResource(Files.newInputStream(tempFile.toPath()));

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");

            // 添加文件删除钩子
            tempFile.deleteOnExit();

            logger.info("【导出】JSON文件生成成功: {}, 数据条数: {}", fileName, failedData.size());

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(Files.size(tempFile.toPath()))
                    .body(resource);

        } catch (Exception e) {
            logger.error("【导出】导出失败数据为JSON文件失败", e);
            throw new RuntimeException("导出失败: " + e.getMessage());
        }
    }


    /**
     * 获取队列状态
     */
    @GetMapping("/queueStatus")
    public AjaxResult getQueueStatus() {
        try {
            Map<String, Object> status = qrCodeQueueManager.getQueueStatus();
            return AjaxResult.success("获取状态成功", status);
        } catch (Exception e) {
            logger.error("获取队列状态失败", e);
            return AjaxResult.error("获取状态失败: " + e.getMessage());
        }
    }

    /**
     * 启动队列处理
     */
    @PostMapping("/startQueue")
    public AjaxResult startQueue() {
        try {
            qrCodeQueueManager.startProcessing();
            return AjaxResult.success("队列处理已启动");
        } catch (Exception e) {
            logger.error("启动队列处理失败", e);
            return AjaxResult.error("启动失败: " + e.getMessage());
        }
    }

    /**
     * 停止队列处理
     */
    @PostMapping("/stopQueue")
    public AjaxResult stopQueue() {
        try {
            qrCodeQueueManager.stopProcessing();
            return AjaxResult.success("队列处理已停止");
        } catch (Exception e) {
            logger.error("停止队列处理失败", e);
            return AjaxResult.error("停止失败: " + e.getMessage());
        }
    }

    /**
     * 清空所有队列
     */
    @PreAuthorize("@ss.hasPermi('smsQrCode:clearAllQueue')")
    @PostMapping("/clearAllQueues")
    public AjaxResult clearAllQueues() {
        try {
            qrCodeQueueManager.clearAllQueues();
            return AjaxResult.success("所有队列已清空");
        } catch (Exception e) {
            logger.error("清空队列失败", e);
            return AjaxResult.error("清空失败: " + e.getMessage());
        }
    }



    /**
     * 获取所有队列状态（用于前端显示）
     */
    @GetMapping("/getAllQueueStatus")
    public AjaxResult getAllQueueStatus() {
        try {
            Map<String, Object> status = new HashMap<>();

            // 基础状态
            status.putAll(qrCodeQueueManager.getQueueStatus());

            // 队列一数据
            status.put("queue1Data", qrCodeQueueManager.getQueue1AllData());

            // 队列二数据
            status.put("queue2Data", qrCodeQueueManager.getQueue2AllData());

            // 当前处理数据详情
            Map<String, Object> currentData = qrCodeQueueManager.getCurrentProcessingData();
            status.put("currentDataDetail", currentData);

            return AjaxResult.success("获取队列状态成功", status);

        } catch (Exception e) {
            logger.error("获取队列状态失败", e);
            return AjaxResult.error("获取状态失败: " + e.getMessage());
        }
    }


    /**
     * 导出队列二数据并自动删除
     */
    @PreAuthorize("@ss.hasPermi('smsQrCode:exportAndClearFiledData')")
    @PostMapping("/exportAndClearFailedData")
    public AjaxResult exportAndClearFailedData() {
        try {
            // 导出数据
            List<Map<String, Object>> failedData = qrCodeQueueManager.exportQueue2Data();

            Map<String, Object> result = new HashMap<>();
            result.put("failedData", failedData);
            result.put("exportCount", failedData.size());
            result.put("exportTime", System.currentTimeMillis());

            logger.info("【导出】导出未成功数据，数量: {}，导出后自动清空队列二", failedData.size());

            return AjaxResult.success("导出成功，数据已自动删除", result);

        } catch (Exception e) {
            logger.error("【导出】导出失败数据失败", e);
            return AjaxResult.error("导出失败: " + e.getMessage());
        }
    }

    /**
     * 导出指定数据（可选择部分导出）
     */
    @PostMapping("/exportSelectedData")
    public AjaxResult exportSelectedData(@RequestBody List<String> dataIds) {
        try {
            List<Map<String, Object>> exportedData = new ArrayList<>();
            List<Map<String, Object>> queue2Data = qrCodeQueueManager.getQueue2AllData();

            // 筛选要导出的数据
            for (Map<String, Object> data : queue2Data) {
                String dataId = (String) data.get("_dataId");
                if (dataIds.contains(dataId)) {
                    exportedData.add(data);
                    // 从队列二中删除
                    qrCodeQueueManager.removeFromQueue2(dataId);
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("exportedData", exportedData);
            result.put("exportCount", exportedData.size());
            result.put("exportTime", System.currentTimeMillis());

            logger.info("【选择性导出】导出指定数据，数量: {}", exportedData.size());

            return AjaxResult.success("选择性导出成功", result);

        } catch (Exception e) {
            logger.error("【选择性导出】失败", e);
            return AjaxResult.error("选择性导出失败: " + e.getMessage());
        }
    }

    /**
     * 应用启动时自动初始化队列处理
     */
    @PostConstruct
    public void init() {
        try {
            // 等待其他组件初始化完成
            Thread.sleep(3000);
            qrCodeQueueManager.startProcessing();
            logger.info("🔧 应用启动，自动初始化队列处理");
        } catch (Exception e) {
            logger.error("应用启动初始化失败", e);
        }
    }


    /**
     * 统计接收数据量和生成二维码数量
     */
    /**
     * 获取统计数据的接口
     */
    /**
     * 统计接收数据量和生成二维码数量
     * @param year
     * @return
     */
    @GetMapping("/getStatistics")
    public AjaxResult getStatistics(@RequestParam(required = false) Integer year) {
        try {
            if (year == null) {
                Calendar cal = Calendar.getInstance();
                year = cal.get(Calendar.YEAR);
            }

            List<Map<String, Object>> monthlyStats = statisticsService.getMonthlyStatistics(year);
            List<Integer> availableYears = statisticsService.getAvailableYears();

            Map<String, Object> result = new HashMap<>();
            result.put("monthlyStats", monthlyStats);
            result.put("availableYears", availableYears);
            result.put("currentYear", year);

            return AjaxResult.success("获取统计成功", result);
        } catch (Exception e) {
            logger.error("获取统计数据失败", e);
            return AjaxResult.error("获取统计失败: " + e.getMessage());
        }
    }



    //定时清理数据*************************************************

    /**
     * 清理已完成数据（手动触发）
     */
    @PreAuthorize("@ss.hasPermi('smsQrCode:cleanupCompleted')")
    @PostMapping("/cleanupCompleted")
    public AjaxResult cleanupCompletedData(@RequestBody Map<String, Object> params) {
        try {
            logger.info("🧹 手动清理已完成数据，参数: {}", params);

            // 获取保留天数参数，默认为7天
            int retentionDays = 7;
            if (params.containsKey("retentionDays")) {
                try {
                    retentionDays = Integer.parseInt(params.get("retentionDays").toString());
                    if (retentionDays < 0) {
                        return AjaxResult.error("保留天数不能为负数");
                    }
                } catch (NumberFormatException e) {
                    return AjaxResult.error("保留天数格式错误");
                }
            }

            // 执行清理
            CleanupResult result = dataCleanupService.cleanupCompletedData(retentionDays);

            // 返回结果
            Map<String, Object> response = new HashMap<>();
            response.put("deletedCount", result.getDeletedCount());
            response.put("retentionDays", result.getRetentionDays());
            response.put("cutoffDate", result.getCutoffDate());
            response.put("remainingCount", result.getRemainingCount());
            response.put("message", result.getMessage());

            return AjaxResult.success("清理已完成数据成功", response);

        } catch (Exception e) {
            logger.error("❌ 清理已完成数据异常", e);
            return AjaxResult.error("清理失败: " + e.getMessage());
        }
    }

    /**
     * 查看已完成数据统计
     */
    @PreAuthorize("@ss.hasPermi('smsQrCode:viewCompletedStats')")
    @GetMapping("/completedStats")
    public AjaxResult getCompletedStats() {
        try {
            CompletedDataStats stats = dataCleanupService.getCompletedDataStats();

            if (stats == null) {
                return AjaxResult.error("获取统计信息失败");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("totalCompleted", stats.getTotalCount());
            result.put("lastWeekCount", stats.getLastWeekCount());
            result.put("recentData", stats.getRecentData());
            result.put("timestamp", System.currentTimeMillis());

            return AjaxResult.success("获取已完成数据统计成功", result);

        } catch (Exception e) {
            logger.error("❌ 获取已完成数据统计异常", e);
            return AjaxResult.error("获取统计失败: " + e.getMessage());
        }
    }

    /**
     * 查看即将被清理的已完成数据预览
     */
    @PreAuthorize("@ss.hasPermi('smsQrCode:viewCleanupPreview')")
    @GetMapping("/cleanupPreview")
    public AjaxResult getCleanupPreview(@RequestParam(defaultValue = "7") int retentionDays) {
        try {
            if (retentionDays < 0) {
                return AjaxResult.error("保留天数不能为负数");
            }

            logger.info("🔍 查看即将清理的已完成数据，保留天数: {}", retentionDays);

            // 计算截止日期
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, -retentionDays);
            Date cutoffDate = calendar.getTime();

            // 查询即将被清理的数据
            var dataToClean = qrCodeQueueManager.selectCompletedDataBeforeDate(cutoffDate);

            // 统计信息
            Map<String, Object> result = new HashMap<>();
            result.put("count", dataToClean.size());
            result.put("retentionDays", retentionDays);
            result.put("cutoffDate", cutoffDate);
            result.put("totalCompleted", qrCodeQueueManager.countCompletedData());

            // 只返回前50条数据预览
            int previewSize = Math.min(dataToClean.size(), 50);
            result.put("previewSize", previewSize);
            result.put("previewData", dataToClean.stream()
                    .limit(50)
                    .collect(Collectors.toList()));

            return AjaxResult.success("即将清理的已完成数据预览", result);

        } catch (Exception e) {
            logger.error("❌ 获取清理预览异常", e);
            return AjaxResult.error("获取预览失败: " + e.getMessage());
        }
    }

    /**
     * 获取数据清理计划信息
     */
    @GetMapping("/cleanupSchedule")
    public AjaxResult getCleanupSchedule() {
        try {
            Map<String, Object> schedule = new HashMap<>();
            schedule.put("autoCleanupEnabled", true);
            schedule.put("cronExpression", "0 0 3 ? * MON"); // 每周一凌晨3点
            schedule.put("retentionDays", 7);
            schedule.put("targetStatus", "3（已完成）");
            schedule.put("description", "每周一凌晨3点自动清理7天前的已完成数据");

            // 计算下次执行时间
            Calendar next = Calendar.getInstance();
            next.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            next.set(Calendar.HOUR_OF_DAY, 3);
            next.set(Calendar.MINUTE, 0);
            next.set(Calendar.SECOND, 0);
            next.set(Calendar.MILLISECOND, 0);

            // 如果当前时间已经超过本周一凌晨3点，则计算下周一
            if (next.getTime().before(new Date())) {
                next.add(Calendar.DAY_OF_WEEK, 7);
            }

            schedule.put("nextExecutionTime", next.getTime());
            schedule.put("nextExecutionTimeStr",
                    String.format("%tF %tT", next.getTime(), next.getTime()));

            return AjaxResult.success("数据清理计划", schedule);

        } catch (Exception e) {
            logger.error("❌ 获取清理计划异常", e);
            return AjaxResult.error("获取清理计划失败");
        }
    }

}


