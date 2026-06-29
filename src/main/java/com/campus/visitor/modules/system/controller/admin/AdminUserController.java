package com.campus.visitor.modules.system.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.visitor.common.result.Result;
import com.campus.visitor.modules.system.dto.admin.*;
import com.campus.visitor.modules.system.entity.SysRole;
import com.campus.visitor.modules.system.security.LoginUser;
import com.campus.visitor.modules.system.service.AdminAuditService;
import com.campus.visitor.modules.system.service.AdminUserService;
import com.campus.visitor.modules.system.service.SysRoleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final SysRoleService roleService;
    private final AdminAuditService adminAuditService;

    @GetMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<SysRole>> listRoles() {
        return Result.success(roleService.list());
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<IPage<AdminUserVO>> pageUsers(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String roleCode
    ) {
        return Result.success(adminUserService.pageUsers(new Page<>(current, size), keyword, status, roleCode));
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Long> createUser(
            @AuthenticationPrincipal LoginUser loginUser,
            HttpServletRequest httpRequest,
            @RequestBody @Valid AdminUserCreateRequest request
    ) {
        Long id = adminUserService.createUser(request);
        adminAuditService.logSuccess(loginUser.getUserId(), loginUser.getUsername(), "创建用户", "POST /api/v1/admin/users", auditParams("userId", id, "username", request.getUsername(), "roleCodes", request.getRoleCodes()), clientIp(httpRequest));
        return Result.success(id);
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> updateUser(
            @AuthenticationPrincipal LoginUser loginUser,
            HttpServletRequest httpRequest,
            @PathVariable Long id,
            @RequestBody @Valid AdminUserUpdateRequest request
    ) {
        adminUserService.updateUser(id, request);
        adminAuditService.logSuccess(loginUser.getUserId(), loginUser.getUsername(), "编辑用户", "PUT /api/v1/admin/users/{id}", auditParams("userId", id, "realName", request.getRealName(), "phone", request.getPhone(), "idCard", request.getIdCard(), "deptId", request.getDeptId()), clientIp(httpRequest));
        return Result.success("操作成功");
    }

    @PatchMapping("/users/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> setStatus(
            @AuthenticationPrincipal LoginUser loginUser,
            HttpServletRequest httpRequest,
            @PathVariable Long id,
            @RequestBody @Valid AdminUserStatusRequest request
    ) {
        adminUserService.setUserStatus(id, request.getStatus(), loginUser.getUserId());
        adminAuditService.logSuccess(loginUser.getUserId(), loginUser.getUsername(), request.getStatus() != null && request.getStatus() == 1 ? "启用用户" : "禁用用户", "PATCH /api/v1/admin/users/{id}/status", auditParams("userId", id, "status", request.getStatus()), clientIp(httpRequest));
        return Result.success("操作成功");
    }

    @PostMapping("/users/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> resetPassword(
            @AuthenticationPrincipal LoginUser loginUser,
            HttpServletRequest httpRequest,
            @PathVariable Long id,
            @RequestBody @Valid ResetPasswordRequest request
    ) {
        adminUserService.resetPassword(id, request.getNewPassword());
        adminAuditService.logSuccess(loginUser.getUserId(), loginUser.getUsername(), "重置密码", "POST /api/v1/admin/users/{id}/reset-password", auditParams("userId", id), clientIp(httpRequest));
        return Result.success("操作成功");
    }

    @PutMapping("/users/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> updateRoles(
            @AuthenticationPrincipal LoginUser loginUser,
            HttpServletRequest httpRequest,
            @PathVariable Long id,
            @RequestBody @Valid UpdateRolesRequest request
    ) {
        adminUserService.updateRoles(id, request.getRoleCodes(), loginUser.getUserId());
        adminAuditService.logSuccess(loginUser.getUserId(), loginUser.getUsername(), "分配角色", "PUT /api/v1/admin/users/{id}/roles", auditParams("userId", id, "roleCodes", request.getRoleCodes()), clientIp(httpRequest));
        return Result.success("操作成功");
    }

    private Map<String, Object> auditParams(Object... kv) {
        Map<String, Object> map = new HashMap<>();
        if (kv == null) return map;
        for (int i = 0; i + 1 < kv.length; i += 2) {
            Object k = kv[i];
            if (k == null) continue;
            map.put(String.valueOf(k), kv[i + 1]);
        }
        return map;
    }

    private String clientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.trim().isEmpty()) return xf.split(",")[0].trim();
        return request.getRemoteAddr();
    }
}
