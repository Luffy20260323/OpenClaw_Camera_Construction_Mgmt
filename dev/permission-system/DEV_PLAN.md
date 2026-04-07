# 权限管理系统开发任务规划

**项目**：Camera 项目权限管理系统 V4.3  
**启动时间**：2026-04-04 11:23  
**预计完成**：2026-04-11（7 天）

---

## 📋 任务总览

| 阶段 | 任务 | 优先级 | 预计时间 | 状态 |
|------|------|--------|----------|------|
| Phase 1 | 数据库迁移 | P0 | 1 天 | 待开始 |
| Phase 2 | 后端服务开发 | P0 | 2 天 | 待开始 |
| Phase 3 | 前端页面开发 | P0 | 2 天 | 待开始 |
| Phase 4 | 权限缓存优化 | P1 | 1 天 | 待开始 |
| Phase 5 | 测试验证 | P0 | 1 天 | 待开始 |

---

## 🎯 Phase 1：数据库迁移（1 天）

### 任务清单

| 序号 | 任务 | 说明 | 优先级 | 状态 |
|------|------|------|--------|------|
| 1.1 | 创建 migration 脚本 | 创建 Flyway 迁移脚本 | P0 | 待开始 |
| 1.2 | 创建 resource 表 | 从旧表迁移数据 | P0 | 待开始 |
| 1.3 | 创建 permission 表 | 原 role_resource 表 | P0 | 待开始 |
| 1.4 | 创建 user_permission_adjust 表 | 原 user_resource_adjust 表 | P0 | 待开始 |
| 1.5 | 创建 role_data_scope 表 | 角色数据范围 | P0 | 待开始 |
| 1.6 | 创建 user_data_scope 表 | 用户数据范围 | P0 | 待开始 |
| 1.7 | 创建审计日志表 | 2 张审计表 | P0 | 待开始 |
| 1.8 | 创建软删除触发器 | 级联软删除 | P0 | 待开始 |
| 1.9 | 创建循环引用检查触发器 | 防止循环引用 | P0 | 待开始 |
| 1.10 | 创建两种管理员角色 | ROLE_SUPER_ADMIN/ROLE_SYSTEM_ADMIN | P0 | 待开始 |
| 1.11 | 验证数据迁移 | 检查数据完整性 | P0 | 待开始 |

**执行顺序**：1.1 → 1.2 → 1.3 → 1.4 → 1.5 → 1.6 → 1.7 → 1.8 → 1.9 → 1.10 → 1.11

---

## 🔧 Phase 2：后端服务开发（2 天）

### 任务清单

| 序号 | 任务 | 说明 | 优先级 | 状态 |
|------|------|------|--------|------|
| 2.1 | 创建 Entity 类 | Resource, Permission, Role 等 | P0 | 待开始 |
| 2.2 | 创建 Mapper 接口 | MyBatis Mapper | P0 | 待开始 |
| 2.3 | 创建 Service 接口 | 业务逻辑接口 | P0 | 待开始 |
| 2.4 | 创建 Service 实现 | 业务逻辑实现 | P0 | 待开始 |
| 2.5 | 创建 Controller | REST API 控制器 | P0 | 待开始 |
| 2.6 | 实现资源扫描器 | 自动从代码提取 API | P1 | 待开始 |
| 2.7 | 实现权限码生成器 | 自动生成权限码 | P1 | 待开始 |
| 2.8 | 实现循环引用检查 | wouldCreateCycle() 方法 | P0 | 待开始 |
| 2.9 | 实现数据范围服务 | DataScopeService | P0 | 待开始 |
| 2.10 | 实现权限缓存服务 | PermissionCacheService | P0 | 待开始 |
| 2.11 | 实现审计日志服务 | PermissionAuditService | P0 | 待开始 |
| 2.12 | 配置权限拦截器 | PermissionInterceptor | P0 | 待开始 |
| 2.13 | 配置 Web MVC | 注册拦截器 | P0 | 待开始 |
| 2.14 | 编写单元测试 | Service 层测试 | P1 | 待开始 |

**执行顺序**：
```
2.1 (Entity) → 2.2 (Mapper) → 2.3/2.4 (Service) → 2.5 (Controller)
→ 2.12/2.13 (拦截器配置) → 2.8 (循环引用检查) → 2.9 (数据范围)
→ 2.10 (缓存) → 2.11 (审计) → 2.6/2.7 (扫描器/生成器) → 2.14 (测试)
```

---

## 🎨 Phase 3：前端页面开发（2 天）

### 任务清单

| 序号 | 任务 | 说明 | 优先级 | 状态 |
|------|------|------|--------|------|
| 3.1 | 创建 API 封装 | resource.js, permission.js 等 | P0 | 待开始 |
| 3.2 | 创建 Pinia Store | usePermissionStore, useUserStore | P0 | 待开始 |
| 3.3 | 开发 PermissionTree 组件 | 权限树形选择器 | P0 | 待开始 |
| 3.4 | 开发 ParentResourceSelector 组件 | 父资源选择器 | P0 | 待开始 |
| 3.5 | 开发资源列表页面 | /system/resource | P0 | 待开始 |
| 3.6 | 开发孤儿资源页面 | /system/resource/orphaned | P0 | 待开始 |
| 3.7 | 开发角色列表页面 | /system/role | P0 | 待开始 |
| 3.8 | 开发角色权限配置页面 | /system/role/:id/permission | P0 | 待开始 |
| 3.9 | 开发角色数据范围页面 | /system/role/:id/data-scope | P0 | 待开始 |
| 3.10 | 开发用户权限配置页面 | /system/user/:id/permission | P0 | 待开始 |
| 3.11 | 开发审计日志页面 | /system/audit/* | P1 | 待开始 |
| 3.12 | 开发刷新菜单功能 | 全局导航栏按钮 | P0 | 待开始 |
| 3.13 | 配置路由 | router/index.js | P0 | 待开始 |
| 3.14 | 开发权限指令 | v-permission, v-menu-permission | P0 | 待开始 |

**执行顺序**：
```
3.1 (API) → 3.2 (Store) → 3.13 (路由) → 3.14 (指令)
→ 3.3/3.4 (组件) → 3.5 (资源列表) → 3.6 (孤儿资源)
→ 3.7 (角色列表) → 3.8 (角色权限) → 3.9 (角色数据范围)
→ 3.10 (用户权限) → 3.12 (刷新菜单) → 3.11 (审计日志)
```

---

## ⚡ Phase 4：权限缓存优化（1 天）

| 序号 | 任务 | 说明 | 优先级 | 状态 |
|------|------|------|--------|------|
| 4.1 | 实现 Redis 缓存 | 权限缓存、菜单缓存 | P0 | 待开始 |
| 4.2 | 实现缓存失效逻辑 | 权限变更时自动失效 | P0 | 待开始 |
| 4.3 | 实现手动刷新功能 | 清除缓存按钮 | P1 | 待开始 |
| 4.4 | 性能测试 | 响应时间 < 50ms | P0 | 待开始 |

---

## 🧪 Phase 5：测试验证（1 天）

| 序号 | 任务 | 说明 | 优先级 | 状态 |
|------|------|------|--------|------|
| 5.1 | 单元测试 | Service 层测试 | P0 | 待开始 |
| 5.2 | 集成测试 | API 接口测试 | P0 | 待开始 |
| 5.3 | 前端测试 | 页面功能测试 | P0 | 待开始 |
| 5.4 | 循环引用测试 | 验证防止逻辑 | P0 | 待开始 |
| 5.5 | 性能测试 | 缓存命中率、响应时间 | P0 | 待开始 |
| 5.6 | 编写测试报告 | 测试用例和结果 | P0 | 待开始 |

---

## 🚀 子任务启动计划

### 立即启动（T+0）

| 子任务 | 执行者 | 说明 |
|--------|--------|------|
| **Phase 1 数据库迁移** | 子代理 1 | 创建 Flyway 迁移脚本 |
| **Phase 2.1-2.5 后端基础** | 子代理 2 | Entity/Mapper/Service/Controller |
| **Phase 3.1-3.2 前端基础** | 子代理 3 | API 封装 + Pinia Store |

### 第二阶段（T+1 天）

| 子任务 | 执行者 | 说明 |
|--------|--------|------|
| Phase 2.6-2.14 后端进阶 | 子代理 2 | 扫描器、缓存、审计 |
| Phase 3.3-3.7 前端组件 | 子代理 3 | 公共组件 + 资源/角色页面 |

### 第三阶段（T+2 天）

| 子任务 | 执行者 | 说明 |
|--------|--------|------|
| Phase 3.8-3.14 前端进阶 | 子代理 3 | 权限配置页面 |
| Phase 4 缓存优化 | 子代理 2 | Redis 缓存实现 |
| Phase 5 测试验证 | 子代理 1 | 测试用例执行 |

---

## 📊 进度追踪

### 关键里程碑

| 里程碑 | 预计时间 | 验收标准 |
|--------|----------|----------|
| M1：数据库迁移完成 | Day 1 结束 | 10 张表创建完成，数据迁移验证通过 |
| M2：后端 API 完成 | Day 3 结束 | 所有 REST API 可调用，单元测试通过 |
| M3：前端页面完成 | Day 5 结束 | 所有页面可访问，功能正常 |
| M4：缓存优化完成 | Day 6 结束 | 响应时间 < 50ms |
| M5：测试验证完成 | Day 7 结束 | 所有测试用例通过 |

### 汇报机制

- **每日汇报**：每个子代理每日汇报进展
- **里程碑汇报**：完成每个里程碑时汇报
- **问题上报**：遇到阻塞问题立即上报

---

## 📁 输出物清单

### 数据库
- [ ] Flyway 迁移脚本：`V4.3__permission_system.sql`
- [ ] 回滚脚本：`V4.3__undo_permission_system.sql`

### 后端
- [ ] Entity 类：Resource, Permission, Role, UserRole, etc.
- [ ] Mapper 接口：ResourceMapper, PermissionMapper, etc.
- [ ] Service 类：ResourceService, PermissionService, DataScopeService, etc.
- [ ] Controller 类：ResourceController, PermissionController, etc.
- [ ] 配置类：WebMvcConfig, PermissionInterceptor
- [ ] 单元测试：*ServiceTest.java

### 前端
- [ ] API 封装：resource.js, permission.js, role.js, user.js, data-scope.js, audit.js
- [ ] Store：usePermissionStore.js, useUserStore.js
- [ ] 组件：PermissionTree.vue, ParentResourceSelector.vue, etc.
- [ ] 页面：ResourceList.vue, RolePermission.vue, etc.
- [ ] 指令：permission.js, menu-permission.js
- [ ] 路由配置：router/index.js

### 文档
- [ ] API 文档：Swagger/OpenAPI
- [ ] 测试报告：test-report.md
- [ ] 部署文档：deployment.md

---

**任务规划完成，准备启动子代理！**
