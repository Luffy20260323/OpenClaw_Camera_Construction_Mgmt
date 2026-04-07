package com.qidian.camera.common.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.UUID;

/**
 * API 请求日志切面
 * 记录所有 API 请求的详细信息
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ApiRequestLogAspect {
    
    private final LogConfigManager logConfigManager;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Controller 层切点
     */
    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void controllerPointcut() {}
    
    /**
     * 环绕通知
     */
    @Around("controllerPointcut()")
    public Object logApiRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取当前日志级别
        String sessionId = getSessionId();
        String packageName = joinPoint.getTarget().getClass().getPackage().getName();
        LogLevel logLevel = logConfigManager.getLogLevel(sessionId, packageName);
        
        // 根据日志级别决定是否记录详细日志
        if (logLevel.ordinal() < LogLevel.DEBUG.ordinal()) {
            return joinPoint.proceed();
        }
        
        // 生成请求 ID
        String requestId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();
        
        // 获取请求信息
        HttpServletRequest request = getRequest();
        String method = request != null ? request.getMethod() : "UNKNOWN";
        String uri = request != null ? request.getRequestURI() : "UNKNOWN";
        String userAgent = request != null ? request.getHeader("User-Agent") : "UNKNOWN";
        
        // 记录请求开始
        log.debug("【API 请求开始】requestId={}, method={}, uri={}, userAgent={}", 
            requestId, method, uri, userAgent);
        log.debug("【API 请求参数】requestId={}, params={}", 
            requestId, toJson(joinPoint.getArgs()));
        
        Object result;
        try {
            result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            // 记录响应
            log.debug("【API 请求成功】requestId={}, duration={}ms, result={}", 
                requestId, duration, toJson(result));
            
            return result;
        } catch (Throwable e) {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            // 记录异常
            log.error("【API 请求失败】requestId={}, duration={}ms, error={}", 
                requestId, duration, e.getMessage(), e);
            
            throw e;
        }
    }
    
    /**
     * 获取当前会话 ID
     */
    private String getSessionId() {
        HttpServletRequest request = getRequest();
        if (request != null) {
            // 从请求头获取 userId
            String userId = request.getHeader("X-User-Id");
            if (userId != null) {
                return logConfigManager.generateSessionId(Long.parseLong(userId));
            }
        }
        return null;
    }
    
    /**
     * 获取 HttpServletRequest
     */
    private HttpServletRequest getRequest() {
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 对象转 JSON 字符串
     */
    private String toJson(Object obj) {
        try {
            if (obj == null) {
                return "null";
            }
            if (obj.getClass().isArray()) {
                return Arrays.toString((Object[]) obj);
            }
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }
}
