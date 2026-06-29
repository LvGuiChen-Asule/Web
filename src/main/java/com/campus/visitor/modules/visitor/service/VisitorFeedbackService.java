package com.campus.visitor.modules.visitor.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.visitor.modules.visitor.dto.FeedbackSubmitRequest;
import com.campus.visitor.modules.visitor.dto.FeedbackWallItem;
import com.campus.visitor.modules.visitor.entity.VisitorFeedback;

import java.util.List;

public interface VisitorFeedbackService {
    void submit(Long visitorId, FeedbackSubmitRequest request);

    IPage<VisitorFeedback> page(Page<VisitorFeedback> page, Boolean featured);

    void feature(Long id, boolean featured);

    List<FeedbackWallItem> wall(Integer limit);

    List<Long> eligibleAppointmentIds(Long visitorId);
}
