package com.qidian.camera.module.menu.service;

import com.qidian.camera.module.menu.dto.MenuDTO;
import com.qidian.camera.module.menu.dto.UpdateUserMenuPermissionRequest;
import com.qidian.camera.module.menu.dto.UserMenuPermissionDTO;
import com.qidian.camera.module.menu.entity.Menu;
import com.qidian.camera.module.menu.entity.RoleMenuPermission;
import com.qidian.camera.module.menu.entity.UserMenuPermission;
import com.qidian.camera.module.menu.mapper.MenuMapper;
import com.qidian.camera.module.role.mapper.RoleMapper;
import com.qidian.camera.module.user.entity.User;
import com.qidian.camera.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 菜单权限服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MenuService {
    
    private final MenuMapper menuMapper;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final JdbcTemplate jdbcTemplate;
    
    /**
     * 获取用户的可见菜单列表
     */
    public List<MenuDTO> getUserMenus(Long userId) {
        List<Menu> menus = menuMapper.selectVisibleMenusByUserId(userId);
        return menus.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取所有菜单（系统管理员使用）
     */
    public List<MenuDTO> getAllMenus() {
        List<Menu> menus = menuMapper.selectList(null);
        return menus.stream()
                .sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取用户的菜单权限详情（包含角色默认权限和自定义权限）
     */
    public List<UserMenuPermissionDTO> getUserMenuPermissions(Long targetUserId) {
        User user = userMapper.selectById(targetUserId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        
        String sql = """
            SELECT 
                ump.id as ump_id,
                ump.user_id,
                ump.menu_id,
                ump.can_view as ump_can_view,
                ump.can_operate as ump_can_operate,
                ump.granted_by,
                ump.granted_at,
                u.username,
                u.real_name,
                m.menu_code,
                m.menu_name,
                rmp.can_view as rmp_can_view,
                rmp.can_operate as rmp_can_operate,
                CASE 
                    WHEN ump.id IS NOT NULL THEN 'CUSTOM'
                    ELSE 'ROLE'
                END as permission_source
            FROM menus m
            LEFT JOIN user_menu_permissions ump ON m.id = ump.menu_id AND ump.user_id = ?
            LEFT JOIN role_menu_permissions rmp ON m.id = rmp.menu_id 
            LEFT JOIN user_roles ur ON ur.user_id = ? AND ur.role_id = rmp.role_id
            LEFT JOIN users u ON u.id = ?
            WHERE m.is_visible = TRUE
            ORDER BY m.sort_order
        """;
        
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, targetUserId, targetUserId, targetUserId);
        
        return results.stream().map(row -> {
            Boolean canView = (Boolean) row.get("ump_can_view");
            Boolean canOperate = (Boolean) row.get("ump_can_operate");
            
            // 如果自定义权限存在，使用自定义权限；否则使用角色默认权限
            if (canView == null) {
                canView = (Boolean) row.get("rmp_can_view");
            }
            if (canOperate == null) {
                canOperate = (Boolean) row.get("rmp_can_operate");
            }
            
            return UserMenuPermissionDTO.builder()
                    .id(row.get("ump_id") != null ? ((Number) row.get("ump_id")).longValue() : null)
                    .userId(((Number) row.get("user_id")).longValue())
                    .username((String) row.get("username"))
                    .realName((String) row.get("real_name"))
                    .menuId(((Number) row.get("menu_id")).longValue())
                    .menuCode((String) row.get("menu_code"))
                    .menuName((String) row.get("menu_name"))
                    .canView(canView != null ? canView : false)
                    .canOperate(canOperate != null ? canOperate : false)
                    .grantedBy(row.get("granted_by") != null ? ((Number) row.get("granted_by")).longValue() : null)
                    .grantedAt(row.get("granted_at") != null ? row.get("granted_at").toString() : null)
                    .permissionSource((String) row.get("permission_source"))
                    .build();
        }).collect(Collectors.toList());
    }
    
    /**
     * 更新用户菜单权限（系统管理员操作）
     */
    @Transactional
    public void updateUserMenuPermission(UpdateUserMenuPermissionRequest request, Long operatorId) {
        // 检查操作人是否为系统管理员
        User operator = userMapper.selectById(operatorId);
        if (operator == null) {
            throw new IllegalArgumentException("操作人不存在");
        }
        
        List<String> operatorRoles = roleMapper.selectByUserId(operatorId).stream()
                .map(r -> r.getRoleCode())
                .toList();
        
        boolean isSystemAdmin = operatorRoles.stream()
                .anyMatch(r -> r.contains("SYSTEM_ADMIN"));
        
        if (!isSystemAdmin) {
            throw new SecurityException("只有系统管理员可以修改用户权限");
        }
        
        // 检查目标用户是否存在
        User targetUser = userMapper.selectById(request.getUserId());
        if (targetUser == null) {
            throw new IllegalArgumentException("目标用户不存在");
        }
        
        // 检查菜单是否存在
        Menu menu = menuMapper.selectById(request.getMenuId());
        if (menu == null) {
            throw new IllegalArgumentException("菜单不存在");
        }
        
        // 系统保护菜单不允许修改
        if (menu.getIsSystemProtected()) {
            throw new SecurityException("系统保护菜单权限不可修改");
        }
        
        // 查询是否已存在自定义权限
        String checkSql = "SELECT id FROM user_menu_permissions WHERE user_id = ? AND menu_id = ?";
        List<Map<String, Object>> existing = jdbcTemplate.queryForList(checkSql, request.getUserId(), request.getMenuId());
        
        if (existing.isEmpty()) {
            // 插入新权限
            String insertSql = """
                INSERT INTO user_menu_permissions (user_id, menu_id, can_view, can_operate, granted_by, granted_at, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """;
            jdbcTemplate.update(insertSql, 
                    request.getUserId(), 
                    request.getMenuId(), 
                    request.getCanView() != null ? request.getCanView() : false,
                    request.getCanOperate() != null ? request.getCanOperate() : false,
                    operatorId);
        } else {
            // 更新现有权限
            String updateSql = """
                UPDATE user_menu_permissions 
                SET can_view = ?, can_operate = ?, granted_by = ?, granted_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP
                WHERE user_id = ? AND menu_id = ?
            """;
            jdbcTemplate.update(updateSql,
                    request.getCanView() != null ? request.getCanView() : false,
                    request.getCanOperate() != null ? request.getCanOperate() : false,
                    operatorId,
                    request.getUserId(),
                    request.getMenuId());
        }
        
        log.info("系统管理员 {} 更新了用户 {} 的菜单 {} 权限", 
                operator.getUsername(), targetUser.getUsername(), menu.getMenuName());
    }
    
    /**
     * 删除用户自定义菜单权限（恢复为角色默认权限）
     */
    @Transactional
    public void deleteUserMenuPermission(Long userId, Long menuId, Long operatorId) {
        // 检查操作人是否为系统管理员
        User operator = userMapper.selectById(operatorId);
        if (operator == null) {
            throw new IllegalArgumentException("操作人不存在");
        }
        
        List<String> operatorRoles = roleMapper.selectByUserId(operatorId).stream()
                .map(r -> r.getRoleCode())
                .toList();
        
        boolean isSystemAdmin = operatorRoles.stream()
                .anyMatch(r -> r.contains("SYSTEM_ADMIN"));
        
        if (!isSystemAdmin) {
            throw new SecurityException("只有系统管理员可以修改用户权限");
        }
        
        String deleteSql = "DELETE FROM user_menu_permissions WHERE user_id = ? AND menu_id = ?";
        jdbcTemplate.update(deleteSql, userId, menuId);
        
        log.info("系统管理员 {} 删除了用户 {} 的菜单 {} 自定义权限", 
                operator.getUsername(), userId, menuId);
    }
    
    /**
     * 批量更新用户菜单权限
     */
    @Transactional
    public void batchUpdateUserMenuPermissions(Long userId, List<UpdateUserMenuPermissionRequest> requests, Long operatorId) {
        for (UpdateUserMenuPermissionRequest request : requests) {
            request.setUserId(userId);
            updateUserMenuPermission(request, operatorId);
        }
    }
    
    private MenuDTO convertToDTO(Menu menu) {
        return MenuDTO.builder()
                .id(menu.getId())
                .menuCode(menu.getMenuCode())
                .menuName(menu.getMenuName())
                .menuPath(menu.getMenuPath())
                .parentId(menu.getParentId())
                .sortOrder(menu.getSortOrder())
                .icon(menu.getIcon())
                .isVisible(menu.getIsVisible())
                .requiredPermission(menu.getRequiredPermission())
                .description(menu.getDescription())
                .build();
    }
}
