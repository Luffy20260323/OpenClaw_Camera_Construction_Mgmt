package com.qidian.camera.module.auth.controller;

import com.qidian.camera.common.response.Result;
import com.qidian.camera.module.auth.dto.*;
import com.qidian.camera.module.auth.entity.Resource;
import com.qidian.camera.module.auth.service.*;
import com.qidian.camera.module.menu.dto.MenuDTO;
import com.qidian.camera.module.menu.service.MenuService;
import com.qidian.camera.module.auth.annotation.ApiPermission;
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
@RequestMapping("/resources")
@RequiredArgsConstructor
public class ResourceController {
    
    private final ResourceService resourceService;
    private final MenuService menuService;
    
    /**
     * 获取资源树
     */
    @Operation(summary = "获取资源树", description = "返回所有资源的树形结构")
    @ApiPermission("system:resource:view")
    @GetMapping("/tree")
    public Result<List<ResourceService.ResourceTreeNode>> getResourceTree() {
        return Result.success(resourceService.getResourceTree());
    }
    
    /**
     * 获取菜单树
     */
    @Operation(summary = "获取菜单树", description = "返回模块和菜单的树形结构，用于前端菜单渲染")
    @ApiPermission("system:resource:view")
    @GetMapping("/menu-tree")
    public Result<List<ResourceService.ResourceTreeNode>> getMenuTree() {
        return Result.success(resourceService.getMenuTree());
    }
    
    /**
     * 获取当前用户的菜单树（权限过滤）
     * 用于刷新菜单时获取用户专属菜单，与登录时返回的菜单一致
     */
    @Operation(summary = "获取我的菜单树", description = "返回当前用户有权访问的菜单树，用于前端刷新菜单")
    @ApiPermission("system:resource:view")
    @GetMapping("/my-menu-tree")
    public Result<List<MenuDTO>> getMyMenuTree(@RequestAttribute("userId") Long userId) {
        List<MenuDTO> userMenus = menuService.getUserMenus(userId);
        return Result.success(userMenus);
    }
    
    /**
     * 获取所有资源（支持分页）
     */
    @Operation(summary = "获取资源列表", description = "返回资源列表，支持分页和过滤")
    @ApiPermission("system:resource:view")
    @GetMapping
    public Result<Map<String, Object>> getAllResources(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String search) {
        return Result.success(resourceService.getResourcesPaged(page, size, type, search));
    }
    
    /**
     * 获取单个资源
     */
    @Operation(summary = "获取单个资源", description = "根据 ID 获取资源详情")
    @ApiPermission("system:resource:view")
    @GetMapping("/{id}")
    public Result<Resource> getResource(@PathVariable Long id) {
        Resource resource = resourceService.getById(id);
        if (resource == null) {
            return Result.error("资源不存在");
        }
        return Result.success(resource);
    }
    
    /**
     * 创建菜单
     */
    @Operation(summary = "创建菜单", description = "创建新的菜单资源")
    @ApiPermission("system:resource:create")
    @PostMapping("/menu")
    public Result<Resource> createMenu(@RequestBody ResourceDTO dto) {
        // 检查 code 是否重复
        if (resourceService.getByCode(dto.getCode()) != null) {
            return Result.error("资源编码已存在");
        }
        
        Resource resource = new Resource();
        resource.setName(dto.getName());
        resource.setCode(dto.getCode());
        resource.setType("MENU");
        resource.setParentId(dto.getParentId());
        resource.setPermissionKey(dto.getPermissionKey());
        resource.setIcon(dto.getIcon());
        // MENU 类型不需要 path 字段（路由由 PAGE 资源控制）
        // resource.setPath(dto.getPath());
        resource.setComponent(dto.getComponent());
        resource.setSortOrder(dto.getSortOrder());
        resource.setIsBasic(0);
        resource.setIsTopLevel(false);
        resource.setFilePath(null);
        resource.setDescription(dto.getDescription());
        
        return Result.success(resourceService.create(resource));
    }
    
    /**
     * 更新资源
     */
    @Operation(summary = "更新资源", description = "更新资源信息（支持名称、父资源、描述等）")
    @ApiPermission("system:resource:edit")
    @PutMapping("/{id}")
    public Result<Resource> updateResource(@PathVariable Long id, @RequestBody ResourceDTO dto) {
        Resource resource = resourceService.getById(id);
        if (resource == null) {
            return Result.error("资源不存在");
        }
        
        // 更新名称
        if (dto.getName() != null) {
            resource.setName(dto.getName());
        }
        // 更新父资源
        if (dto.getParentId() != null) {
            resource.setParentId(dto.getParentId());
        }
        // 更新描述
        if (dto.getDescription() != null) {
            resource.setDescription(dto.getDescription());
        }
        // 更新其他字段
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
        if (dto.getIsTopLevel() != null) {
            resource.setIsTopLevel(dto.getIsTopLevel());
        }
        if (dto.getFilePath() != null) {
            resource.setFilePath(dto.getFilePath());
        }
        if (dto.getRequiredPermission() != null) {
            resource.setRequiredPermission(dto.getRequiredPermission());
        }
        
        return Result.success(resourceService.update(resource));
    }
    
    /**
     * 删除资源
     * 模块和菜单：只删除本身，子资源变为未分类
     * 其他资源：级联删除子资源
     */
    @Operation(summary = "删除资源", description = "模块/菜单只删除本身，其他资源级联删除")
    @ApiPermission("system:resource:delete")
    @DeleteMapping("/{id}")
    public Result<Map<String, Object>> deleteResource(@PathVariable Long id) {
        Resource resource = resourceService.getById(id);
        if (resource == null) {
            return Result.error("资源不存在");
        }
        
        // 基本权限不可删除
        if (resource.getIsBasic() == 1) {
            return Result.error("基本权限资源不可删除");
        }
        
        // 获取子资源信息（用于返回）
        int childCount = resourceService.getChildCount(id);
        List<Resource> children = resourceService.getChildren(id);
        
        resourceService.delete(id);
        
        // 返回删除结果
        Map<String, Object> result = new HashMap<>();
        result.put("deletedId", id);
        result.put("deletedName", resource.getName());
        result.put("deletedType", resource.getType());
        result.put("childCount", childCount);
        result.put("children", children.stream().map(c -> {
            Map<String, Object> child = new HashMap<>();
            child.put("id", c.getId());
            child.put("name", c.getName());
            child.put("code", c.getCode());
            child.put("type", c.getType());
            return child;
        }).toList());
        
        return Result.success(result);
    }
    
    /**
     * 获取删除预览信息
     */
    @Operation(summary = "删除预览", description = "获取删除资源前的预览信息")
    @ApiPermission("system:resource:view")
    @GetMapping("/{id}/delete-preview")
    public Result<Map<String, Object>> getDeletePreview(@PathVariable Long id) {
        Resource resource = resourceService.getById(id);
        if (resource == null) {
            return Result.error("资源不存在");
        }
        
        int childCount = resourceService.getChildCount(id);
        List<Resource> children = resourceService.getChildren(id);
        
        Map<String, Object> preview = new HashMap<>();
        preview.put("id", id);
        preview.put("name", resource.getName());
        preview.put("code", resource.getCode());
        preview.put("type", resource.getType());
        preview.put("childCount", childCount);
        preview.put("children", children.stream().map(c -> {
            Map<String, Object> child = new HashMap<>();
            child.put("id", c.getId());
            child.put("name", c.getName());
            child.put("code", c.getCode());
            child.put("type", c.getType());
            return child;
        }).toList());
        
        // 模块和菜单删除时子资源父级置空
        if (resource.getType().equals("MODULE") || resource.getType().equals("MENU")) {
            preview.put("deleteMode", "selfOnly");
            preview.put("message", "删除后将把 " + childCount + " 个子资源变为未分类状态");
        } else {
            preview.put("deleteMode", "cascade");
            preview.put("message", "将同时删除 " + childCount + " 个子资源");
        }
        
        return Result.success(preview);
    }
    
    /**
     * 更新资源父级
     */
    @Operation(summary = "更新父级", description = "更新资源的父级（用于重新分类未分类资源）")
    @ApiPermission("system:resource:edit")
    @PutMapping("/{id}/parent")
    public Result<Void> updateParent(@PathVariable Long id, @RequestBody Map<String, Long> request) {
        Long newParentId = request.get("parentId");
        Resource resource = resourceService.getById(id);
        if (resource == null) {
            return Result.error("资源不存在");
        }
        
        // 如果指定了新父级，检查是否存在
        if (newParentId != null) {
            Resource parent = resourceService.getById(newParentId);
            if (parent == null) {
                return Result.error("父级资源不存在");
            }
            // 检查是否形成循环
            if (newParentId.equals(id)) {
                return Result.error("不能将自己设为父级");
            }
        }
        
        resourceService.updateParent(id, newParentId);
        return Result.success();
    }
    
    /**
     * 获取基本权限资源
     */
    @Operation(summary = "获取基本权限", description = "返回所有基本权限资源")
    @ApiPermission("system:resource:view")
    @GetMapping("/basic")
    public Result<List<Resource>> getBasicResources() {
        return Result.success(resourceService.getBasicResources());
    }
    
    /**
     * 刷新资源缓存
     */
    @Operation(summary = "刷新资源缓存", description = "清除并重新加载资源缓存")
    @PostMapping("/refresh-cache")
    public Result<Void> refreshCache() {
        resourceService.refreshCache();
        return Result.success();
    }

    /**
     * 获取孤儿资源（没有父资源的资源）
     */
    @Operation(summary = "获取孤儿资源", description = "返回没有父资源的资源列表")
    @GetMapping("/orphans")
    public Result<List<Resource>> getOrphanResources() {
        List<Resource> orphans = resourceService.getOrphanResources();
        return Result.success(orphans);
    }
}