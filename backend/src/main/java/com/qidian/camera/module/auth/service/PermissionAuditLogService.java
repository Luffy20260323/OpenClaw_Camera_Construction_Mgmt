package com.qidian.camera.module.auth.service;

import com.qidian.camera.module.auth.entity.PermissionAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

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
    
    /**
     * 查询权限审计日志列表（多条件查询）
     */
    List<PermissionAuditLog> queryLogs(String operationType, String targetType, Long targetId,
                                       Long operatorId, String operatorName,
                                       LocalDateTime startTime, LocalDateTime endTime,
                                       Integer pageNum, Integer pageSize);
    
    /**
     * 根据资源 ID 查询审计日志
     */
    List<PermissionAuditLog> queryByResourceId(Long resourceId, Integer pageNum, Integer pageSize);
    
    /**
     * 根据角色 ID 查询审计日志
     */
    List<PermissionAuditLog> queryByRoleId(Long roleId, Integer pageNum, Integer pageSize);
    
    /**
     * 根据用户 ID 查询审计日志
     */
    List<PermissionAuditLog> queryByUserId(Long userId, Integer pageNum, Integer pageSize);
    
    /**
     * 获取操作类型列表
     */
    List<String> getOperationTypes();
    
    /**
     * 获取目标类型列表
     */
    List<String> getTargetTypes();
}
