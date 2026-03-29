package com.qidian.camera.module.role.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qidian.camera.common.response.Result;
import com.qidian.camera.module.role.dto.RoleDTO;
import com.qidian.camera.module.role.dto.CreateRoleRequest;
import com.qidian.camera.module.role.dto.UpdateRoleRequest;
import com.qidian.camera.module.role.service.RoleService;
import com.qidian.camera.module.auth.annotation.ApiPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 角色控制器
 */
@Tag(name = "角色管理", description = "角色信息管理（仅系统管理员）")
@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;
    private final com.qidian.camera.module.user.service.UserService userService;

    @Operation(summary = "分页查询角色列表", description = "支持按公司类型、关键词筛选")
    @ApiPermission("role:list")
    @GetMapping
    public Result<Page<RoleDTO>> getRoles(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long companyTypeId,
            @RequestParam(required = false) String keyword) {
        Page<RoleDTO> page = roleService.getRoles(pageNum, pageSize, companyTypeId, keyword);
        return Result.success(page);
    }

    @Operation(summary = "获取角色详情", description = "根据 ID 获取角色详细信息")
    @ApiPermission("role:view")
    @GetMapping("/{id}")
    public Result<RoleDTO> getRole(@PathVariable Long id) {
        RoleDTO role = roleService.getRoleById(id);
        return Result.success(role);
    }

    @Operation(summary = "创建角色", description = "创建新角色（仅系统管理员）")
    @ApiPermission("role:create")
    @PostMapping
    public Result<RoleDTO> createRole(
            @RequestAttribute("userId") Long operatorId,
            @Valid @RequestBody CreateRoleRequest request) {
        userService.validateRoleManagementPermission(operatorId);
        RoleDTO role = roleService.createRole(request);
        return Result.success(role);
    }

    @Operation(summary = "更新角色", description = "更新角色信息（仅系统管理员，系统保护的角色不可修改）")
    @ApiPermission("role:edit")
    @PutMapping("/{id}")
    public Result<RoleDTO> updateRole(
            @PathVariable Long id,
            @RequestAttribute("userId") Long operatorId,
            @Valid @RequestBody UpdateRoleRequest request) {
        userService.validateRoleManagementPermission(operatorId);
        RoleDTO role = roleService.updateRole(id, request);
        return Result.success(role);
    }

    @Operation(summary = "删除角色", description = "删除角色（仅系统管理员，系统保护的角色不可删除）")
    @ApiPermission("role:delete")
    @DeleteMapping("/{id}")
    public Result<Void> deleteRole(
            @PathVariable Long id,
            @RequestAttribute("userId") Long operatorId) {
        userService.validateRoleManagementPermission(operatorId);
        roleService.deleteRole(id);
        return Result.success();
    }
}
