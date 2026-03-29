package com.qidian.camera.module.auth.service.impl;

import com.qidian.camera.module.auth.entity.PermissionAuditLog;
import com.qidian.camera.module.auth.service.PermissionAuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

/**
 * 权限审计日志服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionAuditLogServiceImpl implements PermissionAuditLogService {
    
    private final JdbcTemplate jdbcTemplate;
    
    private static final RowMapper<PermissionAuditLog> ROW_MAPPER = (rs, rowNum) -> {
        PermissionAuditLog log = new PermissionAuditLog();
        log.setId(rs.getLong("id"));
        log.setOperatorId(rs.getLong("operator_id"));
        log.setOperatorName(rs.getString("operator_name"));
        log.setOperationType(rs.getString("operation_type"));
        log.setTargetType(rs.getString("target_type"));
        log.setTargetId(rs.getLong("target_id"));
        log.setTargetName(rs.getString("target_name"));
        log.setPermissionIdsBefore(rs.getString("permission_ids_before"));
        log.setPermissionIdsAfter(rs.getString("permission_ids_after"));
        log.setChangeDescription(rs.getString("change_description"));
        log.setIpAddress(rs.getString("ip_address"));
        log.setUserAgent(rs.getString("user_agent"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        log.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);
        
        return log;
    };
    
    @Override
    public void logPermissionConfig(PermissionAuditLog auditLogEntry) {
        String sql = """
            INSERT INTO permission_audit_log 
            (operator_id, operator_name, operation_type, target_type, target_id, 
             target_name, permission_ids_before, permission_ids_after, 
             change_description, ip_address, user_agent)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        jdbcTemplate.update(sql,
            auditLogEntry.getOperatorId(),
            auditLogEntry.getOperatorName(),
            auditLogEntry.getOperationType(),
            auditLogEntry.getTargetType(),
            auditLogEntry.getTargetId(),
            auditLogEntry.getTargetName(),
            auditLogEntry.getPermissionIdsBefore(),
            auditLogEntry.getPermissionIdsAfter(),
            auditLogEntry.getChangeDescription(),
            auditLogEntry.getIpAddress(),
            auditLogEntry.getUserAgent()
        );
        
        log.info("记录权限配置审计日志：操作人={}, 操作类型={}, 目标={}", 
            auditLogEntry.getOperatorName(), auditLogEntry.getOperationType(), auditLogEntry.getTargetName());
    }
    
    @Override
    public Page<PermissionAuditLog> getAuditLogs(Long targetId, String targetType, 
                                                  Long operatorId, String operationType,
                                                  String operatorName, Pageable pageable) {
        // 动态构建查询条件
        List<Object> params = new java.util.ArrayList<>();
        
        StringBuilder countSql = new StringBuilder(
            "SELECT COUNT(*) FROM permission_audit_log WHERE 1=1"
        );
        StringBuilder querySql = new StringBuilder(
            "SELECT * FROM permission_audit_log WHERE 1=1"
        );
        
        // 添加查询条件
        if (targetId != null) {
            countSql.append(" AND target_id = ?");
            querySql.append(" AND target_id = ?");
            params.add(targetId);
        }
        if (targetType != null && !targetType.isEmpty()) {
            countSql.append(" AND target_type = ?");
            querySql.append(" AND target_type = ?");
            params.add(targetType);
        }
        if (operatorId != null) {
            countSql.append(" AND operator_id = ?");
            querySql.append(" AND operator_id = ?");
            params.add(operatorId);
        }
        if (operationType != null && !operationType.isEmpty()) {
            countSql.append(" AND operation_type = ?");
            querySql.append(" AND operation_type = ?");
            params.add(operationType);
        }
        if (operatorName != null && !operatorName.isEmpty()) {
            countSql.append(" AND operator_name LIKE ?");
            querySql.append(" AND operator_name LIKE ?");
            params.add("%" + operatorName + "%");
        }
        
        querySql.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
        
        // 查询总数
        Long total = jdbcTemplate.queryForObject(countSql.toString(), Long.class, params.toArray());
        
        // 查询数据
        List<Object> queryParams = new java.util.ArrayList<>(params);
        queryParams.add(pageable.getPageSize());
        queryParams.add((long) pageable.getPageNumber() * pageable.getPageSize());
        
        List<PermissionAuditLog> content = jdbcTemplate.query(
            querySql.toString(), ROW_MAPPER, queryParams.toArray());
        
        return new PageImpl<>(content, pageable, total);
    }
    
    @Override
    public PermissionAuditLog getById(Long id) {
        String sql = "SELECT * FROM permission_audit_log WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, ROW_MAPPER, id);
    }
}
