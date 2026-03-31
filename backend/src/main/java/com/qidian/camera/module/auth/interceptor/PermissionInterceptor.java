package com.qidian.camera.module.auth.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qidian.camera.common.exception.ErrorCode;
import com.qidian.camera.common.response.Result;
import com.qidian.camera.module.auth.annotation.RequirePermission;
import com.qidian.camera.module.auth.entity.Resource;
import com.qidian.camera.module.auth.entity.UserContext;
import com.qidian.camera.module.auth.mapper.ResourceMapper;
import com.qidian.camera.module.auth.service.JwtTokenProvider;
import com.qidian.camera.module.auth.service.PermissionService;
import com.qidian.camera.module.auth.util.UrlPatternMatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

/**
 * API 权限拦截器
 * 基于 URL 和 Method 匹配 resource 表中的 API 记录
 * 支持通配符和路径变量匹配
 * 
 * @author qidian
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionInterceptor implements HandlerInterceptor {
    
    private final PermissionService permissionService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ResourceMapper resourceMapper;
    private final UrlPatternMatcher urlPatternMatcher;
    private final ObjectMapper objectMapper;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) 
            throws Exception {
        
        // 非方法处理器，直接放行（如静态资源）
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        
        // 检查方法或类是否有 @RequirePermission 注解
        RequirePermission methodPermission = handlerMethod.getMethodAnnotation(RequirePermission.class);
        RequirePermission classPermission = handlerMethod.getBeanType().getAnnotation(RequirePermission.class);
        
        // 没有注解，通过 URL 匹配 resource 表进行权限检查
        if (methodPermission == null && classPermission == null) {
            return checkApiPermissionByUri(request, response);
        }
        
        // 有注解，优先使用注解的 permission_key
        String permissionKey = methodPermission != null ? methodPermission.value() : classPermission.value();
        log.debug("检查注解权限：{}", permissionKey);
        
        // 从请求头获取 token
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            sendErrorResponse(response, ErrorCode.UNAUTHORIZED.getCode(), "未登录，请先登录");
            return false;
        }
        
        String token = authorization.substring(7);
        
        try {
            // 验证 token 并获取用户 ID
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            if (userId == null) {
                sendErrorResponse(response, ErrorCode.UNAUTHORIZED.getCode(), "Token 无效");
                return false;
            }
            
            // 检查权限
            if (!permissionService.hasPermission(userId, permissionKey)) {
                log.warn("用户 {} 无权限访问 {} (需要权限：{})", 
                    userId, request.getRequestURI(), permissionKey);
                sendErrorResponse(response, ErrorCode.FORBIDDEN.getCode(), ErrorCode.FORBIDDEN.getMessage());
                return false;
            }
            
            log.debug("用户 {} 通过权限检查：{}", userId, permissionKey);
            return true;
            
        } catch (Exception e) {
            log.error("权限检查失败", e);
            sendErrorResponse(response, ErrorCode.UNAUTHORIZED.getCode(), "认证失败：" + e.getMessage());
            return false;
        }
    }
    
    /**
     * 通过 URI 匹配 resource 表进行权限检查
     */
    private boolean checkApiPermissionByUri(HttpServletRequest request, HttpServletResponse response) 
            throws Exception {
        
        String uri = request.getRequestURI();
        String method = request.getMethod();
        
        log.debug("检查 API 权限：{} {}", method, uri);
        
        // 获取所有 API 类型的资源
        List<Resource> apiResources = resourceMapper.findByType(Resource.TYPE_API);
        
        // 查找匹配的 API 资源
        Resource matchedResource = null;
        for (Resource resource : apiResources) {
            if (resource.getUriPattern() != null && 
                resource.getMethod() != null &&
                resource.getMethod().equals(method) &&
                urlPatternMatcher.matches(resource.getUriPattern(), uri)) {
                matchedResource = resource;
                break;
            }
        }
        
        // 未配置权限的 API 默认放行
        if (matchedResource == null) {
            log.debug("未找到匹配的 API 资源，默认放行：{} {}", method, uri);
            return true;
        }
        
        log.debug("匹配到 API 资源：id={}, name={}, permission_key={}", 
            matchedResource.getId(), matchedResource.getName(), matchedResource.getPermissionKey());
        
        // 从请求头获取 token
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            sendErrorResponse(response, ErrorCode.UNAUTHORIZED.getCode(), "未登录，请先登录");
            return false;
        }
        
        String token = authorization.substring(7);
        
        try {
            // 验证 token 并获取用户 ID
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            if (userId == null) {
                sendErrorResponse(response, ErrorCode.UNAUTHORIZED.getCode(), "Token 无效");
                return false;
            }
            
            // 检查用户是否有该资源权限
            if (!permissionService.hasResourcePermission(userId, matchedResource.getId())) {
                log.warn("用户 {} 无权限访问 API 资源：{} {}", userId, method, uri);
                sendErrorResponse(response, ErrorCode.FORBIDDEN.getCode(), ErrorCode.FORBIDDEN.getMessage());
                return false;
            }
            
            log.debug("用户 {} 通过 API 资源权限检查：{}", userId, matchedResource.getName());
            return true;
            
        } catch (Exception e) {
            log.error("API 权限检查失败", e);
            sendErrorResponse(response, ErrorCode.UNAUTHORIZED.getCode(), "认证失败：" + e.getMessage());
            return false;
        }
    }
    
    /**
     * 发送错误响应
     */
    private void sendErrorResponse(HttpServletResponse response, Integer code, String message) 
            throws Exception {
        response.setStatus(code);
        response.setContentType("application/json;charset=UTF-8");
        Result<?> result = Result.error(code, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
