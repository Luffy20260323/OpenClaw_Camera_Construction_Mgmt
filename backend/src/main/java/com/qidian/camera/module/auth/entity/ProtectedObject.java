package com.qidian.camera.module.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 保护对象实体
 * 对应表：protected_objects
 * 用于标记核心数据对象不可删除/修改
 */
@Data
@TableName("protected_objects")
public class ProtectedObject {
    
    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 对象类型：ROLE, USER, PERMISSION
     */
    private String objectType;
    
    /**
     * 对象 ID
     */
    private Long objectId;
    
    /**
     * 对象名称
     */
    private String objectName;
    
    /**
     * 保护规则：NO_DELETE, NO_MODIFY, NO_TRANSFER
     */
    private String protectionRules;
    
    /**
     * 保护者
     */
    private String protectedBy;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
