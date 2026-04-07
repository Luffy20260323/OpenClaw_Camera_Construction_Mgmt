package com.qidian.camera.module.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qidian.camera.module.auth.config.PermissionCacheConfig;
import com.qidian.camera.module.auth.entity.*;
import com.qidian.camera.module.auth.mapper.*;
import com.qidian.camera.module.role.entity.Role;
import com.qidian.camera.module.role.mapper.RoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 权限计算服务
 * 核心逻辑：计算用户/角色的完整权限
 * 
 * 缓存策略：
 * - 读：先查 Redis 缓存，缓存不存在则计算并写入
 * - 写：更新权限后立即清除相关缓存
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService {
    
    private final ResourceMapper resourceMapper;
    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final RolePermissionAdjustmentMapper rolePermissionAdjustmentMapper;
    private final UserPermissionAdjustmentMapper userPermissionAdjustmentMapper;
    private final UserRoleMapper userRoleMapper;
    private final PermissionCacheService permissionCacheService;
    private final PermissionCacheConfig cacheConfig;
    
    /**
     * 计算角色的完整权限
     * 公式：role_permission(basic + default) + adjustments(ADD) - adjustments(REMOVE)
     * 
     * 缓存策略：
     * - 先查 Redis 缓存 (role:permission:{roleId})
     * - 缓存不存在则计算并写入，TTL: 60 分钟
     */
    public Set<Long> calculateRolePermissions(Long roleId) {
        // 1. 如果缓存未启用，直接计算
        if (!cacheConfig.isEnabled()) {
            return doCalculateRolePermissions(roleId);
        }
        
        // 2. 尝试从缓存获取
        Set<Long> cached = permissionCacheService.getRolePermissions(roleId);
        if (cached != null) {
            log.debug("角色权限缓存命中：roleId={}", roleId);
            return cached;
        }
        
        // 3. 缓存未命中，计算并写入缓存
        log.debug("角色权限缓存未命中，计算并缓存：roleId={}", roleId);
        Set<Long> permissions = doCalculateRolePermissions(roleId);
        permissionCacheService.cacheRolePermissions(roleId, permissions);
        return permissions;
    }
    
    /**
     * 执行角色权限计算（无缓存）
     */
    private Set<Long> doCalculateRolePermissions(Long roleId) {
        // 1. 获取角色的基础权限（basic + default）
        List<RolePermission> basePermissions = rolePermissionMapper.findByRoleId(roleId);
        Set<Long> baseResourceIds = basePermissions.stream()
            .map(RolePermission::getResourceId)
            .collect(Collectors.toSet());
        
        // 2. 获取权限调整
        List<RolePermissionAdjustment> adjustments = rolePermissionAdjustmentMapper.findByRoleId(roleId);
        
        // 3. 增加的权限
        Set<Long> addedResourceIds = adjustments.stream()
            .filter(a -> a.getAction().equals(RolePermissionAdjustment.ACTION_ADD))
            .map(RolePermissionAdjustment::getResourceId)
            .collect(Collectors.toSet());
        
        // 4. 移除的权限
        Set<Long> removedResourceIds = adjustments.stream()
            .filter(a -> a.getAction().equals(RolePermissionAdjustment.ACTION_REMOVE))
            .map(RolePermissionAdjustment::getResourceId)
            .collect(Collectors.toSet());
        
        // 5. 计算最终权限
        Set<Long> finalPermissions = new HashSet<>(baseResourceIds);
        finalPermissions.addAll(addedResourceIds);
        finalPermissions.removeAll(removedResourceIds);
        
        return finalPermissions;
    }
    
    /**
     * 计算用户的完整权限
     * 公式：∑(各角色的完整权限) + user_adjustments(ADD) - user_adjustments(REMOVE)
     * 
     * 缓存策略：
     * - 先查 Redis 缓存 (user:permission:{userId})
     * - 缓存不存在则计算并写入，TTL: 30 分钟
     */
    public Set<Long> calculateUserPermissions(Long userId) {
        // 1. 如果缓存未启用，直接计算
        if (!cacheConfig.isEnabled()) {
            return doCalculateUserPermissions(userId);
        }
        
        // 2. 尝试从缓存获取
        Set<Long> cached = permissionCacheService.getUserPermissions(userId);
        if (cached != null) {
            log.debug("用户权限缓存命中：userId={}", userId);
            return cached;
        }
        
        // 3. 缓存未命中，计算并写入缓存
        log.debug("用户权限缓存未命中，计算并缓存：userId={}", userId);
        Set<Long> permissions = doCalculateUserPermissions(userId);
        permissionCacheService.cacheUserPermissions(userId, permissions);
        return permissions;
    }
    
    /**
     * 执行用户权限计算（无缓存）
     */
    private Set<Long> doCalculateUserPermissions(Long userId) {
        // 1. 获取用户的所有角色
        List<UserRole> userRoles = userRoleMapper.selectList(
            new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId)
        );
        List<Long> roleIds = userRoles.stream()
            .map(UserRole::getRoleId)
            .collect(Collectors.toList());
        
        // 2. 计算各角色的完整权限并合并
        Set<Long> rolePermissions = new HashSet<>();
        for (Long roleId : roleIds) {
            rolePermissions.addAll(calculateRolePermissions(roleId));
        }
        
        // 3. 获取用户个人的权限调整
        List<UserPermissionAdjustment> userAdjustments = userPermissionAdjustmentMapper.findByUserId(userId);
        
        // 4. 用户增加的权限
        Set<Long> userAddedIds = userAdjustments.stream()
            .filter(a -> a.getAction().equals(UserPermissionAdjustment.ACTION_ADD))
            .map(UserPermissionAdjustment::getResourceId)
            .collect(Collectors.toSet());
        
        // 5. 用户移除的权限
        Set<Long> userRemovedIds = userAdjustments.stream()
            .filter(a -> a.getAction().equals(UserPermissionAdjustment.ACTION_REMOVE))
            .map(UserPermissionAdjustment::getResourceId)
            .collect(Collectors.toSet());
        
        // 6. 计算最终权限
        Set<Long> finalPermissions = new HashSet<>(rolePermissions);
        finalPermissions.addAll(userAddedIds);
        finalPermissions.removeAll(userRemovedIds);
        
        return finalPermissions;
    }
    
    /**
     * 获取用户权限的详细信息（包含资源信息）
     */
    public List<Resource> getUserPermissionResources(Long userId) {
        Set<Long> resourceIds = calculateUserPermissions(userId);
        if (resourceIds.isEmpty()) {
            return Collections.emptyList();
        }
        return resourceMapper.selectBatchIds(resourceIds);
    }
    
    /**
     * 获取用户的所有 permission_key
     */
    public Set<String> getUserPermissionKeys(Long userId) {
        List<Resource> resources = getUserPermissionResources(userId);
        return resources.stream()
            .filter(r -> r.getPermissionKey() != null)
            .map(Resource::getPermissionKey)
            .collect(Collectors.toSet());
    }
    
    /**
     * 判断用户是否有指定权限
     */
    public boolean hasPermission(Long userId, String permissionKey) {
        Set<String> permissionKeys = getUserPermissionKeys(userId);
        return permissionKeys.contains(permissionKey);
    }
    
    /**
     * 判断用户是否有指定资源的访问权限
     */
    public boolean hasResourcePermission(Long userId, Long resourceId) {
        Set<Long> permissions = calculateUserPermissions(userId);
        return permissions.contains(resourceId);
    }
    
    /**
     * 根据 URI 和 Method 判断用户是否有 API 权限
     */
    public boolean hasApiPermission(Long userId, String uri, String method) {
        // 查找对应的 API 资源
        Resource apiResource = resourceMapper.findByUriAndMethod(uri, method);
        if (apiResource == null) {
            // 未配置权限的 API 默认放行
            return true;
        }
        return hasResourcePermission(userId, apiResource.getId());
    }
    
    // ==================== 缓存清除方法 ====================
    
    /**
     * 清除用户权限缓存
     * 触发时机：用户角色变更、用户权限调整
     */
    public void evictUserPermissionCache(Long userId) {
        log.info("清除用户权限缓存：userId={}", userId);
        permissionCacheService.evictUserPermissions(userId);
    }
    
    /**
     * 清除角色权限缓存
     * 触发时机：角色权限调整
     * 
     * 同时清除该角色下所有用户的权限缓存
     */
    public void evictRolePermissionCache(Long roleId) {
        log.info("清除角色权限缓存：roleId={}", roleId);
        
        // 1. 清除角色权限缓存
        permissionCacheService.evictRolePermissions(roleId);
        
        // 2. 清除该角色下所有用户的权限缓存
        List<Long> userIds = userRoleMapper.findUserIdsByRoleId(roleId);
        if (userIds != null && !userIds.isEmpty()) {
            permissionCacheService.evictUserPermissionsByRole(roleId, userIds);
        }
    }
    
    /**
     * 批量清除用户权限缓存
     * 触发时机：批量用户角色变更
     */
    public void evictUserPermissionCacheBatch(Collection<Long> userIds) {
        log.info("批量清除用户权限缓存：用户数={}", userIds != null ? userIds.size() : 0);
        permissionCacheService.evictUserPermissionsBatch(userIds);
    }
    
    /**
     * 清除资源相关的权限缓存
     * 触发时机：资源权限变更（角色权限调整、用户权限调整）
     * 
     * @param resourceId 资源 ID
     * @param affectedRoleIds 受影响的角色 ID 列表
     */
    public void evictResourcePermissionCache(Long resourceId, Collection<Long> affectedRoleIds) {
        log.info("清除资源相关的权限缓存：resourceId={}, 影响角色数={}", resourceId, 
            affectedRoleIds != null ? affectedRoleIds.size() : 0);
        
        if (affectedRoleIds != null) {
            for (Long roleId : affectedRoleIds) {
                evictRolePermissionCache(roleId);
            }
        }
    }
}
