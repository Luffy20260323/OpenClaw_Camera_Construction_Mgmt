# 安全配置指南

本文档介绍如何安全地部署和配置摄像头生命周期管理系统。

---

## 🔐 必须修改的敏感配置

### 1. 数据库密码

**位置：** `.env` 文件中的 `DB_PASSWORD`

**风险：** 默认密码 `postgres` 是公开已知的，攻击者可以轻易尝试

**建议：**
```bash
# 生成强密码（16 位随机字符串）
openssl rand -base64 16

# 示例：DB_PASSWORD=x7K#mP9$vL2@nQ5w
```

**要求：**
- 至少 12 位字符
- 包含大小写字母、数字、特殊字符
- 不要使用常见单词或短语

---

### 2. JWT 密钥

**位置：** `.env` 文件中的 `JWT_SECRET`

**风险：** JWT 密钥用于签名用户令牌，泄露后可伪造任意用户身份

**建议：**
```bash
# 生成强随机密钥（32 位）
openssl rand -base64 32

# 示例：JWT_SECRET=a8F3kL9mN2pQ5rT7vX0zB4cE6gH8jK1l
```

**要求：**
- 至少 32 位随机字符
- 使用加密安全的随机数生成器
- 定期轮换（建议每 90 天）

---

### 3. MinIO 访问密钥

**位置：** `.env` 文件中的 `MINIO_ACCESS_KEY` 和 `MINIO_SECRET_KEY`

**风险：** 默认密钥 `minioadmin` 是公开已知的

**建议：**
```bash
# 生成 Access Key（20 位）
openssl rand -hex 10

# 生成 Secret Key（40 位）
openssl rand -hex 20
```

**要求：**
- Access Key: 至少 20 位字母数字
- Secret Key: 至少 40 位随机字符

---

### 4. Redis 密码

**位置：** `.env` 文件中的 `REDIS_PASSWORD`

**风险：** Redis 默认无密码，攻击者可直接访问缓存数据

**建议：**
```bash
# 生成强密码
openssl rand -base64 16
```

**配置：**
```bash
REDIS_PASSWORD=YourSecureRedisPassword123!
```

---

## 🔒 文件权限设置

### 生产环境权限

```bash
# .env 文件权限（仅所有者可读写）
chmod 600 .env
chown root:root .env

# 脚本文件权限（可执行）
chmod +x scripts/*.sh

# 日志目录权限
chmod 750 /var/log/camera-system
chown www-data:www-data /var/log/camera-system
```

---

## 🛡️ 网络安全

### 防火墙配置

```bash
# 仅开放必要端口
ufw allow 22/tcp    # SSH
ufw allow 80/tcp    # HTTP（可选，如使用 HTTPS）
ufw allow 443/tcp   # HTTPS
ufw deny 5432/tcp   # PostgreSQL（仅本地访问）
ufw deny 6379/tcp   # Redis（仅本地访问）
ufw deny 9000/tcp   # MinIO（仅本地访问）
ufw enable
```

### Docker 网络隔离

使用 `docker-compose.yml` 中的内部网络，确保数据库和缓存不暴露到公网：

```yaml
services:
  postgres:
    # 注释掉 ports 映射，仅内部访问
    # ports:
    #   - "5432:5432"
    networks:
      - camera-network
```

---

## 📝 HTTPS 配置

### 使用 Let's Encrypt 免费证书

```bash
# 安装 Certbot
apt install certbot python3-certbot-nginx

# 申请证书
certbot --nginx -d your-domain.com

# 自动续期（已配置定时器）
certbot renew --dry-run
```

### Nginx HTTPS 配置

参考 `deploy/nginx-https.conf`，确保：
- 启用 TLS 1.2 和 1.3
- 禁用弱加密套件
- 启用 HSTS

---

## 🔍 安全审计

### 定期检查项目

1. **检查默认密码是否修改**
   ```bash
   grep -r "postgres" .env
   grep -r "minioadmin" .env
   grep -r "your-super-secret" .env
   ```

2. **检查文件权限**
   ```bash
   ls -la .env
   ls -la deploy/
   ```

3. **检查开放端口**
   ```bash
   netstat -tlnp
   ufw status
   ```

4. **查看访问日志**
   ```bash
   tail -f /var/log/nginx/access.log
   tail -f /var/log/camera-system/application.log
   ```

---

## 🚨 应急响应

### 发现安全漏洞时

1. **立即修改所有密钥和密码**
2. **撤销所有 JWT 令牌**（重启后端服务）
3. **检查访问日志，确认是否有未授权访问**
4. **更新到最新安全版本**

### 密钥轮换流程

1. 生成新的 JWT 密钥
2. 更新 `.env` 文件
3. 重启后端服务（所有现有令牌失效）
4. 通知用户重新登录

---

## ✅ 安全检查清单

部署前请确认：

- [ ] 数据库密码已修改（不是 `postgres`）
- [ ] JWT 密钥已生成（不是默认值）
- [ ] MinIO 密钥已修改（不是 `minioadmin`）
- [ ] Redis 密码已设置（生产环境）
- [ ] `.env` 文件权限设置为 600
- [ ] 防火墙已配置（仅开放必要端口）
- [ ] HTTPS 已启用（生产环境）
- [ ] 默认管理员密码已修改
- [ ] 日志记录已启用
- [ ] 定期备份已配置

---

## 📚 参考资源

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [Docker 安全最佳实践](https://docs.docker.com/engine/security/)
- [Spring Boot 安全指南](https://spring.io/guides/gs/securing-web/)
- [Let's Encrypt 文档](https://letsencrypt.org/docs/)

---

**安全无小事，请认真对待每一项配置！**
