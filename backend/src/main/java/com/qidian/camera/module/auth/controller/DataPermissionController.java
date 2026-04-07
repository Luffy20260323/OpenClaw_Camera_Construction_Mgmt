package com.qidian.camera.module.auth.controller;

import com.qidian.camera.module.auth.entity.UserContext;
import com.qidian.camera.module.auth.service.DataPermissionService;
import com.qidian.camera.module.auth.service.impl.PermissionServiceImpl;
import com.qidian.camera.common.response.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据权限 Controller
 * 提供数据权限查询和配置接口
 */
@RestController
@RequestMapping("/data-permission")
@RequiredArgsConstructor
@Slf4j
public class DataPermissionController {
    
    private final DataPermissionService dataPermissionService;
    private final PermissionServiceImpl permissionService;
    
    /**
     * 获取当前用户的数据权限信息
     */
    @GetMapping("/current")
    public Result<Map<String, Object>> getCurrentUserDataPermission() {
        try {
            UserContext user = permissionService.getCurrentUser();
            Long userId = user.getUserId();
            
            Map<String, Object> result = new HashMap<>();
            result.put("userId", userId);
            result.put("scopeType", dataPermissionService.getUserDataScopeType(userId));
            result.put("deptId", dataPermissionService.getUserDataDeptId(userId));
            result.put("deptIds", dataPermissionService.getUserDataDeptIds(userId));
            result.put("sqlCondition", dataPermissionService.getUserDataPermissionSql(userId, "default"));
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取当前用户数据权限失败", e);
            return Result.error("获取数据权限失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取指定用户的数据权限信息（管理员）
     */
    @GetMapping("/user/{userId}")
    public Result<Map<String, Object>> getUserDataPermission(@PathVariable Long userId) {
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("userId", userId);
            result.put("scopeType", dataPermissionService.getUserDataScopeType(userId));
            result.put("deptId", dataPermissionService.getUserDataDeptId(userId));
            result.put("deptIds", dataPermissionService.getUserDataDeptIds(userId));
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取用户数据权限失败：userId={}", userId, e);
            return Result.error("获取数据权限失败：" + e.getMessage());
        }
    }
    
    /**
     * 设置用户数据权限（管理员）
     */
    @PostMapping("/user/{userId}")
    public Result<Void> setUserDataPermission(
            @PathVariable Long userId,
            @RequestParam String scopeType,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) String deptIds) {
        try {
            UserContext operator = permissionService.getCurrentUser();
            dataPermissionService.setUserDataPermission(userId, scopeType, deptId, deptIds, operator.getUserId());
            return Result.success();
        } catch (Exception e) {
            log.error("设置用户数据权限失败：userId={}", userId, e);
            return Result.error("设置数据权限失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取角色数据权限信息
     */
    @GetMapping("/role/{roleId}")
    public Result<Map<String, Object>> getRoleDataPermission(@PathVariable Long roleId) {
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("roleId", roleId);
            result.put("scopeType", dataPermissionService.getRoleDataScopeType(roleId));
            result.put("deptId", dataPermissionService.getRoleDataDeptId(roleId));
            result.put("deptIds", dataPermissionService.getRoleDataDeptIds(roleId));
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取角色数据权限失败：roleId={}", roleId, e);
            return Result.error("获取数据权限失败：" + e.getMessage());
        }
    }
    
    /**
     * 设置角色数据权限（管理员）
     */
    @PostMapping("/role/{roleId}")
    public Result<Void> setRoleDataPermission(
            @PathVariable Long roleId,
            @RequestParam String scopeType,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) String deptIds) {
        try {
            dataPermissionService.setRoleDataPermission(roleId, scopeType, deptId, deptIds);
            return Result.success();
        } catch (Exception e) {
            log.error("设置角色数据权限失败：roleId={}", roleId, e);
            return Result.error("设置数据权限失败：" + e.getMessage());
        }
    }
}
