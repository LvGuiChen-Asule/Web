package com.campus.visitor.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.visitor.modules.system.entity.SysUser;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    @Insert("INSERT INTO sys_user_role(user_id, role_id) VALUES(#{userId}, #{roleId})")
    int saveUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    @Select("""
            SELECT DISTINCT u.*
            FROM sys_user u
            JOIN sys_user_role ur ON u.id = ur.user_id
            JOIN sys_role r ON ur.role_id = r.id
            WHERE u.status = 1
              AND r.role_code = 'ROLE_HOST'
              AND (
                    u.real_name LIKE CONCAT('%', #{keyword}, '%')
                 OR u.phone LIKE CONCAT('%', #{keyword}, '%')
              )
            """)
    java.util.List<SysUser> selectHostsByKeyword(@Param("keyword") String keyword);

    @Select("""
            SELECT ur.user_id
            FROM sys_user_role ur
            JOIN sys_role r ON ur.role_id = r.id
            WHERE r.role_code = #{roleCode}
            """)
    java.util.List<Long> selectUserIdsByRoleCode(@Param("roleCode") String roleCode);

    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    int deleteUserRolesByUserId(@Param("userId") Long userId);

    @Select("""
            SELECT DISTINCT u.*
            FROM sys_user u
            JOIN sys_user_role ur ON u.id = ur.user_id
            JOIN sys_role r ON ur.role_id = r.id
            WHERE u.status = 1
              AND r.role_code = 'ROLE_ADMIN'
            """)
    java.util.List<SysUser> selectAdmins();
}
