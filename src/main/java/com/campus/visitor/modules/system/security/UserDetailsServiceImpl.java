package com.campus.visitor.modules.system.security;

import com.campus.visitor.modules.system.entity.SysRole;
import com.campus.visitor.modules.system.entity.SysUser;
import com.campus.visitor.modules.system.service.SysRoleService;
import com.campus.visitor.modules.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserService userService;
    private final SysRoleService roleService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userService.getByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        List<SysRole> roles = roleService.getRolesByUserId(user.getId());
        List<String> roleCodes = roles.stream()
                .map(SysRole::getRoleCode)
                .collect(Collectors.toList());

        return new LoginUser(user, roleCodes);
    }
}
