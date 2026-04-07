package com.qidian.camera.common.logging;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 会话日志配置
 */
@Data
public class SessionLogConfig {
    /**
     * 日志级别
     */
    private LogLevel logLevel;
    
    /**
     * 过期时间
     */
    private LocalDateTime expireTime;
    
    /**
     * 用户 ID
     */
    private String userId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime = LocalDateTime.now();
    
    /**
     * 是否过期
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expireTime);
    }
    
    /**
     * 剩余时间（分钟）
     */
    public long getRemainingMinutes() {
        if (isExpired()) {
            return 0;
        }
        return java.time.Duration.between(LocalDateTime.now(), expireTime).toMinutes();
    }
}
