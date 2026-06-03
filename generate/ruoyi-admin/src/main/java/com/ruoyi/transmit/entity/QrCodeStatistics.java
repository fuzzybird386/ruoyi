package com.ruoyi.transmit.entity;


import lombok.Data;

import java.util.Date;

@Data
public class QrCodeStatistics {
    private Long id;
    private Date statDate;
    private Integer year;
    private Integer month;
    private Integer receiveCount;
    private Integer qrCodeCount;
    private Date createTime;
}
