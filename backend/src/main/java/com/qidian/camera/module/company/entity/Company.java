package com.qidian.camera.module.company.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公司实体
 */
@Data
@TableName("companies")
public class Company implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 公司 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 公司类型 ID
     */
    private Long typeId;

    /**
     * 统一社会信用代码
     */
    private String unifiedSocialCreditCode;

    /**
     * 联系人
     */
    private String contactPerson;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 联系邮箱
     */
    private String contactEmail;

    /**
     * 地址
     */
    private String address;

    /**
     * 状态（active:正常 inactive:禁用）
     */
    private String status;

    /**
     * 描述
     */
    private String description;

    /**
     * 是否系统保护
     */
    private Boolean isSystemProtected;

    /**
     * 是否允许匿名注册
     */
    private Boolean allowAnonymousRegister;

    /**
     * 创建人 ID
     */
    private Long createdBy;

    /**
     * 更新人 ID
     */
    private Long updatedBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
