package com.campus.visitor.modules.visitor.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FeedbackWallItem {
    private Integer approvalSpeed;
    private Integer guardAttitude;
    private Integer environment;
    private Integer overall;
    private String comment;
    private String displayName;
    private LocalDateTime createTime;
}
