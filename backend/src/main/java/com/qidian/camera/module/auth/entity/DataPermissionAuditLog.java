package com.qidian.camera.module.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据权限审计日志实体
 * 对应表：data_permission_audit_log
 * 用于记录数据权限变更
 */
@Data
@TableName("data_permission_audit_log")
public class DataPermissionAuditLog {
    
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
     * 目标类型：USER, ROLE
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
     * 旧范围类型
     */
    private String oldScopeType;
    
    /**
     * 新范围类型
     */
    private String newScopeType;
    
    /**
     * 旧公司 ID
     */
    private Long oldCompanyId;
    
    /**
     * 新公司 ID
     */
    private Long newCompanyId;
    
    /**
     * 旧作业区 ID 列表
     */
    private String oldWorkareaIds;
    
    /**
     * 新作业区 ID 列表
     */
    private String newWorkareaIds;
    
    /**
     * 变更原因
     */
    private String changeReason;
    
    /**
     * IP 地址
     */
    private String ipAddress;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
