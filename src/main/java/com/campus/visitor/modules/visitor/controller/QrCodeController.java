package com.campus.visitor.modules.visitor.controller;

import com.campus.visitor.common.result.Result;
import com.campus.visitor.modules.visitor.service.QrCodeService;
import com.campus.visitor.modules.visitor.service.dto.QrVerifyResult;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/qrcode")
@RequiredArgsConstructor
public class QrCodeController {
    private final QrCodeService qrCodeService;

    @GetMapping("/generate/{appointmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> generate(@PathVariable Long appointmentId) {
        return Result.success(qrCodeService.generate(appointmentId));
    }

    @PostMapping("/verify")
    @PreAuthorize("hasRole('GUARD')")
    public Result<QrVerifyResult> verify(@RequestBody @Valid VerifyRequest request) {
        return Result.success(qrCodeService.verify(request.getQrContent(), request.getCheckUsed() != null && request.getCheckUsed()));
    }

    @Data
    public static class VerifyRequest {
        private String qrContent;
        private Boolean checkUsed;
    }
}
