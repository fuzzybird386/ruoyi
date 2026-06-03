//package com.ruoyi.accept.mapper;
//
//
//
//import com.ruoyi.accept.entity.DataQueue;
//import org.apache.ibatis.annotations.Param;
//
//import java.util.List;
//
//
//
///**
// * 数据队列Mapper接口
// */
//public interface DataQueueMapper {
//
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
//     * 删除数据队列
//     */
//    int deleteDataQueueById(String dataId);
//
//    /**
//     * 批量删除数据队列
//     */
//    int deleteDataQueueByIds(String[] dataIds);
//
//    /**
//     * 根据队列类型查询数据
//     */
//    List<DataQueue> selectByQueueType(String queueType);
//
//    /**
//     * 根据状态查询数据
//     */
//    List<DataQueue> selectByStatus(String status);
//
//    /**
//     * 更新数据状态
//     */
//    int updateStatus(@Param("dataId") String dataId, @Param("status") String status);
//
//    /**
//     * 增加重试次数
//     */
//    int incrementRetryCount(@Param("dataId") String dataId);
//
//    /**
//     * 更新发送结果
//     */
//    int updateSendResult(@Param("dataId") String dataId, @Param("sendResult") String sendResult,
//                         @Param("status") String status);
//
//    /**
//     * 查询需要重试的数据
//     */
//    List<DataQueue> selectNeedRetryData(@Param("queueType") String queueType,
//                                        @Param("maxRetryCount") int maxRetryCount);
//
//    /**
//     * 更新队列类型
//     */
//    int updateQueueType(@Param("dataId") String dataId, @Param("queueType") String queueType);
//
//}


package com.ruoyi.accept.mapper;


import com.ruoyi.accept.entity.DataQueue;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 数据队列Mapper接口
 */
public interface DataQueueMapper {
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
     * 删除数据队列
     */
    int deleteDataQueueById(String dataId);

    /**
     * 批量删除数据队列
     */
    int deleteDataQueueByIds(String[] dataIds);

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
    int updateStatus(@Param("dataId") String dataId, @Param("status") String status);

    /**
     * 增加重试次数
     */
    int incrementRetryCount(@Param("dataId") String dataId);

    /**
     * 更新发送结果
     */
    int updateSendResult(@Param("dataId") String dataId, @Param("sendResult") String sendResult,
                         @Param("status") String status);

    /**
     * 查询需要重试的数据
     */
    List<DataQueue> selectNeedRetryData(@Param("queueType") String queueType,
                                        @Param("maxRetryCount") int maxRetryCount);

    /**
     * 更新队列类型
     */
    int updateQueueType(@Param("dataId") String dataId, @Param("queueType") String queueType);


    /**
     * 删除指定日期前的成功数据
     * @param cutoffDate 截止日期
     * @return 删除的记录数
     */
    int deleteSuccessDataBeforeDate(@Param("cutoffDate") Date cutoffDate);

    /**
     * 统计成功数据数量
     * @return 成功数据数量
     */
    int countSuccessData();

    /**
     * 统计指定日期前的成功数据数量
     */
    int countSuccessDataBeforeDate(@Param("cutoffDate") Date cutoffDate);
}