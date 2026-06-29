package com.campus.visitor.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.visitor.modules.system.entity.UserMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMessageMapper extends BaseMapper<UserMessage> {
}
