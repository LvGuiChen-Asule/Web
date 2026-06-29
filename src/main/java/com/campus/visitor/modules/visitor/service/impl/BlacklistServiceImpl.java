package com.campus.visitor.modules.visitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.visitor.common.exception.BizException;
import com.campus.visitor.modules.visitor.entity.Blacklist;
import com.campus.visitor.modules.visitor.mapper.BlacklistMapper;
import com.campus.visitor.modules.visitor.service.BlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BlacklistServiceImpl implements BlacklistService {
    private final BlacklistMapper blacklistMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(Long operatorId, Blacklist blacklist) {
        if (blacklist.getVisitorIdCard() == null || blacklist.getVisitorIdCard().isBlank()) {
            throw new BizException(400, "身份证号不能为空");
        }
        Blacklist existing = blacklistMapper.selectOne(new LambdaQueryWrapper<Blacklist>()
                .eq(Blacklist::getVisitorIdCard, blacklist.getVisitorIdCard())
                .last("LIMIT 1"));
        if (existing != null) {
            existing.setVisitorName(blacklist.getVisitorName());
            existing.setReason(blacklist.getReason());
            existing.setOperatorId(operatorId);
            existing.setExpireTime(blacklist.getExpireTime());
            blacklistMapper.updateById(existing);
            return;
        }
        blacklist.setOperatorId(operatorId);
        blacklistMapper.insert(blacklist);
    }

    @Override
    public void remove(Long id) {
        blacklistMapper.deleteById(id);
    }

    @Override
    public IPage<Blacklist> page(Page<Blacklist> page, String idCard) {
        return blacklistMapper.selectPage(page, new LambdaQueryWrapper<Blacklist>()
                .like(idCard != null && !idCard.isBlank(), Blacklist::getVisitorIdCard, idCard)
                .orderByDesc(Blacklist::getCreateTime));
    }

    @Override
    public boolean isActiveBlacklisted(String idCard) {
        if (idCard == null || idCard.isBlank()) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        Long cnt = blacklistMapper.selectCount(new LambdaQueryWrapper<Blacklist>()
                .eq(Blacklist::getVisitorIdCard, idCard)
                .and(w -> w.isNull(Blacklist::getExpireTime).or().gt(Blacklist::getExpireTime, now)));
        return cnt != null && cnt > 0;
    }
}
