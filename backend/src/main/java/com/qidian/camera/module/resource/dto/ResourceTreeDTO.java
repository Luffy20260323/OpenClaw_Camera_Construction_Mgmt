package com.qidian.camera.module.resource.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

/**
 * 资源树 DTO - 用于前端展示
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceTreeDTO {
    
    private Long id;
    private String name;
    private String code;
    private String type;
    private Long parentId;
    private String permissionKey;
    private String icon;
    private String path;
    private String component;
    private Integer sortOrder;
    private Integer status;
    private Integer isBasic;
    private String moduleCode;
    private String description;
    private String uriPattern;
    private String method;
    
    /**
     * 子节点列表
     */
    private List<ResourceTreeDTO> children;
    
    /**
     * 是否有子节点
     */
    private Boolean hasChildren;
    
    /**
     * 层级深度
     */
    private Integer level;
    
    /**
     * 层级路径名称
     */
    private String pathName;
}