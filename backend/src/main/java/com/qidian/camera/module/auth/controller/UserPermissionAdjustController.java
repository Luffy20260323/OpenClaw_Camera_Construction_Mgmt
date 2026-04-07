package com.qidian.camera.module.auth.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qidian.camera.common.response.Result;
import com.qidian.camera.module.auth.constant.RoleConstants;
import com.qidian.camera.module.auth.dto.RolePermissionAdjustDTO;
import com.qidian.camera.module.auth.entity.PermissionAuditLog;
import com.qidian.camera.module.auth.entity.Resource;
import com.qidian.camera.module.auth.entity.UserPermissionAdjustment;
import com.qidian.camera.module.auth.mapper.PermissionAuditLogMapper;
import com.qidian.camera.module.auth.mapper.UserPermissionAdjustmentMapper;
import com.qidian.camera.module.auth.service.PermissionService;
import com.qidian.camera.module.auth.service.ResourceService;
import com.qidian.camera.module.user.entity.User;
import com.qidian.camera.module.user.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * 用户权限调整 Controller
 * 用于调整用户个人的权限（独立于角色权限）
 */
@Slf4j
@Tag(name = "用户权限调整", description = "调整用户个人权限（增加/移除）")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserPermissionAdjustController {
    
    private final UserPermissionAdjustmentMapper userPermissionAdjustmentMapper;
    private final ResourceService resourceService;
    private final PermissionService permissionService;
    private final PermissionAuditLogMapper auditLogMapper;
    private final UserMapper userMapper;
    
    /**
     * 调整用户权限
     * 
     * @param userId 用户 ID
     * @param dto 调整请求（resourceId, action）
     * @return 操作结果
     */
    @Operation(summary = "调整用户权限", description = "增加或移除用户个人权限")
    @PostMapping("/{userId}/permissions/adjust")
    @Transactional
    public Result<Void> adjustUserPermission(
            @PathVariable Long userId,
            @RequestBody RolePermissionAdjustDTO dto) {
        
        // 检查是否为 admin 用户（特殊保护）
        User user = userMapper.selectById(userId);
        if (user != null && "admin".equals(user.getUsername())) {
            return Result.error("admin 用户权限不可调整");
        }
        
        Long resourceId = dto.getResourceId();
        String action = dto.getAction();
        
        // 检查资源是否存在
        Resource resource = resourceService.getById(resourceId);
        if (resource == null) {
            return Result.error("资源不存在");
        }
        
        // 检查是否已存在调整记录
        UserPermissionAdjustment existing = userPermissionAdjustmentMapper.selectOne(
            new LambdaQueryWrapper<UserPermissionAdjustment>()
                .eq(UserPermissionAdjustment::getUserId, userId)
                .eq(UserPermissionAdjustment::getResourceId, resourceId)
        );
        
        if (existing != null) {
            // 更新现有调整记录
            if (existing.getAction().equals(action)) {
                // 相同操作，删除调整记录（恢复原状态）
                userPermissionAdjustmentMapper.deleteById(existing.getId());
            } else {
                // 相反操作，更新调整记录
                existing.setAction(action);
                userPermissionAdjustmentMapper.updateById(existing);
            }
        } else {
            // 创建新的调整记录
            UserPermissionAdjustment adjustment = new UserPermissionAdjustment();
            adjustment.setUserId(userId);
            adjustment.setResourceId(resourceId);
            adjustment.setAction(action);
            // TODO: 从上下文获取当前用户 ID
            // adjustment.setCreatedBy(0L); // 字段不存在
            userPermissionAdjustmentMapper.insert(adjustment);
        }
        
        // 清除用户权限缓存（用户权限调整）
        permissionService.evictUserPermissionCache(userId);
        
        // 记录审计日志
        recordAuditLog(userId, resourceId, action, "USER");
        
        log.info("调整用户权限：userId={}, resourceId={}, action={}", userId, resourceId, action);
        return Result.success();
    }
    
    /**
     * 批量调整用户权限
     * 
     * @param userId 用户 ID
     * @param adjustments 调整请求列表
     * @return 操作结果
     */
    @Operation(summary = "批量调整用户权限", description = "批量增加或移除用户个人权限")
    @PostMapping("/{userId}/permissions/adjust-batch")
    @Transactional
    public Result<Void> adjustUserPermissionBatch(
            @PathVariable Long userId,
            @RequestBody java.util.List<RolePermissionAdjustDTO> adjustments) {
        
        for (RolePermissionAdjustDTO dto : adjustments) {
            adjustUserPermission(userId, dto);
        }
        
        return Result.success();
    }
    
    /**
     * 记录审计日志
     */
    private void recordAuditLog(Long targetId, Long resourceId, String action, String targetType) {
        PermissionAuditLog auditLog = PermissionAuditLog.builder()
            .targetType(targetType)
            .targetId(targetId)
            .operationType(action.equals("ADD") ? "GRANT" : "REVOKE")
            .changeDescription("调整" + targetType + "权限：resourceId=" + resourceId)
            .operatorId(0L) // TODO: 从上下文获取当前用户 ID
            .build();
        auditLogMapper.insert(auditLog);
    }
}
