# 编译产物上传策略

_最后更新：2026-03-30_

---

## 📋 策略概述

本策略确保所有本地编译生成的文件都能自动上传到 GitHub，同时控制存储空间使用。

---

## 🎯 核心原则

1. **自动上传** - 所有编译产物必须自动上传到 GitHub
2. **保留期限** - Artifact 统一保留 **3 天**
3. **大文件监控** - 对大于 **5MB** 的文件进行标记和警告
4. **多种途径** - 通过 GitHub Actions 和 Releases 两种方式上传

---

## 📁 编译产物范围

### 后端 (Backend)

| 文件/目录 | 上传方式 | 说明 |
|----------|---------|------|
| `backend/target/*.jar` | ✅ GitHub Actions + Releases | 可执行 jar 包 |
| `backend/target/classes/` | ❌ 不上传 | 包含在 jar 中 |
| `backend/target/maven-archiver/` | ❌ 不上传 | 构建元数据 |

### 前端 (Frontend)

| 文件/目录 | 上传方式 | 说明 |
|----------|---------|------|
| `frontend/dist/` | ✅ GitHub Actions + Releases | 构建产物（压缩上传） |
| `frontend/node_modules/` | ❌ 不上传 | 通过 package.json 重建 |

### 测试报告

| 文件/目录 | 上传方式 | 保留期 |
|----------|---------|--------|
| `backend/tests/reports/` | ✅ GitHub Actions | 3 天 |
| `e2e/results/` | ✅ GitHub Actions | 3 天 |
| `/tmp/backend.log` | ✅ GitHub Actions (失败时) | 3 天 |

---

## 🔧 工作流程

### 1. CI 工作流自动上传

**触发条件：** push / pull_request

**上传内容：**
- 后端 jar 包
- 前端 dist 目录
- 测试报告

**保留期限：** 3 天

**工作流文件：**
- `.github/workflows/ci-camera1001.yml`
- `.github/workflows/ci-master.yml`
- `.github/workflows/ci-full-automation.yml`
- `.github/workflows/api-test.yml`

### 2. Releases 自动发布

**触发条件：** push (所有分支)

**上传内容：**
- 后端 jar 包
- 前端 dist 压缩包
- 构建说明文档

**保留期限：** 3 天（自动清理）

**工作流文件：**
- `.github/workflows/upload-to-releases.yml`

### 3. 本地手动上传

**使用场景：** 本地编译后需要快速分享

**脚本：** `scripts/upload-build-artifacts.sh`

**使用方法：**
```bash
# 1. 本地编译
cd backend && mvn clean package -DskipTests
cd ../frontend && npm run build

# 2. 运行上传脚本
cd ..
./scripts/upload-build-artifacts.sh
```

**注意事项：**
- 需要手动确认（因为编译产物通常在 .gitignore 中）
- 会创建独立的 git 提交
- 适合临时分享，不建议频繁使用

---

## 📊 大文件监控

### 检查阈值

| 类型 | 阈值 | 动作 |
|------|------|------|
| 单个文件 | > 5MB | ⚠️ 警告标记 |
| 目录总计 | > 100MB | ⚠️ 警告标记 |

### 检查步骤

每个上传 artifact 的步骤前都会添加大小检查：

```yaml
- name: 🔍 检查构建产物大小
  run: |
    echo "检查大于 5MB 的构建产物..."
    find backend/target -type f -size +5M -exec echo "⚠️ 大文件：{} ($(du -h {} | cut -f1))" \; || true
    ls -lh backend/target/*.jar 2>/dev/null || true
```

---

## 🧹 自动清理策略

### GitHub Actions Artifact

- **保留期：** 3 天
- **清理方式：** 自动过期
- **配置位置：** 每个 `upload-artifact` 步骤的 `retention-days: 3`

### GitHub Releases

- **保留期：** 3 天
- **清理方式：** `cleanup-old-releases` 工作流
- **触发条件：** master 分支 push 后

### 本地编译产物

- **清理责任：** 开发者自行管理
- **建议：** 定期运行 `mvn clean` 和 `rm -rf frontend/dist`

---

## 📈 存储空间管理

### 建议实践

1. **避免频繁提交编译产物到 Git 历史**
   - 使用 GitHub Actions artifact
   - 使用 Releases 而非 git commit

2. **使用 Git LFS 管理大文件**
   ```bash
   # 安装 Git LFS
   git lfs install
   
   # 跟踪大文件
   git lfs track "*.jar"
   ```

3. **定期清理旧 Release**
   - 已配置自动清理（3 天）
   - 手动清理：GitHub → Releases → 删除旧版本

---

## 🔍 监控和告警

### CI 日志检查

每次 CI 运行都会输出：
- 构建产物大小
- 大文件警告
- 上传状态

### 查询当前 Artifact

```bash
# 查看仓库 artifact（需要 gh CLI）
gh run list --repo RichardQidian/OpenClaw_Camera_Construction_Mgmt

# 查看特定运行的 artifact
gh run download <run-id> --repo RichardQidian/OpenClaw_Camera_Construction_Mgmt
```

### 查询 Releases

```bash
# 查看 Releases
gh release list --repo RichardQidian/OpenClaw_Camera_Construction_Mgmt

# 删除旧 Release
gh release delete build-<sha> --repo RichardQidian/OpenClaw_Camera_Construction_Mgmt
```

---

## 📝 变更日志

| 日期 | 变更内容 | 负责人 |
|------|---------|--------|
| 2026-03-30 | 统一 artifact 保留期为 3 天 | AI Assistant |
| 2026-03-30 | 添加大文件检查步骤 | AI Assistant |
| 2026-03-30 | 创建自动上传 Releases 工作流 | AI Assistant |
| 2026-03-30 | 创建本地上传脚本 | AI Assistant |

---

## ❓ 常见问题

### Q: 为什么不直接提交编译产物到 Git？

A: 编译产物通常较大且频繁变化，会：
- 快速增加仓库大小
- 影响 clone/pull 速度
- 产生大量无意义的提交历史

### Q: 如何下载最近的构建产物？

A: 三种方式：
1. GitHub Actions → 最近的运行 → Artifacts
2. GitHub Releases → 下载最新 Release
3. 本地运行 `gh run download`

### Q: 大文件告警如何处理？

A: 
1. 检查是否包含不必要的资源文件
2. 考虑使用外部存储（如 OSS、S3）
3. 优化构建配置减小产物大小

---

**相关文档：**
- [CI 工作流配置](../.github/workflows/)
- [Git 使用规范](./GIT_WORKFLOW.md)
- [部署指南](../deploy/DEPLOYMENT.md)
