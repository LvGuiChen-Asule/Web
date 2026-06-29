package com.campus.visitor.modules.system.controller;

import com.campus.visitor.common.result.Result;
import com.campus.visitor.modules.system.service.ConfigService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/config")
@RequiredArgsConstructor
public class ConfigController {
    private final ConfigService configService;

    @GetMapping("/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> get(@PathVariable String key) {
        return Result.success(configService.getString(key));
    }

    @PutMapping("/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> update(@PathVariable String key, @RequestBody @Valid UpdateRequest request) {
        configService.update(key, request.getValue());
        return Result.success("已更新");
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, String>> listAll() {
        return Result.success(configService.listAll());
    }

    @GetMapping("/list-detailed")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<com.campus.visitor.modules.system.entity.SystemConfig>> listDetailed() {
        return Result.success(configService.listDetailed());
    }

    @Data
    public static class UpdateRequest {
        @NotBlank(message = "配置值不能为空")
        private String value;
    }
}
