package com.ruoyi.transmit.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class QrQueue2 {
        private Long id;
        private String dataId;
        private String content;
        private String receivePhone;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private Date createTime;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private Date failTime;

        private String failReason;
        private Integer refreshCount;
}
