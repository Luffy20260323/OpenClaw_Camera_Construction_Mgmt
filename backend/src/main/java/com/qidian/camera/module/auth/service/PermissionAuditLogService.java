package com.qidian.camera.module.auth.service;

import com.qidian.camera.module.auth.entity.PermissionAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 权限审计日志服务
 */
public interface PermissionAuditLogService {
    
    /**
     * 记录权限配置日志
     */
    void logPermissionConfig(PermissionAuditLog log);
    
    /**
     * 查询审计日志
     */
    Page<PermissionAuditLog> getAuditLogs(Long targetId, String targetType, 
                                          Long operatorId, String operationType,
                                          String operatorName, Pageable pageable);
    
    /**
     * 根据 ID 查询日志
     */
    PermissionAuditLog getById(Long id);
}
