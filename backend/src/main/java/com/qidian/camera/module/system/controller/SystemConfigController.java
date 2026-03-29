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
}
