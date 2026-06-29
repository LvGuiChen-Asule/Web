package com.campus.visitor.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.visitor.common.exception.BizException;
import com.campus.visitor.modules.system.dto.admin.AdminUserCreateRequest;
import com.campus.visitor.modules.system.dto.admin.AdminUserUpdateRequest;
import com.campus.visitor.modules.system.dto.admin.AdminUserVO;
import com.campus.visitor.modules.system.dto.admin.UserRolePair;
import com.campus.visitor.modules.system.entity.SysRole;
import com.campus.visitor.modules.system.entity.SysUser;
import com.campus.visitor.modules.system.mapper.SysRoleMapper;
import com.campus.visitor.modules.system.mapper.SysUserMapper;
import com.campus.visitor.modules.system.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public IPage<AdminUserVO> pageUsers(Page<?> page, String keyword, Integer status, String roleCode) {
        Page<SysUser> p = new Page<>(page.getCurrent(), page.getSize());

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, SysUser::getStatus, status);
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(SysUser::getUsername, kw)
                    .or().like(SysUser::getRealName, kw)
                    .or().like(SysUser::getPhone, kw));
        }

        if (roleCode != null && !roleCode.trim().isEmpty()) {
            List<Long> userIds = userMapper.selectUserIdsByRoleCode(roleCode.trim());
            if (userIds == null || userIds.isEmpty()) {
                Page<AdminUserVO> empty = new Page<>(page.getCurrent(), page.getSize());
                empty.setTotal(0);
                empty.setRecords(List.of());
                return empty;
            }
            wrapper.in(SysUser::getId, userIds);
        }

        wrapper.orderByDesc(SysUser::getCreateTime);
        IPage<SysUser> userPage = userMapper.selectPage(p, wrapper);
        List<SysUser> users = userPage.getRecords();

        Map<Long, List<String>> roleCodesByUserId = loadRoleCodes(users);
        List<AdminUserVO> records = users.stream().map(u -> toVO(u, roleCodesByUserId.get(u.getId()))).collect(Collectors.toList());

        Page<AdminUserVO> voPage = new Page<>(userPage.getCurrent(), userPage.getSize());
        voPage.setTotal(userPage.getTotal());
        voPage.setRecords(records);
        return voPage;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long createUser(AdminUserCreateRequest request) {
        SysUser user = new SysUser();
        user.setUsername(request.getUsername().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(trimToNull(request.getRealName()));
        user.setPhone(trimToNull(request.getPhone()));
        user.setIdCard(trimToNull(request.getIdCard()));
        user.setDeptId(request.getDeptId());
        user.setStatus(request.getStatus() == null ? 1 : request.getStatus());

        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException e) {
            throw new BizException(400, "用户名或手机号已存在");
        }

        assignRoles(user.getId(), request.getRoleCodes());
        return user.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateUser(Long id, AdminUserUpdateRequest request) {
        SysUser exists = userMapper.selectById(id);
        if (exists == null) throw new BizException(404, "用户不存在");

        SysUser user = new SysUser();
        user.setId(id);
        user.setRealName(trimToNull(request.getRealName()));
        user.setPhone(trimToNull(request.getPhone()));
        user.setIdCard(trimToNull(request.getIdCard()));
        user.setDeptId(request.getDeptId());

        try {
            userMapper.updateById(user);
        } catch (DuplicateKeyException e) {
            throw new BizException(400, "用户名或手机号已存在");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void setUserStatus(Long id, Integer status, Long operatorUserId) {
        if (operatorUserId != null && operatorUserId.equals(id)) {
            throw new BizException(400, "不能禁用自己");
        }
        SysUser exists = userMapper.selectById(id);
        if (exists == null) throw new BizException(404, "用户不存在");
        SysUser user = new SysUser();
        user.setId(id);
        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void resetPassword(Long id, String newPassword) {
        SysUser exists = userMapper.selectById(id);
        if (exists == null) throw new BizException(404, "用户不存在");
        SysUser user = new SysUser();
        user.setId(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateRoles(Long id, List<String> roleCodes, Long operatorUserId) {
        SysUser exists = userMapper.selectById(id);
        if (exists == null) throw new BizException(404, "用户不存在");
        if (operatorUserId != null && operatorUserId.equals(id)) {
            boolean keepAdmin = roleCodes.stream().anyMatch(rc -> "ROLE_ADMIN".equals(rc));
            if (!keepAdmin) throw new BizException(400, "不能移除自己的管理员角色");
        }
        userMapper.deleteUserRolesByUserId(id);
        assignRoles(id, roleCodes);
    }

    private void assignRoles(Long userId, List<String> roleCodes) {
        List<String> codes = roleCodes == null ? List.of() : roleCodes.stream()
                .filter(s -> s != null && !s.trim().isEmpty())
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
        if (codes.isEmpty()) throw new BizException(400, "至少分配一个角色");

        List<SysRole> roles = roleMapper.selectByRoleCodes(codes);
        Map<String, Long> idByCode = roles.stream().collect(Collectors.toMap(SysRole::getRoleCode, SysRole::getId, (a, b) -> a));

        List<String> missing = new ArrayList<>();
        for (String code : codes) {
            if (!idByCode.containsKey(code)) missing.add(code);
        }
        if (!missing.isEmpty()) throw new BizException(400, "角色不存在: " + String.join(",", missing));

        for (String code : codes) {
            userMapper.saveUserRole(userId, idByCode.get(code));
        }
    }

    private Map<Long, List<String>> loadRoleCodes(List<SysUser> users) {
        if (users == null || users.isEmpty()) return Map.of();
        List<Long> ids = users.stream().map(SysUser::getId).collect(Collectors.toList());
        List<UserRolePair> pairs = roleMapper.selectRoleCodesByUserIds(ids);
        Map<Long, List<String>> map = new HashMap<>();
        for (UserRolePair p : pairs) {
            map.computeIfAbsent(p.getUserId(), k -> new ArrayList<>()).add(p.getRoleCode());
        }
        return map;
    }

    private AdminUserVO toVO(SysUser u, List<String> roles) {
        AdminUserVO vo = new AdminUserVO();
        vo.setId(u.getId());
        vo.setUsername(u.getUsername());
        vo.setRealName(u.getRealName());
        vo.setPhone(u.getPhone());
        vo.setIdCard(u.getIdCard());
        vo.setDeptId(u.getDeptId());
        vo.setStatus(u.getStatus());
        vo.setRoles(roles == null ? List.of() : roles);
        vo.setCreateTime(u.getCreateTime());
        vo.setUpdateTime(u.getUpdateTime());
        return vo;
    }

    private String trimToNull(String v) {
        if (v == null) return null;
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }
}

