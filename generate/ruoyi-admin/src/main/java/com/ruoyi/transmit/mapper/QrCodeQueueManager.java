package com.ruoyi.transmit.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.transmit.config.QrCodeWebSocketCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class QrCodeQueueManager {

    private static final int QUEUE_TYPE_1 = 1;
    private static final int QUEUE_TYPE_2 = 2;

    private static final int STATUS_PENDING = 1;
    private static final int STATUS_PROCESSING = 2;
    private static final int STATUS_COMPLETED = 3;
    private static final int STATUS_FAILED = 4;

    /** 测试环境：不依赖 GPIO 成功信号，每张二维码固定展示时长后自动完成 */
    private static final boolean TEST_MODE = true;

    private static final int REFRESH_INTERVAL_SECONDS = 3;
    private static final int DISPLAY_DURATION_SECONDS = 3;
    private static final int MAX_REFRESH_COUNT = 10;
    private static final String TIMEOUT_REASON = "刷新10次未收到成功信号";

    @Autowired
    private QrMapper qrCodeQueueMapper;

    @Autowired
    private QrMapper qrMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Object consumerLock = new Object();

    private volatile Map<String, Object> currentProcessingData = null;
    private volatile boolean consumerIdle = true;
    private volatile long currentDisplayStartMs = 0L;
    private final AtomicInteger refreshCount = new AtomicInteger(0);

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> refreshTask = null;

    private QrCodeUpdateCallback callback;

    public interface QrCodeUpdateCallback {
        void onQrCodeUpdate(Map<String, Object> data);
        void onQueueStatusUpdate(int queue1Size, int queue2Size);
        void onDataSuccess(String dataId);
        void onDataFailed(String dataId, String reason);
    }

    @Autowired
    public void setWebSocketCallback(QrCodeWebSocketCallback callback) {
        this.callback = callback;
    }

    public void setCallback(QrCodeUpdateCallback callback) {
        this.callback = callback;
    }

    public int getQueue1Size() {
        return qrCodeQueueMapper.getQueueSize(QUEUE_TYPE_1, STATUS_PENDING);
    }

    public int getQueue2Size() {
        return qrCodeQueueMapper.getQueueSize(QUEUE_TYPE_2, STATUS_FAILED);
    }

    /**
     * 生产者入队；若消费者空闲则立即 dequeue。
     */
    @Transactional
    public void addToQueue1(Map<String, Object> data) {
        try {
            String dataId = generateDataId(data);
            String contentJson = objectMapper.writeValueAsString(data);

            qrCodeQueueMapper.insertQueueData(dataId, QUEUE_TYPE_1, contentJson, STATUS_PENDING);
            notifyQueueStatus();
            System.out.println("✅ 数据已添加到队列一: " + dataId + "，队列大小: " + getQueue1Size());

            synchronized (consumerLock) {
                if (currentProcessingData == null) {
                    System.out.println("🚀 检测到无当前处理数据，立即开始处理新数据");
                    dequeueNext();
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("数据序列化失败", e);
        }
    }

    public void startProcessing() {
        if (refreshTask != null && !refreshTask.isCancelled()) {
            return;
        }

        restoreCurrentProcessingData();

        synchronized (consumerLock) {
            if (currentProcessingData == null && getQueue1Size() > 0) {
                System.out.println("🔄 检测到队列一有数据，立即开始处理");
                dequeueNext();
            }
        }

        refreshTask = scheduler.scheduleAtFixedRate(() -> {
            try {
                onRefreshTick();
            } catch (Exception e) {
                System.err.println("处理数据异常: " + e.getMessage());
                e.printStackTrace();
            }
        }, 0, REFRESH_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    private void restoreCurrentProcessingData() {
        try {
            Map<String, Object> dbCurrent = qrCodeQueueMapper.selectCurrentProcessing();
            if (dbCurrent != null) {
                String contentJson = (String) dbCurrent.get("content_json");
                Map<String, Object> data = objectMapper.readValue(contentJson, Map.class);

                currentProcessingData = data;
                refreshCount.set((Integer) dbCurrent.get("refresh_count"));
                consumerIdle = false;
                currentDisplayStartMs = System.currentTimeMillis();

                String dataId = generateDataId(currentProcessingData);
                System.out.println("🔄 恢复当前处理数据: " + dataId + "，刷新次数: " + refreshCount.get());
                publishQrUpdate(currentProcessingData);
            } else {
                System.out.println("📭 没有需要恢复的当前处理数据");
            }
        } catch (Exception e) {
            System.err.println("恢复当前处理数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 定时 tick：处理中则刷新/超时，空闲则兜底 dequeue。
     */
    private void onRefreshTick() {
        synchronized (consumerLock) {
            if (currentProcessingData != null) {
                if (TEST_MODE) {
                    advanceTestModeItem();
                } else {
                    refreshCurrentItem();
                }
            } else {
                dequeueNext();
            }
        }
    }

    /** 测试模式：展示满 {@link #DISPLAY_DURATION_SECONDS} 秒后自动完成，不刷新、不判成功/超时 */
    private void advanceTestModeItem() {
        long elapsedMs = System.currentTimeMillis() - currentDisplayStartMs;
        if (elapsedMs < DISPLAY_DURATION_SECONDS * 1000L) {
            return;
        }

        String dataId = generateDataId(currentProcessingData);
        System.out.println("⏱️ 【测试模式】展示 " + DISPLAY_DURATION_SECONDS + " 秒，自动完成: " + dataId);
        ackCurrentItem(dataId);
    }

    /**
     * 消费者取队：从队列一拉取下一条，或进入空闲状态。
     */
    private void dequeueNext() {
        try {
            if (currentProcessingData != null) {
                return;
            }

            List<Map<String, Object>> dbDataList = qrCodeQueueMapper.selectFirstFromQueue1();
            if (dbDataList == null || dbDataList.isEmpty()) {
                enterIdleState();
                return;
            }

            Map<String, Object> dbData = dbDataList.get(0);
            String contentJson = (String) dbData.get("content_json");
            Map<String, Object> data = objectMapper.readValue(contentJson, Map.class);
            String dataId = (String) dbData.get("data_id");

            beginProcessingItem(dataId, data, contentJson, dbDataList.size() - 1);
        } catch (Exception e) {
            System.err.println("从队列一获取数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void beginProcessingItem(String dataId, Map<String, Object> data,
                                     String contentJson, int pendingBehind) {
        consumerIdle = false;
        currentProcessingData = data;
        currentDisplayStartMs = System.currentTimeMillis();
        refreshCount.set(1);

        qrCodeQueueMapper.updateDataStatus(dataId, STATUS_PROCESSING);
        qrCodeQueueMapper.insertCurrentProcessing(dataId, contentJson, 1);

        System.out.println("🎯 开始处理数据: " + dataId);
        System.out.println("📊 队列一剩余待处理: " + pendingBehind);

        publishQrUpdate(currentProcessingData);
        notifyQueueStatus();
    }

    private void refreshCurrentItem() {
        int count = refreshCount.incrementAndGet();
        String currentDataId = generateDataId(currentProcessingData);

        qrCodeQueueMapper.updateCurrentProcessingRefreshCount(currentDataId, count);
        System.out.println("🔄 刷新二维码，数据ID: " + currentDataId + "，刷新次数: " + count);
        publishQrUpdate(currentProcessingData);

        if (count >= MAX_REFRESH_COUNT) {
            failCurrentItem(TIMEOUT_REASON);
            dequeueNext();
        }
    }

    private void enterIdleState() {
        if (consumerIdle) {
            return;
        }
        consumerIdle = true;
        currentProcessingData = null;
        currentDisplayStartMs = 0L;
        refreshCount.set(0);
        System.out.println("📭 队列一为空，无待处理数据");
        publishQrUpdate(null);
    }

    private void clearCurrentState() {
        currentProcessingData = null;
        currentDisplayStartMs = 0L;
        refreshCount.set(0);
    }

    /**
     * ACK：消费成功，继续 dequeue 下一条。
     */
    private void ackCurrentItem(String dataId) {
        qrCodeQueueMapper.updateDataStatus(dataId, STATUS_COMPLETED);
        qrCodeQueueMapper.deleteCurrentProcessing(dataId);

        if (callback != null) {
            callback.onDataSuccess(dataId);
        }

        clearCurrentState();
        dequeueNext();
    }

    /**
     * NACK：超时失败，移入队列二。
     */
    private void failCurrentItem(String reason) {
        if (currentProcessingData == null) {
            return;
        }

        String dataId = generateDataId(currentProcessingData);
        moveToQueue2(currentProcessingData, reason);
        clearCurrentState();

        if (callback != null) {
            callback.onDataFailed(dataId, reason);
        }
    }

    @Transactional
    public boolean handleSuccessSignal(String dataId) {
        if (TEST_MODE) {
            System.out.println("⚠️ 【测试模式】忽略成功信号: " + dataId);
            return false;
        }
        synchronized (consumerLock) {
            if (currentProcessingData == null) {
                System.out.println("⚠️ 当前没有正在处理的数据，忽略成功信号: " + dataId);
                return false;
            }

            String currentDataId = generateDataId(currentProcessingData);
            if (!dataId.equals(currentDataId)) {
                System.out.println("⚠️ 成功信号ID不匹配，当前: " + currentDataId + "，收到: " + dataId);
                return false;
            }

            System.out.println("✅ 收到成功信号，完成数据: " + dataId);
            ackCurrentItem(dataId);
            return true;
        }
    }

    @Transactional
    public boolean handleGeneralSuccessSignal() {
        if (TEST_MODE) {
            System.out.println("⚠️ 【测试模式】忽略通用成功信号");
            return false;
        }
        synchronized (consumerLock) {
            if (currentProcessingData == null) {
                System.out.println("⚠️ 当前没有正在处理的数据，忽略通用成功信号");
                return false;
            }

            String dataId = generateDataId(currentProcessingData);
            System.out.println("✅ 收到通用成功信号，处理当前数据: " + dataId);
            ackCurrentItem(dataId);
            return true;
        }
    }

    private String generateDataId(Map<String, Object> data) {
        if (data == null) {
            return "NULL_DATA_" + System.currentTimeMillis();
        }

        if (data.containsKey("_dataId")) {
            return (String) data.get("_dataId");
        }

        String dataId = "DATA_" + UUID.randomUUID().toString().substring(0, 8) + "_" + System.currentTimeMillis();
        data.put("_dataId", dataId);
        return dataId;
    }

    @Transactional
    public void moveToQueue2(Map<String, Object> data, String reason) {
        try {
            String dataId = generateDataId(data);

            qrCodeQueueMapper.moveToQueue2(dataId, reason);
            qrCodeQueueMapper.deleteCurrentProcessing(dataId);
            notifyQueueStatus();

            System.out.println("❌ 数据移动到队列二: " + dataId + "，原因: " + reason);
        } catch (Exception e) {
            System.err.println("移动到队列二失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Transactional
    public List<Map<String, Object>> exportQueue2Data() {
        try {
            List<Map<String, Object>> dbDataList = qrCodeQueueMapper.selectAllFromQueue2();
            List<Map<String, Object>> exportData = new ArrayList<>();

            for (Map<String, Object> dbData : dbDataList) {
                String contentJson = (String) dbData.get("content_json");
                Map<String, Object> data = objectMapper.readValue(contentJson, Map.class);
                exportData.add(data);

                String dataId = (String) dbData.get("data_id");
                qrCodeQueueMapper.deleteFromQueue2(dataId);
            }

            notifyQueueStatus();
            System.out.println("📤 导出队列二数据，数量: " + exportData.size());

            return exportData;
        } catch (Exception e) {
            throw new RuntimeException("导出队列二数据失败", e);
        }
    }

    public Map<String, Object> getQueueStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("queue1Size", getQueue1Size());
        status.put("queue2Size", getQueue2Size());
        status.put("currentProcessing", currentProcessingData != null);
        status.put("currentDataId", currentProcessingData != null ?
                generateDataId(currentProcessingData) : null);
        status.put("refreshCount", refreshCount.get());
        status.put("timestamp", System.currentTimeMillis());

        return status;
    }

    public Map<String, Object> getCurrentProcessingData() {
        return currentProcessingData;
    }

    private void publishQrUpdate(Map<String, Object> data) {
        if (callback != null) {
            callback.onQrCodeUpdate(data);
        }
    }

    private void notifyQueueStatus() {
        if (callback != null) {
            callback.onQueueStatusUpdate(getQueue1Size(), getQueue2Size());
        }
    }

    @Transactional
    public void clearAllQueues() {
        qrCodeQueueMapper.clearQueue(QUEUE_TYPE_1);
        qrCodeQueueMapper.clearQueue(QUEUE_TYPE_2);
        qrCodeQueueMapper.clearCurrentProcessing();

        synchronized (consumerLock) {
            clearCurrentState();
            consumerIdle = true;
        }
        notifyQueueStatus();
        System.out.println("所有队列已清空！");
    }

    public List<Map<String, Object>> getQueue1AllData() {
        try {
            List<Map<String, Object>> dbDataList = qrCodeQueueMapper.selectFirstFromQueue1();
            List<Map<String, Object>> result = new ArrayList<>();

            for (Map<String, Object> dbData : dbDataList) {
                String contentJson = (String) dbData.get("content_json");
                Map<String, Object> data = objectMapper.readValue(contentJson, Map.class);
                result.add(data);
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException("获取队列一数据失败", e);
        }
    }

    public List<Map<String, Object>> getQueue2AllData() {
        try {
            List<Map<String, Object>> dbDataList = qrCodeQueueMapper.selectAllFromQueue2();
            List<Map<String, Object>> result = new ArrayList<>();

            for (Map<String, Object> dbData : dbDataList) {
                String contentJson = (String) dbData.get("content_json");
                Map<String, Object> data = objectMapper.readValue(contentJson, Map.class);
                result.add(data);
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException("获取队列二数据失败", e);
        }
    }

    public void checkAndStartProcessing() {
        if (refreshTask == null || refreshTask.isCancelled()) {
            startProcessing();
            System.out.println("🔄 自动启动队列处理");
        }
    }

    @Transactional
    public boolean removeFromQueue2(String dataId) {
        int result = qrCodeQueueMapper.deleteFromQueue2(dataId);
        if (result > 0) {
            notifyQueueStatus();
            System.out.println("🗑️ 从队列二删除数据: " + dataId);
            return true;
        }
        return false;
    }

    public void stopProcessing() {
        if (refreshTask != null) {
            refreshTask.cancel(true);
        }
    }

    @PostConstruct
    public void init() {
        System.out.println("🔧 QrCodeQueueManager 初始化..."
                + (TEST_MODE ? "（测试模式：每张二维码展示 " + DISPLAY_DURATION_SECONDS + " 秒后自动下一张）" : ""));
        startProcessing();
    }

    public int countCompletedData() {
        try {
            return qrMapper.countCompletedData();
        } catch (Exception e) {
            return 0;
        }
    }

    public List<Map<String, Object>> selectCompletedDataBeforeDate(Date cutoffDate) {
        try {
            return qrMapper.selectCompletedDataBeforeDate(cutoffDate);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> selectAllCompletedData() {
        try {
            return qrMapper.selectAllCompletedData();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
