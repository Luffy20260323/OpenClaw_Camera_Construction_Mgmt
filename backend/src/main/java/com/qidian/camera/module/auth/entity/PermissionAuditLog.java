package com.qidian.camera.module.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 权限配置审计日志实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionAuditLog {
    
    /**
     * 日志 ID
     */
    private Long id;
    
    /**
     * 操作人 ID
     */
    private Long operatorId;
    
    /**
     * 操作人姓名
     */
    private String operatorName;
    
    /**
     * 操作类型
     * CONFIG_ROLE_PERMISSION - 配置角色权限
     * ROLLBACK_ROLE_PERMISSION - 回滚权限配置
     * GRANT_USER_PERMISSION - 授予用户权限
     * REVOKE_USER_PERMISSION - 撤销用户权限
     */
    private String operationType;
    
    /**
     * 目标类型
     * ROLE - 角色
     * USER - 用户
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
     * 修改前的权限 ID 列表（JSON）
     */
    private String permissionIdsBefore;
    
    /**
     * 修改后的权限 ID 列表（JSON）
     */
    private String permissionIdsAfter;
    
    /**
     * 变更描述
     */
    private String changeDescription;
    
    /**
     * 操作 IP
     */
    private String ipAddress;
    
    /**
     * User-Agent
     */
    private String userAgent;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
