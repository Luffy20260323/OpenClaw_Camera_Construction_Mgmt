package com.qidian.camera.module.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色权限调整实体
 * 对应表：role_permission_adjustment
 * 记录角色权限的增减
 */
@Data
@TableName("role_permission_adjustment")
public class RolePermissionAdjustment {
    
    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 角色 ID
     */
    private Long roleId;
    
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
     * 操作类型常量
     */
    public static final String ACTION_ADD = "ADD";
    public static final String ACTION_REMOVE = "REMOVE";
}