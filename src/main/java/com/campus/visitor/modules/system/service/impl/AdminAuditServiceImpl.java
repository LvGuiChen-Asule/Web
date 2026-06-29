package com.campus.visitor.modules.system.service.impl;

import com.campus.visitor.modules.system.entity.SysLog;
import com.campus.visitor.modules.system.mapper.SysLogMapper;
import com.campus.visitor.modules.system.service.AdminAuditService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminAuditServiceImpl implements AdminAuditService {

    private final SysLogMapper sysLogMapper;
    private final ObjectMapper objectMapper;

    @Override
    public void logSuccess(Long actorUserId, String actorUsername, String operation, String method, Map<String, Object> params, String ip) {
        SysLog log = new SysLog();
        log.setUserId(actorUserId);
        log.setUsername(actorUsername);
        log.setOperation(operation);
        log.setMethod(method);
        log.setParams(serializeParams(params));
        log.setIp(ip);
        sysLogMapper.insert(log);
    }

    private String serializeParams(Map<String, Object> params) {
        if (params == null || params.isEmpty()) return null;

        Map<String, Object> safe = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            if (key == null) continue;

            String lower = key.toLowerCase();
            if (lower.contains("password") || lower.contains("token") || lower.contains("authorization")) {
                continue;
            }
            if (lower.contains("phone")) {
                Object v = entry.getValue();
                safe.put(key, maskPhone(v == null ? null : String.valueOf(v)));
                continue;
            }
            if (lower.contains("idcard") || lower.contains("id_card")) {
                Object v = entry.getValue();
                safe.put(key, maskIdCard(v == null ? null : String.valueOf(v)));
                continue;
            }
            safe.put(key, entry.getValue());
        }

        try {
            return objectMapper.writeValueAsString(safe);
        } catch (Exception e) {
            return null;
        }
    }

    private String maskPhone(String phone) {
        if (phone == null) return null;
        String p = phone.trim();
        if (p.length() < 7) return p;
        return p.substring(0, 3) + "****" + p.substring(p.length() - 4);
    }

    private String maskIdCard(String idCard) {
        if (idCard == null) return null;
        String v = idCard.trim();
        if (v.length() <= 8) return v;
        return v.substring(0, 4) + "********" + v.substring(v.length() - 4);
    }
}

