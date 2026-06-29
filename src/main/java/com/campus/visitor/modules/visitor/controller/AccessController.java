package com.campus.visitor.modules.visitor.controller;

import com.campus.visitor.common.exception.BizException;
import com.campus.visitor.common.result.Result;
import com.campus.visitor.modules.system.security.LoginUser;
import com.campus.visitor.modules.visitor.service.VisAppointmentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/access")
@RequiredArgsConstructor
public class AccessController {
    private final VisAppointmentService appointmentService;
    private final ObjectMapper objectMapper;

    @PostMapping("/entry")
    @PreAuthorize("hasRole('GUARD')")
    public Result<String> entry(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody @Valid AccessRequest request
    ) {
        appointmentService.checkIn(loginUser.getUserId(), normalizeQr(request));
        return Result.success("操作成功");
    }

    @PostMapping("/exit")
    @PreAuthorize("hasRole('GUARD')")
    public Result<String> exit(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody @Valid AccessRequest request
    ) {
        appointmentService.checkOut(loginUser.getUserId(), normalizeQr(request));
        return Result.success("操作成功");
    }

    private String normalizeQr(AccessRequest request) {
        if (request.getQrContent() != null && !request.getQrContent().isBlank()) {
            return request.getQrContent();
        }
        if (request.getAppointmentId() == null) {
            throw new BizException(400, "二维码内容或预约ID不能为空");
        }
        try {
            return objectMapper.writeValueAsString(new MinimalQr(request.getAppointmentId()));
        } catch (JsonProcessingException e) {
            throw new BizException(500, "生成二维码内容失败");
        }
    }

    @Data
    public static class AccessRequest {
        private String qrContent;
        private Long appointmentId;
    }

    @Data
    public static class MinimalQr {
        private final Long appointmentId;
    }
}
