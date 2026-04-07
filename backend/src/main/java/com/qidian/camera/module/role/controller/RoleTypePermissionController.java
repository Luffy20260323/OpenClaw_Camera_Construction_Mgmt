package com.qidian.camera.module.role.controller;

import com.qidian.camera.module.role.dto.RoleTypePermissionDTO;
import com.qidian.camera.module.role.dto.RoleTypePermissionRequest;
import com.qidian.camera.module.role.service.RoleTypePermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色类型缺省权限控制器
 */
@RestController
@RequestMapping("/role-type-permissions")
@RequiredArgsConstructor
@Tag(name = "角色类型缺省权限管理")
public class RoleTypePermissionController {

    private final RoleTypePermissionService service;

    /**
     * 获取某角色类型的缺省权限列表
     */
    @GetMapping("/{roleType}")
    @Operation(summary = "获取角色类型缺省权限列表")
    public List<RoleTypePermissionDTO> getByRoleType(@PathVariable String roleType) {
        return service.getByRoleType(roleType);
    }

    /**
     * 批量添加缺省权限
     */
    @PostMapping
    @Operation(summary = "批量添加缺省权限")
    public void batchAdd(@RequestBody RoleTypePermissionRequest request) {
        service.batchAdd(request.getRoleType(), request.getResourceIds());
    }

    /**
     * 删除缺省权限
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除缺省权限")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    /**
     * 批量删除缺省权限
     */
    @PostMapping("/batch-delete")
    @Operation(summary = "批量删除缺省权限")
    public void batchDelete(@RequestBody List<Long> ids) {
        service.batchDelete(ids);
    }
}
