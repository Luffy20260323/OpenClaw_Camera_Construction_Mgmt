package com.qidian.camera.module.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色权限调整实体
 * 对应表：role_permission_adjustment
 * 用于记录角色级别的权限调整
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
     * 调整动作：ADD(增加)/REMOVE(移除)
     */
    private String action;
    
    /**
     * 调整原因/备注
     */
    private String reason;
    
    /**
     * 操作人 ID
     */
    private Long operatorId;
    
    /**
     * 状态：1=生效，0=失效
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 调整动作常量
     */
    public static final String ACTION_ADD = "ADD";
    public static final String ACTION_REMOVE = "REMOVE";
}
