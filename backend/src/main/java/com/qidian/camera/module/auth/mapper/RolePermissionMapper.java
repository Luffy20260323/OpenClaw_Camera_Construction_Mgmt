package com.qidian.camera.module.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qidian.camera.module.auth.entity.RolePermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色-资源关联 Mapper
 */
@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {
    
    /**
     * 查询角色的所有权限
     */
    @Select("SELECT * FROM role_permissions WHERE role_id = #{roleId}")
    List<RolePermission> findByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 查询角色的基本权限
     */
    @Select("SELECT * FROM role_permissions WHERE role_id = #{roleId} AND permission_type = 'basic'")
    List<RolePermission> findBasicByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 查询角色的缺省权限
     */
    @Select("SELECT * FROM role_permissions WHERE role_id = #{roleId} AND permission_type = 'default'")
    List<RolePermission> findDefaultByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 查询资源被哪些角色使用
     */
    @Select("SELECT role_id FROM role_permissions WHERE resource_id = #{resourceId}")
    List<Long> findRoleIdsByResourceId(@Param("resourceId") Long resourceId);
    
    /**
     * 检查角色是否拥有指定资源权限
     */
    @Select("SELECT COUNT(*) > 0 FROM role_permissions WHERE role_id = #{roleId} AND resource_id = #{resourceId}")
    boolean hasPermission(@Param("roleId") Long roleId, @Param("resourceId") Long resourceId);
}