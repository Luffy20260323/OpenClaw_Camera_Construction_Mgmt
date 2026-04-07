package com.qidian.camera.module.component.controller;

import com.qidian.camera.common.response.Result;
import com.qidian.camera.module.auth.annotation.ApiPermission;
import com.qidian.camera.module.component.dto.ComponentInstanceDTO;
import com.qidian.camera.module.component.service.ComponentInstanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 零部件实例控制器
 */
@Tag(name = "零部件实例管理", description = "零部件实例 CRUD 操作及状态管理")
@Slf4j
@RestController
@RequestMapping("/component-instances")
@RequiredArgsConstructor
public class ComponentInstanceController {

    private final ComponentInstanceService componentInstanceService;

    @Operation(summary = "获取实例列表", description = "获取零部件实例列表，支持按种类和状态筛选")
    @ApiPermission("system:component-instance:view")
    @GetMapping
    public Result<List<ComponentInstanceDTO>> getInstanceList(
            @Parameter(description = "零部件种类 ID（可选）") @RequestParam(required = false) Long componentTypeId,
            @Parameter(description = "状态（可选）：normal-正常使用，replaced-已更换，scrapped-已报废") @RequestParam(required = false) String status) {
        List<ComponentInstanceDTO> instances = componentInstanceService.getInstanceList(componentTypeId, status);
        return Result.success(instances);
    }

    @Operation(summary = "获取实例详情", description = "获取指定零部件实例的详细信息")
    @ApiPermission("system:component-instance:view")
    @GetMapping("/{id}")
    public Result<ComponentInstanceDTO> getInstanceDetail(@PathVariable Long id) {
        ComponentInstanceDTO instance = componentInstanceService.getInstanceDetail(id);
        return Result.success(instance);
    }

    @Operation(summary = "创建实例", description = "创建新的零部件实例")
    @ApiPermission("system:component-instance:create")
    @PostMapping
    public Result<ComponentInstanceDTO> createInstance(
            @RequestBody ComponentInstanceDTO dto,
            @RequestAttribute("userId") Long userId) {
        ComponentInstanceDTO instance = componentInstanceService.createInstance(dto, userId);
        return Result.success(instance);
    }

    @Operation(summary = "更新实例", description = "更新指定零部件实例的信息")
    @ApiPermission("system:component-instance:edit")
    @PutMapping("/{id}")
    public Result<ComponentInstanceDTO> updateInstance(
            @PathVariable Long id,
            @RequestBody ComponentInstanceDTO dto) {
        ComponentInstanceDTO instance = componentInstanceService.updateInstance(id, dto);
        return Result.success(instance);
    }

    @Operation(summary = "删除实例", description = "删除指定零部件实例")
    @ApiPermission("system:component-instance:delete")
    @DeleteMapping("/{id}")
    public Result<Void> deleteInstance(@PathVariable Long id) {
        componentInstanceService.deleteInstance(id);
        return Result.success(null);
    }

    @Operation(summary = "更新实例状态", description = "更新零部件实例的状态")
    @ApiPermission("system:component-instance:edit")
    @PutMapping("/{id}/status")
    public Result<ComponentInstanceDTO> updateStatus(
            @PathVariable Long id,
            @Parameter(description = "新状态：normal-正常使用，replaced-已更换，scrapped-已报废") @RequestParam String status) {
        ComponentInstanceDTO instance = componentInstanceService.updateStatus(id, status);
        return Result.success(instance);
    }
}
