package com.qidian.camera.module.menu.service;

import com.qidian.camera.module.menu.dto.MenuDTO;
import com.qidian.camera.module.auth.service.ResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单服务（已整合到资源服务）
 * 注意：此类仅作为向后兼容的包装器，实际数据从 resource 表读取
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MenuService {
    
    private final ResourceService resourceService;
    
    /**
     * 获取用户的可见菜单列表
     * 实际从 resource 表的 MENU 类型资源读取
     */
    public List<MenuDTO> getUserMenus(Long userId) {
        log.info("获取用户 {} 的菜单列表", userId);
        // 调用资源服务获取菜单树
        List<ResourceService.ResourceTreeNode> tree = resourceService.getMenuTree();
        log.info("获取到 {} 个顶级菜单项", tree.size());
        for (ResourceService.ResourceTreeNode node : tree) {
            log.info("  - {}: {} ({} children)", node.getId(), node.getMenuName(), 
                node.getChildren() != null ? node.getChildren().size() : 0);
        }
        // 转换为 MenuDTO 树形结构
        List<MenuDTO> menus = convertTree(tree);
        log.info("转换后共 {} 个顶级菜单", menus.size());
        return menus;
    }
    
    /**
     * 获取所有菜单（系统管理员使用）
     * 实际从 resource 表的 MENU 类型资源读取
     */
    public List<MenuDTO> getAllMenus() {
        log.info("获取所有菜单");
        List<ResourceService.ResourceTreeNode> tree = resourceService.getMenuTree();
        return convertTree(tree);
    }
    
    /**
     * 将 ResourceTreeNode 树转换为 MenuDTO 树
     */
    private List<MenuDTO> convertTree(List<ResourceService.ResourceTreeNode> nodes) {
        return convertTree(nodes, null);
    }
    
    /**
     * 递归转换树形结构
     */
    private List<MenuDTO> convertTree(List<ResourceService.ResourceTreeNode> nodes, Long parentId) {
        List<MenuDTO> result = new ArrayList<>();
        for (ResourceService.ResourceTreeNode node : nodes) {
            MenuDTO dto = MenuDTO.builder()
                .id(node.getId())
                .menuName(node.getMenuName())
                .menuCode(node.getMenuCode())
                .menuPath(node.getMenuPath())
                .parentId(parentId)
                .sortOrder(node.getSortOrder())
                .icon(node.getIcon())
                .isVisible(node.getIsVisible() != null ? node.getIsVisible() : true)
                .requiredPermission(node.getPermissionKey())
                .userCanView(true)
                .userCanOperate(false)
                .children(node.getChildren() != null ? convertTree(node.getChildren(), node.getId()) : null)
                .build();
            result.add(dto);
        }
        return result;
    }
}
