package com.campus.visitor.modules.system.dto.admin;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminUserUpdateRequest {

    @Size(max = 50)
    private String realName;

    @Size(max = 20)
    private String phone;

    @Size(max = 20)
    private String idCard;

    private Long deptId;
}

