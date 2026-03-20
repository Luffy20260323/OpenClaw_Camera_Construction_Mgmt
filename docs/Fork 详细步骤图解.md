# 🍴 GitHub Fork 详细步骤图解指南

**目标：** Fork https://github.com/openclaw/openclaw

---

## 📍 步骤 1: 访问 OpenClaw 仓库

在浏览器中打开：
```
https://github.com/openclaw/openclaw
```

---

## 📍 步骤 2: 找到 Fork 按钮

### 页面右上角

在页面**右上角**区域，你会看到几个按钮：

```
┌─────────────────────────────────────────┐
│  🔍 Search or jump to...     [登录/头像] │
│                                          │
│  openclaw / openclaw  Public             │
│                                          │
│  [⭐ Star]  [🍴 Fork]  [👁 Watch]  [📦 Fork] │
│                                          │
│  0  Stars    0  Forks    0  Watching     │
└─────────────────────────────────────────┘
```

**Fork 按钮特征：**
- 图标：🍴 (叉子图标)
- 文字：**"Fork"**
- 位置：Star 按钮右边，Watch 按钮左边
- 数字：显示 Fork 数量（可能是 0）

### 如果看不到 Fork 按钮

**可能原因：**
1. ❌ 未登录 GitHub
2. ❌ 屏幕太小（按钮被隐藏）
3. ❌ 网络问题页面未完全加载

**解决方法：**
1. **登录 GitHub：** https://github.com/login
2. **放大页面：** Ctrl + + (Windows) 或 Cmd + + (Mac)
3. **刷新页面：** F5 或 Ctrl + R

---

## 📍 步骤 3: 点击 Fork 按钮

1. **点击 "Fork" 按钮**
2. 等待几秒钟
3. 页面会自动跳转到 `https://github.com/RichardQidian/openclaw`

---

## 📍 步骤 4: 确认 Fork 成功

成功后的页面特征：

```
┌─────────────────────────────────────────┐
│  RichardQidian / openclaw  Public       │
│                                          │
│  Forked from openclaw/openclaw          │
│                                          │
│  [⭐ Star]  [📦 Fork]  [👁 Watch]        │
│                                          │
│  📁 extensions/                          │
│  📁 skills/                              │
│  📄 README.md                            │
└─────────────────────────────────────────┘
```

**关键标识：**
- ✅ 仓库名显示 `RichardQidian / openclaw`
- ✅ 有文字 "Forked from openclaw/openclaw"
- ✅ 能看到文件列表

---

## 📍 步骤 5: 推送代码到 Fork 的仓库

确认 Fork 成功后，在终端执行：

```bash
cd /tmp/openclaw-github/openclaw

# 使用 HTTPS 推送
git remote set-url origin https://github.com/RichardQidian/openclaw.git
git push -u origin main
```

**推送成功标志：**
```
Enumerating objects: X, done.
Counting objects: 100% (X/X), done.
Delta compression using up to X threads
Compressing objects: 100% (X/X), done.
Writing objects: 100% (X/X), X KiB | X MiB/s, done.
Total X (delta 0), reused 0 (delta 0), pack-reused 0
To https://github.com/RichardQidian/openclaw.git
 * [new branch]      main -> main
Branch 'main' set up to track remote branch 'main' from 'origin'.
```

---

## 📍 步骤 6: 验证推送成功

访问：https://github.com/RichardQidian/openclaw

应该能看到：
- ✅ 最新提交：`feat(feishu): add bot-refresh skill for identity refresh`
- ✅ 文件：`extensions/feishu/skills/feishu-bot-refresh/`

---

## 📍 步骤 7: 创建 Pull Request

1. **访问：** https://github.com/openclaw/openclaw/pulls
2. **点击：** "New pull request" (绿色按钮)
3. **点击：** "compare across forks" (如果看不到你的分支)
4. **选择：**
   - **base repository:** `openclaw/openclaw`
   - **base branch:** `main`
   - **head repository:** `RichardQidian/openclaw`
   - **compare branch:** `main`
5. **点击：** "Create pull request"
6. **填写标题和描述**
7. **点击：** "Create pull request"

---

## ❓ 常见问题

### Q1: 页面右上角没有 Fork 按钮？

**A:** 你可能没有登录 GitHub

1. 访问：https://github.com/login
2. 登录你的账号
3. 重新访问：https://github.com/openclaw/openclaw

### Q2: Fork 按钮是灰色的？

**A:** 这可能是只读镜像或其他特殊情况

尝试直接访问：https://github.com/openclaw/openclaw/fork

### Q3: 点击 Fork 后没反应？

**A:** 网络问题或浏览器问题

1. 刷新页面
2. 清除浏览器缓存
3. 尝试其他浏览器

### Q4: 提示 "You can't fork this repository because you're not a collaborator"?

**A:** 仓库可能是私有的

联系仓库管理员获取权限，或确认你访问的是正确的仓库地址。

---

## 🔗 快速链接

- **OpenClaw 仓库：** https://github.com/openclaw/openclaw
- **你的 Fork：** https://github.com/RichardQidian/openclaw
- **Pull Requests：** https://github.com/openclaw/openclaw/pulls
- **GitHub 登录：** https://github.com/login

---

## 📞 需要帮助？

如果还是找不到 Fork 按钮，可以：

1. **截图询问：** 在 Discord 社区求助
2. **查看 GitHub 教程：** https://docs.github.com/en/get-started/quickstart/fork-a-repo
3. **使用 GitHub Desktop：** 图形化工具更直观

---

**现在请尝试访问 https://github.com/openclaw/openclaw 并找到 Fork 按钮！** 🍴

**需要我继续指导哪一步？**
