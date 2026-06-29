package com.campus.visitor.modules.system.controller.stats;

import com.campus.visitor.common.result.Result;
import com.campus.visitor.modules.visitor.dto.stats.HostRankingStats;
import com.campus.visitor.modules.visitor.dto.stats.VisitorCountStats;
import com.campus.visitor.modules.visitor.service.VisAppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
public class StatsController {

    private final VisAppointmentService appointmentService;

    @GetMapping("/visitor-trend")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<VisitorCountStats>> getVisitorTrend(
            @RequestParam String startDate,
            @RequestParam String endDate
    ) {
        return Result.success(appointmentService.getVisitorTrend(startDate, endDate));
    }

    @GetMapping("/host-ranking")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<HostRankingStats>> getHostRanking(
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        return Result.success(appointmentService.getHostRanking(limit));
    }
}
