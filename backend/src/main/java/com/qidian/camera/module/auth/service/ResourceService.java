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
     * 获取资源列表(分页)
     * @param page 页码(从 1 开始)
     * @param size 每页数量
     * @param type 类型过滤(可选)
     * @param search 搜索关键词(可选)
     * @return 分页结果
     */
    public Map<String, Object> getResourcesPaged(int page, int size, String type, String search) {
        // 获取所有启用的资源
        List<Resource> allResources = findAllEnabled();

        // 对资源进行类型过滤
        List<Resource> filteredResources = allResources;
        if (type != null && !type.isEmpty()) {
            filteredResources = filteredResources.stream()
                .filter(r -> r.getType().equals(type))
                .collect(Collectors.toList());
        }

        // 对资源进行搜索过滤
        if (search != null && !search.isEmpty()) {
            String keyword = search.toLowerCase();
            filteredResources = filteredResources.stream()
                .filter(r -> matchesSearch(r, keyword))
                .collect(Collectors.toList());
        }

        // 对资源进行排序(先按component,再按name)
        filteredResources.sort((a, b) -> {
            // 先按 component 排序(null 排在最后)
            String compA = a.getComponent() != null ? a.getComponent() : "";
            String compB = b.getComponent() != null ? b.getComponent() : "";
            int compResult = compA.compareTo(compB);
            if (compResult != 0) return compResult;

            // 再按 name 排序
            String nameA = a.getName() != null ? a.getName() : "";
            String nameB = b.getName() != null ? b.getName() : "";
            return nameA.compareTo(nameB);
        });

        // 对资源进行分页
        int total = filteredResources.size();
        int start = (page - 1) * size;
        int end = Math.min(start + size, total);
        List<Resource> pagedResources = start < total ? filteredResources.subList(start, end) : new ArrayList<>();

        // 将 Resource 转换为 ResourceTreeNode(扁平化,不包含 children)
        List<ResourceTreeNode> flatNodes = new ArrayList<>();
        for (Resource resource : pagedResources) {
            ResourceTreeNode node = new ResourceTreeNode();
            node.setId(resource.getId());
            node.setParentId(resource.getParentId());
            node.setName(resource.getName());
            node.setCode(resource.getCode());
            node.setType(resource.getType());
            node.setPermissionKey(resource.getPermissionKey());
            node.setIcon(resource.getIcon());
            node.setPath(resource.getPath());
            node.setComponent(resource.getComponent());
            node.setSortOrder(resource.getSortOrder());
            node.setIsBasic(resource.getIsBasic());
            node.setIsTopLevel(resource.getIsTopLevel());
            node.setFilePath(resource.getFilePath());
            node.setDescription(resource.getDescription());
            node.setModuleCode(resource.getModuleCode());
            node.setChildren(new ArrayList<>()); // 空 children,表示扁平化
            flatNodes.add(node);
        }

        // 返回分页结果
        Map<String, Object> result = new HashMap<>();
        result.put("content", flatNodes);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (int) Math.ceil((double) total / size));

        return result;
    }

    /**
     * 检查资源是否匹配搜索关键词
     */
    private boolean matchesSearch(Resource r, String keyword) {
        return (r.getName() != null && r.getName().toLowerCase().contains(keyword)) ||
               (r.getCode() != null && r.getCode().toLowerCase().contains(keyword)) ||
               (r.getPermissionKey() != null && r.getPermissionKey().toLowerCase().contains(keyword));
    }

    /**
     * 检查指定父节点下是否有匹配搜索关键词的子节点
     */
    private boolean hasMatchingChild(Long parentId, List<Resource> allResources, String keyword) {
        for (Resource r : allResources) {
            if (parentId.equals(r.getParentId())) {
                if (matchesSearch(r, keyword)) return true;
                // 递归检查更深层的子节点
                if (hasMatchingChild(r.getId(), allResources, keyword)) return true;
            }
        }
        return false;
    }

    /**
     * 构建单个树节点(包含所有子节点)
     */
    private ResourceTreeNode buildTreeNode(Resource resource, List<Resource> allResources, int level) {
        ResourceTreeNode node = new ResourceTreeNode();
        node.setId(resource.getId());
        node.setParentId(resource.getParentId());
        node.setName(resource.getName());
        node.setCode(resource.getCode());
        node.setType(resource.getType());
        node.setPermissionKey(resource.getPermissionKey());
        node.setIcon(resource.getIcon());
        node.setPath(resource.getPath());
        node.setSortOrder(resource.getSortOrder());
        node.setIsBasic(resource.getIsBasic());
        node.setIsTopLevel(resource.getIsTopLevel());
        node.setFilePath(resource.getFilePath());
        node.setDescription(resource.getDescription());
        node.setModuleCode(resource.getModuleCode());

        // 找出直接子节点
        List<Resource> children = allResources.stream()
            .filter(r -> resource.getId().equals(r.getParentId()))
            .sorted((a, b) -> {
                int orderA = a.getSortOrder() != null ? a.getSortOrder() : 0;
                int orderB = b.getSortOrder() != null ? b.getSortOrder() : 0;
                return orderA - orderB;
            })
            .collect(Collectors.toList());

        // 递归构建子节点
        List<ResourceTreeNode> childNodes = new ArrayList<>();
        for (Resource child : children) {
            ResourceTreeNode childNode = buildTreeNode(child, allResources, level + 1);
            childNodes.add(childNode);
        }
        node.setChildren(childNodes);

        return node;
    }

    /**
     * 获取资源树(树形结构)
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
     * 删除资源(只删除本身,子资源父级置空)
     * 模块和菜单作为容器,删除时只删除本身,子资源变为未分类
     */
    @Transactional
    public void delete(Long id) {
        Resource resource = getById(id);
        if (resource == null) {
            return;
        }

        // 模块和菜单类型:只删除本身,子资源父级置空
        if (resource.getType().equals("MODULE") || resource.getType().equals("MENU")) {
            List<Resource> children = resourceMapper.findByParentId(id);
            for (Resource child : children) {
                child.setParentId(null);
                resourceMapper.updateById(child);
                log.info("子资源父级置空: id={}, code={}", child.getId(), child.getCode());
            }
            resourceMapper.deleteById(id);
            log.info("删除模块/菜单: id={}, code={}, 子资源数量={}", id, resource.getCode(), children.size());
        } else {
            // 其他资源类型:递归删除子资源(保持原有行为)
            List<Resource> children = resourceMapper.findByParentId(id);
            for (Resource child : children) {
                delete(child.getId());
            }
            resourceMapper.deleteById(id);
            log.info("删除资源: id={}, code={}", id, resource.getCode());
        }
    }

    /**
     * 删除资源(级联删除子资源)
     * 用于需要强制删除所有子资源的场景
     */
    @Transactional
    public void deleteWithChildren(Long id) {
        List<Resource> children = resourceMapper.findByParentId(id);
        for (Resource child : children) {
            deleteWithChildren(child.getId());
        }
        resourceMapper.deleteById(id);
        log.info("级联删除资源: id={}", id);
    }

    /**
     * 获取资源的子资源数量
     */
    public int getChildCount(Long parentId) {
        return resourceMapper.findByParentId(parentId).size();
    }

    /**
     * 获取资源的子资源列表
     */
    public List<Resource> getChildren(Long parentId) {
        return resourceMapper.findByParentId(parentId);
    }

    /**
     * 更新资源的父级
     */
    @Transactional
    public void updateParent(Long resourceId, Long newParentId) {
        Resource resource = getById(resourceId);
        if (resource == null) {
            return;
        }
        resource.setParentId(newParentId);
        resourceMapper.updateById(resource);
        log.info("更新资源父级: id={}, newParentId={}", resourceId, newParentId);
    }

    /**
     * 获取基本权限资源
     */
    public List<Resource> getBasicResources() {
        return resourceMapper.findBasicResources();
    }

    /**
     * 获取菜单树(用于前端菜单渲染)
     * 支持多级动态结构:MODULE -> MODULE -> MENU -> MENU ...
     * 根据资源数据中的 parent_id 层级关系递归构建
     */
    public List<ResourceTreeNode> getMenuTree() {
        // 获取所有 MODULE 和 MENU 类型的资源
        List<Resource> allResources = resourceMapper.findMenuTree();
        log.info("getMenuTree: 获取资源数量={}, MODULE={}, MENU={}",
            allResources.size(),
            allResources.stream().filter(r -> "MODULE".equals(r.getType())).count(),
            allResources.stream().filter(r -> "MENU".equals(r.getType())).count());

        // 递归构建完整的树形结构
        // 从顶级节点(parent_id 为空)开始
        List<ResourceTreeNode> tree = buildMenuTreeRecursive(allResources, null);
        log.info("getMenuTree: 最终树节点数量={}", tree.size());

        return tree;
    }

    /**
     * 递归构建菜单树
     * @param allResources 所有 MODULE 和 MENU 资源
     * @param parentId 当前层级的父节点 ID(null 表示顶级)
     * @return 当前层级的子节点列表
     */
    private List<ResourceTreeNode> buildMenuTreeRecursive(List<Resource> allResources, Long parentId) {
        // 先统计当前层级的子节点数量(用于日志)
        long childCount = allResources.stream()
            .filter(r -> parentId == null ? r.getParentId() == null : (r.getParentId() != null && r.getParentId().equals(parentId)))
            .count();
        log.info("buildMenuTreeRecursive: parentId={}, 子节点数量={}, 包含: MODULE={}, MENU={}",
            parentId, childCount,
            allResources.stream().filter(r -> "MODULE".equals(r.getType()) && (parentId == null ? r.getParentId() == null : (r.getParentId() != null && r.getParentId().equals(parentId)))).count(),
            allResources.stream().filter(r -> "MENU".equals(r.getType()) && (parentId == null ? r.getParentId() == null : (r.getParentId() != null && r.getParentId().equals(parentId)))).count());

        return allResources.stream()
            // 筛选当前层级的子节点(parent_id 匹配)
            .filter(r -> {
                // 处理顶级节点:parent_id 为 null
                if (parentId == null) {
                    return r.getParentId() == null;
                }
                // 处理子节点:parent_id 匹配
                return r.getParentId() != null && r.getParentId().equals(parentId);
            })
            .map(r -> {
                ResourceTreeNode node = convertToNode(r);
                log.info("构建节点: id={}, type={}, code={}, name={}, parentId={}",
                    r.getId(), r.getType(), r.getCode(), r.getName(), r.getParentId());

                // 递归查找子节点(支持 MODULE 和 MENU 都可以有子节点)
                List<ResourceTreeNode> children = buildMenuTreeRecursive(allResources, r.getId());
                if (!children.isEmpty()) {
                    node.setChildren(children);
                    log.info("节点 {} (id={}) 有 {} 个子节点", r.getName(), r.getId(), children.size());
                }

                return node;
            })
            .sorted(Comparator.comparing(ResourceTreeNode::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder())))
            .collect(Collectors.toList());
    }

    /**
     * 转换为节点
     */
    private ResourceTreeNode convertToNode(Resource r) {
        ResourceTreeNode node = new ResourceTreeNode();
        node.setId(r.getId());
        node.setParentId(r.getParentId());
        node.setName(r.getName());
        node.setCode(r.getCode());
        node.setType(r.getType());
        node.setPermissionKey(r.getPermissionKey());
        node.setIcon(r.getIcon());
        // MENU 类型不设置 path(路由由 PAGE 资源控制)
        if (!"MENU".equals(r.getType())) {
            node.setPath(r.getPath());
        }
        node.setSortOrder(r.getSortOrder());
        node.setIsBasic(r.getIsBasic());
        node.setIsTopLevel(r.getIsTopLevel());
        node.setFilePath(r.getFilePath());
        node.setDescription(r.getDescription());
        node.setModuleCode(r.getModuleCode());
        return node;
    }

    /**
     * 资源树节点 DTO
     * 兼容旧的 MenuDTO 字段名
     */
    @lombok.Data
    public static class ResourceTreeNode {
        private Long id;
        private Long parentId;
        private String name;
        private String code;
        private String type;
        private String permissionKey;
        private String icon;
        private String path;
        private String component;
        private Integer sortOrder;
        private Integer isBasic;
        private Boolean isTopLevel;
        private String filePath;
        private String description;
        private String moduleCode;
        private List<ResourceTreeNode> children;
        
        // 前端兼容字段（JSON 序列化时作为额外字段）
        @com.fasterxml.jackson.annotation.JsonProperty("menuName")
        public String getMenuName() { return name; }
        
        @com.fasterxml.jackson.annotation.JsonProperty("menuCode")
        public String getMenuCode() { return code; }
        
        @com.fasterxml.jackson.annotation.JsonProperty("menuPath")
        public String getMenuPath() { 
            // MENU 类型不返回 path（路由由 PAGE 资源控制）
            if ("MENU".equals(type)) {
                return null;
            }
            return path;
        }
    }

    /**
     * 刷新资源缓存
     * 由于 ResourceService 使用数据库直接查询,刷新缓存只需重新加载资源列表
     */
    public void refreshCache() {
        log.info("刷新资源缓存:重新加载资源列表");
        // 重新加载资源列表(触发数据库查询)
        findAllEnabled();
        log.info("资源缓存刷新完成");
    }

    /**
     * 获取孤儿资源(没有父资源的资源)
     * 顶级资源(is_top_level = true)不被认定为孤儿资源
     */
    public List<Resource> getOrphanResources() {
        List<Resource> allResources = findAllEnabled();
        return allResources.stream()
            .filter(r -> (r.getParentId() == null || r.getParentId() == 0))
            .filter(r -> !Boolean.TRUE.equals(r.getIsTopLevel())) // 排除顶级资源
            .collect(Collectors.toList());
    }
}