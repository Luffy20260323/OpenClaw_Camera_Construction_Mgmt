# 版本控制与发布流程指南

> 本文档定义了项目的开发、测试、Bug 追踪与版本发布流程。

---

## 目录

1. [Git 工作流](#1-git-工作流)
2. [开发阶段](#2-开发阶段)
3. [测试阶段](#3-测试阶段)
4. [Bug 追踪流程](#4-bug-追踪流程)
5. [版本发布流程](#5-版本发布流程)
6. [Git 命令速查](#6-git-命令速查)
7. [GitHub Issue 规范](#7-github-issue-规范)

---

## 1. Git 工作流

### 分支策略

```
master (主分支)
    │
    ├── feature/xxx (功能分支)
    │       │
    │       └── 开发完成后合并回 master
    │
    ├── fix/xxx (修复分支)
    │       │
    │       └── 修复完成后合并回 master
    │
    └── release/vx.x.x (发布分支)
            │
            └── 用于发布后维护
```

### 分支命名规范

| 分支类型 | 命名格式 | 示例 |
|----------|----------|------|
| 功能分支 | `feature/功能名` | `feature/audit-log` |
| 修复分支 | `fix/bug描述` | `fix/login-error` |
| 发布分支 | `release/v版本号` | `release/v1.0.0` |
| 热修复分支 | `hotfix/问题描述` | `fix/security-vulnerability` |

---

## 2. 开发阶段

### 标准开发流程

```bash
# 1. 确保本地 master 是最新的
git checkout master
git pull origin master

# 2. 从 master 创建功能分支
git checkout -b feature/新功能名

# 3. 开发过程中定期同步 master 变更
git fetch origin
git merge origin/master
# 或使用 rebase（保持线性历史）
git rebase origin/master

# 4. 开发完成，提交代码
git add .
git commit -m "feat: 添加 xxx 功能

- 新增审计日志查询接口
- 添加审计日志页面组件"

# 5. 切换回 master 并合并
git checkout master
git merge feature/新功能名

# 6. 推送至 GitHub
git push origin master

# 7. （可选）删除功能分支
git branch -d feature/新功能名
git push origin --delete feature/新功能名
```

---

### 完整构建与部署流程（重要！）

**每次代码修改后，必须按以下完整流程执行，确保新代码真正生效：**

#### 后端构建流程

```bash
cd /root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/backend

# ========== Step 1: 确认修改的文件 ==========
git status

# ========== Step 2: 完整编译打包（不要跳过任何步骤！） ==========
mvn clean package -DskipTests

# ========== Step 3: 确认 jar 文件是最新的 ==========
ls -la target/*.jar
# 注意检查文件更新时间

# ========== Step 4: 构建 Docker 镜像 ==========
docker build -t openclaw_camera_construction_mgmt-backend:latest .

# ========== Step 5: 备份当前运行的容器（推荐） ==========
docker commit camera-backend backup-camera-backend:working

# ========== Step 6: 停止旧容器 ==========
docker rm -f camera-backend

# ========== Step 7: 启动新容器 ==========
docker run -d --name camera-backend \
  --network openclaw_camera_construction_mgmt_camera-network \
  -e DB_HOST=camera-postgres \
  -e DB_PASSWORD=postgres \
  -e REDIS_HOST=camera-redis \
  -e JWT_SECRET="camera-lifecycle-system-secret-key-2026-qidian-technology" \
  -p 8080:8080 \
  openclaw_camera_construction_mgmt-backend:latest

# ========== Step 8: 验证启动成功 ==========
sleep 15
docker logs camera-backend --tail 20
docker ps | grep camera-backend
```

#### 前端构建流程

```bash
cd /root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/frontend

# ========== Step 1: 确认修改的文件 ==========
git status

# ========== Step 2: 安装依赖（如果 package.json 变更） ==========
npm install

# ========== Step 3: 编译构建 ==========
npm run build

# ========== Step 4: 确认构建产物 ==========
ls -la dist/

# ========== Step 5: 部署（根据实际情况选择） ==========
# 方式 A：直接替换
cp -r dist/* /var/www/html/

# 方式 B：通过 Docker（如果前端也在容器中）
# ...

# ========== Step 6: 验证 ==========
# 刷新浏览器，确认新功能生效
```

---

### 构建检查清单（必须检查）

| 步骤 | 检查项 | 命令 |
|------|--------|------|
| 1 | 代码已提交 | `git status` 无未提交文件 |
| 2 | 编译成功 | `mvn clean package` 无 ERROR（**必须是 clean package，不是 package**） |
| 3 | jar 文件是最新的 | `ls -la target/*.jar` 时间戳 |
| 4 | Docker 镜像构建成功 | 无 "Successfully tagged" 错误 |
| 5 | 容器启动成功 | `docker ps` 状态为 Up |
| 6 | 服务日志正常 | `docker logs camera-backend` 无 ERROR |
| 7 | 功能验证通过 | 实际访问测试 |

---

### 常见错误及排查

| 错误现象 | 可能原因 | 解决方法 |
|----------|----------|----------|
| 容器启动后功能未生效 | 镜像未重新构建 | 重新执行 `docker build` |
| 编译成功但运行报错 | 依赖未更新 | 检查 pom.xml 是否有新增依赖 |
| 容器启动失败 | 配置文件错误 | 检查环境变量、挂载配置 |
| 登录失败 | JWT 配置不一致 | 确认 JWT_SECRET 一致 |

---

### 回滚操作（出问题时的快速恢复）

```bash
# 快速回滚到上一个工作版本
docker rm -f camera-backend
docker run -d --name camera-backend backup-camera-backend:working ...

# 或回滚到特定的镜像版本
docker run -d --name camera-backend openclaw_camera_construction_mgmt-backend:v1.0.0
```

---

### 实战案例：修改不生效问题排查（重要！）

> **案例日期**：2026-04-05
> **问题**：修改了 SystemConfigService 代码，但修改后功能不生效

#### 问题现象
- 源代码已正确修改（添加了 INSERT 语句）
- 但运行时日志显示仍是旧的查询逻辑（SELECT 查询）
- Docker 容器已重启，但问题依旧

#### 根因分析

| 步骤 | 实际情况 | 问题 |
|------|----------|------|
| 修改代码 | ✅ 源代码正确 | - |
| Maven 编译 | ⚠️ 使用 `mvn package` | 旧 class 文件未清除 |
| Docker 构建 | ⚠️ 可能使用了缓存 | 没有强制重新打包 |

**关键问题：使用了 `mvn package` 而不是 `mvn clean package`**

- 不带 `clean` 时，Maven 只会编译修改过的文件
- 旧 class 文件可能残留，导致新代码未被打包

#### 解决步骤

```bash
# 1. 重新编译（使用 clean）
cd backend
mvn clean package -DskipTests

# 2. 确认 jar 文件已更新（检查时间戳）
ls -la target/*.jar

# 3. 重新构建 Docker 镜像
docker build -t openclaw_camera_construction_mgmt-backend:latest .

# 4. 重启容器
docker rm -f camera-backend
docker run -d --name camera-backend ...

# 5. 验证
docker logs camera-backend --tail 20
```

#### 经验教训

1. **必须使用 `mvn clean package`**：删除旧的 target 目录，强制重新编译所有文件
2. **验证 jar 内容**：可以用 `unzip -l target/*.jar | grep 类名` 确认新 class 存在
3. **检查 Docker 构建日志**：确认看到了 javac 编译输出
4. **容器重启后务必验证**：查看日志确认新代码已加载

#### 预防措施

- 养成习惯：任何编译都使用 `mvn clean package`，不要省掉 `clean`
- 可以在 IDE 或脚本中设置别名：`alias mcp='mvn clean package'`

### 提交信息规范（Conventional Commits）

```
<类型>[可选范围]: <描述>

[可选正文]

[可选脚注]
```

**类型（Type）：**

| 类型 | 说明 |
|------|------|
| `feat` | 新功能 |
| `fix` | Bug 修复 |
| `docs` | 文档变更 |
| `style` | 格式调整（不影响代码） |
| `refactor` | 重构（不是新功能也不是修复） |
| `perf` | 性能优化 |
| `test` | 测试相关 |
| `chore` | 构建/工具变更 |

**示例：**

```
feat(auth): 添加登录验证码功能

- 新增图形验证码接口
- 前端添加验证码组件
- 验证码有效期设为 5 分钟

Closes #123
```

---

## 3. 测试阶段

### 测试版本发布

当需要提测时，创建 RC（Release Candidate）标签：

```bash
# 创建测试版本标签
git tag -a v1.0.0-rc1 -m "测试版本 v1.0.0-rc1

新增功能：
- 审计日志
- 资源管理

测试重点：
- 权限控制
- 数据隔离"

# 推送标签到 GitHub
git push origin v1.0.0-rc1
```

### 部署测试版本

```bash
# 后端
cd backend
mvn clean package -DskipTests
docker build -t openclaw_camera_construction_mgmt-backend:v1.0.0-rc1 .
docker tag openclaw_camera_construction_mgmt-backend:v1.0.0-rc1 openclaw_camera_construction_mgmt-backend:latest

# 前端（如果需要）
cd frontend
npm run build
# 部署到测试服务器...
```

---

## 4. Bug 追踪流程

### Bug 发现 → 报告流程

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│  发现 Bug    │ ──▶ │  创建 Issue  │ ──▶ │  分配给开发者│
└──────────────┘     └──────────────┘     └──────────────┘
                                                    │
                                                    ▼
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│  回归测试    │ ◀── │  合并修复    │ ◀── │   修复 Bug   │
└──────────────┘     └──────────────┘     └──────────────┘
```

### Bug 生命周期

| 状态 | 说明 |
|------|------|
| `Open` | 新发现的 Bug |
| `Confirmed` | 已确认的 Bug |
| `In Progress` | 正在修复 |
| `Resolved` | 已修复，待验证 |
| `Closed` | 已验证，关闭 |

### 修复并关联 Issue

```bash
# 1. 创建修复分支
git checkout -b fix/issue-123-description

# 2. 修复 Bug
# ... 修改代码 ...

# 3. 提交（关联 Issue）
git commit -m "fix: 修复审计日志权限问题

问题原因：权限校验逻辑未考虑审计日志的特殊情况
修复方案：添加针对审计日志的特殊处理

Closes #123"

# 4. 合并到 master
git checkout master
git merge fix/issue-123-description
git push origin master

# 5. 推送修复分支（可选，方便 Code Review）
git push origin fix/issue-123-description

# 6. 关闭 Issue（在 GitHub 上操作，或使用命令）
gh issue close 123
```

---

## 5. 版本发布流程

### 发布检查清单

- [ ] 所有功能开发完成
- [ ] 所有测试用例通过
- [ ] 所有已知 Bug 已修复
- [ ] 代码已通过 Code Review
- [ ] 文档已更新
- [ ] 数据库迁移脚本已准备（如需要）

### 正式版本发布步骤

```bash
# 1. 确保 master 是最新的
git checkout master
git pull origin master

# 2. 创建发布分支（可选，用于发布后维护）
git checkout -b release/v1.0.0

# 3. 更新版本号（如需要）
# 修改 pom.xml、package.json 等

# 4. 创建正式版本标签
git tag -a v1.0.0 -m "正式版本 v1.0.0

发布内容：
- 新增审计日志功能
- 新增资源管理功能
- 优化权限控制逻辑

感谢所有贡献者！"

# 5. 推送标签
git push origin v1.0.0

# 6. 如创建了发布分支，推送它
git push origin release/v1.0.0

# 7. 合并回 master（如果在发布分支上做修改）
git checkout master
git merge release/v1.0.0
git push origin master
```

### GitHub Release 操作

在 GitHub 上创建 Release：

1. 进入仓库 → 点击 "Releases" → "Draft a new release"
2. 填写版本信息：
   - **Tag version**: `v1.0.0`
   - **Release title**: `Version 1.0.0 - 正式发布`
3. 填写 Release Notes（可自动生成）
4. 点击 "Publish release"

### 版本号规范

```
v主版本.次版本.修订版本[-阶段]

示例说明：
v1.0.0       - 正式版本
v1.0.0-rc1   - 第一个候选版本 (Release Candidate)
v1.0.0-rc2   - 第二个候选版本
v1.0.0-beta  - 测试版
v1.0.1       - 修复了小问题
v1.1.0       - 新增了功能
v2.0.0       - 重大更新，可能不兼容之前版本
```

---

## 6. Git 命令速查

### 分支操作

```bash
# 查看分支
git branch -a

# 创建分支
git checkout -b feature/xxx

# 切换分支
git checkout master

# 删除本地分支
git branch -d feature/xxx

# 删除远程分支
git push origin --delete feature/xxx
```

### 标签操作

```bash
# 查看标签
git tag

# 创建标签
git tag -a v1.0.0 -m "版本说明"

# 推送标签到远程
git push origin v1.0.0

# 推送所有标签
git push origin --tags

# 删除本地标签
git tag -d v1.0.0

# 删除远程标签
git push origin --delete v1.0.0
```

### 撤销操作

```bash
# 撤销未提交的修改
git checkout -- filename

# 撤销已提交到暂存区的修改
git reset HEAD filename

# 撤销最近一次提交（保留修改）
git reset --soft HEAD~1

# 撤销最近一次提交（不保留修改）
git reset --hard HEAD~1
```

---

## 7. GitHub Issue 规范

### Bug 报告模板

```markdown
## 版本
v1.0.0-rc1

## Bug 描述
[清晰描述问题]

## 复现步骤
1. 登录 admin 账号
2. 导航到 xxx 页面
3. 点击 xxx 按钮

## 预期行为
[期望发生什么]

## 实际行为
[实际发生了什么]

## 相关截图
[贴图]

## 日志信息
```
[错误日志]
```

## 环境信息
- 浏览器：
- 操作系统：
- 后端版本：
```

### 功能请求模板

```markdown
## 功能描述
[清晰描述你想要的功能]

## 使用场景
作为 [角色]，我希望 [功能]，以便 [价值]

## 方案建议
[可选：提出你的实现建议]

## 附加信息
- [ ] 这是一个新功能
- [ ] 这是一个改进
```

---

## 附录：回滚操作

### 回滚到指定版本

```bash
# 查看版本历史
git log --oneline --graph

# 回滚到指定提交
git checkout v1.0.0

# 或创建回滚分支
git checkout -b rollback-v1.0.0 v1.0.0
```

### 回滚合并

```bash
# 回滚合并的提交
git revert -m 1 merge-commit-sha
```

---

## 总结

| 阶段 | 关键操作 | 产出 |
|------|----------|------|
| 开发 | 创建 feature 分支 → 开发 → 合并 | 代码变更 |
| 测试 | 创建 rc 标签 → 部署测试 | 测试版本 |
| Bug 追踪 | 创建 Issue → 修复 → 关联提交 | Bug 修复 |
| 发布 | 创建正式标签 → GitHub Release | 正式版本 |

---

*文档版本：1.0*
*最后更新：2026-04-05*