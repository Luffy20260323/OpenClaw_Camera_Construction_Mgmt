package com.qidian.camera.module.auth.controller;

import com.qidian.camera.common.response.Result;
import com.qidian.camera.module.auth.dto.*;
import com.qidian.camera.module.auth.entity.Resource;
import com.qidian.camera.module.auth.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 资源管理 Controller
 */
@Tag(name = "资源管理", description = "资源的增删改查和树形结构查询")
@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {
    
    private final ResourceService resourceService;
    
    /**
     * 获取资源树
     */
    @Operation(summary = "获取资源树", description = "返回所有资源的树形结构")
    @GetMapping("/tree")
    public Result<List<ResourceService.ResourceTreeNode>> getResourceTree() {
        return Result.success(resourceService.getResourceTree());
    }
    
    /**
     * 获取菜单树
     */
    @Operation(summary = "获取菜单树", description = "返回模块和菜单的树形结构，用于前端菜单渲染")
    @GetMapping("/menu-tree")
    public Result<List<ResourceService.ResourceTreeNode>> getMenuTree() {
        return Result.success(resourceService.getMenuTree());
    }
    
    /**
     * 获取所有资源
     */
    @Operation(summary = "获取所有资源", description = "返回所有启用的资源列表")
    @GetMapping
    public Result<List<Resource>> getAllResources() {
        return Result.success(resourceService.findAllEnabled());
    }
    
    /**
     * 获取单个资源
     */
    @Operation(summary = "获取单个资源", description = "根据 ID 获取资源详情")
    @GetMapping("/{id}")
    public Result<Resource> getResource(@PathVariable Long id) {
        Resource resource = resourceService.getById(id);
        if (resource == null) {
            return Result.error("资源不存在");
        }
        return Result.success(resource);
    }
    
    /**
     * 创建资源
     */
    @Operation(summary = "创建资源", description = "创建新的资源")
    @PostMapping
    public Result<Resource> createResource(@RequestBody ResourceDTO dto) {
        // 检查 code 是否重复
        if (resourceService.getByCode(dto.getCode()) != null) {
            return Result.error("资源编码已存在");
        }
        
        Resource resource = new Resource();
        resource.setName(dto.getName());
        resource.setCode(dto.getCode());
        resource.setType(dto.getType());
        resource.setParentId(dto.getParentId());
        resource.setPermissionKey(dto.getPermissionKey());
        resource.setUriPattern(dto.getUriPattern());
        resource.setMethod(dto.getMethod());
        resource.setIcon(dto.getIcon());
        resource.setPath(dto.getPath());
        resource.setComponent(dto.getComponent());
        resource.setSortOrder(dto.getSortOrder());
        resource.setIsBasic(dto.getIsBasic() != null ? dto.getIsBasic() : 0);
        resource.setIsVisible(dto.getIsVisible() != null ? dto.getIsVisible() : true);
        resource.setIsSystemProtected(dto.getIsSystemProtected() != null ? dto.getIsSystemProtected() : false);
        resource.setRequiredPermission(dto.getRequiredPermission());
        
        return Result.success(resourceService.create(resource));
    }
    
    /**
     * 更新资源
     */
    @Operation(summary = "更新资源", description = "更新资源信息")
    @PutMapping("/{id}")
    public Result<Resource> updateResource(@PathVariable Long id, @RequestBody ResourceDTO dto) {
        Resource resource = resourceService.getById(id);
        if (resource == null) {
            return Result.error("资源不存在");
        }
        
        resource.setName(dto.getName());
        resource.setType(dto.getType());
        resource.setParentId(dto.getParentId());
        resource.setPermissionKey(dto.getPermissionKey());
        resource.setUriPattern(dto.getUriPattern());
        resource.setMethod(dto.getMethod());
        resource.setIcon(dto.getIcon());
        resource.setPath(dto.getPath());
        resource.setComponent(dto.getComponent());
        resource.setSortOrder(dto.getSortOrder());
        if (dto.getIsBasic() != null) {
            resource.setIsBasic(dto.getIsBasic());
        }
        if (dto.getIsVisible() != null) {
            resource.setIsVisible(dto.getIsVisible());
        }
        if (dto.getIsSystemProtected() != null) {
            resource.setIsSystemProtected(dto.getIsSystemProtected());
        }
        if (dto.getRequiredPermission() != null) {
            resource.setRequiredPermission(dto.getRequiredPermission());
        }
        
        return Result.success(resourceService.update(resource));
    }
    
    /**
     * 删除资源
     */
    @Operation(summary = "删除资源", description = "删除资源及其子资源")
    @DeleteMapping("/{id}")
    public Result<Void> deleteResource(@PathVariable Long id) {
        Resource resource = resourceService.getById(id);
        if (resource == null) {
            return Result.error("资源不存在");
        }
        
        // 基本权限不可删除
        if (resource.getIsBasic() == 1) {
            return Result.error("基本权限资源不可删除");
        }
        
        resourceService.delete(id);
        return Result.success();
    }
    
    /**
     * 获取基本权限资源
     */
    @Operation(summary = "获取基本权限", description = "返回所有基本权限资源")
    @GetMapping("/basic")
    public Result<List<Resource>> getBasicResources() {
        return Result.success(resourceService.getBasicResources());
    }
}