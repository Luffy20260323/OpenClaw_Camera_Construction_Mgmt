package com.qidian.camera.module.role.service;

import com.qidian.camera.module.role.dto.RoleTypePermissionDTO;
import com.qidian.camera.module.role.mapper.RoleTypePermissionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色类型缺省权限服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleTypePermissionService {

    private final RoleTypePermissionMapper mapper;

    /**
     * 获取某角色类型的缺省权限列表
     */
    @Transactional(readOnly = true)
    public List<RoleTypePermissionDTO> getByRoleType(String roleType) {
        return mapper.selectByRoleType(roleType);
    }

    /**
     * 批量添加缺省权限
     */
    @Transactional
    public void batchAdd(String roleType, List<Long> resourceIds) {
        if (resourceIds == null || resourceIds.isEmpty()) {
            return;
        }

        int count = 0;
        for (Long resourceId : resourceIds) {
            mapper.insert(roleType, resourceId);
            count++;
        }

        log.info("批量添加缺省权限：roleType={}, 数量={}", roleType, count);
    }

    /**
     * 删除缺省权限
     */
    @Transactional
    public void delete(Long id) {
        mapper.deleteById(id);
        log.info("删除缺省权限：id={}", id);
    }

    /**
     * 批量删除缺省权限
     */
    @Transactional
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        for (Long id : ids) {
            mapper.deleteById(id);
        }

        log.info("批量删除缺省权限：数量={}", ids.size());
    }
}
