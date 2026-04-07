package com.qidian.camera.module.auth.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qidian.camera.common.response.Result;
import com.qidian.camera.module.auth.dto.*;
import com.qidian.camera.module.auth.entity.*;
import com.qidian.camera.module.auth.mapper.*;
import com.qidian.camera.module.auth.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户权限查询 Controller
 */
@Slf4j
@Tag(name = "用户权限", description = "查询用户的权限信息")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserPermissionController {
    
    private final PermissionService permissionService;
    private final ResourceService resourceService;
    private final JdbcTemplate jdbcTemplate;
    private final UserRoleMapper userRoleMapper;
    
    /**
     * 获取用户的权限列表
     */
    @Operation(summary = "获取用户权限", description = "返回用户拥有的所有资源权限")
    @GetMapping("/{userId}/permissions")
    public Result<List<Resource>> getUserPermissions(@PathVariable Long userId) {
        return Result.success(permissionService.getUserPermissionResources(userId));
    }
    
    /**
     * 获取用户的权限标识列表
     */
    @Operation(summary = "获取用户权限标识", description = "返回用户拥有的所有 permission_key")
    @GetMapping("/{userId}/permission-keys")
    public Result<Set<String>> getUserPermissionKeys(@PathVariable Long userId) {
        return Result.success(permissionService.getUserPermissionKeys(userId));
    }
    
    /**
     * 获取用户的菜单树（根据权限过滤）
     */
    @Operation(summary = "获取用户菜单树", description = "返回用户有权访问的菜单树")
    @GetMapping("/{userId}/menu-tree")
    public Result<List<ResourceService.ResourceTreeNode>> getUserMenuTree(@PathVariable Long userId) {
        Set<Long> permissionIds = permissionService.calculateUserPermissions(userId);
        
        // 获取所有菜单资源
        List<ResourceService.ResourceTreeNode> menuTree = resourceService.getMenuTree();
        
        // 过滤用户有权访问的菜单
        List<ResourceService.ResourceTreeNode> filteredTree = filterMenuTree(menuTree, permissionIds);
        
        return Result.success(filteredTree);
    }
    
    /**
     * 过滤菜单树（只保留用户有权限的节点）
     */
    private List<ResourceService.ResourceTreeNode> filterMenuTree(
            List<ResourceService.ResourceTreeNode> tree, Set<Long> permissionIds) {
        return tree.stream()
            .filter(node -> permissionIds.contains(node.getId()) || hasChildPermission(node, permissionIds))
            .map(node -> {
                node.setChildren(filterMenuTree(node.getChildren(), permissionIds));
                return node;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 检查节点是否有子节点权限
     */
    private boolean hasChildPermission(ResourceService.ResourceTreeNode node, Set<Long> permissionIds) {
        if (node.getChildren() == null || node.getChildren().isEmpty()) {
            return false;
        }
        return node.getChildren().stream()
            .anyMatch(child -> permissionIds.contains(child.getId()) || hasChildPermission(child, permissionIds));
    }
    
    /**
     * 检查用户是否有指定权限
     */
    @Operation(summary = "检查用户权限", description = "检查用户是否拥有指定权限")
    @GetMapping("/{userId}/has-permission")
    public Result<Boolean> checkPermission(
            @PathVariable Long userId,
            @RequestParam String permissionKey) {
        return Result.success(permissionService.hasPermission(userId, permissionKey));
    }
    
    /**
     * 清除用户权限缓存
     */
    @Operation(summary = "清除权限缓存", description = "清除用户的权限缓存")
    @PostMapping("/{userId}/permissions/refresh-cache")
    public Result<Void> refreshCache(@PathVariable Long userId) {
        permissionService.evictUserPermissionCache(userId);
        return Result.success();
    }
    
    /**
     * 获取用户权限详情（包含来源追溯信息）
     * 权限来源分类：
     * - BASIC: 基本权限（来自 role_permission）
     * - DEFAULT: 缺省权限（来自 role_permission）
     * - ADJUSTMENT: 调整权限（来自 user_permission_adjustments）
     */
    @Operation(summary = "获取用户权限详情", description = "返回用户权限详情，包含来源追溯信息")
    @GetMapping("/{userId}/permissions/detail")
    public Result<List<UserPermissionDetailDTO>> getUserPermissionsDetail(@PathVariable Long userId) {
        List<UserPermissionDetailDTO> result = new ArrayList<>();
        
        // 获取用户的所有角色
        List<UserRole> userRoles = userRoleMapper.selectList(
            new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId)
        );
        
        if (userRoles == null || userRoles.isEmpty()) {
            return Result.success(result);
        }
        
        List<Long> roleIds = userRoles.stream()
            .map(UserRole::getRoleId)
            .collect(Collectors.toList());
        
        // 获取角色名称映射
        Map<Long, String> roleNameMap = new HashMap<>();
        for (Long roleId : roleIds) {
            String roleName = jdbcTemplate.queryForObject(
                "SELECT role_name FROM roles WHERE id = ?", String.class, roleId);
            if (roleName != null) {
                roleNameMap.put(roleId, roleName);
            }
        }
        
        // 1. 获取基本权限（来自 permission 表）
        String basicPermSql = """
            SELECT DISTINCT r.id as resource_id, r.name as resource_name, r.code as resource_code,
                   r.type as resource_type, r.permission_key, 'BASIC' as permission_source,
                   p.role_id, null as adjustment_type
            FROM permission p
            JOIN resource r ON p.permission_id = r.id
            WHERE p.role_id IN (?::bigint[])
            ORDER BY r.type, r.code
            """;
        
        try {
            String sqlWithArray = basicPermSql.replace("?::bigint[]", 
                roleIds.stream().map(id -> id.toString()).collect(Collectors.joining(",", "ARRAY[", "]")));
            
            List<Map<String, Object>> basicPerms = jdbcTemplate.queryForList(sqlWithArray);
            for (Map<String, Object> perm : basicPerms) {
                UserPermissionDetailDTO dto = new UserPermissionDetailDTO();
                dto.setResourceId(((Number) perm.get("resource_id")).longValue());
                dto.setResourceName((String) perm.get("resource_name"));
                dto.setResourceCode((String) perm.get("resource_code"));
                dto.setResourceType((String) perm.get("resource_type"));
                dto.setPermissionKey((String) perm.get("permission_key"));
                dto.setPermissionSource("BASIC");
                
                Long roleId = ((Number) perm.get("role_id")).longValue();
                dto.setRoleName(roleNameMap.get(roleId));
                dto.setSourceDetail(roleNameMap.get(roleId));
                dto.setEnabled(true);
                
                result.add(dto);
            }
        } catch (Exception e) {
            log.warn("查询基本权限失败：{}", e.getMessage());
        }
        
        // 2. 获取缺省权限（临时从 permission 表获取）
        String defaultPermSql = """
            SELECT DISTINCT r.id as resource_id, r.name as resource_name, r.code as resource_code,
                   r.type as resource_type, r.permission_key, 'DEFAULT' as permission_source,
                   p.role_id, null as adjustment_type
            FROM permission p
            JOIN resource r ON p.permission_id = r.id
            WHERE p.role_id IN (?::bigint[])
            ORDER BY r.type, r.code
            """;
        
        try {
            String sqlWithArray = defaultPermSql.replace("?::bigint[]", 
                roleIds.stream().map(id -> id.toString()).collect(Collectors.joining(",", "ARRAY[", "]")));
            
            List<Map<String, Object>> defaultPerms = jdbcTemplate.queryForList(sqlWithArray);
            for (Map<String, Object> perm : defaultPerms) {
                UserPermissionDetailDTO dto = new UserPermissionDetailDTO();
                dto.setResourceId(((Number) perm.get("resource_id")).longValue());
                dto.setResourceName((String) perm.get("resource_name"));
                dto.setResourceCode((String) perm.get("resource_code"));
                dto.setResourceType((String) perm.get("resource_type"));
                dto.setPermissionKey((String) perm.get("permission_key"));
                dto.setPermissionSource("DEFAULT");
                
                Long roleId = ((Number) perm.get("role_id")).longValue();
                dto.setRoleName(roleNameMap.get(roleId));
                dto.setSourceDetail(roleNameMap.get(roleId) + " (缺省)");
                dto.setEnabled(true);
                
                result.add(dto);
            }
        } catch (Exception e) {
            log.warn("查询缺省权限失败：{}", e.getMessage());
        }
        
        // 3. 获取用户调整权限（来自 user_permission_adjustments）
        String adjustPermSql = """
            SELECT r.id as resource_id, r.name as resource_name, r.code as resource_code,
                   r.type as resource_type, r.permission_key, 'ADJUSTMENT' as permission_source,
                   upa.action as adjustment_type
            FROM user_permission_adjustments upa
            JOIN resources r ON upa.resource_id = r.id
            WHERE upa.user_id = ?
            ORDER BY r.type, r.code
            """;
        
        try {
            List<Map<String, Object>> adjustPerms = jdbcTemplate.queryForList(adjustPermSql, userId);
            for (Map<String, Object> perm : adjustPerms) {
                UserPermissionDetailDTO dto = new UserPermissionDetailDTO();
                dto.setResourceId(((Number) perm.get("resource_id")).longValue());
                dto.setResourceName((String) perm.get("resource_name"));
                dto.setResourceCode((String) perm.get("resource_code"));
                dto.setResourceType((String) perm.get("resource_type"));
                dto.setPermissionKey((String) perm.get("permission_key"));
                dto.setPermissionSource("ADJUSTMENT");
                dto.setAdjustmentType((String) perm.get("adjustment_type"));
                dto.setSourceDetail("ADD".equals(perm.get("adjustment_type")) ? "个人新增" : "个人移除");
                dto.setEnabled(!"REMOVE".equals(perm.get("adjustment_type")));
                
                result.add(dto);
            }
        } catch (Exception e) {
            log.warn("查询调整权限失败：{}", e.getMessage());
        }
        
        // 去重：同一个资源可能有多个来源，保留优先级最高的（ADJUSTMENT > BASIC > DEFAULT）
        Map<Long, UserPermissionDetailDTO> uniquePerms = new LinkedHashMap<>();
        for (UserPermissionDetailDTO dto : result) {
            Long resourceId = dto.getResourceId();
            if (!uniquePerms.containsKey(resourceId)) {
                uniquePerms.put(resourceId, dto);
            } else {
                // 如果已有记录，根据来源优先级决定是否替换
                UserPermissionDetailDTO existing = uniquePerms.get(resourceId);
                if (shouldReplace(existing, dto)) {
                    uniquePerms.put(resourceId, dto);
                }
            }
        }
        
        return Result.success(new ArrayList<>(uniquePerms.values()));
    }
    
    /**
     * 判断是否应该替换现有权限记录
     * 优先级：ADJUSTMENT > BASIC > DEFAULT
     */
    private boolean shouldReplace(UserPermissionDetailDTO existing, UserPermissionDetailDTO newDto) {
        int existingPriority = getPriority(existing.getPermissionSource());
        int newPriority = getPriority(newDto.getPermissionSource());
        return newPriority > existingPriority;
    }
    
    /**
     * 获取权限来源优先级
     */
    private int getPriority(String source) {
        if (source == null) return 0;
        return switch (source) {
            case "ADJUSTMENT" -> 3;
            case "BASIC" -> 2;
            case "DEFAULT" -> 1;
            default -> 0;
        };
    }
}