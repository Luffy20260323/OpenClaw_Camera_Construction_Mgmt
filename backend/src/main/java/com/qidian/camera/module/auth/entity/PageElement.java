package com.qidian.camera.module.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 页面元素实体
 * 对应表：page_elements
 * 用于记录页面中的按钮、表格等元素
 */
@Data
@TableName("page_elements")
public class PageElement {
    
    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 页面 ID
     */
    private Long pageId;
    
    /**
     * 元素代码
     */
    private String elementCode;
    
    /**
     * 元素名称
     */
    private String elementName;
    
    /**
     * 元素类型：button, table, form, dialog, menu, input...
     */
    private String elementType;
    
    /**
     * CSS 选择器或 Vue ref 名称
     */
    private String elementSelector;
    
    /**
     * 父元素 ID（用于元素层级）
     */
    private Long parentElementId;
    
    /**
     * 元素描述
     */
    private String elementDescription;
    
    /**
     * 排序顺序
     */
    private Integer sortOrder;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
