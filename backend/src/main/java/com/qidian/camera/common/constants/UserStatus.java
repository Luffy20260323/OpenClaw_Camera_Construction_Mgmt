package com.qidian.camera.common.constants;

/**
 * 用户状态常量
 * 
 * @author qidian
 * @since 1.0.0
 */
public class UserStatus {

    private UserStatus() {
        throw new IllegalStateException("Constant class");
    }

    /**
     * 待审批
     */
    public static final Integer PENDING = 0;

    /**
     * 正常
     */
    public static final Integer NORMAL = 1;

    /**
     * 禁用
     */
    public static final Integer DISABLED = 2;

}
