package com.qidian.camera.module.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 权限缓存服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionCacheService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String USER_PERMISSIONS_PREFIX = "user:permissions:";
    private static final String USER_ROLES_PREFIX = "user:roles:";
    private static final String ROLE_PERMISSIONS_PREFIX = "role:permissions:";
    private static final long CACHE_EXPIRE_HOURS = 2;
    
    /**
     * 获取用户权限（从缓存）
     */
    @SuppressWarnings("unchecked")
    public Set<String> getUserPermissionsFromCache(Long userId) {
        String key = USER_PERMISSIONS_PREFIX + userId;
        try {
            Set<Object> cached = redisTemplate.opsForSet().members(key);
            if (cached != null) {
                return cached.stream()
                    .map(Object::toString)
                    .collect(Collectors.toSet());
            }
        } catch (Exception e) {
            log.warn("从缓存读取用户权限失败：userId={}", userId, e);
        }
        return null;
    }
    
    /**
     * 缓存用户权限
     */
    public void cacheUserPermissions(Long userId, List<String> permissions) {
        String key = USER_PERMISSIONS_PREFIX + userId;
        try {
            redisTemplate.opsForSet().add(key, permissions.toArray());
            redisTemplate.expire(key, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            log.debug("缓存用户权限：userId={}, 权限数={}", userId, permissions.size());
        } catch (Exception e) {
            log.warn("缓存用户权限失败：userId={}", userId, e);
        }
    }
    
    /**
     * 获取用户角色（从缓存）
     */
    @SuppressWarnings("unchecked")
    public Set<String> getUserRolesFromCache(Long userId) {
        String key = USER_ROLES_PREFIX + userId;
        try {
            Set<Object> cached = redisTemplate.opsForSet().members(key);
            if (cached != null) {
                return cached.stream()
                    .map(Object::toString)
                    .collect(Collectors.toSet());
            }
        } catch (Exception e) {
            log.warn("从缓存读取用户角色失败：userId={}", userId, e);
        }
        return null;
    }
    
    /**
     * 缓存用户角色
     */
    public void cacheUserRoles(Long userId, List<String> roles) {
        String key = USER_ROLES_PREFIX + userId;
        try {
            redisTemplate.opsForSet().add(key, roles.toArray());
            redisTemplate.expire(key, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            log.debug("缓存用户角色：userId={}, 角色数={}", userId, roles.size());
        } catch (Exception e) {
            log.warn("缓存用户角色失败：userId={}", userId, e);
        }
    }
    
    /**
     * 获取角色权限（从缓存）
     */
    @SuppressWarnings("unchecked")
    public Set<Long> getRolePermissionsFromCache(Long roleId) {
        String key = ROLE_PERMISSIONS_PREFIX + roleId;
        try {
            Set<Object> cached = redisTemplate.opsForSet().members(key);
            if (cached != null) {
                return cached.stream()
                    .map(obj -> ((Number) obj).longValue())
                    .collect(Collectors.toSet());
            }
        } catch (Exception e) {
            log.warn("从缓存读取角色权限失败：roleId={}", roleId, e);
        }
        return null;
    }
    
    /**
     * 缓存角色权限
     */
    public void cacheRolePermissions(Long roleId, List<Long> permissionIds) {
        String key = ROLE_PERMISSIONS_PREFIX + roleId;
        try {
            redisTemplate.opsForSet().add(key, permissionIds.toArray());
            redisTemplate.expire(key, 24, TimeUnit.HOURS);  // 角色权限缓存 24 小时
            log.debug("缓存角色权限：roleId={}, 权限数={}", roleId, permissionIds.size());
        } catch (Exception e) {
            log.warn("缓存角色权限失败：roleId={}", roleId, e);
        }
    }
    
    /**
     * 失效用户权限缓存
     */
    public void evictUserPermissions(Long userId) {
        String key = USER_PERMISSIONS_PREFIX + userId;
        try {
            redisTemplate.delete(key);
            log.debug("失效用户权限缓存：userId={}", userId);
        } catch (Exception e) {
            log.warn("失效用户权限缓存失败：userId={}", userId, e);
        }
    }
    
    /**
     * 失效角色权限缓存
     */
    public void evictRolePermissions(Long roleId) {
        String key = ROLE_PERMISSIONS_PREFIX + roleId;
        try {
            redisTemplate.delete(key);
            log.debug("失效角色权限缓存：roleId={}", roleId);
        } catch (Exception e) {
            log.warn("失效角色权限缓存失败：roleId={}", roleId, e);
        }
    }
    
    /**
     * 失效角色下所有用户的权限缓存
     */
    public void evictUserPermissionsByRole(Long roleId) {
        // 这里简化实现，实际应该查询该角色下的所有用户
        log.debug("失效角色下所有用户的权限缓存：roleId={}", roleId);
        // 实际实现需要查询 user_roles 表
    }
}
