package com.qidian.camera.module.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * 授权管理服务
 * 实现分级授权和二次授权限制
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GrantAuthorityService {
    
    private final JdbcTemplate jdbcTemplate;
    
    /**
     * 授权级别常量
     */
    public static final short GRANT_LEVEL_NONE = 0;      // 不可传递
    public static final short GRANT_LEVEL_PRIMARY = 1;   // 一级授权（admin→系统管理员）
    
    /**
     * 检查用户是否是 admin（超级管理员）
     */
    public boolean isAdmin(Long userId) {
        String sql = "SELECT username FROM users WHERE id = ?";
        String username = jdbcTemplate.queryForObject(sql, String.class, userId);
        return "admin".equals(username);
    }
    
    /**
     * 检查用户是否是系统管理员（company_type_id = 4）
     */
    public boolean isSystemAdminUser(Long userId) {
        // 方法1：通过用户角色检查
        String sql = """
            SELECT COUNT(1) FROM user_roles ur
            JOIN roles r ON ur.role_id = r.id
            JOIN company_types ct ON r.company_type_id = ct.id
            WHERE ur.user_id = ? AND ct.id = 4
            """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null && count > 0;
    }
    
    /**
     * 检查角色是否是系统管理员类型角色
     */
    public boolean isSystemAdminRole(Long roleId) {
        String sql = "SELECT company_type_id FROM roles WHERE id = ?";
        Integer companyTypeId = jdbcTemplate.queryForObject(sql, Integer.class, roleId);
        return companyTypeId != null && companyTypeId == 4;
    }
    
    /**
     * 检查用户是否可以授权给目标角色
     * 规则：
     * 1. admin 可以授权给系统管理员角色（company_type_id = 4）
     * 2. 其他用户不能授权
     */
    public boolean canGrantToRole(Long operatorId, Long targetRoleId) {
        // 只有 admin 可以授权
        if (!isAdmin(operatorId)) {
            log.warn("非 admin 用户尝试授权：operatorId={}", operatorId);
            return false;
        }
        
        // 目标角色必须是系统管理员类型
        if (!isSystemAdminRole(targetRoleId)) {
            log.warn("admin 只能授权给系统管理员角色：targetRoleId={}, companyTypeId != 4", targetRoleId);
            return false;
        }
        
        return true;
    }
    
    /**
     * 检查用户是否可以使用授权功能
     * 规则：
     * 1. admin 可以使用授权功能
     * 2. 获得授权的系统管理员可以使用，但不能二次授权
     */
    public boolean canUseGrantAuthority(Long userId) {
        return isAdmin(userId) || hasGrantedAuthority(userId);
    }
    
    /**
     * 检查用户是否获得了授权（grant_level > 0）
     */
    public boolean hasGrantedAuthority(Long userId) {
        String sql = """
            SELECT COUNT(1) FROM permission p
            JOIN user_roles ur ON p.role_id = ur.role_id
            WHERE ur.user_id = ? AND p.grant_level > 0
            """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null && count > 0;
    }
    
    /**
     * 检查用户是否可以二次授权
     * 规则：
     * 1. admin 可以授权
     * 2. 获得授权的系统管理员不能二次授权（grant_level = 1，不能传递）
     */
    public boolean canGrantAgain(Long userId) {
        // admin 可以无限授权
        if (isAdmin(userId)) {
            return true;
        }
        
        // 获得授权的用户不能二次授权
        if (hasGrantedAuthority(userId)) {
            log.warn("获得授权的用户尝试二次授权：userId={}", userId);
            return false;
        }
        
        return false;
    }
    
    /**
     * 授权给角色
     * 设置 grant_level 和 grantable
     */
    public void grantToRole(Long roleId, Long resourceId, Long operatorId) {
        // 检查授权权限
        if (!canGrantAgain(operatorId)) {
            throw new IllegalStateException("您没有授权权限或不能二次授权");
        }
        
        // 确定授权级别
        short grantLevel = GRANT_LEVEL_PRIMARY; // admin 授权给系统管理员 = 一级授权
        
        String sql = """
            INSERT INTO permission (role_id, permission_id, grant_level, grantable, granted_by, granted_at)
            VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            ON CONFLICT (role_id, permission_id) DO UPDATE SET
                grant_level = EXCLUDED.grant_level,
                grantable = EXCLUDED.grantable,
                granted_by = EXCLUDED.granted_by,
                granted_at = EXCLUDED.granted_at
            """;
        
        jdbcTemplate.update(sql, roleId, resourceId, grantLevel, false, operatorId);
        log.info("授权成功：roleId={}, resourceId={}, grantLevel={}, operatorId={}", 
            roleId, resourceId, grantLevel, operatorId);
    }
    
    /**
     * 获取用户的授权级别
     */
    public short getUserGrantLevel(Long userId, Long resourceId) {
        String sql = """
            SELECT COALESCE(p.grant_level, 0) FROM permission p
            JOIN user_roles ur ON p.role_id = ur.role_id
            WHERE ur.user_id = ? AND p.permission_id = ?
            """;
        
        try {
            Short level = jdbcTemplate.queryForObject(sql, Short.class, userId, resourceId);
            return level != null ? level : GRANT_LEVEL_NONE;
        } catch (Exception e) {
            return GRANT_LEVEL_NONE;
        }
    }
}