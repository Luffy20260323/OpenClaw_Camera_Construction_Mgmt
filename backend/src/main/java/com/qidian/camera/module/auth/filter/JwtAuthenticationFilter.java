package com.qidian.camera.module.auth.filter;

import com.qidian.camera.module.auth.service.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * JWT 认证过滤器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 1. 从请求头中获取 JWT 令牌
            String token = getTokenFromRequest(request);

            if (StringUtils.hasText(token)) {
                log.debug("获取到 Token: {}", token.substring(0, Math.min(50, token.length())));
                
                // 2. 验证令牌
                boolean isValid = jwtTokenProvider.validateToken(token, null);
                log.debug("Token 验证结果：{}", isValid);
                
                if (isValid) {
                    // 3. 从令牌中获取用户信息
                    String username = jwtTokenProvider.getUsernameFromToken(token);
                    Long userId = jwtTokenProvider.getUserIdFromToken(token);
                    List<String> roles = jwtTokenProvider.getRolesFromToken(token);
                    List<String> permissions = jwtTokenProvider.getPermissionsFromToken(token);
                    
                    log.debug("用户信息：username={}, roles={}, permissions={}", username, roles, permissions);

                    // 4. 创建认证对象
                    JwtUserDetails userDetails = new JwtUserDetails(userId, username, roles, permissions);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // 5. 设置到 SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    // 6. 将 userId 设置到 request attribute，供控制器使用
                    request.setAttribute("userId", userId);

                    log.info("设置用户认证信息：username={}, roles={}, permissions={}", 
                        username, roles, permissions);
                }
            }
        } catch (Exception e) {
            log.error("无法设置用户认证信息：{}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中获取令牌
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * JWT 用户详情
     */
    @RequiredArgsConstructor
    public static class JwtUserDetails implements org.springframework.security.core.userdetails.UserDetails {

        private final Long userId;
        private final String username;
        private final List<String> roles;
        private final List<String> permissions;

        public Long getUserId() {
            return userId;
        }

        @Override
        public List<org.springframework.security.core.GrantedAuthority> getAuthorities() {
            return permissions.stream()
                    .map(permission -> (org.springframework.security.core.GrantedAuthority)
                            () -> "PERMISSION_" + permission)
                    .collect(java.util.stream.Collectors.toList());
        }

        @Override
        public String getPassword() {
            return null;
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }
}
