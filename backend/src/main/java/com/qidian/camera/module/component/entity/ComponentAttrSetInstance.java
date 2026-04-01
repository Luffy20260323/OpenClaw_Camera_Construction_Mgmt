package com.qidian.camera.module.component.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 零部件属性集实例实体
 */
@Data
@TableName("component_attr_set_instances")
@Schema(description = "零部件属性集实例")
public class ComponentAttrSetInstance implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 实例 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "实例 ID")
    private Long id;

    /**
     * 关联属性集 ID
     */
    @Schema(description = "关联属性集 ID")
    private Long attrSetId;

    /**
     * 实例名称
     */
    @Schema(description = "实例名称")
    private String name;

    /**
     * 属性值 JSON
     */
    @Schema(description = "属性值 JSON")
    private String attrValues;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private Long createdBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
