package com.qidian.camera.module.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

/**
 * 管理员更新用户信息请求 DTO
 */
@Data
@Schema(description = "管理员更新用户信息请求")
public class UpdateUserRequest {

    @Schema(description = "真实姓名", example = "张三")
    private String realName;

    @Schema(description = "性别（0:未知 1:男 2:女）", example = "1")
    private Integer gender;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "手机号", example = "13800138000")
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "审批状态（0:待审批 1:已通过 2:已拒绝）", example = "1")
    private Integer approvalStatus;

    @Schema(description = "拒绝原因", example = "资料不完整")
    private String rejectionReason;

    @Schema(description = "用户状态（0:禁用 1:正常）", example = "1")
    private Integer status;

    @Schema(description = "角色 ID 列表")
    private List<Long> roleIds;

    @Schema(description = "作业区 ID 列表")
    private List<Long> workAreaIds;
}
