package com.qidian.camera.module.pointbatchassignment.controller;

import com.qidian.camera.common.response.Result;
import com.qidian.camera.module.auth.annotation.ApiPermission;
import com.qidian.camera.module.pointbatchassignment.dto.BatchAssignmentRequest;
import com.qidian.camera.module.pointbatchassignment.dto.PointDTO;
import com.qidian.camera.module.pointbatchassignment.dto.PointDeviceAssignmentDTO;
import com.qidian.camera.module.pointbatchassignment.service.PointBatchAssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 点位批量分配控制器
 */
@Tag(name = "点位批量分配管理", description = "点位设备模型实例的批量分配")
@Slf4j
@RestController
@RequestMapping("/point-batch-assignment")
@RequiredArgsConstructor
public class PointBatchAssignmentController {

    private final PointBatchAssignmentService pointBatchAssignmentService;

    @Operation(summary = "获取未分配的点位列表", description = "获取所有未分配设备的点位，用于批量分配")
    @ApiPermission("system:point-batch-assignment:view")
    @GetMapping("/available-points")
    public Result<List<PointDTO>> getAvailablePoints() {
        List<PointDTO> points = pointBatchAssignmentService.getAvailablePoints();
        return Result.success(points);
    }

    @Operation(summary = "批量分配设备模型实例给点位", description = "将指定的设备模型实例批量分配给多个点位")
    @ApiPermission("system:point-batch-assignment:execute")
    @PostMapping
    public Result<Integer> batchAssign(
            @RequestBody BatchAssignmentRequest request,
            @RequestAttribute("userId") Long userId) {
        int successCount = pointBatchAssignmentService.batchAssign(request, userId);
        return Result.success("分配成功 " + successCount + " 个点位", successCount);
    }

    @Operation(summary = "获取点位设备配置", description = "根据点位 ID 获取该点位的设备分配信息")
    @ApiPermission("system:point-batch-assignment:view")
    @GetMapping("/point/{pointId}")
    public Result<PointDeviceAssignmentDTO> getPointDeviceConfig(@PathVariable Long pointId) {
        PointDeviceAssignmentDTO config = pointBatchAssignmentService.getPointDeviceConfig(pointId);
        return Result.success(config);
    }
}
