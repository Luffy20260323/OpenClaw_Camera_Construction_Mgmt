package com.qidian.camera.module.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 用户信息 DTO
 */
@Data
@Schema(description = "用户信息")
public class UserProfileDTO {

    @Schema(description = "用户 ID", example = "1")
    private Long userId;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "真实姓名", example = "张三")
    private String realName;

    @Schema(description = "性别（0:未知 1:男 2:女）", example = "1")
    private Integer gender;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "公司名称")
    private String companyName;

    @Schema(description = "公司类型")
    private String companyType;

    @Schema(description = "公司类型 ID（1:甲方 2:乙方 3:监理 4:系统）")
    private Integer companyTypeId;

    @Schema(description = "角色列表")
    private java.util.List<String> roles;

    @Schema(description = "作业区列表（仅甲方公司显示）")
    private java.util.List<WorkAreaInfo> workAreas;

    /**
     * 作业区信息
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @Schema(description = "作业区信息")
    public static class WorkAreaInfo {
        @Schema(description = "作业区 ID")
        private Long id;
        @Schema(description = "作业区名称")
        private String workAreaName;
        @Schema(description = "作业区编码")
        private String workAreaCode;
        @Schema(description = "是否主要作业区")
        private Boolean isPrimary;
    }
}
