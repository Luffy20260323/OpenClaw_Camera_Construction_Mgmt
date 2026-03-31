package com.qidian.camera.module.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 权限缓存服务
 * 
 * 缓存结构：
 * - user:permission:{userId} → Set<Long> resourceIds, TTL: 30 minutes
 * - role:permission:{roleId} → Set<Long> resourceIds, TTL: 60 minutes
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionCacheService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    // 缓存 Key 前缀
    private static final String USER_PERMISSION_PREFIX = "user:permission:";
    private static final String ROLE_PERMISSION_PREFIX = "role:permission:";
    
    // 缓存 TTL（分钟）
    private static final long USER_PERMISSION_TTL_MINUTES = 30;
    private static final long ROLE_PERMISSION_TTL_MINUTES = 60;
    
    /**
     * 获取用户权限缓存
     * 
     * @param userId 用户 ID
     * @return 资源 ID 集合，缓存不存在返回 null
     */
    @SuppressWarnings("unchecked")
    public Set<Long> getUserPermissions(Long userId) {
        String key = buildUserPermissionKey(userId);
        try {
            Set<Object> members = redisTemplate.opsForSet().members(key);
            if (members != null && !members.isEmpty()) {
                return members.stream()
                    .map(obj -> {
                        if (obj instanceof Number) {
                            return ((Number) obj).longValue();
                        } else if (obj instanceof String) {
                            return Long.parseLong((String) obj);
                        }
                        return null;
                    })
                    .filter(id -> id != null)
                    .collect(java.util.stream.Collectors.toSet());
            }
        } catch (Exception e) {
            log.warn("从缓存读取用户权限失败：userId={}", userId, e);
        }
        return null;
    }
    
    /**
     * 缓存用户权限
     * 
     * @param userId 用户 ID
     * @param resourceIds 资源 ID 集合
     */
    public void cacheUserPermissions(Long userId, Collection<Long> resourceIds) {
        if (resourceIds == null || resourceIds.isEmpty()) {
            return;
        }
        String key = buildUserPermissionKey(userId);
        try {
            redisTemplate.opsForSet().add(key, resourceIds.toArray());
            redisTemplate.expire(key, USER_PERMISSION_TTL_MINUTES, TimeUnit.MINUTES);
            log.debug("缓存用户权限：userId={}, 资源数={}", userId, resourceIds.size());
        } catch (Exception e) {
            log.warn("缓存用户权限失败：userId={}", userId, e);
        }
    }
    
    /**
     * 获取角色权限缓存
     * 
     * @param roleId 角色 ID
     * @return 资源 ID 集合，缓存不存在返回 null
     */
    @SuppressWarnings("unchecked")
    public Set<Long> getRolePermissions(Long roleId) {
        String key = buildRolePermissionKey(roleId);
        try {
            Set<Object> members = redisTemplate.opsForSet().members(key);
            if (members != null && !members.isEmpty()) {
                return members.stream()
                    .map(obj -> {
                        if (obj instanceof Number) {
                            return ((Number) obj).longValue();
                        } else if (obj instanceof String) {
                            return Long.parseLong((String) obj);
                        }
                        return null;
                    })
                    .filter(id -> id != null)
                    .collect(java.util.stream.Collectors.toSet());
            }
        } catch (Exception e) {
            log.warn("从缓存读取角色权限失败：roleId={}", roleId, e);
        }
        return null;
    }
    
    /**
     * 缓存角色权限
     * 
     * @param roleId 角色 ID
     * @param resourceIds 资源 ID 集合
     */
    public void cacheRolePermissions(Long roleId, Collection<Long> resourceIds) {
        if (resourceIds == null || resourceIds.isEmpty()) {
            return;
        }
        String key = buildRolePermissionKey(roleId);
        try {
            redisTemplate.opsForSet().add(key, resourceIds.toArray());
            redisTemplate.expire(key, ROLE_PERMISSION_TTL_MINUTES, TimeUnit.MINUTES);
            log.debug("缓存角色权限：roleId={}, 资源数={}", roleId, resourceIds.size());
        } catch (Exception e) {
            log.warn("缓存角色权限失败：roleId={}", roleId, e);
        }
    }
    
    /**
     * 清除用户权限缓存
     * 
     * @param userId 用户 ID
     */
    public void evictUserPermissions(Long userId) {
        String key = buildUserPermissionKey(userId);
        try {
            redisTemplate.delete(key);
            log.debug("清除用户权限缓存：userId={}", userId);
        } catch (Exception e) {
            log.warn("清除用户权限缓存失败：userId={}", userId, e);
        }
    }
    
    /**
     * 清除角色权限缓存
     * 
     * @param roleId 角色 ID
     */
    public void evictRolePermissions(Long roleId) {
        String key = buildRolePermissionKey(roleId);
        try {
            redisTemplate.delete(key);
            log.debug("清除角色权限缓存：roleId={}", roleId);
        } catch (Exception e) {
            log.warn("清除角色权限缓存失败：roleId={}", roleId, e);
        }
    }
    
    /**
     * 清除角色下所有用户的权限缓存
     * 
     * @param roleId 角色 ID
     * @param userIds 该角色下的所有用户 ID
     */
    public void evictUserPermissionsByRole(Long roleId, Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        try {
            for (Long userId : userIds) {
                evictUserPermissions(userId);
            }
            log.debug("清除角色下所有用户的权限缓存：roleId={}, 用户数={}", roleId, userIds.size());
        } catch (Exception e) {
            log.warn("清除角色下所有用户的权限缓存失败：roleId={}", roleId, e);
        }
    }
    
    /**
     * 批量清除用户权限缓存
     * 
     * @param userIds 用户 ID 集合
     */
    public void evictUserPermissionsBatch(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        try {
            for (Long userId : userIds) {
                evictUserPermissions(userId);
            }
            log.debug("批量清除用户权限缓存：用户数={}", userIds.size());
        } catch (Exception e) {
            log.warn("批量清除用户权限缓存失败", e);
        }
    }
    
    /**
     * 构建用户权限缓存 Key
     */
    private String buildUserPermissionKey(Long userId) {
        return USER_PERMISSION_PREFIX + userId;
    }
    
    /**
     * 构建角色权限缓存 Key
     */
    private String buildRolePermissionKey(Long roleId) {
        return ROLE_PERMISSION_PREFIX + roleId;
    }
    
    // ==================== 兼容旧版本的方法（基于 permission_code 字符串） ====================
    
    /**
     * 获取用户权限缓存（旧版本，基于 permission_code 字符串）
     * @deprecated 使用 getUserPermissions 代替
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public java.util.Set<String> getUserPermissionsFromCache(Long userId) {
        String key = "user:permissions:" + userId;
        try {
            java.util.Set<Object> cached = redisTemplate.opsForSet().members(key);
            if (cached != null) {
                return cached.stream()
                    .map(Object::toString)
                    .collect(java.util.stream.Collectors.toSet());
            }
        } catch (Exception e) {
            log.warn("从缓存读取用户权限失败：userId={}", userId, e);
        }
        return null;
    }
    
    /**
     * 缓存用户权限（旧版本，基于 permission_code 字符串）
     * @deprecated 使用 cacheUserPermissions 代替
     */
    @Deprecated
    public void cacheUserPermissions(Long userId, java.util.List<String> permissions) {
        String key = "user:permissions:" + userId;
        try {
            redisTemplate.opsForSet().add(key, permissions.toArray());
            redisTemplate.expire(key, 2, java.util.concurrent.TimeUnit.HOURS);
            log.debug("缓存用户权限：userId={}, 权限数={}", userId, permissions.size());
        } catch (Exception e) {
            log.warn("缓存用户权限失败：userId={}", userId, e);
        }
    }
    
    /**
     * 获取用户角色缓存（旧版本，基于 role_code 字符串）
     * @deprecated 使用新的缓存方法代替
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public java.util.Set<String> getUserRolesFromCache(Long userId) {
        String key = "user:roles:" + userId;
        try {
            java.util.Set<Object> cached = redisTemplate.opsForSet().members(key);
            if (cached != null) {
                return cached.stream()
                    .map(Object::toString)
                    .collect(java.util.stream.Collectors.toSet());
            }
        } catch (Exception e) {
            log.warn("从缓存读取用户角色失败：userId={}", userId, e);
        }
        return null;
    }
    
    /**
     * 缓存用户角色（旧版本，基于 role_code 字符串）
     * @deprecated 使用新的缓存方法代替
     */
    @Deprecated
    public void cacheUserRoles(Long userId, java.util.List<String> roles) {
        String key = "user:roles:" + userId;
        try {
            redisTemplate.opsForSet().add(key, roles.toArray());
            redisTemplate.expire(key, 2, java.util.concurrent.TimeUnit.HOURS);
            log.debug("缓存用户角色：userId={}, 角色数={}", userId, roles.size());
        } catch (Exception e) {
            log.warn("缓存用户角色失败：userId={}", userId, e);
        }
    }
}
