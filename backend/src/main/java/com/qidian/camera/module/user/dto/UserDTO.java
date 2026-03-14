package com.qidian.camera.module.user.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户信息 DTO
 */
@Data
public class UserDTO {

    /**
     * 用户 ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
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
     * 性别（0:未知 1:男 2:女）
     */
    private Integer gender;

    /**
     * 公司 ID
     */
    private Long companyId;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 公司类型 ID
     */
    private Integer companyTypeId;

    /**
     * 公司类型名称
     */
    private String companyTypeName;

    /**
     * 角色 ID 列表
     */
    private List<Long> roleIds;

    /**
     * 角色编码列表
     */
    private List<String> roleCodes;

    /**
     * 角色名称列表
     */
    private List<String> roleNames;

    /**
     * 审批状态（0:待审批 1:通过 2:拒绝）
     */
    private Integer approvalStatus;

    /**
     * 审批人 ID
     */
    private Long approvedBy;

    /**
     * 审批人姓名
     */
    private String approvedByName;

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
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
