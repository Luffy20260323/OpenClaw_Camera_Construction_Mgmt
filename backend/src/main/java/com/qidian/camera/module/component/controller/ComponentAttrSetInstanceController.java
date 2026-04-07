package com.qidian.camera.module.component.controller;

import com.qidian.camera.common.response.Result;
import com.qidian.camera.module.auth.annotation.ApiPermission;
import com.qidian.camera.module.component.dto.ComponentAttrSetInstanceDTO;
import com.qidian.camera.module.component.service.ComponentAttrSetInstanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 零部件属性集实例控制器
 */
@Tag(name = "零部件属性集实例管理", description = "零部件属性集实例 CRUD 操作")
@Slf4j
@RestController
@RequestMapping("/component-attr-set-instances")
@RequiredArgsConstructor
public class ComponentAttrSetInstanceController {

    private final ComponentAttrSetInstanceService componentAttrSetInstanceService;

    @Operation(summary = "获取实例列表", description = "获取零部件属性集实例列表")
    @ApiPermission("system:attr-instance:view")
    @GetMapping
    public Result<List<ComponentAttrSetInstanceDTO>> getInstanceList(
            @RequestParam(required = false) Long attrSetId) {
        List<ComponentAttrSetInstanceDTO> instances = componentAttrSetInstanceService.getInstanceList(attrSetId);
        return Result.success(instances);
    }

    @Operation(summary = "获取实例详情", description = "获取指定实例的详细信息")
    @ApiPermission("system:attr-instance:view")
    @GetMapping("/{id}")
    public Result<ComponentAttrSetInstanceDTO> getInstanceDetail(@PathVariable Long id) {
        ComponentAttrSetInstanceDTO instance = componentAttrSetInstanceService.getInstanceDetail(id);
        return Result.success(instance);
    }

    @Operation(summary = "创建实例", description = "创建新的零部件属性集实例")
    @ApiPermission("system:attr-instance:create")
    @PostMapping
    public Result<ComponentAttrSetInstanceDTO> createInstance(
            @RequestBody ComponentAttrSetInstanceDTO dto,
            @RequestAttribute("userId") Long userId) {
        ComponentAttrSetInstanceDTO instance = componentAttrSetInstanceService.createInstance(dto, userId);
        return Result.success(instance);
    }

    @Operation(summary = "更新实例", description = "更新指定实例的信息")
    @ApiPermission("system:attr-instance:edit")
    @PutMapping("/{id}")
    public Result<ComponentAttrSetInstanceDTO> updateInstance(
            @PathVariable Long id,
            @RequestBody ComponentAttrSetInstanceDTO dto) {
        ComponentAttrSetInstanceDTO instance = componentAttrSetInstanceService.updateInstance(id, dto);
        return Result.success(instance);
    }

    @Operation(summary = "删除实例", description = "删除指定实例")
    @ApiPermission("system:attr-instance:delete")
    @DeleteMapping("/{id}")
    public Result<Void> deleteInstance(@PathVariable Long id) {
        componentAttrSetInstanceService.deleteInstance(id);
        return Result.success(null);
    }
}
