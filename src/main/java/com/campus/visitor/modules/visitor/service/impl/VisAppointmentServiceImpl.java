package com.campus.visitor.modules.visitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.visitor.common.exception.BlacklistException;
import com.campus.visitor.common.exception.BizException;
import com.campus.visitor.common.exception.ConflictException;
import com.campus.visitor.common.exception.LimitExceededException;
import com.campus.visitor.modules.system.entity.SysUser;
import com.campus.visitor.modules.system.service.ConfigService;
import com.campus.visitor.modules.system.service.MessageService;
import com.campus.visitor.modules.system.service.SysUserService;
import com.campus.visitor.modules.visitor.dto.AppointmentRequest;
import com.campus.visitor.modules.visitor.dto.AppointmentItemRequest;
import com.campus.visitor.modules.visitor.entity.VisAppointment;
import com.campus.visitor.modules.visitor.entity.AppointmentItem;
import com.campus.visitor.modules.visitor.enums.AppointmentStatus;
import com.campus.visitor.modules.visitor.mapper.VisAppointmentMapper;
import com.campus.visitor.modules.visitor.service.QrCodeService;
import com.campus.visitor.modules.visitor.service.VisAppointmentService;
import com.campus.visitor.modules.visitor.service.BlacklistService;
import com.campus.visitor.modules.visitor.dto.AuditRequest;
import com.campus.visitor.modules.visitor.dto.QrCodePayload;
import com.campus.visitor.modules.visitor.entity.AccessRecord;
import com.campus.visitor.modules.visitor.entity.ApprovalRecord;
import com.campus.visitor.modules.visitor.mapper.AccessRecordMapper;
import com.campus.visitor.modules.visitor.mapper.ApprovalRecordMapper;
import com.campus.visitor.modules.visitor.mapper.AppointmentItemMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.campus.visitor.modules.visitor.dto.stats.HostRankingStats;
import com.campus.visitor.modules.visitor.dto.stats.VisitorCountStats;
import com.campus.visitor.modules.visitor.service.AIPreReviewService;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VisAppointmentServiceImpl extends ServiceImpl<VisAppointmentMapper, VisAppointment> implements VisAppointmentService {

    private final SysUserService sysUserService;
    private final ConfigService configService;
    private final MessageService messageService;
    private final BlacklistService blacklistService;
    private final QrCodeService qrCodeService;
    private final AIPreReviewService aiPreReviewService;
    private final ApprovalRecordMapper approvalRecordMapper;
    private final AccessRecordMapper accessRecordMapper;
    private final AppointmentItemMapper appointmentItemMapper;
    private final ObjectMapper objectMapper;

    @Override
    public List<VisitorCountStats> getVisitorTrend(String startDate, String endDate) {
        return baseMapper.selectVisitorTrend(startDate, endDate);
    }

    @Override
    public List<HostRankingStats> getHostRanking(Integer limit) {
        return baseMapper.selectHostRanking(limit);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkIn(Long guardId, String qrContent) {
        Long appointmentId = parseAppointmentId(qrContent);
        VisAppointment appointment = this.getById(appointmentId);
        if (appointment == null) {
            throw new BizException(404, "预约不存在");
        }

        if (!AppointmentStatus.APPROVED.name().equals(appointment.getStatus())) {
            throw new BizException(400, "预约未通过审批");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(appointment.getVisitStartTime()) || now.isAfter(appointment.getVisitEndTime())) {
            throw new BizException(400, "不在预约有效时间范围内");
        }

        Long open = accessRecordMapper.selectCount(new LambdaQueryWrapper<AccessRecord>()
                .eq(AccessRecord::getAppointmentId, appointmentId)
                .eq(AccessRecord::getStatus, "IN_CAMPUS"));
        if (open != null && open > 0) {
            throw new BizException(400, "该访客已在校内");
        }

        AccessRecord record = new AccessRecord();
        record.setAppointmentId(appointmentId);
        record.setVisitorName(appointment.getVisitorName());
        record.setEntryTime(now);
        record.setStatus("IN_CAMPUS");
        accessRecordMapper.insert(record);

        appointment.setStatus(AppointmentStatus.CHECKED_IN.name());
        this.updateById(appointment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkOut(Long guardId, String qrContent) {
        Long appointmentId = parseAppointmentId(qrContent);
        VisAppointment appointment = this.getById(appointmentId);
        if (appointment == null) {
            throw new BizException(404, "预约不存在");
        }

        AccessRecord record = accessRecordMapper.selectOne(new LambdaQueryWrapper<AccessRecord>()
                .eq(AccessRecord::getAppointmentId, appointmentId)
                .eq(AccessRecord::getStatus, "IN_CAMPUS")
                .orderByDesc(AccessRecord::getEntryTime)
                .last("LIMIT 1"));
        if (record == null) {
            throw new BizException(400, "未找到在校记录");
        }

        LocalDateTime now = LocalDateTime.now();
        record.setExitTime(now);
        long minutes = ChronoUnit.MINUTES.between(record.getEntryTime(), now);
        record.setDurationMinutes((int) Math.max(0, minutes));
        record.setStatus("LEFT");
        accessRecordMapper.updateById(record);

        appointment.setStatus(AppointmentStatus.CHECKED_OUT.name());
        this.updateById(appointment);
        messageService.send(appointment.getVisitorId(), "FEEDBACK_INVITE", "服务评价", "感谢来访，欢迎在评价页面对本次体验进行评分与留言");
    }

    @Override
    public IPage<VisAppointment> getMyVisitorAppointments(Page<VisAppointment> page, Long visitorId, String status, String keyword, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<VisAppointment> w = new LambdaQueryWrapper<VisAppointment>()
                .eq(VisAppointment::getVisitorId, visitorId)
                .eq(status != null && !status.isBlank(), VisAppointment::getStatus, status)
                .ge(startTime != null, VisAppointment::getVisitStartTime, startTime)
                .le(endTime != null, VisAppointment::getVisitStartTime, endTime)
                .orderByDesc(VisAppointment::getCreateTime);
        applyKeyword(w, keyword);
        return this.page(page, w);
    }

    @Override
    public IPage<VisAppointment> getMyHostAppointments(Page<VisAppointment> page, Long hostId, String status, String keyword, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<VisAppointment> w = new LambdaQueryWrapper<VisAppointment>()
                .eq(VisAppointment::getHostId, hostId)
                .eq(status != null && !status.isBlank(), VisAppointment::getStatus, status)
                .ge(startTime != null, VisAppointment::getVisitStartTime, startTime)
                .le(endTime != null, VisAppointment::getVisitStartTime, endTime)
                .orderByDesc(VisAppointment::getCreateTime);
        applyKeyword(w, keyword);
        return this.page(page, w);
    }

    @Override
    public IPage<VisAppointment> getAdminAuditAppointments(Page<VisAppointment> page, String status, String keyword, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<VisAppointment> w = new LambdaQueryWrapper<VisAppointment>()
                .eq(status != null && !status.isBlank(), VisAppointment::getStatus, status)
                .ge(startTime != null, VisAppointment::getVisitStartTime, startTime)
                .le(endTime != null, VisAppointment::getVisitStartTime, endTime)
                .orderByDesc(VisAppointment::getCreateTime);
        applyKeyword(w, keyword);
        return this.page(page, w);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAppointment(Long visitorId, AppointmentRequest request) {
        if (request.getVisitEndTime().isBefore(request.getVisitStartTime())) {
            throw new BizException("结束时间必须晚于开始时间");
        }

        if (!Boolean.TRUE.equals(configService.getBool("appointment_enabled", true))) {
            throw new BizException(403, "预约功能已关闭");
        }

        Integer maxDurationHours = configService.getInt("max_duration_hours", 4);
        long durationMinutes = ChronoUnit.MINUTES.between(request.getVisitStartTime(), request.getVisitEndTime());
        if (durationMinutes > (long) maxDurationHours * 60) {
            throw new BizException(400, "预约时长超过限制");
        }

        SysUser visitor = sysUserService.getById(visitorId);
        if (visitor == null) {
            throw new BizException(404, "访客不存在");
        }

        String idCard = visitor.getIdCard();
        if (blacklistService.isActiveBlacklisted(idCard)) {
            throw new BlacklistException("访客已被加入黑名单，禁止预约");
        }

        int hostConflicts = baseMapper.countConflictingAppointments(
                request.getHostId(),
                request.getVisitStartTime(),
                request.getVisitEndTime(),
                null
        );
        if (hostConflicts > 0) {
            throw new ConflictException("时间冲突：被访人在该时间段已有预约");
        }

        int visitorConflicts = baseMapper.countVisitorConflictingAppointments(
                visitorId,
                idCard,
                request.getVisitStartTime(),
                request.getVisitEndTime(),
                null
        );
        if (visitorConflicts > 0) {
            throw new ConflictException("时间冲突：访客在该时间段已有重叠预约");
        }

        int limit = configService.getInt("daily_max_appointments", 3);
        LocalDateTime dayStart = request.getVisitStartTime().toLocalDate().atStartOfDay();
        LocalDateTime dayEnd = request.getVisitStartTime().toLocalDate().plusDays(1).atStartOfDay();
        int used = baseMapper.countDailyAppointments(visitorId, idCard, dayStart, dayEnd);
        if (used >= limit) {
            throw new LimitExceededException("当日预约次数已达上限");
        }

        VisAppointment appointment = new VisAppointment();
        appointment.setVisitorId(visitorId);
        appointment.setVisitorName(visitor.getRealName() == null || visitor.getRealName().isBlank() ? visitor.getUsername() : visitor.getRealName());
        appointment.setVisitorPhone(visitor.getPhone());
        appointment.setVisitorIdCard(visitor.getIdCard());
        appointment.setHostId(request.getHostId());
        appointment.setAreaId(request.getAreaId());
        appointment.setVisitStartTime(request.getVisitStartTime());
        appointment.setVisitEndTime(request.getVisitEndTime());
        appointment.setReason(request.getReason());
        appointment.setStatus(AppointmentStatus.PENDING.name());
        
        // AI预审：异步调用AI服务进行风险评估
        AIPreReviewService.RiskLevel riskLevel = aiPreReviewService.assessRisk(appointment);
        appointment.setAiRiskLevel(riskLevel.name());
        
        // 根据AI预审结果决定是否跳过初审
        if (riskLevel == AIPreReviewService.RiskLevel.HIGH_RISK) {
            // 高风险预约直接进入管理员终审
            appointment.setStatus(AppointmentStatus.FIRST_APPROVED.name()); // 跳过初审
        }
        
        this.save(appointment);
        
        // 如果是高风险预约，发送通知给管理员
        if (riskLevel == AIPreReviewService.RiskLevel.HIGH_RISK) {
            messageService.sendToAdmin("HIGH_RISK_APPOINTMENT", "高风险预约提醒", 
                "访客 " + appointment.getVisitorName() + " 提交的预约被AI识别为高风险，请立即审核。事由：" + appointment.getReason());
        }
        
        saveItems(appointment.getId(), request.getItems());
        return appointment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditByHost(Long userId, AuditRequest request) {
        VisAppointment appointment = this.getById(request.getId());
        if (appointment == null) {
            throw new BizException(404, "预约不存在");
        }
        if (!appointment.getHostId().equals(userId)) {
            throw new BizException("无权限：你不是该预约的被访人");
        }
        if (!AppointmentStatus.PENDING.name().equals(appointment.getStatus())) {
            throw new BizException("状态不允许操作");
        }

        if (Boolean.TRUE.equals(request.getPassed())) {
            int conflicts = baseMapper.countConflictingAppointments(
                    appointment.getHostId(),
                    appointment.getVisitStartTime(),
                    appointment.getVisitEndTime(),
                    appointment.getId()
            );
            if (conflicts > 0) {
                throw new ConflictException("时间冲突");
            }
            appointment.setStatus(AppointmentStatus.FIRST_APPROVED.name());
            messageService.send(appointment.getVisitorId(), "APPROVAL_RESULT", "预约审批更新", "被访人已通过，等待管理员审批");
        } else {
            appointment.setStatus(AppointmentStatus.REJECTED.name());
            messageService.send(appointment.getVisitorId(), "APPROVAL_RESULT", "预约审批结果", "被访人已拒绝");
        }

        ApprovalRecord record = new ApprovalRecord();
        record.setAppointmentId(appointment.getId());
        record.setApproverId(userId);
        record.setApproverRole("FIRST");
        record.setAction(Boolean.TRUE.equals(request.getPassed()) ? "APPROVE" : "REJECT");
        record.setComment(request.getRemark());
        approvalRecordMapper.insert(record);

        this.updateById(appointment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditByAdmin(Long userId, AuditRequest request) {
        VisAppointment appointment = this.getById(request.getId());
        if (appointment == null) {
            throw new BizException(404, "预约不存在");
        }
        if (!AppointmentStatus.FIRST_APPROVED.name().equals(appointment.getStatus())) {
            throw new BizException("状态不允许操作");
        }

        if (Boolean.TRUE.equals(request.getPassed())) {
            int conflicts = baseMapper.countConflictingAppointments(
                    appointment.getHostId(),
                    appointment.getVisitStartTime(),
                    appointment.getVisitEndTime(),
                    appointment.getId()
            );
            if (conflicts > 0) {
                throw new ConflictException("时间冲突");
            }
            appointment.setStatus(AppointmentStatus.APPROVED.name());
            appointment.setQrcodeUrl(null);
            messageService.send(appointment.getVisitorId(), "APPROVAL_RESULT", "预约审批结果", "管理员已通过");
            messageService.send(appointment.getHostId(), "APPROVAL_RESULT", "预约审批结果", "管理员已通过");
        } else {
            appointment.setStatus(AppointmentStatus.REJECTED.name());
            messageService.send(appointment.getVisitorId(), "APPROVAL_RESULT", "预约审批结果", "管理员已拒绝");
            messageService.send(appointment.getHostId(), "APPROVAL_RESULT", "预约审批结果", "管理员已拒绝");
        }

        ApprovalRecord record = new ApprovalRecord();
        record.setAppointmentId(appointment.getId());
        record.setApproverId(userId);
        record.setApproverRole("SECOND");
        record.setAction(Boolean.TRUE.equals(request.getPassed()) ? "APPROVE" : "REJECT");
        record.setComment(request.getRemark());
        approvalRecordMapper.insert(record);

        this.updateById(appointment);
        if (Boolean.TRUE.equals(request.getPassed())) {
            qrCodeService.generate(appointment.getId());
        }
    }

    private Long parseAppointmentId(String qrContent) {
        if (qrContent == null || qrContent.isBlank()) {
            throw new BizException(400, "二维码内容不能为空");
        }
        try {
            QrCodePayload payload = objectMapper.readValue(qrContent, QrCodePayload.class);
            if (payload.getAppointmentId() == null) {
                throw new BizException(400, "二维码内容不合法");
            }
            return payload.getAppointmentId();
        } catch (JsonProcessingException e) {
            throw new BizException(400, "二维码内容不合法");
        }
    }

    private void saveItems(Long appointmentId, List<AppointmentItemRequest> items) {
        if (appointmentId == null || items == null || items.isEmpty()) {
            return;
        }
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

    private void applyKeyword(LambdaQueryWrapper<VisAppointment> w, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return;
        }
        String k = keyword.trim();
        Long id = null;
        try {
            id = Long.parseLong(k);
        } catch (NumberFormatException ignored) {
        }
        Long finalId = id;
        w.and(q -> {
            if (finalId != null) {
                q.eq(VisAppointment::getId, finalId).or();
            }
            q.like(VisAppointment::getVisitorName, k)
                    .or()
                    .like(VisAppointment::getVisitorPhone, k)
                    .or()
                    .like(VisAppointment::getVisitorIdCard, k);
        });
    }
}
