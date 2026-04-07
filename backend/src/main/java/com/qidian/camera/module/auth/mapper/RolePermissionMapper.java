package com.qidian.camera.module.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qidian.camera.module.auth.entity.RolePermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Delete;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色 - 资源关联 Mapper
 * 对应表：permission
 */
@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {
    
    /**
     * 查询角色的所有权限
     */
    @Select("SELECT role_id, permission_id as resource_id, grant_level, grantable, granted_by, granted_at " +
            "FROM permission WHERE role_id = #{roleId}")
    List<RolePermission> findByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 查询角色的基本权限（所有权限都是基本权限，permission表没有类型区分）
     */
    @Select("SELECT role_id, permission_id as resource_id, grant_level, grantable, granted_by, granted_at " +
            "FROM permission WHERE role_id = #{roleId}")
    List<RolePermission> findBasicByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 查询角色的缺省权限（与basic相同，permission表没有类型区分）
     */
    @Select("SELECT role_id, permission_id as resource_id, grant_level, grantable, granted_by, granted_at " +
            "FROM permission WHERE role_id = #{roleId}")
    List<RolePermission> findDefaultByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 查询角色是否有某个资源的权限
     */
    @Select("SELECT COUNT(*) > 0 FROM permission WHERE role_id = #{roleId} AND permission_id = #{resourceId}")
    boolean hasPermission(@Param("roleId") Long roleId, @Param("resourceId") Long resourceId);
    
    /**
     * 给角色添加权限
     */
    @Insert("INSERT INTO permission (role_id, permission_id, grant_level, grantable, granted_by, granted_at) " +
            "VALUES (#{roleId}, #{resourceId}, #{grantLevel}, #{grantable}, #{grantedBy}, #{grantedAt}) " +
            "ON CONFLICT (role_id, permission_id) DO UPDATE SET " +
            "grant_level = #{grantLevel}, grantable = #{grantable}, granted_by = #{grantedBy}, granted_at = #{grantedAt}")
    int insertPermission(@Param("roleId") Long roleId, 
                         @Param("resourceId") Long resourceId,
                         @Param("grantLevel") Short grantLevel,
                         @Param("grantable") Boolean grantable,
                         @Param("grantedBy") Long grantedBy,
                         @Param("grantedAt") LocalDateTime grantedAt);
    
    /**
     * 删除角色的权限
     */
    @Delete("DELETE FROM permission WHERE role_id = #{roleId} AND permission_id = #{resourceId}")
    int deletePermission(@Param("roleId") Long roleId, @Param("resourceId") Long resourceId);
    
    /**
     * 删除角色的所有权限
     */
    @Delete("DELETE FROM permission WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 统计角色权限数量
     */
    @Select("SELECT COUNT(*) FROM permission WHERE role_id = #{roleId}")
    int countByRoleId(@Param("roleId") Long roleId);
}