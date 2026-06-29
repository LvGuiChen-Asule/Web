package com.campus.visitor.modules.visitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.visitor.modules.visitor.entity.VisitorFeedback;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface VisitorFeedbackMapper extends BaseMapper<VisitorFeedback> {
    @Select("""
            SELECT a.id
            FROM appointment a
            LEFT JOIN visitor_feedback f ON f.appointment_id = a.id
            WHERE a.visitor_id = #{visitorId}
              AND a.status = 'CHECKED_OUT'
              AND f.id IS NULL
            ORDER BY a.end_time DESC
            LIMIT 100
            """)
    List<Long> selectEligibleAppointmentIds(@Param("visitorId") Long visitorId);
}
