package com.qidian.camera.module.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户数据权限实体
 * 对应表：user_data_permissions
 * 用于用户级别的数据范围控制
 */
@Data
@TableName("user_data_permissions")
public class UserDataPermission {
    
    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户 ID
     */
    private Long userId;
    
    /**
     * 模板 ID
     */
    private Long templateId;
    
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
     * 是否固定（不可自动变更）
     */
    private Boolean isFixed;
    
    /**
     * 来源：MANUAL(手动), ROLE(角色继承), TEMPLATE(模板)
     */
    private String source;
    
    /**
     * 授权人 ID
     */
    private Long grantedBy;
    
    /**
     * 授权时间
     */
    private LocalDateTime grantedAt;
    
    /**
     * 过期时间（NULL 为永久）
     */
    private LocalDateTime expiresAt;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
