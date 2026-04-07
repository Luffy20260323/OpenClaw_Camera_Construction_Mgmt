package com.qidian.camera.module.system.service;

import com.qidian.camera.module.system.dto.SystemConfigDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 系统配置服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemConfigService {

    private final JdbcTemplate jdbcTemplate;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String CONFIG_CACHE_PREFIX = "system:config:";
    private static final long CACHE_EXPIRE_MINUTES = 10;

    /**
     * 获取所有系统配置
     */
    public List<SystemConfigDTO> getAllConfigs() {
        String sql = "SELECT id, config_key, config_value, config_type, description, updated_at " +
                     "FROM system_configs ORDER BY config_key";
        
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            SystemConfigDTO dto = new SystemConfigDTO();
            dto.setId(rs.getLong("id"));
            dto.setConfigKey(rs.getString("config_key"));
            dto.setConfigValue(rs.getString("config_value"));
            dto.setConfigType(rs.getString("config_type"));
            dto.setDescription(rs.getString("description"));
            dto.setUpdatedAt(rs.getTimestamp("updated_at") != null ? 
                rs.getTimestamp("updated_at").toLocalDateTime() : null);
            return dto;
        });
    }

    /**
     * 根据键获取配置
     */
    public SystemConfigDTO getConfigByKey(String key) {
        String sql = "SELECT id, config_key, config_value, config_type, description, updated_at " +
                     "FROM system_configs WHERE config_key = ?";
        
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            SystemConfigDTO dto = new SystemConfigDTO();
            dto.setId(rs.getLong("id"));
            dto.setConfigKey(rs.getString("config_key"));
            dto.setConfigValue(rs.getString("config_value"));
            dto.setConfigType(rs.getString("config_type"));
            dto.setDescription(rs.getString("description"));
            dto.setUpdatedAt(rs.getTimestamp("updated_at") != null ? 
                rs.getTimestamp("updated_at").toLocalDateTime() : null);
            return dto;
        }, key);
    }

    /**
     * 更新配置（仅管理员）
     * 如果配置不存在则创建新记录
     */
    @Transactional
    public SystemConfigDTO updateConfig(String key, String value, String description) {
        // 检查配置是否存在
        SystemConfigDTO existing = null;
        try {
            existing = getConfigByKey(key);
        } catch (Exception e) {
            // 配置不存在，忽略异常
            log.debug("配置项不存在，将创建新记录：{}", key);
        }

        if (existing == null) {
            // 创建新配置记录
            String insertSql = "INSERT INTO system_configs (config_key, config_value, config_type, description, created_at, updated_at) " +
                               "VALUES (?, ?, 'string', ?, ?, ?)";
            Timestamp now = new Timestamp(System.currentTimeMillis());
            jdbcTemplate.update(insertSql, key, value, description, now, now);
            log.info("系统配置已创建：{} = {}", key, value);
        } else {
            // 更新现有配置
            String updateSql = "UPDATE system_configs SET config_value = ?, description = ?, updated_at = ? " +
                               "WHERE config_key = ?";
            jdbcTemplate.update(updateSql, value, description, new Timestamp(System.currentTimeMillis()), key);
            log.info("系统配置已更新：{} = {}", key, value);
        }

        // 清除缓存
        clearCache(key);

        // 返回更新后的配置
        return getConfigByKey(key);
    }

    /**
     * 获取配置值（带缓存）
     */
    public String getConfigValue(String key, String defaultValue) {
        // 先查缓存
        String cacheKey = CONFIG_CACHE_PREFIX + key;
        String value = redisTemplate.opsForValue().get(cacheKey);
        
        if (value != null) {
            return value;
        }

        // 查数据库
        try {
            SystemConfigDTO config = getConfigByKey(key);
            if (config != null) {
                value = config.getConfigValue();
                // 写入缓存
                redisTemplate.opsForValue().set(cacheKey, value, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
                return value;
            }
        } catch (Exception e) {
            log.warn("获取配置失败：{}", key, e);
        }

        return defaultValue;
    }

    /**
     * 清除配置缓存
     */
    public void clearCache(String key) {
        String cacheKey = CONFIG_CACHE_PREFIX + key;
        redisTemplate.delete(cacheKey);
        log.debug("已清除配置缓存：{}", key);
    }

    /**
     * 获取验证码类型
     */
    public String getCaptchaType() {
        return getConfigValue("captcha-type", "none");
    }

    /**
     * 获取验证码长度配置
     */
    public int getCaptchaLength() {
        String value = getConfigValue("captcha-length", "4");
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 4;
        }
    }

    /**
     * 获取验证码过期时间（分钟）
     */
    public int getCaptchaExpireMinutes() {
        String value = getConfigValue("captcha-expire-minutes", "5");
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 5;
        }
    }

    /**
     * 获取短信验证码发送间隔（秒）
     */
    public int getSmsIntervalSeconds() {
        String value = getConfigValue("sms-interval-seconds", "60");
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 60;
        }
    }

    /**
     * 获取侧边栏位置配置
     * @return "LEFT" 或 "RIGHT"
     */
    public String getSidebarPosition() {
        return getConfigValue("sidebar-position", "LEFT");
    }

    /**
     * 获取侧边栏显示模式配置
     * @return "FIXED" 或 "COLLAPSIBLE"
     */
    public String getSidebarMode() {
        return getConfigValue("sidebar-mode", "FIXED");
    }

    /**
     * 获取侧边栏配置
     */
    public Map<String, String> getSidebarConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("sidebarPosition", getSidebarPosition());
        config.put("sidebarMode", getSidebarMode());
        return config;
    }

    /**
     * 更新侧边栏位置配置
     */
    @Transactional
    public void updateSidebarPosition(String position) {
        if (!"LEFT".equals(position) && !"RIGHT".equals(position)) {
            throw new IllegalArgumentException("侧边栏位置必须是 LEFT 或 RIGHT");
        }
        updateConfig("sidebar-position", position, "侧边栏位置：LEFT-左边，RIGHT-右边");
    }

    /**
     * 更新侧边栏显示模式配置
     */
    @Transactional
    public void updateSidebarMode(String mode) {
        if (!"FIXED".equals(mode) && !"COLLAPSIBLE".equals(mode)) {
            throw new IllegalArgumentException("侧边栏模式必须是 FIXED 或 COLLAPSIBLE");
        }
        updateConfig("sidebar-mode", mode, "侧边栏显示模式：FIXED-一直显示，COLLAPSIBLE-可隐藏");
    }
}
