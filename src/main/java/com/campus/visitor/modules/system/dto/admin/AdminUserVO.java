package com.campus.visitor.modules.system.dto.admin;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AdminUserVO {
    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String idCard;
    private Long deptId;
    private Integer status;
    private List<String> roles;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

