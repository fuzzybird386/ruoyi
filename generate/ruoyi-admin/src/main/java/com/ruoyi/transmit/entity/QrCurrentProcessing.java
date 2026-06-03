package com.ruoyi.transmit.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

import lombok.Data;

/**
 * 当前处理数据表
 */
@Data
public class QrCurrentProcessing {
    private Long id;
    private String dataId;
    private String content;
    private String receivePhone;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    private Integer refreshCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastRefreshTime;
}
