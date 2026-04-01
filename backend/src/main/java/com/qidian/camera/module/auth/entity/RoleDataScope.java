package com.qidian.camera.module.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色数据权限实体
 * 对应表：role_data_scope
 * 定义角色的数据访问范围
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
     * 资源 ID
     */
    private Long resourceId;
    
    /**
     * 范围类型：SELF/DEPT/DEPT_AND_SUB/ALL
     */
    private String scopeType;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 范围类型常量
     */
    public static final String SCOPE_SELF = "SELF";
    public static final String SCOPE_DEPT = "DEPT";
    public static final String SCOPE_DEPT_AND_SUB = "DEPT_AND_SUB";
    public static final String SCOPE_ALL = "ALL";
}