package com.qidian.camera.module.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 权限-API 关联实体
 * 对应表：permission_apis
 * 用于明确权限与 API 的对应关系
 */
@Data
@TableName("permission_apis")
public class PermissionApi {
    
    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 权限 ID
     */
    private Long permissionId;
    
    /**
     * API ID
     */
    private Long apiId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
