package com.qidian.camera.module.user.dto;

import lombok.Data;

/**
 * 用户查询请求
 */
@Data
public class UserQueryRequest {

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 10;

    /**
     * 用户名（模糊搜索）
     */
    private String username;

    /**
     * 真实姓名（模糊搜索）
     */
    private String realName;

    /**
     * 公司 ID
     */
    private Long companyId;

    /**
     * 公司类型 ID
     */
    private Integer companyTypeId;

    /**
     * 角色 ID
     */
    private Long roleId;

    /**
     * 审批状态（0:待审批 1:通过 2:拒绝）
     */
    private Integer approvalStatus;

    /**
     * 用户状态（0:待审批 1:正常 2:禁用）
     */
    private Integer status;

    /**
     * 关键词（搜索用户名、真实姓名、手机号、邮箱）
     */
    private String keyword;
}
