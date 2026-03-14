package com.qidian.camera.module.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 登录请求 DTO
 */
@Data
@Schema(description = "登录请求")
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", example = "admin")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "Admin@2026")
    private String password;

    @Schema(description = "验证码（可选）", example = "1234")
    private String captcha;

    @Schema(description = "验证码 ID（图形验证码）或手机号（手机验证码）", example = "abc123")
    private String captchaId;

    @Schema(description = "验证码类型：image-图形验证码，sms-手机验证码", example = "image")
    private String captchaType;
}
