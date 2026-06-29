package com.campus.visitor.modules.visitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.visitor.modules.visitor.entity.VisAppointment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

import com.campus.visitor.modules.visitor.dto.stats.HostRankingStats;
import com.campus.visitor.modules.visitor.dto.stats.VisitorCountStats;
import java.util.List;

@Mapper
public interface VisAppointmentMapper extends BaseMapper<VisAppointment> {
    
    int countConflictingAppointments(@Param("hostId") Long hostId, 
                                     @Param("startTime") LocalDateTime startTime, 
                                     @Param("endTime") LocalDateTime endTime,
                                     @Param("excludeId") Long excludeId);

    int countVisitorConflictingAppointments(@Param("visitorId") Long visitorId,
                                            @Param("visitorIdCard") String visitorIdCard,
                                            @Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime,
                                            @Param("excludeId") Long excludeId);

    int countDailyAppointments(@Param("visitorId") Long visitorId,
                               @Param("visitorIdCard") String visitorIdCard,
                               @Param("dayStart") LocalDateTime dayStart,
                               @Param("dayEnd") LocalDateTime dayEnd);

    List<VisitorCountStats> selectVisitorTrend(@Param("startDate") String startDate, @Param("endDate") String endDate);
    
    List<HostRankingStats> selectHostRanking(@Param("limit") Integer limit);
}
