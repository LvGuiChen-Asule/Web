package com.campus.visitor.modules.system.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotBlank
    @Size(min = 6, max = 72)
    private String newPassword;
}

