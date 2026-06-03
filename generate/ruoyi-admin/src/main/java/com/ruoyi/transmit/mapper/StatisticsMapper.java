package com.ruoyi.transmit.mapper;


import com.ruoyi.transmit.entity.QrCodeStatistics;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

import java.util.List;
import java.util.Map;

@Mapper
public interface StatisticsMapper {
    // 插入统计记录
    int insert(QrCodeStatistics statistics);

    // 更新统计记录
    int update(QrCodeStatistics statistics);

    // 查询某月的统计
    QrCodeStatistics selectByYearAndMonth(@Param("year") int year, @Param("month") int month);

    // 按月统计
    List<Map<String, Object>> selectMonthlyStatistics(@Param("year") Integer year);

    // 获取所有有数据的年份
    List<Integer> selectAvailableYears();
}
