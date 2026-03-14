package com.qidian.camera.common.exception;

import lombok.Getter;

/**
 * 错误码枚举
 * 
 * @author qidian
 * @since 1.0.0
 */
@Getter
public enum ErrorCode {

    // 通用错误（1000-1999）
    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    
    // 用户相关（2000-2999）
    USER_NOT_FOUND(2001, "用户不存在"),
    USER_PASSWORD_ERROR(2002, "用户名或密码错误"),
    USER_DISABLED(2003, "用户已被禁用"),
    USER_NOT_APPROVED(2004, "用户尚未审批通过"),
    USERNAME_EXISTS(2005, "用户名已存在"),
    EMAIL_EXISTS(2006, "邮箱已被注册"),
    PHONE_EXISTS(2007, "手机号已被注册"),
    
    // 验证码相关（3000-3999）
    CAPTCHA_ERROR(3001, "验证码错误"),
    CAPTCHA_EXPIRED(3002, "验证码已过期"),
    CAPTCHA_SEND_ERROR(3003, "验证码发送失败"),
    
    // 项目相关（4000-4999）
    PROJECT_NOT_FOUND(4001, "项目不存在"),
    SECTION_NOT_FOUND(4002, "标段不存在"),
    POINT_NOT_FOUND(4003, "点位不存在"),
    
    // 子任务相关（5000-5999）
    SUBTASK_NOT_FOUND(5001, "子任务不存在"),
    SUBTASK_STATUS_ERROR(5002, "子任务状态错误"),
    SUBTASK_DEPENDENCY_ERROR(5003, "前序子任务未完成"),
    
    // 审核相关（6000-6999）
    AUDIT_NOT_FOUND(6001, "审核记录不存在"),
    AUDIT_STATUS_ERROR(6002, "审核状态错误"),
    
    // 文件相关（7000-7999）
    FILE_UPLOAD_ERROR(7001, "文件上传失败"),
    FILE_NOT_FOUND(7002, "文件不存在"),
    FILE_TYPE_ERROR(7003, "文件类型不支持"),
    FILE_SIZE_ERROR(7004, "文件大小超出限制"),
    
    // 认证相关（8000-8999）
    INVALID_CREDENTIALS(8001, "用户名或密码错误"),
    INVALID_TOKEN(8002, "无效的令牌"),
    TOKEN_EXPIRED(8003, "令牌已过期");

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误消息
     */
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

}
