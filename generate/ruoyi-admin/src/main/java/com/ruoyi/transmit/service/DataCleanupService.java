package com.ruoyi.transmit.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.transmit.entity.CleanupResult;
import com.ruoyi.transmit.entity.CompletedDataStats;
import com.ruoyi.transmit.mapper.QrMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;

@Slf4j
@Service
public class DataCleanupService {

    @Autowired
    private QrMapper qrMapper;

    @Autowired
    private ObjectMapper objectMapper;

    // 已完成数据保留天数（可配置）
    private static final int COMPLETED_DATA_RETENTION_DAYS = 7; // 默认保留7天


    /**
     * 定期清理已完成数据 - 每周一凌晨3点执行
     */
    @Scheduled(cron = "0 0 3 ? * MON") // 每周一凌晨3点执行
    public void cleanUpCompletedData() {
        try {
            log.info("🧹 开始定期清理已完成数据（状态为3）...");

            // 计算删除截止时间（7天前）
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, -COMPLETED_DATA_RETENTION_DAYS);
            Date cutoffDate = calendar.getTime();

            // 先统计要删除的数据量
            int toDeleteCount = qrMapper.countCompletedDataBeforeDate(cutoffDate);

            if (toDeleteCount == 0) {
                log.info("✅ 没有需要清理的已完成数据");
                return;
            }

            log.info("📊 发现 {} 条超过 {} 天的已完成数据需要清理",
                    toDeleteCount, COMPLETED_DATA_RETENTION_DAYS);

            // 查询要删除的数据详情（用于日志记录）
            var dataToDelete = qrMapper.selectCompletedDataBeforeDate(cutoffDate);
            if (!dataToDelete.isEmpty()) {
                log.info("📋 即将删除的已完成数据（前10条）:");
                int count = 0;
                for (var data : dataToDelete) {
                    if (count++ >= 10) break;
                    log.info("   - ID: {}, DataID: {}, 创建时间: {}",
                            data.get("id"), data.get("data_id"), data.get("created_time"));
                }
            }

            // 执行删除
            int deletedCount = qrMapper.deleteCompletedDataBeforeDate(cutoffDate);

            // 删除后的统计
            int remainingCount = qrMapper.countCompletedData();

            log.info("✅ 成功清理 {} 条已完成数据，剩余 {} 条已完成数据",
                    deletedCount, remainingCount);

        } catch (Exception e) {
            log.error("❌ 清理已完成数据异常", e);
        }
    }

    /**
     * 获取已完成数据统计信息
     */
    public CompletedDataStats getCompletedDataStats() {
        try {
            CompletedDataStats stats = new CompletedDataStats();
            stats.setTotalCount(qrMapper.countCompletedData());

            // 统计7天内的数据
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, -7);
            Date weekAgo = calendar.getTime();
            stats.setLastWeekCount(qrMapper.countCompletedDataBeforeDate(weekAgo));

            // 统计最近的数据
            var recentData = qrMapper.selectAllCompletedData();
            stats.setRecentData(recentData);

            return stats;
        } catch (Exception e) {
            log.error("获取已完成数据统计失败", e);
            return null;
        }
    }

    /**
     * 手动清理已完成数据
     */
    public CleanupResult cleanupCompletedData(int retentionDays) {
        try {
            if (retentionDays < 0) {
                throw new IllegalArgumentException("保留天数不能为负数");
            }

            log.info("🗑️ 手动清理已完成数据，保留天数: {}", retentionDays);

            // 计算删除截止时间
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, -retentionDays);
            Date cutoffDate = calendar.getTime();

            // 先统计要删除的数据量
            int toDeleteCount = qrMapper.countCompletedDataBeforeDate(cutoffDate);

            if (toDeleteCount == 0) {
                return new CleanupResult(0, retentionDays, cutoffDate,
                        qrMapper.countCompletedData(), "没有需要清理的数据");
            }

            log.info("📊 将要删除 {} 条已完成数据（{}天前的数据）",
                    toDeleteCount, retentionDays);

            // 执行删除
            int deletedCount = qrMapper.deleteCompletedDataBeforeDate(cutoffDate);

            // 删除后的统计
            int remainingCount = qrMapper.countCompletedData();

            log.info("✅ 成功删除 {} 条已完成数据，剩余 {} 条已完成数据",
                    deletedCount, remainingCount);

            return new CleanupResult(deletedCount, retentionDays, cutoffDate,
                    remainingCount, "清理成功");

        } catch (Exception e) {
            log.error("手动清理已完成数据失败", e);
            throw new RuntimeException("清理失败: " + e.getMessage(), e);
        }
    }

}
