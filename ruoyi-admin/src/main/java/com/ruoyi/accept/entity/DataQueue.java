package com.ruoyi.accept.entity;


import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.common.annotation.Excel;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;



/**
 * 数据队列对象 sys_data_queue
 */
@Data
public class DataQueue extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 数据ID */
    private String dataId;

    /** JSON数据内容 */
    private String jsonData;

    /** 队列类型：QUEUE1-待发送 QUEUE2-发送失败 */
    private String queueType;

    /** 重试次数 */
    @Excel(name = "重试次数")
    private Integer retryCount;

    /** 最大重试次数 */
    private Integer maxRetryCount;

    /** 状态：PENDING-待处理 SENDING-发送中 SUCCESS-成功 FAILED-失败 */
    @Excel(name = "状态")
    private String status;

    /** 最后发送时间 */
    private Date lastSendTime;

    /** 发送结果 */
    private String sendResult;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("dataId", getDataId())
                .append("jsonData", getJsonData())
                .append("queueType", getQueueType())
                .append("retryCount", getRetryCount())
                .append("maxRetryCount", getMaxRetryCount())
                .append("status", getStatus())
                .append("createTime", getCreateTime())
                .append("updateTime", getUpdateTime())
                .append("lastSendTime", getLastSendTime())
                .append("sendResult", getSendResult())
                .append("remark", getRemark())
                .toString();
    }
}
