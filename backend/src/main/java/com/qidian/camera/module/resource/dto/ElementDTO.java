package com.qidian.camera.module.resource.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * ELEMENT 资源 DTO - 用于 ELEMENT 的 CRUD 操作
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ElementDTO {
    
    /**
     * 元素 ID
     */
    private Long id;
    
    /**
     * 元素名称
     */
    private String name;
    
    /**
     * 元素编码
     */
    private String code;
    
    /**
     * 所属页面 ID
     */
    private Long pageId;
    
    /**
     * 所属页面名称
     */
    private String pageName;
    
    /**
     * 权限标识（格式：module:resource:action:button）
     */
    private String permissionKey;
    
    /**
     * 元素类型（button, link, icon, menu 等）
     */
    private String elementType;
    
    /**
     * 排序号
     */
    private Integer sortOrder;
    
    /**
     * 状态：1=启用，0=禁用
     */
    private Integer status;
    
    /**
     * 所属模块编码
     */
    private String moduleCode;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 关联的子页面 ID（点击该元素后跳转的页面）
     */
    private Long childPageId;
    
    /**
     * 关联的子页面名称
     */
    private String childPageName;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
