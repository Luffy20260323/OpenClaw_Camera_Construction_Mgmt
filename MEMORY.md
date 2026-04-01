# MEMORY.md - 长期记忆

_最后更新：2026-03-31 23:09_

---

## ⚠️ 当前项目（最高优先级）

**当前项目 = Camera 项目**
- **项目名称**: OpenClaw_Camera_Construction_Mgmt
- **项目路径**: `/root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt`
- **项目类型**: 摄像机施工管理平台
- **技术栈**: Spring Boot 3.2 + Vue 3 + PostgreSQL + Redis
- **GitHub 仓库**: RichardQidian/OpenClaw_Camera_Construction_Mgmt
- **强调原因**: 避免混淆其他项目（如 SDMGT），所有任务、文档都以 Camera 项目为准

---

## 📋 重要项目

### SDMGT - 软件开发进度管理平台
- **项目描述**: 多项目开发进度管理和协作平台
- **服务器**: 10.8.0.241 (SSH: ubuntu@10.8.0.241)
- **项目路径**: `/home/ubuntu/SDMGT`
- **技术栈**: Spring Boot 3.2 + Vue 3 + PostgreSQL + Redis
- **访问地址**:
  - 前端：http://10.8.0.241:3000
  - 后端 API：http://10.8.0.241:8081
  - API 文档：http://10.8.0.241:8081/swagger-ui.html
- **当前状态**: 🔄 项目初始化阶段，后端/前端基础结构已创建，待部署
- **创建日期**: 2026-03-30
- **当前任务**: 后端 API 实现、前端页面开发

### OpenClaw_Camera_Construction_Mgmt
- 摄像机施工管理项目
- 相关技能：`feishu-bot-refresh`, `feishu-chat-members`
- **项目路径**: `/root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt`

---

## 🤖 飞书机器人配置

### 机器人账号列表

| 账号 ID | 名称 | AppID | 机器人名称 | OpenID | 状态 |
|--------|------|-------|------------|--------|------|
| default | 主机器人 | cli_a93e8b1824789cb5 | Emp10@HWC | ou_bfb219bf456b785c142da81b10fd9b5f | 已激活 |
| dev-1 | 软件团队 1-开发 1 | cli_a93e6ea211b89cd2 | OCT10-开发 1 | ou_0900e4a9853a4369b7010352d36d8a6c | 已激活 |
| dev-2 | 软件团队 1-开发 2 | cli_a93e6f4f2b399cc8 | OCT10-开发 2 | ou_92f5b82758f1622edc38a48e14082dc4 | 已激活 |
| test-1 | 软件团队 1-测试 1 | cli_a93e6f6269f8dced | OCT10-测试 1 | ou_8eb05909fa616aa165bcdcf76cda0e9e | 已激活 |
| doc-1 | 软件团队 1-文档 1 | cli_a93e6fb827b9dcb6 | OCT10-文档 1 | ou_1166101a303d1239118a8c813857f31d | 已激活 |
| mgr-1 | 软件团队 1-管理 1 | cli_a93e6fca3a381cb1 | OCT10-管理 1 | ou_bf8a6a30f3ccad4eba4a8515f35d16bb | 已激活 |

### 当前使用账号
- **默认账号**: `dev-1` → **OCT10-开发 1**

### 配置位置
- 配置文件：`/root/.openclaw/openclaw.json`
- 机器人身份缓存：`/root/.openclaw/agents/main/sessions/bot-identity.json`

---

## 🐙 GitHub 配置

### ⚠️ 账号使用规则（重要）

| 账号 | 用户名 | 角色 | 使用场景 |
|------|--------|------|----------|
| **主账号** | **RichardQidian** | 主用 | **默认使用，如非特别说明一律使用此账号** |
| 备用账号 | Luffy20260323 | 备用 | 仅在主账号不可用时使用 |

### 账号迁移记录

| 项目 | 旧账号 | 新账号 | 迁移日期 | 原因 |
|------|--------|--------|----------|------|
| OpenClaw_Camera_Construction_Mgmt | RichardQidian | Luffy20260323 | 2026-03-23 | 原账号免费空间用完 |

### 可用账号详情

| 账号 | 用户名 | Token 环境变量 | 状态 |
|------|--------|---------------|------|
| 主账号 | RichardQidian | GITHUB_TOKEN | ✅ 正常 |
| 备用账号 | Luffy20260323 | GITHUB_TOKEN_LUFFY | ✅ 正常 |

### 配置位置
- 环境变量：`~/.openclaw/.env`（两个账号的 Token 都在这里）
- 共享配置：`~/.openclaw/workspace/.shared-configs/github.json`
- gh CLI 配置：`~/.config/gh/hosts.yml`

---

## 📋 重要项目

### OpenClaw_Camera_Construction_Mgmt
- 摄像机施工管理项目
- 相关技能：`feishu-bot-refresh`, `feishu-chat-members`
- **项目路径**: `/root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt`

#### ⚠️ Docker 构建优化（2026-03-31 更新）

**重要变更：采用"宿主机编译 + Docker 复制"方式，构建速度提升 80%**

**之前的问题**：
- Docker 构建时每次都要下载 Maven/npm 依赖
- 构建时间长（5-15 分钟），网络依赖严重

**现在的方案**：
- 宿主机编译（利用本地缓存依赖）
- Docker 只复制编译产物（秒级构建）

**标准流程**：
```bash
# 后端构建
cd /root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/backend
mvn clean package -DskipTests
docker build -t camera-backend:latest .

# 前端构建
cd ../frontend
npm install  # 首次需要
npm run build
docker build -t camera-frontend:latest .

# 启动服务
cd ..
docker-compose up -d
```

**或使用构建脚本（推荐）**：
```bash
cd backend && ./build-docker.sh
cd ../frontend && ./build-docker.sh
cd .. && docker-compose up -d
```

**注意事项**：
- ⚠️ **必须先编译再构建 Docker**（否则报错：file not found）
- 浏览器缓存可能导致看不到更新，需要强制刷新 (Ctrl+Shift+R)
- 前端使用 Vue 3 + Vite，构建后会生成静态文件到 nginx 容器
- 所有页面都应使用 `AdminLayout` 组件包裹，保持头部/底部一致

**详见**：`/root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/BUILD.md`

#### 页面框架规范

所有系统页面应使用 `AdminLayout` 组件：

```vue
<template>
  <AdminLayout>
    <!-- 页面内容 -->
  </AdminLayout>
</template>

<script setup>
import AdminLayout from '@/layouts/AdminLayout.vue'
</script>
```

---

## 🔧 系统配置

### Gateway 配置
- 端口：18789
- 绑定：loopback (127.0.0.1)
- 模式：local

### 记忆维护
- 定期审查 `memory/YYYY-MM-DD.md` 文件
- 将重要信息迁移到 MEMORY.md
- 删除过时的记忆

### 会话记忆读取配置（2026-03-31 更新）

**配置位置：** `AGENTS.md`

**读取策略：** 每次会话启动时自动读取 **最近 7 天** 的记忆文件

**示例（今天是 2026-03-31）：**
- `memory/2026-03-31.md` (今天)
- `memory/2026-03-30.md` (昨天)
- `memory/2026-03-29.md` (2 天前)
- `memory/2026-03-28.md` (3 天前)
- `memory/2026-03-27.md` (4 天前)
- `memory/2026-03-26.md` (5 天前)
- `memory/2026-03-25.md` (6 天前)

**目的：** 保持项目记忆连续性，避免会话中断后忘记上下文

**手动读取指令：**
- "读取最近 7 天的记忆"
- "查看昨天的记忆"
- "汇总最近 3 天的工作内容"

**脚本工具：** `/root/.openclaw/workspace/scripts/read-memory.sh`

---

## 💬 用户偏好

### 回复格式要求（2026-04-01 新增）

**每次回复末尾需标注当前使用的模型**，格式如下：

```
---
_Model: glm-5 | Provider: bailian_
```

或简化版：

```
_🤖 glm-5_
```

**原因**：让用户清楚知道当前对话使用的是哪个模型，便于区分不同模型的能力和表现。

---

## 📝 使用说明

1. **添加新记忆**: 直接编辑此文件
2. **删除旧记忆**: 移除不再相关的内容
3. **保持简洁**: 只保留真正重要的信息
