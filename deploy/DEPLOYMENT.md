# 部署指南

本指南介绍如何在新服务器上自动化部署摄像头生命周期管理系统。

## 快速开始

### 方式一：全新服务器（推荐）

在新服务器上执行：

```bash
# 1. 下载并执行初始化脚本
curl -sSL <your-repo-raw-url>/scripts/init-server.sh | sudo bash

# 或手动执行
wget -O init-server.sh <your-repo-raw-url>/scripts/init-server.sh
sudo bash init-server.sh
```

### 方式二：已有项目代码

```bash
# 1. 进入项目目录
cd /root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt

# 2. 完整部署（HTTP）
sudo ./scripts/deploy.sh --all

# 3. 设置 HTTPS
sudo ./scripts/deploy.sh --https -d camera1001.pipecos.cn -e admin@example.com

# 或
sudo ./scripts/setup-https.sh camera1001.pipecos.cn admin@example.com
```

---

## 部署脚本说明

### deploy.sh - 主部署脚本

```bash
# 完整部署（前端 + 后端 + Nginx HTTP）
sudo ./scripts/deploy.sh --all

# 仅部署前端
sudo ./scripts/deploy.sh --frontend

# 仅构建后端
sudo ./scripts/deploy.sh --backend

# 仅部署 Nginx 配置
sudo ./scripts/deploy.sh --nginx

# 设置 HTTPS
sudo ./scripts/deploy.sh --https -d your-domain.com -e your-email@example.com
```

### setup-https.sh - HTTPS 设置脚本

```bash
# 申请并配置 Let's Encrypt 证书
sudo ./scripts/setup-https.sh camera1001.pipecos.cn admin@example.com
```

### init-server.sh - 服务器初始化脚本

```bash
# 新服务器首次使用
curl -sSL <raw-url>/scripts/init-server.sh | sudo bash
```

---

## 配置文件

### Nginx 配置模板

| 文件 | 用途 |
|------|------|
| `deploy/nginx-http.conf` | HTTP 基础配置 |
| `deploy/nginx-https.conf` | HTTPS 完整配置 |
| `deploy/nginx-ssl-params.conf` | SSL 优化参数 |

### 环境变量模板

复制并修改：

```bash
cp deploy/.env.example deploy/.env
```

编辑 `deploy/.env`：

```bash
# 服务器配置
SERVER_IP=1.95.153.137
DOMAIN=camera1001.pipecos.cn

# 数据库配置
DB_HOST=localhost
DB_PORT=5432
DB_NAME=camera_construction_db
DB_USER=postgres
DB_PASSWORD=postgres

# Redis 配置
REDIS_HOST=localhost
REDIS_PORT=6379

# 证书配置
SSL_EMAIL=admin@example.com
SSL_CERT_PATH=/etc/letsencrypt/live/camera1001.pipecos.cn/fullchain.pem
SSL_KEY_PATH=/etc/letsencrypt/live/camera1001.pipecos.cn/privkey.pem
```

---

## 手动部署步骤

### 1. 安装依赖

```bash
# 系统更新
apt update && apt upgrade -y

# 安装基础软件
apt install -y nginx nodejs npm openjdk-17-jdk maven postgresql redis-server git curl

# 安装 Certbot
apt install -y certbot python3-certbot-nginx
```

### 2. 配置数据库

```bash
# 启动 PostgreSQL
systemctl enable postgresql && systemctl start postgresql

# 创建数据库
sudo -u postgres psql -c "ALTER USER postgres PASSWORD 'postgres';"
sudo -u postgres createdb camera_construction_db
```

### 3. 配置 Redis

```bash
systemctl enable redis-server && systemctl start redis-server
```

### 4. 构建前端

```bash
cd /root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/frontend

npm install
npm run build

# 部署到 Nginx 目录
rm -rf /var/www/html/*
cp -r dist/* /var/www/html/
```

### 5. 构建后端

```bash
cd /root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/backend

mvn clean package -DskipTests

# 启动服务
nohup java -jar target/*.jar > /var/log/camera-backend.log 2>&1 &
```

### 6. 配置 Nginx

```bash
# 复制配置
cp deploy/nginx-http.conf /etc/nginx/sites-available/camera-system
ln -sf /etc/nginx/sites-available/camera-system /etc/nginx/sites-enabled/camera-system

# 测试并重载
nginx -t && systemctl reload nginx
```

### 7. 配置 HTTPS

```bash
# 申请证书
certbot --nginx -d camera1001.pipecos.cn --non-interactive --agree-tos --email admin@example.com --redirect

# 切换到 HTTPS 配置
cp deploy/nginx-https.conf /etc/nginx/sites-available/camera-https
ln -sf /etc/nginx/sites-available/camera-https /etc/nginx/sites-enabled/camera-https
rm -f /etc/nginx/sites-enabled/camera-system

nginx -t && systemctl reload nginx
```

---

## 验证部署

### 检查服务状态

```bash
systemctl status nginx
systemctl status postgresql
systemctl status redis-server

# 检查后端
pgrep -f camera-lifecycle-system
```

### 测试访问

```bash
# HTTP
curl -I http://localhost

# HTTPS
curl -kI https://localhost

# API
curl -k https://localhost/api/auth/login
```

### 查看日志

```bash
# Nginx 日志
tail -f /var/log/nginx/camera-access.log
tail -f /var/log/nginx/camera-error.log

# 后端日志
tail -f /var/log/camera-backend.log
```

---

## 证书管理

### 查看证书

```bash
certbot certificates
```

### 手动续期

```bash
certbot renew
```

### 验证自动续期

```bash
certbot renew --dry-run
systemctl list-timers | grep certbot
```

---

## 故障排查

### Nginx 启动失败

```bash
# 测试配置
nginx -t

# 查看错误日志
tail -f /var/log/nginx/error.log
```

### 后端启动失败

```bash
# 查看日志
tail -f /var/log/camera-backend.log

# 检查端口占用
lsof -i :8080
```

### 证书申请失败

```bash
# 检查域名解析
nslookup camera1001.pipecos.cn

# 检查 80 端口是否开放
curl -I http://camera1001.pipecos.cn

# 查看 Certbot 日志
tail -f /var/log/letsencrypt/letsencrypt.log
```

---

## 安全建议

1. **修改默认密码**
   - 数据库密码
   - Redis 密码（如需要）
   - 管理员账号密码

2. **配置防火墙**
   ```bash
   ufw allow 22/tcp    # SSH
   ufw allow 80/tcp    # HTTP
   ufw allow 443/tcp   # HTTPS
   ufw enable
   ```

3. **禁用 root SSH 登录**
   ```bash
   # 编辑 /etc/ssh/sshd_config
   PermitRootLogin no
   systemctl restart sshd
   ```

4. **配置 fail2ban**
   ```bash
   apt install fail2ban
   systemctl enable fail2ban
   ```

5. **定期更新系统**
   ```bash
   apt update && apt upgrade -y
   ```

---

## 自动化部署（CI/CD）

### GitHub Actions 示例

创建 `.github/workflows/deploy.yml`：

```yaml
name: Deploy

on:
  push:
    branches: [ main ]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Deploy to server
        uses: appleboy/ssh-action@master
        with:
          host: ${{ secrets.SERVER_HOST }}
          username: ${{ secrets.SERVER_USER }}
          key: ${{ secrets.SERVER_KEY }}
          script: |
            cd /root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt
            git pull
            sudo ./scripts/deploy.sh --all
```

---

## 常见问题

**Q: 证书申请失败怎么办？**

A: 确保：
- 域名已正确解析到服务器 IP
- 80 端口开放
- Nginx 配置正确

**Q: 如何切换回 HTTP？**

A: 
```bash
cp deploy/nginx-http.conf /etc/nginx/sites-available/camera-system
ln -sf /etc/nginx/sites-available/camera-system /etc/nginx/sites-enabled/camera-system
rm -f /etc/nginx/sites-enabled/camera-https
nginx -t && systemctl reload nginx
```

**Q: 如何备份配置？**

A:
```bash
# 备份 Nginx 配置
tar -czf nginx-backup-$(date +%Y%m%d).tar.gz /etc/nginx/

# 备份数据库
sudo -u postgres pg_dump camera_construction_db > backup-$(date +%Y%m%d).sql
```
