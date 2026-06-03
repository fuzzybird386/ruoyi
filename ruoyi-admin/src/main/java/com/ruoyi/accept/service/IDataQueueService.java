//package com.ruoyi.accept.service;
//
//
//
//import com.ruoyi.accept.entity.DataQueue;
//
//import java.util.List;
//
//public interface IDataQueueService {
//    /**
//     * 查询数据队列
//     */
//    DataQueue selectDataQueueById(String dataId);
//
//    /**
//     * 查询数据队列列表
//     */
//    List<DataQueue> selectDataQueueList(DataQueue dataQueue);
//
//    /**
//     * 新增数据队列
//     */
//    int insertDataQueue(DataQueue dataQueue);
//
//    /**
//     * 修改数据队列
//     */
//    int updateDataQueue(DataQueue dataQueue);
//
//    /**
//     * 批量删除数据队列
//     */
//    int deleteDataQueueByIds(String[] dataIds);
//
//    /**
//     * 删除数据队列信息
//     */
//    int deleteDataQueueById(String dataId);
//
//    /**
//     * 根据队列类型查询数据
//     */
//    List<DataQueue> selectByQueueType(String queueType);
//
//    /**
//     * 更新数据状态
//     */
//    int updateStatus(String dataId, String status);
//
//    /**
//     * 增加重试次数
//     */
//    int incrementRetryCount(String dataId);
//
//    /**
//     * 更新发送结果
//     */
//    int updateSendResult(String dataId, String sendResult, String status);
//
//    /**
//     * 查询需要重试的数据
//     */
//    List<DataQueue> selectNeedRetryData(String queueType, int maxRetryCount);
//
//    /**
//     * 更新队列类型
//     */
//    int updateQueueType(String dataId, String queueType);
//
//
//}

package com.ruoyi.accept.service;


import com.ruoyi.accept.entity.DataQueue;

import java.util.Date;
import java.util.List;


import java.util.List;

/**
 * 数据队列Service接口
 */
public interface IDataQueueService {
    /**
     * 查询数据队列
     */
    DataQueue selectDataQueueById(String dataId);

    /**
     * 查询数据队列列表
     */
    List<DataQueue> selectDataQueueList(DataQueue dataQueue);

    /**
     * 新增数据队列
     */
    int insertDataQueue(DataQueue dataQueue);

    /**
     * 修改数据队列
     */
    int updateDataQueue(DataQueue dataQueue);

    /**
     * 批量删除数据队列
     */
    int deleteDataQueueByIds(String[] dataIds);

    /**
     * 删除数据队列信息
     */
    int deleteDataQueueById(String dataId);

    /**
     * 根据队列类型查询数据
     */
    List<DataQueue> selectByQueueType(String queueType);

    /**
     * 根据状态查询数据
     */
    List<DataQueue> selectByStatus(String status);

    /**
     * 更新数据状态
     */
    int updateStatus(String dataId, String status);

    /**
     * 增加重试次数
     */
    int incrementRetryCount(String dataId);

    /**
     * 更新发送结果
     */
    int updateSendResult(String dataId, String sendResult, String status);

    /**
     * 查询需要重试的数据
     */
    List<DataQueue> selectNeedRetryData(String queueType, int maxRetryCount);

    /**
     * 更新队列类型
     */
    int updateQueueType(String dataId, String queueType);

    /**
     * 删除指定日期前的成功数据
     * @param cutoffDate 截止日期
     * @return 删除的记录数
     */
    int deleteSuccessDataBeforeDate(Date cutoffDate);

    /**
     * 统计成功数据数量
     * @return 成功数据数量
     */
    int countSuccessData();

    /**
     * 统计指定日期前的成功数据数量
     * @param cutoffDate 截止日期
     * @return 数量
     */
    int countSuccessDataBeforeDate(Date cutoffDate);
}