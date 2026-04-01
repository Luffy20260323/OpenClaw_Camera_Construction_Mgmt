package com.qidian.camera.module.auth.dto;

import lombok.Data;

/**
 * 角色权限调整 DTO
 */
@Data
public class RolePermissionAdjustDTO {
    
    /**
     * 资源 ID
     */
    private Long resourceId;
    
    /**
     * 操作类型：ADD/REMOVE
     */
    private String action;
}