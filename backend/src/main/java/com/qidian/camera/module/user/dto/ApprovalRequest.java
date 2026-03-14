package com.qidian.camera.module.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户审批请求
 */
@Data
public class ApprovalRequest {

    /**
     * 用户 ID
     */
    @NotNull(message = "用户 ID 不能为空")
    private Long userId;

    /**
     * 是否通过（true:通过 false:拒绝）
     */
    @NotNull(message = "审批结果不能为空")
    private Boolean approved;

    /**
     * 拒绝原因（仅拒绝时需要）
     */
    private String rejectionReason;
}
