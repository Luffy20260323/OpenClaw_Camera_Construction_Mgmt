# 推送 systematic-debug 技能到 OpenClaw 官方

## 问题说明

由于网络连接问题，无法自动执行 Git 操作。请按以下步骤手动推送。

---

## 📋 操作步骤

### 第 1 步：Fork OpenClaw 官方仓库

1. 访问 https://github.com/openclaw/openclaw
2. 点击右上角的 **Fork** 按钮
3. 等待 Fork 完成（会跳转到你的账号下的仓库）

---

### 第 2 步：克隆你的 Fork

```bash
# 克隆到你的临时目录
cd /tmp
git clone https://github.com/RichardQidian/openclaw.git
cd openclaw
```

**如果提示仓库不存在**，说明还没 Fork 成功，请回到第 1 步。

---

### 第 3 步：复制技能文件

```bash
# 从你的 workspace 复制技能到 OpenClaw 仓库
cp -r /root/.openclaw/workspace/skills/systematic-debug \
      /tmp/openclaw/skills/

# 验证复制成功
ls -la /tmp/openclaw/skills/systematic-debug/
```

应该看到：
```
SKILL.md
```

---

### 第 4 步：提交并推送

```bash
cd /tmp/openclaw

# 添加文件
git add skills/systematic-debug/

# 提交
git commit -m "feat: add systematic-debug skill for troubleshooting

- Universal debugging methodology for frontend/backend/deployment issues
- 6-step verification process: architecture → changes → build → deploy → runtime → code
- Core principle: confirm architecture first, then fix
- Includes checklists, common pitfalls, and real-world cases
- Based on real Docker deployment troubleshooting experience"

# 推送到你的 Fork
git push origin master
```

**如果需要认证**：
- 使用你的 GitHub Token（不是密码）
- Token 生成：https://github.com/settings/tokens

---

### 第 5 步：创建 Pull Request

1. 访问 https://github.com/openclaw/openclaw/pulls
2. 点击 **New pull request**
3. 选择：
   - **base repository**: `openclaw/openclaw`
   - **base branch**: `master` (或 `main`)
   - **head repository**: `RichardQidian/openclaw`
   - **compare branch**: `master`
4. 点击 **Create pull request**
5. 填写 PR 描述：

```markdown
## 新增技能：systematic-debug

### 技能描述
系统性问题排查技能，适用于所有技术问题的诊断和修复。

### 核心价值
- 提供 6 步系统化排查流程
- 避免"盲目修改代码"的常见错误
- 适用于前端/后端/数据库/网络/部署等所有场景

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
```

6. 点击 **Create pull request** 完成提交

---

## 🔍 验证技能格式

在推送前，可以验证技能格式是否正确：

```bash
# 查看技能文件
cat /root/.openclaw/workspace/skills/systematic-debug/SKILL.md

# 检查文件大小（应该小于 10KB）
ls -lh /root/.openclaw/workspace/skills/systematic-debug/SKILL.md
```

---

## 📦 备选方案：通过 Issue 提交

如果 PR 流程太复杂，可以通过 Issue 提交：

1. 访问 https://github.com/openclaw/openclaw/issues/new
2. 选择 "Feature Request"
3. 粘贴以下内容：

```markdown
## 技能名称
systematic-debug

## 技能描述
系统性问题排查技能，适用于所有技术问题的诊断和修复。

## 核心价值
- 提供 6 步系统化排查流程
- 避免"盲目修改代码"的常见错误
- 适用于前端/后端/数据库/网络/部署等所有场景

## 核心原则
1. 先确认架构，再动手修复
2. 不要假设，要验证
3. 80% 的"疑难杂症"都是架构/部署/状态问题，不是代码逻辑问题

## 技能文件
技能文件已打包，可以通过以下方式获取：
- 从贡献者的 workspace 复制：`/root/.openclaw/workspace/skills/systematic-debug/SKILL.md`
- 或联系贡献者获取

## 使用场景
- 用户报告"修改代码后没生效"
- 问题长期无法定位
- 试了很多方法都不行
- 功能突然不工作了

## 排查流程
1. 确认系统架构（Docker/宿主机/云服务）
2. 确认变更状态
3. 确认构建/编译是否生效
4. 确认部署是否生效
5. 确认运行时状态
6. 最后才是代码逻辑调试

## 来源
基于实际 Docker 部署场景下的前端问题排查经验总结
```

---

## 📞 联系方式

如果在推送过程中遇到问题：
- Discord: https://discord.com/invite/clawd
- 在 `#skills` 或 `#help` 频道求助

---

## ✅ 完成标志

推送成功后，你应该能看到：
- 你的 Fork 仓库中有 `skills/systematic-debug/SKILL.md`
- OpenClaw 官方仓库的 PR 列表中看到你的提交
- PR 被合并后，技能会出现在官方技能列表中
