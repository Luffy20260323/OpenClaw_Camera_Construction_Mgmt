package com.qidian.camera.module.auth.controller;

import com.qidian.camera.common.response.Result;
import com.qidian.camera.module.auth.dto.CaptchaRequest;
import com.qidian.camera.module.auth.dto.CaptchaResponse;
import com.qidian.camera.module.auth.dto.LoginRequest;
import com.qidian.camera.module.auth.dto.LoginResponse;
import com.qidian.camera.module.auth.service.AuthService;
import com.qidian.camera.module.auth.service.CaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 认证控制器
 */
@Tag(name = "认证管理", description = "用户登录、登出、令牌刷新等")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final CaptchaService captchaService;
    private final RedisTemplate<String, String> redisTemplate;
    private final JdbcTemplate jdbcTemplate;

    @Operation(summary = "获取系统验证码配置", description = "获取当前系统的验证码类型配置")
    @GetMapping("/captcha/config")
    public Result<Map<String, String>> getCaptchaConfig() {
        Map<String, String> config = new HashMap<>();
        // 从 Redis 缓存读取，如果没有则从数据库读取
        String captchaType = (String) redisTemplate.opsForValue().get("system:config:captcha-type");
        if (captchaType == null) {
            try {
                String sql = "SELECT config_value FROM system_configs WHERE config_key = 'captcha-type'";
                captchaType = jdbcTemplate.queryForObject(sql, String.class);
                redisTemplate.opsForValue().set("system:config:captcha-type", captchaType, 5, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.warn("读取验证码配置失败", e);
                captchaType = "none";
            }
        }
        config.put("captchaType", captchaType);
        return Result.success(config);
    }

    @Operation(summary = "生成图形验证码", description = "生成图形验证码图片")
    @GetMapping(value = "/captcha/image", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] getImageCaptcha(HttpServletResponse response) {
        try {
            CaptchaService.CaptchaData captchaData = captchaService.generateImageCaptcha();
            
            // 设置验证码 ID 到响应头
            response.setHeader("X-Captcha-Id", captchaData.getCaptchaId());
            
            // 将图片转为字节数组
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(captchaData.getImage(), "png", baos);
            baos.flush();
            byte[] imageBytes = baos.toByteArray();
            baos.close();
            
            return imageBytes;
        } catch (Exception e) {
            log.error("生成验证码图片失败", e);
            return new byte[0];
        }
    }

    @Operation(summary = "获取图形验证码", description = "获取图形验证码的 Base64 编码和 ID")
    @GetMapping("/captcha")
    public Result<CaptchaResponse> getCaptcha() {
        try {
            CaptchaService.CaptchaData captchaData = captchaService.generateImageCaptcha();
            
            // 将图片转为 Base64
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(captchaData.getImage(), "png", baos);
            baos.flush();
            byte[] imageBytes = baos.toByteArray();
            baos.close();
            
            String base64Image = "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
            
            CaptchaResponse response = CaptchaResponse.builder()
                    .captchaId(captchaData.getCaptchaId())
                    .imageBase64(base64Image)
                    .expiresIn(300L)
                    .build();
            
            return Result.success(response);
        } catch (Exception e) {
            log.error("获取验证码失败", e);
            return Result.error(500, "获取验证码失败");
        }
    }

    @Operation(summary = "发送手机验证码", description = "向指定手机号发送验证码")
    @PostMapping("/captcha/sms")
    public Result<CaptchaResponse> sendSmsCaptcha(@Valid @RequestBody CaptchaRequest request) {
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            return Result.error(400, "手机号不能为空");
        }
        
        String captchaId = captchaService.generateSmsCaptcha(request.getPhone());
        
        CaptchaResponse response = CaptchaResponse.builder()
                .captchaId(request.getPhone()) // 手机号作为 captchaId
                .sent(true)
                .expiresIn(300L)
                .build();
        
        return Result.success(response);
    }

    @Operation(summary = "用户登录", description = "用户名密码登录，返回访问令牌和刷新令牌")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success(response);
    }

    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
    @PostMapping("/refresh")
    public Result<String> refreshToken(@RequestParam String refreshToken) {
        String newAccessToken = authService.refreshToken(refreshToken);
        return Result.success(newAccessToken);
    }

    @Operation(summary = "用户登出", description = "使当前令牌失效")
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String authorization) {
        String token = extractToken(authorization);
        authService.logout(token);
        return Result.success();
    }

    @Operation(summary = "验证令牌", description = "验证访问令牌是否有效")
    @GetMapping("/validate")
    public Result<Boolean> validateToken(@RequestHeader("Authorization") String authorization) {
        String token = extractToken(authorization);
        boolean isValid = authService.validateToken(token);
        return Result.success(isValid);
    }

    /**
     * 从 Authorization 头中提取令牌
     */
    private String extractToken(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return authorization;
    }
}
