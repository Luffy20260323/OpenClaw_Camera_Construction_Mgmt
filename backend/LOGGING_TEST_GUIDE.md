# 日志系统测试指南

**执行时间**: 2026-04-04 15:30  
**状态**: 等待手动测试

---

## 后端状态

- ✅ 后端服务已重启
- ✅ 日志系统类已加载
- ⚠️ 需要正确密码进行测试

---

## 手动测试步骤

### 方式 1: 使用测试脚本（推荐）

```bash
cd /root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/backend
chmod +x test-logging-manual.sh
./test-logging-manual.sh
# 输入 admin 密码
```

---

### 方式 2: 使用 Postman 或 curl

#### 1. 获取 Token

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"<YOUR_PASSWORD>"}'
```

**保存返回的** `accessToken`

---

#### 2. 启用 DEBUG 模式

```bash
curl -X POST http://localhost:8080/api/admin/logs/debug/enable \
  -H "Authorization: Bearer <accessToken>"
```

**预期响应**:
```json
{
  "code": 200,
  "data": {
    "globalLogLevel": "DEBUG",
    "enabledPackages": [...]
  },
  "message": "DEBUG 模式已启用"
}
```

---

#### 3. 设置会话日志

```bash
curl -X PUT "http://localhost:8080/api/admin/logs/level/session?userId=1&level=DEBUG&expireMinutes=60" \
  -H "Authorization: Bearer <accessToken>"
```

---

#### 4. 测试菜单 API

```bash
curl http://localhost:8080/api/resources/menu-tree \
  -H "Authorization: Bearer <accessToken>"
```

**预期**: 返回菜单树数据

---

#### 5. 查看日志

**方式 A: 查看容器日志**
```bash
docker logs camera-backend 2>&1 | grep -i "menu\|菜单" | tail -20
```

**方式 B: 查看日志文件**
```bash
tail -f /root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/backend/logs/menu.log
```

**预期日志**:
```
【菜单操作开始】method=getMenuTree, params=[]
【菜单操作成功】method=getMenuTree, duration=333ms, resultCount=30
```

---

### 方式 3: 浏览器测试

#### 1. 打开 Swagger UI

访问：`http://localhost:8080/swagger-ui.html`

#### 2. 测试日志接口

找到 **日志管理** 分组：

1. **启用 DEBUG 模式**
   - POST `/api/admin/logs/debug/enable`
   - Execute

2. **设置会话日志**
   - PUT `/api/admin/logs/level/session`
   - userId: 1
   - level: DEBUG
   - expireMinutes: 60
   - Execute

3. **查看配置**
   - GET `/api/admin/logs/config`
   - Execute

#### 3. 测试菜单 API

- GET `/api/resources/menu-tree`
- 添加 Authorization Header
- Execute

#### 4. 查看日志

```bash
docker logs camera-backend 2>&1 | tail -50
```

---

## 预期结果

### 成功标志

1. **API 返回成功**
   ```json
   {"code":200,"message":"DEBUG 模式已启用",...}
   ```

2. **日志文件生成**
   ```
   backend/logs/
   ├── menu.log          ← 新生成
   ├── debug.log         ← 新生成
   └── app.log           ← 有 DEBUG 级别日志
   ```

3. **日志内容**
   ```
   2026-04-04 15:30:00.123 DEBUG 【菜单操作开始】method=getMenuTree
   2026-04-04 15:30:00.456 DEBUG 【菜单操作成功】resultCount=30
   ```

---

## 问题排查

### 问题 1: 日志文件未生成

**检查**:
```bash
# 检查日志目录权限
ls -la /root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/backend/logs/

# 检查容器内日志
docker logs camera-backend 2>&1 | grep -i "logback\|logging"
```

**解决**:
```bash
# 创建日志目录
mkdir -p /root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/backend/logs
chmod 777 /root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/backend/logs

# 重启容器
docker compose restart backend
```

---

### 问题 2: 没有 DEBUG 日志

**检查**:
```bash
# 查看当前配置
curl http://localhost:8080/api/admin/logs/config \
  -H "Authorization: Bearer <token>" | jq '.data.globalLogLevel'
```

**解决**:
```bash
# 重新启用 DEBUG
curl -X POST http://localhost:8080/api/admin/logs/debug/enable \
  -H "Authorization: Bearer <token>"
```

---

### 问题 3: 菜单 API 返回空

**检查日志**:
```bash
docker logs camera-backend 2>&1 | grep -i "menu\|getMenuTree" | tail -20
```

**分析**:
- `resultCount=0` → 数据库问题
- `resultCount>0` → 前端问题
- 无日志 → 请求未到达后端

---

## 下一步

### 立即执行

1. **运行手动测试脚本**
   ```bash
   cd /root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/backend
   ./test-logging-manual.sh
   ```

2. **或手动测试**
   - 使用 Postman/Swagger 测试 API
   - 查看日志输出

3. **排查菜单问题**
   - 根据日志定位菜单为空的原因

---

### 完成后

1. **禁用 DEBUG 模式**（避免日志过多）
   ```bash
   curl -X POST http://localhost:8080/api/admin/logs/debug/disable \
     -H "Authorization: Bearer <token>"
   ```

2. **恢复正常日志级别**
   ```bash
   curl -X PUT "http://localhost:8080/api/admin/logs/level/global?level=INFO" \
     -H "Authorization: Bearer <token>"
   ```

---

**状态**: ⏳ 等待手动测试  
**文档**: `backend/docs/LOGGING_SYSTEM_GUIDE.md`
