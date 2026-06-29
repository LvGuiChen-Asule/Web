package com.campus.visitor.modules.visitor.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.visitor.common.result.Result;
import com.campus.visitor.modules.system.security.LoginUser;
import com.campus.visitor.modules.visitor.entity.Blacklist;
import com.campus.visitor.modules.visitor.service.BlacklistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/blacklist")
@RequiredArgsConstructor
public class BlacklistController {
    private final BlacklistService blacklistService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> add(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody @Valid Blacklist body
    ) {
        blacklistService.add(loginUser.getUserId(), body);
        return Result.success("操作成功");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> remove(@PathVariable Long id) {
        blacklistService.remove(id);
        return Result.success("操作成功");
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<IPage<Blacklist>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String idCard
    ) {
        return Result.success(blacklistService.page(new Page<>(current, size), idCard));
    }
}
