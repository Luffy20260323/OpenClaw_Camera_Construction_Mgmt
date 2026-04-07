package com.qidian.camera.module.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qidian.camera.module.auth.entity.UserPermissionAdjustment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户权限调整 Mapper
 */
@Mapper
public interface UserPermissionAdjustmentMapper extends BaseMapper<UserPermissionAdjustment> {
    
    /**
     * 根据用户 ID 查询调整记录
     */
    @Select("SELECT * FROM user_permission_adjustment WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<UserPermissionAdjustment> findByUserId(@Param("userId") Long userId);
    
    /**
     * 根据用户 ID 和资源 ID 查询调整记录
     */
    @Select("SELECT * FROM user_permission_adjustment WHERE user_id = #{userId} AND resource_id = #{resourceId} ORDER BY created_at DESC")
    List<UserPermissionAdjustment> findByUserIdAndResourceId(@Param("userId") Long userId, @Param("resourceId") Long resourceId);
    
    /**
     * 查询用户的有效调整记录（在有效期内）
     */
    @Select("SELECT * FROM user_permission_adjustment WHERE user_id = #{userId} " +
            "AND (expire_at IS NULL OR expire_at >= NOW()) " +
            "ORDER BY created_at DESC")
    List<UserPermissionAdjustment> findValidByUserId(@Param("userId") Long userId);
    
    /**
     * 查询用户的增加权限调整
     */
    @Select("SELECT * FROM user_permission_adjustment WHERE user_id = #{userId} AND action = 'ADD' ORDER BY created_at DESC")
    List<UserPermissionAdjustment> findAddActionsByUserId(@Param("userId") Long userId);
    
    /**
     * 查询用户的移除权限调整
     */
    @Select("SELECT * FROM user_permission_adjustment WHERE user_id = #{userId} AND action = 'REMOVE' ORDER BY created_at DESC")
    List<UserPermissionAdjustment> findRemoveActionsByUserId(@Param("userId") Long userId);
    
    /**
     * 删除用户的调整记录
     */
    @Select("DELETE FROM user_permission_adjustment WHERE user_id = #{userId} AND resource_id = #{resourceId}")
    void deleteByUserIdAndResourceId(@Param("userId") Long userId, @Param("resourceId") Long resourceId);
}