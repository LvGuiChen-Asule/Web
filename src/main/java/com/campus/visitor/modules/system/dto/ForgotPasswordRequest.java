package com.campus.visitor.modules.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ForgotPasswordRequest {
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "身份证号不能为空")
    private String idCard;

    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
