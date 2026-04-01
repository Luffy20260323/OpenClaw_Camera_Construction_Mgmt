# 手动推送 systematic-debug 技能到 OpenClaw

## 问题说明
由于 Git 克隆网络超时，请使用 GitHub 网页界面完成推送。

---

## 📋 操作步骤（全网页操作，无需 Git 命令）

### 第 1 步：访问你的 Fork 仓库

打开浏览器访问：
```
https://github.com/RichardQidian/openclaw
```

这是你之前（2026-03-20）Fork 的官方仓库。

---

### 第 2 步：上传技能文件

1. 在仓库页面，点击 **Add file** 按钮
2. 选择 **Upload files**
3. 将以下文件拖拽到上传区域：
   ```
   /root/.openclaw/workspace/skills/systematic-debug/SKILL.md
   ```
4. 或者点击 **choose your files** 选择该文件

---

### 第 3 步：填写提交信息

在 "Commit changes" 区域填写：

**Commit message**:
```
feat: add systematic-debug skill for troubleshooting
```

**Description** (可选):
```markdown
## 技能说明

系统性问题排查技能，适用于所有技术问题的诊断和修复。

### 核心原则
- 先确认架构，再动手修复
- 不要假设，要验证
- 80% 的"疑难杂症"都是架构/部署/状态问题，不是代码逻辑问题

### 排查流程（6 步）
1. 确认系统架构（Docker/宿主机/云服务）
2. 确认变更状态
3. 确认构建/编译是否生效
4. 确认部署是否生效
5. 确认运行时状态
6. 最后才是代码逻辑调试

### 来源
基于实际 Docker 部署场景下的前端问题排查经验总结
```

5. 选择 **Commit directly to the `main` branch**（或 `master`）
6. 点击 **Commit changes** 按钮

---

### 第 4 步：创建 Pull Request

1. 访问 https://github.com/openclaw/openclaw/pulls
2. 点击 **New pull request**
3. 点击 **compare across forks**
4. 设置对比：
   - **base repository**: `openclaw/openclaw`
   - **base branch**: `main` (或 `master`，看你 Fork 的默认分支)
   - **head repository**: `RichardQidian/openclaw`
   - **compare branch**: `main` (或 `master`)
5. 点击 **Create pull request**
6. 填写 PR 标题和描述：

**标题**:
```
feat: Add systematic-debug skill for universal troubleshooting
```

**描述**:
```markdown
## 新增技能：systematic-debug

### 技能描述
系统性问题排查技能，适用于所有技术问题的诊断和修复（前端/后端/数据库/网络/部署等）。

### 核心价值
- 提供 6 步系统化排查流程
- 避免"盲目修改代码"的常见错误
- 适用于所有技术场景

### 核心原则
1. 先确认架构，再动手修复
2. 不要假设，要验证
3. 80% 的"疑难杂症"都是架构/部署/状态问题，不是代码逻辑问题

### 排查流程
1. 确认系统架构（Docker/宿主机/云服务）
2. 确认变更状态
3. 确认构建/编译是否生效
4. 确认部署是否生效
5. 确认运行时状态
6. 最后才是代码逻辑调试

### 来源
基于实际 Docker 部署场景下的前端问题排查经验总结

### 测试
已在 OpenClaw 环境中测试，技能可正常加载和触发

---

## Checklist
- [x] 技能文件无硬编码路径
- [x] 技能文件无敏感信息（API Key/密码等）
- [x] 技能描述清晰
- [x] 排查流程通用化（适用于所有场景）
```

7. 点击 **Create pull request** 完成提交

---

## ✅ 完成标志

成功后你应该看到：
- 你的 Fork 仓库中多了一个文件：`skills/systematic-debug/SKILL.md`
- OpenClaw 官方仓库的 PR 列表中显示你的提交
- PR 状态为 "Open"

---

## 📁 技能文件位置

```
/root/.openclaw/workspace/skills/systematic-debug/SKILL.md
```

文件大小：约 6.7 KB

---

## 🔍 验证技能格式

在上传前，可以查看技能内容：
```bash
cat /root/.openclaw/workspace/skills/systematic-debug/SKILL.md
```

---

## 📞 需要帮助？

如果在操作过程中遇到问题：
- Discord: https://discord.com/invite/clawd
- 在 `#skills` 或 `#help` 频道求助
- 附上截图和错误信息

---

## 🎯 总结

整个流程：
1. ✅ 访问 https://github.com/RichardQidian/openclaw
2. ✅ 上传 `SKILL.md` 文件到 `skills/systematic-debug/` 目录
3. ✅ 提交到主分支
4. ✅ 在 https://github.com/openclaw/openclaw/pulls 创建 PR

全程使用 GitHub 网页界面，无需 Git 命令行！
