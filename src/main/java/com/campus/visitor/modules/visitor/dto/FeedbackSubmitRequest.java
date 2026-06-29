package com.campus.visitor.modules.visitor.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FeedbackSubmitRequest {
    @NotNull(message = "预约ID不能为空")
    private Long appointmentId;

    @NotNull(message = "审批速度评分不能为空")
    @Min(value = 1, message = "审批速度评分必须在 1-5 之间")
    @Max(value = 5, message = "审批速度评分必须在 1-5 之间")
    private Integer approvalSpeed;

    @NotNull(message = "门岗态度评分不能为空")
    @Min(value = 1, message = "门岗态度评分必须在 1-5 之间")
    @Max(value = 5, message = "门岗态度评分必须在 1-5 之间")
    private Integer guardAttitude;

    @NotNull(message = "校园环境评分不能为空")
    @Min(value = 1, message = "校园环境评分必须在 1-5 之间")
    @Max(value = 5, message = "校园环境评分必须在 1-5 之间")
    private Integer environment;

    @NotNull(message = "总体满意度评分不能为空")
    @Min(value = 1, message = "总体满意度评分必须在 1-5 之间")
    @Max(value = 5, message = "总体满意度评分必须在 1-5 之间")
    private Integer overall;

    private String comment;

    private Boolean anonymous;
}
