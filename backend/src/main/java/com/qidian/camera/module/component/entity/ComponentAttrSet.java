package com.qidian.camera.module.component.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 零部件属性集实体
 */
@Data
@TableName("component_attr_sets")
public class ComponentAttrSet implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 属性集 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联零部件种类 ID
     */
    private Long componentTypeId;

    /**
     * 属性集名称
     */
    private String name;

    /**
     * 属性集编码
     */
    private String code;

    /**
     * 描述
     */
    private String description;

    /**
     * 显示序号
     */
    private Integer sequenceNo;

    /**
     * 是否启用
     */
    private Boolean isActive;

    /**
     * 创建人 ID
     */
    private Long createdBy;

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
