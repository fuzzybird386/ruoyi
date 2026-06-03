package com.ruoyi.accept.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 短信数据实体类
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class SmsData extends BaseEntity {
//    private static final long serialVersionUID = 1L;
//
//
//    private Long id;
//
//    /** 批次ID */
//    private String batchId;
//
//    /** 发送用户ID */
//    private String sendUserId;
//
//    /** 发送用户姓名 */
//    private String sendUserName;
//
//    /** 短信内容 */
//    private String content;
//
//    /** 系统ID */
//    private String systemId;
//
//    /** 接收手机号（原始字符串） */
//    private String receivePhone;
//
//    /** 消息加密标志 */
//    private Integer msgSecret;
//
//    /** 发送状态（0-待发送，1-发送中，2-发送成功，3-发送失败） */
//    private Integer status;
//
//    /** 发送时间 */
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    private Date sendTime;
//
//    /** 完成时间 */
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    private Date finishTime;
//
//    /** 成功数量 */
//    private Integer successCount;
//
//    /** 失败数量 */
//    private Integer failCount;
//
//    /** 总数量 */
//    private Integer totalCount;
//
//    /** 错误信息 */
//    private String errorMsg;
}
