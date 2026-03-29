package com.qidian.camera.module.auth.dto;

import lombok.Data;
import java.util.List;

/**
 * 角色权限配置 DTO
 */
@Data
public class RolePermissionDTO {
    private Long roleId;
    private String roleName;
    private String roleCode;
    private List<Long> permissionIds;
}
