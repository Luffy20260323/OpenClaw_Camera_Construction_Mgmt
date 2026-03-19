# 🚀 OpenClaw PR 提交指南

## ✅ 已完成

- [x] 技能文件创建完成
- [x] 技能已安装到 `/usr/lib/node_modules/openclaw/extensions/feishu/skills/feishu-bot-refresh/`
- [x] 功能测试通过（dev-1, mgr-1 账号）
- [x] 文档完整（SKILL.md, README.md, refresh-bot.sh）

---

## 📋 下一步：提交 PR 到 GitHub

### 步骤 1: Fork OpenClaw 仓库（30 秒）

1. 访问：https://github.com/openclaw/openclaw
2. 点击右上角 **"Fork"** 按钮
3. 等待 Fork 完成

### 步骤 2: 克隆你的 Fork（1 分钟）

```bash
# 替换 <your-username> 为你的 GitHub 用户名
git clone https://github.com/<your-username>/openclaw.git
cd openclaw
```

### 步骤 3: 创建功能分支（30 秒）

```bash
git checkout -b feat/feishu-bot-refresh-skill
```

### 步骤 4: 复制技能文件（1 分钟）

```bash
# 从已安装位置复制
cp -r /usr/lib/node_modules/openclaw/extensions/feishu/skills/feishu-bot-refresh \
  extensions/feishu/skills/

# 验证文件
ls -la extensions/feishu/skills/feishu-bot-refresh/
# 应该看到：SKILL.md, README.md, refresh-bot.sh
```

### 步骤 5: 提交代码（1 分钟）

```bash
# 添加文件
git add extensions/feishu/skills/feishu-bot-refresh/

# 提交
git commit -m "feat(feishu): add bot-refresh skill for identity refresh

- Add /refresh-bot-name command to refresh bot identity from Feishu API
- Support all configured Feishu accounts (dev-1, dev-2, test-1, etc.)
- Include shell script for programmatic use
- Store identity in bot-identity.json for caching
- Complete documentation (SKILL.md, README.md)

Usage:
  /refresh-bot-name          # In Feishu chat
  refresh-bot.sh dev-1       # Command line

Related: User-triggered refresh after bot name change in Feishu platform"
```

### 步骤 6: 推送到 GitHub（1 分钟）

```bash
git push origin feat/feishu-bot-refresh-skill
```

### 步骤 7: 创建 Pull Request（2 分钟）

1. 访问：https://github.com/openclaw/openclaw/pulls
2. 点击 **"New pull request"**
3. 选择：
   - **base repository:** `openclaw/openclaw`
   - **base branch:** `main` (或默认分支)
   - **head repository:** `<your-username>/openclaw`
   - **compare branch:** `feat/feishu-bot-refresh-skill`
4. 点击 **"Create pull request"**
5. 填写 PR 信息：

**标题：**
```
feat(feishu): add bot-refresh skill for identity refresh
```

**描述：**
```markdown
## 📝 Description

This PR adds a new skill `feishu-bot-refresh` to the Feishu extension that allows users to refresh their bot's identity information on-demand.

## ✨ Features

- **User-triggered refresh**: `/refresh-bot-name` command
- **Multi-account support**: Works with all configured Feishu accounts
- **Shell script**: `refresh-bot.sh` for programmatic use
- **Identity caching**: Stores in `bot-identity.json`

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

6. 点击 **"Create pull request"** 完成提交

---

## 📞 后续跟进

### 监控 PR 状态

- 关注 GitHub 通知
- 回复维护者的评论
- 根据反馈修改代码（如需要）

### 修改代码（如需要）

```bash
# 在本地修改文件
# ...

# 提交修改
git add extensions/feishu/skills/feishu-bot-refresh/
git commit -m "fix: address review comments"
git push origin feat/feishu-bot-refresh-skill

# PR 会自动更新
```

---

## 🎉 成功标志

PR 合并后：

1. 技能将包含在下一个 OpenClaw 版本中
2. 所有 Feishu 用户都可以使用 `/refresh-bot-name` 命令
3. 你的名字将出现在贡献者列表中

---

## 📚 相关资源

- **OpenClaw 文档:** https://docs.openclaw.ai
- **技能规范:** https://docs.openclaw.ai/skills/overview
- **Discord 社区:** https://discord.com/invite/clawd
- **ClawHub 技能市场:** https://clawhub.com

---

## ❓ 遇到问题？

### 常见问题

**Q: Fork 失败？**
A: 确保已登录 GitHub，检查网络连接

**Q: Git 命令不熟悉？**
A: 使用 GitHub Desktop 等图形工具

**Q: PR 被拒绝？**
A: 根据维护者反馈修改，这是正常流程

**Q: 技能文件位置不对？**
A: 确保在 `extensions/feishu/skills/` 目录下

---

## 📝 快速检查清单

提交前确认：

- [ ] 已 Fork openclaw/openclaw 仓库
- [ ] 已创建功能分支 `feat/feishu-bot-refresh-skill`
- [ ] 文件在正确位置 `extensions/feishu/skills/feishu-bot-refresh/`
- [ ] 包含 3 个文件：SKILL.md, README.md, refresh-bot.sh
- [ ] refresh-bot.sh 有执行权限
- [ ] 提交信息清晰完整
- [ ] PR 描述包含功能说明和测试情况

---

**准备好了吗？开始提交你的第一个 OpenClaw PR！** 🚀

**当前时间：** 2026-03-20 00:16 CST  
**技能版本：** v1.0  
**状态：** ✅ 准备提交
