package com.qidian.camera.module.workarea.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 作业区信息 DTO
 */
@Data
@Schema(description = "作业区信息")
public class WorkAreaDTO {

    @Schema(description = "作业区 ID", example = "1")
    private Long id;

    @Schema(description = "作业区名称", example = "朝阳作业区")
    private String workAreaName;

    @Schema(description = "作业区编码", example = "CY001")
    private String workAreaCode;

    @Schema(description = "公司 ID", example = "1")
    private Long companyId;

    @Schema(description = "公司名称")
    private String companyName;

    @Schema(description = "负责人姓名", example = "张三")
    private String leaderName;

    @Schema(description = "负责人电话", example = "13800138000")
    private String leaderPhone;

    @Schema(description = "地理范围")
    private String geographicRange;

    @Schema(description = "最大容量", example = "1000")
    private Integer maxCapacity;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "是否系统保护", example = "false")
    private Boolean isSystemProtected;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
