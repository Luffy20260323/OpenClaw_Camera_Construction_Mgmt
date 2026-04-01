package com.qidian.camera.module.auth.dto;

import lombok.Data;

/**
 * 资源 DTO
 */
@Data
public class ResourceDTO {
    
    /**
     * 资源名称
     */
    private String name;
    
    /**
     * 资源编码
     */
    private String code;
    
    /**
     * 资源类型
     */
    private String type;
    
    /**
     * 父资源 ID
     */
    private Long parentId;
    
    /**
     * 权限标识
     */
    private String permissionKey;
    
    /**
     * URI 匹配模式
     */
    private String uriPattern;
    
    /**
     * HTTP 方法
     */
    private String method;
    
    /**
     * 图标
     */
    private String icon;
    
    /**
     * 前端路由路径
     */
    private String path;
    
    /**
     * 前端组件路径
     */
    private String component;
    
    /**
     * 排序号
     */
    private Integer sortOrder;
    
    /**
     * 是否基本权限
     */
    private Integer isBasic;
}