package com.qidian.camera.module.component.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 零部件种类 DTO
 */
@Data
public class ComponentTypeDTO {

    /**
     * 种类 ID
     */
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
     * 创建人 ID
     */
    private Long createdBy;

    /**
     * 创建人姓名
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
}
