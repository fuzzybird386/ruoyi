package com.ruoyi.transmit.entity;

/**
 *  // 统计结果类
 */
public class CompletedDataStats {
    private int totalCount;
    private int lastWeekCount;
    private Object recentData;

    // getters and setters
    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
    public int getLastWeekCount() { return lastWeekCount; }
    public void setLastWeekCount(int lastWeekCount) { this.lastWeekCount = lastWeekCount; }
    public Object getRecentData() { return recentData; }
    public void setRecentData(Object recentData) { this.recentData = recentData; }
}
