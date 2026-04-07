package com.qidian.camera.module.component.controller;

import com.qidian.camera.common.response.Result;
import com.qidian.camera.module.auth.annotation.ApiPermission;
import com.qidian.camera.module.component.dto.ComponentAttrSetDTO;
import com.qidian.camera.module.component.service.ComponentAttrSetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 零部件属性集控制器
 */
@Tag(name = "零部件属性集管理", description = "零部件属性集的增删改查")
@Slf4j
@RestController
@RequestMapping("/component-attr-sets")
@RequiredArgsConstructor
public class ComponentAttrSetController {

    private final ComponentAttrSetService componentAttrSetService;

    @Operation(summary = "获取属性集列表", description = "获取零部件属性集列表，支持按种类和启用状态筛选")
    @ApiPermission("component-attr-set:view")
    @GetMapping
    public Result<List<ComponentAttrSetDTO>> getAttrSetList(
            @RequestParam(required = false) Long componentTypeId,
            @RequestParam(required = false) Boolean isActive) {
        List<ComponentAttrSetDTO> attrSets = componentAttrSetService.getAttrSetList(componentTypeId, isActive);
        return Result.success(attrSets);
    }

    @Operation(summary = "获取属性集详情", description = "获取指定属性集的详细信息，包含所有属性定义")
    @ApiPermission("component-attr-set:view")
    @GetMapping("/{id}")
    public Result<ComponentAttrSetDTO> getAttrSetDetail(@PathVariable Long id) {
        ComponentAttrSetDTO attrSet = componentAttrSetService.getAttrSetDetail(id);
        return Result.success(attrSet);
    }

    @Operation(summary = "创建属性集", description = "创建新的零部件属性集")
    @ApiPermission("component-attr-set:create")
    @PostMapping
    public Result<ComponentAttrSetDTO> createAttrSet(
            @RequestBody ComponentAttrSetDTO dto,
            @RequestAttribute("userId") Long userId) {
        ComponentAttrSetDTO created = componentAttrSetService.createAttrSet(dto, userId);
        return Result.success(created);
    }

    @Operation(summary = "更新属性集", description = "更新指定属性集的信息")
    @ApiPermission("component-attr-set:edit")
    @PutMapping("/{id}")
    public Result<ComponentAttrSetDTO> updateAttrSet(
            @PathVariable Long id,
            @RequestBody ComponentAttrSetDTO dto) {
        ComponentAttrSetDTO updated = componentAttrSetService.updateAttrSet(id, dto);
        return Result.success(updated);
    }

    @Operation(summary = "删除属性集", description = "删除指定的零部件属性集")
    @ApiPermission("component-attr-set:delete")
    @DeleteMapping("/{id}")
    public Result<Void> deleteAttrSet(@PathVariable Long id) {
        componentAttrSetService.deleteAttrSet(id);
        return Result.success(null);
    }

    @Operation(summary = "更新属性集序号", description = "更新零部件属性集的显示序号")
    @ApiPermission("component-attr-set:edit")
    @PutMapping("/{id}/sequence")
    public Result<ComponentAttrSetDTO> updateAttrSetSequence(
            @PathVariable Long id,
            @RequestParam Integer sequenceNo) {
        ComponentAttrSetDTO updated = componentAttrSetService.updateAttrSetSequence(id, sequenceNo);
        return Result.success(updated);
    }
}
