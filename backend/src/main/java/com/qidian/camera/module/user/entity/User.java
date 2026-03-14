package com.qidian.camera.module.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@TableName("users")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码哈希
     */
    private String passwordHash;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 性别（0:未知 1:男 2:女）
     */
    private Integer gender;

    /**
     * 公司 ID
     */
    private Long companyId;

    /**
     * 审批状态（0:待审批 1:通过 2:拒绝）
     */
    private Integer approvalStatus;

    /**
     * 审批人 ID
     */
    private Long approvedBy;

    /**
     * 审批时间
     */
    private LocalDateTime approvedAt;

    /**
     * 拒绝原因
     */
    private String rejectionReason;

    /**
     * 用户状态（0:待审批 1:正常 2:禁用）
     */
    private Integer status;

    /**
     * 是否系统保护
     */
    private Boolean isSystemProtected;

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

    /**
     * 关联公司（非数据库字段）
     */
    @TableField(exist = false)
    private Company company;
}
