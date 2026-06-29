package com.campus.visitor.modules.visitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("access_record")
public class AccessRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long appointmentId;

    private String visitorName;

    private LocalDateTime entryTime;

    private LocalDateTime exitTime;

    private Integer durationMinutes;

    private String status;
}
