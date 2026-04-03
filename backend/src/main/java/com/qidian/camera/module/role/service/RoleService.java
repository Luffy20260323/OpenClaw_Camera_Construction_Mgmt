package com.qidian.camera.module.role.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qidian.camera.common.exception.BusinessException;
import com.qidian.camera.common.exception.ErrorCode;
import com.qidian.camera.module.auth.service.PermissionService;
import com.qidian.camera.module.role.dto.RoleDTO;
import com.qidian.camera.module.role.dto.CreateRoleRequest;
import com.qidian.camera.module.role.dto.UpdateRoleRequest;
import com.qidian.camera.module.role.entity.Role;
import com.qidian.camera.module.role.mapper.RoleMapper;
import com.qidian.camera.module.role.mapper.RoleResourceMapper;
import com.qidian.camera.module.company.mapper.CompanyTypeMapper;
import com.qidian.camera.module.auth.entity.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleMapper roleMapper;
    private final CompanyTypeMapper companyTypeMapper;
    private final PermissionService permissionService;
    private final RoleResourceMapper roleResourceMapper;

    /**
     * 分页查询角色列表
     */
    @Transactional(readOnly = true)
    public Page<RoleDTO> getRoles(Integer pageNum, Integer pageSize, Long companyTypeId, String keyword) {
        Page<Role> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        
        // 按公司类型筛选
        if (companyTypeId != null) {
            wrapper.eq(Role::getCompanyTypeId, companyTypeId);
        }
        
        // 关键词搜索（角色名称、角色编码）
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w
                .like(Role::getRoleName, keyword)
                .or()
                .like(Role::getRoleCode, keyword)
            );
        }
        
        // 排序：先按公司类型 ID，再按角色 ID
        wrapper.orderByAsc(Role::getCompanyTypeId, Role::getId);
        
        Page<Role> rolePage = roleMapper.selectPage(page, wrapper);
        
        // 转换为 DTO
        List<RoleDTO> dtoList = rolePage.getRecords().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        
        Page<RoleDTO> dtoPage = new Page<>(pageNum, pageSize, rolePage.getTotal());
        dtoPage.setRecords(dtoList);
        
        return dtoPage;
    }

    /**
     * 获取角色详情
     */
    @Transactional(readOnly = true)
    public RoleDTO getRoleById(Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "角色不存在");
        }
        return convertToDTO(role);
    }

    /**
     * 创建角色
     */
    @Transactional
    public RoleDTO createRole(CreateRoleRequest request) {
        // 检查角色编码是否重复
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getRoleCode, request.getRoleCode());
        Long count = roleMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "角色编码已存在");
        }
        
        // 检查公司类型是否存在
        if (companyTypeMapper.selectById(request.getCompanyTypeId()) == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "公司类型不存在");
        }
        
        Role role = new Role();
        role.setRoleName(request.getRoleName());
        role.setRoleCode(request.getRoleCode());
        role.setRoleDescription(request.getRoleDescription());
        role.setCompanyTypeId(request.getCompanyTypeId());
        role.setIsSystemProtected(false);
        role.setType(request.getType() != null ? request.getType() : "DEFAULT"); // 默认 DEFAULT 类型
        
        roleMapper.insert(role);
        
        // 自动分配缺省权限（根据角色类型）
        assignDefaultPermissions(role);
        
        log.info("创建角色成功：id={}, name={}, type={}", role.getId(), role.getRoleName(), role.getType());
        
        return getRoleById(role.getId());
    }

    /**
     * 更新角色
     */
    @Transactional
    public RoleDTO updateRole(Long id, UpdateRoleRequest request) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "角色不存在");
        }
        
        // 系统保护的角色不可修改
        if (role.getIsSystemProtected()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "系统保护的角色不可修改");
        }
        
        // 检查角色编码是否与其他角色重复
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getRoleCode, request.getRoleCode());
        wrapper.ne(Role::getId, id);
        Long count = roleMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "角色编码已存在");
        }
        
        role.setRoleName(request.getRoleName());
        role.setRoleCode(request.getRoleCode());
        role.setRoleDescription(request.getRoleDescription());
        role.setCompanyTypeId(request.getCompanyTypeId());
        
        roleMapper.updateById(role);
        
        log.info("更新角色成功：id={}, name={}", id, role.getRoleName());
        
        return getRoleById(id);
    }

    /**
     * 删除角色
     */
    @Transactional
    public void deleteRole(Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "角色不存在");
        }
        
        // 系统保护的角色不可删除
        if (role.getIsSystemProtected()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "系统保护的角色不可删除");
        }
        
        // 检查是否有关联的用户
        Long userCount = roleMapper.countUsersByRoleId(id);
        if (userCount != null && userCount > 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "角色下还有 " + userCount + " 个用户，无法删除");
        }
        
        // 清除角色权限缓存
        permissionService.evictRolePermissionCache(id);
        
        roleMapper.deleteById(id);
        
        log.info("删除角色成功：id={}, 已清除权限缓存", id);
    }

    /**
     * 转换为 DTO
     */
    private RoleDTO convertToDTO(Role role) {
        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setRoleName(role.getRoleName());
        dto.setRoleCode(role.getRoleCode());
        dto.setRoleDescription(role.getRoleDescription());
        dto.setCompanyTypeId(role.getCompanyTypeId());
        dto.setIsSystemProtected(role.getIsSystemProtected());
        dto.setType(role.getType());
        
        // 查询公司类型名称
        if (role.getCompanyTypeId() != null) {
            var companyType = companyTypeMapper.selectById(role.getCompanyTypeId());
            if (companyType != null) {
                dto.setCompanyTypeName(companyType.getTypeName());
            }
        }
        
        return dto;
    }
    
    /**
     * 根据角色类型自动分配缺省权限
     */
    private void assignDefaultPermissions(Role role) {
        String roleType = role.getType() != null ? role.getType() : "DEFAULT";
        
        // 从 role_type_default_permissions 表获取该类型的缺省权限
        List<Long> defaultResourceIds = roleResourceMapper.findDefaultResourceIdsByType(roleType);
        
        if (defaultResourceIds != null && !defaultResourceIds.isEmpty()) {
            int count = 0;
            for (Long resourceId : defaultResourceIds) {
                roleResourceMapper.insertDefaultPermission(role.getId(), resourceId, 1L);
                count++;
            }
            log.info("角色自动分配缺省权限：roleId={}, type={}, 权限数={}", role.getId(), roleType, count);
        }
    }
}
