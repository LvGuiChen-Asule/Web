package com.campus.visitor.modules.visitor.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.visitor.common.result.Result;
import com.campus.visitor.modules.visitor.entity.Area;
import com.campus.visitor.modules.visitor.mapper.AreaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/area")
@RequiredArgsConstructor
public class AreaController {
    private final AreaMapper areaMapper;

    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public Result<List<Area>> listActive() {
        return Result.success(areaMapper.selectList(new LambdaQueryWrapper<Area>()
                .eq(Area::getIsActive, true)
                .orderByAsc(Area::getAreaName)));
    }
}
