package com.qidian.camera.module.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统配置 DTO
 */
@Data
@Schema(description = "系统配置")
public class SystemConfigDTO {

    @Schema(description = "配置 ID")
    private Long id;

    @Schema(description = "配置键", example = "captcha-type")
    private String configKey;

    @Schema(description = "配置值", example = "image")
    private String configValue;

    @Schema(description = "配置类型", example = "string")
    private String configType;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
