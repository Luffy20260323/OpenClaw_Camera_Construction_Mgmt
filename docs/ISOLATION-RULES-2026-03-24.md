# 代码隔离规则

**生效时间：** 2026-03-24 15:50 GMT+8  
**状态：** ✅ 已执行

---

## 🚫 禁止操作

### 1. 禁止从 master 同步

```bash
# ❌ 绝对不要执行
git pull origin master
git merge origin/master
git rebase origin/master
git checkout master
```

### 2. 禁止提交到 master

```bash
# ❌ 绝对不要执行
git push origin master
git push origin HEAD:master
```

### 3. 禁止强制推送

```bash
# ❌ 绝对不要执行
git push --force
git push --force-with-lease
```

---

## ✅ 允许操作

### 1. 在当前分支开发

```bash
# ✅ 安全操作
git checkout camera1001-fixed
git add .
git commit -m "fix: xxx"
git push origin camera1001-fixed
```

### 2. 创建新分支

```bash
# ✅ 安全操作
git checkout -b feature/xxx
git push origin feature/xxx
```

### 3. 读取 master 进行对比

```bash
# ✅ 安全操作（只读）
git fetch origin master
git diff origin/master
```

---

## 📋 当前状态

| 项目 | 状态 | 说明 |
|------|------|------|
| 当前分支 | `camera1001-fixed` | ✅ 基于本地修复 |
| 与 master 关系 | 独立 | ✅ 不同步 |
| GitHub Token | 已配置 | ✅ 用于读取对比 |
| 数据库备份 | ✅ 已完成 | `docs/db-*.sql` |
| 配置备份 | ✅ 已完成 | `docs/.env.backup-*` |

---

## 🔒 保护机制

### .gitignore 规则

```gitignore
# 敏感配置文件（绝对不要提交）
.env
.env.local
*.env
*.env.backup
docs/.env.backup-*

# 数据库备份
*.sql
docs/db-*.sql

# 密钥文件
*.key
*.pem
*.crt
secrets/
```

### Git 配置

```bash
# 查看当前分支
git branch

# 查看远程仓库
git remote -v

# 查看未提交的修改
git status
```

---

## ⚠️ 违规后果

如果违反隔离规则：
1. 可能覆盖本地修复
2. 可能导致系统无法使用
3. 需要重新执行所有修复

---

**隔离规则已生效！请严格遵守！** 🔒
