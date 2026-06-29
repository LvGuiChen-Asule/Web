package com.campus.visitor.modules.system.dto.admin;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class UpdateRolesRequest {
    @NotEmpty
    private List<String> roleCodes;
}

