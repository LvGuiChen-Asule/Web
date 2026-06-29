package com.campus.visitor.modules.system.service;

import java.util.Map;

public interface AdminAuditService {
    void logSuccess(Long actorUserId, String actorUsername, String operation, String method, Map<String, Object> params, String ip);
}

