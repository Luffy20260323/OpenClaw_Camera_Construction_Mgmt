# GitHub Actions 存储空间管理指南

## 📊 当前配置

| 资源类型 | 配额 | 已用 | 可用 |
|---------|------|------|------|
| Actions 存储空间 | 500 MB | - | - |
| Actions 分钟数 | 2000 分钟/月 | - | - |

## ⚠️ 问题原因

1. **默认 artifact 保留 90 天** - GitHub 默认设置
2. **每次 CI 上传多个 artifact**：
   - backend-jar (~50MB)
   - frontend-dist (~30MB)
   - api-test-results (~1MB)
   - playwright-report (~10MB)
   - backend-log (~5MB)
3. **频繁提交触发 CI** - 每次 push 都运行完整测试

## ✅ 已优化配置

### 1. 减少保留时间

```yaml
# 之前
retention-days: 30

# 现在
retention-days: 1  # 构建产物
retention-days: 3  # 测试报告
retention-days: 7  # 日志文件（仅失败时）
```

### 2. 条件上传

```yaml
# 仅手动触发时上传构建产物
if: github.event_name == 'workflow_dispatch'

# 仅失败时上传日志
if: failure() && github.event_name == 'workflow_dispatch'
```

### 3. 预期效果

| 项目 | 优化前 | 优化后 | 节省 |
|------|--------|--------|------|
| 每次 CI 存储占用 | ~100MB | ~10MB | 90% |
| 每月存储占用（按 50 次 CI） | 5000MB | 500MB | 90% |
| artifact 保留时间 | 30 天 | 1-7 天 | - |

## 🧹 清理旧 artifact

### 手动清理

```bash
# 访问 GitHub 页面手动删除
https://github.com/RichardQidian/OpenClaw_Camera_Construction_Mgmt/actions
```

### 自动清理脚本

```bash
# 清理 7 天前的所有 workflow runs
bash scripts/cleanup-artifacts.sh
```

### 使用 GitHub API 批量删除

```bash
# 删除所有超过 7 天的 artifact
gh run list --limit 500 --json databaseId,createdAt \
  | jq -r '.[] | select(.createdAt < "2026-03-15") | .databaseId' \
  | xargs -I {} gh run delete {}
```

## 📈 监控存储空间

### 查看存储使用量

1. 访问：https://github.com/settings/billing
2. 查看 "Actions" 部分
3. 查看 "Storage" 使用量

### 设置存储警告

GitHub 目前没有存储警告功能，建议：
- 每周检查一次存储使用量
- 当使用量 > 80% 时手动清理

## 🎯 最佳实践

### 1. 减少 artifact 上传

```yaml
# ❌ 不好 - 每次都上传
- uses: actions/upload-artifact@v4
  with:
    name: my-artifact
    path: ./dist

# ✅ 好 - 仅手动触发时上传
- uses: actions/upload-artifact@v4
  if: github.event_name == 'workflow_dispatch'
  with:
    name: my-artifact
    path: ./dist
```

### 2. 缩短保留时间

```yaml
# ❌ 不好 - 保留 30 天
retention-days: 30

# ✅ 好 - 保留 1-7 天
retention-days: 1  # 日常构建
retention-days: 7  # 重要发布
```

### 3. 使用缓存替代 artifact

```yaml
# ✅ 使用缓存（不计入存储配额）
- uses: actions/cache@v4
  with:
    path: ~/.m2/repository
    key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}
```

### 4. 定期清理

```bash
# 添加到 crontab，每周清理一次
0 2 * * 0 /path/to/cleanup-artifacts.sh
```

## 🔧 故障排查

### CI 超时或失败

**症状**：CI 运行几秒就失败，提示存储不足

**解决**：
1. 清理旧 artifact
2. 检查当前存储使用量
3. 优化 workflow 配置

### 无法上传 artifact

**症状**：`Error: Unable to upload artifact`

**解决**：
1. 检查存储配额是否已满
2. 删除不必要的 artifact
3. 减少 artifact 大小或数量

## 📚 相关资源

- [GitHub Actions 存储配额](https://docs.github.com/en/billing/managing-billing-for-github-actions/about-billing-for-github-actions#storage-minutes-and-storage)
- [actions/upload-artifact](https://github.com/actions/upload-artifact)
- [清理 GitHub Actions artifact](https://docs.github.com/en/actions/managing-workflow-runs/deleting-a-workflow-run)

---

*最后更新：2026-03-22*
*维护者：OpenClaw Team*
