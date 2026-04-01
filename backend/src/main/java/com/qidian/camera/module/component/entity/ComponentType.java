package com.qidian.camera.module.component.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 零部件种类实体
 */
@Data
@TableName("component_types")
public class ComponentType implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 种类 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 种类名称
     */
    private String name;

    /**
     * 种类编码
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
     * 创建人
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
