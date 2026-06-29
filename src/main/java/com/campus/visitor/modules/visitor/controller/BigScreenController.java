package com.campus.visitor.modules.visitor.controller;

import com.campus.visitor.common.result.Result;
import com.campus.visitor.modules.visitor.dto.bigscreen.AreaCount;
import com.campus.visitor.modules.visitor.dto.bigscreen.BigScreenOverview;
import com.campus.visitor.modules.visitor.dto.bigscreen.HourCount;
import com.campus.visitor.modules.visitor.mapper.BigScreenMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/bigscreen")
@RequiredArgsConstructor
public class BigScreenController {
    private final BigScreenMapper bigScreenMapper;

    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN') or hasRole('GUARD')")
    public Result<BigScreenOverview> overview() {
        BigScreenOverview o = new BigScreenOverview();
        Long inCampus = bigScreenMapper.countInCampus();
        o.setInCampusTotal(inCampus == null ? 0L : inCampus);

        LocalDate today = LocalDate.now();
        LocalDateTime dayStart = today.atStartOfDay();
        LocalDateTime dayEnd = today.plusDays(1).atStartOfDay();

        List<HourCount> rawTrend = bigScreenMapper.selectTodayAppointmentTrend(dayStart, dayEnd);
        o.setTodayAppointmentTrend(fill24Hours(rawTrend));

        List<AreaCount> areas = new ArrayList<>(bigScreenMapper.selectInCampusByArea());
        Long withoutArea = bigScreenMapper.countInCampusWithoutArea();
        if (withoutArea != null && withoutArea > 0) {
            AreaCount unknown = new AreaCount();
            unknown.setAreaId(0L);
            unknown.setAreaName("未指定区域");
            unknown.setCount(withoutArea);
            areas.add(unknown);
        }
        o.setInCampusByArea(areas);
        return Result.success(o);
    }

    private List<HourCount> fill24Hours(List<HourCount> raw) {
        long[] bucket = new long[24];
        if (raw != null) {
            for (HourCount h : raw) {
                if (h == null || h.getHour() == null || h.getCount() == null) {
                    continue;
                }
                int idx = h.getHour();
                if (idx >= 0 && idx < 24) {
                    bucket[idx] = h.getCount();
                }
            }
        }
        List<HourCount> out = new ArrayList<>(24);
        for (int i = 0; i < 24; i++) {
            HourCount hc = new HourCount();
            hc.setHour(i);
            hc.setCount(bucket[i]);
            out.add(hc);
        }
        return out;
    }
}
