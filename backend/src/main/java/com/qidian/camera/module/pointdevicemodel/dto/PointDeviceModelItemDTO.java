package com.qidian.camera.module.pointdevicemodel.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 点位设备模型项 DTO
 */
@Data
public class PointDeviceModelItemDTO {

    /**
     * 模型项 ID
     */
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
     * 零部件种类名称（非数据库字段）
     */
    private String componentTypeName;

    /**
     * 零部件种类编码（非数据库字段）
     */
    private String componentTypeCode;

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
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
