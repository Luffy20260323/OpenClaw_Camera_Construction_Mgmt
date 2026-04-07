package com.qidian.camera.module.auth.controller;

import com.qidian.camera.common.response.Result;
import com.qidian.camera.module.auth.dto.PermissionDTO;
import com.qidian.camera.module.auth.dto.RolePermissionDTO;
import com.qidian.camera.module.auth.entity.PermissionAuditLog;
import com.qidian.camera.module.auth.entity.UserContext;
import com.qidian.camera.module.auth.mapper.UserRoleMapper;
import com.qidian.camera.module.auth.service.GrantAuthorityService;
import com.qidian.camera.module.auth.service.PermissionAuditLogService;
import com.qidian.camera.module.auth.service.PermissionService;
import com.qidian.camera.module.auth.service.impl.PermissionServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 权限管理控制器
 * 
 * 授权规则：
 * 1. 只有 admin 用户可以授权
 * 2. admin 只能授权给系统管理员角色（company_type_id = 4）
 * 3. 获得授权的系统管理员不能二次授权
 */
@Tag(name = "权限管理", description = "权限配置管理")
@RestController
@RequestMapping("/permission")
@RequiredArgsConstructor
@Slf4j
public class PermissionController {
    
    private final PermissionService permissionService;
    private final PermissionServiceImpl permissionServiceImpl;
    private final JdbcTemplate jdbcTemplate;
    private final PermissionAuditLogService auditLogService;
    private final com.qidian.camera.module.auth.service.PermissionCacheService permissionCacheService;
    private final UserRoleMapper userRoleMapper;
    private final GrantAuthorityService grantAuthorityService;
    
    @Operation(summary = "获取所有权限列表", description = "获取系统中所有权限定义")
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> getAllPermissions() {
        String sql = "SELECT id, code as permission_code, name as permission_name, description, module_code, type as permission_type FROM resource WHERE permission_key IS NOT NULL ORDER BY id";
        List<Map<String, Object>> permissions = jdbcTemplate.queryForList(sql);
        return Result.success(permissions);
    }
    
    @Operation(summary = "获取所有角色列表", description = "获取系统中所有角色定义")
    @GetMapping("/roles")
    public Result<List<Map<String, Object>>> getAllRoles() {
        String sql = "SELECT id, role_name, role_code, role_description, company_type_id FROM roles ORDER BY id";
        List<Map<String, Object>> roles = jdbcTemplate.queryForList(sql);
        return Result.success(roles);
    }
    
    @Operation(summary = "获取系统管理员角色列表", description = "获取可以被授权的系统管理员角色（company_type_id = 4）")
    @GetMapping("/roles/system-admin")
    public Result<List<Map<String, Object>>> getSystemAdminRoles() {
        String sql = "SELECT id, role_name, role_code, role_description FROM roles WHERE company_type_id = 4 ORDER BY id";
        List<Map<String, Object>> roles = jdbcTemplate.queryForList(sql);
        return Result.success(roles);
    }
    
    @Operation(summary = "获取权限分组列表", description = "获取系统中所有权限分组")
    @GetMapping("/groups")
    public Result<List<Map<String, Object>>> getPermissionGroups() {
        String sql = "SELECT group_code, group_name, group_description FROM permission_groups ORDER BY group_code";
        List<Map<String, Object>> groups = jdbcTemplate.queryForList(sql);
        return Result.success(groups);
    }
    
    @Operation(summary = "获取角色的权限配置", description = "获取指定角色的权限列表")
    @GetMapping("/role/{roleId}")
    public Result<RolePermissionDTO> getRolePermissions(@PathVariable Long roleId) {
        String roleSql = "SELECT id, role_name, role_code FROM roles WHERE id = ?";
        Map<String, Object> roleMap = jdbcTemplate.queryForMap(roleSql, roleId);
        
        String permSql = "SELECT permission_id, grant_level, grantable, granted_by FROM permission WHERE role_id = ?";
        List<Map<String, Object>> permRecords = jdbcTemplate.queryForList(permSql, roleId);
        List<Long> permissionIds = permRecords.stream()
            .map(r -> ((Number) r.get("permission_id")).longValue())
            .toList();
        
        RolePermissionDTO dto = new RolePermissionDTO();
        dto.setRoleId(roleId);
        dto.setRoleName((String) roleMap.get("role_name"));
        dto.setRoleCode((String) roleMap.get("role_code"));
        dto.setPermissionIds(permissionIds);
        
        return Result.success(dto);
    }
    
    @Operation(summary = "配置角色权限", description = "为角色分配权限（仅 admin，且只能授权给系统管理员角色）")
    @PutMapping("/role/{roleId}")
    public Result<Void> updateRolePermissions(
            @PathVariable Long roleId,
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {
        
        @SuppressWarnings("unchecked")
        List<Long> permissionIds = (List<Long>) request.get("permissionIds");
        String comment = (String) request.getOrDefault("comment", "");
        
        // 获取当前用户
        UserContext currentUser = permissionServiceImpl.getCurrentUser();
        Long operatorId = currentUser.getUserId();
        
        // ===== 授权规则检查 =====
        // 1. 只有 admin 可以授权
        if (!grantAuthorityService.isAdmin(operatorId)) {
            log.warn("非 admin 用户尝试授权：operatorId={}, username={}", operatorId, currentUser.getUsername());
            return Result.error(403, "只有 admin 用户可以分配权限");
        }
        
        // 2. 目标角色必须是系统管理员类型（company_type_id = 4）
        if (!grantAuthorityService.isSystemAdminRole(roleId)) {
            log.warn("admin 只能授权给系统管理员角色：roleId={}, companyTypeId != 4", roleId);
            String roleName = jdbcTemplate.queryForObject(
                "SELECT role_name FROM roles WHERE id = ?", String.class, roleId);
            return Result.error(403, "只能授权给系统管理员角色，当前角色 '" + roleName + "' 不是系统管理员类型");
        }
        
        // ===== 执行授权 =====
        // 获取原有权限
        String oldPermSql = "SELECT STRING_AGG(permission_id::text, ',') FROM permission WHERE role_id = ?";
        String oldPermissionIds = jdbcTemplate.queryForObject(oldPermSql, String.class, roleId);
        
        // 删除原有权限
        jdbcTemplate.update("DELETE FROM permission WHERE role_id = ?", roleId);
        
        // 添加新权限（设置 grant_level = 1，表示一级授权）
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long permissionId : permissionIds) {
                jdbcTemplate.update(
                    "INSERT INTO permission (role_id, permission_id, grant_level, grantable, granted_by, granted_at) " +
                    "VALUES (?, ?, 1, false, ?, CURRENT_TIMESTAMP)",
                    roleId, permissionId, operatorId);
            }
        }
        
        // 获取角色名称
        String roleName = jdbcTemplate.queryForObject(
            "SELECT role_name FROM roles WHERE id = ?", String.class, roleId);
        
        // 记录审计日志
        PermissionAuditLog auditLog = PermissionAuditLog.builder()
            .operatorId(currentUser.getUserId())
            .operatorName(currentUser.getRealName())
            .operationType("CONFIG_ROLE_PERMISSION")
            .targetType("ROLE")
            .targetId(roleId)
            .targetName(roleName)
            .permissionIdsBefore(oldPermissionIds)
            .permissionIdsAfter(permissionIds != null ? permissionIds.toString() : "[]")
            .changeDescription(comment + " (授权级别: 一级授权, 授权人: " + currentUser.getUsername() + ")")
            .ipAddress(getClientIp(httpRequest))
            .userAgent(httpRequest.getHeader("User-Agent"))
            .build();
        
        auditLogService.logPermissionConfig(auditLog);
        
        // 失效缓存
        permissionCacheService.evictRolePermissions(roleId);
        List<Long> userIds = userRoleMapper.findUserIdsByRoleId(roleId);
        if (userIds != null && !userIds.isEmpty()) {
            permissionCacheService.evictUserPermissionsByRole(roleId, userIds);
        }
        
        log.info("角色 {} 权限配置更新，操作人：{}, 权限数量：{}, 授权级别：一级授权", 
            roleId, currentUser.getUsername(), permissionIds != null ? permissionIds.size() : 0);
        return Result.success();
    }
    
    @Operation(summary = "获取权限矩阵", description = "获取所有角色和权限的矩阵关系")
    @GetMapping("/matrix")
    public Result<Map<String, Object>> getPermissionMatrix() {
        Map<String, Object> matrix = new HashMap<>();
        
        String rolesSql = "SELECT id, role_code, role_name, role_description, company_type_id FROM roles ORDER BY id";
        List<Map<String, Object>> roles = jdbcTemplate.queryForList(rolesSql);
        matrix.put("roles", roles);
        
        String permsSql = "SELECT id, code as permission_code, name as permission_name, type as resource_type, 'view' as action " +
                         "FROM resource WHERE permission_key IS NOT NULL ORDER BY type, code";
        List<Map<String, Object>> permissions = jdbcTemplate.queryForList(permsSql);
        matrix.put("permissions", permissions);
        
        Map<String, List<Map<String, Object>>> permissionGroups = new HashMap<>();
        for (Map<String, Object> perm : permissions) {
            String resourceType = (String) perm.get("resource_type");
            permissionGroups.computeIfAbsent(resourceType, k -> new ArrayList<>()).add(perm);
        }
        matrix.put("permissionGroups", permissionGroups);
        
        Map<Long, Set<Long>> rolePermissionMap = new HashMap<>();
        for (Map<String, Object> role : roles) {
            Long roleId = ((Number) role.get("id")).longValue();
            String permSql = "SELECT permission_id FROM permission WHERE role_id = ?";
            List<Long> permIds = jdbcTemplate.queryForList(permSql, Long.class, roleId);
            rolePermissionMap.put(roleId, new HashSet<>(permIds));
        }
        matrix.put("rolePermissionMap", rolePermissionMap);
        
        return Result.success(matrix);
    }
    
    @Operation(summary = "查询审计日志", description = "查询权限配置审计日志")
    @GetMapping("/audit-logs")
    public Result<Map<String, Object>> getAuditLogs(
            @RequestParam(required = false) Long targetId,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) Long operatorId,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String operatorName,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<PermissionAuditLog> auditPage = auditLogService.getAuditLogs(
            targetId, targetType, operatorId, operationType, operatorName, pageable);
        
        Map<String, Object> result = new HashMap<>();
        result.put("content", auditPage.getContent());
        result.put("total", auditPage.getTotalElements());
        result.put("totalPages", auditPage.getTotalPages());
        result.put("number", auditPage.getNumber() + 1);
        result.put("size", auditPage.getSize());
        
        return Result.success(result);
    }
    
    /**
     * 获取客户端 IP 地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}