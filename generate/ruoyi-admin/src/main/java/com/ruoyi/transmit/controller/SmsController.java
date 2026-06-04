package com.ruoyi.transmit.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.transmit.entity.CleanupResult;
import com.ruoyi.transmit.entity.CompletedDataStats;
import com.ruoyi.transmit.display.FramebufferQrDisplayService;
import com.ruoyi.transmit.display.QrDisplayProperties;
import com.ruoyi.transmit.handler.QrCodeWebSocketHandler;
import com.ruoyi.transmit.mapper.QrCodeQueueManager;
import com.ruoyi.transmit.signal.SuccessSignalHandler;
import com.ruoyi.transmit.signal.gpio.GpioSignalMonitor;
import com.ruoyi.transmit.service.DataCleanupService;
import com.ruoyi.transmit.service.QrCodeQueueService;
import com.ruoyi.transmit.service.StatisticsService;
import com.ruoyi.transmit.utils.QRCodeUtil;
import com.ruoyi.transmit.utils.QrContentUtil;
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
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
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

    /** 未接入运维平台时设为 true；接入后改 false 恢复校验 POST 参数 */
    private static final boolean RECEIVE_DATA_TEST_MODE = true;

    /** 测试模式下自动入队间隔（秒） */
    private static final long TEST_AUTO_GENERATE_INTERVAL_SECONDS = 2;

    private static final String[] CHINESE_PLACE_NAMES = {
            "北京", "上海", "广州", "深圳", "杭州", "南京", "苏州", "成都", "重庆", "武汉",
            "西安", "天津", "青岛", "大连", "厦门", "福州", "济南", "郑州", "长沙", "昆明",
            "贵阳", "南宁", "海口", "三亚", "拉萨", "乌鲁木齐", "兰州", "银川", "西宁", "呼和浩特",
            "哈尔滨", "长春", "沈阳", "石家庄", "太原", "合肥", "南昌", "宁波", "无锡", "珠海",
            "桂林", "丽江", "大理", "敦煌", "张家界", "黄山", "九寨沟", "峨眉山", "泰山", "华山"
    };

    private static final Random TEST_DATA_RANDOM = new Random();


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

    @Autowired
    private QrDisplayProperties qrDisplayProperties;

    @Autowired
    private FramebufferQrDisplayService framebufferQrDisplayService;


    private Map<String, Object> latestQrCodeData = new ConcurrentHashMap<>();

    private AtomicLong lastUpdateTime = new AtomicLong(0);
    private final Object lock = new Object();

    private ScheduledExecutorService testDataScheduler;
    private ScheduledFuture<?> testDataAutoTask;


    /**
     * 生成二维码API
     * @param data 接收的JSON数据
     * @param response HTTP响应
     */
    @PostMapping("/generate")
    public void generateQrCode(@RequestBody Map<String, Object> data, HttpServletResponse response) {
        try {
            String content = StringUtils.format("{}", data);

            response.setContentType("image/png");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Pragma", "no-cache");

            QRCodeUtil.createCodeToStream(content, 300, 300, "png", response.getOutputStream());

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
            String content = StringUtils.format("{}", data);

            String base64QrCode = QRCodeUtil.createCodeToBase64(content,
                    300, 300, "png");
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
     * 自动生成二维码（从队列获取当前处理数据）
     */
    @PreAuthorize("@ss.hasPermi('smsQrCode:autoGenerate')")
    @PostMapping("/autoGenerate")
    public void autoGenerateQrCode(@RequestBody(required = false) Map<String, Object> data,
                                   HttpServletResponse response) {
        try {
            Map<String, Object> currentData = qrCodeQueueManager.getCurrentProcessingData();
            String content;

            ObjectMapper objectMapper = new ObjectMapper();

            logger.info("【自动生成】接收到的数据: {}", data);
            logger.info("【自动生成】队列当前数据: {}", currentData);

            if (currentData != null) {
                logger.info("【自动生成】currentData 类型: {}", currentData.getClass().getName());
                logger.info("【自动生成】currentData 键值: {}", currentData.keySet());

                if (currentData instanceof Map) {
                    Map<String, Object> cleanData = new HashMap<>((Map<String, Object>) currentData);
                    cleanData.keySet().removeIf(key -> key.startsWith("_"));

                    content = objectMapper.writeValueAsString(cleanData);
                    logger.info("【自动生成】使用队列当前数据生成二维码");
                } else {
                    content = currentData.toString();
                    logger.warn("【自动生成】currentData 不是 Map 类型，使用 toString()");
                }
            } else if (data != null && !data.isEmpty()) {
                logger.info("【自动生成】data 类型: {}", data.getClass().getName());

                Map<String, Object> cleanData = new HashMap<>(data);
                cleanData.keySet().removeIf(key -> key.startsWith("_"));
                content = objectMapper.writeValueAsString(cleanData);
                logger.info("【自动生成】使用传入数据生成二维码");
            } else {
                content = "{\"message\":\"暂无处理数据\"}";
                logger.info("【自动生成】无数据，生成默认二维码");
            }

            logger.info("【自动生成】最终二维码内容: {}", content);
            logger.info("【自动生成】内容长度: {}", content.length());
            logger.info("【自动生成】是否包含{{}: {}", content.contains("{"));
            logger.info("【自动生成】是否包含}}: {}", content.contains("}"));
            logger.info("【自动生成】内容前100字符: {}", content.substring(0, Math.min(content.length(), 100)));

            response.setContentType("image/png");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Pragma", "no-cache");

            QRCodeUtil.createCodeToStream(content, 300, 300, "png", response.getOutputStream());

            logger.info("【自动生成】二维码生成成功");

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

        try {
            logger.info("【自动生成Base64】接收到二维码生成请求，数据: {}", data);

            String content;
            if (data == null || data.isEmpty()) {
                content = "暂无数据";
                logger.warn("【自动生成Base64】接收到空数据，使用默认内容");
            } else {
                content = StringUtils.format("{}", data);
            }

            logger.info("【自动生成Base64】二维码内容: {}", content);

            String base64QrCode = QRCodeUtil.createCodeToBase64(content, 300, 300, "png");

            logger.info("【自动生成Base64】二维码生成成功，Base64长度: {}",
                    base64QrCode != null ? base64QrCode.length() : 0);

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

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            QRCodeUtil.createCodeToStream(content, 300, 300, "png", outputStream);

            String base64Image = Base64.getEncoder().encodeToString(outputStream.toByteArray());

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

            response.setContentType("image/png");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Pragma", "no-cache");

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
     * @return 包含多个二维码的结果
     */
    @PostMapping("/batchAutoGenerate")
    public AjaxResult batchAutoGenerateQrCode(@RequestBody Map<String, Object> requestData) {
        try {
            logger.info("【批量自动生成】接收到批量生成请求，数据: {}", requestData);

            Map<String, Object> result = new ConcurrentHashMap<>();
            result.put("timestamp", System.currentTimeMillis());
            result.put("total", 0);

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
     * 获取当前二维码（队列当前处理数据）
     */
    @GetMapping("/getCurrentQrCode")
    public AjaxResult getCurrentQrCode() {
        try {
            Map<String, Object> currentData = qrCodeQueueManager.getCurrentProcessingData();

            Map<String, Object> result = new HashMap<>();
            result.put("timestamp", System.currentTimeMillis());

            if (currentData != null) {
                ObjectMapper objectMapper = new ObjectMapper();

                Map<String, Object> cleanData = new HashMap<>(currentData);
                cleanData.remove("_dataId");
                cleanData.remove("_refreshCount");
                cleanData.remove("_failReason");

                String content = objectMapper.writeValueAsString(cleanData);
                String base64QrCode = QRCodeUtil.createCodeToBase64(content, 300, 300, "png");

                result.put("hasData", true);
                result.put("qrCodeImage", base64QrCode);
                result.put("content", content);
                result.put("currentData", currentData);

                logger.debug("返回当前数据二维码");
            } else {
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
     * 二维码显示配置（MIPI framebuffer / 浏览器）
     */
    @GetMapping("/displayConfig")
    public AjaxResult displayConfig()
    {
        Map<String, Object> config = new HashMap<>();
        config.put("mode", qrDisplayProperties.getMode());
        config.put("enabled", qrDisplayProperties.isEnabled());
        config.put("framebuffer", qrDisplayProperties.getFramebuffer());
        config.put("framebufferMode", qrDisplayProperties.isFramebufferMode());
        return AjaxResult.success(config);
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
     * 测试模式：生成随机中国地名作为二维码 content
     */
    private Map<String, String> buildTestReceiveParams() {
        String place = CHINESE_PLACE_NAMES[TEST_DATA_RANDOM.nextInt(CHINESE_PLACE_NAMES.length)];
        String phone = "138" + String.format("%08d", TEST_DATA_RANDOM.nextInt(100_000_000));

        Map<String, String> data = new HashMap<>();
        data.put("content", place);
        data.put("receivePhone", phone);
        data.put("systemId", "test-mode");
        return data;
    }

    /**
     * 测试模式：生成一条模拟数据并入队
     */
    private void enqueueTestReceiveData(String source) {
        Map<String, String> params = buildTestReceiveParams();
        logger.info("【接收数据】【测试模式】【{}】模拟数据 - 内容: {}, 手机号: {}",
                source, params.get("content"), params.get("receivePhone"));

        Map<String, Object> processData = new HashMap<>(params);
        qrCodeQueueManager.startProcessing();
        qrCodeQueueManager.addToQueue1(processData);
        statisticsService.recordDataReceive();

        logger.info("✅ 数据已存入队列一，当前队列大小: {}", qrCodeQueueManager.getQueue1Size());
    }

    private void startTestAutoGenerate() {
        if (!RECEIVE_DATA_TEST_MODE || testDataAutoTask != null) {
            return;
        }
        testDataScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "SmsTestDataAutoGenerate");
            t.setDaemon(true);
            return t;
        });
        testDataAutoTask = testDataScheduler.scheduleAtFixedRate(
                () -> {
                    try {
                        enqueueTestReceiveData("自动");
                    } catch (Exception e) {
                        logger.error("【接收数据】【测试模式】自动生成失败", e);
                    }
                },
                0,
                TEST_AUTO_GENERATE_INTERVAL_SECONDS,
                TimeUnit.SECONDS);
        logger.info("【接收数据】【测试模式】已启动自动入队，间隔 {} 秒", TEST_AUTO_GENERATE_INTERVAL_SECONDS);
    }

    private void stopTestAutoGenerate() {
        if (testDataAutoTask != null) {
            testDataAutoTask.cancel(false);
            testDataAutoTask = null;
        }
        if (testDataScheduler != null) {
            testDataScheduler.shutdown();
            testDataScheduler = null;
        }
    }

    /**
     * 接收数据接口 - 确保自动启动
     * 测试模式（RECEIVE_DATA_TEST_MODE=true）下忽略 POST 体，自动生成随机中国地名入队
     */
    @PostMapping("/receiveData")
    public Map<String, Object> receiveData(@RequestBody(required = false) Map<String, String> params) {
        try {
            Map<String, Object> result = new HashMap<>();

            if (RECEIVE_DATA_TEST_MODE) {
                enqueueTestReceiveData("手动请求");
            } else {
                if (params == null || !params.containsKey("content") || !params.containsKey("receivePhone")) {
                    result.put("code", 1);
                    result.put("msg", "缺少必需参数: content 或 receivePhone");
                    return result;
                }
                logger.info("【接收数据】接收到运维平台数据 - 内容: {}, 手机号: {}",
                        params.get("content"), params.get("receivePhone"));

                Map<String, Object> processData = new HashMap<>(params);
                qrCodeQueueManager.startProcessing();
                qrCodeQueueManager.addToQueue1(processData);
                statisticsService.recordDataReceive();
                logger.info("✅ 数据已存入队列一，当前队列大小: {}", qrCodeQueueManager.getQueue1Size());
            }

            result.put("code", 0);
            result.put("msg", "成功");
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
    @PreAuthorize("@ss.hasPermi('smsQrCode:exportFailedData')")
    @PostMapping("/exportFailedData")
    public ResponseEntity<Resource> exportFailedData() {
        try {
            List<Map<String, Object>> failedData = qrCodeQueueManager.exportQueue2Data();

            logger.info("【导出】导出未成功数据为JSON文件，数量: {}", failedData.size());

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

            String jsonContent = objectMapper.writeValueAsString(failedData);

            String fileName = "failed_data_" + System.currentTimeMillis() + ".json";
            File tempFile = File.createTempFile("failed_data_", ".json");
            Files.write(tempFile.toPath(), jsonContent.getBytes(StandardCharsets.UTF_8));

            Resource resource = new InputStreamResource(Files.newInputStream(tempFile.toPath()));

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");

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

            status.putAll(qrCodeQueueManager.getQueueStatus());

            status.put("queue1Data", qrCodeQueueManager.getQueue1AllData());

            status.put("queue2Data", qrCodeQueueManager.getQueue2AllData());

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

            for (Map<String, Object> data : queue2Data) {
                String dataId = (String) data.get("_dataId");
                if (dataIds.contains(dataId)) {
                    exportedData.add(data);
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
            Thread.sleep(3000);
            qrCodeQueueManager.startProcessing();
            logger.info("🔧 应用启动，自动初始化队列处理");
            if (RECEIVE_DATA_TEST_MODE) {
                startTestAutoGenerate();
            }
        } catch (Exception e) {
            logger.error("应用启动初始化失败", e);
        }
    }

    @PreDestroy
    public void destroy() {
        stopTestAutoGenerate();
    }


    /**
     * 获取统计数据（接收数据量与二维码生成量）
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


    /**
     * 清理已完成数据（手动触发）
     */
    @PreAuthorize("@ss.hasPermi('smsQrCode:cleanupCompleted')")
    @PostMapping("/cleanupCompleted")
    public AjaxResult cleanupCompletedData(@RequestBody Map<String, Object> params) {
        try {
            logger.info("🧹 手动清理已完成数据，参数: {}", params);

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

            CleanupResult result = dataCleanupService.cleanupCompletedData(retentionDays);

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

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, -retentionDays);
            Date cutoffDate = calendar.getTime();

            List<Map<String, Object>> dataToClean = qrCodeQueueManager.selectCompletedDataBeforeDate(cutoffDate);

            Map<String, Object> result = new HashMap<>();
            result.put("count", dataToClean.size());
            result.put("retentionDays", retentionDays);
            result.put("cutoffDate", cutoffDate);
            result.put("totalCompleted", qrCodeQueueManager.countCompletedData());

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
            schedule.put("cronExpression", "0 0 3 ? * MON");
            schedule.put("retentionDays", 7);
            schedule.put("targetStatus", "3（已完成）");
            schedule.put("description", "每周一凌晨3点自动清理7天前的已完成数据");

            Calendar next = Calendar.getInstance();
            next.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            next.set(Calendar.HOUR_OF_DAY, 3);
            next.set(Calendar.MINUTE, 0);
            next.set(Calendar.SECOND, 0);
            next.set(Calendar.MILLISECOND, 0);

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


