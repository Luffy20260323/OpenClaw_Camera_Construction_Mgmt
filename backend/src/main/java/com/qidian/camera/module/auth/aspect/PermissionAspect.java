package com.qidian.camera.module.auth.aspect;

import com.qidian.camera.common.exception.BusinessException;
import com.qidian.camera.common.exception.ErrorCode;
import com.qidian.camera.module.auth.annotation.RequirePermission;
import com.qidian.camera.module.auth.service.JwtTokenProvider;
import com.qidian.camera.module.auth.service.PermissionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * API 权限切面
 * 用于处理 @RequirePermission 注解的权限检查
 * 
 * @author qidian
 * @since 1.0.0
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {
    
    private final PermissionService permissionService;
    private final JwtTokenProvider jwtTokenProvider;
    
    /**
     * 切点：标记有 @RequirePermission 注解的方法
     */
    @Pointcut("@annotation(com.qidian.camera.module.auth.annotation.RequirePermission)")
    public void requirePermissionPointcut() {
    }
    
    /**
     * 切点：类上标记有 @RequirePermission 注解的方法
     */
    @Pointcut("within(@com.qidian.camera.module.auth.annotation.RequirePermission *)")
    public void classRequirePermissionPointcut() {
    }
    
    /**
     * 环绕通知：在方法执行前检查权限
     */
    @Around("requirePermissionPointcut() || classRequirePermissionPointcut()")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        // 获取注解（优先方法注解，其次类注解）
        RequirePermission requirePermission = method.getAnnotation(RequirePermission.class);
        if (requirePermission == null) {
            requirePermission = joinPoint.getTarget().getClass().getAnnotation(RequirePermission.class);
        }
        
        if (requirePermission == null) {
            // 没有注解，直接执行方法
            return joinPoint.proceed();
        }
        
        String permissionKey = requirePermission.value();
        log.debug("AOP 权限检查：{}", permissionKey);
        
        // 获取当前请求
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED.getCode(), "无法获取请求上下文");
        }
        
        HttpServletRequest request = attributes.getRequest();
        
        // 从请求头获取 token
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED.getCode(), "未登录，请先登录");
        }
        
        String token = authorization.substring(7);
        
        // 验证 token 并获取用户 ID
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED.getCode(), "Token 无效");
        }
        
        // 检查权限
        if (!permissionService.hasPermission(userId, permissionKey)) {
            log.warn("用户 {} 无权限访问方法 {} (需要权限：{})", 
                userId, method.getName(), permissionKey);
            throw new BusinessException(ErrorCode.FORBIDDEN.getCode(), ErrorCode.FORBIDDEN.getMessage());
        }
        
        log.debug("用户 {} 通过 AOP 权限检查：{}", userId, permissionKey);
        
        // 执行目标方法
        return joinPoint.proceed();
    }
}
