# MEMORY.md - 长期记忆

_最后更新：2026-03-29_

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

---

## 📝 使用说明

1. **添加新记忆**: 直接编辑此文件
2. **删除旧记忆**: 移除不再相关的内容
3. **保持简洁**: 只保留真正重要的信息
