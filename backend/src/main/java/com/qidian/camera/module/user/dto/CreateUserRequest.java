package com.qidian.camera.module.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 管理员创建用户请求
 */
@Data
public class CreateUserRequest {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在 3-50 之间")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 50, message = "密码长度必须在 6-50 之间")
    private String password;

    /**
     * 确认密码
     */
    private String confirmPassword;

    /**
     * 真实姓名
     */
    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 公司 ID
     */
    private Long companyId;

    /**
     * 角色 ID 列表
     */
    private List<Long> roleIds;

    /**
     * 性别（0:未知 1:男 2:女）
     */
    private Integer gender;

    /**
     * 是否直接通过审批（仅管理员创建时使用）
     */
    private Boolean autoApprove;
}
