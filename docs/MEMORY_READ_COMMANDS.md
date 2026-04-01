# 📖 记忆读取指令 - Memory Read Commands

> **目的：** 让机器人读取历史记忆文件，保持项目记忆连续性

---

## 🚀 快速指令

### 指令 1：读取最近记忆

```
读取最近 7 天的记忆
```

```
查看昨天的记忆
```

```
汇总最近 3 天的工作内容
```

### 指令 2：读取特定日期

```
读取 2026-03-30 的记忆
```

```
查看 2026-03-27 到 2026-03-31 的记忆
```

### 指令 3：项目相关记忆

```
读取摄像头项目的所有记忆
```

```
汇总 Docker 构建相关的记忆
```

---

## 🤖 机器人自动行为

### 每次会话启动时

根据 `AGENTS.md`，机器人会自动读取：

1. **SOUL.md** - 机器人身份
2. **USER.md** - 用户信息
3. **memory/*.md** - **最近 7 天**的记忆文件
4. **MEMORY.md** - 长期记忆（主会话）

### 读取的记忆文件示例

假设今天是 `2026-03-31`，机器人会自动读取：

```
memory/2026-03-31.md  ✅ (今天)
memory/2026-03-30.md  ✅ (昨天)
memory/2026-03-29.md  ✅ (2 天前)
memory/2026-03-28.md  ✅ (3 天前)
memory/2026-03-27.md  ✅ (4 天前)
memory/2026-03-26.md  ✅ (5 天前)
memory/2026-03-25.md  ✅ (6 天前)
```

---

## 📋 可用脚本

### 脚本 1：read-memory.sh

**位置：** `/root/.openclaw/workspace/scripts/read-memory.sh`

**用法：**
```bash
# 读取最近 7 天
./scripts/read-memory.sh 7

# 读取最近 3 天
./scripts/read-memory.sh 3

# 读取最近 14 天
./scripts/read-memory.sh 14
```

**输出示例：**
```
📖 读取最近 7 天的记忆文件
=============================================

✅ 2026-03-31 (0 天前)
---
# 2026-03-31 记忆日志

## 🔧 Docker 构建优化（重要）
...

✅ 2026-03-30 (1 天前)
---
# 2026-03-30 记忆日志
...
```

---

## 💡 最佳实践

### 1. 日常记忆维护

**每天结束时（或第二天开始时）：**
```markdown
# 在 memory/YYYY-MM-DD.md 记录

## 📋 今日工作
- 09:00 开始 XXX 功能开发
- 11:00 完成 API 实现
- 14:00 修复 bug #123
- 17:00 部署到测试环境

## 🎯 重要决策
- 采用方案 A 而非方案 B（原因：...）

## ⚠️ 注意事项
- XXX 问题需要注意
```

### 2. 每周记忆整理

**每周末或周一：**
1. 回顾上周的 `memory/*.md` 文件
2. 提取重要信息到 `MEMORY.md`
3. 删除或归档旧文件

### 3. 项目里程碑

**项目关键节点：**
- 更新 `MEMORY.md` 的项目状态
- 创建 `docs/项目总结.md`
- 记录经验教训

---

## 🔧 自定义配置

### 修改读取天数

编辑 `AGENTS.md`：

```markdown
## Session Startup

Before doing anything else:

1. Read `SOUL.md` — this is who you are
2. Read `USER.md` — this is who you're helping
3. Read `memory/YYYY-MM-DD.md` (**last N days**) for recent context
4. **If in MAIN SESSION**: Also read `MEMORY.md`
```

**建议值：**
- **3 天** - 轻量级，token 使用少
- **7 天** - 默认推荐，平衡型
- **14 天** - 复杂项目，完整上下文
- **30 天** - 长期项目（token 使用多）

### 添加特定项目记忆

创建项目专属记忆文件：

```bash
# 摄像头项目记忆
memory/camera-project.md

# SDMGT 项目记忆
memory/sdmgt-project.md
```

在 `AGENTS.md` 中添加：

```markdown
### Project-Specific Memory

For ongoing projects, also read:
- `memory/camera-project.md` (if working on camera system)
- `memory/sdmgt-project.md` (if working on SDMGT)
```

---

## 📊 记忆文件结构

### 现有记忆文件

```
memory/
├── 2026-03-15-requirements.md  # 需求文档
├── 2026-03-20.md               # 3 月 20 日记忆
├── 2026-03-24.md               # 3 月 24 日记忆
├── 2026-03-27.md               # 3 月 27 日记忆
├── 2026-03-28.md               # 3 月 28 日记忆
├── 2026-03-29.md               # 3 月 29 日记忆
├── 2026-03-30.md               # 3 月 30 日记忆
├── 2026-03-31.md               # 3 月 31 日记忆
└── manager-bot-prompt.md       # 管理机器人提示
```

### 推荐结构

```markdown
# YYYY-MM-DD 记忆日志

## 🔧 技术工作
- 功能开发
- Bug 修复
- 性能优化

## 📋 项目进展
- 完成的任务
- 进行中的任务
- 待办事项

## 🎯 重要决策
- 技术选型
- 架构变更
- 流程优化

## ⚠️ 注意事项
- 已知问题
- 待解决风险
- 经验教训

## 🤖 机器人相关
- 配置变更
- 技能更新
- 对话记录
```

---

## 🎯 实际使用示例

### 场景 1：中断后继续工作

**用户：** 继续昨天的 Docker 构建优化

**机器人：**
1. 读取 `memory/2026-03-31.md`（今天）
2. 读取 `memory/2026-03-30.md`（昨天）
3. 查看 `MEMORY.md` 中的相关记录
4. 基于上下文继续工作

### 场景 2：查询历史信息

**用户：** 上周我们讨论了什么？

**机器人：**
1. 读取最近 7 天的 `memory/*.md`
2. 汇总关键事件
3. 提供时间线总结

### 场景 3：项目回顾

**用户：** 摄像头项目的进展如何？

**机器人：**
1. 搜索所有 `memory/*.md` 中的摄像头相关内容
2. 提取关键里程碑
3. 生成项目报告

---

## 📞 故障排查

### 问题 1：记忆文件未读取

**检查：**
```bash
# 查看文件是否存在
ls -la memory/*.md

# 查看文件内容
cat memory/2026-03-31.md
```

**解决：** 确保文件命名正确（`YYYY-MM-DD.md`）

### 问题 2：token 使用过多

**解决：**
- 减少读取天数（7 天 → 3 天）
- 精简记忆文件内容
- 定期整理 `MEMORY.md`

### 问题 3：记忆不连续

**解决：**
- 每天创建记忆文件
- 使用统一的模板
- 定期更新 `MEMORY.md`

---

## 📚 相关文档

- `AGENTS.md` - 会话启动配置
- `MEMORY.md` - 长期记忆
- `memory/YYYY-MM-DD.md` - 每日记忆
- `scripts/read-memory.sh` - 记忆读取脚本

---

**最后更新：** 2026-03-31  
**维护者：** 北京其点技术服务有限公司
