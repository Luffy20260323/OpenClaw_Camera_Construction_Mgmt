package com.qidian.camera.module.auth.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qidian.camera.common.response.Result;
import com.qidian.camera.module.auth.dto.*;
import com.qidian.camera.module.auth.entity.*;
import com.qidian.camera.module.auth.mapper.*;
import com.qidian.camera.module.auth.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 角色权限管理 Controller
 */
@Slf4j
@Tag(name = "角色权限管理", description = "角色的权限分配和调整")
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RolePermissionController {
    
    private final RolePermissionMapper rolePermissionMapper;
    private final RolePermissionAdjustmentMapper rolePermissionAdjustmentMapper;
    private final ResourceService resourceService;
    private final PermissionService permissionService;
    private final PermissionAuditLogMapper auditLogMapper;
    
    /**
     * 获取角色的权限树（标记权限状态）
     */
    @Operation(summary = "获取角色权限树", description = "返回资源树，并标记角色当前拥有的权限状态")
    @GetMapping("/{roleId}/permissions/tree")
    public Result<List<PermissionTreeNode>> getRolePermissionTree(@PathVariable Long roleId) {
        // 获取角色的完整权限
        Set<Long> permissionIds = permissionService.calculateRolePermissions(roleId);
        
        // 获取角色的基础权限
        Set<Long> basicIds = rolePermissionMapper.findBasicByRoleId(roleId).stream()
            .map(RolePermission::getResourceId)
            .collect(Collectors.toSet());
        
        // 获取权限调整
        Map<Long, String> adjustments = rolePermissionAdjustmentMapper.findByRoleId(roleId).stream()
            .collect(Collectors.toMap(
                RolePermissionAdjustment::getResourceId,
                RolePermissionAdjustment::getAction
            ));
        
        // 构建权限树
        List<ResourceService.ResourceTreeNode> resourceTree = resourceService.getResourceTree();
        List<PermissionTreeNode> permissionTree = buildPermissionTree(resourceTree, permissionIds, basicIds, adjustments);
        
        return Result.success(permissionTree);
    }
    
    /**
     * 构建权限树（添加状态标记）
     */
    private List<PermissionTreeNode> buildPermissionTree(
            List<ResourceService.ResourceTreeNode> resourceTree,
            Set<Long> permissionIds,
            Set<Long> basicIds,
            Map<Long, String> adjustments) {
        return resourceTree.stream()
            .map(node -> {
                PermissionTreeNode permNode = new PermissionTreeNode();
                permNode.setId(node.getId());
                permNode.setName(node.getName());
                permNode.setCode(node.getCode());
                permNode.setType(node.getType());
                permNode.setPermissionKey(node.getPermissionKey());
                permNode.setIcon(node.getIcon());
                permNode.setPath(node.getPath());
                permNode.setSortOrder(node.getSortOrder());
                
                // 标记权限状态
                if (basicIds.contains(node.getId())) {
                    permNode.setStatus("basic");  // 基本权限
                } else if (adjustments.containsKey(node.getId())) {
                    permNode.setStatus(adjustments.get(node.getId()).toLowerCase());  // added/removed
                } else if (permissionIds.contains(node.getId())) {
                    permNode.setStatus("default");  // 缺省权限
                } else {
                    permNode.setStatus("none");  // 无权限
                }
                
                permNode.setChildren(buildPermissionTree(node.getChildren(), permissionIds, basicIds, adjustments));
                return permNode;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 调整角色权限
     */
    @Operation(summary = "调整角色权限", description = "增加或移除角色权限")
    @PostMapping("/{roleId}/permissions/adjust")
    @Transactional
    public Result<Void> adjustRolePermission(
            @PathVariable Long roleId,
            @RequestBody RolePermissionAdjustDTO dto) {
        
        Long resourceId = dto.getResourceId();
        String action = dto.getAction();
        
        // 检查是否是基本权限
        Resource resource = resourceService.getById(resourceId);
        if (resource != null && resource.getIsBasic() == 1) {
            return Result.error("基本权限不可调整");
        }
        
        // 检查是否已存在调整记录
        RolePermissionAdjustment existing = rolePermissionAdjustmentMapper.selectOne(
            new LambdaQueryWrapper<RolePermissionAdjustment>()
                .eq(RolePermissionAdjustment::getRoleId, roleId)
                .eq(RolePermissionAdjustment::getResourceId, resourceId)
        );
        
        if (existing != null) {
            // 更新现有调整记录
            if (existing.getAction().equals(action)) {
                // 相同操作，删除调整记录（恢复原状态）
                rolePermissionAdjustmentMapper.deleteById(existing.getId());
            } else {
                // 相反操作，更新调整记录
                existing.setAction(action);
                rolePermissionAdjustmentMapper.updateById(existing);
            }
        } else {
            // 创建新的调整记录
            RolePermissionAdjustment adjustment = new RolePermissionAdjustment();
            adjustment.setRoleId(roleId);
            adjustment.setResourceId(resourceId);
            adjustment.setAction(action);
            // TODO: 从上下文获取当前用户 ID
            adjustment.setCreatedBy(0L);
            rolePermissionAdjustmentMapper.insert(adjustment);
        }
        
        // 清除权限缓存
        permissionService.evictRolePermissionCache(roleId);
        
        // 记录审计日志
        recordAuditLog(roleId, resourceId, action, "ROLE");
        
        log.info("调整角色权限: roleId={}, resourceId={}, action={}", roleId, resourceId, action);
        return Result.success();
    }
    
    /**
     * 批量调整角色权限
     */
    @Operation(summary = "批量调整角色权限", description = "批量增加或移除角色权限")
    @PostMapping("/{roleId}/permissions/adjust-batch")
    @Transactional
    public Result<Void> adjustRolePermissionBatch(
            @PathVariable Long roleId,
            @RequestBody List<RolePermissionAdjustDTO> adjustments) {
        
        for (RolePermissionAdjustDTO dto : adjustments) {
            adjustRolePermission(roleId, dto);
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
    
    /**
     * 权限树节点 DTO（包含状态标记）
     */
    @lombok.Data
    public static class PermissionTreeNode {
        private Long id;
        private String name;
        private String code;
        private String type;
        private String permissionKey;
        private String icon;
        private String path;
        private Integer sortOrder;
        private String status;  // basic, default, added, removed, none
        private List<PermissionTreeNode> children;
    }
}