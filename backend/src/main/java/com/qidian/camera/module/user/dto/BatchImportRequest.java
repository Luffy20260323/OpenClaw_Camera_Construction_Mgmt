package com.qidian.camera.module.user.dto;

import lombok.Data;

import java.util.List;

/**
 * 批量导入用户请求
 */
@Data
public class BatchImportRequest {

    /**
     * 用户列表
     */
    private List<ImportUserDTO> users;

    /**
     * 是否直接通过审批
     */
    private Boolean autoApprove;

    /**
     * 导入用户 DTO
     */
    @Data
    public static class ImportUserDTO {
        /**
         * 用户名
         */
        private String username;

        /**
         * 密码
         */
        private String password;

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
         * 公司 ID
         */
        private Long companyId;

        /**
         * 角色 ID 列表
         */
        private List<Long> roleIds;

        /**
         * 作业区 ID 列表
         */
        private List<Long> workAreaIds;

        /**
         * 性别（0:未知 1:男 2:女）
         */
        private Integer gender;
    }
}
