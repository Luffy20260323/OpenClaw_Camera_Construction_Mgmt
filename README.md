# 摄像头生命周期管理系统

Camera Lifecycle Management System

## 📁 项目结构

```
/var/www/Camera_Construction_Project_Mgmt/
├── backend/          # 后端代码（Spring Boot + MyBatis Plus）
├── frontend/         # 前端代码（Vue 3 + Vite + Element Plus）
├── docs/            # 项目文档
└── scripts/         # 部署脚本
```

## 🚀 快速部署

### 前端部署

```bash
# 方式 1：使用部署脚本
/var/www/Camera_Construction_Project_Mgmt/scripts/deploy-frontend.sh

# 方式 2：手动部署
cd /var/www/Camera_Construction_Project_Mgmt/frontend
npm run build
sudo nginx -s reload
```

### 后端部署

```bash
cd /var/www/Camera_Construction_Project_Mgmt/backend
mvn clean package
# 运行 jar 包或使用 mvn spring-boot:run
```

## 🔧 技术栈

### 后端
- Spring Boot 3.2.4
- MyBatis Plus 3.5.5
- PostgreSQL
- Redis
- JWT 认证

### 前端
- Vue 3
- Vite
- Element Plus
- Pinia (状态管理)
- Vue Router

## 📋 功能模块

### 已实现
- ✅ 用户登录/认证
- ✅ 用户管理（创建、审批、批量导入）
- ✅ 角色管理
- ✅ 公司管理
- ✅ 作业区管理
- ✅ 系统配置

### 用户管理功能
1. **三种注册方式**
   - 用户自主注册（需审批）
   - 管理员手动创建
   - 管理员批量导入

2. **分级审批**
   - 系统管理员可审批所有用户
   - 甲方管理员只能审批甲方用户
   - 乙方管理员只能审批乙方用户
   - 监理方管理员只能审批监理方用户

3. **权限控制**
   - 管理员只能创建/管理自己公司范围内的用户

## 🔐 默认账号

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| admin | Admin@2026 | 系统管理员 | 最高权限 |

## 🌐 访问地址

- **前端**: http://localhost/
- **API 文档**: http://localhost/doc.html
- **后端 API**: http://localhost/api/

## 📖 详细文档

查看 `docs/` 目录下的详细文档：
- 用户管理功能说明.md
- 用户管理功能实现总结.md
- test-user-management.sql

## 🔧 Nginx 配置

配置文件：`/etc/nginx/sites-available/camera-system`

主要配置：
- 静态文件：`/var/www/Camera_Construction_Project_Mgmt/frontend/dist`
- API 代理：`http://localhost:8080/api/`

## 📝 更新日志

### 2026-03-11
- ✅ 实现用户管理功能
- ✅ 添加三种用户注册方式
- ✅ 实现分级审批流程
- ✅ 添加批量导入功能
- ✅ 创建用户管理前端页面
- ✅ 规范项目目录结构

---

**Copyright © 2026 北京其点技术服务有限公司**
