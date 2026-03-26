# TOOLS.md - Local Notes

Skills define _how_ tools work. This file is for _your_ specifics — the stuff that's unique to your setup.

## 飞书配置

### 当前机器人身份

- **机器人名称：** OCT10-开发 1
- **OpenID：** ou_0900e4a9853a4369b7010352d36d8a6c
- **激活状态：** 已激活 (activate_status=2)
- **配置文件：** `/root/.openclaw/agents/main/sessions/bot-identity.json`
- **最后刷新：** 2026-03-19 23:54

### 刷新机器人名称

如果用户在飞书后台修改了机器人名称，发送以下指令刷新：

- **指令：** `/refresh-bot-name` 或 `刷新机器人名称`
- **脚本：** `/root/.openclaw/extensions/feishu/skills/feishu-bot-refresh/refresh-bot.sh`

## 飞书账号配置

| 账号 ID | 名称 | AppID | 机器人名称 |
|--------|------|-------|------------|
| default | 主机器人 | cli_a93e8b1824789cb5 | - |
| dev-1 | 软件团队 1-开发 1 | cli_a93e6ea211b89cd2 | OCT10-开发 1 |
| dev-2 | 软件团队 1-开发 2 | cli_a93e6f4f2b399cc8 | - |
| test-1 | 软件团队 1-测试 1 | cli_a93e6f6269f8dced | - |
| doc-1 | 软件团队 1-文档 1 | cli_a93e6fb827b9dcb6 | - |
| mgr-1 | 软件团队 1-管理 1 | cli_a93e6fca3a381cb1 | OCT10-管理 1 |

当前使用账号：`dev-1` → 对应机器人 **OCT10-开发 1**

---

## 🔐 共享配置（多会话通用）

### GitHub 配置

**敏感信息（Token）** → 存储在环境变量：
- 文件：`~/.openclaw/.env`
- 变量名：`GITHUB_TOKEN`

**非敏感配置** → 存储在工作区文件：
- 文件：`~/.openclaw/workspace/.shared-configs/github.json`
- 内容：用户名、默认仓库、偏好设置

**使用方法**：
1. 读取 `.env` 中的 `GITHUB_TOKEN` 环境变量
2. 读取 `.shared-configs/github.json` 获取用户名和默认配置
3. 所有会话共享这些信息，无需重复询问

---

## What Goes Here

Things like:

- Camera names and locations
- SSH hosts and aliases
- Preferred voices for TTS
- Speaker/room names
- Device nicknames
- Anything environment-specific

## Examples

```markdown
### Cameras

- living-room → Main area, 180° wide angle
- front-door → Entrance, motion-triggered

### SSH

- home-server → 192.168.1.100, user: admin

### TTS

- Preferred voice: "Nova" (warm, slightly British)
- Default speaker: Kitchen HomePod
```

## Why Separate?

Skills are shared. Your setup is yours. Keeping them apart means you can update skills without losing your notes, and share skills without leaking your infrastructure.

---

Add whatever helps you do your job. This is your cheat sheet.
