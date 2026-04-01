package com.qidian.camera.module.pointdevicemodel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 点位设备模型项实体
 */
@Data
@TableName("point_device_model_items")
public class PointDeviceModelItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 模型项 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联模型 ID
     */
    private Long modelId;

    /**
     * 零部件种类 ID
     */
    private Long componentTypeId;

    /**
     * 数量
     */
    private Integer quantity;

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
