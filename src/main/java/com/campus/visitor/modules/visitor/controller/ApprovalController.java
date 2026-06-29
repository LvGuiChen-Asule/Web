package com.campus.visitor.modules.visitor.controller;

import com.campus.visitor.common.result.Result;
import com.campus.visitor.modules.system.security.LoginUser;
import com.campus.visitor.modules.visitor.dto.AuditRequest;
import com.campus.visitor.modules.visitor.service.VisAppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/approval")
@RequiredArgsConstructor
public class ApprovalController {
    private final VisAppointmentService appointmentService;

    @PostMapping("/first")
    @PreAuthorize("hasRole('HOST')")
    public Result<String> first(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody @Valid AuditRequest request
    ) {
        appointmentService.auditByHost(loginUser.getUserId(), request);
        return Result.success("操作成功");
    }

    @PostMapping("/second")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> second(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody @Valid AuditRequest request
    ) {
        appointmentService.auditByAdmin(loginUser.getUserId(), request);
        return Result.success("操作成功");
    }
}
