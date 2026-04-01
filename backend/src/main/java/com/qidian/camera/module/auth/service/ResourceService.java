package com.qidian.camera.module.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qidian.camera.module.auth.entity.Resource;
import com.qidian.camera.module.auth.mapper.ResourceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 资源管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceService {
    
    private final ResourceMapper resourceMapper;
    
    /**
     * 获取所有启用的资源
     */
    public List<Resource> findAllEnabled() {
        return resourceMapper.findAllEnabled();
    }
    
    /**
     * 获取资源树（树形结构）
     */
    public List<ResourceTreeNode> getResourceTree() {
        List<Resource> allResources = findAllEnabled();
        return buildTree(allResources, null);
    }
    
    /**
     * 构建资源树
     */
    private List<ResourceTreeNode> buildTree(List<Resource> resources, Long parentId) {
        return resources.stream()
            .filter(r -> Objects.equals(r.getParentId(), parentId))
            .map(r -> {
                ResourceTreeNode node = new ResourceTreeNode();
                node.setId(r.getId());
                node.setName(r.getName());
                node.setCode(r.getCode());
                node.setType(r.getType());
                node.setPermissionKey(r.getPermissionKey());
                node.setIcon(r.getIcon());
                node.setPath(r.getPath());
                node.setSortOrder(r.getSortOrder());
                node.setIsBasic(r.getIsBasic());
                node.setChildren(buildTree(resources, r.getId()));
                return node;
            })
            .sorted(Comparator.comparing(ResourceTreeNode::getSortOrder))
            .collect(Collectors.toList());
    }
    
    /**
     * 根据 ID 获取资源
     */
    public Resource getById(Long id) {
        return resourceMapper.selectById(id);
    }
    
    /**
     * 根据 code 获取资源
     */
    public Resource getByCode(String code) {
        return resourceMapper.selectOne(
            new LambdaQueryWrapper<Resource>().eq(Resource::getCode, code)
        );
    }
    
    /**
     * 创建资源
     */
    @Transactional
    public Resource create(Resource resource) {
        resource.setStatus(1);
        resourceMapper.insert(resource);
        log.info("创建资源: id={}, code={}, name={}", resource.getId(), resource.getCode(), resource.getName());
        return resource;
    }
    
    /**
     * 更新资源
     */
    @Transactional
    public Resource update(Resource resource) {
        resourceMapper.updateById(resource);
        log.info("更新资源: id={}, code={}", resource.getId(), resource.getCode());
        return resource;
    }
    
    /**
     * 删除资源（同时删除子资源）
     */
    @Transactional
    public void delete(Long id) {
        // 递归删除子资源
        List<Resource> children = resourceMapper.findByParentId(id);
        for (Resource child : children) {
            delete(child.getId());
        }
        resourceMapper.deleteById(id);
        log.info("删除资源: id={}", id);
    }
    
    /**
     * 获取基本权限资源
     */
    public List<Resource> getBasicResources() {
        return resourceMapper.findBasicResources();
    }
    
    /**
     * 获取菜单树（用于前端菜单渲染）
     */
    public List<ResourceTreeNode> getMenuTree() {
        List<Resource> menuResources = resourceMapper.findMenuTree();
        return buildTree(menuResources, null);
    }
    
    /**
     * 资源树节点 DTO
     */
    @lombok.Data
    public static class ResourceTreeNode {
        private Long id;
        private String name;
        private String code;
        private String type;
        private String permissionKey;
        private String icon;
        private String path;
        private Integer sortOrder;
        private Integer isBasic;
        private List<ResourceTreeNode> children;
    }
}