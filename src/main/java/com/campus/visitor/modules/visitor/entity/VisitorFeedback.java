package com.campus.visitor.modules.visitor.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("visitor_feedback")
public class VisitorFeedback implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long appointmentId;

    private Long visitorId;

    private String visitorName;

    private Integer approvalSpeed;

    private Integer guardAttitude;

    private Integer environment;

    private Integer overall;

    private String comment;

    private Boolean isAnonymous;

    private Boolean isFeatured;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
