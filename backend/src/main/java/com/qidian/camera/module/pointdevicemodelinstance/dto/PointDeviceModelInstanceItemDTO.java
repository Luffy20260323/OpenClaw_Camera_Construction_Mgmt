package com.qidian.camera.module.pointdevicemodelinstance.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 点位设备模型实例项 DTO
 */
@Data
public class PointDeviceModelInstanceItemDTO {

    /**
     * 实例项 ID
     */
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
     * 零部件种类名称（非数据库字段）
     */
    private String componentTypeName;

    /**
     * 零部件种类编码（非数据库字段）
     */
    private String componentTypeCode;

    /**
     * 零部件实例 ID（具体的设备实例）
     */
    private Long componentInstanceId;

    /**
     * 零部件实例名称（非数据库字段）
     */
    private String componentInstanceName;

    /**
     * 零部件实例编码（非数据库字段）
     */
    private String componentInstanceCode;

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
