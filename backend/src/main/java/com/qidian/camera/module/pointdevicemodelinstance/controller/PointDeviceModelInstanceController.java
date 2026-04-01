package com.qidian.camera.module.pointdevicemodelinstance.controller;

import com.qidian.camera.common.response.Result;
import com.qidian.camera.module.auth.annotation.ApiPermission;
import com.qidian.camera.module.pointdevicemodelinstance.dto.PointDeviceModelInstanceDTO;
import com.qidian.camera.module.pointdevicemodelinstance.service.PointDeviceModelInstanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 点位设备模型实例控制器
 */
@Tag(name = "点位设备模型实例管理", description = "点位设备模型实例的增删改查")
@Slf4j
@RestController
@RequestMapping("/api/point-device-model-instances")
@RequiredArgsConstructor
public class PointDeviceModelInstanceController {

    private final PointDeviceModelInstanceService pointDeviceModelInstanceService;

    @Operation(summary = "获取点位设备模型实例列表", description = "获取所有点位设备模型实例，可按启用状态筛选")
    @ApiPermission("system:point-device-model-instance:view")
    @GetMapping
    public Result<List<PointDeviceModelInstanceDTO>> getInstanceList(
            @RequestParam(required = false) Boolean isActive) {
        List<PointDeviceModelInstanceDTO> instances = pointDeviceModelInstanceService.getInstanceList(isActive);
        return Result.success(instances);
    }

    @Operation(summary = "获取点位设备模型实例详情", description = "根据 ID 获取实例详细信息，包含所有实例项")
    @ApiPermission("system:point-device-model-instance:view")
    @GetMapping("/{id}")
    public Result<PointDeviceModelInstanceDTO> getInstanceDetail(@PathVariable Long id) {
        PointDeviceModelInstanceDTO instance = pointDeviceModelInstanceService.getInstanceDetail(id);
        return Result.success(instance);
    }

    @Operation(summary = "从模型创建点位设备模型实例", description = "根据指定的点位设备模型创建实例，同时复制模型项结构")
    @ApiPermission("system:point-device-model-instance:create")
    @PostMapping("/from-model")
    public Result<PointDeviceModelInstanceDTO> createInstanceFromModel(
            @Parameter(description = "模型 ID", required = true) @RequestParam Long modelId,
            @Parameter(description = "实例名称", required = true) @RequestParam String name,
            @Parameter(description = "实例编码", required = true) @RequestParam String code,
            @Parameter(description = "描述") @RequestParam(required = false) String description,
            @RequestAttribute("userId") Long userId) {
        PointDeviceModelInstanceDTO created = pointDeviceModelInstanceService.createInstanceFromModel(
                modelId, name, code, description, userId);
        return Result.success(created);
    }

    @Operation(summary = "创建点位设备模型实例", description = "新建点位设备模型实例，可自定义实例项")
    @ApiPermission("system:point-device-model-instance:create")
    @PostMapping
    public Result<PointDeviceModelInstanceDTO> createInstance(
            @RequestBody PointDeviceModelInstanceDTO dto,
            @RequestAttribute("userId") Long userId) {
        PointDeviceModelInstanceDTO created = pointDeviceModelInstanceService.createInstance(dto, userId);
        return Result.success(created);
    }

    @Operation(summary = "更新点位设备模型实例", description = "修改点位设备模型实例信息")
    @ApiPermission("system:point-device-model-instance:edit")
    @PutMapping("/{id}")
    public Result<PointDeviceModelInstanceDTO> updateInstance(
            @PathVariable Long id,
            @RequestBody PointDeviceModelInstanceDTO dto) {
        PointDeviceModelInstanceDTO updated = pointDeviceModelInstanceService.updateInstance(id, dto);
        return Result.success(updated);
    }

    @Operation(summary = "删除点位设备模型实例", description = "删除指定点位设备模型实例，同时删除所有实例项")
    @ApiPermission("system:point-device-model-instance:delete")
    @DeleteMapping("/{id}")
    public Result<Void> deleteInstance(@PathVariable Long id) {
        pointDeviceModelInstanceService.deleteInstance(id);
        return Result.success(null);
    }

    @Operation(summary = "更新实例项", description = "更换实例项关联的零部件实例")
    @ApiPermission("system:point-device-model-instance:edit")
    @PutMapping("/{id}/items/{itemId}")
    public Result<PointDeviceModelInstanceDTO> updateInstanceItem(
            @PathVariable("id") Long instanceId,
            @PathVariable("itemId") Long itemId,
            @Parameter(description = "新的零部件实例 ID", required = true) @RequestParam Long componentInstanceId) {
        PointDeviceModelInstanceDTO updated = pointDeviceModelInstanceService.updateInstanceItem(
                instanceId, itemId, componentInstanceId);
        return Result.success(updated);
    }
}
