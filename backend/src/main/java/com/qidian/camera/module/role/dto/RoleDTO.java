package com.qidian.camera.module.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 角色信息 DTO
 */
@Data
@Schema(description = "角色信息")
public class RoleDTO {

    @Schema(description = "角色 ID", example = "1")
    private Long id;

    @Schema(description = "角色名称", example = "甲方管理员")
    private String roleName;

    @Schema(description = "角色编码", example = "jiafang_admin")
    private String roleCode;

    @Schema(description = "角色描述")
    private String roleDescription;

    @Schema(description = "公司类型 ID", example = "1")
    private Long companyTypeId;

    @Schema(description = "公司类型名称", example = "甲方公司")
    private String companyTypeName;

    @Schema(description = "是否系统保护", example = "false")
    private Boolean isSystemProtected;
    
    @Schema(description = "角色类型：SYSTEM/DEFAULT/PRESET", example = "DEFAULT")
    private String type;
}
