package com.qidian.camera.module.pointdevicemodel.controller;

import com.qidian.camera.common.response.Result;
import com.qidian.camera.module.auth.annotation.ApiPermission;
import com.qidian.camera.module.pointdevicemodel.dto.PointDeviceModelDTO;
import com.qidian.camera.module.pointdevicemodel.service.PointDeviceModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 点位设备模型控制器
 */
@Tag(name = "点位设备模型管理", description = "点位设备模型的增删改查")
@Slf4j
@RestController
@RequestMapping("/point-device-models")
@RequiredArgsConstructor
public class PointDeviceModelController {

    private final PointDeviceModelService pointDeviceModelService;

    @Operation(summary = "获取点位设备模型列表", description = "获取所有点位设备模型，可按启用状态筛选")
    @ApiPermission("system:point-device-model:view")
    @GetMapping
    public Result<List<PointDeviceModelDTO>> getModelList(
            @RequestParam(required = false) Boolean isActive) {
        List<PointDeviceModelDTO> models = pointDeviceModelService.getModelList(isActive);
        return Result.success(models);
    }

    @Operation(summary = "获取点位设备模型详情", description = "根据 ID 获取模型详细信息，包含所有模型项")
    @ApiPermission("system:point-device-model:view")
    @GetMapping("/{id}")
    public Result<PointDeviceModelDTO> getModelDetail(@PathVariable Long id) {
        PointDeviceModelDTO model = pointDeviceModelService.getModelDetail(id);
        return Result.success(model);
    }

    @Operation(summary = "创建点位设备模型", description = "新建点位设备模型，可同时创建模型项")
    @ApiPermission("system:point-device-model:create")
    @PostMapping
    public Result<PointDeviceModelDTO> createModel(
            @RequestBody PointDeviceModelDTO dto,
            @RequestAttribute("userId") Long userId) {
        PointDeviceModelDTO created = pointDeviceModelService.createModel(dto, userId);
        return Result.success(created);
    }

    @Operation(summary = "更新点位设备模型", description = "修改点位设备模型信息，可同时更新模型项")
    @ApiPermission("system:point-device-model:edit")
    @PutMapping("/{id}")
    public Result<PointDeviceModelDTO> updateModel(
            @PathVariable Long id,
            @RequestBody PointDeviceModelDTO dto) {
        PointDeviceModelDTO updated = pointDeviceModelService.updateModel(id, dto);
        return Result.success(updated);
    }

    @Operation(summary = "删除点位设备模型", description = "删除指定点位设备模型，同时删除所有模型项")
    @ApiPermission("system:point-device-model:delete")
    @DeleteMapping("/{id}")
    public Result<Void> deleteModel(@PathVariable Long id) {
        pointDeviceModelService.deleteModel(id);
        return Result.success(null);
    }
}
