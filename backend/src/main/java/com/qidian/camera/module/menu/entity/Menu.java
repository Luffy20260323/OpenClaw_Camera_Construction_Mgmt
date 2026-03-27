package com.qidian.camera.module.menu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 菜单实体
 */
@Data
@TableName("menus")
public class Menu {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String menuCode;
    
    private String menuName;
    
    private String menuPath;
    
    private Long parentId;
    
    private Integer sortOrder;
    
    private String icon;
    
    private Boolean isVisible;
    
    private String requiredPermission;
    
    private String description;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private Boolean isSystemProtected;
}
