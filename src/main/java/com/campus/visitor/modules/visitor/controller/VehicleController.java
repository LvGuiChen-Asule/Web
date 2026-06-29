package com.campus.visitor.modules.visitor.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.visitor.common.exception.BizException;
import com.campus.visitor.common.result.Result;
import com.campus.visitor.modules.system.security.LoginUser;
import com.campus.visitor.modules.visitor.entity.Vehicle;
import com.campus.visitor.modules.visitor.mapper.VehicleMapper;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/vehicle")
@RequiredArgsConstructor
public class VehicleController {
    private final VehicleMapper vehicleMapper;

    @PostMapping
    @PreAuthorize("hasRole('VISITOR')")
    public Result<String> upsert(@AuthenticationPrincipal LoginUser loginUser, @RequestBody @Valid Vehicle body) {
        if (body.getAppointmentId() == null) {
            throw new BizException(400, "预约ID不能为空");
        }
        if (body.getLicensePlate() == null || body.getLicensePlate().isBlank()) {
            throw new BizException(400, "车牌号不能为空");
        }
        Vehicle existing = vehicleMapper.selectOne(new LambdaQueryWrapper<Vehicle>()
                .eq(Vehicle::getAppointmentId, body.getAppointmentId())
                .last("LIMIT 1"));
        if (existing == null) {
            body.setStatus("OUT");
            vehicleMapper.insert(body);
        } else {
            existing.setLicensePlate(body.getLicensePlate());
            existing.setVisitorName(body.getVisitorName());
            existing.setVisitorPhone(body.getVisitorPhone());
            vehicleMapper.updateById(existing);
        }
        return Result.success("操作成功");
    }

    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("isAuthenticated()")
    public Result<Vehicle> getByAppointment(@PathVariable Long appointmentId) {
        Vehicle v = vehicleMapper.selectOne(new LambdaQueryWrapper<Vehicle>()
                .eq(Vehicle::getAppointmentId, appointmentId)
                .last("LIMIT 1"));
        return Result.success(v);
    }

    @PostMapping("/entry")
    @PreAuthorize("hasRole('GUARD')")
    public Result<String> entry(@RequestBody @Valid VehicleAccessRequest request) {
        Vehicle v = vehicleMapper.selectOne(new LambdaQueryWrapper<Vehicle>()
                .eq(Vehicle::getAppointmentId, request.getAppointmentId())
                .last("LIMIT 1"));
        if (v == null) {
            throw new BizException(404, "车辆信息不存在");
        }
        v.setEntryTime(LocalDateTime.now());
        v.setStatus("IN");
        vehicleMapper.updateById(v);
        return Result.success("操作成功");
    }

    @PostMapping("/exit")
    @PreAuthorize("hasRole('GUARD')")
    public Result<String> exit(@RequestBody @Valid VehicleAccessRequest request) {
        Vehicle v = vehicleMapper.selectOne(new LambdaQueryWrapper<Vehicle>()
                .eq(Vehicle::getAppointmentId, request.getAppointmentId())
                .last("LIMIT 1"));
        if (v == null) {
            throw new BizException(404, "车辆信息不存在");
        }
        v.setExitTime(LocalDateTime.now());
        v.setStatus("OUT");
        vehicleMapper.updateById(v);
        return Result.success("操作成功");
    }

    @Data
    public static class VehicleAccessRequest {
        private Long appointmentId;
    }
}
