package com.qidian.camera.module.auth.service.impl;

import com.qidian.camera.module.auth.constant.RoleConstants;
import com.qidian.camera.module.auth.entity.UserContext;
import com.qidian.camera.module.auth.entity.PermissionInfo;
import com.qidian.camera.module.auth.service.DataPermissionService;
import com.qidian.camera.module.auth.service.PermissionCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 权限服务实现（基于 JdbcTemplate）
 * 提供权限查询、验证和缓存功能
 * 注意：这是基于 permissions 表的旧实现，与 PermissionService（基于 resource 表）是独立的服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionServiceImpl {
    
    private final JdbcTemplate jdbcTemplate;
    private final PermissionCacheService permissionCacheService;
    private final DataPermissionService dataPermissionService;
    
    private static final ThreadLocal<UserContext> USER_CONTEXT_THREAD_LOCAL = new ThreadLocal<>();
    
    /**
     * 设置当前用户上下文（在认证过滤器中调用）
     */
    public void setCurrentUser(UserContext userContext) {
        USER_CONTEXT_THREAD_LOCAL.set(userContext);
    }
    
    /**
     * 清除当前用户上下文
     */
    public void clearCurrentUser() {
        USER_CONTEXT_THREAD_LOCAL.remove();
    }
    
    /**
     * 获取当前用户上下文
     */
    public UserContext getCurrentUser() {
        UserContext userContext = USER_CONTEXT_THREAD_LOCAL.get();
        if (userContext == null) {
            throw new IllegalStateException("用户未登录");
        }
        return userContext;
    }
    
    /**
     * 检查当前用户是否有指定权限
     */
    public boolean hasPermission(String permissionCode) {
        try {
            UserContext userContext = getCurrentUser();
            return userContext.hasPermission(permissionCode);
        } catch (IllegalStateException e) {
            log.warn("获取当前用户失败", e);
            return false;
        }
    }
    
    /**
     * 检查指定用户是否有指定权限
     */
    public boolean hasPermission(Long userId, String permissionCode) {
        // 系统管理员拥有所有权限
        if (isSystemAdmin(userId)) {
            return true;
        }
        
        List<String> permissions = getUserPermissions(userId);
        return permissions.contains(permissionCode);
    }
    
    /**
     * 获取用户权限列表（带缓存）
     */
    public List<String> getUserPermissions(Long userId) {
        // 1. 先从缓存读取
        java.util.Set<String> cached = permissionCacheService.getUserPermissionsFromCache(userId);
        if (cached != null) {
            log.debug("权限缓存命中：userId={}", userId);
            return new java.util.ArrayList<>(cached);
        }
        
        // 2. 缓存未命中，从数据库查询
        log.debug("权限缓存未命中，查询数据库：userId={}", userId);
        String sql = "SELECT DISTINCT p.permission_code " +
                     "FROM user_roles ur " +
                     "JOIN permission p ON ur.role_id = p.role_id " +
                     "JOIN resource r ON rr.resource_id = r.id " +
                     "WHERE ur.user_id = ? AND r.permission_key IS NOT NULL";
        
        List<String> permissions = jdbcTemplate.queryForList(sql, String.class, userId);
        
        // 3. 写入缓存
        permissionCacheService.cacheUserPermissions(userId, permissions);
        
        return permissions;
    }
    
    /**
     * 获取用户角色列表（带缓存）
     */
    public List<String> getUserRoles(Long userId) {
        // 1. 先从缓存读取
        java.util.Set<String> cached = permissionCacheService.getUserRolesFromCache(userId);
        if (cached != null) {
            return new java.util.ArrayList<>(cached);
        }
        
        // 2. 缓存未命中，从数据库查询
        String sql = "SELECT r.role_code " +
                     "FROM user_roles ur " +
                     "JOIN roles r ON ur.role_id = r.id " +
                     "WHERE ur.user_id = ?";
        
        List<String> roles = jdbcTemplate.queryForList(sql, String.class, userId);
        
        // 3. 写入缓存
        permissionCacheService.cacheUserRoles(userId, roles);
        
        return roles;
    }
    
    /**
     * 获取所有权限列表
     */
    public List<PermissionInfo> getAllPermissions() {
        // 已废弃：从 resource 表查询
        String sql = "SELECT id, code as permission_code, name as permission_name FROM resource WHERE permission_key IS NOT NULL ORDER BY id";
        
        return jdbcTemplate.query(sql, new RowMapper<PermissionInfo>() {
            @Override
            public PermissionInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
                PermissionInfo info = new PermissionInfo();
                info.setId(rs.getLong("id"));
                info.setPermissionCode(rs.getString("permission_code"));
                info.setPermissionName(rs.getString("permission_name"));
                return info;
            }
        });
    }
    
    /**
     * 检查用户是否是超级管理员
     * 通过角色 Code 判断（使用常量，避免分散硬编码）
     */
    public boolean isSuperAdmin(Long userId) {
        String sql = "SELECT COUNT(1) FROM user_roles ur " +
                     "JOIN roles r ON ur.role_id = r.id " +
                     "WHERE ur.user_id = ? AND r.role_code = ?";
        
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, RoleConstants.ROLE_SUPER_ADMIN);
        return count != null && count > 0;
    }
    
    /**
     * 检查用户是否是系统管理员（保留方法，向后兼容）
     * @deprecated 使用 isSuperAdmin() 代替
     */
    @Deprecated
    public boolean isSystemAdmin(Long userId) {
        return isSuperAdmin(userId);
    }
    
    /**
     * 获取用户数据范围类型
     */
    public String getUserDataScopeType(Long userId) {
        return dataPermissionService.getUserDataScopeType(userId);
    }
    
    /**
     * 获取用户数据权限 SQL
     */
    public String getUserDataPermissionSql(Long userId, String entityType) {
        return dataPermissionService.getUserDataPermissionSql(userId, entityType);
    }
}
