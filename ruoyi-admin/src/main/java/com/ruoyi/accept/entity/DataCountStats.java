package com.ruoyi.accept.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import lombok.Data;

import java.util.Date;


@Data
public class DataCountStats {
    private static final long serialVersionUID = 1L;

    /** 统计ID */
    private Long id;

    /** 统计月份 (yyyy-MM) */
    @Excel(name = "统计月份")
    private String statsMonth;

    /** TCP接收数据次数 */
    @Excel(name = "TCP接收次数")
    private Long tcpReceiveCount;

    /** API调用次数 */
    @Excel(name = "API调用次数")
    private Long apiCallCount;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
