package com.campus.visitor.modules.system.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class AdminUserCreateRequest {

    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank
    @Size(min = 6, max = 72)
    private String password;

    @Size(max = 50)
    private String realName;

    @Size(max = 20)
    private String phone;

    @Size(max = 20)
    private String idCard;

    private Long deptId;

    private Integer status;

    @NotEmpty
    private List<String> roleCodes;
}

