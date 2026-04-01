package com.qidian.camera.module.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 元素 - 权限关联实体
 * 对应表：element_permissions
 * 用于控制页面元素的显示/隐藏
 */
@Data
@TableName("element_permissions")
public class ElementPermission {
    
    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 元素 ID
     */
    private Long elementId;
    
    /**
     * 权限 ID
     */
    private Long permissionId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
