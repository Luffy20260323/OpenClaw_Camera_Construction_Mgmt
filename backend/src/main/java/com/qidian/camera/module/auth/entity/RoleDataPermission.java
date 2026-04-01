package com.qidian.camera.module.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色数据权限实体
 * 对应表：role_data_permissions
 * 用于角色级别的数据范围控制
 */
@Data
@TableName("role_data_permissions")
public class RoleDataPermission {
    
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
     * 数据范围类型：ALL, DEPT, DEPT_AND_SUB, SELF
     */
    private String dataScopeType;
    
    /**
     * 关联部门 ID
     */
    private Long deptId;
    
    /**
     * 部门 ID 列表（逗号分隔）
     */
    private String deptIds;
    
    /**
     * 自定义数据范围（JSON 格式）
     */
    private String customScope;
    
    /**
     * 是否从上级角色继承
     */
    private Boolean isInherited;
    
    /**
     * 继承自哪个角色
     */
    private Long inheritedFromRoleId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
