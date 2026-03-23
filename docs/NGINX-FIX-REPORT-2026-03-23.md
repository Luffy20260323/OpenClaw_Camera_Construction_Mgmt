# 🔧 Nginx 前端问题修复报告

**修复时间:** 2026-03-23 22:47 GMT+8  
**问题类型:** Nginx 配置错误  
**影响范围:** 前端页面和 API 代理  
**修复状态:** ✅ 已完成

---

## 📊 问题诊断

### 问题现象

1. **前端页面返回 500 错误**
   ```bash
   curl http://localhost:80/
   # 返回：500 Internal Server Error
   ```

2. **API 代理返回 502 错误**
   ```bash
   curl http://localhost:80/api/auth/login
   # 返回：502 Bad Gateway
   ```

---

### 根本原因

#### 问题 1: 静态文件路径错误

**Nginx 配置:**
```nginx
location / {
    root /var/www/html;  # ❌ 配置路径
    ...
}
```

**实际情况:**
- 容器内文件路径：`/usr/share/nginx/html/`
- 配置期望路径：`/var/www/html/` (不存在)

**错误日志:**
```
rewrite or internal redirection cycle while internally redirecting to "/index.html"
```

---

#### 问题 2: 后端代理地址错误

**Nginx 配置:**
```nginx
location /api {
    proxy_pass http://localhost:8080;  # ❌ 在容器内无法解析
    ...
}
```

**实际情况:**
- 在 Docker 容器内，`localhost` 指向容器本身
- 应该使用 Docker 服务名称：`camera-backend:8080`

**错误日志:**
```
502 Bad Gateway - upstream connection failed
```

---

## ✅ 修复方案

### 修复 1: 创建静态文件目录

```bash
# 在容器内创建目录
docker exec camera-frontend mkdir -p /var/www/html

# 复制静态文件
docker exec camera-frontend cp -r /usr/share/nginx/html/* /var/www/html/
```

**验证:**
```bash
docker exec camera-frontend ls -la /var/www/html/
# 输出：index.html, assets/, vite.svg
```

---

### 修复 2: 更新 Nginx 代理配置

**修改文件:** `deploy/nginx-http.conf`

**修改前:**
```nginx
location /api {
    proxy_pass http://localhost:8080;
}

location /doc.html {
    proxy_pass http://localhost:8080/doc.html;
}

location /webjars/ {
    proxy_pass http://localhost:8080/webjars/;
}
```

**修改后:**
```nginx
location /api {
    proxy_pass http://camera-backend:8080;
}

location /doc.html {
    proxy_pass http://camera-backend:8080/doc.html;
}

location /webjars/ {
    proxy_pass http://camera-backend:8080/webjars/;
}
```

---

### 修复 3: 重启容器应用配置

```bash
# 重启前端容器
docker restart camera-frontend

# 验证配置
docker exec camera-frontend nginx -t
# 输出：syntax is ok, test is successful
```

---

## 🎯 验证结果

### 前端页面 ✅

```bash
curl http://localhost:80/
```

**返回:**
```html
<!doctype html>
<html lang="zh-CN">
  <head>
    <title>视频监控点位施工项目管理系统</title>
    ...
  </head>
  <body>
    <div id="app"></div>
  </body>
</html>
```

---

### API 代理 ✅

```bash
curl http://localhost:80/api/auth/login \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin@2026"}'
```

**返回:**
```json
{
  "code": 3001,
  "message": "请输入验证码",
  "timestamp": 1774277242757,
  "success": false
}
```

---

### 容器状态 ✅

```bash
docker ps --filter "name=camera"
```

| 容器 | 状态 | 说明 |
|------|------|------|
| camera-frontend | Up (healthy) | ✅ 正常运行 |
| camera-backend | Up | ✅ 正常运行 |
| camera-postgres | Up (healthy) | ✅ 正常运行 |
| camera-redis | Up (healthy) | ✅ 正常运行 |

---

## 📋 修复步骤总结

### 临时修复（已完成）

1. ✅ 在容器内创建 `/var/www/html` 目录
2. ✅ 复制静态文件到正确位置
3. ✅ 修改 `deploy/nginx-http.conf` 配置
4. ✅ 重启前端容器

### 永久修复（建议）

1. ⏳ 更新前端 Dockerfile，使用正确的文件路径
2. ⏳ 在 docker-compose.yml 中配置正确的挂载点
3. ⏳ 添加健康检查配置

---

## 🔍 技术细节

### Docker 网络

```bash
docker network inspect camera-network
```

**容器 IP:**
- camera-backend: 172.18.0.6
- camera-frontend: 172.18.0.2
- camera-postgres: 172.18.0.5
- camera-redis: 172.18.0.4

**网络连通性:**
```bash
docker exec camera-frontend ping -c 1 camera-backend
# 输出：64 bytes from 172.18.0.6: seq=0 ttl=64 time=0.150 ms
```

---

### Nginx 配置最佳实践

**在 Docker 环境中:**
- ✅ 使用 Docker 服务名称（如 `camera-backend`）
- ❌ 避免使用 `localhost` 或 `127.0.0.1`

**静态文件路径:**
- ✅ 与 Dockerfile 中的路径一致
- ✅ 或使用 volume 挂载

---

## 📄 修改的文件

| 文件 | 修改内容 | 状态 |
|------|---------|------|
| `deploy/nginx-http.conf` | 更新 proxy_pass 地址 | ✅ 已修复 |
| `/var/www/html/` (容器内) | 创建目录并复制文件 | ✅ 已修复 |

---

## 🎯 测试建议

### 功能测试

```bash
# 1. 测试前端页面
curl http://localhost:80/

# 2. 测试 API 代理
curl http://localhost:80/api/auth/login

# 3. 测试 API 文档
curl http://localhost:80/doc.html

# 4. 测试静态资源
curl http://localhost:80/assets/index-Ckxpu4A4.js
```

### 性能测试

```bash
# 使用 ab 或 wrk 进行压力测试
ab -n 1000 -c 10 http://localhost:80/
```

---

## 🚨 安全建议

### 当前发现的问题

1. **大量恶意扫描请求**
   ```
   GET /.git/config
   GET /.env.backup
   GET /vendor/phpunit/phpunit/src/Util/PHP/eval-stdin.php
   POST /?d+allow_url_include=1+d+auto_prepend_file=php://input
   ```

2. **建议措施:**
   - 配置 fail2ban 封禁恶意 IP
   - 添加 WAF 规则
   - 限制请求频率
   - 隐藏服务器版本号（已配置）

---

## 📊 修复前后对比

| 项目 | 修复前 | 修复后 |
|------|--------|--------|
| 前端页面 | ❌ 500 Error | ✅ 正常访问 |
| API 代理 | ❌ 502 Error | ✅ 正常代理 |
| 静态文件 | ❌ 路径错误 | ✅ 路径正确 |
| 后端连接 | ❌ localhost | ✅ camera-backend |
| 容器健康 | ⚠️ Unhealthy | ✅ Healthy |

---

## 📝 经验总结

### 问题根源

1. **Docker 镜像构建时路径不一致**
   - Dockerfile 中使用 `/usr/share/nginx/html`
   - Nginx 配置使用 `/var/www/html`

2. **Docker 网络理解不足**
   - 容器内 `localhost` 不是宿主机
   - 应使用 Docker 服务名称

### 改进建议

1. **统一文件路径**
   - 在 Dockerfile 和 Nginx 配置中使用相同路径
   - 或者使用 volume 挂载

2. **添加健康检查**
   ```yaml
   healthcheck:
     test: ["CMD", "curl", "-f", "http://localhost:80/"]
     interval: 30s
     timeout: 10s
     retries: 3
   ```

3. **完善监控**
   - 添加日志分析
   - 配置告警通知

---

**修复人:** Luffy20260323 / OCT10-管理 1  
**修复完成时间:** 2026-03-23 22:47 GMT+8  
**下次检查时间:** 2026-03-24
