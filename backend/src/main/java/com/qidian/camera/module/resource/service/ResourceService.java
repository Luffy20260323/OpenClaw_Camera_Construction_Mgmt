package com.qidian.camera.module.resource.service;

import com.qidian.camera.module.auth.mapper.ResourceMapper;
import com.qidian.camera.module.auth.entity.Resource;
import com.qidian.camera.module.resource.dto.ResourceStatsDTO;
import com.qidian.camera.module.resource.dto.ResourceTreeDTO;
import com.qidian.camera.module.resource.dto.ModuleStatsDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.Collections;;
import java.util.stream.Collectors;

/**
 * 统一资源服务 - 基于新的 resource 表（V24迁移后）
 */
@Service("unifiedResourceService")
@RequiredArgsConstructor
@Slf4j
public class ResourceService {
    
    private final ResourceMapper resourceMapper;
    private final JdbcTemplate jdbcTemplate;
    
    /**
     * 获取所有资源列表
     */
    public List<Resource> getAllResources() {
        return resourceMapper.findAllEnabled();
    }
    
    /**
     * 获取资源树（完整层级结构）
     */
    public List<ResourceTreeDTO> getResourceTree() {
        List<Resource> resources = resourceMapper.findAllEnabled();
        
        // 构建树形结构
        Map<Long, ResourceTreeDTO> dtoMap = new HashMap<>();
        List<ResourceTreeDTO> rootNodes = new ArrayList<>();
        
        // 先创建所有节点
        for (Resource r : resources) {
            ResourceTreeDTO dto = convertToDTO(r);
            dto.setChildren(new ArrayList<>());
            dto.setHasChildren(false);
            dtoMap.put(r.getId(), dto);
        }
        
        // 建立父子关系
        for (Resource r : resources) {
            ResourceTreeDTO dto = dtoMap.get(r.getId());
            if (r.getParentId() == null || r.getParentId() == 0) {
                dto.setLevel(0);
                dto.setPathName(r.getName());
                rootNodes.add(dto);
            } else {
                ResourceTreeDTO parent = dtoMap.get(r.getParentId());
                if (parent != null) {
                    parent.getChildren().add(dto);
                    parent.setHasChildren(true);
                    dto.setLevel(parent.getLevel() + 1);
                    dto.setPathName(parent.getPathName() + " > " + dto.getName());
                }
            }
        }
        
        return rootNodes.stream()
            .sorted(Comparator.comparing(ResourceTreeDTO::getSortOrder))
            .collect(Collectors.toList());
    }
    
    /**
     * 按类型获取资源列表
     */
    public List<Resource> getResourcesByType(String type) {
        return resourceMapper.findByType(type);
    }
    
    /**
     * 按模块获取资源树
     */
    public List<ResourceTreeDTO> getResourceTreeByModule(String moduleCode) {
        List<Resource> resources = resourceMapper.findByModule(moduleCode);
        
        Map<Long, ResourceTreeDTO> dtoMap = new HashMap<>();
        List<ResourceTreeDTO> rootNodes = new ArrayList<>();
        
        for (Resource r : resources) {
            ResourceTreeDTO dto = convertToDTO(r);
            dto.setChildren(new ArrayList<>());
            dtoMap.put(r.getId(), dto);
        }
        
        for (Resource r : resources) {
            ResourceTreeDTO dto = dtoMap.get(r.getId());
            if (r.getParentId() == null || r.getParentId() == 0) {
                rootNodes.add(dto);
            } else {
                ResourceTreeDTO parent = dtoMap.get(r.getParentId());
                if (parent != null) {
                    parent.getChildren().add(dto);
                }
            }
        }
        
        return rootNodes;
    }
    
    /**
     * 获取资源统计
     */
    public List<ResourceStatsDTO> getResourceStats() {
        String sql = "SELECT type, total_count, active_count, basic_count, root_count FROM v_resource_stats";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            ResourceStatsDTO dto = new ResourceStatsDTO();
            dto.setType(rs.getString("type"));
            dto.setTotalCount(rs.getInt("total_count"));
            dto.setActiveCount(rs.getInt("active_count"));
            dto.setBasicCount(rs.getInt("basic_count"));
            dto.setRootCount(rs.getInt("root_count"));
            return dto;
        });
    }
    
    /**
     * 获取模块统计
     */
    public List<ModuleStatsDTO> getModuleStats() {
        String sql = "SELECT module_code, total_count, module_count, menu_count, page_count, " +
                     "element_count, api_count, permission_count FROM v_module_resource_stats";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            ModuleStatsDTO dto = new ModuleStatsDTO();
            dto.setModuleCode(rs.getString("module_code"));
            dto.setTotalCount(rs.getInt("total_count"));
            dto.setModuleCount(rs.getInt("module_count"));
            dto.setMenuCount(rs.getInt("menu_count"));
            dto.setPageCount(rs.getInt("page_count"));
            dto.setElementCount(rs.getInt("element_count"));
            dto.setApiCount(rs.getInt("api_count"));
            dto.setPermissionCount(rs.getInt("permission_count"));
            return dto;
        });
    }
    
    /**
     * 获取基本权限列表
     */
    public List<Resource> getBasicPermissions() {
        return resourceMapper.findBasicPermissions();
    }
    
    /**
     * 根据权限标识查找资源
     */
    public Resource findByPermissionKey(String permissionKey) {
        return resourceMapper.findByPermissionKey(permissionKey);
    }
    
    /**
     * 根据URI和Method查找API资源
     */
    public Resource findByUriAndMethod(String uri, String method) {
        return resourceMapper.findByUriAndMethod(uri, method);
    }
    
    /**
     * 获取子资源列表
     */
    public List<Resource> getChildren(Long parentId) {
        return resourceMapper.findByParentId(parentId);
    }
    
    /**
     * 根据 ID 获取资源
     */
    public Resource getById(Long id) {
        return resourceMapper.selectById(id);
    }
    
    /**
     * 更新资源（仅允许更新特定字段）
     * 允许更新：name, sortOrder, icon, path, description, parentId（限模块）
     * 禁止更新：type, permissionKey, code（系统维护）
     */
    public Resource updateResource(Long id, Resource updates) {
        Resource existing = resourceMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("资源不存在: " + id);
        }
        
        // 只更新允许的字段
        if (updates.getName() != null) {
            existing.setName(updates.getName());
        }
        if (updates.getSortOrder() != null) {
            existing.setSortOrder(updates.getSortOrder());
        }
        if (updates.getIcon() != null) {
            existing.setIcon(updates.getIcon());
        }
        if (updates.getPath() != null) {
            existing.setPath(updates.getPath());
        }
        if (updates.getDescription() != null) {
            existing.setDescription(updates.getDescription());
        }
        
        // 父资源修改规则：
        // 1. MODULE 类型可以修改父资源（可以是 MODULE 或 null）
        // 2. 其他类型只有当当前父资源类型是 MODULE 时，才允许修改父资源
        if (updates.getParentId() != null) {
            // MODULE 类型可以修改父资源
            if ("MODULE".equals(existing.getType())) {
                Resource newParent = resourceMapper.selectById(updates.getParentId());
                if (newParent != null && !"MODULE".equals(newParent.getType())) {
                    throw new RuntimeException("MODULE 的父资源必须是 MODULE 类型或 null");
                }
                existing.setParentId(updates.getParentId());
                log.info("修改模块父资源: id={}, newParentId={}", id, updates.getParentId());
            } else {
                // 非 MODULE 类型：只有当前父资源是 MODULE 时才允许修改
                Resource currentParent = existing.getParentId() != null 
                    ? resourceMapper.selectById(existing.getParentId()) : null;
                
                if (currentParent == null || !"MODULE".equals(currentParent.getType())) {
                    throw new RuntimeException(String.format(
                        "%s 类型的父资源由系统维护，不允许修改（当前父资源类型: %s）",
                        existing.getType(),
                        currentParent != null ? currentParent.getType() : "null"
                    ));
                }
                
                // 验证新父资源必须是 MODULE 类型
                Resource newParent = resourceMapper.selectById(updates.getParentId());
                if (newParent == null) {
                    throw new RuntimeException("父资源不存在: " + updates.getParentId());
                }
                if (!"MODULE".equals(newParent.getType())) {
                    throw new RuntimeException("父资源必须是 MODULE 类型，当前类型: " + newParent.getType());
                }
                existing.setParentId(updates.getParentId());
                log.info("修改资源父模块: id={}, type={}, newParentId={}", 
                    id, existing.getType(), updates.getParentId());
            }
        }
        
        existing.setUpdatedAt(java.time.LocalDateTime.now());
        resourceMapper.updateById(existing);
        
        log.info("更新资源: id={}, name={}, sortOrder={}", id, existing.getName(), existing.getSortOrder());
        return existing;
    }
    
    /**
     * 创建模块（仅支持创建 MODULE 类型）
     */
    public Resource createModule(Resource module) {
        if (!"MODULE".equals(module.getType())) {
            throw new RuntimeException("只允许创建 MODULE 类型资源");
        }
        
        // 检查 code 是否已存在
        Resource existing = resourceMapper.findByPermissionKey(module.getPermissionKey());
        if (existing != null) {
            throw new RuntimeException("权限标识已存在: " + module.getPermissionKey());
        }
        
        module.setStatus(1);
        module.setIsBasic(0);
        module.setCreatedAt(java.time.LocalDateTime.now());
        module.setUpdatedAt(java.time.LocalDateTime.now());
        
        resourceMapper.insert(module);
        
        log.info("创建模块: id={}, name={}, parentId={}", module.getId(), module.getName(), module.getParentId());
        return module;
    }
    
    /**
     * 删除资源（仅允许删除 MODULE 类型）
     * 注意：删除模块前需要确保没有子资源
     */
    public void deleteResource(Long id) {
        Resource existing = resourceMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("资源不存在: " + id);
        }
        
        // 检查是否有子资源
        List<Resource> children = resourceMapper.findByParentId(id);
        if (!children.isEmpty()) {
            throw new RuntimeException("该资源有子资源，无法删除。请先删除或移动子资源。");
        }
        
        resourceMapper.deleteById(id);
        log.info("删除资源: id={}, name={}, type={}", id, existing.getName(), existing.getType());
    }
    
    /**
     * 转换为 DTO
     */
    private ResourceTreeDTO convertToDTO(Resource r) {
        return ResourceTreeDTO.builder()
            .id(r.getId())
            .name(r.getName())
            .code(r.getCode())
            .type(r.getType())
            .parentId(r.getParentId())
            .permissionKey(r.getPermissionKey())
            .icon(r.getIcon())
            .path(r.getPath())
            .component(r.getComponent())
            .sortOrder(r.getSortOrder() != null ? r.getSortOrder() : 0)
            .status(r.getStatus() != null ? r.getStatus() : 1)
            .isBasic(r.getIsBasic() != null ? r.getIsBasic() : 0)
            .moduleCode(r.getModuleCode())
            .description(r.getDescription())
            .uriPattern(r.getUriPattern())
            .method(r.getMethod())
            .level(0)
            .hasChildren(false)
            .build();
    }

    /**
     * 获取与指定资源关联的 API 列表
     */
    public List<Resource> getRelatedApis(Long resourceId) {
        Resource resource = resourceMapper.selectById(resourceId);
        if (resource == null) {
            return Collections.emptyList();
        }
        
        if ("API".equals(resource.getType())) {
            return Collections.singletonList(resource);
        }
        
        String moduleCode = resource.getModuleCode();
        if (moduleCode == null || moduleCode.isEmpty()) {
            return Collections.emptyList();
        }
        
        return resourceMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Resource>()
                .eq("type", "API")
                .eq("module_code", moduleCode)
                .orderByAsc("permission_key")
        );
    }
}
