package com.qidian.camera.module.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 更新用户信息请求 DTO
 */
@Data
@Schema(description = "更新用户信息请求")
public class UpdateUserProfileRequest {

    @NotBlank(message = "姓名不能为空")
    @Schema(description = "真实姓名", example = "张三")
    private String realName;

    @Schema(description = "性别（0:未知 1:男 2:女）", example = "1")
    private Integer gender;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;
}
