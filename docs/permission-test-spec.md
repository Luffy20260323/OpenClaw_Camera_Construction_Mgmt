# 权限体系测试规范

> 文档版本：v1.0  
> 最后更新：2026-04-01  
> 状态：待执行  
> 关联设计文档：[permission-design-spec.md](./permission-design-spec.md)

---

## 目录

1. [测试范围](#1-测试范围)
2. [测试策略](#2-测试策略)
3. [测试环境](#3-测试环境)
4. [测试数据准备](#4-测试数据准备)
5. [测试执行流程](#5-测试执行流程)
6. [缺陷管理](#6-缺陷管理)
7. [测试交付物](#7-测试交付物)

---

## 1. 测试范围

### 1.1 功能模块覆盖

| 模块 | 测试内容 | 优先级 |
|------|---------|--------|
| 资源管理 | 资源增删改查、资源树查询、基本权限保护 | P1 |
| 角色权限计算 | 角色权限计算公式验证、基本/缺省权限管理 | P1 |
| 用户权限计算 | 用户权限继承、多角色权限合并、个人权限调整 | P1 |
| 权限调整 | ADD/REMOVE 逻辑、基本权限不可调整、临时权限 | P1 |
| API 权限校验 | 接口权限拦截、permission_key 校验、缓存机制 | P1 |
| 审计日志 | 权限变更记录、操作追溯、日志查询 | P2 |

### 1.2 核心 Entity 测试覆盖

| Entity | 测试重点 |
|--------|---------|
| `Resource` | 资源类型、树形结构、permission_key 唯一性、基本权限标识 |
| `RolePermission` | 权限类型（basic/default）、角色 - 资源关联唯一性 |
| `RolePermissionAdjustment` | ADD/REMOVE 操作、调整记录唯一性 |
| `UserPermissionAdjustment` | 用户个人调整、临时权限过期时间 |
| `PermissionAuditLog` | 审计日志完整性、操作追溯 |

### 1.3 核心 Service 测试覆盖

| Service | 测试重点 |
|---------|---------|
| `PermissionService` | 权限计算公式、缓存机制、权限判断方法 |
| `ResourceService` | 资源树构建、菜单树过滤、基本权限查询 |
| `PermissionAuditLogService` | 日志记录、日志查询 |

### 1.4 核心 Controller 测试覆盖

| Controller | 测试重点 |
|------------|---------|
| `ResourceController` | 资源 CRUD、基本权限删除保护 |
| `RolePermissionController` | 角色权限树查询、权限调整、批量调整 |
| `UserPermissionController` | 用户权限查询、权限标识获取、菜单树过滤 |

---

## 2. 测试策略

### 2.1 测试类型

| 测试类型 | 说明 | 工具 |
|---------|------|------|
| 单元测试 | 测试 Service 层核心逻辑 | JUnit 5 + Mockito |
| 集成测试 | 测试 Controller + Service + Mapper 链路 | Spring Boot Test + TestContainers |
| API 测试 | 测试 REST API 接口 | Postman / RestAssured |
| 端到端测试 | 完整业务流程验证 | Playwright |

### 2.2 测试层级

```
┌─────────────────────────────────────┐
│         E2E 测试 (5%)               │  完整业务流程
├─────────────────────────────────────┤
│       API 集成测试 (25%)            │  接口契约验证
├─────────────────────────────────────┤
│       Service 单元测试 (70%)        │  核心业务逻辑
└─────────────────────────────────────┘
```

### 2.3 重点测试场景

#### 2.3.1 权限计算公式验证

**角色权限计算公式**：
```
角色完整权限 = role_permissions(basic + default) 
            + role_permission_adjustments(ADD)
            - role_permission_adjustments(REMOVE)
```

**用户权限计算公式**：
```
用户完整权限 = ∑(各角色的完整权限) 
            + user_permission_adjustments(ADD)
            - user_permission_adjustments(REMOVE)
```

**验证点**：
- 基本权限始终存在
- 缺省权限可被调整移除
- ADD 调整增加权限
- REMOVE 调整移除权限（非基本权限）
- 多角色权限正确合并（并集）
- 用户个人调整优先级高于角色继承

#### 2.3.2 基本权限保护

**验证点**：
- 基本权限资源不可删除（`is_basic=1`）
- 基本权限不可通过调整移除
- 新建角色自动分配基本权限

#### 2.3.3 权限调整 ADD/REMOVE 逻辑

**验证点**：
- 同一资源重复 ADD 保持 ADD 状态
- 同一资源重复 REMOVE 保持 REMOVE 状态
- ADD 后再 REMOVE 等效于无调整
- REMOVE 后再 ADD 等效于无调整
- 调整记录删除后恢复原状态

#### 2.3.4 用户权限继承 + 个人调整

**验证点**：
- 用户继承所有角色的权限（并集）
- 用户个人 ADD 增加权限
- 用户个人 REMOVE 移除权限（包括角色继承的）
- 临时权限过期时间生效
- 多角色权限冲突正确处理

#### 2.3.5 权限缓存一致性

**验证点**：
- 用户权限变更后缓存清除
- 角色权限变更后相关用户缓存清除
- 缓存命中时返回正确数据
- 缓存未命中时从数据库加载并缓存

---

## 3. 测试环境

### 3.1 环境配置

| 配置项 | 值 |
|--------|-----|
| JDK 版本 | 17+ |
| Spring Boot | 3.x |
| 数据库 | MySQL 8.0+ |
| 缓存 | Redis 6.x+ |
| 测试框架 | JUnit 5 + Spring Boot Test |

### 3.2 测试数据库

**要求**：
- 使用独立测试数据库，与生产/开发环境隔离
- 每次测试前执行 schema 初始化
- 测试后清理测试数据（或使用事务回滚）

**初始化脚本**：
```sql
-- 1. 创建测试数据库
CREATE DATABASE IF NOT EXISTS camera_auth_test;

-- 2. 执行 schema 初始化
SOURCE /path/to/db-schema-2026-03-24.sql;

-- 3. 执行测试数据初始化
SOURCE /path/to/test-data-init.sql;
```

### 3.3 测试配置文件

**application-test.yml**：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/camera_auth_test?useSSL=false&serverTimezone=Asia/Shanghai
    username: test_user
    password: test_password
  redis:
    host: localhost
    port: 6379
    database: 1  # 使用独立数据库避免污染

mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl  # 测试时开启 SQL 日志
```

---

## 4. 测试数据准备

### 4.1 基础数据

#### 4.1.1 基本权限资源

```sql
INSERT INTO resource (name, code, type, permission_key, is_basic, status) VALUES
('登录', 'login', 'PERMISSION', 'system:login', 1, 1),
('退出', 'logout', 'PERMISSION', 'system:logout', 1, 1),
('查看个人信息', 'profile:view', 'PERMISSION', 'profile:view', 1, 1),
('修改个人密码', 'profile:password', 'PERMISSION', 'profile:password', 1, 1);
```

#### 4.1.2 测试角色

```sql
INSERT INTO role (name, code, type, status) VALUES
('超级管理员', 'admin', 'SYSTEM', 1),
('普通用户', 'user', 'DEFAULT', 1),
('测试角色 A', 'test_role_a', 'CUSTOM', 1),
('测试角色 B', 'test_role_b', 'CUSTOM', 1);
```

#### 4.1.3 测试用户

```sql
INSERT INTO user (username, password, real_name, status, is_admin) VALUES
('admin', '$2a$10$...', '系统管理员', 1, 1),
('user1', '$2a$10$...', '测试用户 1', 1, 0),
('user2', '$2a$10$...', '测试用户 2', 1, 0),
('user3', '$2a$10$...', '测试用户 3', 1, 0);
```

#### 4.1.4 用户 - 角色关联

```sql
-- user1: 只拥有 test_role_a
INSERT INTO user_role (user_id, role_id, created_by) VALUES (2, 3, 1);

-- user2: 拥有 test_role_a 和 test_role_b（测试多角色）
INSERT INTO user_role (user_id, role_id, created_by) VALUES (3, 3, 1);
INSERT INTO user_role (user_id, role_id, created_by) VALUES (3, 4, 1);

-- user3: 只拥有 test_role_b
INSERT INTO user_role (user_id, role_id, created_by) VALUES (4, 4, 1);
```

### 4.2 角色权限配置

#### 4.2.1 基本权限分配

```sql
-- 所有角色都分配基本权限
INSERT INTO role_permission (role_id, resource_id, permission_type, created_by)
SELECT r.id, res.id, 'basic', 1
FROM role r
CROSS JOIN resource res
WHERE res.is_basic = 1;
```

#### 4.2.2 缺省权限分配

```sql
-- 为测试角色分配缺省权限
-- test_role_a: 资源 10, 11, 12
INSERT INTO role_permission (role_id, resource_id, permission_type, created_by) VALUES
(3, 10, 'default', 1),
(3, 11, 'default', 1),
(3, 12, 'default', 1);

-- test_role_b: 资源 13, 14, 15
INSERT INTO role_permission (role_id, resource_id, permission_type, created_by) VALUES
(4, 13, 'default', 1),
(4, 14, 'default', 1),
(4, 15, 'default', 1);
```

### 4.3 权限调整数据

#### 4.3.1 角色权限调整

```sql
-- test_role_a: 增加资源 20，移除资源 11
INSERT INTO role_permission_adjustment (role_id, resource_id, action, created_by) VALUES
(3, 20, 'ADD', 1),
(3, 11, 'REMOVE', 1);
```

#### 4.3.2 用户权限调整

```sql
-- user2: 个人增加资源 30，个人移除资源 13（从 test_role_b 继承的）
INSERT INTO user_permission_adjustment (user_id, resource_id, action, created_by) VALUES
(3, 30, 'ADD', 1),
(3, 13, 'REMOVE', 1);

-- user3: 临时权限（测试过期）
INSERT INTO user_permission_adjustment (user_id, resource_id, action, created_by, expire_at) VALUES
(4, 40, 'ADD', 1, DATE_ADD(NOW(), INTERVAL 1 DAY));
```

### 4.4 测试资源树

```
资源树结构：
├── 用户管理模块 (id=1, MODULE)
│   ├── 用户列表菜单 (id=2, MENU)
│   │   ├── 用户列表页面 (id=3, PAGE)
│   │   │   ├── 新增按钮 (id=4, ELEMENT, permission_key=user:create)
│   │   │   ├── 编辑按钮 (id=5, ELEMENT, permission_key=user:update)
│   │   │   ├── 删除按钮 (id=6, ELEMENT, permission_key=user:delete)
│   │   │   └── 导出按钮 (id=7, ELEMENT, permission_key=user:export)
│   │   └── API
│   │       ├── GET /api/users (id=8, API, permission_key=user:view)
│   │       ├── POST /api/users (id=9, API, permission_key=user:create)
│   │       ├── PUT /api/users/{id} (id=10, API, permission_key=user:update)
│   │       └── DELETE /api/users/{id} (id=11, API, permission_key=user:delete)
├── 订单管理模块 (id=20, MODULE)
│   └── 订单列表菜单 (id=21, MENU)
│       └── ...
└── 系统管理模块 (id=30, MODULE)
    └── 系统设置菜单 (id=31, MENU)
        └── ...
```

---

## 5. 测试执行流程

### 5.1 测试准备阶段

1. **环境检查**
   - [ ] 测试数据库可用
   - [ ] Redis 缓存可用
   - [ ] 测试配置文件加载
   - [ ] 测试依赖包安装

2. **数据初始化**
   - [ ] 执行 Schema 初始化
   - [ ] 执行基础数据插入
   - [ ] 执行测试数据插入
   - [ ] 验证数据完整性

3. **服务启动**
   - [ ] 启动测试环境应用
   - [ ] 验证健康检查接口
   - [ ] 验证数据库连接
   - [ ] 验证缓存连接

### 5.2 测试执行顺序

```
1. 单元测试（Service 层）
   ├── PermissionService 测试
   ├── ResourceService 测试
   └── PermissionAuditLogService 测试

2. 集成测试（Controller 层）
   ├── ResourceController 测试
   ├── RolePermissionController 测试
   └── UserPermissionController 测试

3. API 测试（端到端）
   ├── 资源管理 API
   ├── 角色权限 API
   └── 用户权限 API

4. 缓存测试
   ├── 缓存命中测试
   ├── 缓存清除测试
   └── 缓存一致性测试
```

### 5.3 测试通过标准

| 测试类型 | 通过率要求 | 说明 |
|---------|-----------|------|
| 单元测试 | 100% | 所有单元测试必须通过 |
| 集成测试 | 100% | 所有集成测试必须通过 |
| API 测试 | ≥95% | 允许少量非阻塞性问题 |
| E2E 测试 | ≥90% | 核心流程必须通过 |

### 5.4 测试退出标准

- [ ] 所有 P1 优先级测试用例通过
- [ ] P2 优先级测试用例通过率 ≥ 95%
- [ ] 无严重（Critical）缺陷
- [ ] 无主要（Major）缺陷未解决
- [ ] 代码覆盖率 ≥ 80%

---

## 6. 缺陷管理

### 6.1 缺陷等级定义

| 等级 | 说明 | 响应时间 |
|------|------|---------|
| Critical | 核心功能失效、数据丢失、安全漏洞 | 立即修复 |
| Major | 主要功能异常、权限计算错误 | 24 小时内 |
| Minor | 次要功能异常、UI 问题 | 版本内修复 |
| Trivial | 文档错误、拼写错误 | 酌情修复 |

### 6.2 缺陷报告模板

```markdown
### 缺陷标题

**缺陷 ID**：PERM-XXX  
**优先级**：P1/P2/P3  
**严重程度**：Critical/Major/Minor/Trivial  

**前置条件**：
- 条件 1
- 条件 2

**复现步骤**：
1. 步骤 1
2. 步骤 2

**预期结果**：
- 结果 1

**实际结果**：
- 结果 1

**环境信息**：
- 版本：v1.0
- 数据库：MySQL 8.0
- 测试时间：2026-04-01

**附件**：
- 日志截图
- API 响应数据
```

---

## 7. 测试交付物

### 7.1 测试文档

| 文档名称 | 说明 |
|---------|------|
| `permission-test-spec.md` | 测试规范（本文档） |
| `permission-test-cases.md` | 详细测试用例 |
| `permission-test-report.md` | 测试执行报告 |

### 7.2 测试代码

| 文件/目录 | 说明 |
|----------|------|
| `PermissionServiceTest.java` | Service 层单元测试 |
| `ResourceControllerTest.java` | Controller 层集成测试 |
| `PermissionApiTest.java` | API 端到端测试 |
| `test-data-init.sql` | 测试数据初始化脚本 |

### 7.3 测试报告

**测试报告内容**：
- 测试执行概况（通过率、覆盖率）
- 缺陷统计（按等级、模块分布）
- 风险评估
- 上线建议

---

## 附录

### A. 测试命令

```bash
# 运行所有单元测试
mvn test -Dtest=PermissionServiceTest

# 运行所有集成测试
mvn test -Dtest=*ControllerTest

# 运行所有权限相关测试
mvn test -Dtest=*Permission*Test

# 生成测试覆盖率报告
mvn clean test jacoco:report
```

### B. 相关文档

- [权限设计文档](./permission-design-spec.md)
- [API 文档](./Knife4j_API 文档_文档中心.md)
- [数据库设计](./permission-system-design-v3.md)

---

> 文档维护：测试团队  
> 最后审核：待审核
