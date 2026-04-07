package com.qidian.camera.module.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 资源实体
 * 对应表：resource
 * 统一抽象所有需要权限控制的对象
 */
@Data
@TableName("resource")
public class Resource {
    
    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 资源名称
     */
    private String name;
    
    /**
     * 资源编码（唯一）
     */
    private String code;
    
    /**
     * 资源类型：MODULE/MENU/PAGE/ELEMENT/API/PERMISSION
     */
    private String type;
    
    /**
     * 父资源 ID，形成树形结构
     */
    private Long parentId;
    
    /**
     * 权限标识（唯一）
     */
    private String permissionKey;
    
    /**
     * URI 匹配模式，用于 API 资源
     */
    private String uriPattern;
    
    /**
     * HTTP 方法：GET/POST/PUT/DELETE
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
     * 状态：1=启用, 0=禁用
     */
    private Integer status;
    
    /**
     * 所需权限（用于菜单资源）
     */
    private String requiredPermission;
    
    /**
     * 是否基本权限：1=是, 0=否
     */
    private Integer isBasic;
    
    /**
     * 所属模块编码
     */
    private String moduleCode;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 是否顶级资源
     * 顶级资源的父资源必须为空，不被认定为孤儿资源
     * 只有 MODULE 类型才允许设置为顶级
     */
    private Boolean isTopLevel;
    
    /**
     * 文件路径
     * PAGE/ELEMENT/API 资源的源代码文件相对项目目录的完整路径
     * 便于排错
     */
    private String filePath;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 资源类型常量
     */
    public static final String TYPE_MODULE = "MODULE";
    public static final String TYPE_MENU = "MENU";
    public static final String TYPE_PAGE = "PAGE";
    public static final String TYPE_ELEMENT = "ELEMENT";
    public static final String TYPE_API = "API";
    public static final String TYPE_PERMISSION = "PERMISSION";
}