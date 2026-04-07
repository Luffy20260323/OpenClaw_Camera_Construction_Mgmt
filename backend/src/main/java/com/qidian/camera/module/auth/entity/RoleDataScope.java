package com.qidian.camera.module.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色数据范围实体
 * 对应表：role_data_scope
 * 定义角色对数据资源的访问范围
 */
@Data
@TableName("role_data_scope")
public class RoleDataScope {
    
    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 角色 ID
     */
    private Long roleId;
    
    /**
     * 数据范围类型：ALL(全部)/DEPT(本部门)/DEPT_AND_SUB(本部门及下级)/SELF(仅本人)/CUSTOM(自定义)
     */
    private String scopeType;
    
    /**
     * 自定义部门 ID 列表（JSON 数组）
     */
    private String customDeptIds;
    
    /**
     * 资源 ID（关联 resource.id）
     */
    private Long resourceId;
    
    /**
     * 权限标识
     */
    private String permissionKey;
    
    /**
     * 状态：1=启用，0=禁用
     */
    private Integer status;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 数据范围类型常量
     */
    public static final String SCOPE_ALL = "ALL";
    public static final String SCOPE_DEPT = "DEPT";
    public static final String SCOPE_DEPT_AND_SUB = "DEPT_AND_SUB";
    public static final String SCOPE_SELF = "SELF";
    public static final String SCOPE_CUSTOM = "CUSTOM";
}
