package com.qidian.camera.module.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 验证码响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "验证码响应")
public class CaptchaResponse {

    @Schema(description = "验证码 ID（用于验证）", example = "captcha_123456")
    private String captchaId;

    @Schema(description = "验证码图片 Base64（仅图形验证码）", example = "data:image/png;base64,iVBORw0KG...")
    private String imageBase64;

    @Schema(description = "是否已发送（仅手机验证码）", example = "true")
    private Boolean sent;

    @Schema(description = "过期时间（秒）", example = "300")
    private Long expiresIn;
}
