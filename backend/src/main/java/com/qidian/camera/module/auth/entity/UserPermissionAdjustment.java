package com.qidian.camera.module.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户权限调整实体
 * 对应表：user_permission_adjustment
 * 记录用户个人的权限增减
 */
@Data
@TableName("user_permission_adjustment")
public class UserPermissionAdjustment {
    
    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户 ID
     */
    private Long userId;
    
    /**
     * 资源 ID
     */
    private Long resourceId;
    
    /**
     * 操作类型：ADD/REMOVE
     */
    private String action;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 操作人 ID
     */
    private Long createdBy;
    
    /**
     * 过期时间（临时权限）
     */
    private LocalDateTime expireAt;
    
    /**
     * 操作类型常量
     */
    public static final String ACTION_ADD = "ADD";
    public static final String ACTION_REMOVE = "REMOVE";
}