# 日志分级系统 - 完成报告

**完成时间**: 2026-04-04 15:20  
**状态**: ✅ 完成

---

## 系统概述

日志分级系统支持动态调整日志级别，按**会话**、**包**、**全局**三个维度管理日志输出，帮助快速定位问题。

---

## 已创建文件

### 核心类（7 个）

| 文件 | 说明 | 行数 |
|------|------|------|
| `LogLevel.java` | 日志级别枚举 | 30 |
| `LogConfigManager.java` | 日志配置管理器 | 150 |
| `SessionLogConfig.java` | 会话日志配置 | 40 |
| `LogConfigHistory.java` | 配置历史记录 | 40 |
| `ApiRequestLogAspect.java` | API 请求日志切面 | 120 |
| `MenuLogAspect.java` | 菜单日志切面 | 100 |
| `LogManagementController.java` | 日志管理 API | 180 |

### 配置文件（2 个）

| 文件 | 说明 |
|------|------|
| `application-logging.yml` | 日志配置 |
| `logback-spring.xml` | Logback 详细配置 |

### 文档和脚本（2 个）

| 文件 | 说明 |
|------|------|
| `docs/LOGGING_SYSTEM_GUIDE.md` | 使用指南 |
| `test-logging.sh` | 测试脚本 |

---

## 核心功能

### 1. 日志级别管理

**5 个级别**:
- ERROR - 只记录错误
- WARN - 错误 + 警告
- INFO - 默认级别
- DEBUG - 详细信息（API 请求/响应）
- TRACE - 所有细节（SQL、参数）

### 2. 多维度配置

**全局级别**:
```bash
curl -X PUT "http://localhost:8080/api/admin/logs/level/global?level=DEBUG"
```

**会话级别**:
```bash
curl -X PUT "http://localhost:8080/api/admin/logs/level/session?userId=1&level=DEBUG&expireMinutes=60"
```

**包级别**:
```bash
curl -X PUT "http://localhost:8080/api/admin/logs/level/package?packageName=com.qidian.camera.module.menu&level=DEBUG"
```

### 3. 自动日志切面

**API 请求日志**:
- 记录所有 API 请求
- 记录请求参数和响应
- 记录执行时间
- 记录异常信息

**菜单操作日志**:
- 专门记录菜单相关操作
- 记录菜单树查询
- 记录结果数量

### 4. 日志文件组织

**按日期**:
```
logs/
├── 2026-04-04/
│   ├── app-2026-04-04.log
│   ├── debug-2026-04-04.log
│   ├── error-2026-04-04.log
│   └── menu-2026-04-04.log
```

**按级别**:
- `app.log` - 主日志
- `debug.log` - DEBUG 日志
- `error.log` - ERROR 日志
- `menu.log` - 菜单日志

---

## 快速使用

### 启用 DEBUG 模式

```bash
# 方式 1: 使用 API
curl -X POST http://localhost:8080/api/admin/logs/debug/enable \
  -H "Authorization: Bearer <admin_token>"

# 方式 2: 使用测试脚本
./test-logging.sh
```

### 查看日志

```bash
# 实时查看菜单日志
tail -f /root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/backend/logs/menu.log

# 查看最近 100 行
tail -100 /root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/backend/logs/menu.log

# 搜索特定用户日志
grep "sessionId=1_" /root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/backend/logs/app.log
```

### 禁用 DEBUG 模式

```bash
curl -X POST http://localhost:8080/api/admin/logs/debug/disable \
  -H "Authorization: Bearer <admin_token>"
```

---

## 排查菜单为空问题

### 步骤

1. **启用 DEBUG 模式**
   ```bash
   curl -X POST http://localhost:8080/api/admin/logs/debug/enable
   ```

2. **为 admin 设置会话日志**
   ```bash
   curl -X PUT "http://localhost:8080/api/admin/logs/level/session?userId=1&level=DEBUG&expireMinutes=60"
   ```

3. **重新登录并访问**
   - admin 用户重新登录
   - 访问首页

4. **查看日志**
   ```bash
   tail -f /root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/backend/logs/menu.log
   ```

### 预期日志

```
2026-04-04 15:20:00.123 [http-nio-8080-exec-1] DEBUG c.q.c.m.a.s.ResourceService [1_20260404152000] - 【菜单操作开始】method=getMenuTree, params=[]
2026-04-04 15:20:00.456 [http-nio-8080-exec-1] DEBUG c.q.c.m.a.s.ResourceService [1_20260404152000] - 【菜单操作成功】method=getMenuTree, duration=333ms, resultCount=30
2026-04-04 15:20:00.789 [http-nio-8080-exec-1] DEBUG c.q.c.m.a.c.ResourceController [1_20260404152000] - 【API 请求成功】requestId=xxx, duration=400ms
```

### 问题分析

| 日志情况 | 可能原因 |
|----------|----------|
| `resultCount=0` | 数据库或查询逻辑问题 |
| `resultCount>0` 但前端空 | 前端过滤或渲染问题 |
| 没有日志 | 日志配置问题或请求未到达 |

---

## API 接口列表

| 方法 | 端点 | 说明 |
|------|------|------|
| PUT | `/api/admin/logs/level/global` | 设置全局日志级别 |
| PUT | `/api/admin/logs/level/session` | 设置会话日志级别 |
| PUT | `/api/admin/logs/level/package` | 设置包日志级别 |
| GET | `/api/admin/logs/config` | 查看当前配置 |
| POST | `/api/admin/logs/reset` | 重置配置 |
| POST | `/api/admin/logs/cleanup` | 清理过期会话 |
| POST | `/api/admin/logs/debug/enable` | 启用 DEBUG 模式 |
| POST | `/api/admin/logs/debug/disable` | 禁用 DEBUG 模式 |

---

## 日志格式

### 控制台格式

```
2026-04-04 15:20:00.123 [http-nio-8080-exec-1] DEBUG c.q.c.m.a.s.ResourceService [1_20260404152000] - 【菜单操作开始】method=getMenuTree
```

### 字段说明

| 字段 | 说明 |
|------|------|
| 时间戳 | 日志记录时间（精确到毫秒） |
| 线程名 | 处理请求的线程 |
| 日志级别 | ERROR/WARN/INFO/DEBUG/TRACE |
| 类名 | 记录日志的类（缩写） |
| sessionId | 会话 ID（userId_timestamp） |
| 消息 | 日志详细内容 |

---

## 下一步

### 立即执行

1. **重启后端服务**（加载新配置）
   ```bash
   # 重启你的后端服务
   ```

2. **运行测试脚本**
   ```bash
   cd /root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/backend
   ./test-logging.sh
   ```

3. **查看日志输出**
   ```bash
   tail -f logs/menu.log
   ```

### 验证菜单问题

1. 启用 DEBUG 模式
2. admin 重新登录
3. 查看菜单日志
4. 根据日志定位问题

---

## 总结

### 已完成

- ✅ 日志级别枚举和配置管理
- ✅ API 请求日志切面
- ✅ 菜单日志切面
- ✅ 日志管理 API
- ✅ Logback 详细配置
- ✅ 使用文档
- ✅ 测试脚本

### 优势

- **动态调整**: 无需重启服务
- **多维度**: 全局/会话/包级别
- **自动记录**: AOP 切面自动拦截
- **分类存储**: 按日期、级别、模块分类
- **快速定位**: 专门的菜单日志

---

**报告人**: AI Assistant  
**报告时间**: 2026-04-04 15:20  
**状态**: ✅ 完成，等待重启验证
