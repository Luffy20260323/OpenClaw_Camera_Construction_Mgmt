# Nginx 配置模板

本目录包含 Nginx 配置模板，用于自动化部署。

## 文件说明

| 文件 | 用途 |
|------|------|
| `nginx-http.conf` | HTTP 基础配置（用于 Let's Encrypt 证书申请前） |
| `nginx-https.conf` | HTTPS 完整配置（证书申请后使用） |
| `nginx-ssl.conf` | SSL 优化配置片段（可复用） |

## 自动化部署流程

### 首次部署（HTTP）

```bash
# 1. 复制 HTTP 配置
sudo cp deploy/nginx-http.conf /etc/nginx/sites-available/camera-system
sudo ln -sf /etc/nginx/sites-available/camera-system /etc/nginx/sites-enabled/camera-system

# 2. 重启 Nginx
sudo nginx -t && sudo systemctl restart nginx
```

### 启用 HTTPS

```bash
# 1. 安装 Certbot
sudo apt update && sudo apt install certbot python3-certbot-nginx -y

# 2. 申请证书
sudo certbot --nginx -d camera1001.pipecos.cn --non-interactive --agree-tos --email YOUR_EMAIL --redirect

# 3. 或使用自动化脚本
./scripts/setup-https.sh camera1001.pipecos.cn YOUR_EMAIL
```

### 手动配置 HTTPS

```bash
# 1. 复制 HTTPS 配置
sudo cp deploy/nginx-https.conf /etc/nginx/sites-available/camera-https

# 2. 修改证书路径（如需要）
# 编辑 /etc/nginx/sites-available/camera-https
# 修改 ssl_certificate 和 ssl_certificate_key 路径

# 3. 启用配置
sudo ln -sf /etc/nginx/sites-available/camera-https /etc/nginx/sites-enabled/camera-https
sudo rm -f /etc/nginx/sites-enabled/camera-system

# 4. 重启 Nginx
sudo nginx -t && sudo systemctl reload nginx
```

## 证书管理

### 查看证书状态

```bash
sudo certbot certificates
```

### 手动续期

```bash
sudo certbot renew
```

### 自动续期验证

```bash
sudo certbot renew --dry-run
sudo systemctl list-timers | grep certbot
```

## 安全加固建议

1. **防火墙配置**
   ```bash
   sudo ufw allow 80/tcp    # HTTP
   sudo ufw allow 443/tcp   # HTTPS
   sudo ufw allow 22/tcp    # SSH
   sudo ufw enable
   ```

2. **禁用不安全的 SSL 协议**
   - 已配置 TLSv1.2 和 TLSv1.3
   - 已禁用 SSLv3、TLSv1.0、TLSv1.1

3. **隐藏 Nginx 版本号**
   ```nginx
   server_tokens off;
   ```

4. **配置 HSTS**（可选，生产环境建议启用）
   ```nginx
   add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
   ```

## 故障排查

### 检查 Nginx 配置

```bash
sudo nginx -t
```

### 查看 Nginx 日志

```bash
sudo tail -f /var/log/nginx/access.log
sudo tail -f /var/log/nginx/error.log
```

### 查看 Certbot 日志

```bash
sudo tail -f /var/log/letsencrypt/letsencrypt.log
```

### 测试 HTTPS 连接

```bash
curl -I https://camera1001.pipecos.cn
echo | openssl s_client -connect camera1001.pipecos.cn:443 2>/dev/null | openssl x509 -noout -dates
```
