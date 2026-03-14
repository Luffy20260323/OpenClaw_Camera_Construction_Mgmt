package com.qidian.camera.module.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 验证码生成请求 DTO
 */
@Data
@Schema(description = "验证码生成请求")
public class CaptchaRequest {

    @Schema(description = "验证码类型：image-图形验证码，sms-手机验证码", example = "image")
    private String type;

    @Schema(description = "手机号（仅手机验证码需要）", example = "13800138000")
    private String phone;
}
