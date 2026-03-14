package com.qidian.camera.module.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 频率限制常量
 */
class RateLimitConstants {
    public static final String IMAGE_CAPTCHA_LIMIT_PREFIX = "ratelimit:captcha:image:";
    public static final String SMS_CAPTCHA_LIMIT_PREFIX = "ratelimit:captcha:sms:";
    public static final int IMAGE_CAPTCHA_MAX_PER_HOUR = 10; // 每小时最多 10 次
    public static final int SMS_CAPTCHA_MAX_PER_HOUR = 5;    // 每小时最多 5 次
    public static final int SMS_CAPTCHA_INTERVAL_SECONDS = 60; // 发送间隔 60 秒
}

/**
 * 验证码服务
 */
@Slf4j
@Service
public class CaptchaService {

    private final RedisTemplate<String, String> redisTemplate;

    public CaptchaService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private static final String CAPTCHA_PREFIX = "captcha:image:";
    private static final String SMS_CAPTCHA_PREFIX = "captcha:sms:";
    private static final int EXPIRE_MINUTES = 5;

    /**
     * 生成图形验证码
     *
     * @return 验证码 ID 和图片
     */
    public CaptchaData generateImageCaptcha() {
        // 频率限制检查（按 IP，实际应该从请求中获取 IP）
        String rateLimitKey = RateLimitConstants.IMAGE_CAPTCHA_LIMIT_PREFIX + "global";
        Long count = redisTemplate.opsForValue().increment(rateLimitKey);
        if (count == 1) {
            redisTemplate.expire(rateLimitKey, 1, TimeUnit.HOURS);
        }
        
        if (count > RateLimitConstants.IMAGE_CAPTCHA_MAX_PER_HOUR) {
            throw new RuntimeException("获取验证码过于频繁，请稍后再试");
        }

        int length = 4; // TODO: 从系统配置读取
        String captchaCode = generateRandomCode(length);
        String captchaId = UUID.randomUUID().toString().replace("-", "");

        // 生成验证码图片
        BufferedImage image = createBufferedImage(captchaCode);

        // 存储到 Redis
        String key = CAPTCHA_PREFIX + captchaId;
        redisTemplate.opsForValue().set(key, captchaCode.toLowerCase(), EXPIRE_MINUTES, TimeUnit.MINUTES);

        log.info("生成图形验证码：{}, 验证码：{}", captchaId, captchaCode);

        return new CaptchaData(captchaId, image, captchaCode);
    }

    /**
     * 生成手机验证码
     *
     * @param phone 手机号
     * @return 验证码 ID
     */
    public String generateSmsCaptcha(String phone) {
        // 频率限制检查
        String rateLimitKey = RateLimitConstants.SMS_CAPTCHA_LIMIT_PREFIX + phone;
        String intervalKey = RateLimitConstants.SMS_CAPTCHA_LIMIT_PREFIX + "interval:" + phone;
        
        // 检查发送间隔
        Long lastSendTime = redisTemplate.opsForValue().get(intervalKey) != null ? 
            Long.parseLong(redisTemplate.opsForValue().get(intervalKey)) : 0;
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSendTime < RateLimitConstants.SMS_CAPTCHA_INTERVAL_SECONDS * 1000L) {
            long waitSeconds = RateLimitConstants.SMS_CAPTCHA_INTERVAL_SECONDS - (currentTime - lastSendTime) / 1000;
            throw new RuntimeException("请等待" + waitSeconds + "秒后再发送");
        }
        
        // 检查每小时发送次数
        String hourLimitKey = RateLimitConstants.SMS_CAPTCHA_LIMIT_PREFIX + "hour:" + phone;
        Long count = redisTemplate.opsForValue().increment(hourLimitKey);
        if (count == 1) {
            redisTemplate.expire(hourLimitKey, 1, TimeUnit.HOURS);
        }
        
        if (count > RateLimitConstants.SMS_CAPTCHA_MAX_PER_HOUR) {
            throw new RuntimeException("发送次数已达上限，请 1 小时后再试");
        }

        int length = 6; // TODO: 从系统配置读取
        String captchaCode = generateRandomCode(length);

        // 存储到 Redis
        String key = SMS_CAPTCHA_PREFIX + phone;
        redisTemplate.opsForValue().set(key, captchaCode, EXPIRE_MINUTES, TimeUnit.MINUTES);
        
        // 记录发送时间
        redisTemplate.opsForValue().set(intervalKey, String.valueOf(currentTime), 
            RateLimitConstants.SMS_CAPTCHA_INTERVAL_SECONDS, TimeUnit.SECONDS);

        log.info("生成手机验证码：手机号={}, 验证码={}", phone, captchaCode);

        // TODO: 实际项目中这里需要调用短信服务商 API 发送短信
        // 开发环境直接在日志中输出验证码

        return phone; // 返回手机号作为 captchaId
    }

    /**
     * 验证验证码
     *
     * @param captchaId 验证码 ID（图形验证码）或手机号（手机验证码）
     * @param code      用户输入的验证码
     * @param type      验证码类型：image 或 sms
     * @return 是否验证通过
     */
    public boolean validateCaptcha(String captchaId, String code, String type) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }

        String key;
        if ("sms".equals(type)) {
            key = SMS_CAPTCHA_PREFIX + captchaId; // captchaId 是手机号
        } else {
            key = CAPTCHA_PREFIX + captchaId;
        }

        String storedCode = redisTemplate.opsForValue().get(key);
        if (storedCode == null) {
            log.warn("验证码已过期或不存在：{}", captchaId);
            return false;
        }

        boolean isValid = storedCode.equalsIgnoreCase(code.trim());
        if (isValid) {
            // 验证成功后删除验证码
            redisTemplate.delete(key);
        }

        log.info("验证码验证结果：{}, 输入：{}, 存储：{}", isValid, code, storedCode);
        return isValid;
    }

    /**
     * 删除验证码
     *
     * @param captchaId 验证码 ID
     * @param type      验证码类型
     */
    public void removeCaptcha(String captchaId, String type) {
        String key;
        if ("sms".equals(type)) {
            key = SMS_CAPTCHA_PREFIX + captchaId;
        } else {
            key = CAPTCHA_PREFIX + captchaId;
        }
        redisTemplate.delete(key);
    }

    /**
     * 生成随机验证码
     *
     * @param length 长度
     * @return 随机码
     */
    private String generateRandomCode(int length) {
        String chars = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"; // 排除易混淆字符
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * 创建验证码图片
     *
     * @param code 验证码文本
     * @return BufferedImage
     */
    private BufferedImage createBufferedImage(String code) {
        int width = 120;
        int height = 40;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 设置背景色
        g.setColor(getRandomColor(240, 250));
        g.fillRect(0, 0, width, height);

        // 设置字体
        g.setFont(new Font("Arial", Font.BOLD, 24));

        // 绘制干扰线
        for (int i = 0; i < 5; i++) {
            g.setColor(getRandomColor(160, 220));
            int x1 = randomInt(width);
            int y1 = randomInt(height);
            int x2 = randomInt(width);
            int y2 = randomInt(height);
            g.drawLine(x1, y1, x2, y2);
        }

        // 绘制验证码文本
        char[] chars = code.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            g.setColor(new Color(20 + randomInt(100), 20 + randomInt(100), 20 + randomInt(100)));
            g.drawString(String.valueOf(chars[i]), 25 * i + 10, 25 + randomInt(10));
        }

        // 绘制干扰点
        for (int i = 0; i < 100; i++) {
            g.setColor(getRandomColor(160, 220));
            int x = randomInt(width);
            int y = randomInt(height);
            g.drawRect(x, y, 1, 1);
        }

        g.dispose();
        return image;
    }

    /**
     * 获取随机颜色
     */
    private Color getRandomColor(int min, int max) {
        return new Color(
                min + randomInt(max - min),
                min + randomInt(max - min),
                min + randomInt(max - min)
        );
    }

    /**
     * 获取随机整数
     */
    private int randomInt(int bound) {
        return new Random().nextInt(bound);
    }

    /**
     * 验证码数据内部类
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class CaptchaData {
        private String captchaId;
        private BufferedImage image;
        private String code;
    }
}
