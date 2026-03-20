# 🚀 推送到 GitHub 最终指南

**当前状态：** ✅ 本地 Git 仓库已准备就绪  
**GitHub 用户名：** RichardQidian  
**提交哈希：** `c37498a`

---

## ⚠️ 推送失败原因

GitHub 上还没有 `RichardQidian/openclaw` 仓库，需要先 Fork 官方仓库。

---

## 📋 完整步骤（5 分钟）

### 步骤 1: Fork 官方仓库（1 分钟）

1. **访问：** https://github.com/openclaw/openclaw
2. **点击右上角 "Fork" 按钮**
3. 等待 Fork 完成
4. 确认看到 `RichardQidian/openclaw`

### 步骤 2: 推送到你的 Fork（2 分钟）

本地仓库已准备好，在 `/tmp/openclaw-github/openclaw/`

**方式 A：使用 HTTPS（推荐）**

```bash
cd /tmp/openclaw-github/openclaw

# 更新远程仓库地址（使用你的 GitHub 用户名）
git remote set-url origin https://github.com/RichardQidian/openclaw.git

# 推送
git push -u origin main
```

**方式 B：使用 SSH**

```bash
cd /tmp/openclaw-github/openclaw

# 更新远程仓库地址
git remote set-url origin git@github.com:RichardQidian/openclaw.git

# 推送（需要配置 SSH key）
git push -u origin main
```

### 步骤 3: 创建 Pull Request（2 分钟）

1. **访问：** https://github.com/openclaw/openclaw/pulls
2. **点击：** "New pull request"
3. **选择：**
   - base repository: `openclaw/openclaw`
   - base branch: `main`
   - head repository: `RichardQidian/openclaw`
   - compare branch: `main`
4. **填写 PR 信息：**
   - 标题：`feat(feishu): add bot-refresh skill for identity refresh`
   - 描述：复制下方 PR 模板
5. **点击：** "Create pull request"

---

## 📝 PR 描述模板

```markdown
## 📝 Description

This PR adds a new skill `feishu-bot-refresh` to the Feishu extension.

## ✨ Features

- User-triggered refresh via `/refresh-bot-name` command
- Multi-account support (dev-1, dev-2, test-1, etc.)
- Shell script for programmatic use
- Identity caching in bot-identity.json

## 📁 Files Added

- `extensions/feishu/skills/feishu-bot-refresh/SKILL.md`
- `extensions/feishu/skills/feishu-bot-refresh/README.md`
- `extensions/feishu/skills/feishu-bot-refresh/refresh-bot.sh`

## 🧪 Testing

Tested successfully with:
- ✅ dev-1 (软件团队 1-开发 1)
- ✅ mgr-1 (软件团队 1-管理 1)

## 📚 Documentation

Complete documentation included following AgentSkills specification.

## 🔐 Security

- Uses existing app credentials
- No external dependencies
- Only calls official Feishu API
```

---

## 🔍 验证推送

推送成功后，访问：
- https://github.com/RichardQidian/openclaw

应该能看到：
- ✅ 仓库存在
- ✅ 最新提交 `c37498a`
- ✅ 文件 `extensions/feishu/skills/feishu-bot-refresh/`

---

## ❓ 常见问题

### Q: Fork 失败？
A: 确保已登录 GitHub 账号

### Q: 推送时提示认证？
A: 使用 HTTPS 需要 GitHub token，或使用 SSH key

### Q: 找不到 Pull Request 按钮？
A: 确保先 Fork 了仓库，然后访问 https://github.com/openclaw/openclaw/pulls

---

## 📞 需要帮助？

**GitHub 文档：**
- Fork 教程：https://docs.github.com/en/get-started/quickstart/fork-a-repo
- PR 指南：https://docs.github.com/en/pull-requests

**OpenClaw 社区：**
- Discord: https://discord.com/invite/clawd
- 文档：https://docs.openclaw.ai

---

## ✅ 检查清单

完成前确认：

- [ ] 已访问 https://github.com/openclaw/openclaw
- [ ] 已点击 Fork 按钮
- [ ] 看到 RichardQidian/openclaw 仓库
- [ ] 执行了 git push 命令
- [ ] 推送成功（无错误）
- [ ] 访问 GitHub 能看到提交
- [ ] 创建了 Pull Request

---

**准备就绪！开始执行吧！** 🚀

**当前时间：** 2026-03-20 00:25 CST  
**本地提交：** c37498a  
**状态：** ⏳ 等待推送到 GitHub
