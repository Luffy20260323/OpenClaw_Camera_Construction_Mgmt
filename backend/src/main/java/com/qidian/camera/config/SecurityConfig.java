package com.qidian.camera.config;

import com.qidian.camera.module.auth.filter.JwtAuthenticationFilter;
import com.qidian.camera.module.auth.filter.MenuPermissionFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 配置
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final MenuPermissionFilter menuPermissionFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF
            .csrf(csrf -> csrf.disable())
            
            // 配置 CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 禁用 Session（JWT 无状态）
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 禁用匿名认证（避免 AnonymousAuthenticationFilter 覆盖 JWT 认证）
            .anonymous(anonymous -> anonymous.disable())
            
            // 添加 JWT 过滤器（必须在 authorizeHttpRequests 之前）
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            
            // 添加菜单权限过滤器（在 JWT 过滤器之后）
            .addFilterAfter(menuPermissionFilter, JwtAuthenticationFilter.class)
            
            // 配置授权规则（注意：路径相对于上下文路径 /api）
            .authorizeHttpRequests(auth -> auth
                // 登录接口必须放在最前面
                .requestMatchers("/auth/login").permitAll()
                .requestMatchers("/auth/refresh").permitAll()
                .requestMatchers("/auth/validate").permitAll()
                .requestMatchers("/auth/**").permitAll()
                // 用户注册接口（公开）
                .requestMatchers("/user/register").permitAll()
                // 系统管理接口（需要认证）
                .requestMatchers("/company/**").permitAll()
                .requestMatchers("/role/**").permitAll()
                .requestMatchers("/workarea/**").permitAll()
                // 权限管理接口（权限配置页面使用）
                .requestMatchers("/permission/list").permitAll()
                .requestMatchers("/permission/groups").permitAll()
                .requestMatchers("/permission/roles").permitAll()
                .requestMatchers("/permission/**").authenticated()
                // 权限管理接口（依赖菜单权限过滤器控制，此处只需认证）
                .requestMatchers("/permission/**").authenticated()
                // 资源管理接口（暂时 permitAll 用于调试）
                .requestMatchers("/resource/**").permitAll()
                .requestMatchers("/resources/**").permitAll()
                // 零部件管理接口
                .requestMatchers("/component-types/**").permitAll()
                .requestMatchers("/component-attr-sets/**").permitAll()
                .requestMatchers("/component-instances/**").permitAll()
                .requestMatchers("/component-attr-set-instances/**").permitAll()
                .requestMatchers("/point-device-models/**").permitAll()
                .requestMatchers("/point-device-model-instances/**").permitAll()
                .requestMatchers("/point-batch-assignment/**").permitAll()
                .requestMatchers("/user/**").authenticated()
                .requestMatchers("/system/**").authenticated()
                .requestMatchers("/menu/**").authenticated()
                // 审计日志接口（需要认证）
                .requestMatchers("/audit-logs/**").authenticated()
                // Swagger/API 文档
                .requestMatchers("/doc.html").permitAll()
                .requestMatchers("/doc/**").permitAll()
                .requestMatchers("/swagger/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/swagger-resources/**").permitAll()
                .requestMatchers("/webjars/**").permitAll()
                // 其他所有请求需要认证
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated()
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
