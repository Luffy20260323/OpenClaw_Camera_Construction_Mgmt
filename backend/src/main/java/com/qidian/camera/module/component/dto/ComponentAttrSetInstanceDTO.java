package com.qidian.camera.module.component.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 零部件属性集实例 DTO
 */
@Data
@Schema(description = "零部件属性集实例 DTO")
public class ComponentAttrSetInstanceDTO {

    /**
     * 实例 ID
     */
    @Schema(description = "实例 ID")
    private Long id;

    /**
     * 关联属性集 ID
     */
    @Schema(description = "关联属性集 ID")
    private Long attrSetId;

    /**
     * 属性集名称
     */
    @Schema(description = "属性集名称")
    private String attrSetName;

    /**
     * 零部件种类 ID
     */
    @Schema(description = "零部件种类 ID")
    private Long componentTypeId;

    /**
     * 零部件种类名称
     */
    @Schema(description = "零部件种类名称")
    private String componentTypeName;

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
     * 创建人 ID
     */
    @Schema(description = "创建人 ID")
    private Long createdBy;

    /**
     * 创建人姓名
     */
    @Schema(description = "创建人姓名")
    private String creatorName;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
