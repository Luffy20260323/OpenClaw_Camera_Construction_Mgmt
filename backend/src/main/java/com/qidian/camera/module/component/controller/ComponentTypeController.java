package com.qidian.camera.module.component.controller;

import com.qidian.camera.common.response.Result;
import com.qidian.camera.module.auth.annotation.ApiPermission;
import com.qidian.camera.module.component.dto.ComponentTypeDTO;
import com.qidian.camera.module.component.service.ComponentTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 零部件种类控制器
 */
@Tag(name = "零部件种类管理", description = "零部件种类的增删改查")
@Slf4j
@RestController
@RequestMapping("/component-types")
@RequiredArgsConstructor
public class ComponentTypeController {

    private final ComponentTypeService componentTypeService;

    @Operation(summary = "获取零部件种类列表", description = "获取所有零部件种类，可按启用状态筛选")
    @ApiPermission("system:component:view")
    @GetMapping
    public Result<List<ComponentTypeDTO>> getComponentTypeList(
            @RequestParam(required = false) Boolean isActive) {
        List<ComponentTypeDTO> componentTypes = componentTypeService.getComponentTypeList(isActive);
        return Result.success(componentTypes);
    }

    @Operation(summary = "获取零部件种类详情", description = "根据 ID 获取种类详细信息")
    @ApiPermission("system:component:view")
    @GetMapping("/{id}")
    public Result<ComponentTypeDTO> getComponentTypeDetail(@PathVariable Long id) {
        ComponentTypeDTO componentType = componentTypeService.getComponentTypeDetail(id);
        return Result.success(componentType);
    }

    @Operation(summary = "创建零部件种类", description = "新建零部件种类")
    @ApiPermission("system:component:create")
    @PostMapping
    public Result<ComponentTypeDTO> createComponentType(
            @RequestBody ComponentTypeDTO dto,
            @RequestAttribute("userId") Long userId) {
        ComponentTypeDTO created = componentTypeService.createComponentType(dto, userId);
        return Result.success(created);
    }

    @Operation(summary = "更新零部件种类", description = "修改零部件种类信息")
    @ApiPermission("system:component:edit")
    @PutMapping("/{id}")
    public Result<ComponentTypeDTO> updateComponentType(
            @PathVariable Long id,
            @RequestBody ComponentTypeDTO dto) {
        ComponentTypeDTO updated = componentTypeService.updateComponentType(id, dto);
        return Result.success(updated);
    }

    @Operation(summary = "删除零部件种类", description = "删除指定零部件种类")
    @ApiPermission("system:component:delete")
    @DeleteMapping("/{id}")
    public Result<Void> deleteComponentType(@PathVariable Long id) {
        componentTypeService.deleteComponentType(id);
        return Result.success(null);
    }

    @Operation(summary = "切换零部件种类状态", description = "切换零部件种类的启用/禁用状态")
    @ApiPermission("system:component:edit")
    @PatchMapping("/{id}/status")
    public Result<ComponentTypeDTO> toggleComponentTypeStatus(@PathVariable Long id) {
        ComponentTypeDTO updated = componentTypeService.toggleComponentTypeStatus(id);
        return Result.success(updated);
    }
}
