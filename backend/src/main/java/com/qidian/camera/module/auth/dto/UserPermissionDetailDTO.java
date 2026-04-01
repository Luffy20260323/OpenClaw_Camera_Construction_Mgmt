package com.qidian.camera.module.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户权限详情 DTO（包含权限来源追溯信息）
 */
@Data
@Schema(description = "用户权限详情信息（包含来源追溯）")
public class UserPermissionDetailDTO {
    
    @Schema(description = "资源 ID")
    private Long resourceId;
    
    @Schema(description = "资源名称")
    private String resourceName;
    
    @Schema(description = "资源编码")
    private String resourceCode;
    
    @Schema(description = "资源类型：MENU(菜单)/API(API)/BUTTON(按钮)")
    private String resourceType;
    
    @Schema(description = "权限标识")
    private String permissionKey;
    
    @Schema(description = "权限来源：BASIC(基本权限)/DEFAULT(缺省权限)/ADJUSTMENT(调整权限)")
    private String permissionSource;
    
    @Schema(description = "来源详情：角色名称或调整类型")
    private String sourceDetail;
    
    @Schema(description = "调整类型：ADD(增加)/REMOVE(移除)，仅当来源为 ADJUSTMENT 时有值")
    private String adjustmentType;
    
    @Schema(description = "是否启用")
    private Boolean enabled;
    
    @Schema(description = "所属角色名称（当来源为 BASIC 或 DEFAULT 时）")
    private String roleName;
}
