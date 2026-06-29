package com.campus.visitor.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.visitor.modules.system.entity.SysRole;

import java.util.List;

public interface SysRoleService extends IService<SysRole> {
    List<SysRole> getRolesByUserId(Long userId);
}
