package com.qidian.camera.module.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色缺省权限实体
 * 对应表：role_default_permissions
 * 用于记录角色创建时的默认权限配置
 */
@Data
@TableName("role_default_permissions")
public class RoleDefaultPermission {
    
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
     * 权限 ID
     */
    private Long permissionId;
    
    /**
     * 是否从上级角色继承
     */
    private Boolean isInherited;
    
    /**
     * 继承自哪个角色
     */
    private Long inheritedFromRoleId;
    
    /**
     * 配置人 ID
     */
    private Long configuredBy;
    
    /**
     * 配置时间
     */
    private LocalDateTime configuredAt;
}
