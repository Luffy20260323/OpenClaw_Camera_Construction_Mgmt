package com.qidian.camera.module.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qidian.camera.module.role.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色 Mapper
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 根据用户 ID 查询角色列表
     *
     * @param userId 用户 ID
     * @return 角色列表
     */
    @Select("SELECT r.* FROM roles r " +
            "INNER JOIN user_roles ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<Role> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据用户 ID 查询权限列表（从 resource 表）
     *
     * @param userId 用户 ID
     * @return 权限列表
     */
    @Select("SELECT DISTINCT r.permission_key FROM resource r " +
            "INNER JOIN role_permissions rp ON r.id = rp.permission_id " +
            "INNER JOIN user_roles ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.type IN ('PERMISSION', 'MODULE', 'MENU', 'PAGE', 'ELEMENT', 'API')")
    List<String> selectPermissionsByUserId(@Param("userId") Long userId);
    
    /**
     * 统计角色关联的用户数
     */
    @Select("SELECT COUNT(*) FROM user_roles WHERE role_id = #{roleId}")
    Long countUsersByRoleId(@Param("roleId") Long roleId);
}
