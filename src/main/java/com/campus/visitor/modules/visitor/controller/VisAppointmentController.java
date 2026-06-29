package com.campus.visitor.modules.visitor.controller;

import com.campus.visitor.common.result.Result;
import com.campus.visitor.modules.system.security.LoginUser;
import com.campus.visitor.modules.visitor.dto.AppointmentRequest;
import com.campus.visitor.modules.visitor.service.VisAppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.visitor.modules.visitor.dto.AuditRequest;
import com.campus.visitor.modules.visitor.entity.VisAppointment;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class VisAppointmentController {

    private final VisAppointmentService appointmentService;

    @PostMapping
    @PreAuthorize("hasRole('VISITOR')")
    public Result<Long> createAppointment(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody @Valid AppointmentRequest request
    ) {
        Long id = appointmentService.createAppointment(loginUser.getUserId(), request);
        return Result.success(id);
    }

    @PostMapping("/audit/host")
    @PreAuthorize("hasRole('HOST')")
    public Result<String> auditByHost(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody @Valid AuditRequest request
    ) {
        appointmentService.auditByHost(loginUser.getUserId(), request);
        return Result.success("审核完成");
    }

    @PostMapping("/audit/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> auditByAdmin(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody @Valid AuditRequest request
    ) {
        appointmentService.auditByAdmin(loginUser.getUserId(), request);
        return Result.success("审核完成");
    }

    @GetMapping("/visitor/list")
    @PreAuthorize("hasRole('VISITOR')")
    public Result<IPage<VisAppointment>> getMyVisitorAppointments(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime
    ) {
        return Result.success(appointmentService.getMyVisitorAppointments(new Page<>(current, size), loginUser.getUserId(), status, keyword, startTime, endTime));
    }

    @GetMapping("/host/list")
    @PreAuthorize("hasRole('HOST')")
    public Result<IPage<VisAppointment>> getMyHostAppointments(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime
    ) {
        return Result.success(appointmentService.getMyHostAppointments(new Page<>(current, size), loginUser.getUserId(), status, keyword, startTime, endTime));
    }

    @GetMapping("/admin/list")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<IPage<VisAppointment>> getAdminAuditAppointments(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime
    ) {
        return Result.success(appointmentService.getAdminAuditAppointments(new Page<>(current, size), status, keyword, startTime, endTime));
    }

    @GetMapping("/calendar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOST') or hasRole('VISITOR')")
    public Result<List<VisAppointment>> calendar(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime
    ) {
        boolean isAdmin = loginUser.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        boolean isHost = loginUser.getAuthorities().stream().anyMatch(a -> "ROLE_HOST".equals(a.getAuthority()));
        boolean isVisitor = loginUser.getAuthorities().stream().anyMatch(a -> "ROLE_VISITOR".equals(a.getAuthority()));

        var w = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<VisAppointment>()
                .ge(VisAppointment::getVisitStartTime, startTime)
                .lt(VisAppointment::getVisitStartTime, endTime)
                .eq(isHost && !isAdmin, VisAppointment::getHostId, loginUser.getUserId())
                .eq(isVisitor && !isAdmin, VisAppointment::getVisitorId, loginUser.getUserId())
                .orderByAsc(VisAppointment::getVisitStartTime);

        return Result.success(appointmentService.list(w));
    }
}
