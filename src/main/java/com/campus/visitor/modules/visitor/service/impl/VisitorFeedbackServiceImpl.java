package com.campus.visitor.modules.visitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.visitor.common.exception.BizException;
import com.campus.visitor.modules.visitor.dto.FeedbackSubmitRequest;
import com.campus.visitor.modules.visitor.dto.FeedbackWallItem;
import com.campus.visitor.modules.visitor.entity.VisAppointment;
import com.campus.visitor.modules.visitor.entity.VisitorFeedback;
import com.campus.visitor.modules.visitor.enums.AppointmentStatus;
import com.campus.visitor.modules.visitor.mapper.VisAppointmentMapper;
import com.campus.visitor.modules.visitor.mapper.VisitorFeedbackMapper;
import com.campus.visitor.modules.visitor.service.VisitorFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VisitorFeedbackServiceImpl implements VisitorFeedbackService {
    private final VisitorFeedbackMapper visitorFeedbackMapper;
    private final VisAppointmentMapper appointmentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(Long visitorId, FeedbackSubmitRequest request) {
        VisAppointment appointment = appointmentMapper.selectById(request.getAppointmentId());
        if (appointment == null) {
            throw new BizException(404, "预约不存在");
        }
        if (!visitorId.equals(appointment.getVisitorId())) {
            throw new BizException(403, "无权限");
        }
        if (!AppointmentStatus.CHECKED_OUT.name().equals(appointment.getStatus())) {
            throw new BizException(400, "该预约未离校，暂不可评价");
        }
        Long existing = visitorFeedbackMapper.selectCount(new LambdaQueryWrapper<VisitorFeedback>()
                .eq(VisitorFeedback::getAppointmentId, appointment.getId()));
        if (existing != null && existing > 0) {
            throw new BizException(400, "已提交过评价");
        }

        VisitorFeedback f = new VisitorFeedback();
        f.setAppointmentId(appointment.getId());
        f.setVisitorId(visitorId);
        f.setVisitorName(appointment.getVisitorName());
        f.setApprovalSpeed(request.getApprovalSpeed());
        f.setGuardAttitude(request.getGuardAttitude());
        f.setEnvironment(request.getEnvironment());
        f.setOverall(request.getOverall());
        f.setComment(request.getComment());
        f.setIsAnonymous(Boolean.TRUE.equals(request.getAnonymous()));
        f.setIsFeatured(false);
        visitorFeedbackMapper.insert(f);
    }

    @Override
    public IPage<VisitorFeedback> page(Page<VisitorFeedback> page, Boolean featured) {
        return visitorFeedbackMapper.selectPage(page, new LambdaQueryWrapper<VisitorFeedback>()
                .eq(featured != null, VisitorFeedback::getIsFeatured, featured)
                .orderByDesc(VisitorFeedback::getCreateTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void feature(Long id, boolean featured) {
        VisitorFeedback f = visitorFeedbackMapper.selectById(id);
        if (f == null) {
            throw new BizException(404, "评价不存在");
        }
        f.setIsFeatured(featured);
        visitorFeedbackMapper.updateById(f);
    }

    @Override
    public List<FeedbackWallItem> wall(Integer limit) {
        int n = limit == null ? 20 : Math.min(Math.max(limit, 1), 100);
        List<VisitorFeedback> list = visitorFeedbackMapper.selectList(new LambdaQueryWrapper<VisitorFeedback>()
                .eq(VisitorFeedback::getIsFeatured, true)
                .orderByDesc(VisitorFeedback::getCreateTime)
                .last("LIMIT " + n));
        return list.stream().map(f -> {
            FeedbackWallItem i = new FeedbackWallItem();
            i.setApprovalSpeed(f.getApprovalSpeed());
            i.setGuardAttitude(f.getGuardAttitude());
            i.setEnvironment(f.getEnvironment());
            i.setOverall(f.getOverall());
            i.setComment(f.getComment());
            i.setDisplayName(Boolean.TRUE.equals(f.getIsAnonymous()) ? "匿名访客" : (f.getVisitorName() == null ? "访客" : f.getVisitorName()));
            i.setCreateTime(f.getCreateTime());
            return i;
        }).toList();
    }

    @Override
    public List<Long> eligibleAppointmentIds(Long visitorId) {
        return visitorFeedbackMapper.selectEligibleAppointmentIds(visitorId);
    }
}
