# 摄像头生命周期管理系统 - 版本基线

## 版本信息

- **版本号**: v1.0.0-baseline
- **基线日期**: 2026-03-14
- **基线时间**: 15:11 CST (GMT+8)
- **状态**: ✅ 稳定版本

## 核心功能清单

### 1. 认证与授权 ✅
- [x] 用户登录/登出
- [x] JWT Token 认证
- [x] 基于角色的权限控制（RBAC）
- [x] 用户审批流程
- [x] 匿名注册功能（可配置）

### 2. 用户管理 ✅
- [x] 用户 CRUD 操作
- [x] 用户批量导入（Excel）
- [x] 用户角色分配
- [x] 密码重置
- [x] 用户详情查看
- [x] 审批状态管理

### 3. 公司管理 ✅
- [x] 公司 CRUD 操作
- [x] 公司类型管理
- [x] 匿名注册标记配置
- [x] 系统保护公司（北京其点不可匿名注册）

### 4. 角色管理 ✅
- [x] 角色 CRUD 操作
- [x] 角色权限分配
- [x] 角色与公司类型关联

### 5. 作业区管理 ✅
- [x] 作业区 CRUD 操作
- [x] 作业区与公司关联

### 6. 个人中心 ✅
- [x] 个人信息修改
- [x] 密码修改

### 7. 系统管理 ✅
- [x] 系统配置管理

## 技术栈

### 后端
- **框架**: Spring Boot 3.2.4
- **语言**: Java 17
- **数据库**: PostgreSQL
- **ORM**: MyBatis-Plus
- **安全**: Spring Security + JWT
- **API 文档**: Knife4j (Swagger)

### 前端
- **框架**: Vue 3
- **构建工具**: Vite
- **UI 组件**: Element Plus
- **路由**: Vue Router
- **状态管理**: Pinia
- **HTTP 客户端**: Axios

### 基础设施
- **部署**: Linux (Ubuntu)
- **Web 服务器**: Tomcat (内嵌)
- **进程管理**: 后台运行 (nohup)

## 项目结构

```
/var/www/Camera_Construction_Project_Mgmt/
├── backend/                      # 后端项目
│   ├── src/main/java/
│   │   └── com/qidian/camera/
│   │       ├── module/
│   │       │   ├── auth/        # 认证模块
│   │       │   ├── user/        # 用户模块
│   │       │   ├── company/     # 公司模块
│   │       │   ├── role/        # 角色模块
│   │       │   └── workarea/    # 作业区模块
│   │       └── common/          # 公共模块
│   └── src/main/resources/
│       └── db/migration/        # 数据库迁移脚本
├── frontend/                     # 前端项目
│   └── src/
│       ├── views/               # 页面组件
│       ├── layouts/             # 布局组件
│       │   └── AdminLayout.vue  # 统一管理布局
│       ├── router/              # 路由配置
│       └── stores/              # 状态管理
└── VERSION.md                    # 版本基线文件
```

## 关键特性

### 统一布局 (AdminLayout)
所有管理页面使用统一的 AdminLayout 组件，包含：
- 顶部导航栏（紫色渐变背景）
  - 左侧：系统图标 + 名称
  - 右侧：返回首页按钮 + 用户信息 + 下拉菜单
- 主内容区（flex 布局）
- 底部版权声明

### 匿名注册控制
- 公司可配置"是否允许匿名注册"
- 系统保护的公司（如北京其点）永远不允许匿名注册
- 登录页注册只显示状态正常且允许匿名注册的公司

### 权限体系
- **系统管理员** (companyTypeId=4): 最高权限，可管理所有资源
- **公司管理员**: 管理本公司用户
- **普通用户**: 基础业务操作

## 数据库迁移

### 已应用的迁移
- V1__initial_schema.sql - 初始表结构
- V2__add_allow_anonymous_register.sql - 添加匿名注册标记

## 已知配置

### 默认管理员账户
- **用户名**: admin
- **角色**: 系统管理员
- **公司**: 北京其点技术服务有限公司

### 服务端口
- **后端 API**: http://localhost:8080/api
- **API 文档**: http://localhost:8080/doc.html
- **前端**: 静态文件部署

## 部署说明

### 后端启动
```bash
cd /var/www/Camera_Construction_Project_Mgmt/backend
nohup java -jar target/camera-lifecycle-system-1.0.0-SNAPSHOT.jar > /tmp/camera-system.log 2>&1 &
```

### 前端构建
```bash
cd /var/www/Camera_Construction_Project_Mgmt/frontend
npm run build
```

### 服务检查
```bash
tail -f /tmp/camera-system.log
# 看到 "Started CameraApplication" 表示启动成功
```

## 变更记录

### 2026-03-14 (v1.0.0-baseline)
- ✅ 创建 AdminLayout 统一布局组件
- ✅ 实现匿名注册功能及公司配置
- ✅ 修复用户管理页面角色字段显示问题
- ✅ 完善所有管理页面的导航和底部声明
- ✅ 修复首页路由跳转问题
- ✅ 恢复用户管理页面完整功能

## 下一步计划

### 待开发功能
- [ ] 项目管理模块
- [ ] 任务管理模块
- [ ] 进度管理模块
- [ ] 质量管理模块
- [ ] 结算管理模块
- [ ] 数据统计分析
- [ ] 消息通知系统
- [ ] 文件上传管理

### 优化项
- [ ] 性能优化（缓存、索引）
- [ ] 安全加固（XSS、CSRF 防护）
- [ ] 日志审计
- [ ] 监控告警
- [ ] 单元测试覆盖

## 联系方式

- **开发团队**: 北京其点技术服务有限公司
- **技术支持**: 系统管理员

---

**此版本为基线版本，后续所有开发将基于此版本进行。**

**生成时间**: 2026-03-14 15:11:00 CST
