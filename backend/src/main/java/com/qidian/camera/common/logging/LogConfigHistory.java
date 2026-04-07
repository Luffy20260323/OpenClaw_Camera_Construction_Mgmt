package com.qidian.camera.common.logging;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 日志配置历史记录
 */
@Data
public class LogConfigHistory {
    /**
     * 日期
     */
    private final LocalDate date = LocalDate.now();
    
    /**
     * 配置变更历史
     */
    private final List<ConfigChange> changes = new ArrayList<>();
    
    /**
     * 添加配置变更
     */
    public void addChange(LocalDateTime time, String type, String target, LogLevel level) {
        ConfigChange change = new ConfigChange();
        change.setTime(time);
        change.setType(type);
        change.setTarget(target);
        change.setLevel(level);
        changes.add(change);
    }
    
    /**
     * 配置变更记录
     */
    @Data
    public static class ConfigChange {
        private LocalDateTime time;
        private String type; // GLOBAL, SESSION, PACKAGE
        private String target; // 目标（sessionId 或 packageName）
        private LogLevel level;
    }
}
