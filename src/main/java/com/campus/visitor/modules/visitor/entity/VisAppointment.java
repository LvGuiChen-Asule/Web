package com.campus.visitor.modules.visitor.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("appointment")
public class VisAppointment implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long visitorId;

    private String visitorName;

    private String visitorPhone;

    private String visitorIdCard;

    @TableField("visited_user_id")
    private Long hostId;

    private Long areaId;

    @TableField("start_time")
    private LocalDateTime visitStartTime;

    @TableField("end_time")
    private LocalDateTime visitEndTime;

    private String reason;

    private String status;

    @TableField("qrcode_url")
    private String qrcodeUrl;

    /**
     * AI预审结果：HIGH_RISK 或 NORMAL
     */
    @TableField("ai_risk_level")
    private String aiRiskLevel;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
