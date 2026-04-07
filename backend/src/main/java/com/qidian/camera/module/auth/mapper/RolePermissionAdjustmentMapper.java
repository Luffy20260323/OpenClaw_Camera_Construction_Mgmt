package com.qidian.camera.module.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qidian.camera.module.auth.entity.RolePermissionAdjustment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色权限调整 Mapper
 */
@Mapper
public interface RolePermissionAdjustmentMapper extends BaseMapper<RolePermissionAdjustment> {
    
    /**
     * 根据角色 ID 查询调整记录
     */
    @Select("SELECT * FROM role_permission_adjustment WHERE role_id = #{roleId} ORDER BY created_at DESC")
    List<RolePermissionAdjustment> findByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 根据角色 ID 和资源 ID 查询调整记录
     */
    @Select("SELECT * FROM role_permission_adjustment WHERE role_id = #{roleId} AND resource_id = #{resourceId} ORDER BY created_at DESC")
    List<RolePermissionAdjustment> findByRoleIdAndResourceId(@Param("roleId") Long roleId, @Param("resourceId") Long resourceId);
    
    /**
     * 查询角色的增加权限调整
     */
    @Select("SELECT * FROM role_permission_adjustment WHERE role_id = #{roleId} AND action = 'ADD' ORDER BY created_at DESC")
    List<RolePermissionAdjustment> findAddActionsByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 查询角色的移除权限调整
     */
    @Select("SELECT * FROM role_permission_adjustment WHERE role_id = #{roleId} AND action = 'REMOVE' ORDER BY created_at DESC")
    List<RolePermissionAdjustment> findRemoveActionsByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 删除角色的调整记录
     */
    @Select("DELETE FROM role_permission_adjustment WHERE role_id = #{roleId} AND resource_id = #{resourceId}")
    void deleteByRoleIdAndResourceId(@Param("roleId") Long roleId, @Param("resourceId") Long resourceId);
}