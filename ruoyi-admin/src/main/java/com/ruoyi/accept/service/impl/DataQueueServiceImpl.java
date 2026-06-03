//package com.ruoyi.accept.service.impl;
//
//import com.ruoyi.accept.entity.DataQueue;
//import com.ruoyi.accept.service.IDataQueueService;
//import org.springframework.stereotype.Service;
//
//
//import com.ruoyi.accept.mapper.DataQueueMapper;
//import com.ruoyi.accept.service.IDataQueueService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//
//@Service
//public class DataQueueServiceImpl implements IDataQueueService {
//
//    @Autowired
//    private DataQueueMapper dataQueueMapper;
//
//
//    @Override
//    public DataQueue selectDataQueueById(String dataId) {
//        return dataQueueMapper.selectDataQueueById(dataId);
//    }
//
//    @Override
//    public List<DataQueue> selectDataQueueList(DataQueue dataQueue) {
//        return dataQueueMapper.selectDataQueueList(dataQueue);
//    }
//
//    @Override
//    public int insertDataQueue(DataQueue dataQueue) {
//        return dataQueueMapper.insertDataQueue(dataQueue);
//    }
//
//    @Override
//    public int updateDataQueue(DataQueue dataQueue) {
//        return dataQueueMapper.updateDataQueue(dataQueue);
//    }
//
//    @Override
//    public int deleteDataQueueByIds(String[] dataIds) {
//        return dataQueueMapper.deleteDataQueueByIds(dataIds);
//    }
//
//    @Override
//    public int deleteDataQueueById(String dataId) {
//        return dataQueueMapper.deleteDataQueueById(dataId);
//    }
//
//    @Override
//    public List<DataQueue> selectByQueueType(String queueType) {
//        return dataQueueMapper.selectByQueueType(queueType);
//    }
//
//    @Override
//    public int updateStatus(String dataId, String status) {
//        return dataQueueMapper.updateStatus(dataId, status);
//    }
//
//    @Override
//    public int incrementRetryCount(String dataId) {
//        return dataQueueMapper.incrementRetryCount(dataId);
//    }
//
//    @Override
//    public int updateSendResult(String dataId, String sendResult, String status) {
//        return dataQueueMapper.updateSendResult(dataId, sendResult, status);
//    }
//
//    @Override
//    public List<DataQueue> selectNeedRetryData(String queueType, int maxRetryCount) {
//        return dataQueueMapper.selectNeedRetryData(queueType, maxRetryCount);
//    }
//
//    @Override
//    public int updateQueueType(String dataId, String queueType) {
//        return dataQueueMapper.updateQueueType(dataId, queueType);
//    }
//
//}



package com.ruoyi.accept.service.impl;


import com.ruoyi.accept.entity.DataQueue;
import com.ruoyi.accept.mapper.DataQueueMapper;
import com.ruoyi.accept.service.IDataQueueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 数据队列Service业务层处理
 */
import java.util.List;

/**
 * 数据队列Service业务层处理
 */
@Slf4j
@Service
public class DataQueueServiceImpl implements IDataQueueService {
    @Autowired
    private DataQueueMapper dataQueueMapper;

    @Override
    public DataQueue selectDataQueueById(String dataId) {
        return dataQueueMapper.selectDataQueueById(dataId);
    }

    @Override
    public List<DataQueue> selectDataQueueList(DataQueue dataQueue) {
        return dataQueueMapper.selectDataQueueList(dataQueue);
    }

    @Override
    public int insertDataQueue(DataQueue dataQueue) {
        return dataQueueMapper.insertDataQueue(dataQueue);
    }

    @Override
    public int updateDataQueue(DataQueue dataQueue) {
        return dataQueueMapper.updateDataQueue(dataQueue);
    }

    @Override
    public int deleteDataQueueByIds(String[] dataIds) {
        return dataQueueMapper.deleteDataQueueByIds(dataIds);
    }

    @Override
    public int deleteDataQueueById(String dataId) {
        return dataQueueMapper.deleteDataQueueById(dataId);
    }

    @Override
    public List<DataQueue> selectByQueueType(String queueType) {
        return dataQueueMapper.selectByQueueType(queueType);
    }

    @Override
    public List<DataQueue> selectByStatus(String status) {
        return dataQueueMapper.selectByStatus(status);
    }

    @Override
    public int updateStatus(String dataId, String status) {
        return dataQueueMapper.updateStatus(dataId, status);
    }

    @Override
    public int incrementRetryCount(String dataId) {
        return dataQueueMapper.incrementRetryCount(dataId);
    }

    @Override
    public int updateSendResult(String dataId, String sendResult, String status) {
        return dataQueueMapper.updateSendResult(dataId, sendResult, status);
    }

    @Override
    public List<DataQueue> selectNeedRetryData(String queueType, int maxRetryCount) {
        return dataQueueMapper.selectNeedRetryData(queueType, maxRetryCount);
    }

    @Override
    public int updateQueueType(String dataId, String queueType) {
        return dataQueueMapper.updateQueueType(dataId, queueType);
    }


    @Override
    public int deleteSuccessDataBeforeDate(Date cutoffDate) {
        try {
            log.info("🗑️ 删除截止日期 {} 前的成功数据", cutoffDate);
            return dataQueueMapper.deleteSuccessDataBeforeDate(cutoffDate);
        } catch (Exception e) {
            log.error("❌ 删除成功数据失败", e);
            return 0;
        }
    }

    @Override
    public int countSuccessData() {
        try {
            return dataQueueMapper.countSuccessData();
        } catch (Exception e) {
            log.error("❌ 统计成功数据失败", e);
            return 0;
        }
    }

    @Override
    public int countSuccessDataBeforeDate(Date cutoffDate) {
        try {
            return dataQueueMapper.countSuccessDataBeforeDate(cutoffDate);
        } catch (Exception e) {
            log.error("❌ 统计成功数据失败", e);
            return 0;
        }
    }

}