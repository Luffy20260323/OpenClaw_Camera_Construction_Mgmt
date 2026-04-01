package com.qidian.camera.module.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 权限配置权实体
 * 对应表：permission_config_rights
 * 用于控制用户可以配置哪些权限
 */
@Data
@TableName("permission_config_rights")
public class PermissionConfigRight {
    
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
     * 是否可以配置基本权限集合
     */
    private Boolean canConfigBase;
    
    /**
     * 是否可以配置缺省权限集合
     */
    private Boolean canConfigDefault;
    
    /**
     * 是否可以恢复缺省权限
     */
    private Boolean canResetDefault;
    
    /**
     * 是否可以调整用户权限
     */
    private Boolean canAdjustUser;
    
    /**
     * 是否可以管理角色权限
     */
    private Boolean canManageRoles;
    
    /**
     * 授权人 ID
     */
    private Long grantedBy;
    
    /**
     * 授权时间
     */
    private LocalDateTime grantedAt;
    
    /**
     * 授权过期时间（NULL 为永久）
     */
    private LocalDateTime expiresAt;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
