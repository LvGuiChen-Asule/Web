package com.campus.visitor.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.visitor.common.exception.BizException;
import com.campus.visitor.modules.system.entity.SysRole;
import com.campus.visitor.modules.system.entity.SysUser;
import com.campus.visitor.modules.system.mapper.SysRoleMapper;
import com.campus.visitor.modules.system.mapper.SysUserMapper;
import com.campus.visitor.modules.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final PasswordEncoder passwordEncoder;
    private final SysRoleMapper roleMapper;

    @Override
    public SysUser getByUsername(String username) {
        return this.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
    }

    @Override
    public List<SysUser> searchHosts(String keyword) {
        return baseMapper.selectHostsByKeyword(keyword);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void registerVisitor(SysUser user) {
        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(1); // Enable by default
        this.save(user);

        // Assign Visitor Role
        SysRole visitorRole = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, "ROLE_VISITOR"));
        if (visitorRole != null) {
            baseMapper.saveUserRole(user.getId(), visitorRole.getId());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = this.getById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BizException(400, "原密码不正确");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        this.updateById(user);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void resetPasswordByPhoneAndIdCard(String phone, String idCard, String newPassword) {
        // 查找同时匹配手机号和身份证号的用户
        SysUser user = this.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPhone, phone)
                .eq(SysUser::getIdCard, idCard));

        if (user == null) {
            throw new BizException("验证失败：未找到匹配的手机号和身份证号");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        this.updateById(user);
    }

    @Override
    public List<SysUser> findAdmins() {
        return baseMapper.selectAdmins();
    }
}
