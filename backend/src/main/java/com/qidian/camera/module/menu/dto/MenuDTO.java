package com.qidian.camera.module.menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 菜单 DTO
 */
@Data
@Builder
@Schema(description = "菜单信息")
public class MenuDTO {
    
    @Schema(description = "菜单 ID")
    private Long id;
    
    @Schema(description = "菜单编码")
    private String menuCode;
    
    @Schema(description = "菜单名称")
    private String menuName;
    
    @Schema(description = "菜单路径")
    private String menuPath;
    
    @Schema(description = "父菜单 ID")
    private Long parentId;
    
    @Schema(description = "排序")
    private Integer sortOrder;
    
    @Schema(description = "图标")
    private String icon;
    
    @Schema(description = "是否可见")
    private Boolean isVisible;
    
    @Schema(description = "所需权限")
    private String requiredPermission;
    
    @Schema(description = "描述")
    private String description;
    
    @Schema(description = "用户是否有查看权限")
    private Boolean userCanView;
    
    @Schema(description = "用户是否有操作权限")
    private Boolean userCanOperate;
    
    @Schema(description = "子菜单列表")
    private java.util.List<MenuDTO> children;
}
