package com.qidian.camera.module.resource.controller;

import com.qidian.camera.common.response.Result;
import com.qidian.camera.module.auth.entity.Resource;
import com.qidian.camera.module.auth.mapper.ResourceMapper;
import com.qidian.camera.module.resource.dto.ModuleStatsDTO;
import com.qidian.camera.module.resource.dto.ResourceStatsDTO;
import com.qidian.camera.module.resource.dto.ResourceTreeDTO;
import com.qidian.camera.module.resource.service.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 资源管理控制器
 * 基于统一的 resource 表，提供六种类型资源的查询
 */
@Tag(name = "资源管理", description = "统一资源管理API - 包含模块、菜单、页面、元素、API、权限六种类型")
@RestController
@RequestMapping("/resource")
@RequiredArgsConstructor
@Slf4j
public class UnifiedResourceController {
    
    @org.springframework.beans.factory.annotation.Qualifier("unifiedResourceService")
    private final ResourceService resourceService;
    
    @Operation(summary = "获取资源树", description = "获取完整的资源层级结构（树形）")
    @GetMapping("/tree")
    public Result<List<ResourceTreeDTO>> getResourceTree() {
        List<ResourceTreeDTO> tree = resourceService.getResourceTree();
        return Result.success(tree);
    }
    
    @Operation(summary = "获取所有资源列表", description = "获取所有启用的资源列表（扁平结构）")
    @GetMapping("/list")
    public Result<List<Resource>> getAllResources() {
        List<Resource> resources = resourceService.getAllResources();
        return Result.success(resources);
    }
    
    @Operation(summary = "按类型获取资源", description = "获取指定类型的资源列表")
    @GetMapping("/type/{type}")
    public Result<List<Resource>> getResourcesByType(
            @Parameter(description = "资源类型：MODULE/MENU/PAGE/ELEMENT/API/PERMISSION")
            @PathVariable String type) {
        List<Resource> resources = resourceService.getResourcesByType(type);
        return Result.success(resources);
    }
    
    @Operation(summary = "按模块获取资源树", description = "获取指定模块下的资源层级结构")
    @GetMapping("/module/{moduleCode}/tree")
    public Result<List<ResourceTreeDTO>> getResourceTreeByModule(
            @Parameter(description = "模块编码")
            @PathVariable String moduleCode) {
        List<ResourceTreeDTO> tree = resourceService.getResourceTreeByModule(moduleCode);
        return Result.success(tree);
    }
    
    @Operation(summary = "获取资源统计", description = "按类型统计资源数量")
    @GetMapping("/stats")
    public Result<List<ResourceStatsDTO>> getResourceStats() {
        List<ResourceStatsDTO> stats = resourceService.getResourceStats();
        return Result.success(stats);
    }
    
    @Operation(summary = "获取模块统计", description = "按模块统计各类资源数量")
    @GetMapping("/module-stats")
    public Result<List<ModuleStatsDTO>> getModuleStats() {
        List<ModuleStatsDTO> stats = resourceService.getModuleStats();
        return Result.success(stats);
    }
    
    @Operation(summary = "获取基本权限", description = "获取所有基本权限列表")
    @GetMapping("/basic")
    public Result<List<Resource>> getBasicPermissions() {
        List<Resource> permissions = resourceService.getBasicPermissions();
        return Result.success(permissions);
    }
    
    @Operation(summary = "根据权限标识查找", description = "根据 permission_key 查找资源")
    @GetMapping("/by-key/{permissionKey}")
    public Result<Resource> findByPermissionKey(
            @Parameter(description = "权限标识")
            @PathVariable String permissionKey) {
        Resource resource = resourceService.findByPermissionKey(permissionKey);
        if (resource == null) {
            return Result.error(404, "资源不存在");
        }
        return Result.success(resource);
    }
    
    @Operation(summary = "根据URI和Method查找API", description = "根据URI路径和HTTP方法查找API资源")
    @GetMapping("/api/by-uri")
    public Result<Resource> findByUriAndMethod(
            @Parameter(description = "URI路径")
            @RequestParam String uri,
            @Parameter(description = "HTTP方法")
            @RequestParam String method) {
        Resource resource = resourceService.findByUriAndMethod(uri, method);
        if (resource == null) {
            return Result.error(404, "API资源不存在");
        }
        return Result.success(resource);
    }
    
    @Operation(summary = "获取子资源", description = "获取指定资源的所有子节点")
    @GetMapping("/{parentId}/children")
    public Result<List<Resource>> getChildren(
            @Parameter(description = "父资源ID")
            @PathVariable Long parentId) {
        List<Resource> children = resourceService.getChildren(parentId);
        return Result.success(children);
    }
    
    @Operation(summary = "获取资源总览", description = "获取资源管理页面的总览数据")
    @GetMapping("/overview")
    public Result<Map<String, Object>> getResourceOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        // 资源统计
        overview.put("stats", resourceService.getResourceStats());
        
        // 模块统计
        overview.put("moduleStats", resourceService.getModuleStats());
        
        // 资源树
        overview.put("tree", resourceService.getResourceTree());
        
        // 基本权限
        overview.put("basicPermissions", resourceService.getBasicPermissions());
        
        return Result.success(overview);
    }
    
    @Operation(summary = "获取单个资源", description = "根据ID获取资源详情")
    @GetMapping("/{id}")
    public Result<Resource> getResourceById(
            @Parameter(description = "资源ID")
            @PathVariable Long id) {
        Resource resource = resourceService.getById(id);
        if (resource == null) {
            return Result.error(404, "资源不存在");
        }
        return Result.success(resource);
    }
    
    @Operation(summary = "更新资源", description = "更新资源的名称、显示顺序等（仅允许更新部分字段）")
    @PutMapping("/{id}")
    public Result<Resource> updateResource(
            @Parameter(description = "资源ID")
            @PathVariable Long id,
            @RequestBody Resource updates) {
        try {
            Resource updated = resourceService.updateResource(id, updates);
            return Result.success(updated);
        } catch (RuntimeException e) {
            log.error("更新资源失败: {}", e.getMessage());
            return Result.error(400, e.getMessage());
        }
    }
    
    @Operation(summary = "创建模块", description = "创建新的模块资源（仅支持MODULE类型）")
    @PostMapping
    public Result<Resource> createModule(@RequestBody Resource module) {
        try {
            module.setType("MODULE");  // 强制为模块类型
            Resource created = resourceService.createModule(module);
            return Result.success(created);
        } catch (RuntimeException e) {
            log.error("创建模块失败: {}", e.getMessage());
            return Result.error(400, e.getMessage());
        }
    }
    
    @Operation(summary = "删除资源", description = "删除资源（仅允许删除没有子资源的模块）")
    @DeleteMapping("/{id}")
    public Result<Void> deleteResource(
            @Parameter(description = "资源ID")
            @PathVariable Long id) {
        try {
            resourceService.deleteResource(id);
            return Result.success(null);
        } catch (RuntimeException e) {
            log.error("删除资源失败: {}", e.getMessage());
            return Result.error(400, e.getMessage());
        }
    }

    @Operation(summary = "获取关联 API", description = "获取与指定资源关联的所有 API 列表")
    @GetMapping("/{id}/related-apis")
    public Result<List<Resource>> getRelatedApis(
            @Parameter(description = "资源 ID")
            @PathVariable Long id) {
        try {
            List<Resource> apis = resourceService.getRelatedApis(id);
            return Result.success(apis);
        } catch (Exception e) {
            log.error("获取关联 API 失败：id={}", id, e);
            return Result.error(500, "获取关联 API 失败：" + e.getMessage());
        }
    }
}
