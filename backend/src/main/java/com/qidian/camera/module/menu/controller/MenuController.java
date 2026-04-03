package com.qidian.camera.module.menu.controller;

import com.qidian.camera.common.response.Result;
import com.qidian.camera.module.auth.service.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 菜单权限控制器（已整合到资源管理）
 * 注意：此类仅作为向后兼容的包装器，实际数据从 resource 表读取
 */
@Tag(name = "菜单权限管理", description = "系统菜单权限管理（已整合到资源管理）")
@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
@Slf4j
public class MenuController {
    
    private final ResourceService resourceService;
    
    @Operation(summary = "获取当前用户的菜单列表", description = "【已整合】实际调用 /api/resources/menu-tree")
    @GetMapping("/my-menus")
    public Result<Object> getMyMenus() {
        log.warn("/menu/my-menus 已整合到 /api/resources/menu-tree");
        // 转发到资源服务
        return Result.success(resourceService.getMenuTree());
    }
    
    @Operation(summary = "获取所有菜单", description = "【已整合】实际调用 /api/resources/menu-tree")
    @GetMapping("/all")
    public Result<Object> getAllMenus() {
        log.warn("/menu/all 已整合到 /api/resources/menu-tree");
        // 转发到资源服务
        return Result.success(resourceService.getMenuTree());
    }
}
