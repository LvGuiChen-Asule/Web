package com.campus.visitor.common.exception;

import com.campus.visitor.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public Result<?> handleBizException(BizException e) {
        log.error("BizException: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.error("Validation failed: {}", message);
        return Result.error(400, message);
    }

    @ExceptionHandler(BindException.class)
    public Result<?> handleBindException(BindException e) {
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.error("Binding failed: {}", message);
        return Result.error(400, message);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public Result<?> handleBadCredentialsException(BadCredentialsException e) {
        log.error("Authentication failed: {}", e.getMessage());
        return Result.error(401, "用户名或密码错误");
    }
    
    @ExceptionHandler(UsernameNotFoundException.class)
    public Result<?> handleUsernameNotFoundException(UsernameNotFoundException e) {
        log.error("User not found: {}", e.getMessage());
        return Result.error(401, "用户不存在");
    }

    @ExceptionHandler(DisabledException.class)
    public Result<?> handleDisabledException(DisabledException e) {
        return Result.error(401, "账号已被禁用");
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<?> handleAccessDeniedException(AccessDeniedException e) {
        return Result.error(403, "无权限访问");
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("System error", e);
        return Result.error(500, "服务器内部错误");
    }
}
