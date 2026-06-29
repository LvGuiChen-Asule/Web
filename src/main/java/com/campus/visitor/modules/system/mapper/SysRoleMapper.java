package com.campus.visitor.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.visitor.modules.system.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

import com.campus.visitor.modules.system.dto.admin.UserRolePair;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
    List<SysRole> selectRolesByUserId(@Param("userId") Long userId);

    List<UserRolePair> selectRoleCodesByUserIds(@Param("userIds") List<Long> userIds);

    List<SysRole> selectByRoleCodes(@Param("roleCodes") List<String> roleCodes);
}
