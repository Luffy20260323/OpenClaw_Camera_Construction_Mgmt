package com.qidian.camera.module.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据权限模板实体
 * 对应表：data_permission_templates
 * 用于可复用的数据权限配置
 */
@Data
@TableName("data_permission_templates")
public class DataPermissionTemplate {
    
    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 模板代码
     */
    private String templateCode;
    
    /**
     * 模板名称
     */
    private String templateName;
    
    /**
     * 数据范围类型：ALL, COMPANY, WORKAREA, SELF
     */
    private String dataScopeType;
    
    /**
     * 关联公司 ID（当 scope 为 COMPANY 时）
     */
    private Long companyId;
    
    /**
     * 作业区 ID 列表（逗号分隔，当 scope 为 WORKAREA 时）
     */
    private String workareaIds;
    
    /**
     * 模板描述
     */
    private String templateDescription;
    
    /**
     * 使用次数（被多少个用户引用）
     */
    private Integer usageCount;
    
    /**
     * 是否系统内置（不可删除）
     */
    private Boolean isSystem;
    
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
