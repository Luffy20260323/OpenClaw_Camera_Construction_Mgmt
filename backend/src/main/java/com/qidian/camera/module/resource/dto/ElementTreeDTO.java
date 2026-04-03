package com.qidian.camera.module.resource.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

/**
 * ELEMENT 树 DTO - 用于展示 ELEMENT → 子 PAGE 的层级结构
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ElementTreeDTO {
    
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
     * 权限标识
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
     * 关联的子页面信息
     */
    private ElementPageDTO childPage;
    
    /**
     * 子元素列表（如果该元素是菜单类型）
     */
    private List<ElementTreeDTO> children;
    
    /**
     * 是否有子节点
     */
    private Boolean hasChildren;
    
    /**
     * 子页面 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ElementPageDTO {
        
        /**
         * 页面 ID
         */
        private Long id;
        
        /**
         * 页面名称
         */
        private String name;
        
        /**
         * 页面编码
         */
        private String code;
        
        /**
         * 页面路径
         */
        private String path;
        
        /**
         * 页面组件
         */
        private String component;
        
        /**
         * 权限标识
         */
        private String permissionKey;
    }
}
