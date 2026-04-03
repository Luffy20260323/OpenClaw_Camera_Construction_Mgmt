package com.qidian.camera.module.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 角色类型缺省权限请求 DTO
 */
@Data
@Schema(description = "角色类型缺省权限请求")
public class RoleTypePermissionRequest {

    @Schema(description = "角色类型", required = true)
    private String roleType;

    @Schema(description = "资源 ID 列表", required = true)
    private List<Long> resourceIds;
}
