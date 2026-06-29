package com.campus.visitor.modules.visitor.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.visitor.common.result.Result;
import com.campus.visitor.modules.system.security.LoginUser;
import com.campus.visitor.modules.visitor.dto.FeedbackSubmitRequest;
import com.campus.visitor.modules.visitor.dto.FeedbackWallItem;
import com.campus.visitor.modules.visitor.entity.VisitorFeedback;
import com.campus.visitor.modules.visitor.service.VisitorFeedbackService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/feedback")
@RequiredArgsConstructor
public class FeedbackController {
    private final VisitorFeedbackService visitorFeedbackService;

    @PostMapping
    @PreAuthorize("hasRole('VISITOR')")
    public Result<String> submit(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody @Valid FeedbackSubmitRequest request
    ) {
        visitorFeedbackService.submit(loginUser.getUserId(), request);
        return Result.success("提交成功");
    }

    @GetMapping("/eligible")
    @PreAuthorize("hasRole('VISITOR')")
    public Result<List<Long>> eligible(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(visitorFeedbackService.eligibleAppointmentIds(loginUser.getUserId()));
    }

    @GetMapping("/wall")
    @PreAuthorize("isAuthenticated()")
    public Result<List<FeedbackWallItem>> wall(@RequestParam(required = false) Integer limit) {
        return Result.success(visitorFeedbackService.wall(limit));
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<IPage<VisitorFeedback>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Boolean featured
    ) {
        return Result.success(visitorFeedbackService.page(new Page<>(current, size), featured));
    }

    @PutMapping("/feature/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> feature(@PathVariable Long id, @RequestBody @Valid FeatureRequest request) {
        visitorFeedbackService.feature(id, Boolean.TRUE.equals(request.getFeatured()));
        return Result.success("操作成功");
    }

    @Data
    public static class FeatureRequest {
        private Boolean featured;
    }
}
