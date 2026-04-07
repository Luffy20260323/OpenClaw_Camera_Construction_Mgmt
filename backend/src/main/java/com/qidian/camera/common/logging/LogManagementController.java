package com.qidian.camera.common.logging;

import com.qidian.camera.common.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 日志管理 Controller
 * 提供动态调整日志级别的 API
 */
@Slf4j
@Tag(name = "日志管理", description = "动态调整日志级别")
@RestController
@RequestMapping("/admin/logs")
@RequiredArgsConstructor
public class LogManagementController {
    
    private final LogConfigManager logConfigManager;
    
    /**
     * 设置全局日志级别
     */
    @Operation(summary = "设置全局日志级别", description = "可选值：ERROR, WARN, INFO, DEBUG, TRACE")
    @PutMapping("/level/global")
    public Result<Map<String, Object>> setGlobalLogLevel(@RequestParam String level) {
        LogLevel logLevel = LogLevel.fromString(level);
        logConfigManager.setGlobalLogLevel(logLevel);
        
        Map<String, Object> response = new HashMap<>();
        response.put("globalLogLevel", logConfigManager.getGlobalLogLevel());
        response.put("message", "全局日志级别已设置为：" + level);
        
        return Result.success(response);
    }
    
    /**
     * 设置会话日志级别
     */
    @Operation(summary = "设置会话日志级别", description = "为指定用户会话设置日志级别")
    @PutMapping("/level/session")
    public Result<Map<String, Object>> setSessionLogLevel(
            @RequestParam Long userId,
            @RequestParam String level,
            @RequestParam(defaultValue = "60") int expireMinutes) {
        
        String sessionId = logConfigManager.generateSessionId(userId);
        LogLevel logLevel = LogLevel.fromString(level);
        logConfigManager.setSessionLogLevel(sessionId, logLevel, expireMinutes);
        
        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", sessionId);
        response.put("userId", userId);
        response.put("logLevel", logLevel);
        response.put("expireMinutes", expireMinutes);
        response.put("message", "会话日志级别已设置，" + expireMinutes + "分钟后过期");
        
        return Result.success(response);
    }
    
    /**
     * 设置包日志级别
     */
    @Operation(summary = "设置包日志级别", description = "为指定包设置日志级别")
    @PutMapping("/level/package")
    public Result<Map<String, Object>> setPackageLogLevel(
            @RequestParam String packageName,
            @RequestParam String level) {
        
        LogLevel logLevel = LogLevel.fromString(level);
        logConfigManager.setPackageLogLevel(packageName, logLevel);
        
        Map<String, Object> response = new HashMap<>();
        response.put("packageName", packageName);
        response.put("logLevel", logLevel);
        response.put("message", "包日志级别已设置：" + packageName + " = " + level);
        
        return Result.success(response);
    }
    
    /**
     * 获取当前日志配置
     */
    @Operation(summary = "获取当前日志配置", description = "查看当前的日志级别配置")
    @GetMapping("/config")
    public Result<Map<String, Object>> getCurrentConfig() {
        Map<String, Object> response = new HashMap<>();
        response.put("globalLogLevel", logConfigManager.getGlobalLogLevel());
        response.put("sessionConfigs", logConfigManager.getAllSessionConfigs());
        response.put("packageConfigs", logConfigManager.getAllPackageConfigs());
        response.put("todayHistory", logConfigManager.getTodayHistory());
        
        return Result.success(response);
    }
    
    /**
     * 重置日志配置
     */
    @Operation(summary = "重置日志配置", description = "恢复为默认日志级别")
    @PostMapping("/reset")
    public Result<Map<String, Object>> reset() {
        logConfigManager.reset();
        
        Map<String, Object> response = new HashMap<>();
        response.put("globalLogLevel", logConfigManager.getGlobalLogLevel());
        response.put("message", "日志配置已重置为默认值");
        
        return Result.success(response);
    }
    
    /**
     * 清理过期会话
     */
    @Operation(summary = "清理过期会话", description = "清理已过期的会话日志配置")
    @PostMapping("/cleanup")
    public Result<Map<String, Object>> cleanup() {
        logConfigManager.cleanupExpiredSessions();
        
        Map<String, Object> response = new HashMap<>();
        response.put("activeSessionCount", logConfigManager.getAllSessionConfigs().size());
        response.put("message", "已清理过期会话配置");
        
        return Result.success(response);
    }
    
    /**
     * 快速设置 DEBUG 模式
     */
    @Operation(summary = "启用 DEBUG 模式", description = "快速启用详细日志记录")
    @PostMapping("/debug/enable")
    public Result<Map<String, Object>> enableDebugMode() {
        logConfigManager.setGlobalLogLevel(LogLevel.DEBUG);
        
        // 为关键包设置 DEBUG 级别
        logConfigManager.setPackageLogLevel("com.qidian.camera.module.auth", LogLevel.DEBUG);
        logConfigManager.setPackageLogLevel("com.qidian.camera.module.menu", LogLevel.DEBUG);
        logConfigManager.setPackageLogLevel("com.qidian.camera.common.logging", LogLevel.DEBUG);
        
        Map<String, Object> response = new HashMap<>();
        response.put("globalLogLevel", LogLevel.DEBUG);
        response.put("enabledPackages", new String[]{
            "com.qidian.camera.module.auth",
            "com.qidian.camera.module.menu",
            "com.qidian.camera.common.logging"
        });
        response.put("message", "DEBUG 模式已启用，将记录详细的 API 请求和菜单操作日志");
        
        return Result.success(response);
    }
    
    /**
     * 禁用 DEBUG 模式
     */
    @Operation(summary = "禁用 DEBUG 模式", description = "恢复为 INFO 日志级别")
    @PostMapping("/debug/disable")
    public Result<Map<String, Object>> disableDebugMode() {
        logConfigManager.setGlobalLogLevel(LogLevel.INFO);
        logConfigManager.reset();
        
        Map<String, Object> response = new HashMap<>();
        response.put("globalLogLevel", LogLevel.INFO);
        response.put("message", "DEBUG 模式已禁用，恢复为默认日志级别");
        
        return Result.success(response);
    }
}
