package com.qidian.camera.module.menu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户菜单权限实体
 */
@Data
@TableName("user_menu_permissions")
public class UserMenuPermission {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private Long menuId;
    
    private Boolean canView;
    
    private Boolean canOperate;
    
    private Long grantedBy;
    
    private LocalDateTime grantedAt;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
