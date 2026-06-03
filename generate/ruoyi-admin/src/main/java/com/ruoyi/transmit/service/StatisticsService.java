package com.ruoyi.transmit.service;

import com.ruoyi.transmit.entity.QrCodeStatistics;
import com.ruoyi.transmit.mapper.StatisticsMapper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsService {

    @Autowired
    private StatisticsMapper statisticsMapper;

    /**
     * 记录数据接收
     */
    public void recordDataReceive() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;

        QrCodeStatistics stat = statisticsMapper.selectByYearAndMonth(year, month);
        if (stat == null) {
            stat = new QrCodeStatistics();
            stat.setYear(year);
            stat.setMonth(month);
            stat.setStatDate(new Date());
            stat.setReceiveCount(1);
            stat.setQrCodeCount(0);
            stat.setCreateTime(new Date());
            statisticsMapper.insert(stat);
        } else {
            stat.setReceiveCount(stat.getReceiveCount() + 1);
            statisticsMapper.update(stat);
        }
    }

    /**
     * 记录二维码生成
     */
    public void recordQrCodeGenerate() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;

        QrCodeStatistics stat = statisticsMapper.selectByYearAndMonth(year, month);
        if (stat == null) {
            stat = new QrCodeStatistics();
            stat.setYear(year);
            stat.setMonth(month);
            stat.setStatDate(new Date());
            stat.setReceiveCount(0);
            stat.setQrCodeCount(1);
            stat.setCreateTime(new Date());
            statisticsMapper.insert(stat);
        } else {
            stat.setQrCodeCount(stat.getQrCodeCount() + 1);
            statisticsMapper.update(stat);
        }
    }

    /**
     * 获取月度统计
     */
    public List<Map<String, Object>> getMonthlyStatistics(Integer year) {
        return statisticsMapper.selectMonthlyStatistics(year);
    }

    /**
     * 获取可用年份
     */
    public List<Integer> getAvailableYears() {
        return statisticsMapper.selectAvailableYears();
    }

}
