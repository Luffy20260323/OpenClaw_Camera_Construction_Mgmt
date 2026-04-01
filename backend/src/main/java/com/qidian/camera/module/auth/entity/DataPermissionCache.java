package com.qidian.camera.module.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据权限缓存实体
 * 对应表：data_permission_cache
 * 用于缓存用户数据权限查询结果
 */
@Data
@TableName("data_permission_cache")
public class DataPermissionCache {
    
    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户 ID
     */
    private Long userId;
    
    /**
     * 缓存键（用于快速查找）
     */
    private String cacheKey;
    
    /**
     * 缓存数据（JSON 格式）
     */
    private String cacheData;
    
    /**
     * 缓存版本号（用于失效）
     */
    private Integer cacheVersion;
    
    /**
     * 过期时间
     */
    private LocalDateTime expiresAt;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
