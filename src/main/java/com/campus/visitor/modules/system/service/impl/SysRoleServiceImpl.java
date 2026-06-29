package com.campus.visitor.modules.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.visitor.modules.system.entity.SysRole;
import com.campus.visitor.modules.system.mapper.SysRoleMapper;
import com.campus.visitor.modules.system.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    @Override
    public List<SysRole> getRolesByUserId(Long userId) {
        return baseMapper.selectRolesByUserId(userId);
    }
}
