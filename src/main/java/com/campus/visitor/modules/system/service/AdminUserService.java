package com.campus.visitor.modules.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.visitor.modules.system.dto.admin.AdminUserCreateRequest;
import com.campus.visitor.modules.system.dto.admin.AdminUserUpdateRequest;
import com.campus.visitor.modules.system.dto.admin.AdminUserVO;

import java.util.List;

public interface AdminUserService {
    IPage<AdminUserVO> pageUsers(Page<?> page, String keyword, Integer status, String roleCode);

    Long createUser(AdminUserCreateRequest request);

    void updateUser(Long id, AdminUserUpdateRequest request);

    void setUserStatus(Long id, Integer status, Long operatorUserId);

    void resetPassword(Long id, String newPassword);

    void updateRoles(Long id, List<String> roleCodes, Long operatorUserId);
}

