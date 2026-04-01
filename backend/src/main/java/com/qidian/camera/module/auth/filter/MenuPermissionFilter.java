package com.qidian.camera.module.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qidian.camera.common.response.Result;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 菜单权限过滤器 - 防止越权访问
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MenuPermissionFilter extends OncePerRequestFilter {
    
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    
    // 不需要权限验证的公开路径
    private static final String[] PUBLIC_PATHS = {
        "/api/auth/login",
        "/api/auth/refresh",
        "/api/auth/logout",
        "/api/auth/captcha",
        "/api/auth/captcha/config",
        "/api/user/register",
        "/api/company", // 公司列表（注册用）
        "/api/role", // 角色列表（注册用）
        "/api/workarea", // 作业区列表（注册用）
        "/v3/api-docs",
        "/swagger-ui",
        "/swagger-ui.html"
    };
    
    // 权限配置相关路径（需要认证，但不检查菜单权限）
    private static final String[] PERMISSION_CONFIG_PATHS = {
        "/api/permission/list",
        "/api/permission/groups",
        "/api/permission/roles",
        "/api/permission/role/",
        "/api/permission/"
    };
    
    // 菜单路径映射
    private static final Map<String, String> PATH_TO_MENU_MAP = new HashMap<>();
    static {
        PATH_TO_MENU_MAP.put("/api/user/management", "user_management");
        PATH_TO_MENU_MAP.put("/api/user/profile", "profile");
        PATH_TO_MENU_MAP.put("/api/user", "user_management");
        PATH_TO_MENU_MAP.put("/api/role", "role_management");
        PATH_TO_MENU_MAP.put("/api/workarea", "workarea_management");
        // /api/company 已在 PUBLIC_PATHS 中定义为公开路径（注册和公司列表查询用）
        // 不需要权限检查
        PATH_TO_MENU_MAP.put("/api/system/config", "system_config");
        PATH_TO_MENU_MAP.put("/api/menu", "system_config");
        // /api/permission 相关接口在 PERMISSION_CONFIG_PATHS 中单独处理
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        // 检查是否是公开路径
        if (isPublicPath(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // 检查是否是权限配置路径（需要认证但不检查菜单权限）
        if (isPermissionConfigPath(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // 获取用户 ID（从 JWT 过滤器设置的属性）
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            // 没有用户 ID，可能是未登录，让后续的 JWT 过滤器处理
            filterChain.doFilter(request, response);
            return;
        }
        
        // 获取菜单编码
        String menuCode = getMenuCodeFromPath(requestURI);
        if (menuCode == null) {
            // 没有映射的路径，默认放行（可能是其他 API）
            filterChain.doFilter(request, response);
            return;
        }
        
        // 个人中心始终允许访问
        if ("profile".equals(menuCode)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // 检查用户是否有该菜单的访问权限
        boolean hasPermission = checkMenuPermission(userId, menuCode);
        if (!hasPermission) {
            log.warn("用户 {} 尝试越权访问菜单 {} [{}]", userId, menuCode, requestURI);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            Result<Void> errorResult = Result.error("没有访问权限");
            response.getWriter().write(objectMapper.writeValueAsString(errorResult));
            return;
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * 检查是否是公开路径
     */
    private boolean isPublicPath(String requestURI) {
        for (String publicPath : PUBLIC_PATHS) {
            if (requestURI.startsWith(publicPath)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查是否是权限配置路径（需要认证但不检查菜单权限）
     */
    private boolean isPermissionConfigPath(String requestURI) {
        for (String path : PERMISSION_CONFIG_PATHS) {
            if (requestURI.startsWith(path)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 从请求中获取用户 ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        } else if (userIdObj instanceof Integer) {
            return ((Integer) userIdObj).longValue();
        } else if (userIdObj instanceof String) {
            try {
                return Long.parseLong((String) userIdObj);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
    /**
     * 从路径获取菜单编码
     */
    private String getMenuCodeFromPath(String requestURI) {
        for (Map.Entry<String, String> entry : PATH_TO_MENU_MAP.entrySet()) {
            if (requestURI.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }
    
    /**
     * 检查用户是否有菜单权限
     */
    private boolean checkMenuPermission(Long userId, String menuCode) {
        try {
            // 先检查自定义权限
            String customSql = """
                SELECT can_view FROM user_menu_permissions ump
                INNER JOIN menus m ON ump.menu_id = m.id
                WHERE ump.user_id = ? AND m.menu_code = ?
            """;
            List<Boolean> customResult = jdbcTemplate.queryForList(customSql, Boolean.class, userId, menuCode);
            if (!customResult.isEmpty()) {
                return customResult.get(0) != null && customResult.get(0);
            }
            
            // 再检查角色默认权限
            String roleSql = """
                SELECT rmp.can_view FROM role_menu_permissions rmp
                INNER JOIN menus m ON rmp.menu_id = m.id
                INNER JOIN user_roles ur ON ur.role_id = rmp.role_id
                WHERE ur.user_id = ? AND m.menu_code = ?
                LIMIT 1
            """;
            List<Boolean> roleResult = jdbcTemplate.queryForList(roleSql, Boolean.class, userId, menuCode);
            if (!roleResult.isEmpty()) {
                return roleResult.get(0) != null && roleResult.get(0);
            }
            
            return false;
        } catch (Exception e) {
            log.error("检查菜单权限失败：{}", e.getMessage(), e);
            return false;
        }
    }
}
