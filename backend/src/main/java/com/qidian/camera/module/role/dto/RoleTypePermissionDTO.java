package com.qidian.camera.module.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 角色类型缺省权限 DTO
 */
@Data
@Schema(description = "角色类型缺省权限")
public class RoleTypePermissionDTO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "角色类型")
    private String roleType;

    @Schema(description = "资源 ID")
    private Long resourceId;

    @Schema(description = "权限码")
    private String permissionKey;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "资源名称")
    private String resourceName;

    @Schema(description = "资源类型")
    private String resourceType;

    @Schema(description = "模块编码")
    private String moduleCode;
}
