package com.qidian.camera.module.auth.service;

import com.qidian.camera.module.auth.dto.LoginRequest;
import com.qidian.camera.module.auth.dto.LoginResponse;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录响应（包含令牌和用户信息）
     */
    LoginResponse login(LoginRequest request);

    /**
     * 刷新令牌
     *
     * @param refreshToken 刷新令牌
     * @return 新的访问令牌
     */
    String refreshToken(String refreshToken);

    /**
     * 用户登出
     *
     * @param token 访问令牌
     */
    void logout(String token);

    /**
     * 验证令牌
     *
     * @param token 访问令牌
     * @return 是否有效
     */
    boolean validateToken(String token);
}
