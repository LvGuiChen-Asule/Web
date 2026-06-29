package com.campus.visitor.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.visitor.common.exception.BizException;
import com.campus.visitor.modules.system.entity.UserMessage;
import com.campus.visitor.modules.system.mapper.UserMessageMapper;
import com.campus.visitor.modules.system.service.MessageService;
import com.campus.visitor.modules.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final UserMessageMapper userMessageMapper;
    private final SysUserService sysUserService;

    @Override
    public void send(Long receiverId, String type, String title, String content) {
        if (receiverId == null) {
            throw new BizException(400, "接收人不能为空");
        }
        UserMessage m = new UserMessage();
        m.setReceiverId(receiverId);
        m.setType(type);
        m.setTitle(title);
        m.setContent(content);
        m.setIsRead(false);
        userMessageMapper.insert(m);
    }

    @Override
    public void sendToAdmin(String type, String title, String content) {
        // 发送给所有管理员
        sysUserService.findAdmins().forEach(admin -> {
            UserMessage m = new UserMessage();
            m.setReceiverId(admin.getId());
            m.setType(type);
            m.setTitle(title);
            m.setContent(content);
            m.setIsRead(false);
            userMessageMapper.insert(m);
        });
    }

    @Override
    public IPage<UserMessage> list(Page<UserMessage> page, Long receiverId) {
        return userMessageMapper.selectPage(page, new LambdaQueryWrapper<UserMessage>()
                .eq(UserMessage::getReceiverId, receiverId)
                .orderByDesc(UserMessage::getCreateTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long receiverId, Long id) {
        UserMessage m = userMessageMapper.selectById(id);
        if (m == null || !receiverId.equals(m.getReceiverId())) {
            throw new BizException(404, "消息不存在");
        }
        m.setIsRead(true);
        userMessageMapper.updateById(m);
    }

    @Override
    public void markReadAll(Long receiverId) {
        UserMessage m = new UserMessage();
        m.setIsRead(true);
        userMessageMapper.update(m, new LambdaQueryWrapper<UserMessage>()
                .eq(UserMessage::getReceiverId, receiverId)
                .eq(UserMessage::getIsRead, false));
    }
}
