package com.qidian.camera.module.auth.service.impl;

import com.qidian.camera.module.auth.entity.PermissionAuditLog;
import com.qidian.camera.module.auth.mapper.PermissionAuditLogMapper;
import com.qidian.camera.module.auth.service.PermissionAuditLogService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 权限审计日志服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionAuditLogServiceImpl extends ServiceImpl<PermissionAuditLogMapper, PermissionAuditLog> 
    implements PermissionAuditLogService {
    
    @Override
    public void logPermissionConfig(PermissionAuditLog logEntity) {
        save(logEntity);
        log.debug("记录权限配置日志：operationType={}, targetType={}, targetId={}", 
                logEntity.getOperationType(), logEntity.getTargetType(), logEntity.getTargetId());
    }
    
    @Override
    public org.springframework.data.domain.Page<PermissionAuditLog> getAuditLogs(Long targetId, String targetType, 
                                                  Long operatorId, String operationType,
                                                  String operatorName, Pageable pageable) {
        LambdaQueryWrapper<PermissionAuditLog> wrapper = new LambdaQueryWrapper<>();
        
        if (targetId != null) {
            wrapper.eq(PermissionAuditLog::getTargetId, targetId);
        }
        if (targetType != null && !targetType.isEmpty()) {
            wrapper.eq(PermissionAuditLog::getTargetType, targetType);
        }
        if (operatorId != null) {
            wrapper.eq(PermissionAuditLog::getOperatorId, operatorId);
        }
        if (operationType != null && !operationType.isEmpty()) {
            wrapper.eq(PermissionAuditLog::getOperationType, operationType);
        }
        if (operatorName != null && !operatorName.isEmpty()) {
            wrapper.eq(PermissionAuditLog::getOperatorName, operatorName);
        }
        
        wrapper.orderByDesc(PermissionAuditLog::getCreatedAt);
        
        // 查询数据
        Page<PermissionAuditLog> mpPage = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
        Page<PermissionAuditLog> result = page(mpPage, wrapper);
        
        // 转换为 Spring Data 的 Page
        return new PageImpl<>(result.getRecords(), pageable, result.getTotal());
    }
    
    @Override
    public PermissionAuditLog getById(Long id) {
        return super.getById(id);
    }
    
    @Override
    public List<PermissionAuditLog> queryLogs(String operationType, String targetType, Long targetId,
                                              Long operatorId, String operatorName,
                                              LocalDateTime startTime, LocalDateTime endTime,
                                              Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<PermissionAuditLog> wrapper = new LambdaQueryWrapper<>();
        
        if (operationType != null && !operationType.isEmpty()) {
            wrapper.eq(PermissionAuditLog::getOperationType, operationType);
        }
        if (targetType != null && !targetType.isEmpty()) {
            wrapper.eq(PermissionAuditLog::getTargetType, targetType);
        }
        if (targetId != null) {
            wrapper.eq(PermissionAuditLog::getTargetId, targetId);
        }
        if (operatorId != null) {
            wrapper.eq(PermissionAuditLog::getOperatorId, operatorId);
        }
        if (operatorName != null && !operatorName.isEmpty()) {
            wrapper.eq(PermissionAuditLog::getOperatorName, operatorName);
        }
        if (startTime != null) {
            wrapper.ge(PermissionAuditLog::getCreatedAt, startTime);
        }
        if (endTime != null) {
            wrapper.le(PermissionAuditLog::getCreatedAt, endTime);
        }
        
        wrapper.orderByDesc(PermissionAuditLog::getCreatedAt);
        
        Page<PermissionAuditLog> mpPage = new Page<>(pageNum, pageSize);
        Page<PermissionAuditLog> result = page(mpPage, wrapper);
        
        return result.getRecords();
    }
    
    @Override
    public List<PermissionAuditLog> queryByResourceId(Long resourceId, Integer pageNum, Integer pageSize) {
        return queryLogs(null, "RESOURCE", resourceId, null, null, null, null, pageNum, pageSize);
    }
    
    @Override
    public List<PermissionAuditLog> queryByRoleId(Long roleId, Integer pageNum, Integer pageSize) {
        return queryLogs(null, "ROLE", roleId, null, null, null, null, pageNum, pageSize);
    }
    
    @Override
    public List<PermissionAuditLog> queryByUserId(Long userId, Integer pageNum, Integer pageSize) {
        return queryLogs(null, "USER", userId, null, null, null, null, pageNum, pageSize);
    }
    
    @Override
    public List<String> getOperationTypes() {
        return Arrays.asList(
            "CONFIG_ROLE_PERMISSION",
            "ROLLBACK_ROLE_PERMISSION",
            "GRANT_USER_PERMISSION",
            "REVOKE_USER_PERMISSION",
            "CONFIG_DATA_SCOPE",
            "UPDATE_RESOURCE_PARENT",
            "CREATE_RESOURCE",
            "DELETE_RESOURCE"
        );
    }
    
    @Override
    public List<String> getTargetTypes() {
        return Arrays.asList("ROLE", "USER", "RESOURCE");
    }
    
    /**
     * 记录权限变更日志
     */
    public void logPermissionChange(Long operatorId, String operatorName, String operationType,
                                    String targetType, Long targetId, String targetName,
                                    String permissionIdsBefore, String permissionIdsAfter,
                                    String changeDescription, String ipAddress, String userAgent) {
        PermissionAuditLog logEntity = PermissionAuditLog.builder()
            .operatorId(operatorId)
            .operatorName(operatorName)
            .operationType(operationType)
            .targetType(targetType)
            .targetId(targetId)
            .targetName(targetName)
            .permissionIdsBefore(permissionIdsBefore)
            .permissionIdsAfter(permissionIdsAfter)
            .changeDescription(changeDescription)
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .build();
        save(logEntity);
        log.debug("记录权限变更日志：targetType={}, targetId={}, operationType={}", 
                targetType, targetId, operationType);
    }
}
