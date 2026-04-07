package com.qidian.camera.common.logging;

/**
 * 日志级别枚举
 */
public enum LogLevel {
    /**
     * 错误级别 - 只记录错误
     */
    ERROR(0),
    
    /**
     * 警告级别 - 记录错误和警告
     */
    WARN(1),
    
    /**
     * 信息级别 - 记录错误、警告和信息
     */
    INFO(2),
    
    /**
     * 调试级别 - 记录详细信息（包括 API 请求/响应）
     */
    DEBUG(3),
    
    /**
     * 追踪级别 - 记录所有细节（包括 SQL、参数等）
     */
    TRACE(4);
    
    private final int level;
    
    LogLevel(int level) {
        this.level = level;
    }
    
    public int getLevel() {
        return level;
    }
    
    public static LogLevel fromString(String value) {
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return INFO;
        }
    }
}
