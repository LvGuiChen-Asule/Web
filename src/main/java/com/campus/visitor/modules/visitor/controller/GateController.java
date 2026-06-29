package com.campus.visitor.modules.visitor.controller;

import com.campus.visitor.common.result.Result;
import com.campus.visitor.modules.system.security.LoginUser;
import com.campus.visitor.modules.visitor.service.VisAppointmentService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/gate")
@RequiredArgsConstructor
public class GateController {

    private final VisAppointmentService appointmentService;

    @PostMapping("/check-in")
    @PreAuthorize("hasRole('GUARD')")
    public Result<String> checkIn(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody QrRequest request
    ) {
        appointmentService.checkIn(loginUser.getUserId(), request.getQrContent());
        return Result.success("Check-in successful");
    }

    @PostMapping("/check-out")
    @PreAuthorize("hasRole('GUARD')")
    public Result<String> checkOut(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody QrRequest request
    ) {
        appointmentService.checkOut(loginUser.getUserId(), request.getQrContent());
        return Result.success("Check-out successful");
    }

    @Data
    public static class QrRequest {
        private String qrContent;
    }
}
