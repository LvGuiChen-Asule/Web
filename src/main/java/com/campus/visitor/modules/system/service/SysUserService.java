package com.campus.visitor.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.visitor.modules.system.entity.SysUser;

import java.util.List;

public interface SysUserService extends IService<SysUser> {
    SysUser getByUsername(String username);
    void registerVisitor(SysUser user);
    List<SysUser> searchHosts(String keyword);
    void updatePassword(Long userId, String oldPassword, String newPassword);
    void resetPasswordByPhoneAndIdCard(String phone, String idCard, String newPassword);
    
    /**
     * 获取所有管理员
     */
    List<SysUser> findAdmins();
}
