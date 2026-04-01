package com.qidian.camera.module.component.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 零部件实例实体
 */
@Data
@TableName("component_instances")
@Schema(description = "零部件实例")
public class ComponentInstance implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 零部件实例 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "零部件实例 ID")
    private Long id;

    /**
     * 零部件种类 ID
     */
    @Schema(description = "零部件种类 ID")
    private Long componentTypeId;

    /**
     * 属性集实例 ID（可选）
     */
    @Schema(description = "属性集实例 ID")
    private Long attrSetInstanceId;

    /**
     * 序列号
     */
    @Schema(description = "序列号")
    private String serialNumber;

    /**
     * 状态：normal-正常使用，replaced-已更换，scrapped-已报废
     */
    @Schema(description = "状态：normal-正常使用，replaced-已更换，scrapped-已报废")
    private String status;

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
