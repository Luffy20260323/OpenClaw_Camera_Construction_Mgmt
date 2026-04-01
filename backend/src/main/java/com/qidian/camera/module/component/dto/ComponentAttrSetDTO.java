package com.qidian.camera.module.component.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 零部件属性集 DTO
 */
@Data
public class ComponentAttrSetDTO {

    /**
     * 属性集 ID
     */
    private Long id;

    /**
     * 关联零部件种类 ID
     */
    private Long componentTypeId;

    /**
     * 零部件种类名称（非数据库字段）
     */
    private String componentTypeName;

    /**
     * 属性集名称
     */
    private String name;

    /**
     * 属性集编码
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
     * 属性定义列表（获取详情时包含）
     */
    private List<ComponentAttrSetAttrDTO> attributes;
}
