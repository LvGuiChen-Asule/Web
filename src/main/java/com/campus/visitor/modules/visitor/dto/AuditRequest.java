package com.campus.visitor.modules.visitor.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AuditRequest {
    @NotNull(message = "预约ID不能为空")
    private Long id;

    private Boolean passed; // true: Approve, false: Reject

    private String remark; // Reason for rejection
}
