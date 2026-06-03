package com.ruoyi.transmit.entity;

import java.util.Date;

/**
 * // 清理结果类
 */
public class CleanupResult {
    private int deletedCount;
    private int retentionDays;
    private Date cutoffDate;
    private int remainingCount;
    private String message;

    public CleanupResult(int deletedCount, int retentionDays, Date cutoffDate,
                         int remainingCount, String message) {
        this.deletedCount = deletedCount;
        this.retentionDays = retentionDays;
        this.cutoffDate = cutoffDate;
        this.remainingCount = remainingCount;
        this.message = message;
    }

    // getters
    public int getDeletedCount() { return deletedCount; }
    public int getRetentionDays() { return retentionDays; }
    public Date getCutoffDate() { return cutoffDate; }
    public int getRemainingCount() { return remainingCount; }
    public String getMessage() { return message; }
}
