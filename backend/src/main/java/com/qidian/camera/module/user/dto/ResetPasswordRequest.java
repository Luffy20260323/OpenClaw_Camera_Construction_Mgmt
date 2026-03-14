package com.qidian.camera.module.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 重置密码请求 DTO
 */
@Data
@Schema(description = "重置密码请求")
public class ResetPasswordRequest {

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, message = "密码至少 6 位")
    @Schema(description = "新密码", example = "123456")
    private String newPassword;

    @NotBlank(message = "确认密码不能为空")
    @Schema(description = "确认密码", example = "123456")
    private String confirmPassword;
}
