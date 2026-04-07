package com.qidian.camera.module.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色 - 资源关联实体
 * 对应表：permission（角色-资源权限关联表）
 * 注意：permission.permission_id 实际存储的是 resource.id
 */
@Data
@TableName("permission")
public class RolePermission {
    
    /**
     * 角色 ID
     */
    @TableField("role_id")
    private Long roleId;
    
    /**
     * 资源 ID（对应 permission.permission_id，实际存储 resource.id）
     * 注意：SQL查询时使用 permission_id as resource_id 映射
     */
    @TableField("resource_id")  // SQL查询结果映射
    private Long resourceId;
    
    /**
     * 授权级别：0=不可传递，1=一级授权
     */
    @TableField("grant_level")
    private Short grantLevel;
    
    /**
     * 是否可转授权
     */
    @TableField("grantable")
    private Boolean grantable;
    
    /**
     * 授权人 ID
     */
    @TableField("granted_by")
    private Long grantedBy;
    
    /**
     * 授权时间
     */
    @TableField("granted_at")
    private LocalDateTime grantedAt;
    
    /**
     * 授权级别常量
     */
    public static final short GRANT_LEVEL_NONE = 0;     // 不可传递
    public static final short GRANT_LEVEL_PRIMARY = 1;  // 一级授权（admin→系统管理员）
}