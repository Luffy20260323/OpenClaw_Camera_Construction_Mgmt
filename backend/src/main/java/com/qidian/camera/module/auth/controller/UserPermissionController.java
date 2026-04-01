package com.qidian.camera.module.auth.controller;

import com.qidian.camera.common.response.Result;
import com.qidian.camera.module.auth.dto.*;
import com.qidian.camera.module.auth.entity.*;
import com.qidian.camera.module.auth.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户权限查询 Controller
 */
@Slf4j
@Tag(name = "用户权限", description = "查询用户的权限信息")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserPermissionController {
    
    private final PermissionService permissionService;
    private final ResourceService resourceService;
    
    /**
     * 获取用户的权限列表
     */
    @Operation(summary = "获取用户权限", description = "返回用户拥有的所有资源权限")
    @GetMapping("/{userId}/permissions")
    public Result<List<Resource>> getUserPermissions(@PathVariable Long userId) {
        return Result.success(permissionService.getUserPermissionResources(userId));
    }
    
    /**
     * 获取用户的权限标识列表
     */
    @Operation(summary = "获取用户权限标识", description = "返回用户拥有的所有 permission_key")
    @GetMapping("/{userId}/permission-keys")
    public Result<Set<String>> getUserPermissionKeys(@PathVariable Long userId) {
        return Result.success(permissionService.getUserPermissionKeys(userId));
    }
    
    /**
     * 获取用户的菜单树（根据权限过滤）
     */
    @Operation(summary = "获取用户菜单树", description = "返回用户有权访问的菜单树")
    @GetMapping("/{userId}/menu-tree")
    public Result<List<ResourceService.ResourceTreeNode>> getUserMenuTree(@PathVariable Long userId) {
        Set<Long> permissionIds = permissionService.calculateUserPermissions(userId);
        
        // 获取所有菜单资源
        List<ResourceService.ResourceTreeNode> menuTree = resourceService.getMenuTree();
        
        // 过滤用户有权访问的菜单
        List<ResourceService.ResourceTreeNode> filteredTree = filterMenuTree(menuTree, permissionIds);
        
        return Result.success(filteredTree);
    }
    
    /**
     * 过滤菜单树（只保留用户有权限的节点）
     */
    private List<ResourceService.ResourceTreeNode> filterMenuTree(
            List<ResourceService.ResourceTreeNode> tree, Set<Long> permissionIds) {
        return tree.stream()
            .filter(node -> permissionIds.contains(node.getId()) || hasChildPermission(node, permissionIds))
            .map(node -> {
                node.setChildren(filterMenuTree(node.getChildren(), permissionIds));
                return node;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 检查节点是否有子节点权限
     */
    private boolean hasChildPermission(ResourceService.ResourceTreeNode node, Set<Long> permissionIds) {
        if (node.getChildren() == null || node.getChildren().isEmpty()) {
            return false;
        }
        return node.getChildren().stream()
            .anyMatch(child -> permissionIds.contains(child.getId()) || hasChildPermission(child, permissionIds));
    }
    
    /**
     * 检查用户是否有指定权限
     */
    @Operation(summary = "检查用户权限", description = "检查用户是否拥有指定权限")
    @GetMapping("/{userId}/has-permission")
    public Result<Boolean> checkPermission(
            @PathVariable Long userId,
            @RequestParam String permissionKey) {
        return Result.success(permissionService.hasPermission(userId, permissionKey));
    }
    
    /**
     * 清除用户权限缓存
     */
    @Operation(summary = "清除权限缓存", description = "清除用户的权限缓存")
    @PostMapping("/{userId}/permissions/refresh-cache")
    public Result<Void> refreshCache(@PathVariable Long userId) {
        permissionService.evictUserPermissionCache(userId);
        return Result.success();
    }
}