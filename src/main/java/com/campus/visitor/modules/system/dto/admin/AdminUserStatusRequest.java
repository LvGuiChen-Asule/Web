package com.campus.visitor.modules.system.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminUserStatusRequest {
    @NotNull
    private Integer status;
}

