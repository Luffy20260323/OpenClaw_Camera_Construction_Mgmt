package com.qidian.camera.module.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户-角色关联实体
 * 对应表：user_roles
 */
@Data
@TableName("user_roles")
public class UserRole {
    
    /**
     * 用户 ID
     */
    @TableId
    private Long userId;
    
    /**
     * 角色 ID
     */
    private Long roleId;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    /**
     * 创建人 ID
     */
    private Long createdBy;
}