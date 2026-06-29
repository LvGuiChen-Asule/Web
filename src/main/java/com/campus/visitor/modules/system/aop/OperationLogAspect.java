package com.campus.visitor.modules.system.aop;

import com.campus.visitor.modules.system.entity.OperationLog;
import com.campus.visitor.modules.system.mapper.OperationLogMapper;
import com.campus.visitor.modules.system.security.LoginUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {
    private final OperationLogMapper operationLogMapper;
    private final ObjectMapper objectMapper;
    private final HttpServletRequest request;

    @Pointcut("execution(* com.campus.visitor..controller..*(..))")
    public void controllerMethods() {
    }

    @Around("controllerMethods()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = pjp.proceed();
        long cost = System.currentTimeMillis() - start;

        MethodSignature sig = (MethodSignature) pjp.getSignature();
        String method = request.getMethod() + " " + request.getRequestURI();
        String operation = sig.getDeclaringType().getSimpleName() + "." + sig.getName();

        OperationLog log = new OperationLog();
        log.setUsername(currentUsername());
        log.setOperation(operation);
        log.setMethod(method);
        log.setExecutionTime(cost);
        log.setIpAddress(request.getRemoteAddr());
        log.setParams(maskSensitive(toJsonParams(sig.getParameterNames(), pjp.getArgs())));
        operationLogMapper.insert(log);
        return result;
    }

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof LoginUser loginUser) {
            return loginUser.getUsername();
        }
        if (principal instanceof String s) {
            return s;
        }
        return null;
    }

    private String toJsonParams(String[] names, Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < args.length; i++) {
            String k = names != null && i < names.length ? names[i] : "arg" + i;
            Object v = args[i];
            map.put(k, v);
        }
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private String maskSensitive(String json) {
        if (json == null) {
            return null;
        }
        String masked = json;
        masked = masked.replaceAll("(\"password\"\\s*:\\s*\")[^\"]*(\")", "$1***$2");
        masked = masked.replaceAll("(\"idCard\"\\s*:\\s*\")[^\"]*(\")", "$1***$2");
        masked = masked.replaceAll("(\"visitorIdCard\"\\s*:\\s*\")[^\"]*(\")", "$1***$2");
        return masked;
    }
}
