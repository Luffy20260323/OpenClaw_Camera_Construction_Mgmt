package com.qidian.camera.module.company.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公司类型实体
 */
@Data
@TableName("company_types")
public class CompanyType implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 公司类型 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 类型名称
     */
    private String typeName;

    /**
     * 描述
     */
    private String description;

    /**
     * 是否系统保护
     */
    private Boolean isSystemProtected;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
