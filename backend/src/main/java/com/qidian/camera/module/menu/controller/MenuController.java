package com.qidian.camera.module.menu.controller;

import com.qidian.camera.common.response.Result;
import com.qidian.camera.module.auth.service.ResourceService;
import com.qidian.camera.module.menu.service.MenuService;
import com.qidian.camera.module.menu.dto.MenuDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 菜单权限控制器
 * 
 * 修改说明（2026-04-06）：
 * - getMyMenus() 改为调用 MenuService.getUserMenus()，实现权限过滤
 * - 使用从下向上构建的菜单树逻辑（MENU → MODULE）
 */
@Tag(name = "菜单权限管理", description = "系统菜单权限管理")
@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
@Slf4j
public class MenuController {
    
    private final ResourceService resourceService;
    private final MenuService menuService;
    private final JdbcTemplate jdbcTemplate;
    
    /**
     * 获取当前用户的菜单列表
     * 
     * 新逻辑：
     * 1. 从 SecurityContext 获取当前用户 ID
     * 2. 调用 MenuService.getUserMenus() 计算用户权限
     * 3. 从下向上构建菜单树（MENU → MODULE）
     * 4. 只返回用户有权限的菜单
     */
    @Operation(summary = "获取当前用户的菜单列表", description = "根据用户权限过滤菜单")
    @GetMapping("/my-menus")
    public Result<List<MenuDTO>> getMyMenus() {
        // 从 SecurityContext 获取当前用户 ID
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            log.warn("未获取到认证信息，返回空菜单");
            return Result.success(new ArrayList<>());
        }
        
        Long userId = null;
        if (auth.getPrincipal() instanceof Long) {
            userId = (Long) auth.getPrincipal();
        } else if (auth.getPrincipal() instanceof Map) {
            Map<?, ?> principal = (Map<?, ?>) auth.getPrincipal();
            Object idObj = principal.get("id");
            if (idObj instanceof Long) {
                userId = (Long) idObj;
            } else if (idObj instanceof Integer) {
                userId = ((Integer) idObj).longValue();
            }
        } else if (auth.getPrincipal() instanceof String) {
            // 如果 principal 是 username，需要查询数据库获取 userId
            String username = (String) auth.getPrincipal();
            try {
                userId = jdbcTemplate.queryForObject(
                    "SELECT id FROM users WHERE username = ?", Long.class, username);
            } catch (Exception e) {
                log.warn("无法从 username 获取 userId: {}", username);
                return Result.success(new ArrayList<>());
            }
        }
        
        if (userId == null) {
            log.warn("无法解析用户 ID，principal 类型: {}", auth.getPrincipal().getClass());
            return Result.success(new ArrayList<>());
        }
        
        log.info("获取用户 {} 的菜单列表", userId);
        List<MenuDTO> menus = menuService.getUserMenus(userId);
        return Result.success(menus);
    }
    
    @Operation(summary = "获取所有菜单", description = "返回完整菜单树（超级管理员）")
    @GetMapping("/all")
    public Result<List<MenuDTO>> getAllMenus() {
        List<MenuDTO> menus = menuService.getAllMenus();
        return Result.success(menus);
    }
    
    /**
     * 获取用户的菜单权限列表（已废弃）
     */
    @Operation(summary = "获取用户菜单权限", description = "【已废弃】使用 /menu/my-menus")
    @Deprecated
    @GetMapping("/user-permissions/{userId}")
    public Result<List<Map<String, Object>>> getUserPermissions(@PathVariable Long userId) {
        log.warn("菜单权限功能已废弃，请使用 /menu/my-menus");
        return Result.success(new ArrayList<>());
    }
    
    /**
     * 更新用户菜单权限（已废弃）
     */
    @Operation(summary = "更新用户菜单权限", description = "【已废弃】")
    @Deprecated
    @PutMapping("/user-permission")
    public Result<Void> updateUserPermission(@RequestBody Map<String, Object> request) {
        log.warn("菜单权限功能已废弃");
        return Result.success();
    }
    
    /**
     * 删除用户自定义菜单权限（已废弃）
     */
    @Operation(summary = "删除用户自定义菜单权限", description = "【已废弃】")
    @Deprecated
    @DeleteMapping("/user-permission")
    public Result<Void> deleteUserPermission(@RequestParam Long userId, @RequestParam Long menuId) {
        log.warn("菜单权限功能已废弃");
        return Result.success();
    }
    
    /**
     * 批量更新用户菜单权限（已废弃）
     */
    @Operation(summary = "批量更新用户菜单权限", description = "【已废弃】")
    @Deprecated
    @PutMapping("/user-permissions/batch")
    public Result<Void> batchUpdateUserPermissions(@RequestParam Long userId, @RequestBody List<Map<String, Object>> permissions) {
        log.warn("菜单权限功能已废弃");
        return Result.success();
    }
}