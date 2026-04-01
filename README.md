# 摄像头生命周期管理系统

Camera Lifecycle Management System

[![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)](https://github.com/RichardQidian/OpenClaw_Camera_Construction_Mgmt/releases/tag/v1.0.0)
[![License](https://img.shields.io/badge/license-Apache%202.0-green.svg)](LICENSE)

## 📋 项目简介

摄像头生命周期管理系统是一个聚焦**项目交付过程管理**和**最终结算管理**的多角色协作平台。系统支持甲方、乙方、监理三方协作，实现项目从开工到施工、验收、结算、运维的全生命周期管理。

---

## 🚀 快速开始

### 方式一：Docker 部署（推荐 ⭐）

**环境要求：**
- Docker 20.10+
- Docker Compose 2.0+
- JDK 17+（后端编译）
- Node.js 18+（前端编译）

**⚠️ 重要说明：** 本项目采用**宿主机编译 + Docker 复制**的优化构建方式，构建速度提升 80%。详见：[BUILD.md](BUILD.md)

**一键启动：**

```bash
# 1. 克隆项目
git clone https://github.com/RichardQidian/OpenClaw_Camera_Construction_Mgmt.git
cd OpenClaw_Camera_Construction_Mgmt

# 2. 配置环境变量
cp .env.example .env
# 编辑 .env 文件，修改数据库密码和 JWT 密钥

# 3. 构建后端（宿主机编译）
cd backend
mvn clean package -DskipTests
docker build -t camera-backend:latest .

# 4. 构建前端（宿主机编译）
cd ../frontend
npm install
npm run build
docker build -t camera-frontend:latest .

# 5. 启动所有服务
cd ..
docker-compose up -d

# 6. 查看日志
docker-compose logs -f

# 7. 访问系统
# 前端：http://localhost
# 后端 API：http://localhost:8080/api
# MinIO 控制台：http://localhost:9001
```

**或使用构建脚本（更简单）：**

```bash
cd backend && ./build-docker.sh
cd ../frontend && ./build-docker.sh
cd .. && docker-compose up -d
```

**停止服务：**

```bash
docker-compose down
```

**清除所有数据（谨慎使用）：**

```bash
docker-compose down -v
```

---

### 方式二：手动部署

**环境要求：**

| 软件 | 版本要求 | 说明 |
|------|----------|------|
| JDK | 17+ | Java 运行环境 |
| Maven | 3.9+ | Java 构建工具 |
| Node.js | 18+ | 前端运行环境 |
| PostgreSQL | 16+ | 数据库 |
| Redis | 7+ | 缓存 |
| Nginx | 1.20+ | Web 服务器 |

**部署步骤：**

```bash
# 1. 克隆项目
git clone https://github.com/RichardQidian/OpenClaw_Camera_Construction_Mgmt.git
cd OpenClaw_Camera_Construction_Mgmt

# 2. 配置环境变量
cp .env.example .env
# 编辑 .env 文件

# 3. 初始化数据库
sudo -u postgres createdb camera_construction_db
psql -d camera_construction_db < backend/src/main/resources/db/migration/V1__initial_schema.sql

# 4. 构建前端
cd frontend
npm install
npm run build

# 5. 构建后端
cd ../backend
mvn clean package -DskipTests

# 6. 启动后端
java -jar target/*.jar

# 7. 配置 Nginx
sudo cp ../deploy/nginx-http.conf /etc/nginx/sites-available/camera-system
sudo ln -sf /etc/nginx/sites-available/camera-system /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl reload nginx
```

详细部署指南请参考：[deploy/DEPLOYMENT.md](deploy/DEPLOYMENT.md)

---

## 📁 项目结构

```
OpenClaw_Camera_Construction_Mgmt/
├── backend/                    # 后端代码（Spring Boot）
│   ├── src/main/java/         # Java 源代码
│   ├── src/main/resources/    # 配置文件
│   ├── Dockerfile             # Docker 构建配置
│   └── pom.xml                # Maven 配置
├── frontend/                   # 前端代码（Vue 3）
│   ├── src/                   # Vue 源代码
│   ├── public/                # 静态资源
│   ├── Dockerfile             # Docker 构建配置
│   └── package.json           # NPM 配置
├── deploy/                     # 部署配置
│   ├── .env.example           # 环境变量模板
│   ├── nginx-*.conf           # Nginx 配置
│   └── DEPLOYMENT.md          # 部署指南
├── scripts/                    # 部署脚本
│   ├── init-server.sh         # 服务器初始化
│   ├── deploy.sh              # 一键部署
│   └── setup-https.sh         # HTTPS 配置
├── docs/                       # 项目文档
├── docker-compose.yml          # Docker Compose 配置
├── .env.example               # 环境变量模板
└── README.md                   # 本文件
```

---

## 🔧 技术栈

### 后端
- **框架：** Spring Boot 3.2.4
- **ORM：** MyBatis Plus 3.5.5
- **数据库：** PostgreSQL 16
- **缓存：** Redis 7
- **认证：** JWT
- **API 文档：** Knife4j

### 前端
- **框架：** Vue 3
- **构建工具：** Vite
- **UI 组件：** Element Plus
- **状态管理：** Pinia
- **路由：** Vue Router

### 运维
- **容器：** Docker + Docker Compose
- **Web 服务器：** Nginx
- **文件存储：** MinIO / 阿里云 OSS

---

## 🔐 默认账号

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| admin | Admin@2026 | 系统管理员 | 最高权限 |

**⚠️ 安全提醒：** 首次登录后请立即修改默认密码！

---

## 🌐 访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端 | http://localhost | Web 应用 |
| 后端 API | http://localhost:8080/api | REST API |
| API 文档 | http://localhost:8080/api/doc.html | Knife4j |
| MinIO | http://localhost:9000 | 对象存储 API |
| MinIO Console | http://localhost:9001 | 对象存储管理 |

---

## 📖 文档

| 文档 | 说明 |
|------|------|
| [部署指南](deploy/DEPLOYMENT.md) | 详细部署步骤和配置说明 |
| [用户手册](docs/用户手册_v1.1.0_2026-03-19.md) | 最终用户使用指南 |
| [维护手册](docs/维护手册_v1.1.0_2026-03-19.md) | 系统维护和故障排查 |
| [测试指南](docs/测试指南_v1.1.0_2026-03-19.md) | 测试用例和执行方法 |

---

## 🔒 安全配置

### 必须修改的配置

部署前请确保修改以下配置：

1. **数据库密码** - 在 `.env` 文件中设置 `DB_PASSWORD`
2. **JWT 密钥** - 在 `.env` 文件中设置 `JWT_SECRET`（使用强随机字符串）
3. **MinIO 密钥** - 在 `.env` 文件中设置 `MINIO_ACCESS_KEY` 和 `MINIO_SECRET_KEY`
4. **默认管理员密码** - 首次登录后立即修改

### 生成安全密钥

```bash
# 生成 JWT 密钥（32 位随机字符串）
openssl rand -base64 32

# 生成数据库密码
openssl rand -base64 16
```

---

## 🧪 测试

```bash
# 后端测试
cd backend
mvn test

# 前端测试
cd frontend
npm test
```

---

## 📝 更新日志

### v1.0.0 (2026-03-20)
- ✅ 完整的用户管理功能（注册、审批、批量导入）
- ✅ 角色管理和权限控制
- ✅ 公司管理和作业区管理
- ✅ 手机验证码登录
- ✅ Docker 一键部署
- ✅ 完整的部署文档和测试报告

[查看完整更新日志](docs/)

---

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

---

## 📄 许可证

Apache License 2.0

---

## 👥 开发团队

北京其点技术服务有限公司

## 📞 联系方式

- 邮箱：support@qidian.com
- 官网：https://www.qidian.com

---

**Copyright © 2026 北京其点技术服务有限公司**
