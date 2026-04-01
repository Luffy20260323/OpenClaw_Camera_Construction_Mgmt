package com.qidian.camera.module.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 权限快照实体
 * 对应表：permission_snapshots
 * 用于记录权限配置的历史状态，支持回滚
 */
@Data
@TableName("permission_snapshots")
public class PermissionSnapshot {
    
    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 快照名称
     */
    private String snapshotName;
    
    /**
     * 快照描述
     */
    private String snapshotDescription;
    
    /**
     * 快照类型：ROLE, USER, SYSTEM
     */
    private String snapshotType;
    
    /**
     * 目标类型：ROLE, USER
     */
    private String targetType;
    
    /**
     * 目标 ID（角色 ID 或用户 ID）
     */
    private Long targetId;
    
    /**
     * 快照数据（JSON 格式）
     */
    private String snapshotData;
    
    /**
     * 创建人 ID
     */
    private Long createdBy;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 是否可以回滚
     */
    private Boolean canRollback;
    
    /**
     * 是否已回滚到此快照
     */
    private Boolean rolledBack;
    
    /**
     * 回滚时间
     */
    private LocalDateTime rolledBackAt;
    
    /**
     * 回滚人 ID
     */
    private Long rolledBackBy;
}
