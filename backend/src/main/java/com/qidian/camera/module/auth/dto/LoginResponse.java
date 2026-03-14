package com.qidian.camera.module.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录响应")
public class LoginResponse {

    @Schema(description = "访问令牌")
    private String accessToken;

    @Schema(description = "刷新令牌")
    private String refreshToken;

    @Schema(description = "令牌类型", example = "Bearer")
    private String tokenType;

    @Schema(description = "过期时间（秒）", example = "86400")
    private Long expiresIn;

    @Schema(description = "用户信息")
    private UserInfo userInfo;

    /**
     * 用户信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "用户信息")
    public static class UserInfo {
        @Schema(description = "用户 ID")
        private Long userId;

        @Schema(description = "用户名")
        private String username;

        @Schema(description = "真实姓名")
        private String realName;

        @Schema(description = "邮箱")
        private String email;

        @Schema(description = "手机号")
        private String phone;

        @Schema(description = "公司名称")
        private String companyName;

        @Schema(description = "公司 ID")
        private Long companyId;

        @Schema(description = "公司类型 ID")
        private Integer companyTypeId;

        @Schema(description = "角色列表")
        private java.util.List<String> roles;

        @Schema(description = "权限列表")
        private java.util.List<String> permissions;
    }
}
