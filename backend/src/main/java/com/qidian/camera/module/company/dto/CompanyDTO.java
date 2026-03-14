package com.qidian.camera.module.company.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公司信息 DTO
 */
@Data
@Schema(description = "公司信息")
public class CompanyDTO {

    @Schema(description = "公司 ID", example = "1")
    private Long id;

    @Schema(description = "公司名称", example = "北京其点技术服务有限公司")
    private String companyName;

    @Schema(description = "公司类型 ID", example = "4")
    private Long typeId;

    @Schema(description = "公司类型名称", example = "软件所有者公司")
    private String typeName;

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

    @Schema(description = "是否系统保护", example = "true")
    private Boolean isSystemProtected;

    @Schema(description = "是否允许匿名注册", example = "false")
    private Boolean allowAnonymousRegister;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
