package com.qidian.camera.module.auth.controller;

import com.qidian.camera.common.response.Result;
import com.qidian.camera.module.role.entity.Role;
import com.qidian.camera.module.auth.entity.RolePermission;
import com.qidian.camera.module.role.mapper.RoleMapper;
import com.qidian.camera.module.auth.mapper.RolePermissionMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 权限复制控制器
 * 提供角色权限复制、批量授权等功能
 * 注意：permission 表只有 role_id + permission_id + grant_level 等字段
 */
@Tag(name = "权限复制", description = "角色权限复制和批量授权")
@RestController
@RequestMapping("/permission/copy")
@RequiredArgsConstructor
@Slf4j
public class PermissionCopyController {
    
    private final RolePermissionMapper rolePermissionMapper;
    private final RoleMapper roleMapper;
    
    @Operation(summary = "复制角色权限", description = "将源角色的权限复制到目标角色")
    @PostMapping("/role")
    @Transactional(rollbackFor = Exception.class)
    public Result<Map<String, Object>> copyRolePermissions(
            @RequestBody CopyRolePermissionRequest request) {
        try {
            Long sourceRoleId = request.getSourceRoleId();
            List<Long> targetRoleIds = request.getTargetRoleIds();
            
            // 验证源角色
            Role sourceRole = roleMapper.selectById(sourceRoleId);
            if (sourceRole == null) {
                return Result.error(404, "源角色不存在");
            }
            
            // 获取源角色的所有权限
            List<RolePermission> sourcePermissions = rolePermissionMapper.findByRoleId(sourceRoleId);
            
            Map<String, Object> result = new HashMap<>();
            int totalCopied = 0;
            List<Map<String, Object>> roleResults = new ArrayList<>();
            
            // 复制到每个目标角色
            for (Long targetRoleId : targetRoleIds) {
                Role targetRole = roleMapper.selectById(targetRoleId);
                if (targetRole == null) {
                    continue;
                }
                
                // 删除目标角色现有权限
                rolePermissionMapper.deleteByRoleId(targetRoleId);
                
                // 复制权限
                int copied = 0;
                for (RolePermission rp : sourcePermissions) {
                    // 使用 insertPermission 方法插入
                    rolePermissionMapper.insertPermission(
                        targetRoleId,
                        rp.getResourceId(),
                        rp.getGrantLevel() != null ? rp.getGrantLevel() : (short) 0,
                        rp.getGrantable() != null ? rp.getGrantable() : false,
                        rp.getGrantedBy(),
                        rp.getGrantedAt()
                    );
                    copied++;
                }
                
                totalCopied += copied;
                
                Map<String, Object> roleResult = new HashMap<>();
                roleResult.put("roleId", targetRoleId);
                roleResult.put("roleName", targetRole.getRoleName());
                roleResult.put("copied", copied);
                roleResults.add(roleResult);
            }
            
            result.put("totalCopied", totalCopied);
            result.put("roleResults", roleResults);
            result.put("sourceRole", sourceRole.getRoleName());
            
            log.info("复制角色权限：从 {} 复制到 {} 个角色，共 {} 个权限", 
                sourceRole.getRoleName(), targetRoleIds.size(), totalCopied);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("复制角色权限失败", e);
            return Result.error(500, "复制失败：" + e.getMessage());
        }
    }
    
    @Operation(summary = "批量授权", description = "批量给角色分配权限")
    @PostMapping("/batch")
    @Transactional(rollbackFor = Exception.class)
    public Result<Map<String, Object>> batchGrantPermissions(
            @RequestBody BatchGrantPermissionRequest request) {
        try {
            List<Long> targetIds = request.getTargetIds();
            String targetType = request.getTargetType(); // "user" or "role"
            List<Long> permissionIds = request.getPermissionIds();
            
            if (targetIds == null || targetIds.isEmpty()) {
                return Result.error(400, "目标 ID 列表不能为空");
            }
            
            if (permissionIds == null || permissionIds.isEmpty()) {
                return Result.error(400, "权限 ID 列表不能为空");
            }
            
            Map<String, Object> result = new HashMap<>();
            int totalGranted = 0;
            
            if ("role".equals(targetType)) {
                // 批量给角色授权
                for (Long roleId : targetIds) {
                    int granted = 0;
                    for (Long resourceId : permissionIds) {
                        // 检查是否已存在
                        boolean exists = rolePermissionMapper.hasPermission(roleId, resourceId);
                        
                        if (!exists) {
                            rolePermissionMapper.insertPermission(
                                roleId,
                                resourceId,
                                (short) 0,  // grant_level = 0
                                false,      // grantable = false
                                1L,         // granted_by = admin
                                LocalDateTime.now()
                            );
                            granted++;
                        }
                    }
                    totalGranted += granted;
                }
            } else if ("user".equals(targetType)) {
                // 批量给用户授权（需要调用用户权限服务）
                result.put("message", "用户批量授权需要调用用户权限服务");
            }
            
            result.put("totalGranted", totalGranted);
            
            log.info("批量授权：目标类型={}, 目标数量={}, 权限数量={}, 成功授予={}", 
                targetType, targetIds.size(), permissionIds.size(), totalGranted);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("批量授权失败", e);
            return Result.error(500, "授权失败：" + e.getMessage());
        }
    }
    
    @Operation(summary = "获取角色权限列表", description = "获取指定角色的所有资源 ID")
    @GetMapping("/role/{roleId}")
    public Result<List<Long>> getRolePermissions(@PathVariable Long roleId) {
        try {
            List<RolePermission> rolePermissions = rolePermissionMapper.findByRoleId(roleId);
            
            List<Long> resourceIds = rolePermissions.stream()
                .map(RolePermission::getResourceId)
                .distinct()
                .collect(Collectors.toList());
            
            return Result.success(resourceIds);
        } catch (Exception e) {
            log.error("获取角色权限失败", e);
            return Result.error(500, "获取失败：" + e.getMessage());
        }
    }
    
    // 请求 DTO 类
    @lombok.Data
    public static class CopyRolePermissionRequest {
        private Long sourceRoleId;
        private List<Long> targetRoleIds;
    }
    
    @lombok.Data
    public static class BatchGrantPermissionRequest {
        private List<Long> targetIds;
        private String targetType; // "user" or "role"
        private List<Long> permissionIds;
    }
}