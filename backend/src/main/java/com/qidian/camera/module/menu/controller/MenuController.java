package com.qidian.camera.module.menu.controller;

import com.qidian.camera.module.menu.dto.MenuDTO;
import com.qidian.camera.module.menu.dto.UpdateUserMenuPermissionRequest;
import com.qidian.camera.module.menu.dto.UserMenuPermissionDTO;
import com.qidian.camera.module.menu.service.MenuService;
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
    @GetMapping("/my-menus")
    public List<MenuDTO> getMyMenus(@RequestAttribute("userId") Long userId) {
        return menuService.getUserMenus(userId);
    }
    
    @Operation(summary = "获取所有菜单", description = "系统管理员使用，返回所有菜单定义")
    @GetMapping("/all")
    public List<MenuDTO> getAllMenus(@RequestAttribute("userId") Long userId) {
        return menuService.getAllMenus();
    }
    
    @Operation(summary = "获取指定用户的菜单权限详情", description = "系统管理员使用，查看用户的菜单权限配置")
    @GetMapping("/user-permissions/{userId}")
    public List<UserMenuPermissionDTO> getUserPermissions(
            @PathVariable Long userId,
            @RequestAttribute("userId") Long operatorId) {
        return menuService.getUserMenuPermissions(userId);
    }
    
    @Operation(summary = "更新用户菜单权限", description = "系统管理员为用户定制菜单权限")
    @PutMapping("/user-permission")
    public void updateUserPermission(
            @Valid @RequestBody UpdateUserMenuPermissionRequest request,
            @RequestAttribute("userId") Long operatorId) {
        menuService.updateUserMenuPermission(request, operatorId);
    }
    
    @Operation(summary = "删除用户自定义菜单权限", description = "删除自定义权限，恢复为角色默认权限")
    @DeleteMapping("/user-permission")
    public void deleteUserPermission(
            @RequestParam Long userId,
            @RequestParam Long menuId,
            @RequestAttribute("userId") Long operatorId) {
        menuService.deleteUserMenuPermission(userId, menuId, operatorId);
    }
    
    @Operation(summary = "批量更新用户菜单权限", description = "系统管理员批量配置用户菜单权限")
    @PutMapping("/user-permissions/batch")
    public void batchUpdateUserPermissions(
            @RequestParam Long userId,
            @Valid @RequestBody List<UpdateUserMenuPermissionRequest> requests,
            @RequestAttribute("userId") Long operatorId) {
        menuService.batchUpdateUserMenuPermissions(userId, requests, operatorId);
    }
}
