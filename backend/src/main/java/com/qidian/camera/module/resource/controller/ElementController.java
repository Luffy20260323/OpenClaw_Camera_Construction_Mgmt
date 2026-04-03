package com.qidian.camera.module.resource.controller;

import com.qidian.camera.common.response.Result;
import com.qidian.camera.module.auth.annotation.ApiPermission;
import com.qidian.camera.module.resource.dto.ElementDTO;
import com.qidian.camera.module.resource.dto.ElementTreeDTO;
import com.qidian.camera.module.resource.service.ElementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ELEMENT 资源控制器
 * 提供 ELEMENT 层级的 CRUD API
 */
@Tag(name = "ELEMENT 管理", description = "ELEMENT 资源管理 API - 按钮、链接等操作元素")
@RestController
@RequestMapping("/element")
@RequiredArgsConstructor
@Slf4j
public class ElementController {
    
    private final ElementService elementService;
    
    @Operation(summary = "获取 ELEMENT 列表", description = "获取 ELEMENT 列表（支持按 PAGE 过滤）")
    @GetMapping("/list")
    @ApiPermission("system:element:view")
    public Result<List<ElementDTO>> getElementList(
            @Parameter(description = "页面 ID（可选，用于过滤）")
            @RequestParam(required = false) Long pageId) {
        List<ElementDTO> elements = elementService.getElementList(pageId);
        return Result.success(elements);
    }
    
    @Operation(summary = "获取 ELEMENT 树", description = "获取 ELEMENT 树（ELEMENT → 子 PAGE）")
    @GetMapping("/tree")
    @ApiPermission("system:element:view")
    public Result<List<ElementTreeDTO>> getElementTree(
            @Parameter(description = "页面 ID（可选，用于过滤）")
            @RequestParam(required = false) Long pageId) {
        List<ElementTreeDTO> tree = elementService.getElementTree(pageId);
        return Result.success(tree);
    }
    
    @Operation(summary = "获取某页面的所有 ELEMENT", description = "获取指定页面的所有操作元素")
    @GetMapping("/page/{pageId}")
    @ApiPermission("system:element:view")
    public Result<List<ElementDTO>> getElementsByPageId(
            @Parameter(description = "页面 ID")
            @PathVariable Long pageId) {
        List<ElementDTO> elements = elementService.getElementsByPageId(pageId);
        return Result.success(elements);
    }
    
    @Operation(summary = "获取 ELEMENT 详情", description = "根据 ID 获取 ELEMENT 详情")
    @GetMapping("/{id}")
    @ApiPermission("system:element:view")
    public Result<ElementDTO> getElementById(
            @Parameter(description = "ELEMENT ID")
            @PathVariable Long id) {
        ElementDTO element = elementService.getElementById(id);
        if (element == null) {
            return Result.error(404, "ELEMENT 不存在");
        }
        return Result.success(element);
    }
    
    @Operation(summary = "创建 ELEMENT", description = "创建新的操作元素")
    @PostMapping
    @ApiPermission("system:element:create")
    public Result<ElementDTO> createElement(@RequestBody ElementDTO dto) {
        try {
            ElementDTO created = elementService.createElement(dto);
            return Result.success(created);
        } catch (RuntimeException e) {
            log.error("创建 ELEMENT 失败：{}", e.getMessage());
            return Result.error(400, e.getMessage());
        }
    }
    
    @Operation(summary = "更新 ELEMENT", description = "更新 ELEMENT 的信息")
    @PutMapping("/{id}")
    @ApiPermission("system:element:edit")
    public Result<ElementDTO> updateElement(
            @Parameter(description = "ELEMENT ID")
            @PathVariable Long id,
            @RequestBody ElementDTO dto) {
        try {
            ElementDTO updated = elementService.updateElement(id, dto);
            return Result.success(updated);
        } catch (RuntimeException e) {
            log.error("更新 ELEMENT 失败：{}", e.getMessage());
            return Result.error(400, e.getMessage());
        }
    }
    
    @Operation(summary = "删除 ELEMENT", description = "删除指定的操作元素")
    @DeleteMapping("/{id}")
    @ApiPermission("system:element:delete")
    public Result<Void> deleteElement(
            @Parameter(description = "ELEMENT ID")
            @PathVariable Long id) {
        try {
            elementService.deleteElement(id);
            return Result.success(null);
        } catch (RuntimeException e) {
            log.error("删除 ELEMENT 失败：{}", e.getMessage());
            return Result.error(400, e.getMessage());
        }
    }
}
