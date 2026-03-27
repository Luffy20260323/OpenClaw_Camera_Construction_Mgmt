package com.qidian.camera.module.menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新用户菜单权限请求
 */
@Data
@Schema(description = "更新用户菜单权限请求")
public class UpdateUserMenuPermissionRequest {
    
    @NotNull(message = "用户 ID 不能为空")
    @Schema(description = "用户 ID", required = true)
    private Long userId;
    
    @NotNull(message = "菜单 ID 不能为空")
    @Schema(description = "菜单 ID", required = true)
    private Long menuId;
    
    @Schema(description = "是否可查看")
    private Boolean canView;
    
    @Schema(description = "是否可操作")
    private Boolean canOperate;
}
