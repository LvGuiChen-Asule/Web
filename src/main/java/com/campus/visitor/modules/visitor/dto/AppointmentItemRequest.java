package com.campus.visitor.modules.visitor.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AppointmentItemRequest {
    @NotBlank(message = "物品名称不能为空")
    private String itemName;

    @Min(value = 1, message = "数量至少为 1")
    private Integer quantity = 1;

    private String note;
}
