package com.campus.visitor.modules.visitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.visitor.modules.visitor.dto.AppointmentRequest;
import com.campus.visitor.modules.visitor.dto.AuditRequest;
import com.campus.visitor.modules.visitor.entity.VisAppointment;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.time.LocalDateTime;

import com.campus.visitor.modules.visitor.dto.stats.HostRankingStats;
import com.campus.visitor.modules.visitor.dto.stats.VisitorCountStats;
import java.util.List;

public interface VisAppointmentService extends IService<VisAppointment> {
    Long createAppointment(Long visitorId, AppointmentRequest request);
    void auditByHost(Long userId, AuditRequest request);
    void auditByAdmin(Long userId, AuditRequest request);
    IPage<VisAppointment> getMyVisitorAppointments(Page<VisAppointment> page, Long visitorId, String status, String keyword, LocalDateTime startTime, LocalDateTime endTime);
    IPage<VisAppointment> getMyHostAppointments(Page<VisAppointment> page, Long hostId, String status, String keyword, LocalDateTime startTime, LocalDateTime endTime);
    IPage<VisAppointment> getAdminAuditAppointments(Page<VisAppointment> page, String status, String keyword, LocalDateTime startTime, LocalDateTime endTime);
    
    // Gate Operations
    void checkIn(Long guardId, String qrContent);
    void checkOut(Long guardId, String qrContent);

    // Stats
    List<VisitorCountStats> getVisitorTrend(String startDate, String endDate);
    List<HostRankingStats> getHostRanking(Integer limit);
}
