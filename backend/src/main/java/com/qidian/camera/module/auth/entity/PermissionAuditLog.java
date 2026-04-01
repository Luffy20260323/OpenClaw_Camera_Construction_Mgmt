package com.qidian.camera.module.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 权限审计日志实体
 * 对应表：permission_audit_log
 * 用于记录所有权限变更
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("permission_audit_log")
public class PermissionAuditLog {
    
    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 操作人 ID
     */
    private Long operatorId;
    
    /**
     * 操作人名称
     */
    private String operatorName;
    
    /**
     * 操作类型：CREATE, UPDATE, DELETE, GRANT, REVOKE
     */
    private String operationType;
    
    /**
     * 目标类型：ROLE, USER, PERMISSION
     */
    private String targetType;
    
    /**
     * 目标 ID
     */
    private Long targetId;
    
    /**
     * 目标名称
     */
    private String targetName;
    
    /**
     * 变更前的权限 ID 列表
     */
    private String permissionIdsBefore;
    
    /**
     * 变更后的权限 ID 列表
     */
    private String permissionIdsAfter;
    
    /**
     * 变更描述
     */
    private String changeDescription;
    
    /**
     * IP 地址
     */
    private String ipAddress;
    
    /**
     * 用户代理
     */
    private String userAgent;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}