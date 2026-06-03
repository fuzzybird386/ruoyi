//package com.ruoyi.accept.controller;
//
//
//import com.alibaba.fastjson2.JSONObject;
//import com.ruoyi.accept.entity.DataQueue;
//import com.ruoyi.accept.processor.DataProcessor;
//import com.ruoyi.accept.service.IDataQueueService;
//import com.ruoyi.common.core.controller.BaseController;
//import com.ruoyi.common.core.domain.AjaxResult;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import com.ruoyi.accept.server.TcpServer;
//
//import java.net.Socket;
//import java.util.HashMap;
//
//
//import java.util.Map;
//import java.util.concurrent.BlockingQueue;
//import java.util.concurrent.ConcurrentMap;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import com.ruoyi.accept.server.TcpServer;
//import org.yaml.snakeyaml.events.Event;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//
///**
// * 数据API控制器
// */
//@RestController
//@RequestMapping("/api/data")
//public class DataApiController extends BaseController {
//    // 添加日志记录器
//    private static final Logger logger = LoggerFactory.getLogger(DataApiController.class);
//
//    @Autowired
//    private TcpServer tcpServer;
//
//    @Autowired
//    private DataProcessor dataProcessor;
//
//    @Autowired
//    private IDataQueueService dataQueueService;
//
//    /**
//     * 获取TCP服务器状态
//     */
//    @GetMapping("/server/status")
//    public AjaxResult getServerStatus() {
//        return AjaxResult.success("TCP服务器状态", tcpServer.isRunning());
//    }
//
//    /**
//     * 获取所有数据ID列表
//     */
//    @GetMapping("/list")
//    public AjaxResult getDataList() {
//        ConcurrentMap<String, String> allData = dataProcessor.getAllData();
//        return AjaxResult.success("数据列表", allData.keySet());
//    }
//
//    /**
//     * 根据数据ID获取JSON数据
//     */
//    @GetMapping("/{dataId}")
//    public AjaxResult getDataById(@PathVariable String dataId) {
//        String jsonData = dataProcessor.getReceivedData(dataId);
//        if (jsonData == null) {
//            return AjaxResult.error("数据不存在");
//        }
//        return AjaxResult.success("获取数据成功", jsonData);
//    }
//
//    /**
//     * 调用方调用此API获取数据并返回响应
//     */
//    @PostMapping("/receive")
//    public AjaxResult receiveData(@RequestBody(required = false) Map<String, Object> request) {
//        logger.info("🟢 API接口被调用，请求参数: {}", request);
//
//        String dataId = null;
//        Socket tcpSocket = null;
//        try {
////            String dataId = null;
////            Socket tcpSocket = null;
//
//
//            if (request != null && !request.isEmpty()) {
//                dataId = (String) request.get("dataId");
//                logger.info("📥 接收到dataId: {}", dataId);
//            }else {
//                logger.info("📥 请求体为空，将返回最新数据");
//            }
//
//            // 如果dataId为空，返回最新的数据
//            if (dataId == null || dataId.isEmpty()) {
//                logger.info("🔍 dataId为空，调用getLatestData方法");
//                return getLatestData();
//            }
//
//            String jsonData = dataProcessor.getReceivedData(dataId);
//            logger.info("📊 根据dataId获取数据结果: dataId={}, dataExists={}", dataId, jsonData != null);
//            if (jsonData == null) {
//                logger.warn("❌ 未找到对应数据: dataId={}", dataId);
//                return AjaxResult.error("1001", "应用系统未注册");
//            }
//
//
//            // 获取对应的TCP Socket
//            tcpSocket = dataProcessor.getTcpSocketByDataId(dataId);
//            logger.info("🔗 查找TCP Socket结果: dataId={}, socketExists={}", dataId, tcpSocket != null);
//
//            // 构建成功响应 - 使用HashMap
//            Map<String, Object> successResponse = new HashMap<>();
//            successResponse.put("success", true);
//            successResponse.put("code", "0");
//            successResponse.put("message", "成功获取数据");
//            successResponse.put("data", jsonData);
//
//            // 根据响应发送确认信息给TCP发送方
////            dataProcessor.sendConfirmationIfNeeded("{\"success\":true,\"code\":\"0\"}");
//
//            // TCP 确认发送（增加失败处理）
////            try {
////                dataProcessor.sendConfirmationIfNeeded("{\"success\":true,\"code\":\"0\"}");
////            } catch (Exception tcpEx) {
////                logger.error("TCP确认信息发送失败", tcpEx);
////                return AjaxResult.error("1005", "TCP确认信息发送失败");
////            }
//            // 根据响应发送确认信息给TCP发送方
//            if (tcpSocket != null) {
//                try {
//                    logger.info("🚀 准备发送TCP确认信息...");
//                    dataProcessor.sendConfirmationIfNeeded("{\"success\":true,\"code\":\"0\"}",tcpSocket);
//                    logger.info("✅ TCP确认信息发送成功，dataId: {}", dataId);
//                } catch (Exception tcpEx) {
//                    logger.error("❌ TCP确认信息发送失败，dataId: {}", dataId, tcpEx);
//                }
//            } else {
//                logger.warn("⚠️ 未找到对应的TCP Socket，dataId: {}", dataId);
//                // 可以记录详细的关联状态用于调试
////                dataProcessor.logSocketAssociationStatus();
//            }
//
//            logger.info("🎯 API调用完成，返回成功响应");
//            return AjaxResult.success(successResponse);
//
//        } catch (Exception e) {
//            logger.error("处理API请求异常", e);
//            return AjaxResult.error("1004", "服务端异常");
//        }
//    }
//
//    /**
//     * GET方式获取数据
//     */
//    @GetMapping("/receive")
//    public AjaxResult receiveDataGet(@RequestParam(required = false) String dataId) {
//        Socket tcpSocket = null;
//        try {
//            // 如果dataId为空，返回最新的数据
//            if (dataId == null || dataId.isEmpty()) {
//                return getLatestData();
//            }
//
//            String jsonData = dataProcessor.getReceivedData(dataId);
//            if (jsonData == null) {
//                return AjaxResult.error("1001", "应用系统未注册");
//            }
//
//            // 构建成功响应 - 使用HashMap兼容Java 8
//            Map<String, Object> successResponse = new HashMap<>();
//            successResponse.put("success", true);
//            successResponse.put("code", "0");
//            successResponse.put("message", "成功获取数据");
//            successResponse.put("data", jsonData);
//
//            // 根据响应发送确认信息给TCP发送方
//            dataProcessor.sendConfirmationIfNeeded("{\"success\":true,\"code\":\"0\"}",tcpSocket);
//
//            return AjaxResult.success(successResponse);
//
//        } catch (Exception e) {
//            logger.error("处理API请求异常", e);
//            return AjaxResult.error("1004", "服务端异常");
//        }
//    }
//    /**
//     * 获取最新的数据
//     */
//    private AjaxResult getLatestData() {
//        Socket tcpSocket = null;
//        String latestKey = null;
//
//        try {
//            logger.info("🔍 开始获取最新数据...");
//            ConcurrentMap<String, String> allData = dataProcessor.getAllData();
//            logger.info("📊 当前数据存储总数: {}", allData.size());
//
//            if (allData.isEmpty()) {
//                logger.warn("⚠️ 数据存储为空，无法获取最新数据");
//                return AjaxResult.error("1000", "请求报文为空");
//            }
//
//            // 获取最新的数据（根据key的时间戳）
//            latestKey = allData.keySet().stream()
//                    .sorted((k1, k2) -> k2.compareTo(k1)) // 逆序排序，获取最新的
//                    .findFirst()
//                    .orElse(null);
//
//            logger.info("🎯 找到最新数据Key: {}", latestKey);
//
//            if (latestKey != null) {
//                String jsonData = allData.get(latestKey);
//                logger.info("📦 获取到最新数据内容，长度: {}", jsonData.length());
//
//                // 获取对应的TCP Socket
//                tcpSocket = dataProcessor.getTcpSocketByDataId(latestKey);
//                logger.info("🔗 查找TCP Socket结果: dataId={}, socketExists={}", latestKey, tcpSocket != null);
//
//                // 构建成功响应
//                Map<String, Object> successResponse = new HashMap<>();
//                successResponse.put("success", true);
//                successResponse.put("code", "0");
//                successResponse.put("message", "成功获取最新数据");
//                successResponse.put("data", jsonData);
//                successResponse.put("dataId", latestKey);
//
//                // 根据响应发送确认信息给TCP发送方
//                if (tcpSocket != null) {
//                    try {
//                        logger.info("🚀 准备发送TCP确认信息给最新数据...");
//                        dataProcessor.sendConfirmationIfNeeded("{\"success\":true,\"code\":\"0\"}", tcpSocket);
//                        logger.info("✅ TCP确认信息发送成功，dataId: {}", latestKey);
//                    } catch (Exception tcpEx) {
//                        logger.error("❌ TCP确认信息发送失败，dataId: {}", latestKey, tcpEx);
//                    }
//                } else {
//                    logger.warn("⚠️ 未找到对应的TCP Socket，dataId: {}", latestKey);
//                    // 记录详细的关联状态用于调试
////                    dataProcessor.logSocketAssociationStatus();
//                }
//
//                logger.info("🎯 getLatestData调用完成，返回成功响应");
//                return AjaxResult.success(successResponse);
//            }
//
//            logger.warn("⚠️ 未找到最新数据Key");
//            return AjaxResult.error("1000", "请求报文为空");
//
//        } catch (Exception e) {
//            logger.error("❌ 获取最新数据异常", e);
//            return AjaxResult.error("1004", "服务端异常");
//        }
//    }
//
//    //****修改为消息队列发送***************************
//
//    /**
//     * 获取队列一数据（待发送数据）
//     */
////    @GetMapping("/queue1")
////    public AjaxResult getQueue1Data() {
////        List<Map<String, Object>> queueData = dataProcessor.getQueue1().stream()
////                .map(this::convertQueueDataToMap)
////                .collect(Collectors.toList());
////
////        Map<String, Object> result = new HashMap<>();
////        result.put("count", queueData.size());
////        result.put("data", queueData);
////
////        return AjaxResult.success("队列一数据", result);
////    }
//    @GetMapping("/queue1")
//    public AjaxResult getQueue1Data() {
//        try {
//            logger.info("🔍 开始获取队列一数据");
//
//            // 从数据库查询队列一数据
//            List<DataQueue> queue1Data = dataQueueService.selectByQueueType("QUEUE1");
//            logger.info("📊 从数据库查询到队列一数据: {} 条", queue1Data.size());
//
//            // 转换为前端需要的格式
//            List<Map<String, Object>> queueData = queue1Data.stream()
//                    .map(this::convertDataQueueToMap)
//                    .collect(Collectors.toList());
//
//            Map<String, Object> result = new HashMap<>();
//            result.put("count", queueData.size());
//            result.put("data", queueData);
//
//            return AjaxResult.success("队列一数据", result);
//        } catch (Exception e) {
//            logger.error("❌ 获取队列一数据异常", e);
//            return AjaxResult.error("获取队列一数据失败");
//        }
//    }
//
//    /**
//     * 获取队列二数据（发送失败数据）
//     */
////    @GetMapping("/queue2")
////    public AjaxResult getQueue2Data() {
////        try {
////            logger.info("🔍 开始获取队列二数据");
////            List<Map<String, Object>> queueData = dataProcessor.getQueue2().stream()
////                    .map(this::convertQueueDataToMap)
////                    .collect(Collectors.toList());
////            logger.info("失败队列数据获取完成，数量：{}",queueData.size());
////
////            Map<String, Object> result = new HashMap<>();
////            result.put("count", queueData.size());
////            result.put("data", queueData);
////
////            return AjaxResult.success("队列二数据", result);
////        }catch (Exception e){
////            logger.error("获取失败数据队列异常",e);
////            return AjaxResult.error("获取发送失败数据队列失败");
////        }
////    }
//    /**
//     * 获取队列二数据（发送失败数据） - 从数据库获取
//     */
//    @GetMapping("/queue2")
//    public AjaxResult getQueue2Data() {
//        try {
//            logger.info("🔍 开始获取队列二数据");
//
//            // 从数据库查询队列二数据
//            List<DataQueue> queue2Data = dataQueueService.selectByQueueType("QUEUE2");
//            logger.info("📊 从数据库查询到队列二数据: {} 条", queue2Data.size());
//
//            // 转换为前端需要的格式
//            List<Map<String, Object>> queueData = queue2Data.stream()
//                    .map(this::convertDataQueueToMap)
//                    .collect(Collectors.toList());
//
//            Map<String, Object> result = new HashMap<>();
//            result.put("count", queueData.size());
//            result.put("data", queueData);
//
//            return AjaxResult.success("队列二数据", result);
//        } catch (Exception e) {
//            logger.error("❌ 获取队列二数据异常", e);
//            return AjaxResult.error("获取队列二数据失败");
//        }
//    }
//
//    /**
//     * 手动导入数据到队列二
//     */
//    @PostMapping("/queue2/import")
//    public AjaxResult importToQueue2(@RequestBody Map<String, Object> request) {
//        try {
//            logger.info("🟢 开始手动导入数据，请求参数: {}", request);
//
//            String jsonData = (String) request.get("jsonData");
//            if (jsonData == null || jsonData.trim().isEmpty()) {
//                logger.warn("❌ JSON数据为空");
//                return AjaxResult.error("JSON数据不能为空");
//            }
//
//            // 验证JSON格式
//            try {
//                JSONObject.parseObject(jsonData);
//                logger.info("✅ JSON格式验证通过");
//            } catch (Exception e) {
//                logger.error("❌ JSON格式验证失败: {}", e.getMessage());
//                return AjaxResult.error("JSON格式错误: " + e.getMessage());
//            }
//
//            boolean success = dataProcessor.importToQueue2(jsonData);
//            if (success) {
//                logger.info("✅ 数据成功导入到队列二");
//                return AjaxResult.success("数据已成功导入到队列二");
//            } else {
//                logger.error("❌ 数据导入失败");
//                return AjaxResult.error("导入数据失败");
//            }
//        } catch (Exception e) {
//            logger.error("手动导入数据异常", e);
//            return AjaxResult.error("导入数据异常: " + e.getMessage());
//        }
//    }
//
//    /**
//     * 手动重试队列二数据
//     */
//    @PostMapping("/queue2/retry/{dataId}")
//    public AjaxResult retryQueue2Data(@PathVariable String dataId) {
//        try {
//            // 查找数据
//            DataProcessor.QueueData targetData = null;
//            for (DataProcessor.QueueData data : dataProcessor.getQueue2()) {
//                if (data.getDataId().equals(dataId)) {
//                    targetData = data;
//                    break;
//                }
//            }
//
//            if (targetData == null) {
//                return AjaxResult.error("未找到对应数据");
//            }
//
//            // 从队列二移除并添加到队列一
//            if (dataProcessor.getQueue2().remove(targetData)) {
//                dataProcessor.addToQueue1(targetData.getJsonData());
//                return AjaxResult.success("数据已添加到队列一等待发送");
//            } else {
//                return AjaxResult.error("操作失败");
//            }
//        } catch (Exception e) {
//            logger.error("手动重试数据异常", e);
//            return AjaxResult.error("重试异常: " + e.getMessage());
//        }
//    }
//
//    /**
//     * 转换队列数据为Map（用于存储内存的数据）
//     */
//    private Map<String, Object> convertQueueDataToMap(DataProcessor.QueueData queueData) {
//        Map<String, Object> map = new HashMap<>();
//        map.put("dataId", queueData.getDataId());
//        map.put("jsonData", queueData.getJsonData());
//        map.put("createTime", new Date(queueData.getCreateTime()));
//        map.put("retryCount", queueData.getRetryCount());
//        return map;
//    }
//    /**
//     * 转换DataQueue为Map(用于存储数据库的数据）
//     */
//    private Map<String, Object> convertDataQueueToMap(DataQueue dataQueue) {
//        Map<String, Object> map = new HashMap<>();
//        map.put("dataId", dataQueue.getDataId());
//        map.put("jsonData", dataQueue.getJsonData());
//        map.put("createTime", dataQueue.getCreateTime());
//        map.put("retryCount", dataQueue.getRetryCount());
//        map.put("status", dataQueue.getStatus());
//        map.put("sendResult", dataQueue.getSendResult());
//        return map;
//    }
//
//
//    /**
//     * 调试接口：检查所有数据状态
//     */
//    @GetMapping("/debug/all-data")
//    public AjaxResult debugAllData() {
//        try {
//            Map<String, Object> debugInfo = new HashMap<>();
//
//            // 队列一信息
//            BlockingQueue<DataProcessor.QueueData> queue1 = dataProcessor.getQueue1();
//            List<String> queue1Ids = new ArrayList<>();
//            for (DataProcessor.QueueData data : queue1) {
//                queue1Ids.add(data.getDataId());
//            }
//            debugInfo.put("queue1_size", queue1.size());
//            debugInfo.put("queue1_ids", queue1Ids);
//
//            // 队列二信息
//            BlockingQueue<DataProcessor.QueueData> queue2 = dataProcessor.getQueue2();
//            List<Map<String, Object>> queue2Details = new ArrayList<>();
//            for (DataProcessor.QueueData data : queue2) {
//                Map<String, Object> detail = new HashMap<>();
//                detail.put("dataId", data.getDataId());
//                detail.put("createTime", new Date(data.getCreateTime()));
//                detail.put("retryCount", data.getRetryCount());
//                detail.put("jsonData", data.getJsonData());
//                queue2Details.add(detail);
//            }
//            debugInfo.put("queue2_size", queue2.size());
//            debugInfo.put("queue2_details", queue2Details);
//
//            // 数据存储信息
//            Map<String, String> allData = dataProcessor.getAllData();
//            debugInfo.put("dataStorage_size", allData.size());
//            debugInfo.put("dataStorage_keys", new ArrayList<>(allData.keySet()));
//
//            logger.info("🐛 调试信息: {}", debugInfo);
//
//            return AjaxResult.success("调试信息", debugInfo);
//        } catch (Exception e) {
//            logger.error("❌ 获取调试信息异常", e);
//            return AjaxResult.error("获取调试信息失败");
//        }
//    }
//
//
//}

//package com.ruoyi.accept.controller;
//
//import com.alibaba.fastjson2.JSONObject;
//
//import com.ruoyi.accept.entity.DataQueue;
//import com.ruoyi.accept.processor.DataProcessor;
//import com.ruoyi.accept.service.IDataQueueService;
//import com.ruoyi.common.core.controller.BaseController;
//import com.ruoyi.common.core.domain.AjaxResult;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import com.ruoyi.accept.server.TcpServer;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
///**
// * 数据API控制器
// */
//@RestController
//@RequestMapping("/api/data")
//public class DataApiController extends BaseController {
//
//    private static final Logger logger = LoggerFactory.getLogger(DataApiController.class);
//
//    @Autowired
//    private TcpServer tcpServer;
//
//    @Autowired
//    private DataProcessor dataProcessor;
//
//    @Autowired
//    private IDataQueueService dataQueueService;
//
//    /**
//     * 获取TCP服务器状态
//     */
//    @GetMapping("/server/status")
//    public AjaxResult getServerStatus() {
//        return AjaxResult.success("TCP服务器状态", tcpServer.isRunning());
//    }
//
//    /**
//     * 获取所有数据ID列表
//     */
//    @GetMapping("/list")
//    public AjaxResult getDataList() {
//        // 从数据库获取所有数据
//        List<DataQueue> allData = dataQueueService.selectDataQueueList(new DataQueue());
//        List<String> dataIds = allData.stream()
//                .map(DataQueue::getDataId)
//                .collect(Collectors.toList());
//        return AjaxResult.success("数据列表", dataIds);
//    }
//
//    /**
//     * 根据数据ID获取JSON数据
//     */
//    @GetMapping("/{dataId}")
//    public AjaxResult getDataById(@PathVariable String dataId) {
//        DataQueue dataQueue = dataQueueService.selectDataQueueById(dataId);
//        if (dataQueue == null) {
//            return AjaxResult.error("数据不存在");
//        }
//        return AjaxResult.success("获取数据成功", dataQueue.getJsonData());
//    }
//
//    /**
//     * 获取队列一数据（待发送数据）
//     */
//    @GetMapping("/queue1")
//    public AjaxResult getQueue1Data() {
//        try {
//            logger.info("🔍 开始获取队列一数据");
//
//            // 从数据库查询队列一数据
//            List<DataQueue> queue1Data = dataQueueService.selectByQueueType("QUEUE1");
//            logger.info("📊 从数据库查询到队列一数据: {} 条", queue1Data.size());
//
//            // 转换为前端需要的格式
//            List<Map<String, Object>> queueData = queue1Data.stream()
//                    .map(this::convertDataQueueToMap)
//                    .collect(Collectors.toList());
//
//            Map<String, Object> result = new HashMap<>();
//            result.put("count", queueData.size());
//            result.put("data", queueData);
//
//            return AjaxResult.success("队列一数据", result);
//        } catch (Exception e) {
//            logger.error("❌ 获取队列一数据异常", e);
//            return AjaxResult.error("获取队列一数据失败");
//        }
//    }
//
//    /**
//     * 获取队列二数据（发送失败数据）
//     */
//    @GetMapping("/queue2")
//    public AjaxResult getQueue2Data() {
//        try {
//            logger.info("🔍 开始获取队列二数据");
//
//            // 从数据库查询队列二数据
//            List<DataQueue> queue2Data = dataQueueService.selectByQueueType("QUEUE2");
//            logger.info("📊 从数据库查询到队列二数据: {} 条", queue2Data.size());
//
//            // 转换为前端需要的格式
//            List<Map<String, Object>> queueData = queue2Data.stream()
//                    .map(this::convertDataQueueToMap)
//                    .collect(Collectors.toList());
//
//            Map<String, Object> result = new HashMap<>();
//            result.put("count", queueData.size());
//            result.put("data", queueData);
//
//            return AjaxResult.success("队列二数据", result);
//        } catch (Exception e) {
//            logger.error("❌ 获取队列二数据异常", e);
//            return AjaxResult.error("获取队列二数据失败");
//        }
//    }
//
//    /**
//     * 手动导入数据到队列二
//     */
//    @PostMapping("/queue2/import")
//    public AjaxResult importToQueue2(@RequestBody Map<String, Object> request) {
//        try {
//            logger.info("🟢 开始手动导入数据，请求参数: {}", request);
//
//            String jsonData = (String) request.get("jsonData");
//            if (jsonData == null || jsonData.trim().isEmpty()) {
//                logger.warn("❌ JSON数据为空");
//                return AjaxResult.error("JSON数据不能为空");
//            }
//
//            // 验证JSON格式
//            try {
//                JSONObject.parseObject(jsonData);
//                logger.info("✅ JSON格式验证通过");
//            } catch (Exception e) {
//                logger.error("❌ JSON格式验证失败: {}", e.getMessage());
//                return AjaxResult.error("JSON格式错误: " + e.getMessage());
//            }
//
//            boolean success = dataProcessor.importToQueue2(jsonData);
//            if (success) {
//                logger.info("✅ 数据成功导入到队列二");
//                return AjaxResult.success("数据已成功导入到队列二");
//            } else {
//                logger.error("❌ 数据导入失败");
//                return AjaxResult.error("导入数据失败");
//            }
//        } catch (Exception e) {
//            logger.error("❌ 手动导入数据异常", e);
//            return AjaxResult.error("导入数据异常: " + e.getMessage());
//        }
//    }
//
//    /**
//     * 手动重试队列二数据
//     */
//    @PostMapping("/queue2/retry/{dataId}")
//    public AjaxResult retryQueue2Data(@PathVariable String dataId) {
//        try {
//            logger.info("🔄 手动重试队列二数据, dataId: {}", dataId);
//
//            // 使用DataProcessor的转移方法
//            boolean success = dataProcessor.moveFromQueue2ToQueue1(dataId);
//            if (success) {
//                logger.info("✅ 数据已从队列二转移到队列一, dataId: {}", dataId);
//                return AjaxResult.success("数据已添加到队列一等待发送");
//            } else {
//                logger.error("❌ 数据转移失败, dataId: {}", dataId);
//                return AjaxResult.error("重试失败，数据转移异常");
//            }
//        } catch (Exception e) {
//            logger.error("❌ 手动重试数据异常, dataId: {}", dataId, e);
//            return AjaxResult.error("重试异常: " + e.getMessage());
//        }
//    }
//
//    /**
//     * 获取所有数据统计信息
//     */
//    @GetMapping("/statistics")
//    public AjaxResult getStatistics() {
//        try {
//            Map<String, Object> stats = new HashMap<>();
//
//            // 队列一统计
//            List<DataQueue> queue1Data = dataQueueService.selectByQueueType("QUEUE1");
//            stats.put("queue1Count", queue1Data.size());
//
//            // 队列二统计
//            List<DataQueue> queue2Data = dataQueueService.selectByQueueType("QUEUE2");
//            stats.put("queue2Count", queue2Data.size());
//
//            // 成功统计
//            List<DataQueue> successData = dataQueueService.selectByStatus("SUCCESS");
//            stats.put("successCount", successData.size());
//
//            // 失败统计
//            List<DataQueue> failedData = dataQueueService.selectByStatus("FAILED");
//            stats.put("failedCount", failedData.size());
//
//            return AjaxResult.success("数据统计", stats);
//        } catch (Exception e) {
//            logger.error("获取统计数据异常", e);
//            return AjaxResult.error("获取统计数据失败");
//        }
//    }
//
//    /**
//     * 转换DataQueue为Map（用于数据库存储的数据）
//     */
//    private Map<String, Object> convertDataQueueToMap(DataQueue dataQueue) {
//        Map<String, Object> map = new HashMap<>();
//        map.put("dataId", dataQueue.getDataId());
//        map.put("jsonData", dataQueue.getJsonData());
//        map.put("createTime", dataQueue.getCreateTime());
//        map.put("retryCount", dataQueue.getRetryCount());
//        map.put("status", dataQueue.getStatus());
//        map.put("sendResult", dataQueue.getSendResult());
//        map.put("queueType", dataQueue.getQueueType());
//        return map;
//    }
//
//    /**
//     * 转换QueueData为Map（用于内存队列的数据）
//     */
//    private Map<String, Object> convertQueueDataToMap(DataProcessor.QueueData queueData) {
//        Map<String, Object> map = new HashMap<>();
//        map.put("dataId", queueData.getDataId());
//        map.put("jsonData", queueData.getJsonData());
//        map.put("createTime", new Date(queueData.getCreateTime()));
//        map.put("retryCount", queueData.getRetryCount());
//        return map;
//    }
//}

package com.ruoyi.accept.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import com.ruoyi.accept.entity.DataQueue;
import com.ruoyi.accept.processor.DataProcessor;
import com.ruoyi.accept.service.IDataQueueService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.ruoyi.accept.server.TcpServer;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据API控制器
 */
@RestController
@RequestMapping("/api/data")
public class DataApiController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(DataApiController.class);

    @Autowired
    private TcpServer tcpServer;

    @Autowired
    private DataProcessor dataProcessor;

    @Autowired
    private IDataQueueService dataQueueService;

    /**
     * 获取TCP服务器状态
     */
    @GetMapping("/server/status")
    public AjaxResult getServerStatus() {
        return AjaxResult.success("TCP服务器状态", tcpServer.isRunning());
    }

    /**
     * 获取所有数据ID列表
     */
    @PreAuthorize("@ss.hasPermi('api:data:list')")
    @GetMapping("/list")
    public AjaxResult getDataList() {
        // 从数据库获取所有数据
        List<DataQueue> allData = dataQueueService.selectDataQueueList(new DataQueue());
        List<String> dataIds = allData.stream()
                .map(DataQueue::getDataId)
                .collect(Collectors.toList());
        return AjaxResult.success("数据列表", dataIds);
    }

    /**
     * 根据数据ID获取JSON数据
     */
    @PreAuthorize("@ss.hasPermi('api:data:dataId')")
    @GetMapping("/{dataId}")
    public AjaxResult getDataById(@PathVariable String dataId) {
        DataQueue dataQueue = dataQueueService.selectDataQueueById(dataId);
        if (dataQueue == null) {
            return AjaxResult.error("数据不存在");
        }
        return AjaxResult.success("获取数据成功", dataQueue.getJsonData());
    }

    /**
     * 获取队列一数据（待发送数据）
     */
    @GetMapping("/queue1")
    public AjaxResult getQueue1Data() {
        try {
            logger.info("🔍 开始获取队列一数据");

            // 从数据库查询队列一数据
            List<DataQueue> queue1Data = dataQueueService.selectByQueueType("QUEUE1");
            logger.info("📊 从数据库查询到队列一数据: {} 条", queue1Data.size());

            // 转换为前端需要的格式
            List<Map<String, Object>> queueData = queue1Data.stream()
                    .map(this::convertDataQueueToMap)
                    .collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("count", queueData.size());
            result.put("data", queueData);

            return AjaxResult.success("队列一数据", result);
        } catch (Exception e) {
            logger.error("❌ 获取队列一数据异常", e);
            return AjaxResult.error("获取队列一数据失败");
        }
    }

    /**
     * 获取队列二数据（发送失败数据）
     */
    @GetMapping("/queue2")
    public AjaxResult getQueue2Data() {
        try {
            logger.info("🔍 开始获取队列二数据");

            // 从数据库查询队列二数据
            List<DataQueue> queue2Data = dataQueueService.selectByQueueType("QUEUE2");
            logger.info("📊 从数据库查询到队列二数据: {} 条", queue2Data.size());

            // 转换为前端需要的格式
            List<Map<String, Object>> queueData = queue2Data.stream()
                    .map(this::convertDataQueueToMap)
                    .collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("count", queueData.size());
            result.put("data", queueData);

            return AjaxResult.success("队列二数据", result);
        } catch (Exception e) {
            logger.error("❌ 获取队列二数据异常", e);
            return AjaxResult.error("获取队列二数据失败");
        }
    }

    /**
     * 手动导入数据到队列二
     */
    @PreAuthorize("@ss.hasPermi('api:data:import')")
    @PostMapping("/queue2/import")
    public AjaxResult importToQueue2(@RequestBody Map<String, Object> request) {
        int successCount = 0;
        int failCount = 0;

        try {
            logger.info("开始手动导入数据，请求参数: {}", request);

            Object jsonDataObj = request.get("jsonData");
            if (jsonDataObj == null) {
                logger.warn("❌ JSON数据为空");
                return AjaxResult.error("JSON数据不能为空");
            }

            logger.info("🔍 数据类型: {}", jsonDataObj.getClass().getSimpleName());

            List<Object> dataItems = new ArrayList<>();

            // 直接传入的List对象
            if (jsonDataObj instanceof List) {
                List<?> dataList = (List<?>) jsonDataObj;
                logger.info("📋 检测到List类型数据，共{}条", dataList.size());

                if (dataList.isEmpty()) {
                    logger.warn("❌ JSON数据列表为空");
                    return AjaxResult.error("JSON数据列表不能为空");
                }
                dataItems.addAll(dataList);
            }
            // 情况2：字符串类型的JSON数据
            else if (jsonDataObj instanceof String) {
                String jsonDataStr = ((String) jsonDataObj).trim();
                logger.info("📝 检测到字符串类型JSON数据: {}", jsonDataStr.substring(0, Math.min(50, jsonDataStr.length())));

                if (jsonDataStr.isEmpty()) {
                    logger.warn("❌ JSON数据为空");
                    return AjaxResult.error("JSON数据不能为空");
                }

                // 检查字符串格式
                if (jsonDataStr.startsWith("[") && jsonDataStr.endsWith("]")) {
                    // 是JSON数组
                    try {
                        JSONArray jsonArray = JSONArray.parseArray(jsonDataStr);
                        if (jsonArray != null) {
                            dataItems.addAll(jsonArray);
                            logger.info("📋 解析为JSON数组，共{}条", jsonArray.size());
                        } else {
                            logger.error("❌ JSON数组解析结果为null");
                            return AjaxResult.error("JSON数组格式错误");
                        }
                    } catch (Exception e) {
                        logger.error("❌ JSON数组解析失败: {}", e.getMessage());
                        // 尝试使用更宽松的解析方式
                        try {
                            Object parsed = JSON.parse(jsonDataStr);
                            if (parsed instanceof JSONArray) {
                                dataItems.addAll((JSONArray) parsed);
                                logger.info("📋 使用宽松解析成功，共{}条", ((JSONArray) parsed).size());
                            } else {
                                return AjaxResult.error("JSON格式错误: " + e.getMessage());
                            }
                        } catch (Exception e2) {
                            logger.error("❌ 宽松解析也失败: {}", e2.getMessage());
                            return AjaxResult.error("JSON格式错误: " + e2.getMessage());
                        }
                    }
                } else if (jsonDataStr.startsWith("{") && jsonDataStr.endsWith("}")) {
                    // 是单个JSON对象
                    try {
                        JSONObject jsonObject = JSONObject.parseObject(jsonDataStr);
                        dataItems.add(jsonObject);
                        logger.info("✅ 单条JSON对象");
                    } catch (Exception e) {
                        logger.error("❌ JSON对象解析失败: {}", e.getMessage());
                        return AjaxResult.error("JSON对象格式错误: " + e.getMessage());
                    }
                } else {
                    logger.error("❌ 不支持的JSON字符串格式");
                    return AjaxResult.error("不支持的JSON字符串格式");
                }
            }
            // 情况3：单个Map对象
            else if (jsonDataObj instanceof Map) {
                logger.info("🗂️ 检测到单个Map对象");
                dataItems.add(jsonDataObj);
            } else {
                logger.warn("❌ 不支持的JSON数据类型: {}", jsonDataObj.getClass().getSimpleName());
                return AjaxResult.error("不支持的JSON数据类型: " + jsonDataObj.getClass().getSimpleName());
            }

            if (dataItems.isEmpty()) {
                logger.error("❌ 没有有效的JSON数据");
                return AjaxResult.error("没有有效的JSON数据");
            }

            logger.info("✅ 开始逐条导入 {} 条数据", dataItems.size());

            // 逐条处理每条数据，确保都是单独新增
            for (int i = 0; i < dataItems.size(); i++) {
                Object dataItem = dataItems.get(i);
                String jsonData = null;

                try {
                    // 将每条数据转换为JSON字符串
                    if (dataItem instanceof Map || dataItem instanceof JSONObject) {
                        jsonData = JSONObject.toJSONString(dataItem);
                    } else if (dataItem instanceof String) {
                        jsonData = (String) dataItem;
                        // 验证字符串是否为有效JSON
                        JSONObject.parseObject(jsonData);
                    } else {
                        jsonData = JSONObject.toJSONString(dataItem);
                    }

                    logger.info("📤 正在导入第{}条数据", i + 1);

                    // 调用导入方法，每条数据都是单独的新增操作
                    boolean success = dataProcessor.importToQueue2(jsonData);

                    if (success) {
                        successCount++;
                        logger.info("✅ 第{}条数据成功导入到队列二", i + 1);
                    } else {
                        failCount++;
                        logger.error("❌ 第{}条数据导入失败", i + 1);
                    }

                } catch (Exception e) {
                    failCount++;
                    logger.error("❌ 第{}条数据处理异常: {}", i + 1, e.getMessage());
                }
            }

            logger.info("🎉 批量导入完成: 成功{}条, 失败{}条", successCount, failCount);

            // 返回导入结果
            if (successCount == 0 && failCount > 0) {
                return AjaxResult.error("所有数据导入失败，共" + failCount + "条");
            } else if (failCount == 0) {
                return AjaxResult.success("所有数据已成功导入到队列二，共" + successCount + "条");
            } else {
                return AjaxResult.success("数据导入完成: 成功" + successCount + "条, 失败" + failCount + "条");
            }

        } catch (Exception e) {
            logger.error("❌ 手动导入数据异常", e);
            return AjaxResult.error("导入数据异常: " + e.getMessage());
        }
    }

    /**
     * 手动重试队列二数据
     */
    @PreAuthorize("@ss.hasPermi('api:data:retry')")
    @PostMapping("/queue2/retry/{dataId}")
    public AjaxResult retryQueue2Data(@PathVariable String dataId) {
        try {
            logger.info("🔄 手动重试队列二数据, dataId: {}", dataId);

            // 使用DataProcessor的转移方法
            boolean success = dataProcessor.moveFromQueue2ToQueue1(dataId);
            if (success) {
                logger.info("✅ 数据已从队列二转移到队列一, dataId: {}", dataId);
                return AjaxResult.success("数据已添加到队列一等待发送");
            } else {
                logger.error("❌ 数据转移失败, dataId: {}", dataId);
                return AjaxResult.error("重试失败，数据转移异常");
            }
        } catch (Exception e) {
            logger.error("❌ 手动重试数据异常, dataId: {}", dataId, e);
            return AjaxResult.error("重试异常: " + e.getMessage());
        }
    }

    /**
     * 获取所有数据统计信息
     */
    @GetMapping("/statistics")
    public AjaxResult getStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();

            // 队列一统计
            List<DataQueue> queue1Data = dataQueueService.selectByQueueType("QUEUE1");
            stats.put("queue1Count", queue1Data.size());

            // 队列二统计
            List<DataQueue> queue2Data = dataQueueService.selectByQueueType("QUEUE2");
            stats.put("queue2Count", queue2Data.size());

            // 成功统计
            List<DataQueue> successData = dataQueueService.selectByStatus("SUCCESS");
            stats.put("successCount", successData.size());

            // 失败统计
            List<DataQueue> failedData = dataQueueService.selectByStatus("FAILED");
            stats.put("failedCount", failedData.size());

            return AjaxResult.success("数据统计", stats);
        } catch (Exception e) {
            logger.error("获取统计数据异常", e);
            return AjaxResult.error("获取统计数据失败");
        }
    }

    /**
     * 转换DataQueue为Map（用于数据库存储的数据）
     */
    private Map<String, Object> convertDataQueueToMap(DataQueue dataQueue) {
        Map<String, Object> map = new HashMap<>();
        map.put("dataId", dataQueue.getDataId());
        map.put("jsonData", dataQueue.getJsonData());
        map.put("createTime", dataQueue.getCreateTime());
        map.put("retryCount", dataQueue.getRetryCount());
        map.put("status", dataQueue.getStatus());
        map.put("sendResult", dataQueue.getSendResult());
        map.put("queueType", dataQueue.getQueueType());
        return map;
    }

    /**
     * 转换QueueData为Map（用于内存队列的数据）
     */
    private Map<String, Object> convertQueueDataToMap(DataProcessor.QueueData queueData) {
        Map<String, Object> map = new HashMap<>();
        map.put("dataId", queueData.getDataId());
        map.put("jsonData", queueData.getJsonData());
        map.put("createTime", new Date(queueData.getCreateTime()));
        map.put("retryCount", queueData.getRetryCount());
        return map;
    }
}