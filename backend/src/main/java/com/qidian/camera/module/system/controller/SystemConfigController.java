package com.qidian.camera.module.system.controller;

import com.qidian.camera.common.response.Result;
import com.qidian.camera.module.system.dto.SystemConfigDTO;
import com.qidian.camera.module.system.service.SystemConfigService;
import com.qidian.camera.module.auth.annotation.ApiPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置控制器
 */
@Tag(name = "系统配置管理", description = "系统参数配置管理（仅管理员）")
@RestController
@RequestMapping("/system/config")
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    @Operation(summary = "获取所有系统配置", description = "获取所有系统配置项（仅管理员）")
    @ApiPermission("system:config:view")
    @GetMapping
    public Result<List<SystemConfigDTO>> getAllConfigs() {
        List<SystemConfigDTO> configs = systemConfigService.getAllConfigs();
        return Result.success(configs);
    }

    @Operation(summary = "获取指定配置", description = "根据 key 获取配置项（仅管理员）")
    @ApiPermission("system:config:view")
    @GetMapping("/{key}")
    public Result<SystemConfigDTO> getConfig(@PathVariable String key) {
        SystemConfigDTO config = systemConfigService.getConfigByKey(key);
        return Result.success(config);
    }

    @Operation(summary = "更新系统配置", description = "更新配置项的值（仅管理员）")
    @ApiPermission("system:config:edit")
    @PutMapping("/{key}")
    public Result<SystemConfigDTO> updateConfig(
            @PathVariable String key,
            @RequestBody Map<String, String> updateData) {
        
        String value = updateData.get("configValue");
        String description = updateData.getOrDefault("description", "");
        
        if (value == null) {
            return Result.error(400, "配置值不能为空");
        }
        
        SystemConfigDTO updated = systemConfigService.updateConfig(key, value, description);
        return Result.success(updated);
    }

    @Operation(summary = "获取验证码相关配置", description = "获取所有验证码相关配置")
    @ApiPermission("system:config:view")
    @GetMapping("/captcha/all")
    public Result<Map<String, Object>> getCaptchaConfigs() {
        Map<String, Object> configs = new HashMap<>();
        configs.put("captchaType", systemConfigService.getCaptchaType());
        configs.put("captchaLength", systemConfigService.getCaptchaLength());
        configs.put("captchaExpireMinutes", systemConfigService.getCaptchaExpireMinutes());
        configs.put("smsIntervalSeconds", systemConfigService.getSmsIntervalSeconds());
        return Result.success(configs);
    }

    @Operation(summary = "获取侧边栏配置", description = "获取侧边栏位置和显示模式配置")
    @ApiPermission("system:config:view")
    @GetMapping("/sidebar")
    public Result<Map<String, String>> getSidebarConfig() {
        Map<String, String> config = systemConfigService.getSidebarConfig();
        return Result.success(config);
    }

    @Operation(summary = "更新侧边栏位置", description = "更新侧边栏位置配置（LEFT 或 RIGHT）")
    @ApiPermission("system:config:edit")
    @PutMapping("/sidebar/position")
    public Result<Void> updateSidebarPosition(@RequestBody Map<String, String> updateData) {
        String position = updateData.get("position");
        if (position == null) {
            return Result.error(400, "位置参数不能为空");
        }
        systemConfigService.updateSidebarPosition(position);
        return Result.success(null);
    }

    @Operation(summary = "更新侧边栏显示模式", description = "更新侧边栏显示模式配置（FIXED 或 COLLAPSIBLE）")
    @ApiPermission("system:config:edit")
    @PutMapping("/sidebar/mode")
    public Result<Void> updateSidebarMode(@RequestBody Map<String, String> updateData) {
        String mode = updateData.get("mode");
        if (mode == null) {
            return Result.error(400, "模式参数不能为空");
        }
        systemConfigService.updateSidebarMode(mode);
        return Result.success(null);
    }

    @Operation(summary = "更新验证码类型", description = "更新验证码类型配置（none/math/click/slide 等）")
    @ApiPermission("system:config:edit")
    @PutMapping("/captcha-type")
    public Result<Void> updateCaptchaType(@RequestBody Map<String, String> updateData) {
        String captchaType = updateData.get("captchaType");
        if (captchaType == null) {
            return Result.error(400, "验证码类型不能为空");
        }
        systemConfigService.updateConfig("captcha-type", captchaType, "验证码类型：none-无，math-数学，click-点击，slide-滑动");
        return Result.success(null);
    }

    @Operation(summary = "更新验证码长度", description = "更新验证码长度配置（数字）")
    @ApiPermission("system:config:edit")
    @PutMapping("/captcha-length")
    public Result<Void> updateCaptchaLength(@RequestBody Map<String, Integer> updateData) {
        Integer captchaLength = updateData.get("captchaLength");
        if (captchaLength == null || captchaLength < 1 || captchaLength > 10) {
            return Result.error(400, "验证码长度必须在 1-10 之间");
        }
        systemConfigService.updateConfig("captcha-length", String.valueOf(captchaLength), "验证码长度：1-10 之间的数字");
        return Result.success(null);
    }

    @Operation(summary = "更新验证码过期时间", description = "更新验证码过期时间配置（分钟）")
    @ApiPermission("system:config:edit")
    @PutMapping("/captcha-expire-minutes")
    public Result<Void> updateCaptchaExpireMinutes(@RequestBody Map<String, Integer> updateData) {
        Integer captchaExpireMinutes = updateData.get("captchaExpireMinutes");
        if (captchaExpireMinutes == null || captchaExpireMinutes < 1 || captchaExpireMinutes > 60) {
            return Result.error(400, "验证码过期时间必须在 1-60 分钟之间");
        }
        systemConfigService.updateConfig("captcha-expire-minutes", String.valueOf(captchaExpireMinutes), "验证码过期时间：1-60 分钟");
        return Result.success(null);
    }

    @Operation(summary = "更新短信验证码发送间隔", description = "更新短信验证码发送间隔配置（秒）")
    @ApiPermission("system:config:edit")
    @PutMapping("/sms-interval-seconds")
    public Result<Void> updateSmsIntervalSeconds(@RequestBody Map<String, Integer> updateData) {
        Integer smsIntervalSeconds = updateData.get("smsIntervalSeconds");
        if (smsIntervalSeconds == null || smsIntervalSeconds < 10 || smsIntervalSeconds > 300) {
            return Result.error(400, "短信发送间隔必须在 10-300 秒之间");
        }
        systemConfigService.updateConfig("sms-interval-seconds", String.valueOf(smsIntervalSeconds), "短信验证码发送间隔：10-300 秒");
        return Result.success(null);
    }
}
