package com.qidian.camera.module.auth.config;

import com.qidian.camera.module.auth.interceptor.PermissionInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * 注册 API 权限拦截器
 * 
 * @author qidian
 * @since 1.0.0
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    
    private final PermissionInterceptor permissionInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 API 权限拦截器
        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns("/**")  // 拦截所有请求
                .excludePathPatterns(
                    "/auth/**",                          // 认证接口
                    "/error",                            // 错误页面
                    "/favicon.ico",                      // 图标
                    "/v3/api-docs/**",                   // Swagger 文档
                    "/swagger-ui/**",                    // Swagger UI
                    "/webjars/**",                       // Webjars
                    "/actuator/**",                      // 健康检查
                    "/api/audit-logs/**"                 // 审计日志（暂时放行）
                );
    }
}
