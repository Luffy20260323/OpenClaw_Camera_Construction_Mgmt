package com.qidian.camera.module.menu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色菜单权限实体
 */
@Data
@TableName("role_menu_permissions")
public class RoleMenuPermission {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long roleId;
    
    private Long menuId;
    
    private Boolean canView;
    
    private Boolean canOperate;
    
    private LocalDateTime createdAt;
}
