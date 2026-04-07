package com.qidian.camera.module.auth.controller;

import com.qidian.camera.common.response.Result;
import com.qidian.camera.module.auth.entity.PermissionAuditLog;
import com.qidian.camera.module.auth.service.PermissionAuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计日志控制器
 *
 * @author Camera Team
 * @since 2026-04-04
 */
@RestController
@RequestMapping("/audit-logs")
@RequiredArgsConstructor
@Tag(name = "审计日志管理", description = "审计日志查询和导出")
public class AuditLogController {

    private final PermissionAuditLogService permissionAuditLogService;

    /**
     * 获取权限审计日志列表
     */
    @GetMapping("/permission")
    @Operation(summary = "获取权限审计日志列表")
    public Result<List<PermissionAuditLog>> getPermissionAuditLogs(
            @Parameter(description = "操作类型") @RequestParam(required = false) String operationType,
            @Parameter(description = "目标类型") @RequestParam(required = false) String targetType,
            @Parameter(description = "目标 ID") @RequestParam(required = false) Long targetId,
            @Parameter(description = "操作人 ID") @RequestParam(required = false) Long operatorId,
            @Parameter(description = "操作人姓名") @RequestParam(required = false) String operatorName,
            @Parameter(description = "开始时间") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer pageSize) {

        List<PermissionAuditLog> logs = permissionAuditLogService.queryLogs(
            operationType, targetType, targetId, operatorId, operatorName, startTime, endTime, pageNum, pageSize);
        return Result.success(logs);
    }

    /**
     * 获取权限审计日志详情
     */
    @GetMapping("/permission/{id}")
    @Operation(summary = "获取权限审计日志详情")
    public Result<PermissionAuditLog> getPermissionAuditLogDetail(
            @Parameter(description = "日志 ID") @PathVariable Long id) {

        PermissionAuditLog log = permissionAuditLogService.getById(id);
        return Result.success(log);
    }

    /**
     * 获取资源审计日志
     */
    @GetMapping("/resource/{resourceId}")
    @Operation(summary = "获取资源审计日志")
    public Result<List<PermissionAuditLog>> getResourceAuditLogs(
            @Parameter(description = "资源 ID") @PathVariable Long resourceId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer pageSize) {

        List<PermissionAuditLog> logs = permissionAuditLogService.queryByResourceId(resourceId, pageNum, pageSize);
        return Result.success(logs);
    }

    /**
     * 获取角色审计日志
     */
    @GetMapping("/role/{roleId}")
    @Operation(summary = "获取角色审计日志")
    public Result<List<PermissionAuditLog>> getRoleAuditLogs(
            @Parameter(description = "角色 ID") @PathVariable Long roleId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer pageSize) {

        List<PermissionAuditLog> logs = permissionAuditLogService.queryByRoleId(roleId, pageNum, pageSize);
        return Result.success(logs);
    }

    /**
     * 获取用户审计日志
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户审计日志")
    public Result<List<PermissionAuditLog>> getUserAuditLogs(
            @Parameter(description = "用户 ID") @PathVariable Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer pageSize) {

        List<PermissionAuditLog> logs = permissionAuditLogService.queryByUserId(userId, pageNum, pageSize);
        return Result.success(logs);
    }

    /**
     * 获取操作类型列表
     */
    @GetMapping("/operation-types")
    @Operation(summary = "获取操作类型列表")
    public Result<List<String>> getOperationTypes() {
        List<String> operationTypes = permissionAuditLogService.getOperationTypes();
        return Result.success(operationTypes);
    }

    /**
     * 获取目标类型列表
     */
    @GetMapping("/target-types")
    @Operation(summary = "获取目标类型列表")
    public Result<List<String>> getTargetTypes() {
        List<String> targetTypes = permissionAuditLogService.getTargetTypes();
        return Result.success(targetTypes);
    }
}
