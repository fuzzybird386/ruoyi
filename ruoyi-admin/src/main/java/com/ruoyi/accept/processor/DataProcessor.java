package com.ruoyi.accept.processor;

import com.alibaba.fastjson2.JSONObject;

import com.ruoyi.accept.entity.DataQueue;
import com.ruoyi.accept.service.IDataQueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
public class DataProcessor {

    private static final Logger log = LoggerFactory.getLogger(DataProcessor.class);

    // 内存队列（用于提高性能）
    private final BlockingQueue<QueueData> queue1 = new LinkedBlockingQueue<>();
    private final BlockingQueue<QueueData> queue2 = new LinkedBlockingQueue<>();

    // 数据库服务
    @Autowired
    private IDataQueueService dataQueueService;

    // 远程API地址
    private static final String REMOTE_API_URL = "http://83.243.248.16:9006/aksemas/emas/info/send";
    private static final int MAX_RETRY_COUNT = 5;

    @Autowired
    private RestTemplate restTemplate;

    private volatile boolean isProcessingQueue1 = false;
    private volatile boolean isProcessingQueue2 = false;

    // 添加成功数据保留天数配置（可配置化）
    private static final int SUCCESS_DATA_RETENTION_DAYS = 7; // 保留7天

    /**
     * 应用启动时从数据库加载未处理的数据到内存队列
     */
    @PostConstruct
    public void init() {
        log.info("🔄 初始化数据队列，从数据库加载未处理的数据...");

        // 加载队列一数据
        loadQueue1FromDatabase();

        // 加载队列二数据
        loadQueue2FromDatabase();

        log.info("✅ 数据队列初始化完成");
    }

    /**
     * 从数据库加载队列一数据
     */
//    private void loadQueue1FromDatabase() {
//        try {
//            List<DataQueue> queue1Data = dataQueueService.selectByQueueType("QUEUE1");
//            for (DataQueue data : queue1Data) {
//                if ("PENDING".equals(data.getStatus()) || "FAILED".equals(data.getStatus())) {
//                    QueueData queueData = new QueueData(data.getDataId(), data.getJsonData());
//                    queueData.setRetryCount(data.getRetryCount());
//                    queue1.put(queueData);
//                }
//            }
//            log.info("✅ 从数据库加载队列一数据: {} 条", queue1Data.size());
//        } catch (Exception e) {
//            log.error("❌ 从数据库加载队列一数据失败", e);
//        }
//    }
    private void loadQueue1FromDatabase() {
        try {
            // 修改：查询所有状态的QUEUE1数据
            DataQueue query = new DataQueue();
            query.setQueueType("QUEUE1");
            List<DataQueue> queue1Data = dataQueueService.selectDataQueueList(query);

            int loadedCount = 0;
            for (DataQueue data : queue1Data) {
                // 加载所有非SUCCESS状态的数据
                if (!"SUCCESS".equals(data.getStatus())) {
                    QueueData queueData = new QueueData(data.getDataId(), data.getJsonData());
                    queueData.setRetryCount(data.getRetryCount());
                    queue1.put(queueData);
                    loadedCount++;

                    // 如果状态是SENDING，更新为PENDING重新处理
                    if ("SENDING".equals(data.getStatus())) {
                        dataQueueService.updateSendResult(data.getDataId(),
                                "从SENDING状态恢复为PENDING", "PENDING");
                        log.info("🔄 将SENDING状态数据恢复为PENDING: {}", data.getDataId());
                    }
                }
            }
            log.info("✅ 从数据库加载队列一数据: {} 条，成功加载: {} 条",
                    queue1Data.size(), loadedCount);
        } catch (Exception e) {
            log.error("❌ 从数据库加载队列一数据失败", e);
        }
    }

    /**
     * 从数据库加载队列二数据
     */
    private void loadQueue2FromDatabase() {
        try {
            List<DataQueue> queue2Data = dataQueueService.selectByQueueType("QUEUE2");
            for (DataQueue data : queue2Data) {
                if ("PENDING".equals(data.getStatus()) || "FAILED".equals(data.getStatus())) {
                    QueueData queueData = new QueueData(data.getDataId(), data.getJsonData());
                    queueData.setRetryCount(data.getRetryCount());
                    queue2.put(queueData);
                }
            }
            log.info("✅ 从数据库加载队列二数据: {} 条", queue2Data.size());
        } catch (Exception e) {
            log.error("❌ 从数据库加载队列二数据失败", e);
        }
    }

    /**
     * 添加数据到队列一（同时保存到数据库）
     */
    public void addToQueue1(String jsonData) {
        String dataId = generateDataId();
        QueueData queueData = new QueueData(dataId, jsonData);

        try {
            // 保存到数据库
            saveToDatabase(dataId, jsonData, "QUEUE1", "PENDING");

            // 添加到内存队列
            queue1.put(queueData);
            log.info("✅ 数据已添加到队列一和数据库, dataId: {}", dataId);
        } catch (InterruptedException e) {
            log.error("❌ 添加数据到队列一失败", e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 手动导入数据到队列二（同时保存到数据库）
     */
    public boolean importToQueue2(String jsonData) {
        String dataId = generateDataId();
        QueueData queueData = new QueueData(dataId, jsonData);

        try {
            // 保存到数据库
            saveToDatabase(dataId, jsonData, "QUEUE2", "PENDING");

            // 添加到内存队列
            queue2.put(queueData);
            log.info("✅ 数据已手动导入到队列二和数据库, dataId: {}", dataId);
            return true;
        } catch (InterruptedException e) {
            log.error("❌ 手动导入数据到队列二失败", e);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 将队列二的数据转移到队列一进行重试
     */
    public boolean moveFromQueue2ToQueue1(String dataId) {
        try {
            // 从数据库查找数据
            DataQueue dataQueue = dataQueueService.selectDataQueueById(dataId);
            if (dataQueue == null) {
                log.error("❌ 未找到要转移的数据, dataId: {}", dataId);
                return false;
            }

            // 从队列二内存队列中移除
            boolean removedFromQueue2 = removeFromQueue2(dataId);
            if (!removedFromQueue2) {
                log.warn("⚠️ 数据不在队列二内存中，但继续处理数据库转移, dataId: {}", dataId);
            }

            // 更新数据库：转移到队列一，重置重试次数
            dataQueue.setQueueType("QUEUE1");
            dataQueue.setRetryCount(0); // 重置重试次数
            dataQueue.setStatus("PENDING");
            dataQueue.setUpdateTime(new Date());
            dataQueue.setSendResult("从队列二转移到队列一重新发送");
            dataQueueService.updateDataQueue(dataQueue);

            // 添加到队列一内存队列
            QueueData queueData = new QueueData(dataId, dataQueue.getJsonData());
            queue1.put(queueData);

            log.info("🔄 数据已从队列二转移到队列一, dataId: {}", dataId);
            return true;
        } catch (Exception e) {
            log.error("❌ 从队列二转移到队列一失败, dataId: {}", dataId, e);
            return false;
        }
    }

    /**
     * 从队列二内存队列中移除数据
     */
    private boolean removeFromQueue2(String dataId) {
        try {
            // 遍历队列二找到并移除指定数据
            for (QueueData data : queue2) {
                if (data.getDataId().equals(dataId)) {
                    boolean removed = queue2.remove(data);
                    if (removed) {
                        log.info("✅ 从队列二内存中移除数据, dataId: {}", dataId);
                    }
                    return removed;
                }
            }
            return false;
        } catch (Exception e) {
            log.error("❌ 从队列二内存移除数据失败, dataId: {}", dataId, e);
            return false;
        }
    }

    /**
     * 保存数据到数据库
     */
    private void saveToDatabase(String dataId, String jsonData, String queueType, String status) {
        try {
            DataQueue dataQueue = new DataQueue();
            dataQueue.setDataId(dataId);
            dataQueue.setJsonData(jsonData);
            dataQueue.setQueueType(queueType);
            dataQueue.setRetryCount(0);
            dataQueue.setMaxRetryCount(MAX_RETRY_COUNT);
            dataQueue.setStatus(status);
            dataQueue.setCreateTime(new Date());

            dataQueueService.insertDataQueue(dataQueue);
            log.debug("💾 数据已保存到数据库, dataId: {}", dataId);
        } catch (Exception e) {
            log.error("❌ 保存数据到数据库失败, dataId: {}", dataId, e);
        }
    }

    /**
     * 处理队列一数据 - 每秒检查一次
     */
    @Scheduled(fixedDelay = 1000)
    public void processQueue1() {

//        log.debug("⏰ 队列一调度器触发，isProcessingQueue1: {}, queue1大小: {}", isProcessingQueue1, queue1.size());
//        if (isProcessingQueue1 || queue1.isEmpty()) {
//            if (queue1.isEmpty()) {
//                log.debug("⏸️ 队列一为空，跳过处理");
//            }
//            return;
//        }

        isProcessingQueue1 = true;
        try {
            QueueData data = queue1.poll();
            if (data != null) {
                log.info("🚀 从队列一获取数据开始处理, dataId: {}", data.getDataId());
                processQueue1DataWithRetry(data);
            }
//            else {
//                log.warn("从队列一获取到null数据");
//            }
        }catch (Exception e) {
            log.error("❌ 处理队列一数据异常", e);
        } finally {
            isProcessingQueue1 = false;
        }
    }

    /**
     * 处理队列二数据 - 每60分钟检查一次
     */
    @Scheduled(fixedDelay = 60 * 60 * 1000)
    public void processQueue2() {
        if (isProcessingQueue2 || queue2.isEmpty()) {
            log.debug("⏸️ 队列二处理条件不满足");
            return;
        }

        isProcessingQueue2 = true;
        try {
            log.info("🔄 开始处理队列二数据，数量: {}", queue2.size());

            // 将队列二的所有数据转移到队列一进行重试
            List<QueueData> tempList = new CopyOnWriteArrayList<>();
            queue2.drainTo(tempList);

            for (QueueData data : tempList) {
                log.info("🔄 将队列二数据转移到队列一重试, dataId: {}", data.getDataId());
                moveFromQueue2ToQueue1(data.getDataId());
            }

            log.info("✅ 队列二数据处理完成，转移了 {} 条数据到队列一", tempList.size());
        } finally {
            isProcessingQueue2 = false;
        }
    }

    /**
     * 处理队列一数据（连续重试5次）
     */
    private void processQueue1DataWithRetry(QueueData data) {
        String dataId = data.getDataId();
        String jsonData = data.getJsonData();

        log.info("🚀 开始发送队列一数据, dataId: {}, 当前重试次数: {}", dataId, data.getRetryCount());

        // 更新数据库状态为发送中
        updateDatabaseStatus(dataId, "SENDING", "开始发送数据");

        // 连续重试5次
        for (int attempt = 1; attempt <= MAX_RETRY_COUNT; attempt++) {
            try {
                // 更新重试次数
                data.incrementRetryCount();
                updateDatabaseRetryCount(dataId);

                log.info("🔄 第 {} 次尝试发送, dataId: {}", attempt, dataId);
                boolean success = sendToRemoteApi(jsonData);

                if (success) {
                    log.info("✅ 数据发送成功, dataId: {}, 重试次数: {}", dataId, attempt);
                    // 更新数据库状态为成功
                    updateDatabaseStatus(dataId, "SUCCESS", "发送成功");
                    // 从内存队列中删除（已经处理完成）
                    return;
                } else {
                    log.warn("⚠️ 数据发送失败, dataId: {}, 重试次数: {}/{}", dataId, attempt, MAX_RETRY_COUNT);
                    updateDatabaseStatus(dataId, "FAILED", "第" + attempt + "次发送失败");

                    // 如果不是最后一次重试，等待2秒后继续
                    if (attempt < MAX_RETRY_COUNT) {
                        Thread.sleep(2000);
                    }
                }
            } catch (Exception e) {
                log.error("❌ 发送数据异常, dataId: {}, 重试次数: {}/{}", dataId, attempt, MAX_RETRY_COUNT, e);
                updateDatabaseStatus(dataId, "FAILED", "发送异常: " + e.getMessage());

                if (attempt < MAX_RETRY_COUNT) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        // 如果5次都失败，转移到队列二
        try {
            moveFromQueue1ToQueue2(dataId, jsonData, data.getRetryCount());
            log.info("🔁 数据从队列一转移到队列二等待1小时后重试, dataId: {}", dataId);
        } catch (Exception e) {
            log.error("❌ 转移到队列二失败, dataId: {}", dataId, e);
        }
    }

    /**
     * 将数据从队列一转移到队列二
     */
    private void moveFromQueue1ToQueue2(String dataId, String jsonData, int retryCount) {
        try {
            // 更新数据库：转移到队列二
            DataQueue dataQueue = new DataQueue();
            dataQueue.setDataId(dataId);
            dataQueue.setQueueType("QUEUE2");
            dataQueue.setRetryCount(retryCount);
            dataQueue.setStatus("PENDING");
            dataQueue.setUpdateTime(new Date());
            dataQueue.setSendResult("连续5次发送失败，转移到队列二等待1小时后重试");
            dataQueueService.updateDataQueue(dataQueue);

            // 添加到队列二内存队列
            QueueData queueData = new QueueData(dataId, jsonData);
            queueData.setRetryCount(retryCount);
            queue2.put(queueData);

            log.info("✅ 数据已从队列一转移到队列二, dataId: {}", dataId);
        } catch (Exception e) {
            log.error("❌ 从队列一转移到队列二失败, dataId: {}", dataId, e);
            throw new RuntimeException("转移数据失败", e);
        }
    }

    /**
     * 发送数据到远程API
     */
    private boolean sendToRemoteApi(String jsonData) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> requestEntity = new HttpEntity<>(jsonData, headers);

            log.debug("🌐 发送数据到远程API: {}", REMOTE_API_URL);
            log.info("发送的数据：{}",jsonData);

            ResponseEntity<String> response = restTemplate.exchange(
                    REMOTE_API_URL,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                String responseBody = response.getBody();
                log.debug("📥 远程API响应: {}", responseBody);

                if (responseBody != null) {
                    try {
                        JSONObject responseJson = JSONObject.parseObject(responseBody);
                        boolean success = responseJson.getBooleanValue("success");
                        String code = responseJson.getString("code");

                        return success && "0".equals(code);
                    } catch (Exception e) {
                        log.error("❌ 解析API响应异常: {}", e.getMessage());
                        return false;
                    }
                }
            } else {
                log.error("❌ 远程API返回错误状态码: {}", response.getStatusCode());
            }

            return false;
        } catch (Exception e) {
            log.error("❌ 调用远程API异常: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 更新数据库状态
     */
    private void updateDatabaseStatus(String dataId, String status, String sendResult) {
        try {
            dataQueueService.updateSendResult(dataId, sendResult, status);
        } catch (Exception e) {
            log.error("❌ 更新数据库状态失败, dataId: {}", dataId, e);
        }
    }

    /**
     * 更新数据库重试次数
     */
    private void updateDatabaseRetryCount(String dataId) {
        try {
            dataQueueService.incrementRetryCount(dataId);
        } catch (Exception e) {
            log.error("❌ 更新数据库重试次数失败, dataId: {}", dataId, e);
        }
    }

    /**
     * 获取队列一数据（用于前端显示）
     */
    public BlockingQueue<QueueData> getQueue1() {
        return queue1;
    }

    /**
     * 获取队列二数据（用于前端显示）
     */
    public BlockingQueue<QueueData> getQueue2() {
        return queue2;
    }

    /**
     * 生成数据ID
     */
    private String generateDataId() {
        return System.currentTimeMillis() + "_" + ThreadLocalRandom.current().nextInt(1000, 9999);
    }

    /**
     * 队列数据内部类
     */
    public static class QueueData {
        private final String dataId;
        private final String jsonData;
        private final long createTime;
        private final AtomicInteger retryCount;

        public QueueData(String dataId, String jsonData) {
            this.dataId = dataId;
            this.jsonData = jsonData;
            this.createTime = System.currentTimeMillis();
            this.retryCount = new AtomicInteger(0);
        }

        public String getDataId() { return dataId; }
        public String getJsonData() { return jsonData; }
        public long getCreateTime() { return createTime; }
        public int getRetryCount() { return retryCount.get(); }
        public void setRetryCount(int count) { retryCount.set(count); }
        public void incrementRetryCount() { retryCount.incrementAndGet(); }
    }

    /**
     * 定期删除成功数据 - 每周一凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 ? * MON") // 每周一凌晨2点执行
    public void cleanUpSuccessData() {
        try {
            log.info("🧹 开始定期清理成功数据...");

            // 计算删除截止时间（7天前）
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, -SUCCESS_DATA_RETENTION_DAYS);
            Date cutoffDate = calendar.getTime();



//            // 删除成功且超过7天的数据
//            int deletedCount = dataQueueService.deleteSuccessDataBeforeDate(cutoffDate);
//            log.info("成功清理 {} 条成功数据（清理7天前的数据）", deletedCount);

            // 统计要删除的数据量
            int toDeleteCount = dataQueueService.countSuccessDataBeforeDate(cutoffDate);
            log.info("发现 {} 条超过 {} 天的成功数据需要清理",
                    toDeleteCount, SUCCESS_DATA_RETENTION_DAYS);

            if (toDeleteCount > 0) {
                // 执行删除
                int deletedCount = dataQueueService.deleteSuccessDataBeforeDate(cutoffDate);
                log.info(" 成功清理 {} 条成功数据（清理{}天前的数据）",
                        deletedCount, SUCCESS_DATA_RETENTION_DAYS);
            } else {
                log.info("没有需要清理的成功数据");
            }

        } catch (Exception e) {
            log.error(" 清理成功数据异常", e);
        }
    }
}