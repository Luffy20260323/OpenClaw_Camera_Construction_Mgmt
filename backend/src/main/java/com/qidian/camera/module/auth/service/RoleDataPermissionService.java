package com.qidian.camera.module.auth.service;

import com.qidian.camera.module.auth.entity.RoleDataPermission;
import com.qidian.camera.module.auth.mapper.RoleDataPermissionMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 角色数据权限服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RoleDataPermissionService extends ServiceImpl<RoleDataPermissionMapper, RoleDataPermission> {
    
    /**
     * 获取角色数据权限
     */
    public RoleDataPermission getByRoleId(Long roleId) {
        return lambdaQuery()
            .eq(RoleDataPermission::getRoleId, roleId)
            .one();
    }
}
