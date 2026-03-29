package com.qidian.camera.module.menu.controller;

import com.qidian.camera.common.response.Result;
import com.qidian.camera.module.menu.dto.MenuDTO;
import com.qidian.camera.module.menu.dto.UpdateUserMenuPermissionRequest;
import com.qidian.camera.module.menu.dto.UserMenuPermissionDTO;
import com.qidian.camera.module.menu.service.MenuService;
import com.qidian.camera.module.auth.annotation.ApiPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单权限控制器
 */
@Tag(name = "菜单权限管理", description = "系统菜单权限管理")
@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
public class MenuController {
    
    private final MenuService menuService;
    
    @Operation(summary = "获取当前用户的菜单列表", description = "根据用户角色和自定义权限返回可见菜单")
    @ApiPermission("menu:my:view")
    @GetMapping("/my-menus")
    public Result<List<MenuDTO>> getMyMenus(@RequestAttribute("userId") Long userId) {
        List<MenuDTO> menus = menuService.getUserMenus(userId);
        return Result.success(menus);
    }
    
    @Operation(summary = "获取所有菜单", description = "系统管理员使用，返回所有菜单定义")
    @ApiPermission("menu:all:view")
    @GetMapping("/all")
    public Result<List<MenuDTO>> getAllMenus(@RequestAttribute("userId") Long userId) {
        List<MenuDTO> menus = menuService.getAllMenus();
        return Result.success(menus);
    }
    
    @Operation(summary = "获取指定用户的菜单权限详情", description = "系统管理员使用，查看用户的菜单权限配置")
    @ApiPermission("menu:user:view")
    @GetMapping("/user-permissions/{userId}")
    public Result<List<UserMenuPermissionDTO>> getUserPermissions(
            @PathVariable Long userId,
            @RequestAttribute("userId") Long operatorId) {
        List<UserMenuPermissionDTO> permissions = menuService.getUserMenuPermissions(userId);
        return Result.success(permissions);
    }
    
    @Operation(summary = "更新用户菜单权限", description = "系统管理员为用户定制菜单权限")
    @ApiPermission("menu:user:edit")
    @PutMapping("/user-permission")
    public Result<Void> updateUserPermission(
            @Valid @RequestBody UpdateUserMenuPermissionRequest request,
            @RequestAttribute("userId") Long operatorId) {
        menuService.updateUserMenuPermission(request, operatorId);
        return Result.success();
    }
    
    @Operation(summary = "删除用户自定义菜单权限", description = "删除自定义权限，恢复为角色默认权限")
    @ApiPermission("menu:user:delete")
    @DeleteMapping("/user-permission")
    public Result<Void> deleteUserPermission(
            @RequestParam Long userId,
            @RequestParam Long menuId,
            @RequestAttribute("userId") Long operatorId) {
        menuService.deleteUserMenuPermission(userId, menuId, operatorId);
        return Result.success();
    }
    
    @Operation(summary = "批量更新用户菜单权限", description = "系统管理员批量配置用户菜单权限")
    @ApiPermission("menu:user:batch")
    @PutMapping("/user-permissions/batch")
    public Result<Void> batchUpdateUserPermissions(
            @RequestParam Long userId,
            @Valid @RequestBody List<UpdateUserMenuPermissionRequest> requests,
            @RequestAttribute("userId") Long operatorId) {
        menuService.batchUpdateUserMenuPermissions(userId, requests, operatorId);
        return Result.success();
    }
}
