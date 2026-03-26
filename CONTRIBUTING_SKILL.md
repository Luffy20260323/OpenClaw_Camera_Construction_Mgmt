# 向 OpenClaw 官方贡献技能

## 技能信息

**技能名称**: `systematic-debug`  
**技能描述**: 系统性问题排查技能  
**作者**: 柳生  
**创建日期**: 2026-03-26  

---

## 技能内容

本技能包含一个系统性的问题排查流程，核心原则：
> **先确认架构，再动手修复。不要假设，要验证。**

适用于：前端/后端/数据库/网络/部署等所有技术问题的排查。

---

## 推送方法

### 方法 1：通过 Pull Request（推荐）

1. **Fork OpenClaw 官方仓库**
   ```bash
   # 访问 https://github.com/openclaw/openclaw
   # 点击 Fork 按钮
   ```

2. **克隆你的 Fork**
   ```bash
   git clone https://github.com/<你的用户名>/openclaw.git
   cd openclaw
   ```

3. **复制技能文件**
   ```bash
   # 从你的 workspace 复制
   cp -r /root/.openclaw/workspace/skills/systematic-debug \
         /path/to/openclaw/skills/
   ```

4. **提交并推送**
   ```bash
   git add skills/systematic-debug/
   git commit -m "feat: add systematic-debug skill for troubleshooting

   - Universal debugging methodology for frontend/backend/deployment issues
   - 6-step verification process: architecture → changes → build → deploy → runtime → code
   - Core principle: confirm architecture first, then fix
   - Includes checklists, common pitfalls, and real-world cases"
   git push origin master
   ```

5. **创建 Pull Request**
   - 访问 https://github.com/openclaw/openclaw/pulls
   - 点击 "New Pull Request"
   - 选择你的分支，填写 PR 描述

---

### 方法 2：通过 Issue 提交

1. 访问 https://github.com/openclaw/openclaw/issues/new
2. 选择 "Feature Request" 或类似模板
3. 填写以下信息：

```markdown
## 技能名称
systematic-debug

## 技能描述
系统性问题排查技能，适用于所有技术问题的诊断和修复。

## 核心原则
- 先确认架构，再动手修复
- 不要假设，要验证
- 80% 的"疑难杂症"都是架构/部署/状态问题，不是代码逻辑问题

## 技能文件
见附件 systematic-debug-skill.tar.gz

## 使用场景
- 用户报告"修改代码后没生效"
- 问题长期无法定位
- 试了很多方法都不行
- 功能突然不工作了

## 排查流程
1. 确认系统架构（Docker/宿主机/云服务）
2. 确认变更状态（代码/配置/环境）
3. 确认构建/编译是否生效
4. 确认部署是否生效
5. 确认运行时状态（端口/连接/资源）
6. 最后才是代码逻辑调试

## 来源
基于实际调试经验总结（Docker 部署场景下的前端问题排查）
```

4. 上传附件 `systematic-debug-skill.tar.gz`

---

### 方法 3：通过 Discord 社区提交

1. 加入 OpenClaw Discord: https://discord.com/invite/clawd
2. 在 `#skills` 或 `#showcase` 频道发布技能信息
3. 附上技能文件

---

## 技能文件位置

- **本地路径**: `/root/.openclaw/workspace/skills/systematic-debug/SKILL.md`
- **全局路径**: `/usr/lib/node_modules/openclaw/skills/systematic-debug/SKILL.md`
- **打包文件**: `/root/.openclaw/workspace/systematic-debug-skill.tar.gz`

---

## 技能预览

查看完整技能文档：
```bash
cat /root/.openclaw/workspace/skills/systematic-debug/SKILL.md
```

---

## 联系信息

- **贡献者**: 柳生
- **GitHub**: https://github.com/RichardQidian
- **项目**: OpenClaw_Camera_Construction_Mgmt

---

## 许可证

本技能遵循 OpenClaw 项目的许可证（请确认具体许可证类型）。
