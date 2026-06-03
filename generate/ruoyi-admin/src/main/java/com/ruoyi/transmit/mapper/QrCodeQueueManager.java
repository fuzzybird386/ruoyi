//package com.ruoyi.transmit.mapper;
//
//import com.ruoyi.transmit.config.QrCodeWebSocketCallback;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.*;
//import java.util.concurrent.*;
//import java.util.concurrent.atomic.AtomicInteger;
//
//@Service
//public class QrCodeQueueManager {
//    // 队列一：待处理的正常数据队列
//    private final BlockingQueue<Map<String, Object>> queue1 = new LinkedBlockingQueue<>();
//
//    // 队列二：未成功数据队列
//    private final BlockingQueue<Map<String, Object>> queue2 = new LinkedBlockingQueue<>();
//
//    // 当前正在处理的数据
//    private volatile Map<String, Object> currentProcessingData = null;
//
//    // 刷新计数器
//    private final AtomicInteger refreshCount = new AtomicInteger(0);
//
//    // 定时任务执行器
//    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
//    private ScheduledFuture<?> refreshTask = null;
//
//    // WebSocket通知回调
//    private QrCodeUpdateCallback callback;
//
//    public interface QrCodeUpdateCallback {
//        void onQrCodeUpdate(Map<String, Object> data);
//        void onQueueStatusUpdate(int queue1Size, int queue2Size);
//        void onDataSuccess(String dataId);
//        void onDataFailed(String dataId, String reason);
//    }
//
//    @Autowired
//    public void setWebSocketCallback(QrCodeWebSocketCallback callback) {
//        this.callback = callback;
//    }
//
//    public void setCallback(QrCodeUpdateCallback callback) {
//        this.callback = callback;
//    }
//
//    /**
//     * 获取队列一大小
//     */
//    public int getQueue1Size() {
//        return queue1.size();
//    }
//
//    /**
//     * 获取队列二大小
//     */
//    public int getQueue2Size() {
//        return queue2.size();
//    }
//
//    /**
//     * 获取队列一大小（兼容旧代码）
//     */
//    public int getQueueSize() {
//        return queue1.size();
//    }
//
//    /**
//     * 添加数据到队列一
//     */
//    public void addToQueue1(Map<String, Object> data) {
//        try {
//            //数据添加到队列一
//            Map<String, Object> originalData= new HashMap<>(data);
//
////            String dataId = "DATA_" + System.currentTimeMillis() + "_" + data.hashCode();
////            data.put("_dataId", dataId);
////            data.put("_createTime", System.currentTimeMillis());
////            data.put("_refreshCount", 0);
////            data.put("_status", "waiting");
//
////            queue1.put(data);
//            queue1.put(originalData);
//            notifyQueueStatus();
//            System.out.println("✅ 数据已添加到队列一: " + originalData + "，队列大小: " + queue1.size());
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            throw new RuntimeException("添加数据到队列失败", e);
//        }
//    }
//
//    /**
//     * 开始处理队列
//     */
//    public void startProcessing() {
//        if (refreshTask != null && !refreshTask.isCancelled()) {
//            return; // 已经在运行
//        }
//
//        refreshTask = scheduler.scheduleAtFixedRate(() -> {
//            try {
//                processNextData();
//            } catch (Exception e) {
//                System.err.println("处理数据异常: " + e.getMessage());
//            }
//        }, 0, 3, TimeUnit.SECONDS); // 每3秒处理一次
//    }
//
//    /**
//     * 停止处理队列
//     */
//    public void stopProcessing() {
//        if (refreshTask != null) {
//            refreshTask.cancel(true);
//        }
//        currentProcessingData = null;
//        refreshCount.set(0);
//    }
//
//    /**
//     * 处理下一条数据
//     */
//    private void processNextData() {
//        // 如果当前有数据正在处理，检查刷新次数
//        if (currentProcessingData != null) {
//            int count = refreshCount.incrementAndGet();
////            currentProcessingData.put("_refreshCount", count);
////            currentProcessingData.put("_lastRefresh", System.currentTimeMillis());
//
//            System.out.println("🔄 刷新二维码，数据ID: " + currentProcessingData.get("_dataId") +
//                    "，刷新次数: " + count);
//
//            // 通知前端更新二维码
//            if (callback != null) {
//                callback.onQrCodeUpdate(currentProcessingData);
//            }
//
//            // 如果刷新达到10次，移动到队列二
//            if (count >= 10) {
//                String dataId = (String) currentProcessingData.get("_dataId");
//                moveToQueue2(currentProcessingData, "刷新10次未收到成功信号");
//                currentProcessingData = null;
//                refreshCount.set(0);
//
//                if (callback != null) {
//                    callback.onDataFailed(dataId, "刷新10次未收到成功信号");
//                }
//
//                // 立即处理下一条数据
//                processNextDataImmediately();
//            }
//            return;
//        }
//
//        // 如果没有当前处理数据，从队列一获取新数据
//        try {
//            currentProcessingData = queue1.poll(100, TimeUnit.MILLISECONDS);
//            if (currentProcessingData != null) {
//                refreshCount.set(1);
////                currentProcessingData.put("_refreshCount", 1);
////                currentProcessingData.put("_status", "processing");
////                currentProcessingData.put("_startProcessTime", System.currentTimeMillis());
//
//                System.out.println("🎯 开始处理新数据: " + currentProcessingData);
//
//                // 通知前端显示新二维码
//                if (callback != null) {
//                    callback.onQrCodeUpdate(currentProcessingData);
//                }
//            }
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
//    }
//    /**
//     * 立即处理下一条数据（不等待3秒）
//     */
//    private void processNextDataImmediately() {
//        try {
//            currentProcessingData = queue1.poll(100, TimeUnit.MILLISECONDS);
//            if (currentProcessingData != null) {
//                refreshCount.set(1);
////                currentProcessingData.put("_refreshCount", 1);
////                currentProcessingData.put("_status", "processing");
////                currentProcessingData.put("_startProcessTime", System.currentTimeMillis());
//
//                System.out.println("🎯 立即开始处理下一条数据:{} " + currentProcessingData);
//
//                // 通知前端显示新二维码
//                if (callback != null) {
//                    callback.onQrCodeUpdate(currentProcessingData);
//                }
//            } else {
//                System.out.println("📭 队列一为空，无待处理数据");
//            }
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
//    }
//
//    /**
//     * 处理TCP成功信号
//     */
////    public boolean handleSuccessSignal(String dataId) {
////        if (currentProcessingData != null &&
////                dataId.equals(currentProcessingData.get("_dataId"))) {
////            System.out.println("✅ 收到成功信号，删除数据: " + dataId);
////
////            if(callback!=null){
////                callback.onDataSuccess(dataId);
////            }
////
////            currentProcessingData = null;
////            refreshCount.set(0);
////            //立即处理下一条
////            processNextDataImmediately();
////
////            return true;
////        }
////        return false;
////    }
//    public boolean handleSuccessSignal(String dataId) {
//        if (currentProcessingData == null) {
//            System.out.println("⚠️ 当前没有正在处理的数据，忽略成功信号: " + dataId);
//            return false;
//        }
//
//        // ✅ 生成当前数据的ID进行比较
//        String currentDataId = generateDataId(currentProcessingData);
//        if (dataId.equals(currentDataId)) {
//            System.out.println("✅ 收到成功信号，删除数据: " + dataId);
//
//            if (callback != null) {
//                callback.onDataSuccess(dataId);
//            }
//
//            currentProcessingData = null;
//            refreshCount.set(0);
//            // 立即处理下一条
//            processNextDataImmediately();
//
//            return true;
//        } else {
//            System.out.println("⚠️ 成功信号ID不匹配，当前: " + currentDataId + "，收到: " + dataId);
//            return false;
//        }
//    }
//
//    /**
//     * 处理通用成功信号（无具体dataId）
//     */
////    public boolean handleGeneralSuccessSignal() {
////        if (currentProcessingData != null) {
////            String dataId = (String) currentProcessingData.get("_dataId");
////            return handleSuccessSignal(dataId);
////        }
////        return false;
////    }
//    public boolean handleGeneralSuccessSignal() {
//        if (currentProcessingData == null) {
//            System.out.println("⚠️ 当前没有正在处理的数据，忽略通用成功信号");
//            return false;
//        }
//
//        String dataId = generateDataId(currentProcessingData);
//        System.out.println("✅ 收到通用成功信号，处理当前数据: " + dataId);
//
//        if (callback != null) {
//            callback.onDataSuccess(dataId);
//        }
//
//        currentProcessingData = null;
//        refreshCount.set(0);
//        // 立即处理下一条
//        processNextDataImmediately();
//
//        return true;
//    }
//    /**
//     * 生成数据ID（基于内容哈希）
//     */
//    private String generateDataId(Map<String, Object> data) {
//        if (data == null) {
//            return "NULL_DATA_" + System.currentTimeMillis();
//        }
//        return "DATA_" + data.hashCode() + "_" + System.currentTimeMillis();
//    }
//
//    /**
//     * 移动到队列二
//     */
//    private void moveToQueue2(Map<String, Object> data, String reason) {
//        try {
//            Map<String, Object> filedData= new HashMap<>(data);
////            data.put("_status", "failed");
////            data.put("_failReason", reason);
////            data.put("_failTime", System.currentTimeMillis());
//
////            queue2.put(data);
//            queue2.put(filedData);
//            notifyQueueStatus();
//
//            System.out.println("❌ 数据移动到队列二，{}: " + filedData +
//                    "，原因: " + reason + "，队列二大小: " + queue2.size());
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
//    }
//
//    /**
//     * 导出队列二数据
//     */
//    public java.util.List<Map<String, Object>> exportQueue2Data() {
//        java.util.List<Map<String, Object>> exportData = new java.util.ArrayList<>();
//
//        // 转移所有队列二数据到导出列表
//        while (!queue2.isEmpty()) {
//            Map<String, Object> data = queue2.poll();
//            if (data != null) {
//                exportData.add(data);
//            }
//        }
//
//        notifyQueueStatus();
//        System.out.println("📤 导出队列二数据，数量: " + exportData.size());
//
//        return exportData;
//    }
//
//    /**
//     * 获取队列状态
//     */
//    public Map<String, Object> getQueueStatus() {
//        Map<String, Object> status = new HashMap<>();
//        status.put("queue1Size", queue1.size());
//        status.put("queue2Size", queue2.size());
//        status.put("currentProcessing", currentProcessingData != null);
//        status.put("currentDataId", currentProcessingData != null ?
//                currentProcessingData.get("_dataId") : null);
//        status.put("refreshCount", refreshCount.get());
//        status.put("timestamp",System.currentTimeMillis());
//
//        return status;
//    }
//
//    /**
//     * 获取当前处理的数据
//     */
//    public Map<String, Object> getCurrentProcessingData() {
//        return currentProcessingData;
//    }
//
//    private void notifyQueueStatus() {
//        if (callback != null) {
//            callback.onQueueStatusUpdate(queue1.size(), queue2.size());
//        }
//    }
//
//    /**
//     * 清空所有队列
//     */
//    public void clearAllQueues() {
//        queue1.clear();
//        queue2.clear();
//        currentProcessingData = null;
//        refreshCount.set(0);
//        notifyQueueStatus();
//        System.out.println("所有队列已清空！");
//    }
//
//    /**
//     * 获取队列一中的所有数据（用于调试）
//     */
//    public List<Map<String, Object>> getQueue1Data() {
//        return new ArrayList<>(queue1);
//    }
//
//    /**
//     * 获取队列二中的所有数据（用于调试）
//     */
//    public List<Map<String, Object>> getQueue2Data() {
//        return new ArrayList<>(queue2);
//    }
//
//
//    /**
//     * 检查并自动启动队列处理
//     */
//    public void checkAndStartProcessing() {
//        if (refreshTask == null || refreshTask.isCancelled()) {
//            startProcessing();
//            System.out.println("🔄 自动启动队列处理");
//        }
//    }
//
//    /**
//     * 获取队列一中的所有数据（用于前端显示）
//     */
//    public List<Map<String, Object>> getQueue1AllData() {
//        return new ArrayList<>(queue1);
//    }
//
//    /**
//     * 获取队列二中的所有数据（用于前端显示）
//     */
//    public List<Map<String, Object>> getQueue2AllData() {
//        return new ArrayList<>(queue2);
//    }
//
//    /**
//     * 删除队列二中的指定数据（导出后删除）
//     */
//    public boolean removeFromQueue2(String dataId) {
//        Iterator<Map<String, Object>> iterator = queue2.iterator();
//        while (iterator.hasNext()) {
//            Map<String, Object> data = iterator.next();
//            if (dataId.equals(data.get("_dataId"))) {
//                iterator.remove();
//                notifyQueueStatus();
//                System.out.println("🗑️ 从队列二删除数据: " + dataId);
//                return true;
//            }
//        }
//        return false;
//    }
//}

/**
 * 存储数据库  --未使用mybatis映射
 */
//package com.ruoyi.transmit.mapper;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.ruoyi.transmit.config.QrCodeWebSocketCallback;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.*;
//import java.util.concurrent.*;
//import java.util.concurrent.atomic.AtomicInteger;
//
//@Service
//public class QrCodeQueueManager {
//
//    // 队列类型常量
//    private static final int QUEUE_TYPE_1 = 1; // 队列一
//    private static final int QUEUE_TYPE_2 = 2; // 队列二
//
//    // 状态常量
//    private static final int STATUS_PENDING = 1;    // 待处理
//    private static final int STATUS_PROCESSING = 2; // 处理中
//    private static final int STATUS_COMPLETED = 3;  // 已完成
//    private static final int STATUS_FAILED = 4;     // 失败
//
//    @Autowired
//    private QrMapper qrCodeQueueMapper;
//
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    // 当前正在处理的数据（缓存，减少数据库查询）
//    private volatile Map<String, Object> currentProcessingData = null;
//
//    // 刷新计数器
//    private final AtomicInteger refreshCount = new AtomicInteger(0);
//
//    // 定时任务执行器
//    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
//    private ScheduledFuture<?> refreshTask = null;
//
//    // WebSocket通知回调
//    private QrCodeUpdateCallback callback;
//
//    public interface QrCodeUpdateCallback {
//        void onQrCodeUpdate(Map<String, Object> data);
//        void onQueueStatusUpdate(int queue1Size, int queue2Size);
//        void onDataSuccess(String dataId);
//        void onDataFailed(String dataId, String reason);
//    }
//
//    @Autowired
//    public void setWebSocketCallback(QrCodeWebSocketCallback callback) {
//        this.callback = callback;
//    }
//
//    public void setCallback(QrCodeUpdateCallback callback) {
//        this.callback = callback;
//    }
//
//    /**
//     * 获取队列一大小
//     */
//    public int getQueue1Size() {
//        return qrCodeQueueMapper.getQueueSize(QUEUE_TYPE_1, STATUS_PENDING);
//    }
//
//    /**
//     * 获取队列二大小
//     */
//    public int getQueue2Size() {
//        return qrCodeQueueMapper.getQueueSize(QUEUE_TYPE_2, STATUS_FAILED);
//    }
//
//    /**
//     * 添加数据到队列一
//     */
//    @Transactional
//    public void addToQueue1(Map<String, Object> data) {
//        try {
//            String dataId = generateDataId(data);
//            String contentJson = objectMapper.writeValueAsString(data);
//
//            // 保存到数据库
//            qrCodeQueueMapper.insertQueueData(dataId, QUEUE_TYPE_1, contentJson, STATUS_PENDING);
//
//            notifyQueueStatus();
//            System.out.println("✅ 数据已添加到队列一: " + dataId + "，队列大小: " + getQueue1Size());
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException("数据序列化失败", e);
//        }
//    }
//
//    /**
//     * 开始处理队列
//     */
//    public void startProcessing() {
//        if (refreshTask != null && !refreshTask.isCancelled()) {
//            return; // 已经在运行
//        }
//
//        // 启动时恢复当前处理的数据
//        restoreCurrentProcessingData();
//
//        refreshTask = scheduler.scheduleAtFixedRate(() -> {
//            try {
//                processNextData();
//            } catch (Exception e) {
//                System.err.println("处理数据异常: " + e.getMessage());
//                e.printStackTrace();
//            }
//        }, 0, 3, TimeUnit.SECONDS);
//    }
//
//    /**
//     * 恢复当前处理的数据
//     */
//    private void restoreCurrentProcessingData() {
//        try {
//            Map<String, Object> dbCurrent = qrCodeQueueMapper.selectCurrentProcessing();
//            if (dbCurrent != null) {
//                String contentJson = (String) dbCurrent.get("content_json");
//                Map<String, Object> data = objectMapper.readValue(contentJson, Map.class);
//
//                currentProcessingData = data;
//                refreshCount.set((Integer) dbCurrent.get("refresh_count"));
//
//                System.out.println("🔄 恢复当前处理数据: " + data.get("_dataId"));
//            }
//        } catch (Exception e) {
//            System.err.println("恢复当前处理数据失败: " + e.getMessage());
//        }
//    }
//
//    /**
//     * 处理下一条数据
//     */
//    @Transactional
//    public void processNextData() {
//        // 如果当前有数据正在处理，检查刷新次数
//        if (currentProcessingData != null) {
//            int count = refreshCount.incrementAndGet();
//
//            // 更新数据库中的刷新次数
//            String currentDataId = generateDataId(currentProcessingData);
//            qrCodeQueueMapper.updateCurrentProcessingRefreshCount(currentDataId, count);
//
//            System.out.println("🔄 刷新二维码，数据ID: " + currentDataId + "，刷新次数: " + count);
//
//            // 通知前端更新二维码
//            if (callback != null) {
//                callback.onQrCodeUpdate(currentProcessingData);
//            }
//
//            // 如果刷新达到10次，移动到队列二
//            if (count >= 10) {
//                String dataId = generateDataId(currentProcessingData);
//                moveToQueue2(currentProcessingData, "刷新10次未收到成功信号");
//                currentProcessingData = null;
//                refreshCount.set(0);
//
//                if (callback != null) {
//                    callback.onDataFailed(dataId, "刷新10次未收到成功信号");
//                }
//
//                // 立即处理下一条数据
//                processNextDataImmediately();
//            }
//            return;
//        }
//
//        // 如果没有当前处理数据，从队列一获取新数据
//        try {
//            Map<String, Object> dbData = qrCodeQueueMapper.selectFirstFromQueue1();
//            if (dbData != null) {
//                String contentJson = (String) dbData.get("content_json");
//                Map<String, Object> data = objectMapper.readValue(contentJson, Map.class);
//                String dataId = (String) dbData.get("data_id");
//
//                // 设置为当前处理数据
//                currentProcessingData = data;
//                refreshCount.set(1);
//
//                // 更新数据状态为处理中
//                qrCodeQueueMapper.updateDataStatus(dataId, STATUS_PROCESSING);
//
//                // 保存到当前处理表
//                qrCodeQueueMapper.insertCurrentProcessing(dataId, contentJson, 1);
//
//                System.out.println("🎯 开始处理新数据: " + dataId);
//
//                // 通知前端显示新二维码
//                if (callback != null) {
//                    callback.onQrCodeUpdate(currentProcessingData);
//                }
//            }
//        } catch (Exception e) {
//            System.err.println("从队列一获取数据失败: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 立即处理下一条数据
//     */
//    @Transactional
//    public void processNextDataImmediately() {
//        try {
//            Map<String, Object> dbData = qrCodeQueueMapper.selectFirstFromQueue1();
//            if (dbData != null) {
//                String contentJson = (String) dbData.get("content_json");
//                Map<String, Object> data = objectMapper.readValue(contentJson, Map.class);
//                String dataId = (String) dbData.get("data_id");
//
//                currentProcessingData = data;
//                refreshCount.set(1);
//
//                // 更新数据状态为处理中
//                qrCodeQueueMapper.updateDataStatus(dataId, STATUS_PROCESSING);
//
//                // 保存到当前处理表
//                qrCodeQueueMapper.insertCurrentProcessing(dataId, contentJson, 1);
//
//                System.out.println("🎯 立即开始处理下一条数据: " + dataId);
//
//                if (callback != null) {
//                    callback.onQrCodeUpdate(currentProcessingData);
//                }
//            } else {
//                System.out.println("📭 队列一为空，无待处理数据");
//            }
//        } catch (Exception e) {
//            System.err.println("立即处理下一条数据失败: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 处理TCP成功信号
//     */
//    @Transactional
//    public boolean handleSuccessSignal(String dataId) {
//        if (currentProcessingData == null) {
//            System.out.println("⚠️ 当前没有正在处理的数据，忽略成功信号: " + dataId);
//            return false;
//        }
//
//        String currentDataId = generateDataId(currentProcessingData);
//        if (dataId.equals(currentDataId)) {
//            System.out.println("✅ 收到成功信号，完成数据: " + dataId);
//
//            // 更新数据状态为已完成
//            qrCodeQueueMapper.updateDataStatus(dataId, STATUS_COMPLETED);
//
//            // 删除当前处理记录
//            qrCodeQueueMapper.deleteCurrentProcessing(dataId);
//
//            if (callback != null) {
//                callback.onDataSuccess(dataId);
//            }
//
//            currentProcessingData = null;
//            refreshCount.set(0);
//
//            // 立即处理下一条
//            processNextDataImmediately();
//
//            return true;
//        } else {
//            System.out.println("⚠️ 成功信号ID不匹配，当前: " + currentDataId + "，收到: " + dataId);
//            return false;
//        }
//    }
//
//    /**
//     * 处理通用成功信号
//     */
//    @Transactional
//    public boolean handleGeneralSuccessSignal() {
//        if (currentProcessingData == null) {
//            System.out.println("⚠️ 当前没有正在处理的数据，忽略通用成功信号");
//            return false;
//        }
//
//        String dataId = generateDataId(currentProcessingData);
//        System.out.println("✅ 收到通用成功信号，处理当前数据: " + dataId);
//
//        // 更新数据状态为已完成
//        qrCodeQueueMapper.updateDataStatus(dataId, STATUS_COMPLETED);
//
//        // 删除当前处理记录
//        qrCodeQueueMapper.deleteCurrentProcessing(dataId);
//
//        if (callback != null) {
//            callback.onDataSuccess(dataId);
//        }
//
//        currentProcessingData = null;
//        refreshCount.set(0);
//
//        processNextDataImmediately();
//
//        return true;
//    }
//
//    /**
//     * 生成数据ID
//     */
//    private String generateDataId(Map<String, Object> data) {
//        if (data == null) {
//            return "NULL_DATA_" + System.currentTimeMillis();
//        }
//
//        // 如果数据中已有_dataId，则使用它
//        if (data.containsKey("_dataId")) {
//            return (String) data.get("_dataId");
//        }
//
//        // 否则生成新的ID
//        String dataId = "DATA_" + UUID.randomUUID().toString().substring(0, 8) + "_" + System.currentTimeMillis();
//        data.put("_dataId", dataId);
//        return dataId;
//    }
//
//    /**
//     * 移动到队列二
//     */
//    @Transactional
//    public void moveToQueue2(Map<String, Object> data, String reason) {
//        try {
//            String dataId = generateDataId(data);
//
//            // 更新数据库，移动到队列二
//            qrCodeQueueMapper.moveToQueue2(dataId, reason);
//
//            // 删除当前处理记录
//            qrCodeQueueMapper.deleteCurrentProcessing(dataId);
//
//            notifyQueueStatus();
//
//            System.out.println("❌ 数据移动到队列二: " + dataId + "，原因: " + reason);
//        } catch (Exception e) {
//            System.err.println("移动到队列二失败: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 导出队列二数据
//     */
//    @Transactional
//    public List<Map<String, Object>> exportQueue2Data() {
//        try {
//            List<Map<String, Object>> dbDataList = qrCodeQueueMapper.selectAllFromQueue2();
//            List<Map<String, Object>> exportData = new ArrayList<>();
//
//            for (Map<String, Object> dbData : dbDataList) {
//                String contentJson = (String) dbData.get("content_json");
//                Map<String, Object> data = objectMapper.readValue(contentJson, Map.class);
//                exportData.add(data);
//
//                // 从数据库删除
//                String dataId = (String) dbData.get("data_id");
//                qrCodeQueueMapper.deleteFromQueue2(dataId);
//            }
//
//            notifyQueueStatus();
//            System.out.println("📤 导出队列二数据，数量: " + exportData.size());
//
//            return exportData;
//        } catch (Exception e) {
//            throw new RuntimeException("导出队列二数据失败", e);
//        }
//    }
//
//    /**
//     * 获取队列状态
//     */
//    public Map<String, Object> getQueueStatus() {
//        Map<String, Object> status = new HashMap<>();
//        status.put("queue1Size", getQueue1Size());
//        status.put("queue2Size", getQueue2Size());
//        status.put("currentProcessing", currentProcessingData != null);
//        status.put("currentDataId", currentProcessingData != null ?
//                generateDataId(currentProcessingData) : null);
//        status.put("refreshCount", refreshCount.get());
//        status.put("timestamp", System.currentTimeMillis());
//
//        return status;
//    }
//
//    /**
//     * 获取当前处理的数据
//     */
//    public Map<String, Object> getCurrentProcessingData() {
//        return currentProcessingData;
//    }
//
//    private void notifyQueueStatus() {
//        if (callback != null) {
//            callback.onQueueStatusUpdate(getQueue1Size(), getQueue2Size());
//        }
//    }
//
//    /**
//     * 清空所有队列
//     */
//    @Transactional
//    public void clearAllQueues() {
//        qrCodeQueueMapper.clearQueue(QUEUE_TYPE_1);
//        qrCodeQueueMapper.clearQueue(QUEUE_TYPE_2);
//        qrCodeQueueMapper.clearCurrentProcessing();
//
//        currentProcessingData = null;
//        refreshCount.set(0);
//        notifyQueueStatus();
//        System.out.println("所有队列已清空！");
//    }
//
//    /**
//     * 获取队列一中的所有数据
//     */
//    public List<Map<String, Object>> getQueue1AllData() {
//        try {
//            List<Map<String, Object>> dbDataList = qrCodeQueueMapper.selectAllFromQueue1();
//            List<Map<String, Object>> result = new ArrayList<>();
//
//            for (Map<String, Object> dbData : dbDataList) {
//                String contentJson = (String) dbData.get("content_json");
//                Map<String, Object> data = objectMapper.readValue(contentJson, Map.class);
//                result.add(data);
//            }
//
//            return result;
//        } catch (Exception e) {
//            throw new RuntimeException("获取队列一数据失败", e);
//        }
//    }
//
//    /**
//     * 获取队列二中的所有数据
//     */
//    public List<Map<String, Object>> getQueue2AllData() {
//        try {
//            List<Map<String, Object>> dbDataList = qrCodeQueueMapper.selectAllFromQueue2();
//            List<Map<String, Object>> result = new ArrayList<>();
//
//            for (Map<String, Object> dbData : dbDataList) {
//                String contentJson = (String) dbData.get("content_json");
//                Map<String, Object> data = objectMapper.readValue(contentJson, Map.class);
//                result.add(data);
//            }
//
//            return result;
//        } catch (Exception e) {
//            throw new RuntimeException("获取队列二数据失败", e);
//        }
//    }
//
//    /**
//     * 检查并自动启动队列处理
//     */
//    public void checkAndStartProcessing() {
//        if (refreshTask == null || refreshTask.isCancelled()) {
//            startProcessing();
//            System.out.println("🔄 自动启动队列处理");
//        }
//    }
//
//    /**
//     * 删除队列二中的指定数据
//     */
//    @Transactional
//    public boolean removeFromQueue2(String dataId) {
//        int result = qrCodeQueueMapper.deleteFromQueue2(dataId);
//        if (result > 0) {
//            notifyQueueStatus();
//            System.out.println("🗑️ 从队列二删除数据: " + dataId);
//            return true;
//        }
//        return false;
//    }
//
//    /**
//     * 停止处理队列
//     */
//    public void stopProcessing() {
//        if (refreshTask != null) {
//            refreshTask.cancel(true);
//        }
//        // 注意：这里不清空currentProcessingData，以便恢复时可以继续处理
//    }
//}

//package com.ruoyi.transmit.mapper;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.ruoyi.transmit.config.QrCodeWebSocketCallback;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.*;
//import java.util.concurrent.*;
//import java.util.concurrent.atomic.AtomicInteger;
//
//@Service
//public class QrCodeQueueManager {
//
//    // 队列类型常量
//    private static final int QUEUE_TYPE_1 = 1; // 队列一
//    private static final int QUEUE_TYPE_2 = 2; // 队列二
//
//    // 状态常量
//    private static final int STATUS_PENDING = 1;    // 待处理
//    private static final int STATUS_PROCESSING = 2; // 处理中
//    private static final int STATUS_COMPLETED = 3;  // 已完成
//    private static final int STATUS_FAILED = 4;     // 失败
//
//    @Autowired
//    private QrMapper qrCodeQueueMapper;
//
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    // 当前正在处理的数据（缓存，减少数据库查询）
//    private volatile Map<String, Object> currentProcessingData = null;
//
//    // 刷新计数器
//    private final AtomicInteger refreshCount = new AtomicInteger(0);
//
//    // 定时任务执行器
//    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
//    private ScheduledFuture<?> refreshTask = null;
//
//    // WebSocket通知回调
//    private QrCodeUpdateCallback callback;
//
//    public interface QrCodeUpdateCallback {
//        void onQrCodeUpdate(Map<String, Object> data);
//        void onQueueStatusUpdate(int queue1Size, int queue2Size);
//        void onDataSuccess(String dataId);
//        void onDataFailed(String dataId, String reason);
//    }
//
//    @Autowired
//    public void setWebSocketCallback(QrCodeWebSocketCallback callback) {
//        this.callback = callback;
//    }
//
//    public void setCallback(QrCodeUpdateCallback callback) {
//        this.callback = callback;
//    }
//
//    /**
//     * 获取队列一大小
//     */
//    public int getQueue1Size() {
//        return qrCodeQueueMapper.getQueueSize(QUEUE_TYPE_1, STATUS_PENDING);
//    }
//
//    /**
//     * 获取队列二大小
//     */
//    public int getQueue2Size() {
//        return qrCodeQueueMapper.getQueueSize(QUEUE_TYPE_2, STATUS_FAILED);
//    }
//
//    /**
//     * 添加数据到队列一
//     */
//    @Transactional
//    public void addToQueue1(Map<String, Object> data) {
//        try {
//            String dataId = generateDataId(data);
//            String contentJson = objectMapper.writeValueAsString(data);
//
//            // 保存到数据库
//            qrCodeQueueMapper.insertQueueData(dataId, QUEUE_TYPE_1, contentJson, STATUS_PENDING);
//
//            notifyQueueStatus();
//            System.out.println("✅ 数据已添加到队列一: " + dataId + "，队列大小: " + getQueue1Size());
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException("数据序列化失败", e);
//        }
//    }
//
//    /**
//     * 开始处理队列
//     */
//    public void startProcessing() {
//        if (refreshTask != null && !refreshTask.isCancelled()) {
//            return; // 已经在运行
//        }
//
//        // 启动时恢复当前处理的数据
//        restoreCurrentProcessingData();
//
//        refreshTask = scheduler.scheduleAtFixedRate(() -> {
//            try {
//                processNextData();
//            } catch (Exception e) {
//                System.err.println("处理数据异常: " + e.getMessage());
//                e.printStackTrace();
//            }
//        }, 0, 3, TimeUnit.SECONDS);
//    }
//
//    /**
//     * 恢复当前处理的数据
//     */
//    private void restoreCurrentProcessingData() {
//        try {
//            Map<String, Object> dbCurrent = qrCodeQueueMapper.selectCurrentProcessing();
//            if (dbCurrent != null) {
//                String contentJson = (String) dbCurrent.get("content_json");
//                Map<String, Object> data = objectMapper.readValue(contentJson, Map.class);
//
//                currentProcessingData = data;
//                refreshCount.set((Integer) dbCurrent.get("refresh_count"));
//
//                System.out.println("🔄 恢复当前处理数据: " + data.get("_dataId"));
//            }
//        } catch (Exception e) {
//            System.err.println("恢复当前处理数据失败: " + e.getMessage());
//        }
//    }
//
//    /**
//     * 处理下一条数据
//     */
//    @Transactional
//    public void processNextData() {
//        // 如果当前有数据正在处理，检查刷新次数
//        if (currentProcessingData != null) {
//            int count = refreshCount.incrementAndGet();
//
//            // 更新数据库中的刷新次数
//            String currentDataId = generateDataId(currentProcessingData);
//            qrCodeQueueMapper.updateCurrentProcessingRefreshCount(currentDataId, count);
//
//            System.out.println("🔄 刷新二维码，数据ID: " + currentDataId + "，刷新次数: " + count);
//
//            // 通知前端更新二维码
//            if (callback != null) {
//                callback.onQrCodeUpdate(currentProcessingData);
//            }
//
//            // 如果刷新达到10次，移动到队列二
//            if (count >= 10) {
//                String dataId = generateDataId(currentProcessingData);
//                moveToQueue2(currentProcessingData, "刷新10次未收到成功信号");
//                currentProcessingData = null;
//                refreshCount.set(0);
//
//                if (callback != null) {
//                    callback.onDataFailed(dataId, "刷新10次未收到成功信号");
//                }
//
//                // 立即处理下一条数据
//                processNextDataImmediately();
//            }
//            return;
//        }
//
//        // 如果没有当前处理数据，从队列一获取新数据
//        try {
//            Map<String, Object> dbData = qrCodeQueueMapper.selectFirstFromQueue1();
//            if (dbData != null) {
//                String contentJson = (String) dbData.get("content_json");
//                Map<String, Object> data = objectMapper.readValue(contentJson, Map.class);
//                String dataId = (String) dbData.get("data_id");
//
//                // 设置为当前处理数据
//                currentProcessingData = data;
//                refreshCount.set(1);
//
//                // 更新数据状态为处理中
//                qrCodeQueueMapper.updateDataStatus(dataId, STATUS_PROCESSING);
//
//                // 保存到当前处理表
//                qrCodeQueueMapper.insertCurrentProcessing(dataId, contentJson, 1);
//
//                System.out.println("🎯 开始处理新数据: " + dataId);
//
//                // 通知前端显示新二维码
//                if (callback != null) {
//                    callback.onQrCodeUpdate(currentProcessingData);
//                }
//            }
//        } catch (Exception e) {
//            System.err.println("从队列一获取数据失败: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 立即处理下一条数据
//     */
//    @Transactional
//    public void processNextDataImmediately() {
//        try {
//            Map<String, Object> dbData = qrCodeQueueMapper.selectFirstFromQueue1();
//            if (dbData != null) {
//                String contentJson = (String) dbData.get("content_json");
//                Map<String, Object> data = objectMapper.readValue(contentJson, Map.class);
//                String dataId = (String) dbData.get("data_id");
//
//                currentProcessingData = data;
//                refreshCount.set(1);
//
//                // 更新数据状态为处理中
//                qrCodeQueueMapper.updateDataStatus(dataId, STATUS_PROCESSING);
//
//                // 保存到当前处理表
//                qrCodeQueueMapper.insertCurrentProcessing(dataId, contentJson, 1);
//
//                System.out.println("🎯 立即开始处理下一条数据: " + dataId);
//
//                if (callback != null) {
//                    callback.onQrCodeUpdate(currentProcessingData);
//                }
//            } else {
//                System.out.println("📭 队列一为空，无待处理数据");
//            }
//        } catch (Exception e) {
//            System.err.println("立即处理下一条数据失败: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 处理TCP成功信号
//     */
//    @Transactional
//    public boolean handleSuccessSignal(String dataId) {
//        if (currentProcessingData == null) {
//            System.out.println("⚠️ 当前没有正在处理的数据，忽略成功信号: " + dataId);
//            return false;
//        }
//
//        String currentDataId = generateDataId(currentProcessingData);
//        if (dataId.equals(currentDataId)) {
//            System.out.println("✅ 收到成功信号，完成数据: " + dataId);
//
//            // 更新数据状态为已完成
//            qrCodeQueueMapper.updateDataStatus(dataId, STATUS_COMPLETED);
//
//            // 删除当前处理记录
//            qrCodeQueueMapper.deleteCurrentProcessing(dataId);
//
//            if (callback != null) {
//                callback.onDataSuccess(dataId);
//            }
//
//            currentProcessingData = null;
//            refreshCount.set(0);
//
//            // 立即处理下一条
//            processNextDataImmediately();
//
//            return true;
//        } else {
//            System.out.println("⚠️ 成功信号ID不匹配，当前: " + currentDataId + "，收到: " + dataId);
//            return false;
//        }
//    }
//
//    /**
//     * 处理通用成功信号
//     */
//    @Transactional
//    public boolean handleGeneralSuccessSignal() {
//        if (currentProcessingData == null) {
//            System.out.println("⚠️ 当前没有正在处理的数据，忽略通用成功信号");
//            return false;
//        }
//
//        String dataId = generateDataId(currentProcessingData);
//        System.out.println("✅ 收到通用成功信号，处理当前数据: " + dataId);
//
//        // 更新数据状态为已完成
//        qrCodeQueueMapper.updateDataStatus(dataId, STATUS_COMPLETED);
//
//        // 删除当前处理记录
//        qrCodeQueueMapper.deleteCurrentProcessing(dataId);
//
//        if (callback != null) {
//            callback.onDataSuccess(dataId);
//        }
//
//        currentProcessingData = null;
//        refreshCount.set(0);
//
//        processNextDataImmediately();
//
//        return true;
//    }
//
//    /**
//     * 生成数据ID
//     */
//    private String generateDataId(Map<String, Object> data) {
//        if (data == null) {
//            return "NULL_DATA_" + System.currentTimeMillis();
//        }
//
//        // 如果数据中已有_dataId，则使用它
//        if (data.containsKey("_dataId")) {
//            return (String) data.get("_dataId");
//        }
//
//        // 否则生成新的ID
//        String dataId = "DATA_" + UUID.randomUUID().toString().substring(0, 8) + "_" + System.currentTimeMillis();
//        data.put("_dataId", dataId);
//        return dataId;
//    }
//
//    /**
//     * 移动到队列二
//     */
//    @Transactional
//    public void moveToQueue2(Map<String, Object> data, String reason) {
//        try {
//            String dataId = generateDataId(data);
//
//            // 更新数据库，移动到队列二
//            qrCodeQueueMapper.moveToQueue2(dataId, reason);
//
//            // 删除当前处理记录
//            qrCodeQueueMapper.deleteCurrentProcessing(dataId);
//
//            notifyQueueStatus();
//
//            System.out.println("❌ 数据移动到队列二: " + dataId + "，原因: " + reason);
//        } catch (Exception e) {
//            System.err.println("移动到队列二失败: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 导出队列二数据
//     */
//    @Transactional
//    public List<Map<String, Object>> exportQueue2Data() {
//        try {
//            List<Map<String, Object>> dbDataList = qrCodeQueueMapper.selectAllFromQueue2();
//            List<Map<String, Object>> exportData = new ArrayList<>();
//
//            for (Map<String, Object> dbData : dbDataList) {
//                String contentJson = (String) dbData.get("content_json");
//                Map<String, Object> data = objectMapper.readValue(contentJson, Map.class);
//                exportData.add(data);
//
//                // 从数据库删除
//                String dataId = (String) dbData.get("data_id");
//                qrCodeQueueMapper.deleteFromQueue2(dataId);
//            }
//
//            notifyQueueStatus();
//            System.out.println("📤 导出队列二数据，数量: " + exportData.size());
//
//            return exportData;
//        } catch (Exception e) {
//            throw new RuntimeException("导出队列二数据失败", e);
//        }
//    }
//
//    /**
//     * 获取队列状态
//     */
//    public Map<String, Object> getQueueStatus() {
//        Map<String, Object> status = new HashMap<>();
//        status.put("queue1Size", getQueue1Size());
//        status.put("queue2Size", getQueue2Size());
//        status.put("currentProcessing", currentProcessingData != null);
//        status.put("currentDataId", currentProcessingData != null ?
//                generateDataId(currentProcessingData) : null);
//        status.put("refreshCount", refreshCount.get());
//        status.put("timestamp", System.currentTimeMillis());
//
//        return status;
//    }
//
//    /**
//     * 获取当前处理的数据
//     */
//    public Map<String, Object> getCurrentProcessingData() {
//        return currentProcessingData;
//    }
//
//    private void notifyQueueStatus() {
//        if (callback != null) {
//            callback.onQueueStatusUpdate(getQueue1Size(), getQueue2Size());
//        }
//    }
//
//    /**
//     * 清空所有队列
//     */
//    @Transactional
//    public void clearAllQueues() {
//        qrCodeQueueMapper.clearQueue(QUEUE_TYPE_1);
//        qrCodeQueueMapper.clearQueue(QUEUE_TYPE_2);
//        qrCodeQueueMapper.clearCurrentProcessing();
//
//        currentProcessingData = null;
//        refreshCount.set(0);
//        notifyQueueStatus();
//        System.out.println("所有队列已清空！");
//    }
//
//    /**
//     * 获取队列一中的所有数据
//     */
//    public List<Map<String, Object>> getQueue1AllData() {
//        try {
//            List<Map<String, Object>> dbDataList = qrCodeQueueMapper.selectAllFromQueue1();
//            List<Map<String, Object>> result = new ArrayList<>();
//
//            for (Map<String, Object> dbData : dbDataList) {
//                String contentJson = (String) dbData.get("content_json");
//                Map<String, Object> data = objectMapper.readValue(contentJson, Map.class);
//                result.add(data);
//            }
//
//            return result;
//        } catch (Exception e) {
//            throw new RuntimeException("获取队列一数据失败", e);
//        }
//    }
//
//    /**
//     * 获取队列二中的所有数据
//     */
//    public List<Map<String, Object>> getQueue2AllData() {
//        try {
//            List<Map<String, Object>> dbDataList = qrCodeQueueMapper.selectAllFromQueue2();
//            List<Map<String, Object>> result = new ArrayList<>();
//
//            for (Map<String, Object> dbData : dbDataList) {
//                String contentJson = (String) dbData.get("content_json");
//                Map<String, Object> data = objectMapper.readValue(contentJson, Map.class);
//                result.add(data);
//            }
//
//            return result;
//        } catch (Exception e) {
//            throw new RuntimeException("获取队列二数据失败", e);
//        }
//    }
//
//    /**
//     * 检查并自动启动队列处理
//     */
//    public void checkAndStartProcessing() {
//        if (refreshTask == null || refreshTask.isCancelled()) {
//            startProcessing();
//            System.out.println("🔄 自动启动队列处理");
//        }
//    }
//
//    /**
//     * 删除队列二中的指定数据
//     */
//    @Transactional
//    public boolean removeFromQueue2(String dataId) {
//        int result = qrCodeQueueMapper.deleteFromQueue2(dataId);
//        if (result > 0) {
//            notifyQueueStatus();
//            System.out.println("🗑️ 从队列二删除数据: " + dataId);
//            return true;
//        }
//        return false;
//    }
//
//    /**
//     * 停止处理队列
//     */
//    public void stopProcessing() {
//        if (refreshTask != null) {
//            refreshTask.cancel(true);
//        }
//        // 注意：这里不清空currentProcessingData，以便恢复时可以继续处理
//    }
//}

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

    // 队列类型常量
    private static final int QUEUE_TYPE_1 = 1; // 队列一
    private static final int QUEUE_TYPE_2 = 2; // 队列二

    // 状态常量
    private static final int STATUS_PENDING = 1;    // 待处理
    private static final int STATUS_PROCESSING = 2; // 处理中
    private static final int STATUS_COMPLETED = 3;  // 已完成
    private static final int STATUS_FAILED = 4;     // 失败

    @Autowired
    private QrMapper qrCodeQueueMapper;

    @Autowired
    private QrMapper qrMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 当前正在处理的数据（缓存，减少数据库查询）
    private volatile Map<String, Object> currentProcessingData = null;

    // 刷新计数器
    private final AtomicInteger refreshCount = new AtomicInteger(0);

    // 定时任务执行器
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> refreshTask = null;

    // WebSocket通知回调
    private QrCodeUpdateCallback callback;

    // 在类中添加一个启动标志
    private volatile boolean initialized = false;


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

    /**
     * 获取队列一大小
     */
    public int getQueue1Size() {
        return qrCodeQueueMapper.getQueueSize(QUEUE_TYPE_1, STATUS_PENDING);
    }

    /**
     * 获取队列二大小
     */
    public int getQueue2Size() {
        return qrCodeQueueMapper.getQueueSize(QUEUE_TYPE_2, STATUS_FAILED);
    }

    /**
     * 添加数据到队列一
     */
//    @Transactional
//    public void addToQueue1(Map<String, Object> data) {
//        try {
//            String dataId = generateDataId(data);
//            String contentJson = objectMapper.writeValueAsString(data);
//
//            // 保存到数据库
//            qrCodeQueueMapper.insertQueueData(dataId, QUEUE_TYPE_1, contentJson, STATUS_PENDING);
//
//            notifyQueueStatus();
//            System.out.println("✅ 数据已添加到队列一: " + dataId + "，队列大小: " + getQueue1Size());
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException("数据序列化失败", e);
//        }
//    }
    @Transactional
    public void addToQueue1(Map<String, Object> data) {
        try {
            String dataId = generateDataId(data);
            String contentJson = objectMapper.writeValueAsString(data);

            // 保存到数据库
            qrCodeQueueMapper.insertQueueData(dataId, QUEUE_TYPE_1, contentJson, STATUS_PENDING);

            notifyQueueStatus();
            System.out.println("✅ 数据已添加到队列一: " + dataId + "，队列大小: " + getQueue1Size());

            // 如果没有当前处理数据，立即开始处理新数据
            if (currentProcessingData == null) {
                System.out.println("🚀 检测到无当前处理数据，立即开始处理新数据");
                processNextDataImmediately();
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException("数据序列化失败", e);
        }
    }

    /**
     * 开始处理队列
     */
    public void startProcessing() {
        if (refreshTask != null && !refreshTask.isCancelled()) {
            return; // 已经在运行
        }

        // 启动时恢复当前处理的数据
        restoreCurrentProcessingData();

        // 如果没有当前处理数据，但有队列一数据，立即开始处理
        if (currentProcessingData == null && getQueue1Size() > 0) {
            System.out.println("🔄 检测到队列一有数据，立即开始处理");
            processNextDataImmediately();
        }

        refreshTask = scheduler.scheduleAtFixedRate(() -> {
            try {
                processNextData();
            } catch (Exception e) {
                System.err.println("处理数据异常: " + e.getMessage());
                e.printStackTrace();
            }
        }, 0, 3, TimeUnit.SECONDS);
    }

    /**
     * 恢复当前处理的数据
     */
//    private void restoreCurrentProcessingData() {
//        try {
//            Map<String, Object> dbCurrent = qrCodeQueueMapper.selectCurrentProcessing();
//            if (dbCurrent != null) {
//                String contentJson = (String) dbCurrent.get("content_json");
//                Map<String, Object> data = objectMapper.readValue(contentJson, Map.class);
//
//                currentProcessingData = data;
//                refreshCount.set((Integer) dbCurrent.get("refresh_count"));
//
//                System.out.println("🔄 恢复当前处理数据: " + data.get("_dataId"));
//            }
//        } catch (Exception e) {
//            System.err.println("恢复当前处理数据失败: " + e.getMessage());
//        }
//    }
    private void restoreCurrentProcessingData() {
        try {
            Map<String, Object> dbCurrent = qrCodeQueueMapper.selectCurrentProcessing();
            if (dbCurrent != null) {
                String contentJson = (String) dbCurrent.get("content_json");
                Map<String, Object> data = objectMapper.readValue(contentJson, Map.class);

                currentProcessingData = data;
                refreshCount.set((Integer) dbCurrent.get("refresh_count"));

                String dataId = generateDataId(currentProcessingData);
                System.out.println("🔄 恢复当前处理数据: " + dataId + "，刷新次数: " + refreshCount.get());

                // 恢复后立即通知前端更新
                if (callback != null) {
                    Map<String, Object> cleanData = generateCleanData(currentProcessingData);
                    callback.onQrCodeUpdate(currentProcessingData);
                }
            } else {
                System.out.println("📭 没有需要恢复的当前处理数据");
            }
        } catch (Exception e) {
            System.err.println("恢复当前处理数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 处理下一条数据
     */
    @Transactional
    public void processNextData() {
        // 如果当前有数据正在处理，检查刷新次数
        if (currentProcessingData != null) {
            int count = refreshCount.incrementAndGet();

            // 更新数据库中的刷新次数
            String currentDataId = generateDataId(currentProcessingData);
            qrCodeQueueMapper.updateCurrentProcessingRefreshCount(currentDataId, count);

            System.out.println("🔄 刷新二维码，数据ID: " + currentDataId + "，刷新次数: " + count);

            // 通知前端更新二维码
            if (callback != null) {
                Map<String, Object> cleanData = generateCleanData(currentProcessingData);
                callback.onQrCodeUpdate(currentProcessingData);
            }

            // 如果刷新达到10次，移动到队列二
            if (count >= 10) {
                String dataId = generateDataId(currentProcessingData);
                moveToQueue2(currentProcessingData, "刷新10次未收到成功信号");
                currentProcessingData = null;
                refreshCount.set(0);

                if (callback != null) {
                    callback.onDataFailed(dataId, "刷新10次未收到成功信号");
                }

                // 立即处理下一条数据
                processNextDataImmediately();
            }
            return;
        }

        // 如果没有当前处理数据，从队列一获取新数据
        try {
            List<Map<String, Object>> dbDataList = qrCodeQueueMapper.selectFirstFromQueue1();
            if (dbDataList != null && !dbDataList.isEmpty()) {
                Map<String, Object> dbData = dbDataList.get(0); // 取第一条数据
                String contentJson = (String) dbData.get("content_json");
                Map<String, Object> data = objectMapper.readValue(contentJson, Map.class);
                String dataId = (String) dbData.get("data_id");

                // 设置为当前处理数据
                currentProcessingData = data;
                refreshCount.set(1);

                // 更新数据状态为处理中
                qrCodeQueueMapper.updateDataStatus(dataId, STATUS_PROCESSING);

                // 保存到当前处理表
                qrCodeQueueMapper.insertCurrentProcessing(dataId, contentJson, 1);

                System.out.println("🎯 开始处理新数据: " + dataId);
                System.out.println("📊 队列一剩余数据量: " + (dbDataList.size() - 1));

                // 通知前端显示新二维码
                if (callback != null) {
                    callback.onQrCodeUpdate(currentProcessingData);
                }
            }
        } catch (Exception e) {
            System.err.println("从队列一获取数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 立即处理下一条数据
     */
//    @Transactional
//    private void processNextDataImmediately() {
//        try {
//            Map<String, Object> dbData = qrCodeQueueMapper.selectFirstFromQueue1();
//            if (dbData != null) {
//                String contentJson = (String) dbData.get("content_json");
//                Map<String, Object> data = objectMapper.readValue(contentJson, Map.class);
//                String dataId = (String) dbData.get("data_id");
//
//                currentProcessingData = data;
//                refreshCount.set(1);
//
//                // 更新数据状态为处理中
//                qrCodeQueueMapper.updateDataStatus(dataId, STATUS_PROCESSING);
//
//                // 保存到当前处理表
//                qrCodeQueueMapper.insertCurrentProcessing(dataId, contentJson, 1);
//
//                System.out.println("🎯 立即开始处理下一条数据: " + dataId);
//
//                if (callback != null) {
//                    callback.onQrCodeUpdate(currentProcessingData);
//                }
//            } else {
//                System.out.println("📭 队列一为空，无待处理数据");
//            }
//        } catch (Exception e) {
//            System.err.println("立即处理下一条数据失败: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
    @Transactional
    public void processNextDataImmediately() {
        try {
            // 现在返回的是 List，我们取第一条
            List<Map<String, Object>> dbDataList = qrCodeQueueMapper.selectFirstFromQueue1();
            if (dbDataList != null && !dbDataList.isEmpty()) {
                Map<String, Object> dbData = dbDataList.get(0); // 取第一条数据
                String contentJson = (String) dbData.get("content_json");
                Map<String, Object> data = objectMapper.readValue(contentJson, Map.class);
                String dataId = (String) dbData.get("data_id");

                currentProcessingData = data;
                refreshCount.set(1);

                // 更新数据状态为处理中
                qrCodeQueueMapper.updateDataStatus(dataId, STATUS_PROCESSING);

                // 保存到当前处理表
                qrCodeQueueMapper.insertCurrentProcessing(dataId, contentJson, 1);

                System.out.println("🎯 立即开始处理下一条数据: " + dataId);
                System.out.println("📊 队列一剩余数据量: " + (dbDataList.size() - 1));

                // 通知前端显示新二维码
                if (callback != null) {
                    callback.onQrCodeUpdate(currentProcessingData);
                    Map<String, Object> cleanData = generateCleanData(currentProcessingData);
                    callback.onQueueStatusUpdate(getQueue1Size(), getQueue2Size());
                }
            } else {
                System.out.println("📭 队列一为空，无待处理数据");
                // 如果没有数据，清空当前处理
                currentProcessingData = null;
                refreshCount.set(0);

                if (callback != null) {
                    callback.onQrCodeUpdate(null); // 通知前端清空当前二维码
                }
            }
        } catch (Exception e) {
            System.err.println("立即处理下一条数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * 处理扫描成功信号（GPIO / 其它硬件来源）
     */
    @Transactional
    public boolean handleSuccessSignal(String dataId) {
        if (currentProcessingData == null) {
            System.out.println("⚠️ 当前没有正在处理的数据，忽略成功信号: " + dataId);
            return false;
        }

        String currentDataId = generateDataId(currentProcessingData);
        if (dataId.equals(currentDataId)) {
            System.out.println("✅ 收到成功信号，完成数据: " + dataId);

            // 更新数据状态为已完成
            qrCodeQueueMapper.updateDataStatus(dataId, STATUS_COMPLETED);

            // 删除当前处理记录
            qrCodeQueueMapper.deleteCurrentProcessing(dataId);

            if (callback != null) {
                callback.onDataSuccess(dataId);
            }

            currentProcessingData = null;
            refreshCount.set(0);

            // 立即处理下一条
            processNextDataImmediately();

            return true;
        } else {
            System.out.println("⚠️ 成功信号ID不匹配，当前: " + currentDataId + "，收到: " + dataId);
            return false;
        }
    }

    /**
     * 处理通用扫描成功信号（对应当前正在处理的数据）
     */
    @Transactional
    public boolean handleGeneralSuccessSignal() {
        if (currentProcessingData == null) {
            System.out.println("⚠️ 当前没有正在处理的数据，忽略通用成功信号");
            return false;
        }

        String dataId = generateDataId(currentProcessingData);
        System.out.println("✅ 收到通用成功信号，处理当前数据: " + dataId);

        // 更新数据状态为已完成
        qrCodeQueueMapper.updateDataStatus(dataId, STATUS_COMPLETED);

        // 删除当前处理记录
        qrCodeQueueMapper.deleteCurrentProcessing(dataId);

        if (callback != null) {
            callback.onDataSuccess(dataId);
        }

        currentProcessingData = null;
        refreshCount.set(0);

        processNextDataImmediately();

        return true;
    }

    /**
     * 生成数据ID
     */
    private String generateDataId(Map<String, Object> data) {
        if (data == null) {
            return "NULL_DATA_" + System.currentTimeMillis();
        }

        // 如果数据中已有_dataId，则使用它
        if (data.containsKey("_dataId")) {
            return (String) data.get("_dataId");
        }

        // 否则生成新的ID
        String dataId = "DATA_" + UUID.randomUUID().toString().substring(0, 8) + "_" + System.currentTimeMillis();
        data.put("_dataId", dataId);
        return dataId;
    }

    /**
     * 移动到队列二
     */
    @Transactional
    public void moveToQueue2(Map<String, Object> data, String reason) {
        try {
            String dataId = generateDataId(data);

            // 更新数据库，移动到队列二
            qrCodeQueueMapper.moveToQueue2(dataId, reason);

            // 删除当前处理记录
            qrCodeQueueMapper.deleteCurrentProcessing(dataId);

            notifyQueueStatus();

            System.out.println("❌ 数据移动到队列二: " + dataId + "，原因: " + reason);
        } catch (Exception e) {
            System.err.println("移动到队列二失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 导出队列二数据
     */
    @Transactional
    public List<Map<String, Object>> exportQueue2Data() {
        try {
            List<Map<String, Object>> dbDataList = qrCodeQueueMapper.selectAllFromQueue2();
            List<Map<String, Object>> exportData = new ArrayList<>();

            for (Map<String, Object> dbData : dbDataList) {
                String contentJson = (String) dbData.get("content_json");
                Map<String, Object> data = objectMapper.readValue(contentJson, Map.class);
                exportData.add(data);

                // 从数据库删除
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

    /**
     * 获取队列状态
     */
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

    /**
     * 获取当前处理的数据
     */
    public Map<String, Object> getCurrentProcessingData() {
        return currentProcessingData;
    }

    private void notifyQueueStatus() {
        if (callback != null) {
            callback.onQueueStatusUpdate(getQueue1Size(), getQueue2Size());
        }
    }

    /**
     * 清空所有队列
     */
    @Transactional
    public void clearAllQueues() {
        qrCodeQueueMapper.clearQueue(QUEUE_TYPE_1);
        qrCodeQueueMapper.clearQueue(QUEUE_TYPE_2);
        qrCodeQueueMapper.clearCurrentProcessing();

        currentProcessingData = null;
        refreshCount.set(0);
        notifyQueueStatus();
        System.out.println("所有队列已清空！");
    }

    /**
     * 获取队列一中的所有数据
     */
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

    /**
     * 获取队列二中的所有数据
     */
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

    /**
     * 检查并自动启动队列处理
     */
    public void checkAndStartProcessing() {
        if (refreshTask == null || refreshTask.isCancelled()) {
            startProcessing();
            System.out.println("🔄 自动启动队列处理");
        }
    }

    /**
     * 删除队列二中的指定数据
     */
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

    /**
     * 停止处理队列
     */
    public void stopProcessing() {
        if (refreshTask != null) {
            refreshTask.cancel(true);
        }
        // 注意：这里不清空currentProcessingData，以便恢复时可以继续处理
    }
    /**
     * 应用启动时自动初始化
     */
    @PostConstruct
    public void init() {
        System.out.println("🔧 QrCodeQueueManager 初始化...");
        // 应用启动时自动开始处理
        startProcessing();
        initialized = true;
    }

    /**
     * 生成纯净的二维码数据（移除系统字段）
     */
    private Map<String, Object> generateCleanData(Map<String, Object> originalData) {
        if (originalData == null) {
            return null;
        }

        Map<String, Object> cleanData = new HashMap<>(originalData);
        // 移除所有以 _ 开头的系统字段
        cleanData.keySet().removeIf(key -> key.startsWith("_"));
        return cleanData;
    }


    /**
     * 统计已完成数据数量
     */
    public int countCompletedData() {
        try {
            return qrMapper.countCompletedData();
        } catch (Exception e) {
//            log.error("统计已完成数据失败", e);
            return 0;
        }
    }

    /**
     * 查询指定日期前的已完成数据
     */
    public List<Map<String, Object>> selectCompletedDataBeforeDate(Date cutoffDate) {
        try {
            return qrMapper.selectCompletedDataBeforeDate(cutoffDate);
        } catch (Exception e) {
//            log.error("查询已完成数据失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 查询所有已完成数据
     */
    public List<Map<String, Object>> selectAllCompletedData() {
        try {
            return qrMapper.selectAllCompletedData();
        } catch (Exception e) {
//            log.error("查询所有已完成数据失败", e);
            return Collections.emptyList();
        }
    }
}