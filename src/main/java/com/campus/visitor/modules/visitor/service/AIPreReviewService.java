package com.campus.visitor.modules.visitor.service;

import com.campus.visitor.modules.visitor.entity.VisAppointment;

/**
 * AI智能预审服务接口
 */
public interface AIPreReviewService {
    
    /**
     * 对预约申请进行AI风险评估
     * @param appointment 预约信息
     * @return 风险等级（HIGH_RISK, NORMAL）
     */
    RiskLevel assessRisk(VisAppointment appointment);
    
    /**
     * 风险等级枚举
     */
    enum RiskLevel {
        HIGH_RISK,  // 高风险
        NORMAL      // 正常
    }
}