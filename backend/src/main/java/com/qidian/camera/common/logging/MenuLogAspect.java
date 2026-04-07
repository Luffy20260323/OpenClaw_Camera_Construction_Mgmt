package com.qidian.camera.common.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 菜单日志切面
 * 专门记录菜单相关的详细日志
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class MenuLogAspect {
    
    private final LogConfigManager logConfigManager;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 菜单服务切点
     */
    @Pointcut("execution(* com.qidian.camera.module.menu.service.*.*(..))")
    public void menuServicePointcut() {}
    
    /**
     * 资源服务菜单相关方法切点
     */
    @Pointcut("execution(* com.qidian.camera.module.auth.service.ResourceService.getMenuTree(..)) || " +
              "execution(* com.qidian.camera.module.auth.service.ResourceService.getResourceTree(..))")
    public void resourceMenuPointcut() {}
    
    /**
     * 环绕通知
     */
    @Around("menuServicePointcut() || resourceMenuPointcut()")
    public Object logMenuOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取当前日志级别
        String sessionId = "system"; // 系统级操作
        String packageName = joinPoint.getTarget().getClass().getPackage().getName();
        LogLevel logLevel = logConfigManager.getLogLevel(sessionId, packageName);
        
        // DEBUG 级别以下不记录详细日志
        if (logLevel.ordinal() < LogLevel.DEBUG.ordinal()) {
            return joinPoint.proceed();
        }
        
        String methodName = joinPoint.getSignature().getName();
        long startTime = System.currentTimeMillis();
        
        // 记录方法调用
        log.debug("【菜单操作开始】method={}, params={}", 
            methodName, toJson(joinPoint.getArgs()));
        
        Object result;
        try {
            result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            // 记录结果
            if (result instanceof List) {
                List<?> list = (List<?>) result;
                log.debug("【菜单操作成功】method={}, duration={}ms, resultCount={}", 
                    methodName, duration, list.size());
                
                // TRACE 级别记录详细内容
                if (logLevel.ordinal() >= LogLevel.TRACE.ordinal()) {
                    log.trace("【菜单操作详情】method={}, result={}", methodName, toJson(result));
                }
            } else {
                log.debug("【菜单操作成功】method={}, duration={}ms", methodName, duration);
            }
            
            return result;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            log.error("【菜单操作失败】method={}, duration={}ms, error={}", 
                methodName, duration, e.getMessage(), e);
            
            throw e;
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
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }
}
