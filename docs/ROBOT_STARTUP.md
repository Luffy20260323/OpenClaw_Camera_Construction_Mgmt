# 🤖 Camera 项目 - 机器人启动规范

> **版本:** 2.0 (带自动同步) | **更新:** 2026-04-03  
> **适用范围:** 所有参与 Camera 项目的机器人

---

## ⚡ 快速启动（必须遵守）

```bash
# 步骤 1：切换到项目目录
cd /root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt

# 步骤 2：【必须】运行任务同步脚本
./scripts/sync-tasks.sh --verbose

# 步骤 3：查看同步后的任务看板
cat docs/TASK_BOARD.md

# 步骤 4：选择并开始任务
```

---

## 📋 详细启动流程

### 步骤 0：自动任务同步 ⭐

**为什么必须执行？**
- 确保任务看板反映真实工作状态
- 发现未登记的活跃任务（如开发 2 的 Bug 定位）
- 避免多机器人重复工作

**同步内容:**
- 扫描活跃机器人会话
- 扫描 git 提交记录（过去 24 小时）
- 扫描任务文档执行记录
- 自动更新任务状态（TODO → WIP）

**输出示例:**
```
🔄 开始任务同步...
  → 扫描机器人会话...
  → 扫描任务文档中的执行记录...
  ✓ 发现活跃任务：T005 (负责人：开发 2)
  → 扫描 git 提交记录...
  ✓ 发现活跃开发者：开发 1
📋 同步任务状态...
  ✓ 更新 T005: TODO → WIP (开发 2)

✅ 同步完成
   活跃机器人：2
   同步任务：1
```

---

### 步骤 1：确认当前项目

```bash
cat /root/.openclaw/workspace/CURRENT_PROJECT.md
```

**确认信息:**
- ✅ 当前项目：OpenClaw_Camera_Construction_Mgmt
- ✅ 项目路径：`/root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt`

---

### 步骤 2：查看任务看板

```bash
cat docs/TASK_BOARD.md
```

**关注:**
- 自己是否有进行中的任务（WIP）
- 有待认领的 TODO 任务
- 任务优先级（P0 > P1 > P2）

---

### 步骤 3：选择任务

**优先级规则:**
1. **继续自己的 WIP 任务** - 如果之前有未完成的任务
2. **P0 紧急任务** - 优先级最高的 TODO 任务
3. **其他 TODO 任务** - 根据能力选择

---

### 步骤 4：阅读任务详情

```bash
cat docs/tasks/<任务 ID>.md
```

**确认:**
- 任务说明和完成标准
- 相关文档和代码位置
- 已知问题和注意事项

---

### 步骤 5：认领任务（如需要）

**如果同步脚本已自动更新状态为 WIP，跳过此步骤！**

```bash
./scripts/claim-task.sh <任务 ID> <你的机器人名称>
```

---

### 步骤 6：开始工作

按任务文档要求执行开发工作。

**工作完成后:**
```bash
./scripts/complete-task.sh <任务 ID> "完成说明"
```

---

## 🔧 常用命令

| 操作 | 命令 |
|------|------|
| **任务同步** | `./scripts/sync-tasks.sh --verbose` |
| 查看任务看板 | `cat docs/TASK_BOARD.md` |
| 查看任务详情 | `cat docs/tasks/T001.md` |
| 认领任务 | `./scripts/claim-task.sh T001 机器人名称` |
| 完成任务 | `./scripts/complete-task.sh T001 "说明"` |
| 查看同步状态 | `cat .sync-state.json` |

---

## ⚠️ 注意事项

### 1. 同步频率限制
- 同步脚本每 5 分钟只执行一次实际同步
- 避免频繁调用导致看板状态抖动

### 2. 任务状态流转
```
TODO → WIP → REVIEW → DONE
```

### 3. 发现 Bug 怎么办？
1. 首先运行同步脚本
2. 如果看板没有 Bug 定位任务，手动创建：
   ```bash
   # 在 docs/tasks/ 创建新任务文档
   # 然后运行 claim-task.sh 认领
   ```

### 4. 多人协作
- 如果看到任务已是 WIP 状态，不要重复认领
- 可以在任务文档中留言协作
- 或选择其他 TODO 任务

---

## 📞 需要帮助？

1. 查看多机器人协作指南：`../../OpenClaw_Multi_Rots_Multi_Projects_Multi_Tasks_Rule/docs/MULTI_ROBOT_COLLABORATION_GUIDE.md`
2. 查看项目文档：`docs/` 目录
3. 在任务文档中留言

---

_此规范由多机器人协作系统维护，版本 2.0_
