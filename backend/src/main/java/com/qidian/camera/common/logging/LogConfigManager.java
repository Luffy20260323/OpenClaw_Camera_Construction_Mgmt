package com.qidian.camera.common.logging;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 日志配置管理器
 * 支持动态调整日志级别，按会话和日期维度管理
 */
@Slf4j
@Data
@Component
public class LogConfigManager {
    
    /**
     * 全局日志级别
     */
    private volatile LogLevel globalLogLevel = LogLevel.INFO;
    
    /**
     * 会话级别的日志级别配置
     * Key: sessionId (userId + timestamp)
     * Value: LogLevel
     */
    private final Map<String, SessionLogConfig> sessionLogConfigs = new ConcurrentHashMap<>();
    
    /**
     * 包级别的日志级别配置
     * Key: package name
     * Value: LogLevel
     */
    private final Map<String, LogLevel> packageLogConfigs = new ConcurrentHashMap<>();
    
    /**
     * 日志配置历史
     */
    private final Map<LocalDate, LogConfigHistory> dailyHistory = new ConcurrentHashMap<>();
    
    /**
     * 默认会话过期时间（分钟）
     */
    private static final int DEFAULT_SESSION_EXPIRE_MINUTES = 60;
    
    /**
     * 设置全局日志级别
     */
    public void setGlobalLogLevel(LogLevel level) {
        log.info("设置全局日志级别：{} -> {}", this.globalLogLevel, level);
        this.globalLogLevel = level;
        recordConfigChange("GLOBAL", null, level);
    }
    
    /**
     * 设置会话级别的日志级别
     */
    public void setSessionLogLevel(String sessionId, LogLevel level, int expireMinutes) {
        SessionLogConfig config = new SessionLogConfig();
        config.setLogLevel(level);
        config.setExpireTime(LocalDateTime.now().plusMinutes(expireMinutes));
        config.setUserId(extractUserId(sessionId));
        
        sessionLogConfigs.put(sessionId, config);
        log.info("设置会话日志级别：sessionId={}, level={}, expireMinutes={}", sessionId, level, expireMinutes);
        recordConfigChange("SESSION", sessionId, level);
    }
    
    /**
     * 设置包级别的日志级别
     */
    public void setPackageLogLevel(String packageName, LogLevel level) {
        packageLogConfigs.put(packageName, level);
        log.info("设置包日志级别：package={}, level={}", packageName, level);
        recordConfigChange("PACKAGE", packageName, level);
    }
    
    /**
     * 获取指定上下文的日志级别
     */
    public LogLevel getLogLevel(String sessionId, String packageName) {
        // 1. 检查会话级别配置
        if (sessionId != null) {
            SessionLogConfig config = sessionLogConfigs.get(sessionId);
            if (config != null) {
                if (LocalDateTime.now().isBefore(config.getExpireTime())) {
                    return config.getLogLevel();
                } else {
                    // 过期清理
                    sessionLogConfigs.remove(sessionId);
                }
            }
        }
        
        // 2. 检查包级别配置
        if (packageName != null) {
            LogLevel packageLevel = packageLogConfigs.get(packageName);
            if (packageLevel != null) {
                return packageLevel;
            }
        }
        
        // 3. 返回全局级别
        return globalLogLevel;
    }
    
    /**
     * 清理过期的会话配置
     */
    public void cleanupExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();
        sessionLogConfigs.entrySet().removeIf(entry -> 
            entry.getValue().getExpireTime().isBefore(now)
        );
    }
    
    /**
     * 获取所有会话配置
     */
    public Map<String, SessionLogConfig> getAllSessionConfigs() {
        return new ConcurrentHashMap<>(sessionLogConfigs);
    }
    
    /**
     * 获取所有包配置
     */
    public Map<String, LogLevel> getAllPackageConfigs() {
        return new ConcurrentHashMap<>(packageLogConfigs);
    }
    
    /**
     * 重置为默认配置
     */
    public void reset() {
        sessionLogConfigs.clear();
        packageLogConfigs.clear();
        globalLogLevel = LogLevel.INFO;
        log.info("日志配置已重置为默认值");
    }
    
    /**
     * 记录配置变更历史
     */
    private void recordConfigChange(String type, String target, LogLevel level) {
        LocalDate today = LocalDate.now();
        LogConfigHistory history = dailyHistory.computeIfAbsent(today, k -> new LogConfigHistory());
        history.addChange(LocalDateTime.now(), type, target, level);
    }
    
    /**
     * 获取今日配置历史
     */
    public LogConfigHistory getTodayHistory() {
        return dailyHistory.get(LocalDate.now());
    }
    
    /**
     * 从 sessionId 中提取 userId
     */
    private String extractUserId(String sessionId) {
        if (sessionId == null) {
            return "unknown";
        }
        // sessionId 格式：userId_timestamp
        int underscoreIndex = sessionId.indexOf('_');
        if (underscoreIndex > 0) {
            return sessionId.substring(0, underscoreIndex);
        }
        return sessionId;
    }
    
    /**
     * 生成会话 ID
     */
    public String generateSessionId(Long userId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return userId + "_" + timestamp;
    }
}
