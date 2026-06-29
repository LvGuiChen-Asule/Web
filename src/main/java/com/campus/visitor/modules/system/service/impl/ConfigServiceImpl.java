package com.campus.visitor.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.visitor.modules.system.entity.SystemConfig;
import com.campus.visitor.modules.system.mapper.SystemConfigMapper;
import com.campus.visitor.modules.system.service.ConfigService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {
    private final SystemConfigMapper systemConfigMapper;

    private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        refreshCache();
    }

    @Override
    public String getString(String key) {
        return cache.get(key);
    }

    @Override
    public String getString(String key, String defaultValue) {
        return cache.getOrDefault(key, defaultValue);
    }

    @Override
    public Integer getInt(String key) {
        String v = cache.get(key);
        if (v == null) {
            return null;
        }
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public Integer getInt(String key, Integer defaultValue) {
        Integer v = getInt(key);
        return v == null ? defaultValue : v;
    }

    @Override
    public Boolean getBool(String key) {
        String v = cache.get(key);
        if (v == null) {
            return null;
        }
        return "true".equalsIgnoreCase(v) || "1".equals(v);
    }

    @Override
    public Boolean getBool(String key, Boolean defaultValue) {
        Boolean v = getBool(key);
        return v == null ? defaultValue : v;
    }

    @Override
    public Map<String, String> listAll() {
        return Collections.unmodifiableMap(cache);
    }

    @Override
    public List<SystemConfig> listDetailed() {
        return systemConfigMapper.selectList(new LambdaQueryWrapper<SystemConfig>()
                .orderByAsc(SystemConfig::getConfigKey));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(String key, String value) {
        SystemConfig existing = systemConfigMapper.selectOne(new LambdaQueryWrapper<SystemConfig>()
                .eq(SystemConfig::getConfigKey, key)
                .last("LIMIT 1"));
        if (existing == null) {
            SystemConfig cfg = new SystemConfig();
            cfg.setConfigKey(key);
            cfg.setConfigValue(value);
            systemConfigMapper.insert(cfg);
        } else {
            existing.setConfigValue(value);
            systemConfigMapper.updateById(existing);
        }
        cache.put(key, value);
    }

    @Override
    public void refreshCache() {
        List<SystemConfig> all = systemConfigMapper.selectList(new LambdaQueryWrapper<>());
        Map<String, String> next = all.stream()
                .filter(c -> c.getConfigKey() != null)
                .collect(Collectors.toMap(SystemConfig::getConfigKey, SystemConfig::getConfigValue, (a, b) -> b));
        cache.clear();
        cache.putAll(next);
    }
}
