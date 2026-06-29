package com.campus.visitor.modules.visitor.dto.stats;

import lombok.Data;

@Data
public class HostRankingStats {
    private String hostName;
    private Long deptId; // Can be used to fetch dept name
    private Integer visitCount;
}
