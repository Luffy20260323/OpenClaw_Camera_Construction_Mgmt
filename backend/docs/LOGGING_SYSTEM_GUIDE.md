# 日志分级系统使用指南

**版本**: V1.0  
**创建日期**: 2026-04-04

---

## 系统概述

日志分级系统支持动态调整日志级别，按会话、包、全局三个维度管理日志输出，帮助快速定位问题。

---

## 日志级别

| 级别 | 说明 | 输出内容 |
|------|------|----------|
| **ERROR** | 错误级别 | 只记录错误信息 |
| **WARN** | 警告级别 | 记录错误和警告 |
| **INFO** | 信息级别（默认） | 记录错误、警告和一般信息 |
| **DEBUG** | 调试级别 | 记录详细信息（API 请求/响应） |
| **TRACE** | 追踪级别 | 记录所有细节（SQL、参数等） |

---

## 快速开始

### 1. 启用 DEBUG 模式（推荐排查问题使用）

```bash
# 使用 curl 命令
curl -X POST http://localhost:8080/api/admin/logs/debug/enable \
  -H "Authorization: Bearer <admin_token>"
```

**效果**:
- 全局日志级别设置为 DEBUG
- 关键包（auth, menu, logging）设置为 DEBUG
- 记录详细的 API 请求和菜单操作日志

### 2. 禁用 DEBUG 模式

```bash
curl -X POST http://localhost:8080/api/admin/logs/debug/disable \
  -H "Authorization: Bearer <admin_token>"
```

---

## API 接口

### 设置全局日志级别

```bash
curl -X PUT "http://localhost:8080/api/admin/logs/level/global?level=DEBUG" \
  -H "Authorization: Bearer <admin_token>"
```

**参数**:
- `level`: ERROR, WARN, INFO, DEBUG, TRACE

---

### 设置会话日志级别

```bash
curl -X PUT "http://localhost:8080/api/admin/logs/level/session?userId=1&level=DEBUG&expireMinutes=60" \
  -H "Authorization: Bearer <admin_token>"
```

**参数**:
- `userId`: 用户 ID
- `level`: 日志级别
- `expireMinutes`: 过期时间（分钟），默认 60

**效果**: 为该用户的会话设置 60 分钟的 DEBUG 日志

---

### 设置包日志级别

```bash
curl -X PUT "http://localhost:8080/api/admin/logs/level/package?packageName=com.qidian.camera.module.auth&level=DEBUG" \
  -H "Authorization: Bearer <admin_token>"
```

**参数**:
- `packageName`: 包名
- `level`: 日志级别

---

### 查看当前配置

```bash
curl http://localhost:8080/api/admin/logs/config \
  -H "Authorization: Bearer <admin_token>"
```

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "globalLogLevel": "DEBUG",
    "sessionConfigs": {
      "1_20260404150000": {
        "logLevel": "DEBUG",
        "userId": "1",
        "expireTime": "2026-04-04T16:00:00",
        "remainingMinutes": 55
      }
    },
    "packageConfigs": {
      "com.qidian.camera.module.auth": "DEBUG",
      "com.qidian.camera.module.menu": "DEBUG"
    },
    "todayHistory": {
      "date": "2026-04-04",
      "changes": [
        {
          "time": "2026-04-04T15:00:00",
          "type": "GLOBAL",
          "level": "DEBUG"
        }
      ]
    }
  }
}
```

---

### 重置配置

```bash
curl -X POST http://localhost:8080/api/admin/logs/reset \
  -H "Authorization: Bearer <admin_token>"
```

---

## 日志文件

### 文件位置

```
/root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/backend/logs/
├── app.log                    # 主日志文件
├── debug.log                  # DEBUG 级别日志
├── error.log                  # ERROR 级别日志
├── menu.log                   # 菜单相关日志
└── 2026-04-04/               # 按日期组织的日志
    ├── app-2026-04-04.log
    ├── debug-2026-04-04.log
    ├── error-2026-04-04.log
    └── menu-2026-04-04.log
```

### 查看实时日志

```bash
# 查看主日志
tail -f /root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/backend/logs/app.log

# 查看菜单日志
tail -f /root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/backend/logs/menu.log

# 查看错误日志
tail -f /root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/backend/logs/error.log

# 查看 DEBUG 日志
tail -f /root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/backend/logs/debug.log
```

### 搜索日志

```bash
# 搜索菜单相关日志
grep "菜单" /root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/backend/logs/menu.log

# 搜索特定用户的日志
grep "sessionId=1_" /root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/backend/logs/app.log

# 搜索错误日志
grep "ERROR" /root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/backend/logs/error.log
```

---

## 排查菜单为空问题

### 步骤 1: 启用 DEBUG 模式

```bash
curl -X POST http://localhost:8080/api/admin/logs/debug/enable \
  -H "Authorization: Bearer <admin_token>"
```

### 步骤 2: 为 admin 用户设置会话日志

```bash
curl -X PUT "http://localhost:8080/api/admin/logs/level/session?userId=1&level=DEBUG&expireMinutes=60" \
  -H "Authorization: Bearer <admin_token>"
```

### 步骤 3: 重新登录并操作

1. admin 用户重新登录
2. 访问首页
3. 查看左侧菜单

### 步骤 4: 查看日志

```bash
# 实时查看菜单日志
tail -f /root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/backend/logs/menu.log

# 或查看最近 100 行
tail -100 /root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/backend/logs/menu.log
```

### 预期日志输出

```
2026-04-04 15:10:00.123 [http-nio-8080-exec-1] DEBUG c.q.c.m.a.s.ResourceService [1_20260404151000] - 【菜单操作开始】method=getMenuTree, params=[]
2026-04-04 15:10:00.456 [http-nio-8080-exec-1] DEBUG c.q.c.m.a.s.ResourceService [1_20260404151000] - 【菜单操作成功】method=getMenuTree, duration=333ms, resultCount=30
2026-04-04 15:10:00.789 [http-nio-8080-exec-1] DEBUG c.q.c.m.a.c.ResourceController [1_20260404151000] - 【API 请求成功】requestId=xxx, duration=400ms
```

### 分析问题

- 如果 `resultCount=0`：数据库或查询逻辑问题
- 如果 `resultCount>0` 但前端为空：前端过滤或渲染问题
- 如果没有日志：日志配置问题或请求未到达后端

---

## 日志格式说明

### 日志行格式

```
时间戳 [线程名] 日志级别 类名 [sessionId] - 消息内容
```

**示例**:
```
2026-04-04 15:10:00.123 [http-nio-8080-exec-1] DEBUG c.q.c.m.a.s.ResourceService [1_20260404151000] - 【菜单操作开始】method=getMenuTree
```

### 字段说明

| 字段 | 说明 |
|------|------|
| 时间戳 | 日志记录时间 |
| 线程名 | 处理请求的线程 |
| 日志级别 | ERROR/WARN/INFO/DEBUG/TRACE |
| 类名 | 记录日志的类（缩写） |
| sessionId | 会话 ID（userId_timestamp） |
| 消息内容 | 日志详细信息 |

---

## 最佳实践

### 1. 日常运行

```bash
# 保持 INFO 级别
curl -X PUT "http://localhost:8080/api/admin/logs/level/global?level=INFO"
```

### 2. 排查问题

```bash
# 启用 DEBUG 模式
curl -X POST http://localhost:8080/api/admin/logs/debug/enable

# 问题排查完成后禁用
curl -X POST http://localhost:8080/api/admin/logs/debug/disable
```

### 3. 特定用户调试

```bash
# 为特定用户设置 30 分钟 DEBUG
curl -X PUT "http://localhost:8080/api/admin/logs/level/session?userId=1&level=DEBUG&expireMinutes=30"
```

### 4. 特定模块调试

```bash
# 只设置菜单模块为 DEBUG
curl -X PUT "http://localhost:8080/api/admin/logs/level/package?packageName=com.qidian.camera.module.menu&level=DEBUG"
```

---

## 注意事项

1. **DEBUG 模式会产生大量日志**，建议仅在排查问题时使用
2. **会话日志会自动过期**，默认 60 分钟
3. **日志文件会自动轮转**，保留最近 30 天
4. **ERROR 日志单独记录**，便于快速定位错误

---

**文档版本**: V1.0  
**最后更新**: 2026-04-04
