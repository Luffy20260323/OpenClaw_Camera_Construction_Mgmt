package com.qidian.camera.module.menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 用户菜单权限 DTO
 */
@Data
@Builder
@Schema(description = "用户菜单权限信息")
public class UserMenuPermissionDTO {
    
    @Schema(description = "权限 ID")
    private Long id;
    
    @Schema(description = "用户 ID")
    private Long userId;
    
    @Schema(description = "用户名")
    private String username;
    
    @Schema(description = "用户姓名")
    private String realName;
    
    @Schema(description = "菜单 ID")
    private Long menuId;
    
    @Schema(description = "菜单编码")
    private String menuCode;
    
    @Schema(description = "菜单名称")
    private String menuName;
    
    @Schema(description = "是否可查看")
    private Boolean canView;
    
    @Schema(description = "是否可操作")
    private Boolean canOperate;
    
    @Schema(description = "授权人 ID")
    private Long grantedBy;
    
    @Schema(description = "授权人姓名")
    private String grantedByName;
    
    @Schema(description = "授权时间")
    private String grantedAt;
    
    @Schema(description = "权限来源：ROLE(角色默认) 或 CUSTOM(自定义)")
    private String permissionSource;
}
