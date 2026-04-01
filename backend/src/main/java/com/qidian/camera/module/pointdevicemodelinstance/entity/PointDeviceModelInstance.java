package com.qidian.camera.module.pointdevicemodelinstance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 点位设备模型实例实体
 */
@Data
@TableName("point_device_model_instances")
public class PointDeviceModelInstance implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 实例 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联模型 ID
     */
    private Long modelId;

    /**
     * 实例名称
     */
    private String name;

    /**
     * 实例编码
     */
    private String code;

    /**
     * 描述
     */
    private String description;

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

    /**
     * 实例项列表（非数据库字段，用于关联查询）
     */
    @TableField(exist = false)
    private List<PointDeviceModelInstanceItem> items;
}
