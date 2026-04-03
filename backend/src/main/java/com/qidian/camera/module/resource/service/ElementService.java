package com.qidian.camera.module.resource.service;

import com.qidian.camera.module.auth.entity.Resource;
import com.qidian.camera.module.auth.mapper.ResourceMapper;
import com.qidian.camera.module.resource.dto.ElementDTO;
import com.qidian.camera.module.resource.dto.ElementTreeDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ELEMENT 资源服务
 * 提供 ELEMENT 层级的 CRUD 操作
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ElementService {
    
    private final ResourceMapper resourceMapper;
    
    /**
     * 获取 ELEMENT 列表（支持按 PAGE 过滤）
     */
    public List<ElementDTO> getElementList(Long pageId) {
        List<Resource> elements;
        
        if (pageId != null) {
            // 按页面过滤
            elements = resourceMapper.findByParentIdAndType(pageId, "ELEMENT");
        } else {
            // 获取所有 ELEMENT
            elements = resourceMapper.findByType("ELEMENT");
        }
        
        return elements.stream()
            .map(this::convertToElementDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * 获取 ELEMENT 树（ELEMENT → 子 PAGE）
     */
    public List<ElementTreeDTO> getElementTree(Long pageId) {
        List<Resource> elements;
        
        if (pageId != null) {
            elements = resourceMapper.findByParentIdAndType(pageId, "ELEMENT");
        } else {
            elements = resourceMapper.findByType("ELEMENT");
        }
        
        List<ElementTreeDTO> result = new ArrayList<>();
        
        for (Resource element : elements) {
            ElementTreeDTO dto = convertToElementTreeDTO(element);
            
            // 查找关联的子页面（parent_id 指向该 ELEMENT 的 PAGE）
            List<Resource> childPages = resourceMapper.findByParentIdAndType(element.getId(), "PAGE");
            if (!childPages.isEmpty()) {
                Resource childPage = childPages.get(0);
                ElementTreeDTO.ElementPageDTO pageDTO = ElementTreeDTO.ElementPageDTO.builder()
                    .id(childPage.getId())
                    .name(childPage.getName())
                    .code(childPage.getCode())
                    .path(childPage.getPath())
                    .component(childPage.getComponent())
                    .permissionKey(childPage.getPermissionKey())
                    .build();
                dto.setChildPage(pageDTO);
                dto.setHasChildren(true);
            }
            
            result.add(dto);
        }
        
        return result.stream()
            .sorted(Comparator.comparing(ElementTreeDTO::getSortOrder))
            .collect(Collectors.toList());
    }
    
    /**
     * 获取某页面的所有 ELEMENT
     */
    public List<ElementDTO> getElementsByPageId(Long pageId) {
        return getElementList(pageId);
    }
    
    /**
     * 根据 ID 获取 ELEMENT
     */
    public ElementDTO getElementById(Long id) {
        Resource element = resourceMapper.selectById(id);
        if (element == null || !"ELEMENT".equals(element.getType())) {
            return null;
        }
        return convertToElementDTO(element);
    }
    
    /**
     * 创建 ELEMENT
     */
    @Transactional
    public ElementDTO createElement(ElementDTO dto) {
        // 验证父页面是否存在
        Resource parentPage = resourceMapper.selectById(dto.getPageId());
        if (parentPage == null || !"PAGE".equals(parentPage.getType())) {
            throw new RuntimeException("父页面不存在或类型不正确: " + dto.getPageId());
        }
        
        // 生成权限码：module:resource:action:button
        String permissionKey = generatePermissionKey(dto);
        
        // 检查权限码是否已存在
        Resource existing = resourceMapper.findByPermissionKey(permissionKey);
        if (existing != null) {
            throw new RuntimeException("权限标识已存在: " + permissionKey);
        }
        
        // 生成元素编码
        String code = generateElementCode(dto);
        
        // 创建资源
        Resource element = new Resource();
        element.setName(dto.getName());
        element.setCode(code);
        element.setType("ELEMENT");
        element.setParentId(dto.getPageId());
        element.setPermissionKey(permissionKey);
        element.setModuleCode(parentPage.getModuleCode());
        element.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        element.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        element.setDescription(dto.getDescription());
        element.setCreatedAt(LocalDateTime.now());
        element.setUpdatedAt(LocalDateTime.now());
        
        resourceMapper.insert(element);
        
        // 如果有关联的子页面，更新子页面的 parent_id
        if (dto.getChildPageId() != null) {
            Resource childPage = resourceMapper.selectById(dto.getChildPageId());
            if (childPage != null) {
                childPage.setParentId(element.getId());
                resourceMapper.updateById(childPage);
            }
        }
        
        log.info("创建 ELEMENT: id={}, name={}, permissionKey={}", element.getId(), element.getName(), element.getPermissionKey());
        
        return convertToElementDTO(element);
    }
    
    /**
     * 更新 ELEMENT
     */
    @Transactional
    public ElementDTO updateElement(Long id, ElementDTO dto) {
        Resource element = resourceMapper.selectById(id);
        if (element == null || !"ELEMENT".equals(element.getType())) {
            throw new RuntimeException("ELEMENT 不存在: " + id);
        }
        
        // 只更新允许的字段
        if (dto.getName() != null) {
            element.setName(dto.getName());
        }
        if (dto.getSortOrder() != null) {
            element.setSortOrder(dto.getSortOrder());
        }
        if (dto.getStatus() != null) {
            element.setStatus(dto.getStatus());
        }
        if (dto.getDescription() != null) {
            element.setDescription(dto.getDescription());
        }
        
        element.setUpdatedAt(LocalDateTime.now());
        resourceMapper.updateById(element);
        
        // 如果有关联的子页面变更
        if (dto.getChildPageId() != null) {
            Resource childPage = resourceMapper.selectById(dto.getChildPageId());
            if (childPage != null) {
                childPage.setParentId(id);
                resourceMapper.updateById(childPage);
            }
        }
        
        log.info("更新 ELEMENT: id={}, name={}", id, element.getName());
        
        return convertToElementDTO(element);
    }
    
    /**
     * 删除 ELEMENT
     */
    @Transactional
    public void deleteElement(Long id) {
        Resource element = resourceMapper.selectById(id);
        if (element == null) {
            throw new RuntimeException("ELEMENT 不存在: " + id);
        }
        if (!"ELEMENT".equals(element.getType())) {
            throw new RuntimeException("资源类型不是 ELEMENT: " + id);
        }
        
        // 检查是否有子页面
        List<Resource> children = resourceMapper.findByParentId(id);
        if (!children.isEmpty()) {
            // 将子页面的 parent_id 设置为该 ELEMENT 的父页面
            Resource parentPage = resourceMapper.selectById(element.getParentId());
            if (parentPage != null) {
                for (Resource child : children) {
                    child.setParentId(parentPage.getId());
                    resourceMapper.updateById(child);
                }
            }
        }
        
        resourceMapper.deleteById(id);
        log.info("删除 ELEMENT: id={}, name={}", id, element.getName());
    }
    
    /**
     * 生成权限码
     * 格式：module:resource:action:button
     */
    private String generatePermissionKey(ElementDTO dto) {
        String module = dto.getModuleCode() != null ? dto.getModuleCode() : "system";
        String resource = extractResourceFromName(dto.getName());
        String action = extractActionFromName(dto.getName());
        return String.format("%s:%s:%s:button", module, resource, action);
    }
    
    /**
     * 生成元素编码
     */
    private String generateElementCode(ElementDTO dto) {
        String action = extractActionFromName(dto.getName());
        return "elem_" + dto.getModuleCode() + "_" + action + "_" + System.currentTimeMillis();
    }
    
    /**
     * 从名称中提取资源类型（如"用户"、"角色"等）
     */
    private String extractResourceFromName(String name) {
        if (name == null) return "unknown";
        
        // 简单映射
        if (name.contains("用户")) return "user";
        if (name.contains("角色")) return "role";
        if (name.contains("资源")) return "resource";
        if (name.contains("权限")) return "permission";
        if (name.contains("文档")) return "document";
        
        return "unknown";
    }
    
    /**
     * 从名称中提取操作类型（如 view, create, edit, delete 等）
     */
    private String extractActionFromName(String name) {
        if (name == null) return "unknown";
        
        if (name.contains("查看") || name.contains("详情")) return "view";
        if (name.contains("新建") || name.contains("创建")) return "create";
        if (name.contains("编辑") || name.contains("修改")) return "edit";
        if (name.contains("删除")) return "delete";
        if (name.contains("导出")) return "export";
        if (name.contains("导入")) return "import";
        if (name.contains("上传")) return "upload";
        if (name.contains("下载")) return "download";
        if (name.contains("分配")) return "assign";
        
        return "unknown";
    }
    
    /**
     * 转换为 ElementDTO
     */
    private ElementDTO convertToElementDTO(Resource r) {
        ElementDTO dto = ElementDTO.builder()
            .id(r.getId())
            .name(r.getName())
            .code(r.getCode())
            .pageId(r.getParentId())
            .permissionKey(r.getPermissionKey())
            .sortOrder(r.getSortOrder())
            .status(r.getStatus())
            .moduleCode(r.getModuleCode())
            .description(r.getDescription())
            .createdAt(r.getCreatedAt())
            .updatedAt(r.getUpdatedAt())
            .build();
        
        // 获取父页面名称
        if (r.getParentId() != null) {
            Resource parent = resourceMapper.selectById(r.getParentId());
            if (parent != null) {
                dto.setPageName(parent.getName());
            }
        }
        
        // 获取关联的子页面
        List<Resource> childPages = resourceMapper.findByParentIdAndType(r.getId(), "PAGE");
        if (!childPages.isEmpty()) {
            dto.setChildPageId(childPages.get(0).getId());
            dto.setChildPageName(childPages.get(0).getName());
        }
        
        return dto;
    }
    
    /**
     * 转换为 ElementTreeDTO
     */
    private ElementTreeDTO convertToElementTreeDTO(Resource r) {
        return ElementTreeDTO.builder()
            .id(r.getId())
            .name(r.getName())
            .code(r.getCode())
            .permissionKey(r.getPermissionKey())
            .sortOrder(r.getSortOrder())
            .status(r.getStatus())
            .moduleCode(r.getModuleCode())
            .description(r.getDescription())
            .hasChildren(false)
            .build();
    }
}
