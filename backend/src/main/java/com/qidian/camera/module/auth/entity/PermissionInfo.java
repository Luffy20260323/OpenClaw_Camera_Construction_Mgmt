package com.qidian.camera.module.auth.entity;

import lombok.Data;

/**
 * 权限信息
 */
@Data
public class PermissionInfo {
    private Long id;
    private String permissionCode;
    private String permissionName;
    private String description;
}
