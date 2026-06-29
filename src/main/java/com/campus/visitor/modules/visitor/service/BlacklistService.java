package com.campus.visitor.modules.visitor.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.visitor.modules.visitor.entity.Blacklist;

public interface BlacklistService {
    void add(Long operatorId, Blacklist blacklist);

    void remove(Long id);

    IPage<Blacklist> page(Page<Blacklist> page, String idCard);

    boolean isActiveBlacklisted(String idCard);
}
