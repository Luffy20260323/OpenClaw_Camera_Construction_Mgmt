package com.qidian.camera.module.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 权限缓存配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "permission.cache")
public class PermissionCacheConfig {
    
    /**
     * 是否启用权限缓存
     * 默认：true
     */
    private boolean enabled = true;
    
    /**
     * 用户权限缓存 TTL（分钟）
     * 默认：30
     */
    private long userTtlMinutes = 30;
    
    /**
     * 角色权限缓存 TTL（分钟）
     * 默认：60
     */
    private long roleTtlMinutes = 60;
}
