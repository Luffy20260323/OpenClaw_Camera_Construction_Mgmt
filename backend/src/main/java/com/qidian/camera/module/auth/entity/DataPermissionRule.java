package com.qidian.camera.module.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据权限规则实体
 * 对应表：data_permission_rules
 * 用于定义数据过滤规则
 */
@Data
@TableName("data_permission_rules")
public class DataPermissionRule {
    
    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 规则名称
     */
    private String ruleName;
    
    /**
     * 规则类型：SQL, EXPRESSION
     */
    private String ruleType;
    
    /**
     * 实体类型：user, company, workarea, project
     */
    private String entityType;
    
    /**
     * 规则表达式（SQL 片段或表达式）
     */
    private String ruleExpression;
    
    /**
     * 规则描述
     */
    private String ruleDescription;
    
    /**
     * 优先级（数字越大优先级越高）
     */
    private Integer priority;
    
    /**
     * 是否启用
     */
    private Boolean isEnabled;
    
    /**
     * 创建人 ID
     */
    private Long createdBy;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
