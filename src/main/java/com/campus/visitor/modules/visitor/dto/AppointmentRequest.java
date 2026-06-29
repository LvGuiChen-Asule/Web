package com.campus.visitor.modules.visitor.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AppointmentRequest {

    @NotNull(message = "被访人不能为空")
    private Long hostId;

    private Long areaId;

    @NotNull(message = "开始时间不能为空")
    @Future(message = "开始时间必须是未来时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime visitStartTime;

    @NotNull(message = "结束时间不能为空")
    @Future(message = "结束时间必须是未来时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime visitEndTime;

    @NotBlank(message = "来访事由不能为空")
    private String reason;

    @Min(value = 1, message = "来访人数至少为 1")
    private Integer visitorCount = 1;

    private String carPlate;

    private List<String> attachments;

    private List<AppointmentItemRequest> items;
}
