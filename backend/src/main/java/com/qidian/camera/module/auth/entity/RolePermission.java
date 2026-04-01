package com.qidian.camera.module.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色-资源关联实体
 * 对应表：role_permissions
 */
@Data
@TableName("role_permissions")
public class RolePermission {
    
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
     * 资源 ID（对应 resource.id）
     */
    private Long resourceId;
    
    /**
     * 权限类型：basic/default
     */
    private String permissionType;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 创建人 ID
     */
    private Long createdBy;
    
    /**
     * 权限类型常量
     */
    public static final String TYPE_BASIC = "basic";
    public static final String TYPE_DEFAULT = "default";
}