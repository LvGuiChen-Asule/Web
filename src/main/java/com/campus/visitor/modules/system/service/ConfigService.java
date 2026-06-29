package com.campus.visitor.modules.system.service;

import java.util.Map;
import java.util.List;

public interface ConfigService {
    String getString(String key);

    String getString(String key, String defaultValue);

    Integer getInt(String key);

    Integer getInt(String key, Integer defaultValue);

    Boolean getBool(String key);

    Boolean getBool(String key, Boolean defaultValue);

    Map<String, String> listAll();

    List<com.campus.visitor.modules.system.entity.SystemConfig> listDetailed();

    void update(String key, String value);

    void refreshCache();
}
