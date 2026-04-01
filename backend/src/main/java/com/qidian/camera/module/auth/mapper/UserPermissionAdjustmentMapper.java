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
     * 查询用户的所有权限调整（包括未过期的）
     */
    @Select("SELECT * FROM user_permission_adjustment WHERE user_id = #{userId} AND (expire_at IS NULL OR expire_at > NOW())")
    List<UserPermissionAdjustment> findByUserId(@Param("userId") Long userId);
    
    /**
     * 查询用户增加的权限
     */
    @Select("SELECT * FROM user_permission_adjustment WHERE user_id = #{userId} AND action = 'ADD' AND (expire_at IS NULL OR expire_at > NOW())")
    List<UserPermissionAdjustment> findAddByUserId(@Param("userId") Long userId);
    
    /**
     * 查询用户移除的权限
     */
    @Select("SELECT * FROM user_permission_adjustment WHERE user_id = #{userId} AND action = 'REMOVE' AND (expire_at IS NULL OR expire_at > NOW())")
    List<UserPermissionAdjustment> findRemoveByUserId(@Param("userId") Long userId);
    
    /**
     * 查询用户的临时权限（有过期时间）
     */
    @Select("SELECT * FROM user_permission_adjustment WHERE user_id = #{userId} AND expire_at IS NOT NULL AND expire_at > NOW()")
    List<UserPermissionAdjustment> findTemporaryByUserId(@Param("userId") Long userId);
}