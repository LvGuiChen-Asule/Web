package com.campus.visitor.modules.visitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("approval_record")
public class ApprovalRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long appointmentId;

    private Long approverId;

    private String approverRole;

    private String action;

    private String comment;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
