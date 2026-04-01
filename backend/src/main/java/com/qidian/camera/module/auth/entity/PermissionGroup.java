package com.qidian.camera.module.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 权限分组实体
 * 对应表：permission_groups
 * 用于按模块分组管理权限
 */
@Data
@TableName("permission_groups")
public class PermissionGroup {
    
    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 分组代码，如：auth, user, role
     */
    private String groupCode;
    
    /**
     * 分组名称，如：认证模块，用户模块
     */
    private String groupName;
    
    /**
     * 分组描述
     */
    private String groupDescription;
    
    /**
     * 排序顺序
     */
    private Integer sortOrder;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
