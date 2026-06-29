package com.campus.visitor.modules.visitor.mapper;

import com.campus.visitor.modules.visitor.dto.bigscreen.AreaCount;
import com.campus.visitor.modules.visitor.dto.bigscreen.HourCount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface BigScreenMapper {
    @Select("SELECT COUNT(DISTINCT appointment_id) FROM access_record WHERE status = 'IN_CAMPUS'")
    Long countInCampus();

    @Select("""
            SELECT a.id AS areaId, a.area_name AS areaName, COALESCE(t.cnt, 0) AS count
            FROM area a
            LEFT JOIN (
                SELECT ap.area_id AS areaId, COUNT(DISTINCT ar.appointment_id) AS cnt
                FROM access_record ar
                INNER JOIN appointment ap ON ap.id = ar.appointment_id
                WHERE ar.status = 'IN_CAMPUS' AND ap.area_id IS NOT NULL
                GROUP BY ap.area_id
            ) t ON t.areaId = a.id
            WHERE a.is_active = 1
            ORDER BY a.id
            """)
    List<AreaCount> selectInCampusByArea();

    @Select("""
            SELECT HOUR(start_time) AS hour, COUNT(1) AS count
            FROM appointment
            WHERE start_time >= #{dayStart} AND start_time < #{dayEnd}
              AND status IN ('PENDING','FIRST_APPROVED','APPROVED','CHECKED_IN','CHECKED_OUT')
            GROUP BY HOUR(start_time)
            ORDER BY hour
            """)
    List<HourCount> selectTodayAppointmentTrend(@Param("dayStart") LocalDateTime dayStart, @Param("dayEnd") LocalDateTime dayEnd);

    @Select("""
            SELECT COUNT(DISTINCT ar.appointment_id)
            FROM access_record ar
            INNER JOIN appointment ap ON ap.id = ar.appointment_id
            WHERE ar.status = 'IN_CAMPUS' AND ap.area_id IS NULL
            """)
    Long countInCampusWithoutArea();
}
