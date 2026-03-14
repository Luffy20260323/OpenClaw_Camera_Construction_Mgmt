package com.qidian.camera.common.constants;

/**
 * 审核状态常量
 * 
 * @author qidian
 * @since 1.0.0
 */
public class AuditStatus {

    private AuditStatus() {
        throw new IllegalStateException("Constant class");
    }

    /**
     * 待审核
     */
    public static final String PENDING = "pending";

    /**
     * 审核通过
     */
    public static final String PASS = "pass";

    /**
     * 带风险通过
     */
    public static final String RISK_PASS = "risk_pass";

    /**
     * 审核不通过
     */
    public static final String REJECT = "reject";

}
