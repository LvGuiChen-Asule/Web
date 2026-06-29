package com.campus.visitor.modules.visitor.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.visitor.common.exception.BizException;
import com.campus.visitor.common.result.Result;
import com.campus.visitor.modules.system.security.LoginUser;
import com.campus.visitor.modules.visitor.dto.AppointmentItemRequest;
import com.campus.visitor.modules.visitor.entity.AppointmentItem;
import com.campus.visitor.modules.visitor.entity.VisAppointment;
import com.campus.visitor.modules.visitor.mapper.AppointmentItemMapper;
import com.campus.visitor.modules.visitor.mapper.VisAppointmentMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/appointment/items")
@RequiredArgsConstructor
public class AppointmentItemController {
    private final AppointmentItemMapper appointmentItemMapper;
    private final VisAppointmentMapper appointmentMapper;

    @GetMapping("/{appointmentId}")
    @PreAuthorize("isAuthenticated()")
    public Result<List<AppointmentItem>> list(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long appointmentId
    ) {
        ensureCanAccess(loginUser, appointmentId);
        List<AppointmentItem> items = appointmentItemMapper.selectList(new LambdaQueryWrapper<AppointmentItem>()
                .eq(AppointmentItem::getAppointmentId, appointmentId)
                .orderByAsc(AppointmentItem::getId));
        return Result.success(items);
    }

    @PutMapping("/{appointmentId}")
    @PreAuthorize("hasRole('VISITOR')")
    public Result<String> replace(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long appointmentId,
            @RequestBody @Valid List<AppointmentItemRequest> items
    ) {
        VisAppointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            throw new BizException(404, "预约不存在");
        }
        if (!loginUser.getUserId().equals(appointment.getVisitorId())) {
            throw new BizException(403, "无权限");
        }
        appointmentItemMapper.delete(new LambdaQueryWrapper<AppointmentItem>()
                .eq(AppointmentItem::getAppointmentId, appointmentId));
        if (items != null) {
            for (AppointmentItemRequest req : items) {
                if (req == null || req.getItemName() == null || req.getItemName().isBlank()) {
                    continue;
                }
                AppointmentItem it = new AppointmentItem();
                it.setAppointmentId(appointmentId);
                it.setItemName(req.getItemName());
                it.setQuantity(req.getQuantity() == null ? 1 : req.getQuantity());
                it.setNote(req.getNote());
                appointmentItemMapper.insert(it);
            }
        }
        return Result.success("成功");
    }

    private void ensureCanAccess(LoginUser loginUser, Long appointmentId) {
        if (loginUser == null) {
            throw new BizException(401, "未登录");
        }
        boolean elevated = loginUser.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()) || "ROLE_GUARD".equals(a.getAuthority()));
        if (elevated) {
            return;
        }
        VisAppointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            throw new BizException(404, "预约不存在");
        }
        if (loginUser.getUserId().equals(appointment.getVisitorId()) || loginUser.getUserId().equals(appointment.getHostId())) {
            return;
        }
        throw new BizException(403, "无权限");
    }
}
