package com.campus.visitor.modules.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.visitor.modules.system.entity.UserMessage;

public interface MessageService {
    void send(Long receiverId, String type, String title, String content);

    void sendToAdmin(String type, String title, String content);

    IPage<UserMessage> list(Page<UserMessage> page, Long receiverId);

    void markRead(Long receiverId, Long id);

    void markReadAll(Long receiverId);
}
