package com.campus.visitor.modules.visitor.service.dto;

import lombok.Data;

import java.util.List;

@Data
public class QrVerifyResult {
    private String result;
    private String message;
    private Long appointmentId;
    private Long areaId;
    private String areaName;
    private List<com.campus.visitor.modules.visitor.dto.AppointmentItemView> items;
}
