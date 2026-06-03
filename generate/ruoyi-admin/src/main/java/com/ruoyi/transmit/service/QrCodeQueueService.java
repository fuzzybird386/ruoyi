package com.ruoyi.transmit.service;


import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class QrCodeQueueService {
//    // 消息队列：存储待处理的二维码数据
    private final BlockingQueue<Map<String, String>> messageQueue = new LinkedBlockingQueue<>();
//
    // 未成功数据存储
    private final ConcurrentHashMap<String, Map<String, Object>> failedDataMap = new ConcurrentHashMap<>();

    // 二维码刷新状态跟踪
    private final ConcurrentHashMap<String, QrRefreshTask> refreshTasks = new ConcurrentHashMap<>();

    // 队列一：待处理的正常数据队列
    private final BlockingQueue<Map<String, Object>> queue1 = new LinkedBlockingQueue<>();

    // 队列二：未成功数据队列
    private final BlockingQueue<Map<String, Object>> queue2 = new LinkedBlockingQueue<>();

    // 当前正在处理的数据
    private volatile Map<String, Object> currentProcessingData = null;

    // 刷新计数器
    private final AtomicInteger refreshCount = new AtomicInteger(0);

    // 定时任务执行器
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> refreshTask = null;

    // WebSocket通知回调
    private QrCodeUpdateCallback callback;

    public interface QrCodeUpdateCallback {
        void onQrCodeUpdate(Map<String, Object> data);
        void onQueueStatusUpdate(int queue1Size, int queue2Size);
    }

    public void setCallback(QrCodeUpdateCallback callback) {
        this.callback = callback;
    }

    /**
     * 添加数据到队列一
     */
    public void addToQueue1(Map<String, Object> data) {
        try {
            String dataId = "DATA_" + System.currentTimeMillis() + "_" + data.hashCode();
            data.put("_dataId", dataId);
            data.put("_createTime", System.currentTimeMillis());
            data.put("_refreshCount", 0);
            data.put("_status", "waiting");

            queue1.put(data);
            notifyQueueStatus();
            System.out.println("✅ 数据已添加到队列一，ID: " + dataId + "，队列大小: " + queue1.size());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("添加数据到队列失败", e);
        }
    }

    /**
     * 开始处理队列
     */
    public void startProcessing() {
        if (refreshTask != null && !refreshTask.isCancelled()) {
            return; // 已经在运行
        }

        refreshTask = scheduler.scheduleAtFixedRate(() -> {
            try {
                processNextData();
            } catch (Exception e) {
                System.err.println("处理数据异常: " + e.getMessage());
            }
        }, 0, 3, TimeUnit.SECONDS); // 每3秒处理一次
    }

    /**
     * 停止处理队列
     */
    public void stopProcessing() {
        if (refreshTask != null) {
            refreshTask.cancel(true);
        }
        currentProcessingData = null;
        refreshCount.set(0);
    }

    /**
     * 处理下一条数据
     */
    private void processNextData() {
        // 如果当前有数据正在处理，检查刷新次数
        if (currentProcessingData != null) {
            int count = refreshCount.incrementAndGet();
            currentProcessingData.put("_refreshCount", count);
            currentProcessingData.put("_lastRefresh", System.currentTimeMillis());

            System.out.println("🔄 刷新二维码，数据ID: " + currentProcessingData.get("_dataId") +
                    "，刷新次数: " + count);

            // 通知前端更新二维码
            if (callback != null) {
                callback.onQrCodeUpdate(currentProcessingData);
            }

            // 如果刷新达到10次，移动到队列二
            if (count >= 10) {
                moveToQueue2(currentProcessingData, "刷新10次未收到成功信号");
                currentProcessingData = null;
                refreshCount.set(0);
            }
            return;
        }

        // 如果没有当前处理数据，从队列一获取新数据
        try {
            currentProcessingData = queue1.poll(100, TimeUnit.MILLISECONDS);
            if (currentProcessingData != null) {
                refreshCount.set(1);
                currentProcessingData.put("_refreshCount", 1);
                currentProcessingData.put("_status", "processing");
                currentProcessingData.put("_startProcessTime", System.currentTimeMillis());

                System.out.println("🎯 开始处理新数据，ID: " + currentProcessingData.get("_dataId") +
                        "，刷新次数: 1");

                // 通知前端显示新二维码
                if (callback != null) {
                    callback.onQrCodeUpdate(currentProcessingData);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 处理TCP成功信号
     */
    public boolean handleSuccessSignal(String dataId) {
        if (currentProcessingData != null &&
                dataId.equals(currentProcessingData.get("_dataId"))) {
            System.out.println("✅ 收到成功信号，删除数据: " + dataId);
            currentProcessingData = null;
            refreshCount.set(0);
            return true;
        }
        return false;
    }

    /**
     * 移动到队列二
     */
    private void moveToQueue2(Map<String, Object> data, String reason) {
        try {
            data.put("_status", "failed");
            data.put("_failReason", reason);
            data.put("_failTime", System.currentTimeMillis());

            queue2.put(data);
            notifyQueueStatus();

            System.out.println("❌ 数据移动到队列二，ID: " + data.get("_dataId") +
                    "，原因: " + reason + "，队列二大小: " + queue2.size());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 导出队列二数据
     */
    public java.util.List<Map<String, Object>> exportQueue2Data() {
        java.util.List<Map<String, Object>> exportData = new java.util.ArrayList<>();

        // 转移所有队列二数据到导出列表
        while (!queue2.isEmpty()) {
            Map<String, Object> data = queue2.poll();
            if (data != null) {
                exportData.add(data);
            }
        }

        notifyQueueStatus();
        System.out.println("📤 导出队列二数据，数量: " + exportData.size());

        return exportData;
    }

    /**
     * 获取队列状态
     */
    public Map<String, Object> getQueueStatus() {
        Map<String, Object> status = new java.util.HashMap<>();
        status.put("queue1Size", queue1.size());
        status.put("queue2Size", queue2.size());
        status.put("currentProcessing", currentProcessingData != null);
        status.put("currentDataId", currentProcessingData != null ?
                currentProcessingData.get("_dataId") : null);
        status.put("refreshCount", refreshCount.get());

        return status;
    }

    private void notifyQueueStatus() {
        if (callback != null) {
            callback.onQueueStatusUpdate(queue1.size(), queue2.size());
        }
    }
    /**
     * 添加数据到消息队列
     */
    public void addToQueue(Map<String, String> data) {
        try {
            String dataId = generateDataId(data);
            data.put("_dataId", dataId); // 添加唯一标识
            data.put("_createTime", String.valueOf(System.currentTimeMillis()));
            data.put("_refreshCount", "0"); // 初始化刷新次数

            messageQueue.put(data);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("添加数据到队列失败", e);
        }
    }

    /**
     * 从队列获取数据（非阻塞）
     */
    public Map<String, String> pollFromQueue() {
        return messageQueue.poll();
    }

    /**
     * 获取队列大小
     */
    public int getQueueSize() {
        return messageQueue.size();
    }


    /**
     * 获取所有未成功数据
     */
    public Map<String, Map<String, Object>> getAllFailedData() {
        return new ConcurrentHashMap<>(failedDataMap);
    }

    /**
     * 清空未成功数据
     */
    public void clearFailedData() {
        failedDataMap.clear();
    }

    /**
     * 开始二维码刷新任务
     */
    public void startRefreshTask(String dataId, Map<String, String> data) {
        QrRefreshTask task = new QrRefreshTask(dataId, data);
        refreshTasks.put(dataId, task);
        task.start();
    }

    /**
     * 停止二维码刷新任务
     */
    public void stopRefreshTask(String dataId) {
        QrRefreshTask task = refreshTasks.remove(dataId);
        if (task != null) {
            task.stop();
        }
    }

    /**
     * 处理TCP接收信号
     */
    public boolean handleReceiveSignal(String dataId) {
        QrRefreshTask task = refreshTasks.get(dataId);
        if (task != null) {
            task.stop();
            refreshTasks.remove(dataId);
            return true;
        }
        return false;
    }

    /**
     * 生成数据唯一ID
     */
    private String generateDataId(Map<String, String> data) {
        return data.hashCode() + "_" + System.currentTimeMillis();
    }

    /**
     * 二维码刷新任务类
     */
    private class QrRefreshTask {
        private final String dataId;
        private final Map<String, String> data;
        private volatile boolean running = true;
        private int refreshCount = 0;
        private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        public QrRefreshTask(String dataId, Map<String, String> data) {
            this.dataId = dataId;
            this.data = data;
        }

        public void start() {
            scheduler.scheduleAtFixedRate(() -> {
                if (running && refreshCount < 10) {
                    refreshCount++;
                    data.put("_refreshCount", String.valueOf(refreshCount));
                    data.put("_lastRefresh", String.valueOf(System.currentTimeMillis()));

                    // 通知前端更新二维码
                    notifyFrontendRefresh(data);

                    if (refreshCount >= 10) {
                        // 刷新10次后仍未收到TCP信号，标记为失败
                       addFailedData(data, "刷新10次未收到TCP确认信号");
                        stop();
                    }
                } else {
                    stop();
                }
            }, 0, 3, TimeUnit.SECONDS); // 每3秒刷新一次
        }

        public void stop() {
            running = false;
            scheduler.shutdown();
            refreshTasks.remove(dataId);
        }

        private void notifyFrontendRefresh(Map<String, String> data) {
            // 这里可以通过WebSocket通知前端刷新二维码
            // 实现逻辑与之前的WebSocket通知类似
        }
    }

    /**
     * 添加未成功数据
     */
    public void addFailedData(Map<String, String> data, String reason) {
        String dataId = data.get("_dataId");
        Map<String, Object> failedRecord = new ConcurrentHashMap<>();
        failedRecord.put("originalData", new ConcurrentHashMap<>(data));
        failedRecord.put("failReason", reason);
        failedRecord.put("failTime", System.currentTimeMillis());
        failedRecord.put("refreshCount", data.get("_refreshCount"));

        failedDataMap.put(dataId, failedRecord);
        System.out.println("❌ 数据标记为失败，ID: " + dataId + "，原因: " + reason);
    }
}
