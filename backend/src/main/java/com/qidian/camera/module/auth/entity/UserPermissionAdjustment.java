package com.qidian.camera.module.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户权限调整实体
 * 对应表：user_permission_adjustment
 * 用于记录用户级别的权限调整
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
     * 生效时间
     */
    private LocalDateTime effectiveTime;
    
    /**
     * 过期时间
     */
    private LocalDateTime expireTime;
    
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
