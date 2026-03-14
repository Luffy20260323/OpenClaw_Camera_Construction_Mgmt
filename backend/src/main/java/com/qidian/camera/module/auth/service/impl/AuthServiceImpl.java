package com.qidian.camera.module.auth.service.impl;

import com.qidian.camera.common.exception.BusinessException;
import com.qidian.camera.common.exception.ErrorCode;
import com.qidian.camera.module.auth.dto.LoginRequest;
import com.qidian.camera.module.auth.dto.LoginResponse;
import com.qidian.camera.module.auth.service.AuthService;
import com.qidian.camera.module.auth.service.CaptchaService;
import com.qidian.camera.module.auth.service.JwtTokenProvider;
import com.qidian.camera.module.user.entity.User;
import com.qidian.camera.module.role.entity.Role;
import com.qidian.camera.module.user.mapper.UserMapper;
import com.qidian.camera.module.role.mapper.RoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;
    private final CaptchaService captchaService;
    private final JdbcTemplate jdbcTemplate;

    private static final String TOKEN_BLACKLIST_PREFIX = "auth:token:blacklist:";

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String username = request.getUsername();

        // 0. 验证验证码（如果需要）
        String captchaType = getCaptchaType();
        if (!"none".equals(captchaType)) {
            if (request.getCaptcha() == null || request.getCaptcha().trim().isEmpty()) {
                throw new BusinessException(ErrorCode.CAPTCHA_ERROR, "请输入验证码");
            }
            if (request.getCaptchaId() == null || request.getCaptchaId().trim().isEmpty()) {
                throw new BusinessException(ErrorCode.CAPTCHA_ERROR, "验证码 ID 不能为空");
            }
            
            boolean valid = captchaService.validateCaptcha(
                request.getCaptchaId(), 
                request.getCaptcha(), 
                captchaType
            );
            if (!valid) {
                log.warn("验证码错误：username={}, captchaType={}", username, captchaType);
                throw new BusinessException(ErrorCode.CAPTCHA_ERROR);
            }
        }

        // 1. 查询用户
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            log.warn("用户不存在：{}", username);
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 2. 检查用户状态
        if (user.getStatus() == null || user.getStatus() != 1) {
            log.warn("用户已禁用：{}", username);
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }

        // 3. 检查审批状态
        if (user.getApprovalStatus() == null || user.getApprovalStatus() != 1) {
            log.warn("用户未通过审批：{}", username);
            throw new BusinessException(ErrorCode.USER_NOT_APPROVED);
        }

        // 4. 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("密码错误：{}", username);
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 5. 查询用户角色和权限
        List<Role> roles = roleMapper.selectByUserId(user.getId());
        List<String> roleCodes = roles.stream()
                .map(Role::getRoleCode)
                .collect(Collectors.toList());

        List<String> permissions = roleMapper.selectPermissionsByUserId(user.getId());

        // 查询公司信息（用于返回给前端）
        Long companyId = user.getCompanyId();
        Integer companyTypeId = null;
        String companyName = null;
        log.info("用户 {} 的 companyId: {}", username, companyId);
        if (companyId != null) {
            try {
                String companySql = "SELECT id, company_name, type_id FROM companies WHERE id = ?";
                log.info("查询公司信息 SQL: {}, companyId: {}", companySql, companyId);
                Map<String, Object> companyData = jdbcTemplate.queryForMap(companySql, companyId);
                log.info("查询公司信息结果：{}", companyData);
                if (companyData != null) {
                    companyTypeId = ((Number) companyData.get("type_id")).intValue();
                    companyName = (String) companyData.get("company_name");
                    log.info("公司类型 ID: {}, 公司名称：{}", companyTypeId, companyName);
                }
            } catch (Exception e) {
                log.error("查询公司信息失败：{}", e.getMessage(), e);
            }
        }

        // 6. 生成令牌
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(),
                user.getUsername(),
                roleCodes,
                permissions
        );

        String refreshToken = jwtTokenProvider.generateRefreshToken(
                user.getId(),
                user.getUsername()
        );

        // 7. 构建用户信息
        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .companyId(companyId)
                .companyTypeId(companyTypeId)
                .companyName(companyName)
                .roles(roleCodes)
                .permissions(permissions)
                .build();

        log.info("用户登录成功：{}", username);

        // 8. 返回响应
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .userInfo(userInfo)
                .build();
    }

    @Override
    public String refreshToken(String refreshToken) {
        // 1. 验证刷新令牌
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        if (username == null) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        // 2. 检查令牌类型
        // TODO: 添加令牌类型检查

        // 3. 检查令牌是否在黑名单中
        if (Boolean.TRUE.equals(redisTemplate.hasKey(TOKEN_BLACKLIST_PREFIX + refreshToken))) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        }

        // 4. 查询用户
        User user = userMapper.selectByUsername(username);
        if (user == null || user.getStatus() != 1) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 5. 查询角色和权限
        List<Role> roles = roleMapper.selectByUserId(user.getId());
        List<String> roleCodes = roles.stream()
                .map(Role::getRoleCode)
                .collect(Collectors.toList());

        List<String> permissions = roleMapper.selectPermissionsByUserId(user.getId());

        // 6. 生成新的访问令牌
        return jwtTokenProvider.generateAccessToken(
                user.getId(),
                user.getUsername(),
                roleCodes,
                permissions
        );
    }

    @Override
    public void logout(String token) {
        try {
            // 将令牌加入黑名单
            redisTemplate.opsForValue().set(
                    TOKEN_BLACKLIST_PREFIX + token,
                    "logout",
                    24,
                    TimeUnit.HOURS
            );
            log.info("用户登出成功");
        } catch (Exception e) {
            log.error("登出失败：{}", e.getMessage());
        }
    }

    @Override
    public boolean validateToken(String token) {
        try {
            // 检查是否在黑名单中
            if (Boolean.TRUE.equals(redisTemplate.hasKey(TOKEN_BLACKLIST_PREFIX + token))) {
                return false;
            }

            // 验证令牌
            String username = jwtTokenProvider.getUsernameFromToken(token);
            return username != null && !jwtTokenProvider.isTokenExpired(token);
        } catch (Exception e) {
            log.error("验证令牌失败：{}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取系统配置的验证码类型
     *
     * @return 验证码类型：none, image, sms
     */
    private String getCaptchaType() {
        String key = "system:config:captcha-type";
        String type = (String) redisTemplate.opsForValue().get(key);
        
        if (type == null) {
            // 从数据库查询
            try {
                String sql = "SELECT config_value FROM system_configs WHERE config_key = 'captcha-type'";
                type = jdbcTemplate.queryForObject(sql, String.class);
            } catch (Exception e) {
                log.warn("查询验证码配置失败，使用默认值 none", e);
                type = "none";
            }
            // 写入缓存
            redisTemplate.opsForValue().set(key, type, 5, TimeUnit.MINUTES);
        }
        
        return type;
    }
}
