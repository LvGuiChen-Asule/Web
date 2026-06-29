package com.campus.visitor.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.visitor.modules.system.entity.SysRole;
import com.campus.visitor.modules.system.entity.SysUser;
import com.campus.visitor.modules.system.mapper.SysRoleMapper;
import com.campus.visitor.modules.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.init-demo-data", havingValue = "true")
public class DemoDataInitializer implements CommandLineRunner {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        ensureUserWithRole("host1", "Host One", "ROLE_HOST");
        ensureUserWithRole("guard1", "Guard One", "ROLE_GUARD");
    }

    private void ensureUserWithRole(String username, String realName, String roleCode) {
        SysUser exists = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (exists != null) return;

        SysRole role = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, roleCode));
        if (role == null) return;

        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPhone(username);
        user.setRealName(realName);
        user.setStatus(1);
        user.setPassword(passwordEncoder.encode("123456"));
        userMapper.insert(user);

        userMapper.saveUserRole(user.getId(), role.getId());
    }
}

