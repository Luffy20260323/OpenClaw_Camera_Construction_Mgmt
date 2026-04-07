# 🔧 修改 Bug 指南 - Bug Fix Guide

> **目标**: 快速定位和修复 Bug，提高开发效率  
> **适用**: 开发人员、系统维护人员  
> **最后更新**: 2026-04-05

---

## 📋 目录

1. [项目概览](#1-项目概览)
2. [源代码组织结构](#2-源代码组织结构)
3. [部署模式与运行模式](#3-部署模式与运行模式)
4. [Bug 定位流程](#4-bug 定位流程)
5. [常见 Bug 类型与修复方法](#5-常见 bug 类型与修复方法)
6. [修改 Bug 的最佳实践](#6-修改 bug 的最佳实践)

---

## 1. 项目概览

### 1.1 系统简介

**摄像头生命周期管理系统** - 聚焦项目交付过程管理和最终结算管理的多角色协作平台。

- **甲方、乙方、监理** 三方协作
- **从开工到施工、验收、结算、运维** 全生命周期管理
- **技术栈**: Spring Boot 3.2.4 + Vue 3 + PostgreSQL 16 + Redis 7

### 1.2 项目位置

```
项目根目录：/root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/
GitHub 仓库：RichardQidian/OpenClaw_Camera_Construction_Mgmt
```

---

## 2. 源代码组织结构

### 2.1 顶层目录结构

```
OpenClaw_Camera_Construction_Mgmt/
├── backend/                    # 后端代码 (Spring Boot + Java)
├── frontend/                   # 前端代码 (Vue 3 + Vite)
├── deploy/                     # 部署配置 (Nginx, Docker)
├── scripts/                    # 部署脚本
├── docs/                       # 项目文档
├── config/                     # 配置文件
├── docker-compose.yml          # Docker 编排配置
├── .env                        # 环境变量 (敏感信息)
├── TASK_BOARD.md               # 任务看板
└── README.md                   # 项目说明
```

### 2.2 后端目录结构 (backend/)

```
backend/
├── src/main/java/com/qidian/camera/
│   ├── CameraApplication.java          # 主启动类
│   │
│   ├── common/                         # 公共模块
│   │   ├── constants/                  # 常量定义
│   │   ├── exception/                  # 异常处理
│   │   ├── logging/                    # 日志处理
│   │   └── response/                   # 统一响应格式
│   │
│   ├── config/                         # 全局配置
│   │
│   └── module/                         # 业务模块 (按功能划分)
│       ├── auth/                       # 认证授权 (JWT, 权限，数据权限)
│       │   ├── annotation/             # 注解定义
│       │   ├── aspect/                 # AOP 切面
│       │   ├── config/                 # 安全配置
│       │   ├── constant/               # 认证常量
│       │   ├── controller/             # 认证接口
│       │   ├── dto/                    # 数据传输对象
│       │   ├── entity/                 # 实体类
│       │   ├── filter/                 # 过滤器
│       │   ├── interceptor/            # 拦截器
│       │   ├── mapper/                 # MyBatis Mapper
│       │   ├── service/                # 服务层
│       │   │   └── impl/               # 服务实现
│       │   └── util/                   # 工具类
│       │
│       ├── menu/                       # 菜单管理
│       │   ├── controller/
│       │   ├── dto/
│       │   ├── entity/
│       │   ├── mapper/
│       │   └── service/
│       │
│       ├── resource/                   # 资源管理 (权限核心)
│       │   ├── controller/
│       │   ├── dto/
│       │   ├── entity/
│       │   ├── mapper/
│       │   └── service/
│       │
│       ├── role/                       # 角色管理
│       ├── user/                       # 用户管理
│       ├── company/                    # 公司管理
│       ├── workarea/                   # 作业区管理
│       ├── component/                  # 零部件管理
│       ├── document/                   # 文档管理
│       ├── pointdevicemodel/           # 点位设备模型
│       ├── pointdevicemodelinstance/   # 点位设备模型实例
│       └── pointbatchassignment/       # 点位批量分配
│
├── src/main/resources/
│   ├── application.yml                 # Spring Boot 配置
│   ├── db/migration/                   # 数据库迁移脚本 (Flyway)
│   │   └── V1__xxx.sql, V2__xxx.sql...
│   └── mapper/                         # MyBatis XML 映射文件
│
├── target/                             # 编译输出 (jar 包)
├── pom.xml                             # Maven 配置
├── Dockerfile                          # Docker 构建配置
└── build-docker.sh                     # 构建脚本
```

### 2.3 前端目录结构 (frontend/)

```
frontend/
├── src/
│   ├── main.js                         # 入口文件
│   ├── App.vue                         # 根组件
│   │
│   ├── api/                            # API 调用模块
│   │   ├── auth.js                     # 认证 API
│   │   ├── menu.js                     # 菜单 API
│   │   ├── permission.js               # 权限 API
│   │   ├── resource.js                 # 资源 API
│   │   ├── role.js                     # 角色 API
│   │   ├── user-permission.js          # 用户权限 API
│   │   ├── dataPermission.js           # 数据权限 API
│   │   ├── componentType.js            # 零部件类型 API
│   │   ├── componentInstance.js        # 零部件实例 API
│   │   ├── pointDeviceModel.js         # 点位设备模型 API
│   │   ├── pointDeviceModelInstance.js # 点位设备模型实例 API
│   │   └── ...
│   │
│   ├── components/                     # 公共组件
│   │   ├── DataScopeSelector.vue       # 数据范围选择器
│   │   ├── GlobalSearch.vue            # 全局搜索
│   │   ├── ParentResourceSelector.vue  # 父资源选择器
│   │   ├── PermissionTree.vue          # 权限树
│   │   ├── RefreshMenu.vue             # 刷新菜单按钮
│   │   ├── ResourceSelector.vue        # 资源选择器
│   │   ├── RoleSelector.vue            # 角色选择器
│   │   └── SidebarMenu.vue             # 侧边栏菜单
│   │
│   ├── views/                          # 页面组件
│   │   ├── Login.vue                   # 登录页
│   │   ├── Home.vue                    # 首页
│   │   ├── NotFound.vue                # 404 页
│   │   ├── company/                    # 公司管理页面
│   │   ├── role/                       # 角色管理页面
│   │   ├── user/                       # 用户管理页面
│   │   ├── workarea/                   # 作业区管理页面
│   │   └── system/                     # 系统管理页面
│   │
│   ├── router/                         # 路由配置
│   │   └── index.js
│   │
│   ├── stores/                         # 状态管理 (Pinia)
│   │   ├── user.js                     # 用户状态
│   │   ├── permission.js               # 权限状态
│   │   ├── role.js                     # 角色状态
│   │   ├── resource.js                 # 资源状态
│   │   └── user-permission.js          # 用户权限状态
│   │
│   ├── directives/                     # 自定义指令
│   │   └── permission.js               # 权限指令 v-permission
│   │
│   ├── constants/                      # 常量定义
│   │   └── roles.js
│   │
│   ├── utils/                          # 工具函数
│   │   ├── request.js                  # Axios 封装
│   │   └── permission.js               # 权限工具
│   │
│   ├── layouts/                        # 布局组件
│   ├── assets/                         # 静态资源
│   ├── config/                         # 配置文件
│   └── __tests__/                      # 测试文件
│
├── public/                             # 公共静态文件
├── dist/                               # 构建输出
├── package.json                        # NPM 配置
├── vite.config.js                      # Vite 配置
├── Dockerfile                          # Docker 构建配置
└── build-docker.sh                     # 构建脚本
```

### 2.4 关键文件清单

| 文件路径 | 作用 | 修改频率 |
|---------|------|---------|
| `backend/src/main/java/com/qidian/camera/module/auth/interceptor/DataPermissionInterceptor.java` | 数据权限拦截器 | 中 |
| `backend/src/main/java/com/qidian/camera/module/auth/service/PermissionService.java` | 权限服务核心 | 高 |
| `backend/src/main/java/com/qidian/camera/module/menu/service/MenuService.java` | 菜单服务 | 高 |
| `frontend/src/directives/permission.js` | 前端权限指令 | 中 |
| `frontend/src/stores/permission.js` | 权限状态管理 | 中 |
| `frontend/src/components/SidebarMenu.vue` | 侧边栏菜单组件 | 高 |
| `frontend/src/router/index.js` | 路由配置 | 中 |
| `backend/src/main/resources/db/migration/V*.sql` | 数据库迁移脚本 | 高 |

---

## 3. 部署模式与运行模式

### 3.1 部署模式：Docker Compose

系统采用 **Docker Compose** 进行容器化部署，包含以下服务：

| 服务名 | 容器名 | 镜像 | 端口 | 说明 |
|--------|--------|------|------|------|
| postgres | camera-postgres | postgres:16-alpine | 5432 | PostgreSQL 数据库 |
| redis | camera-redis | redis:7-alpine | 6379 | Redis 缓存 |
| minio | camera-minio | minio/minio | 9000/9001 | 对象存储 (可选) |
| backend | camera-backend | camera-backend:latest | 8080 | Spring Boot 后端 |
| frontend | camera-frontend | camera-frontend:latest | 80 | Nginx 前端 |

### 3.2 运行模式

```
┌─────────────┐     HTTPS/HTTP      ┌─────────────┐
│   浏览器     │ ──────────────────► │    Nginx    │
│  (用户)     │                     │  (端口 80)   │
└─────────────┘                     └──────┬──────┘
                                           │
                          ┌────────────────┼────────────────┐
                          │                │                │
                          ▼                ▼                ▼
                   ┌───────────┐   ┌───────────┐   ┌───────────┐
                   │ 静态文件   │   │ API 代理    │   │ 其他路由   │
                   │ /var/www  │   │ /api/*    │   │           │
                   │           │   │ →8080     │   │           │
                   └───────────┘   └─────┬─────┘   └───────────┘
                                         │
                                         ▼
                                  ┌─────────────┐
                                  │ Spring Boot │
                                  │  (端口 8080) │
                                  └──────┬──────┘
                                         │
                    ┌────────────────────┼────────────────────┐
                    │                    │                    │
                    ▼                    ▼                    ▼
             ┌───────────┐       ┌───────────┐       ┌───────────┐
             │PostgreSQL │       │   Redis   │       │   MinIO   │
             │ (端口 5432)│       │ (端口 6379)│      │ (端口 9000)│
             └───────────┘       └───────────┘       └───────────┘
```

### 3.3 关键配置文件

**docker-compose.yml** (项目根目录):
```yaml
services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: camera_construction_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    ports:
      - "5432:5432"
  
  backend:
    build: ./backend
    ports:
      - "8080:8080"
    depends_on:
      - postgres
      - redis
  
  frontend:
    build: ./frontend
    ports:
      - "80:80"
    depends_on:
      - backend
```

### 3.4 启动/停止命令

```bash
# 进入项目目录
cd /root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt

# 启动所有服务
docker-compose up -d

# 停止所有服务
docker-compose down

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f backend
docker-compose logs -f frontend

# 重启单个服务
docker-compose restart backend
```

### 3.5 构建流程 (重要！)

**⚠️ 采用宿主机编译 + Docker 复制方式，构建速度提升 80%**

```bash
# 后端构建 (必须先编译再构建 Docker)
cd backend
mvn clean package -DskipTests        # 宿主机编译
docker build -t camera-backend:latest .  # Docker 复制

# 前端构建 (必须先构建再构建 Docker)
cd frontend
npm run build                        # 宿主机构建
docker build -t camera-frontend:latest .  # Docker 复制

# 使用构建脚本 (推荐)
cd backend && ./build-docker.sh
cd frontend && ./build-docker.sh
```

---

## 4. Bug 定位流程

### 4.1 快速诊断流程图

```
用户报告 Bug
    │
    ▼
┌─────────────────────────────────┐
│ 1. 复现问题                      │
│ - 什么操作触发？                 │
│ - 错误现象是什么？               │
│ - 有无错误信息？                 │
└───────────────┬─────────────────┘
                │
                ▼
┌─────────────────────────────────┐
│ 2. 判断问题类型                  │
│                                 │
│ ┌─────────────┐ ┌─────────────┐ │
│ │ 前端问题    │ │ 后端问题    │ │
│ │ - 页面不显示│ │ - API 报错   │ │
│ │ - 样式错乱  │ │ - 登录失败  │ │
│ │ - 按钮无效  │ │ - 数据错误  │ │
│ │ - 权限不生效│ │ - 数据库异常│ │
│ └──────┬──────┘ └──────┬──────┘ │
│        │                │        │
│        ▼                ▼        │
│   查前端日志         查后端日志   │
│   F12 控制台          docker logs │
└───────────────┬─────────────────┘
                │
                ▼
┌─────────────────────────────────┐
│ 3. 定位具体模块                  │
│ - 根据功能判断所属模块           │
│ - 查看相关源代码                 │
│ - 检查数据库数据                 │
└───────────────┬─────────────────┘
                │
                ▼
┌─────────────────────────────────┐
│ 4. 修复并验证                    │
│ - 修改代码                       │
│ - 重新构建                       │
│ - 测试验证                       │
└─────────────────────────────────┘
```

### 4.2 前端问题排查

**步骤 1: 打开浏览器开发者工具**
```
按 F12 → Console 标签 → 查看错误信息
按 F12 → Network 标签 → 查看 API 请求
```

**步骤 2: 检查常见错误**

| 错误类型 | 可能原因 | 排查方法 |
|---------|---------|---------|
| `401 Unauthorized` | Token 过期/无效 | 检查登录状态，刷新 Token |
| `403 Forbidden` | 权限不足 | 检查用户权限配置 |
| `404 Not Found` | API 路径错误/资源不存在 | 检查 API 路径和资源 ID |
| `500 Internal Server Error` | 后端异常 | 查看后端日志 |
| 页面空白 | JS 错误/路由错误 | 查看 Console 错误 |
| 样式丢失 | CSS 加载失败 | 检查 Network 中 CSS 文件 |
| 按钮点击无反应 | 事件绑定失败/权限指令 | 检查 v-permission 指令 |

**步骤 3: 查看前端日志**
```bash
# Docker 容器日志
docker logs camera-frontend

# 或实时查看
docker-compose logs -f frontend
```

### 4.3 后端问题排查

**步骤 1: 查看后端日志**
```bash
# 查看最近 100 行日志
docker logs camera-backend --tail 100

# 实时查看日志
docker-compose logs -f backend

# 搜索错误
docker logs camera-backend 2>&1 | grep -i "error"
docker logs camera-backend 2>&1 | grep -i "exception"
```

**步骤 2: 检查 API 接口**
```bash
# 测试健康检查
curl http://localhost:8080/api/health

# 测试登录接口
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin@2026"}'

# 测试菜单接口 (需要 Token)
curl http://localhost:8080/api/menu/tree \
  -H "Authorization: Bearer <your-token>"
```

**步骤 3: 检查数据库**
```bash
# 进入数据库容器
docker exec -it camera-postgres psql -U postgres -d camera_construction_db

# 或直接连接
psql -h localhost -U postgres -d camera_construction_db

# 常用查询
SELECT * FROM sys_user WHERE username = 'admin';
SELECT * FROM sys_role WHERE role_code = 'ADMIN';
SELECT * FROM sys_resource WHERE resource_type = 'MENU';
SELECT * FROM sys_permission WHERE permission_code LIKE '%menu%';
```

**步骤 4: 检查 Redis 缓存**
```bash
# 进入 Redis 容器
docker exec -it camera-redis redis-cli

# 查看缓存键
KEYS *

# 查看权限缓存
GET permission:admin

# 清除所有缓存 (谨慎使用)
FLUSHALL
```

### 4.4 权限问题专项排查

**权限体系是高频问题区域**，按以下步骤排查：

```
1. 确认用户是否有对应权限
   → 查询 sys_user_permission 表

2. 确认权限是否分配给角色
   → 查询 sys_role_permission 表

3. 确认权限是否存在
   → 查询 sys_permission 表

4. 确认资源是否有父资源
   → 查询 sys_resource.parent_id

5. 确认菜单是否显示
   → 检查权限码是否匹配
   → 检查菜单状态是否启用

6. 清除权限缓存
   → Redis: DEL permission:<user_id>
```

**SQL 查询示例**:
```sql
-- 查询用户所有权限
SELECT p.permission_code, p.permission_name
FROM sys_user_permission up
JOIN sys_permission p ON up.permission_id = p.id
WHERE up.user_id = (SELECT id FROM sys_user WHERE username = 'admin');

-- 查询角色所有权限
SELECT p.permission_code, p.permission_name
FROM sys_role_permission rp
JOIN sys_permission p ON rp.permission_id = p.id
WHERE rp.role_id = (SELECT id FROM sys_role WHERE role_code = 'ADMIN');

-- 查询菜单资源
SELECT r.id, r.resource_code, r.resource_name, r.parent_id, r.display_order
FROM sys_resource r
WHERE r.resource_type = 'MENU'
ORDER BY r.display_order;
```

---

## 5. 常见 Bug 类型与修复方法

### 5.1 菜单不显示

**现象**: 登录后侧边栏菜单为空或部分菜单不显示

**可能原因**:
1. 用户没有对应菜单的权限
2. 菜单资源未配置父资源
3. 菜单状态为禁用
4. 权限缓存未更新

**排查步骤**:
```sql
-- 1. 检查菜单资源是否存在
SELECT * FROM sys_resource WHERE resource_type = 'MENU';

-- 2. 检查菜单的父资源配置
SELECT id, resource_code, resource_name, parent_id, display_order
FROM sys_resource
WHERE resource_type = 'MENU'
ORDER BY parent_id, display_order;

-- 3. 检查用户权限
SELECT * FROM sys_user_permission WHERE user_id = 1;

-- 4. 检查权限是否关联菜单
SELECT p.permission_code, r.resource_code
FROM sys_permission p
JOIN sys_resource r ON p.resource_id = r.id
WHERE r.resource_type = 'MENU';
```

**修复方法**:
1. 在资源管理页面配置菜单的父资源
2. 给用户或角色分配对应权限
3. 清除权限缓存：`docker exec camera-redis redis-cli DEL permission:<user_id>`
4. 前端点击"刷新菜单"按钮

### 5.2 按钮点击无反应

**现象**: 按钮可见但点击后无任何反应

**可能原因**:
1. `v-permission` 指令权限校验失败
2. 按钮绑定的方法未定义
3. API 调用失败但未显示错误

**排查步骤**:
```javascript
// 1. 检查按钮的权限指令
<el-button v-permission="'system:user:add'">添加</el-button>

// 2. 查看浏览器 Console 是否有权限警告

// 3. 检查用户是否有该权限
SELECT * FROM sys_permission WHERE permission_code = 'system:user:add';
```

**修复方法**:
1. 给用户分配对应权限
2. 检查前端方法绑定
3. 添加错误处理逻辑

### 5.3 API 返回 500 错误

**现象**: 接口调用返回 500 Internal Server Error

**排查步骤**:
```bash
# 1. 查看后端日志
docker logs camera-backend --tail 50 | grep -A 20 "ERROR"

# 2. 定位异常堆栈
# 查找 Exception 或 Error 关键字

# 3. 检查数据库连接
docker exec camera-postgres pg_isready
```

**常见原因**:
- 空指针异常 (NPE)
- 数据库约束冲突
- SQL 语法错误
- 事务回滚

### 5.4 登录失败

**现象**: 输入正确账号密码但登录失败

**排查步骤**:
```sql
-- 1. 检查用户是否存在
SELECT * FROM sys_user WHERE username = 'admin';

-- 2. 检查用户状态
SELECT id, username, status, is_deleted FROM sys_user;

-- 3. 检查密码是否正确 (对比加密后的密码)
```

```bash
# 4. 查看认证日志
docker logs camera-backend 2>&1 | grep -i "auth"
```

### 5.5 数据不显示/显示错误

**现象**: 列表页面数据为空或显示错误数据

**排查步骤**:
```sql
-- 1. 直接查询数据库验证
SELECT * FROM <table_name> LIMIT 10;

-- 2. 检查数据权限配置
-- 用户可能只能看到部分数据

-- 3. 检查 API 返回数据
# Network 标签查看 API 响应
```

---

## 6. 修改 Bug 的最佳实践

### 6.1 修改前准备

1. **备份当前代码**
   ```bash
   cd /root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt
   git status
   git diff > bugfix_backup.patch
   ```

2. **确认问题可复现**
   - 记录复现步骤
   - 截图/录屏错误现象
   - 保存错误日志

3. **定位问题根源**
   - 不要只修复表面现象
   - 找到根本原因再动手

### 6.2 修改代码

**后端修改**:
```java
// 修改位置：backend/src/main/java/com/qidian/camera/module/xxx/

// 1. 添加日志便于调试
log.debug("DEBUG: userId={}, param={}", userId, param);
log.error("ERROR: 详细错误信息", exception);

// 2. 添加空值检查
if (obj == null) {
    throw new BusinessException("对象不存在");
}

// 3. 添加异常处理
try {
    // 业务逻辑
} catch (Exception e) {
    log.error("操作失败", e);
    throw new BusinessException("操作失败：" + e.getMessage());
}
```

**前端修改**:
```vue
<!-- 修改位置：frontend/src/views/xxx/ 或 frontend/src/components/xxx/ -->

<!-- 1. 添加调试输出 -->
<script setup>
const handleClick = () => {
  console.log('DEBUG: 按钮被点击', params)
  // ...
}
</script>

<!-- 2. 添加错误处理 -->
<script setup>
const fetchData = async () => {
  try {
    const res = await api.getData()
    data.value = res.data
  } catch (error) {
    console.error('ERROR: 获取数据失败', error)
    ElMessage.error('获取数据失败：' + error.message)
  }
}
</script>
```

### 6.3 重新构建与部署

**后端**:
```bash
cd backend
mvn clean package -DskipTests
docker build -t camera-backend:latest .
docker-compose restart backend
docker-compose logs -f backend  # 查看日志验证
```

**前端**:
```bash
cd frontend
npm run build
docker build -t camera-frontend:latest .
docker-compose restart frontend
# 强制刷新浏览器 Ctrl+Shift+R
```

### 6.4 验证修复

1. **复现原问题** - 确认问题是否解决
2. **回归测试** - 确认没有引入新问题
3. **检查日志** - 确认没有新的错误
4. **清理缓存** - 清除浏览器缓存和 Redis 缓存

### 6.5 提交代码

```bash
# 1. 查看修改
git status
git diff

# 2. 提交代码
git add .
git commit -m "fix: 修复 XXX 问题

- 问题描述：...
- 修复方案：...
- 影响范围：..."

# 3. 推送到远程
git push origin main
```

---

## 📞 快速参考

### 常用命令速查

```bash
# 服务管理
docker-compose up -d              # 启动
docker-compose down               # 停止
docker-compose ps                 # 状态
docker-compose logs -f backend    # 后端日志
docker-compose logs -f frontend   # 前端日志

# 数据库
docker exec -it camera-postgres psql -U postgres -d camera_construction_db

# Redis
docker exec -it camera-redis redis-cli

# 构建
cd backend && mvn clean package -DskipTests && docker build -t camera-backend:latest .
cd frontend && npm run build && docker build -t camera-frontend:latest .
```

### 关键路径速查

| 类型 | 路径 |
|------|------|
| 后端代码 | `backend/src/main/java/com/qidian/camera/module/` |
| 前端代码 | `frontend/src/views/` `frontend/src/components/` |
| API 定义 | `frontend/src/api/` |
| 数据库迁移 | `backend/src/main/resources/db/migration/` |
| 配置文件 | `backend/src/main/resources/application.yml` |
| Docker 配置 | `docker-compose.yml` |

---

**最后更新**: 2026-04-05  
**维护者**: 北京其点技术服务有限公司
