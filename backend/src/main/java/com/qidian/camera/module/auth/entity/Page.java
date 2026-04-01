package com.qidian.camera.module.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 页面实体
 * 对应表：pages
 * 用于记录系统所有页面
 */
@Data
@TableName("pages")
public class Page {
    
    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 页面代码，如：user_list, role_detail
     */
    private String pageCode;
    
    /**
     * 页面名称
     */
    private String pageName;
    
    /**
     * 页面路径（URL），如：/user/list
     */
    private String pagePath;
    
    /**
     * Vue 组件路径，如：@/views/user/List.vue
     */
    private String pageComponent;
    
    /**
     * 父页面 ID（用于页面层级）
     */
    private Long parentPageId;
    
    /**
     * 页面描述
     */
    private String pageDescription;
    
    /**
     * 排序顺序
     */
    private Integer sortOrder;
    
    /**
     * 所属模块
     */
    private String moduleCode;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
