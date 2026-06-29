package com.campus.visitor.modules.system.controller;

import com.campus.visitor.common.result.Result;
import com.campus.visitor.modules.system.dto.AuthResponse;
import com.campus.visitor.modules.system.dto.ForgotPasswordRequest;
import com.campus.visitor.modules.system.dto.LoginRequest;
import com.campus.visitor.modules.system.dto.RegisterRequest;
import com.campus.visitor.modules.system.entity.SysUser;
import com.campus.visitor.modules.system.security.LoginUser;
import com.campus.visitor.modules.system.service.SysUserService;
import com.campus.visitor.util.JwtUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SysUserService userService;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public Result<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        LoginUser userDetails = (LoginUser) authentication.getPrincipal();
        String token = jwtUtils.generateToken(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return Result.success(AuthResponse.builder()
                .token(token)
                .userId(userDetails.getUserId())
                .username(userDetails.getUsername())
                .realName(userDetails.getUser().getRealName())
                .roles(roles)
                .build());
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody @Valid RegisterRequest request) {
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setPhone(request.getUsername()); // Phone is username for visitor
        user.setRealName(request.getRealName());
        user.setIdCard(request.getIdCard());
        
        userService.registerVisitor(user);
        return Result.success("Registration successful");
    }

    @PostMapping("/forgot-password")
    public Result<String> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        userService.resetPasswordByPhoneAndIdCard(
                request.getPhone(),
                request.getIdCard(),
                request.getNewPassword()
        );
        return Result.success("密码重置成功");
    }
}
