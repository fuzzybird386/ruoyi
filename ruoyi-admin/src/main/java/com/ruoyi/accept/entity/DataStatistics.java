package com.ruoyi.accept.entity;


import lombok.Data;

import java.util.concurrent.atomic.AtomicLong;

@Data
public class DataStatistics {
    // 接收数据计数
    private AtomicLong receivedCount = new AtomicLong(0);

    // 发送数据计数
    private AtomicLong sentCount = new AtomicLong(0);

    // 最后接收时间
    private long lastReceivedTime;

    // 最后发送时间
    private long lastSentTime;

    // 接收失败计数
    private AtomicLong receiveErrorCount = new AtomicLong(0);

    // 发送失败计数
    private AtomicLong sendErrorCount = new AtomicLong(0);

    /**
     * 增加接收计数
     */
    public void incrementReceived() {
        receivedCount.incrementAndGet();
        lastReceivedTime = System.currentTimeMillis();
    }

    /**
     * 增加发送计数
     */
    public void incrementSent() {
        sentCount.incrementAndGet();
        lastSentTime = System.currentTimeMillis();
    }

    /**
     * 增加接收错误计数
     */
    public void incrementReceiveError() {
        receiveErrorCount.incrementAndGet();
    }

    /**
     * 增加发送错误计数
     */
    public void incrementSendError() {
        sendErrorCount.incrementAndGet();
    }

    /**
     * 获取统计信息
     */
    public String getStatisticsInfo() {
        return String.format(
                "数据统计 - 接收: %d, 发送: %d, 接收错误: %d, 发送错误: %d, 最后接收: %s, 最后发送: %s",
                receivedCount.get(), sentCount.get(), receiveErrorCount.get(), sendErrorCount.get(),
                new java.util.Date(lastReceivedTime), new java.util.Date(lastSentTime)
        );
    }
}
