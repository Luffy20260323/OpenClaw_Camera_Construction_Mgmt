package com.qidian.camera.module.component.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 零部件实例 DTO
 */
@Data
@Schema(description = "零部件实例 DTO")
public class ComponentInstanceDTO {

    /**
     * 零部件实例 ID
     */
    @Schema(description = "零部件实例 ID")
    private Long id;

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
     * 属性集实例 ID
     */
    @Schema(description = "属性集实例 ID")
    private Long attrSetInstanceId;

    /**
     * 属性集实例名称
     */
    @Schema(description = "属性集实例名称")
    private String attrSetInstanceName;

    /**
     * 序列号
     */
    @Schema(description = "序列号")
    private String serialNumber;

    /**
     * 状态：normal-正常使用，replaced-已更换，scrapped-已报废
     */
    @Schema(description = "状态")
    private String status;

    /**
     * 状态标签
     */
    @Schema(description = "状态标签")
    private String statusLabel;

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
