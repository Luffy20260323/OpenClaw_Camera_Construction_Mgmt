package com.qidian.camera.module.workarea.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新作业区请求 DTO
 */
@Data
@Schema(description = "更新作业区请求")
public class UpdateWorkAreaRequest {

    @NotBlank(message = "作业区名称不能为空")
    @Schema(description = "作业区名称", example = "朝阳作业区")
    private String workAreaName;

    @Schema(description = "作业区编码", example = "CY001")
    private String workAreaCode;

    @NotNull(message = "所属公司不能为空")
    @Schema(description = "公司 ID", example = "1")
    private Long companyId;

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
}
