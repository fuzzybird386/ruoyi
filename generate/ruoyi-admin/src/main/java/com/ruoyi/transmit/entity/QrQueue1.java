package com.ruoyi.transmit.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

import lombok.Data;

@Data
public class QrQueue1 {
    private Long id;
    private String dataId;
    private String content;
    private String receivePhone;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    private String status;
    private Integer refreshCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startProcessTime;
}
