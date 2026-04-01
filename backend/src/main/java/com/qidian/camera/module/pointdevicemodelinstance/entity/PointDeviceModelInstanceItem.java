package com.qidian.camera.module.pointdevicemodelinstance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 点位设备模型实例项实体
 */
@Data
@TableName("point_device_model_instance_items")
public class PointDeviceModelInstanceItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 实例项 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联实例 ID
     */
    private Long instanceId;

    /**
     * 零部件种类 ID
     */
    private Long componentTypeId;

    /**
     * 零部件实例 ID（具体的设备实例）
     */
    private Long componentInstanceId;

    /**
     * 显示序号
     */
    private Integer sequenceNo;

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
