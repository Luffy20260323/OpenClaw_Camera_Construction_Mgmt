package com.qidian.camera.module.auth.controller;

import com.qidian.camera.common.response.Result;
import com.qidian.camera.module.auth.dto.PermissionDTO;
import com.qidian.camera.module.auth.dto.RolePermissionDTO;
import com.qidian.camera.module.auth.entity.PermissionAuditLog;
import com.qidian.camera.module.auth.entity.UserContext;
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
    
    @Operation(summary = "获取所有权限列表", description = "获取系统中所有权限定义")
    @GetMapping("/list")
    public Result<List<PermissionDTO>> getAllPermissions() {
        List<PermissionDTO> permissions = permissionService.getAllPermissions()
            .stream()
            .map(p -> {
                PermissionDTO dto = new PermissionDTO();
                dto.setId(p.getId());
                dto.setPermissionCode(p.getPermissionCode());
                dto.setPermissionName(p.getPermissionName());
                dto.setDescription(p.getDescription());
                return dto;
            })
            .toList();
        return Result.success(permissions);
    }
    
    @Operation(summary = "获取所有角色列表", description = "获取系统中所有角色定义")
    @GetMapping("/roles")
    public Result<List<Map<String, Object>>> getAllRoles() {
        String sql = "SELECT id, role_name, role_code, role_description FROM roles ORDER BY id";
        List<Map<String, Object>> roles = jdbcTemplate.queryForList(sql);
        return Result.success(roles);
    }
    
    @Operation(summary = "获取角色的权限配置", description = "获取指定角色的权限列表")
    @GetMapping("/role/{roleId}")
    public Result<RolePermissionDTO> getRolePermissions(@PathVariable Long roleId) {
        // 获取角色信息
        String roleSql = "SELECT id, role_name, role_code FROM roles WHERE id = ?";
        Map<String, Object> roleMap = jdbcTemplate.queryForMap(roleSql, roleId);
        
        // 获取角色权限
        String permSql = "SELECT permission_id FROM role_permissions WHERE role_id = ?";
        List<Long> permissionIds = jdbcTemplate.queryForList(permSql, Long.class, roleId);
        
        RolePermissionDTO dto = new RolePermissionDTO();
        dto.setRoleId(roleId);
        dto.setRoleName((String) roleMap.get("role_name"));
        dto.setRoleCode((String) roleMap.get("role_code"));
        dto.setPermissionIds(permissionIds);
        
        return Result.success(dto);
    }
    
    @Operation(summary = "配置角色权限", description = "为角色分配权限（仅系统管理员）")
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
        
        // 获取原有权限
        String oldPermSql = "SELECT ARRAY_AGG(permission_id) FROM role_permissions WHERE role_id = ?";
        String oldPermissionIds = jdbcTemplate.queryForObject(oldPermSql, String.class, roleId);
        
        // 删除原有权限
        jdbcTemplate.update("DELETE FROM role_permissions WHERE role_id = ?", roleId);
        
        // 添加新权限
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long permissionId : permissionIds) {
                jdbcTemplate.update(
                    "INSERT INTO role_permissions (role_id, permission_id) VALUES (?, ?)",
                    roleId, permissionId);
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
            .changeDescription(comment)
            .ipAddress(getClientIp(httpRequest))
            .userAgent(httpRequest.getHeader("User-Agent"))
            .build();
        
        auditLogService.logPermissionConfig(auditLog);
        
        // 失效缓存
        permissionCacheService.evictRolePermissions(roleId);
        permissionCacheService.evictUserPermissionsByRole(roleId);
        
        log.info("角色 {} 权限配置更新，操作人：{}, 权限数量：{}", 
            roleId, currentUser.getUsername(), permissionIds != null ? permissionIds.size() : 0);
        return Result.success();
    }
    
    @Operation(summary = "获取权限矩阵", description = "获取所有角色和权限的矩阵关系")
    @GetMapping("/matrix")
    public Result<Map<String, Object>> getPermissionMatrix() {
        Map<String, Object> matrix = new HashMap<>();
        
        // 获取所有角色
        String rolesSql = "SELECT id, role_code, role_name, role_description FROM roles ORDER BY id";
        List<Map<String, Object>> roles = jdbcTemplate.queryForList(rolesSql);
        matrix.put("roles", roles);
        
        // 获取所有权限（按 resource_type 分组）
        String permsSql = "SELECT id, permission_code, permission_name, resource_type, action " +
                         "FROM permissions ORDER BY resource_type, action";
        List<Map<String, Object>> permissions = jdbcTemplate.queryForList(permsSql);
        matrix.put("permissions", permissions);
        
        // 按 resource_type 分组
        Map<String, List<Map<String, Object>>> permissionGroups = new HashMap<>();
        for (Map<String, Object> perm : permissions) {
            String resourceType = (String) perm.get("resource_type");
            permissionGroups.computeIfAbsent(resourceType, k -> new ArrayList<>()).add(perm);
        }
        matrix.put("permissionGroups", permissionGroups);
        
        // 获取角色 - 权限映射
        Map<Long, Set<Long>> rolePermissionMap = new HashMap<>();
        for (Map<String, Object> role : roles) {
            Long roleId = ((Number) role.get("id")).longValue();
            String permSql = "SELECT permission_id FROM role_permissions WHERE role_id = ?";
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
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<PermissionAuditLog> auditPage = auditLogService.getAuditLogs(
            targetId, targetType, operatorId, pageable);
        
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
