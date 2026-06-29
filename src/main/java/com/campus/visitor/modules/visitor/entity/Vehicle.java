package com.campus.visitor.modules.visitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("vehicle")
public class Vehicle implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long appointmentId;

    private String licensePlate;

    private String visitorName;

    private String visitorPhone;

    private LocalDateTime entryTime;

    private LocalDateTime exitTime;

    private String status;
}
