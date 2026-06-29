package com.campus.visitor.modules.visitor.dto.bigscreen;

import lombok.Data;

import java.util.List;

@Data
public class BigScreenOverview {
    private Long inCampusTotal;
    private List<HourCount> todayAppointmentTrend;
    private List<AreaCount> inCampusByArea;
}
