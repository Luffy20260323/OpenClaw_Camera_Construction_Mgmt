# 📦 构建指南 - Build Guide

> **重要说明：** 本项目采用**宿主机编译 + Docker 复制**的优化构建方式，避免每次重复下载依赖。

---

## ⚠️ 构建方式变更（2026-03-31）

### 之前的问题
- Docker 构建时每次都要下载 Maven/npm 依赖
- 构建时间长（5-15 分钟）
- 网络依赖严重，容易失败

### 现在的方案
- **宿主机编译** - 利用本地缓存的依赖
- **Docker 复制** - 只复制编译产物，秒级构建
- **构建时间** - 从 10 分钟降至 1-2 分钟

---

## 🚀 快速构建

### 方式一：使用构建脚本（推荐 ⭐）

```bash
# 构建后端
cd OpenClaw_Camera_Construction_Mgmt/backend
./build-docker.sh

# 构建前端
cd ../frontend
./build-docker.sh
```

### 方式二：手动构建

```bash
# 后端构建
cd OpenClaw_Camera_Construction_Mgmt/backend
mvn clean package -DskipTests
docker build -t camera-backend:latest .

# 前端构建
cd ../frontend
npm install  # 首次需要
npm run build
docker build -t camera-frontend:latest .
```

### 方式三：使用 docker-compose

```bash
# 先分别编译
cd backend && mvn clean package -DskipTests
cd ../frontend && npm run build

# 再构建并启动
cd ..
docker-compose build
docker-compose up -d
```

---

## 📁 构建脚本说明

### 后端构建脚本 `backend/build-docker.sh`

**功能：**
1. 在宿主机上编译 Java 项目（`mvn clean package -DskipTests`）
2. 构建 Docker 镜像（`docker build -t camera-backend:latest .`）
3. 显示镜像信息

**依赖缓存位置：** `~/.m2/repository`（永久保存，无需重复下载）

### 前端构建脚本 `frontend/build-docker.sh`

**功能：**
1. 检查并安装 node_modules（首次需要）
2. 构建 Vue 项目（`npm run build`）
3. 构建 Docker 镜像（`docker build -t camera-frontend:latest .`）

**依赖缓存位置：** `frontend/node_modules`（永久保存，无需重复下载）

---

## 🔧 Dockerfile 变更说明

### 后端 Dockerfile

**变更前：**
```dockerfile
# 多阶段构建，在 Docker 内编译
FROM maven:3.9.6-eclipse-temurin-17 AS builder
RUN mvn dependency:go-offline -B
RUN mvn clean package -DskipTests -B
```

**变更后：**
```dockerfile
# 直接从宿主机复制已编译的 jar
COPY target/*.jar app.jar
```

**影响：**
- ✅ 构建速度提升 80%
- ✅ 镜像体积减小 ~200MB
- ⚠️ **必须先编译再构建 Docker**

### 前端 Dockerfile

**变更前：**
```dockerfile
# 多阶段构建，在 Docker 内编译
FROM node:20-alpine AS builder
RUN npm ci
RUN npm run build
```

**变更后：**
```dockerfile
# 直接从宿主机复制已构建的 dist
COPY dist /usr/share/nginx/html
```

**影响：**
- ✅ 构建速度提升 90%
- ✅ 镜像体积减小 ~500MB
- ⚠️ **必须先构建再构建 Docker**

---

## ⚠️ 常见错误与解决方案

### 错误 1：Docker 构建失败 - 找不到 jar 文件

**错误信息：**
```
ERROR: failed to solve: COPY failed: file not found in build context: target/*.jar
```

**原因：** 未先在宿主机编译

**解决方案：**
```bash
cd backend
mvn clean package -DskipTests
docker build -t camera-backend:latest .
```

### 错误 2：Docker 构建失败 - 找不到 dist 目录

**错误信息：**
```
ERROR: failed to solve: COPY failed: file not found in build context: dist
```

**原因：** 未先在宿主机构建前端

**解决方案：**
```bash
cd frontend
npm run build
docker build -t camera-frontend:latest .
```

### 错误 3：浏览器缓存导致看不到更新

**现象：** 代码已更新，但浏览器显示的还是旧版本

**解决方案：**
1. 强制刷新浏览器：`Ctrl + Shift + R` (Windows) 或 `Cmd + Shift + R` (Mac)
2. 或清除浏览器缓存
3. 或使用无痕模式访问

### 错误 4：构建脚本提示权限不足

**错误信息：**
```
bash: ./build-docker.sh: Permission denied
```

**解决方案：**
```bash
chmod +x backend/build-docker.sh
chmod +x frontend/build-docker.sh
```

---

## 📊 构建时间对比

| 步骤 | 之前（Docker 内构建） | 现在（宿主机编译） | 提升 |
|------|---------------------|-------------------|------|
| **后端首次** | 5-10 分钟 | 2-3 分钟 | 60-70% |
| **后端后续** | 5-10 分钟 | 1-2 分钟 | 80% |
| **前端首次** | 3-5 分钟 | 1-2 分钟 | 50-60% |
| **前端后续** | 3-5 分钟 | 30 秒 | 90% |
| **Docker 构建** | 包含编译时间 | 秒级 | 95% |

---

## 🔄 完整部署流程

### 开发环境

```bash
# 1. 克隆项目
git clone https://github.com/RichardQidian/OpenClaw_Camera_Construction_Mgmt.git
cd OpenClaw_Camera_Construction_Mgmt

# 2. 配置环境变量
cp .env.example .env
# 编辑 .env 文件

# 3. 构建后端
cd backend
mvn clean package -DskipTests
docker build -t camera-backend:latest .

# 4. 构建前端
cd ../frontend
npm install
npm run build
docker build -t camera-frontend:latest .

# 5. 启动所有服务
cd ..
docker-compose up -d

# 6. 查看日志
docker-compose logs -f
```

### 生产环境

```bash
# 1. 拉取最新代码
git pull origin main

# 2. 重新编译
cd backend && mvn clean package -DskipTests
cd ../frontend && npm run build

# 3. 重新构建镜像
cd ..
docker-compose build

# 4. 重启服务
docker-compose down
docker-compose up -d

# 5. 验证服务
docker-compose ps
curl http://localhost/health
```

---

## 💡 最佳实践

### 1. 日常开发

```bash
# 修改后端代码后
cd backend
mvn clean package -DskipTests
docker-compose restart backend

# 修改前端代码后
cd frontend
npm run build
docker-compose restart frontend
```

### 2. 依赖更新后

```bash
# 后端 pom.xml 变更后
cd backend
mvn dependency:purge-local-repository -DmanualInclude=true -DskipTests
mvn clean package -DskipTests
docker build -t camera-backend:latest .

# 前端 package.json 变更后
cd frontend
rm -rf node_modules package-lock.json
npm install
npm run build
docker build -t camera-frontend:latest .
```

### 3. 清理旧镜像

```bash
# 删除悬空镜像
docker image prune -f

# 删除所有 camera- 开头的镜像
docker images | grep camera- | awk '{print $3}' | xargs docker rmi -f
```

---

## 📞 问题反馈

如遇到构建问题，请检查：

1. ✅ 是否已安装 JDK 17+ 和 Maven 3.9+
2. ✅ 是否已安装 Node.js 18+ 和 npm
3. ✅ 是否已先编译再构建 Docker
4. ✅ 是否已给脚本添加执行权限

---

**最后更新：** 2026-03-31  
**维护者：** 北京其点技术服务有限公司
