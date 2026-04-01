package com.qidian.camera.module.auth.service.impl;

import com.qidian.camera.module.auth.entity.UserContext;
import com.qidian.camera.module.auth.service.DataPermissionService;
import com.qidian.camera.module.auth.service.PermissionCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 数据权限服务实现
 * 实现用户和角色的数据权限查询与管理
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataPermissionServiceImpl implements DataPermissionService {
    
    private final JdbcTemplate jdbcTemplate;
    private final PermissionCacheService permissionCacheService;
    private final RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 缓存过期时间（秒）
     */
    private static final long CACHE_EXPIRE_SECONDS = 7200; // 2 小时
    
    @Override
    public String getUserDataScopeType(Long userId) {
        // 1. 尝试从缓存读取
        String cached = getCachedDataScope(userId);
        if (cached != null) {
            return cached;
        }
        
        // 2. 从数据库查询用户数据权限
        String sql = "SELECT data_scope_type FROM user_data_permissions WHERE user_id = ?";
        try {
            String scopeType = jdbcTemplate.queryForObject(sql, String.class, userId);
            if (scopeType != null) {
                cacheDataScope(userId, scopeType);
                return scopeType;
            }
        } catch (Exception e) {
            log.debug("查询用户数据权限失败，使用角色权限：userId={}", userId, e);
        }
        
        // 3. 如果用户没有数据权限，使用角色数据权限
        return getRoleDataScopeTypeByUserId(userId);
    }
    
    @Override
    public Long getUserDataDeptId(Long userId) {
        String sql = "SELECT dept_id FROM user_data_permissions WHERE user_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, Long.class, userId);
        } catch (Exception e) {
            log.debug("查询用户部门 ID 失败：userId={}", userId, e);
            return null;
        }
    }
    
    @Override
    public String getUserDataDeptIds(Long userId) {
        String sql = "SELECT dept_ids FROM user_data_permissions WHERE user_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, String.class, userId);
        } catch (Exception e) {
            log.debug("查询用户部门 ID 列表失败：userId={}", userId, e);
            return null;
        }
    }
    
    @Override
    public String getUserDataPermissionSql(Long userId, String entityType) {
        String scopeType = getUserDataScopeType(userId);
        
        // 获取用户的部门信息
        Long deptId = getUserDataDeptId(userId);
        String deptIds = getUserDataDeptIds(userId);
        
        return buildPermissionSql(scopeType, deptId, deptIds, userId);
    }
    
    @Override
    public String getRoleDataScopeType(Long roleId) {
        String sql = "SELECT data_scope_type FROM role_data_permissions WHERE role_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, String.class, roleId);
        } catch (Exception e) {
            log.debug("查询角色数据权限失败：roleId={}", roleId, e);
            return "SELF"; // 默认为仅个人数据
        }
    }
    
    @Override
    public Long getRoleDataDeptId(Long roleId) {
        String sql = "SELECT dept_id FROM role_data_permissions WHERE role_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, Long.class, roleId);
        } catch (Exception e) {
            log.debug("查询角色部门 ID 失败：roleId={}", roleId, e);
            return null;
        }
    }
    
    @Override
    public String getRoleDataDeptIds(Long roleId) {
        String sql = "SELECT dept_ids FROM role_data_permissions WHERE role_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, String.class, roleId);
        } catch (Exception e) {
            log.debug("查询角色部门 ID 列表失败：roleId={}", roleId, e);
            return null;
        }
    }
    
    @Override
    @Transactional
    public void setUserDataPermission(Long userId, String scopeType, Long deptId, 
                                      String deptIds, Long operatorId) {
        // 检查是否已存在
        String checkSql = "SELECT id FROM user_data_permissions WHERE user_id = ?";
        List<Long> existing = jdbcTemplate.queryForList(checkSql, Long.class, userId);
        
        if (existing.isEmpty()) {
            // 插入新记录
            String insertSql = """
                INSERT INTO user_data_permissions 
                (user_id, data_scope_type, dept_id, dept_ids, source, granted_by, granted_at, created_at, updated_at)
                VALUES (?, ?, ?, ?, 'MANUAL', ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """;
            jdbcTemplate.update(insertSql, userId, scopeType, deptId, deptIds, operatorId);
            log.info("设置用户数据权限：userId={}, scopeType={}, deptId={}, deptIds={}", 
                    userId, scopeType, deptId, deptIds);
        } else {
            // 更新现有记录
            String updateSql = """
                UPDATE user_data_permissions 
                SET data_scope_type = ?, dept_id = ?, dept_ids = ?, 
                    granted_by = ?, granted_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP
                WHERE user_id = ?
            """;
            jdbcTemplate.update(updateSql, scopeType, deptId, deptIds, operatorId, userId);
            log.info("更新用户数据权限：userId={}, scopeType={}, deptId={}, deptIds={}", 
                    userId, scopeType, deptId, deptIds);
        }
        
        // 清除缓存
        refreshUserCache(userId);
    }
    
    @Override
    @Transactional
    public void setRoleDataPermission(Long roleId, String scopeType, Long deptId, String deptIds) {
        // 检查是否已存在
        String checkSql = "SELECT id FROM role_data_permissions WHERE role_id = ?";
        List<Long> existing = jdbcTemplate.queryForList(checkSql, Long.class, roleId);
        
        if (existing.isEmpty()) {
            // 插入新记录
            String insertSql = """
                INSERT INTO role_data_permissions 
                (role_id, data_scope_type, dept_id, dept_ids, created_at)
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)
            """;
            jdbcTemplate.update(insertSql, roleId, scopeType, deptId, deptIds);
            log.info("设置角色数据权限：roleId={}, scopeType={}, deptId={}, deptIds={}", 
                    roleId, scopeType, deptId, deptIds);
        } else {
            // 更新现有记录
            String updateSql = """
                UPDATE role_data_permissions 
                SET data_scope_type = ?, dept_id = ?, dept_ids = ?
                WHERE role_id = ?
            """;
            jdbcTemplate.update(updateSql, scopeType, deptId, deptIds, roleId);
            log.info("更新角色数据权限：roleId={}, scopeType={}, deptId={}, deptIds={}", 
                    roleId, scopeType, deptId, deptIds);
        }
    }
    
    @Override
    public void refreshUserCache(Long userId) {
        // 清除数据权限缓存
        String cacheKey = "data:permission:user:" + userId;
        try {
            redisTemplate.delete(cacheKey);
            log.debug("清除用户数据权限缓存：userId={}", userId);
        } catch (Exception e) {
            log.debug("清除数据权限缓存失败：userId={}", userId, e);
        }
        
        // 清除权限缓存
        permissionCacheService.evictUserPermissions(userId);
    }
    
    @Override
    public String buildWhereClause(UserContext userContext, String scopeType, String tableAlias) {
        if (userContext == null) {
            return "1=0";
        }
        
        String alias = (tableAlias != null && !tableAlias.isEmpty()) ? tableAlias + "." : "";
        
        return switch (scopeType) {
            case "ALL" -> "1=1";
            case "DEPT" -> {
                Long deptId = userContext.getDeptId();
                if (deptId == null) {
                    yield "1=0";
                }
                yield alias + "dept_id = " + deptId;
            }
            case "DEPT_AND_SUB" -> {
                String deptPath = userContext.getDeptPath();
                if (deptPath == null || deptPath.isEmpty()) {
                    yield "1=0";
                }
                yield alias + "dept_path LIKE '" + deptPath + "%'";
            }
            case "SELF" -> alias + "created_by = " + userContext.getUserId();
            default -> "1=1";
        };
    }
    
    // ==================== 私有辅助方法 ====================
    
    /**
     * 获取角色数据范围类型（通过用户 ID）
     */
    private String getRoleDataScopeTypeByUserId(Long userId) {
        String sql = """
            SELECT rdp.data_scope_type 
            FROM role_data_permissions rdp
            JOIN user_roles ur ON rdp.role_id = ur.role_id
            WHERE ur.user_id = ? AND ur.is_primary = TRUE
            LIMIT 1
        """;
        try {
            return jdbcTemplate.queryForObject(sql, String.class, userId);
        } catch (Exception e) {
            log.debug("查询角色数据范围失败：userId={}", userId, e);
            return "SELF"; // 默认为仅个人数据
        }
    }
    
    /**
     * 构建权限 SQL 条件
     */
    private String buildPermissionSql(String scopeType, Long deptId, String deptIds, Long userId) {
        return switch (scopeType) {
            case "ALL" -> "1=1";
            case "DEPT" -> {
                if (deptId == null) {
                    yield "1=0";
                }
                yield "dept_id = " + deptId;
            }
            case "DEPT_AND_SUB" -> {
                if (deptIds == null || deptIds.isEmpty()) {
                    yield "1=0";
                }
                yield "dept_id IN (" + deptIds + ")";
            }
            case "SELF" -> "created_by = " + userId;
            default -> "1=1";
        };
    }
    
    /**
     * 从缓存获取数据范围
     */
    @SuppressWarnings("unchecked")
    private String getCachedDataScope(Long userId) {
        String cacheKey = "data:permission:user:" + userId;
        try {
            return (String) redisTemplate.opsForValue().get(cacheKey);
        } catch (Exception e) {
            log.debug("从缓存读取数据范围失败：userId={}", userId, e);
            return null;
        }
    }
    
    /**
     * 缓存数据范围
     */
    private void cacheDataScope(Long userId, String scopeType) {
        String cacheKey = "data:permission:user:" + userId;
        try {
            redisTemplate.opsForValue().set(cacheKey, scopeType, CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
            log.debug("缓存用户数据范围：userId={}, scopeType={}", userId, scopeType);
        } catch (Exception e) {
            log.debug("缓存数据范围失败：userId={}", userId, e);
        }
    }
}
