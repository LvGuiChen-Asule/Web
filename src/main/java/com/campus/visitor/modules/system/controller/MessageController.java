package com.campus.visitor.modules.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.visitor.common.result.Result;
import com.campus.visitor.modules.system.entity.UserMessage;
import com.campus.visitor.modules.system.security.LoginUser;
import com.campus.visitor.modules.system.service.MessageService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/message")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;

    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public Result<IPage<UserMessage>> list(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return Result.success(messageService.list(new Page<>(current, size), loginUser.getUserId()));
    }

    @PutMapping("/read/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<String> read(@AuthenticationPrincipal LoginUser loginUser, @PathVariable Long id) {
        messageService.markRead(loginUser.getUserId(), id);
        return Result.success("操作成功");
    }

    @PutMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public Result<String> readAll(@AuthenticationPrincipal LoginUser loginUser) {
        messageService.markReadAll(loginUser.getUserId());
        return Result.success("操作成功");
    }

    @PostMapping("/system")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> sendSystem(@RequestBody @Valid SendRequest request) {
        messageService.send(request.getReceiverId(), "SYSTEM", request.getTitle(), request.getContent());
        return Result.success("发送成功");
    }

    @Data
    public static class SendRequest {
        private Long receiverId;
        private String title;
        private String content;
    }
}
