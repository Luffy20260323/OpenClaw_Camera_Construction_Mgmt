package com.qidian.camera.module.auth.dto;

import lombok.Data;

/**
 * 权限信息 DTO
 */
@Data
public class PermissionDTO {
    private Long id;
    private String permissionCode;
    private String permissionName;
    private String description;
}
