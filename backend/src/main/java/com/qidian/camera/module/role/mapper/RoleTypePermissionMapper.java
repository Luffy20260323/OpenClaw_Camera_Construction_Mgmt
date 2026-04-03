package com.qidian.camera.module.role.mapper;

import com.qidian.camera.module.role.dto.RoleTypePermissionDTO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 角色类型缺省权限 Mapper
 */
@Mapper
public interface RoleTypePermissionMapper {

    /**
     * 根据角色类型查询缺省权限列表
     */
    @Select("SELECT " +
            "rtp.id, " +
            "rtp.role_type, " +
            "rtp.resource_id, " +
            "rtp.permission_key, " +
            "rtp.sort_order, " +
            "r.name as resource_name, " +
            "r.type as resource_type, " +
            "r.module_code, " +
            "r.permission_key " +
            "FROM role_type_default_permissions rtp " +
            "JOIN resource r ON r.id = rtp.resource_id " +
            "WHERE rtp.role_type = #{roleType} " +
            "ORDER BY rtp.sort_order, rtp.id")
    List<RoleTypePermissionDTO> selectByRoleType(@Param("roleType") String roleType);

    /**
     * 插入缺省权限
     */
    @Insert("INSERT INTO role_type_default_permissions (role_type, resource_id, permission_key, sort_order) " +
            "SELECT #{roleType}, #{resourceId}, r.permission_key, COALESCE(MAX(sort_order), 0) + 1 " +
            "FROM role_type_default_permissions " +
            "WHERE role_type = #{roleType} " +
            "ON CONFLICT (role_type, resource_id) DO NOTHING")
    void insert(@Param("roleType") String roleType, @Param("resourceId") Long resourceId);

    /**
     * 删除缺省权限
     */
    @Delete("DELETE FROM role_type_default_permissions WHERE id = #{id}")
    void deleteById(@Param("id") Long id);

    /**
     * 检查资源是否已存在于某角色类型
     */
    @Select("SELECT COUNT(*) FROM role_type_default_permissions WHERE role_type = #{roleType} AND resource_id = #{resourceId}")
    Long exists(@Param("roleType") String roleType, @Param("resourceId") Long resourceId);
}
