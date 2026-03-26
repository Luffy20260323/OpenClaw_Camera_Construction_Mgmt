# Skill: frontend-debug

## 描述

前端问题排查技能。用于系统性地诊断和修复前端部署/运行问题。

**核心原则**：先确认架构，再动手修复。

---

## 触发条件

当用户报告前端问题时，如：
- "页面没反应"
- "按钮点击无效"
- "修改代码后没生效"
- "功能不工作"
- 前端相关的任何异常行为

---

## 排查流程（必须按顺序执行）

### 📌 第 1 步：确认部署架构（最重要！）

**不要假设！先检查！**

```bash
# 检查是否是 Docker 部署
docker ps | grep -E "nginx|frontend|web"

# 检查宿主机 nginx
ps aux | grep nginx

# 检查项目目录结构
ls -la <项目目录> | grep -i docker
```

**关键问题**：
- 前端运行在 Docker 容器还是宿主机？
- 如果是 Docker，容器名是什么？
- 部署目标目录是哪里？

---

### 📌 第 2 步：确认源码状态

```bash
cd <项目目录>
git status
git diff <相关文件>
```

**确认**：
- 代码是否已修改？
- 修改是否已提交？

---

### 📌 第 3 步：确认编译是否生效

```bash
cd <项目目录>/frontend
npm run build

# 检查编译输出的文件 hash
ls -la dist/assets/*.js | grep <相关组件>
```

**关键点**：
- Vite/Rollup 打包后文件名带 hash（如 `CompanyList-abc123.js`）
- 每次编译 hash 都会变化
- **如果 hash 没变，说明代码实际没变化**

---

### 📌 第 4 步：确认部署是否生效

**根据部署方式选择**：

#### Docker 部署
```bash
# 复制文件到容器
docker cp dist/. <容器名>:/usr/share/nginx/html/

# 重启容器
docker restart <容器名>

# 验证容器内文件
docker exec <容器名> ls -la /usr/share/nginx/html/assets/
```

#### 宿主机部署
```bash
# 复制到 nginx 目录
cp -r dist/* /var/www/html/

# 重载 nginx
nginx -s reload
```

---

### 📌 第 5 步：确认浏览器是否加载新文件

**指导用户在浏览器中检查**：

1. 打开开发者工具（F12）
2. 切换到 **Network（网络）** 面板
3. 刷新页面（Ctrl+Shift+R 强制刷新）
4. 查看加载的 JS 文件名

**或者在控制台执行**：
```javascript
// 检查当前页面加载的脚本
document.querySelectorAll('script[src]').forEach(s => console.log(s.src))
```

**关键点**：
- 浏览器可能缓存了旧的 `index.html`
- 旧的 `index.html` 引用旧的 JS 文件（hash 不同）
- **必须强制刷新（Ctrl+Shift+R）或清除缓存**

---

### 📌 第 6 步：最后才是代码逻辑调试

只有确认前面 5 步都没问题后，才开始调试代码逻辑：
- 检查控制台错误
- 检查事件绑定
- 检查组件状态
- 检查 API 响应

---

## 常见错误模式

### ❌ 错误 1：假设部署方式
> "我修改了代码，编译了，部署到 `/var/www/html` 了"
> → 但实际上是 Docker 部署，容器内的文件没更新

**正确做法**：先 `docker ps` 确认

---

### ❌ 错误 2：盲目修改代码
> 代码改来改去，但浏览器加载的一直是旧文件

**正确做法**：先在 Network 面板确认加载的文件 hash

---

### ❌ 错误 3：忽略缓存
> "我编译了新文件，但浏览器还是旧行为"

**正确做法**：强制刷新（Ctrl+Shift+R）或无痕模式测试

---

## 检查清单

排查前端问题时，按顺序勾选：

```markdown
- [ ] 1. 确认部署方式（Docker/宿主机/其他）
- [ ] 2. 确认容器名/部署目录
- [ ] 3. 确认源码已修改（git diff）
- [ ] 4. 确认编译已执行且 hash 变化
- [ ] 5. 确认文件已部署到正确位置
- [ ] 6. 确认浏览器加载了新文件（Network 面板）
- [ ] 7. 强制刷新浏览器
- [ ] 8. 最后才是代码逻辑调试
```

---

## 快速诊断命令

```bash
# 一键检查部署状态
echo "=== Docker 容器 ===" && docker ps | grep -E "nginx|frontend|web"
echo "=== 宿主机 nginx ===" && ps aux | grep nginx | grep -v grep
echo "=== 项目目录 ===" && ls -la | grep -i docker
```

---

## 案例参考

### 案例：按钮点击无反应

**问题**：用户报告"新建公司按钮点击没反应"

**错误排查**：
1. ❌ 直接修改代码添加对话框
2. ❌ 编译后部署到 `/var/www/html`
3. ❌ 用户反馈还是不行
4. ❌ 继续改代码...

**正确排查**：
1. ✅ `docker ps` → 发现是 Docker 部署
2. ✅ `docker cp` 部署到容器
3. ✅ `docker restart` 重启容器
4. ✅ 指导用户强制刷新
5. ✅ Network 面板确认加载新文件
6. ✅ 问题解决

**根本原因**：部署目标错误，容器内一直是旧代码

---

## 核心教训

> **如果一个问题长期修复不了，基本上是定位思路不对。**
> 
> **先确认架构，再动手修复。**
> 
> **不要假设，要验证。**

---

## 相关技能

- `docker` - Docker 容器操作
- `github` - 代码版本管理
- `nginx` - Web 服务器配置
