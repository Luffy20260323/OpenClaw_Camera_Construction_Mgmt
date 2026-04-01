package com.qidian.camera.module.pointdevicemodel.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 点位设备模型 DTO
 */
@Data
public class PointDeviceModelDTO {

    /**
     * 模型 ID
     */
    private Long id;

    /**
     * 模型名称
     */
    private String name;

    /**
     * 模型编码
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
     * 创建人 ID
     */
    private Long createdBy;

    /**
     * 创建人姓名（非数据库字段）
     */
    private String creatorName;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 模型项列表（获取详情时包含）
     */
    private List<PointDeviceModelItemDTO> items;
}
