package com.campus.visitor.modules.system.controller;

import com.campus.visitor.common.result.Result;
import com.campus.visitor.modules.system.dto.UpdatePasswordRequest;
import com.campus.visitor.modules.system.entity.SysUser;
import com.campus.visitor.modules.system.security.LoginUser;
import com.campus.visitor.modules.system.service.SysUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final SysUserService userService;

    @GetMapping("/hosts")
    public Result<List<SysUser>> searchHosts(@RequestParam String keyword) {
        return Result.success(userService.searchHosts(keyword));
    }

    @PostMapping("/password")
    public Result<String> updatePassword(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody @Valid UpdatePasswordRequest request
    ) {
        userService.updatePassword(loginUser.getUserId(), request.getOldPassword(), request.getNewPassword());
        return Result.success("Password updated successfully");
    }
}
