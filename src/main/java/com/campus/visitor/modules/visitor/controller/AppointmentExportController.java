package com.campus.visitor.modules.visitor.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.visitor.common.exception.BizException;
import com.campus.visitor.modules.system.entity.SysUser;
import com.campus.visitor.modules.system.service.SysUserService;
import com.campus.visitor.modules.visitor.entity.VisAppointment;
import com.campus.visitor.modules.visitor.mapper.VisAppointmentMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/appointment")
@RequiredArgsConstructor
public class AppointmentExportController {
    private final VisAppointmentMapper appointmentMapper;
    private final SysUserService sysUserService;

    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public void export(
            HttpServletResponse response,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String visitorName
    ) {
        List<VisAppointment> list = appointmentMapper.selectList(new LambdaQueryWrapper<VisAppointment>()
                .ge(startTime != null, VisAppointment::getVisitStartTime, startTime)
                .le(endTime != null, VisAppointment::getVisitStartTime, endTime)
                .eq(status != null && !status.isBlank(), VisAppointment::getStatus, status)
                .like(visitorName != null && !visitorName.isBlank(), VisAppointment::getVisitorName, visitorName)
                .orderByDesc(VisAppointment::getCreateTime));

        List<ExportRow> rows = new ArrayList<>(list.size());
        for (VisAppointment a : list) {
            ExportRow r = new ExportRow();
            r.setAppointmentId(a.getId());
            r.setVisitorName(a.getVisitorName());
            r.setVisitorPhone(a.getVisitorPhone());
            r.setVisitorIdCard(a.getVisitorIdCard());
            SysUser host = sysUserService.getById(a.getHostId());
            r.setVisitedUser(host == null ? String.valueOf(a.getHostId()) : (host.getRealName() == null ? host.getUsername() : host.getRealName()));
            r.setStartTime(a.getVisitStartTime());
            r.setEndTime(a.getVisitEndTime());
            r.setStatus(a.getStatus());
            r.setCreateTime(a.getCreateTime());
            rows.add(r);
        }

        try {
            String filename = URLEncoder.encode("appointments.xlsx", StandardCharsets.UTF_8);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + filename);
            EasyExcel.write(response.getOutputStream(), ExportRow.class).sheet("appointments").doWrite(rows);
        } catch (IOException e) {
            throw new BizException(500, "导出失败");
        }
    }

    @Data
    public static class ExportRow {
        @ExcelProperty("预约ID")
        private Long appointmentId;
        @ExcelProperty("访客姓名")
        private String visitorName;
        @ExcelProperty("手机号")
        private String visitorPhone;
        @ExcelProperty("身份证")
        private String visitorIdCard;
        @ExcelProperty("被访人")
        private String visitedUser;
        @ExcelProperty("开始时间")
        private LocalDateTime startTime;
        @ExcelProperty("结束时间")
        private LocalDateTime endTime;
        @ExcelProperty("状态")
        private String status;
        @ExcelProperty("创建时间")
        private LocalDateTime createTime;
    }
}
