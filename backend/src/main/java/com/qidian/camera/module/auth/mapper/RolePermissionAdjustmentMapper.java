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
     * 查询角色的所有权限调整
     */
    @Select("SELECT * FROM role_permission_adjustment WHERE role_id = #{roleId}")
    List<RolePermissionAdjustment> findByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 查询角色增加的权限
     */
    @Select("SELECT * FROM role_permission_adjustment WHERE role_id = #{roleId} AND action = 'ADD'")
    List<RolePermissionAdjustment> findAddByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 查询角色移除的权限
     */
    @Select("SELECT * FROM role_permission_adjustment WHERE role_id = #{roleId} AND action = 'REMOVE'")
    List<RolePermissionAdjustment> findRemoveByRoleId(@Param("roleId") Long roleId);
}