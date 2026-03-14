package com.qidian.camera.module.company.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新公司请求 DTO
 */
@Data
@Schema(description = "更新公司请求")
public class UpdateCompanyRequest {

    @NotBlank(message = "公司名称不能为空")
    @Schema(description = "公司名称", example = "北京其点技术服务有限公司")
    private String companyName;

    @NotNull(message = "公司类型不能为空")
    @Schema(description = "公司类型 ID", example = "1")
    private Long typeId;

    @Schema(description = "统一社会信用代码", example = "91110108MA01XXXXXX")
    private String unifiedSocialCreditCode;

    @Schema(description = "联系人", example = "张三")
    private String contactPerson;

    @Schema(description = "联系电话", example = "010-88888888")
    private String contactPhone;

    @Schema(description = "联系邮箱", example = "contact@qidian.com")
    private String contactEmail;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "状态（active:正常 inactive:禁用）", example = "active")
    private String status;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "是否允许匿名注册", example = "false")
    private Boolean allowAnonymousRegister;
}
