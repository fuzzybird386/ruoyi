//package com.ruoyi.transmit.mapper;
//
//import org.apache.ibatis.annotations.Param;
//import java.util.List;
//import java.util.Map;
//
//
//import org.apache.ibatis.annotations.*;
//import java.util.List;
//import java.util.Map;
//
//@Mapper
//public interface QrMapper {

//
//    // 插入数据到指定队列
//    @Insert("INSERT INTO qr_code_queue (data_id, queue_type, content_json, status, created_time) " +
//            "VALUES (#{dataId}, #{queueType}, #{contentJson}, #{status}, NOW())")
//    int insertQueueData(@Param("dataId") String dataId,
//                        @Param("queueType") int queueType,
//                        @Param("contentJson") String contentJson,
//                        @Param("status") int status);
//
//    // 从队列一获取待处理数据
//    @Select("SELECT id, data_id, content_json, refresh_count FROM qr_code_queue " +
//            "WHERE queue_type = 1 AND status = 1 ORDER BY created_time ASC LIMIT 1")
//    @ResultType(Map.class)
//    Map<String, Object> selectFirstFromQueue1();
//
//    // 更新数据状态
//    @Update("UPDATE qr_code_queue SET status = #{status}, updated_time = NOW() " +
//            "WHERE data_id = #{dataId}")
//    int updateDataStatus(@Param("dataId") String dataId, @Param("status") int status);
//
//    // 移动到队列二
//    @Update("UPDATE qr_code_queue SET queue_type = 2, status = 4, fail_reason = #{failReason}, " +
//            "updated_time = NOW() WHERE data_id = #{dataId}")
//    int moveToQueue2(@Param("dataId") String dataId, @Param("failReason") String failReason);
//
//    // 获取队列大小
//    @Select("SELECT COUNT(*) FROM qr_code_queue WHERE queue_type = #{queueType} AND status = #{status}")
//    int getQueueSize(@Param("queueType") int queueType, @Param("status") int status);
//
//    // 获取队列二所有数据
//    @Select("SELECT id, data_id, content_json, fail_reason, created_time " +
//            "FROM qr_code_queue WHERE queue_type = 2 AND status = 4 ORDER BY created_time DESC")
//    @ResultType(List.class)
//    List<Map<String, Object>> selectAllFromQueue2();
//
//    // 删除队列二数据
//    @Delete("DELETE FROM qr_code_queue WHERE data_id = #{dataId} AND queue_type = 2")
//    int deleteFromQueue2(@Param("dataId") String dataId);
//
//    // 清空指定队列
//    @Delete("DELETE FROM qr_code_queue WHERE queue_type = #{queueType}")
//    int clearQueue(@Param("queueType") int queueType);
//
//    // 插入当前处理数据
//    @Insert("INSERT INTO qr_code_current_processing (data_id, content_json, refresh_count) " +
//            "VALUES (#{dataId}, #{contentJson}, #{refreshCount})")
//    int insertCurrentProcessing(@Param("dataId") String dataId,
//                                @Param("contentJson") String contentJson,
//                                @Param("refreshCount") int refreshCount);
//
//    // 获取当前处理数据
//    @Select("SELECT data_id, content_json, refresh_count FROM qr_code_current_processing LIMIT 1")
//    @ResultType(Map.class)
//    Map<String, Object> selectCurrentProcessing();
//
//    // 更新当前处理数据的刷新次数
//    @Update("UPDATE qr_code_current_processing SET refresh_count = #{refreshCount}, " +
//            "updated_time = NOW() WHERE data_id = #{dataId}")
//    int updateCurrentProcessingRefreshCount(@Param("dataId") String dataId,
//                                            @Param("refreshCount") int refreshCount);
//
//    // 删除当前处理数据
//    @Delete("DELETE FROM qr_code_current_processing WHERE data_id = #{dataId}")
//    int deleteCurrentProcessing(@Param("dataId") String dataId);
//
//    // 清空当前处理数据
//    @Delete("DELETE FROM qr_code_current_processing")
//    int clearCurrentProcessing();
//
//    // 获取所有队列一数据
//    @Select("SELECT id, data_id, content_json, status, created_time " +
//            "FROM qr_code_queue WHERE queue_type = 1 ORDER BY created_time ASC")
//    @ResultType(List.class)
//    List<Map<String, Object>> selectAllFromQueue1();
//}

package com.ruoyi.transmit.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface QrMapper {

    // 插入数据到指定队列
    int insertQueueData(@Param("dataId") String dataId,
                        @Param("queueType") int queueType,
                        @Param("contentJson") String contentJson,
                        @Param("status") int status);

    // 从队列一获取待处理数据
    List<Map<String, Object>> selectFirstFromQueue1();

    // 更新数据状态
    int updateDataStatus(@Param("dataId") String dataId, @Param("status") int status);

    // 移动到队列二
    int moveToQueue2(@Param("dataId") String dataId, @Param("failReason") String failReason);

    // 获取队列大小
    int getQueueSize(@Param("queueType") int queueType, @Param("status") int status);

    // 获取队列二所有数据
    List<Map<String, Object>> selectAllFromQueue2();

    // 删除队列二数据
    int deleteFromQueue2(@Param("dataId") String dataId);

    // 清空指定队列
    int clearQueue(@Param("queueType") int queueType);

    // 插入当前处理数据
    int insertCurrentProcessing(@Param("dataId") String dataId,
                                @Param("contentJson") String contentJson,
                                @Param("refreshCount") int refreshCount);

    // 获取当前处理数据
    Map<String, Object> selectCurrentProcessing();

    // 更新当前处理数据的刷新次数
    int updateCurrentProcessingRefreshCount(@Param("dataId") String dataId,
                                            @Param("refreshCount") int refreshCount);

    // 删除当前处理数据
    int deleteCurrentProcessing(@Param("dataId") String dataId);

    // 清空当前处理数据
    int clearCurrentProcessing();

    // 获取所有队列一数据
    List<Map<String, Object>> selectAllFromQueue1();

    /**
     * 删除指定日期前的已完成数据
     */
    int deleteCompletedDataBeforeDate(@Param("cutoffDate") Date cutoffDate);

    /**
     * 统计已完成数据数量
     */
    int countCompletedData();

    /**
     * 统计指定日期前的已完成数据数量
     */
    int countCompletedDataBeforeDate(@Param("cutoffDate") Date cutoffDate);

    /**
     * 查询指定日期前的已完成数据
     */
    List<Map<String, Object>> selectCompletedDataBeforeDate(@Param("cutoffDate") Date cutoffDate);

    /**
     * 查询所有已完成数据
     */
    List<Map<String, Object>> selectAllCompletedData();
}