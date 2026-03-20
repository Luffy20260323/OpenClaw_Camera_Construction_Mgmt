# 🔐 GitHub 推送认证指南

**当前状态：** ✅ Fork 已完成，等待推送

---

## 方式 1：使用 GitHub Token（推荐）

### 步骤 1: 创建 Personal Access Token

1. **访问：** https://github.com/settings/tokens
2. **点击：** "Generate new token" → "Generate new token (classic)"
3. **填写：**
   - **Note:** `OpenClaw PR Push`
   - **Expiration:** `90 days`（或更长）
   - **Select scopes:** 勾选 `repo` (Full control of private repositories)
4. **点击：** "Generate token"
5. **复制 Token：** `ghp_xxxxxxxxxxxxxxxxxxxx`（只显示一次！）

### 步骤 2: 使用 Token 推送

```bash
cd /tmp/openclaw-github/openclaw

# 使用 Token 推送（替换 YOUR_TOKEN 为实际 token）
git remote set-url origin https://YOUR_TOKEN@github.com/RichardQidian/openclaw.git
git push -u origin main
```

**示例：**
```bash
git remote set-url origin https://ghp_abc123xyz456@github.com/RichardQidian/openclaw.git
git push -u origin main
```

---

## 方式 2：使用 SSH（如果你配置过 SSH key）

### 步骤 1: 检查 SSH key

```bash
# 检查是否有 SSH key
ls -la ~/.ssh/id_rsa.pub
```

### 步骤 2: 如果没有 SSH key，创建它

```bash
# 创建 SSH key
ssh-keygen -t ed25519 -C "your_email@example.com"

# 查看公钥
cat ~/.ssh/id_ed25519.pub
```

### 步骤 3: 添加 SSH key 到 GitHub

1. **复制公钥内容**（`cat ~/.ssh/id_ed25519.pub` 的输出）
2. **访问：** https://github.com/settings/keys
3. **点击：** "New SSH key"
4. **粘贴公钥** 并保存

### 步骤 4: 使用 SSH 推送

```bash
cd /tmp/openclaw-github/openclaw

# 切换到 SSH 方式
git remote set-url origin git@github.com:RichardQidian/openclaw.git

# 推送
git push -u origin main
```

---

## 方式 3：使用 Git Credential Manager（Windows/Mac）

如果你使用 Windows 或 Mac，Git 可能会自动弹出登录窗口：

```bash
cd /tmp/openclaw-github/openclaw

# 直接推送，会弹出登录窗口
git push -u origin main
```

在弹出的窗口中：
1. 输入 GitHub 账号密码
2. 完成登录
3. 推送会自动进行

---

## ✅ 推送成功标志

```
Enumerating objects: 5, done.
Counting objects: 100% (5/5), done.
Delta compression using up to 4 threads
Compressing objects: 100% (3/3), done.
Writing objects: 100% (3/3), 1.23 KiB | 1.23 MiB/s, done.
Total 3 (delta 0), reused 0 (delta 0), pack-reused 0
remote: 
remote: Create a pull request for 'main' on GitHub by visiting:
remote:      https://github.com/RichardQidian/openclaw/pull/new/main
remote: 
To https://github.com/RichardQidian/openclaw.git
 * [new branch]      main -> main
Branch 'main' set up to track remote branch 'main' from 'origin'.
```

---

## 🔍 验证推送成功

访问：https://github.com/RichardQidian/openclaw

应该能看到：
- ✅ 最新提交：`feat(feishu): add bot-refresh skill for identity refresh`
- ✅ 文件：`extensions/feishu/skills/feishu-bot-refresh/`

---

## 📞 选择哪种方式？

| 方式 | 适用场景 | 难度 |
|------|----------|------|
| **Token** | 所有系统，推荐 | ⭐⭐ |
| **SSH** | 已配置 SSH key | ⭐ |
| **Credential Manager** | Windows/Mac 图形界面 | ⭐ |

**推荐：使用 Token 方式**，虽然步骤多一点，但最可靠！

---

## ❓ 需要帮助？

告诉我你选择哪种方式，我可以帮你执行具体命令！
