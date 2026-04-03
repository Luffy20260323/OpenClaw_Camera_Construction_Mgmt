package com.qidian.camera.module.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建角色请求 DTO
 */
@Data
@Schema(description = "创建角色请求")
public class CreateRoleRequest {

    @NotBlank(message = "角色名称不能为空")
    @Schema(description = "角色名称", example = "甲方管理员")
    private String roleName;

    @NotBlank(message = "角色编码不能为空")
    @Schema(description = "角色编码", example = "jiafang_admin")
    private String roleCode;

    @Schema(description = "角色描述")
    private String roleDescription;

    @NotNull(message = "公司类型不能为空")
    @Schema(description = "公司类型 ID", example = "1")
    private Long companyTypeId;
    
    @Schema(description = "角色类型：SYSTEM/DEFAULT/PRESET", example = "DEFAULT")
    private String type;
}
