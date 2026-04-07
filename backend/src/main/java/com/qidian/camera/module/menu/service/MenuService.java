package com.qidian.camera.module.menu.service;

import com.qidian.camera.module.menu.dto.MenuDTO;
import com.qidian.camera.module.auth.service.ResourceService;
import com.qidian.camera.module.auth.service.PermissionService;
import com.qidian.camera.module.role.mapper.RoleMapper;
import com.qidian.camera.module.role.entity.Role;
import com.qidian.camera.module.auth.entity.Resource;
import com.qidian.camera.module.auth.mapper.ResourceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单服务
 * 
 * 菜单树生成逻辑（V2 - 从下向上构建）：
 * 1. 根据用户权限，计算实际权限集合（资源ID集合）
 * 2. 根据权限集合，找到所有 MENU 类型资源
 * 3. 从每个 MENU 向上构建菜单树（补齐父级 MODULE）
 * 
 * 设计原则：
 * - MODULE 只是容器，不作为菜单项显示
 * - MENU 是菜单项的基本单元
 * - 只有用户有权限的 MENU 才显示，MODULE 自动补齐
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MenuService {
    
    private final ResourceService resourceService;
    private final PermissionService permissionService;
    private final RoleMapper roleMapper;
    private final ResourceMapper resourceMapper;
    
    /**
     * 超级管理员角色编码
     */
    private static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";
    
    /**
     * 获取用户的可见菜单列表
     * 
     * 生成逻辑：
     * 1. 计算用户实际权限集合（资源ID集合）
     * 2. 找到所有 MENU 类型资源（用户有权限的）
     * 3. 从 MENU 向上构建菜单树
     */
    public List<MenuDTO> getUserMenus(Long userId) {
        log.info("获取用户 {} 的菜单列表", userId);
        
        // 1. 判断是否超级管理员
        boolean isSuperAdmin = isSuperAdmin(userId);
        
        // 2. 计算用户实际权限集合（资源ID集合）
        Set<Long> userResourceIds = permissionService.calculateUserPermissions(userId);
        log.info("用户 {} 权限资源数量={}, 是否超级管理员={}", userId, userResourceIds.size(), isSuperAdmin);
        
        // 3. 获取所有资源（构建父子关系映射）
        List<Resource> allResources = resourceMapper.selectList(null);
        Map<Long, Resource> resourceMap = allResources.stream()
            .collect(Collectors.toMap(Resource::getId, r -> r));
        Map<Long, List<Resource>> childrenMap = allResources.stream()
            .filter(r -> r.getParentId() != null)
            .collect(Collectors.groupingBy(Resource::getParentId));
        
        // 4. 找到用户有权限的所有 MENU 资源
        List<Resource> userMenus = allResources.stream()
            .filter(r -> "MENU".equals(r.getType()))
            .filter(r -> userResourceIds.contains(r.getId()))
            .collect(Collectors.toList());
        
        log.info("用户 {} 有权限的 MENU 数量={}", userId, userMenus.size());
        
        // 调试：打印用户有权限的 MENU 编码列表
        log.info("用户 {} 有权限的 MENU 编码: {}", userId, 
            userMenus.stream().map(Resource::getCode).collect(Collectors.joining(",")));
        
        // 5. 从 MENU 向上构建菜单树
        // 收集所有需要显示的 MODULE 和 MENU
        Set<Long> visibleResourceIds = new HashSet<>();
        for (Resource menu : userMenus) {
            // 添加 MENU 本身
            visibleResourceIds.add(menu.getId());
            // 向上查找父级 MODULE
            Long parentId = menu.getParentId();
            while (parentId != null) {
                Resource parent = resourceMap.get(parentId);
                if (parent != null && "MODULE".equals(parent.getType())) {
                    visibleResourceIds.add(parent.getId());
                    parentId = parent.getParentId();
                } else {
                    break; // 非 MODULE 类型，停止向上查找
                }
            }
        }
        
        log.info("用户 {} 需显示的资源数量={} (MENU + MODULE)", userId, visibleResourceIds.size());
        
        // 6. 构建菜单树（只包含 visibleResourceIds 中的资源）
        List<MenuDTO> menuTree = buildMenuTree(visibleResourceIds, resourceMap, childrenMap, null);
        
        log.info("用户 {} 最终菜单树顶级节点数量={}", userId, menuTree.size());
        return menuTree;
    }
    
    /**
     * 获取所有菜单（超级管理员使用）
     * 返回完整的菜单树
     */
    public List<MenuDTO> getAllMenus() {
        log.info("获取所有菜单");
        
        List<Resource> allResources = resourceMapper.selectList(null);
        Map<Long, Resource> resourceMap = allResources.stream()
            .collect(Collectors.toMap(Resource::getId, r -> r));
        Map<Long, List<Resource>> childrenMap = allResources.stream()
            .filter(r -> r.getParentId() != null)
            .collect(Collectors.groupingBy(Resource::getParentId));
        
        // 超级管理员显示所有 MODULE 和 MENU
        Set<Long> allModuleMenuIds = allResources.stream()
            .filter(r -> "MODULE".equals(r.getType()) || "MENU".equals(r.getType()))
            .map(Resource::getId)
            .collect(Collectors.toSet());
        
        return buildMenuTree(allModuleMenuIds, resourceMap, childrenMap, null);
    }
    
    /**
     * 判断用户是否是超级管理员
     */
    private boolean isSuperAdmin(Long userId) {
        List<Role> roles = roleMapper.selectByUserId(userId);
        return roles.stream()
            .anyMatch(role -> ROLE_SUPER_ADMIN.equals(role.getRoleCode()));
    }
    
    /**
     * 构建菜单树
     * 
     * @param visibleResourceIds 需要显示的资源ID集合
     * @param resourceMap 资源ID -> 资源对象映射
     * @param childrenMap 父ID -> 子资源列表映射
     * @param parentId 当前层级父ID（null 表示顶级）
     */
    private List<MenuDTO> buildMenuTree(
            Set<Long> visibleResourceIds,
            Map<Long, Resource> resourceMap,
            Map<Long, List<Resource>> childrenMap,
            Long parentId) {
        
        List<MenuDTO> result = new ArrayList<>();
        
        // 获取当前层级的子资源
        // 对于顶级（parentId=null），需要从 resourceMap 中筛选 parentId 为 null 的资源
        List<Resource> children;
        if (parentId == null) {
            // 顶级节点：从 resourceMap 中筛选 parentId 为 null 的资源
            children = resourceMap.values().stream()
                .filter(r -> r.getParentId() == null)
                .filter(r -> visibleResourceIds.contains(r.getId()))
                .filter(r -> "MODULE".equals(r.getType()) || "MENU".equals(r.getType()))
                .sorted(Comparator.comparing(Resource::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
        } else {
            // 子节点：从 childrenMap 中获取
            children = childrenMap.getOrDefault(parentId, Collections.emptyList())
                .stream()
                .filter(r -> visibleResourceIds.contains(r.getId()))
                .filter(r -> "MODULE".equals(r.getType()) || "MENU".equals(r.getType()))
                .sorted(Comparator.comparing(Resource::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
        }
        
        for (Resource child : children) {
            // 递归构建子树
            List<MenuDTO> childMenus = buildMenuTree(visibleResourceIds, resourceMap, childrenMap, child.getId());
            
            // 调试日志：打印日志中心和其子菜单
            if (child.getId() == 506L) {
                log.info("日志中心(id=506) 子菜单数量: {}, 子菜单详情: {}", childMenus.size(), 
                    childMenus.stream().map(m -> m.getMenuCode() + "(" + m.getId() + ")").collect(Collectors.joining(",")));
            }
            
            MenuDTO dto = MenuDTO.builder()
                .id(child.getId())
                .menuName(child.getName())
                .menuCode(child.getCode())
                .menuPath(child.getPath())
                .parentId(parentId)
                .sortOrder(child.getSortOrder())
                .icon(child.getIcon())
                .isVisible(true)
                .requiredPermission(child.getPermissionKey())
                .userCanView(true)
                .userCanOperate(false)
                .children(childMenus.isEmpty() ? null : childMenus)
                .build();
            
            result.add(dto);
        }
        
        return result;
    }
}