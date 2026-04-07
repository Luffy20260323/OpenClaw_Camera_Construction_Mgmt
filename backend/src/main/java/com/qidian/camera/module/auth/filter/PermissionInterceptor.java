package com.qidian.camera.module.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qidian.camera.module.auth.annotation.ApiPermission;
import com.qidian.camera.module.auth.entity.UserContext;
import com.qidian.camera.module.auth.service.impl.PermissionServiceImpl;
import com.qidian.camera.module.auth.service.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 权限拦截器（基于 Filter）
 */
@Component("filterPermissionInterceptor")
@RequiredArgsConstructor
@Slf4j
public class PermissionInterceptor implements HandlerInterceptor {
    
    private final PermissionServiceImpl permissionService;
    private final JwtTokenProvider jwtTokenProvider;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) 
            throws Exception {
        
        // 非方法处理器，直接放行（如静态资源）
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        
        // 检查方法是否有 @ApiPermission 注解
        ApiPermission apiPermission = handlerMethod.getMethodAnnotation(ApiPermission.class);
        
        // 没有注解，说明不需要权限控制，放行
        if (apiPermission == null) {
            return true;
        }
        
        String permissionCode = apiPermission.value();
        log.debug("检查权限：{}", permissionCode);
        
        // 从请求头获取 token
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(
                Map.of("code", 401, "message", "未登录", "data", null)));
            return false;
        }
        
        String token = authorization.substring(7);
        
        try {
            // 验证 token 并获取用户 ID
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            if (userId == null) {
                response.setStatus(401);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(objectMapper.writeValueAsString(
                    Map.of("code", 401, "message", "Token 无效", "data", null)));
                return false;
            }
            
            // 构建用户上下文
            UserContext userContext = buildUserContext(userId);
            permissionService.setCurrentUser(userContext);
            
            // 检查权限
            if (!userContext.hasPermission(permissionCode)) {
                log.warn("用户 {} 无权限访问 {} (需要权限：{})", 
                    userContext.getUsername(), request.getRequestURI(), permissionCode);
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(objectMapper.writeValueAsString(
                    Map.of("code", 403, "message", "无权限访问", "data", null)));
                return false;
            }
            
            log.debug("用户 {} 通过权限检查：{}", userContext.getUsername(), permissionCode);
            return true;
            
        } catch (Exception e) {
            log.error("权限检查失败", e);
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(
                Map.of("code", 401, "message", "认证失败：" + e.getMessage(), "data", null)));
            return false;
        }
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                Object handler, Exception ex) throws Exception {
        // 清除用户上下文
        permissionService.clearCurrentUser();
    }
    
    /**
     * 构建用户上下文
     */
    private UserContext buildUserContext(Long userId) {
        UserContext userContext = new UserContext();
        userContext.setUserId(userId);
        
        // 查询用户基本信息
        String userSql = "SELECT u.username, u.real_name, u.company_id, c.type_id as company_type_id " +
                         "FROM users u " +
                         "LEFT JOIN companies c ON u.company_id = c.id " +
                         "WHERE u.id = ?";
        
        Map<String, Object> userMap = jdbcTemplate.queryForMap(userSql, userId);
        userContext.setUsername((String) userMap.get("username"));
        userContext.setRealName((String) userMap.get("real_name"));
        userContext.setCompanyId(((Number) userMap.get("company_id")).longValue());
        userContext.setCompanyTypeId(((Number) userMap.get("company_type_id")).intValue());
        
        // 查询用户角色
        String roleSql = "SELECT r.role_code FROM user_roles ur " +
                         "JOIN roles r ON ur.role_id = r.id " +
                         "WHERE ur.user_id = ?";
        List<String> roleCodes = jdbcTemplate.queryForList(roleSql, String.class, userId);
        userContext.setRoleCodes(roleCodes);
        
        // 查询用户权限
        String permSql = "SELECT DISTINCT r.permission_key FROM user_roles ur " +
                         "JOIN permission p ON ur.role_id = p.role_id " +
                         "JOIN resource r ON p.resource_id = r.id " +
                         "WHERE ur.user_id = ? AND r.permission_key IS NOT NULL";
        List<String> permissions = jdbcTemplate.queryForList(permSql, String.class, userId);
        userContext.setPermissions(permissions);
        
        // 查询用户绑定的作业区
        String workAreaSql = "SELECT work_area_id FROM user_work_areas WHERE user_id = ?";
        List<Long> workAreaIds = jdbcTemplate.queryForList(workAreaSql, Long.class, userId);
        userContext.setWorkAreaIds(workAreaIds);
        
        return userContext;
    }
}
