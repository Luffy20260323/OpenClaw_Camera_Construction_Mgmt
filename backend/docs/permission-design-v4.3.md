# Camera 项目权限设计方案 V4.3

**版本**：V4.3  
**创建日期**：2026-04-04  
**提出人**：柳生  
**状态**：待审核

---

## 📋 文档修订历史

| 版本 | 日期 | 修订内容 | 修订人 |
|------|------|----------|--------|
| V1.0 | 2026-03-27 | 初始权限控制方案（47 个权限，22 个角色） | AI |
| V2.0 | 2026-03-28 | 增加权限缓存、DataScope AOP | AI |
| V3.0 | 2026-04-02 | ELEMENT 层级权限设计 | AI |
| V4.0 | 2026-04-04 | 权限驱动开发流程规范（8 步流程） | 柳生/AI |
| V4.1 | 2026-04-04 | 合并 resource 表和 permission 表 | 柳生/AI |
| **V4.3** | **2026-04-04** | **资源与权限分离设计 + 审计日志 + 软删除** | **柳生/AI** |

---

## 目录

1. [核心理念](#一核心理念)
2. [权限驱动开发流程（8 步）](#二权限驱动开发流程 8 步)
3. [资源与权限模型](#三资源与权限模型)
4. [权限码命名规范](#四权限码命名规范)
5. [数据库表结构](#五数据库表结构)
6. [两种管理员角色设计](#六两种管理员角色设计)
7. [数据范围权限设计](#七数据范围权限设计)
8. [权限继承与冲突处理](#八权限继承与冲突处理)
9. [后端实现方案](#九后端实现方案)
10. [前端实现方案](#十前端实现方案)
11. [权限缓存机制](#十一权限缓存机制)
12. [审计日志设计](#十二审计日志设计)
13. [实施步骤](#十三实施步骤)

---

## 一、核心理念

### 1.1 核心原则

**资源与权限分离**：
- **资源（Resource）**：系统中可访问的对象（菜单、页面、按钮、API）
- **权限（Permission）**：角色/用户对资源的访问权（通过关联表体现）
- **权限码（permission_key）**：资源的唯一标识符，用于后端注解

**开发与权限配置分离**：新功能开发时先完成功能和不带父资源的资源收集，开发完成后再通过配置建立权限层级关系。

### 1.2 设计目标

| 目标 | 说明 |
|------|------|
| **开发友好** | 新功能开发不受权限配置影响，可独立进行 |
| **配置灵活** | 开发完成后，管理员可灵活配置权限层级和分配 |
| **实时生效** | 权限变更后无需退出系统，实时刷新菜单 |
| **高性能** | 权限验证响应时间 < 50ms（缓存命中） |
| **可追溯** | 所有权限配置操作记录审计日志 |
| **语义清晰** | 资源表存储资源，关联表存储权限，概念分离 |

### 1.3 关键设计原则

1. **资源孤儿状态**：新功能开发阶段，资源无父资源，便于收集和管理
2. **admin 超级权限**：admin 用户始终拥有全部权限，否则是数据配置错误
3. **菜单显示逻辑**：用户是否具备菜单下关联资源的权限决定菜单是否显示
4. **权限缓存机制**：用户权限数据缓存 + 手动/自动刷新机制
5. **资源与权限分离**：
   - `resource` 表：存储资源定义（什么是可授予的）
   - `permission` 表：存储权限关系（谁拥有什么）
6. **软删除机制**：资源删除时级联软删除子资源，保留审计历史

---

## 二、权限驱动开发流程（8 步）

### 2.1 流程总览

```
┌─────────────────────────────────────────────────────────────────┐
│                    权限驱动开发流程（8 步）                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  开发阶段（1-4）              运行阶段（5-8）                    │
│  ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐   ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐ │
│  │Step1│→│Step2│→│Step3│→│Step4│   │Step5│→│Step6│→│Step7│→│Step8│ │
│  └─────┘ └─────┘ └─────┘ └─────┘   └─────┘ └─────┘ └─────┘ └─────┘ │
│   功能开发  权限生成  admin 授权  资源树构建   资源配置   admin 验证  权限分配  用户验证  │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 详细步骤

#### 第 1 步：开发阶段 - 功能开发与资源收集

**工作内容**：
- 完成功能开发（页面 URL、布局、元素、数据库、API）
- 实时更新 `resource` 表（增删改），资源此时**无父资源**（孤儿状态）
- 资源类型包括：API、PAGE、ELEMENT

**产出**：
- 功能代码
- 资源数据（孤儿状态，parent_id = NULL）

**示例**：
```sql
-- 开发阶段插入的资源（无父资源）
INSERT INTO resource (name, code, type, parent_id, permission_key, module_code) VALUES
  ('用户列表页面', 'page_user_list', 'PAGE', NULL, 'system:user:list:page', 'system'),
  ('查看详情按钮', 'elem_user_view', 'ELEMENT', NULL, 'system:user:view:button', 'system'),
  ('新建用户按钮', 'elem_user_create', 'ELEMENT', NULL, 'system:user:create:button', 'system'),
  ('获取用户列表 API', 'api_user_list', 'API', NULL, 'system:user:list:api', 'system');
```

---

#### 第 2 步：开发阶段 - 权限代码生成

**工作内容**：
- 为 PAGE、ELEMENT、API 生成权限代码
- 权限代码按编码规则生成，保证唯一性
- 权限码直接存储在 `resource` 表的 `permission_key` 字段

**权限码格式**：
```
module:resource:action:type
```

**示例**：
```sql
-- 权限码在插入时直接生成
INSERT INTO resource (name, code, type, permission_key, module_code) VALUES
  ('用户列表页面', 'page_user_list', 'PAGE', 'system:user:list:page', 'system'),
  ('查看用户详情按钮', 'elem_user_view', 'ELEMENT', 'system:user:view:button', 'system'),
  ('新建用户按钮', 'elem_user_create', 'ELEMENT', 'system:user:create:button', 'system'),
  ('获取用户列表 API', 'api_user_list', 'API', 'system:user:list:api', 'system');
```

---

#### 第 3 步：开发阶段 - Admin 授权

**工作内容**：
- 将新生成的权限（资源）全部分配给 admin 用户
- 操作 `permission` 表
- 确保 admin 用户登录后可访问新功能

**两种管理员角色设计**：

| 角色类型 | 用户 | 权限来源 | 说明 |
|---------|------|----------|------|
| **超级管理员** | admin（唯一） | 系统预置，自动拥有全部权限 | 新功能缺省授权对象 |
| **系统管理员** | 可由 admin 创建/授权 | 由 admin 分配权限 | 日常运维角色 |

**示例**：
```sql
-- 分配给超级管理员角色（role_id=1）
-- 这行记录就是"权限"：角色 1 拥有资源 id 的访问权
INSERT INTO permission (role_id, resource_id, is_basic, is_effective)
SELECT 1, id, true, true FROM resource WHERE module_code = 'system';
```

---

#### 第 4 步：开发阶段 - 资源树构建

**工作内容**：
- 生成菜单项（MENU）资源、模块（MODULE）资源
- 将这两类资源写入 `resource` 表
- 在系统运行时提供资源维护页面
- 管理员可将菜单/模块设置为第 1 步中资源的父资源

**层级规则**：
- MENU 的父资源只能是 MODULE
- MODULE 的父资源只能是 MODULE（支持嵌套模块）
- API 的父资源可以是 MODULE/MENU/PAGE/ELEMENT/API 中的任何一种

**示例**：
```sql
-- 创建模块和菜单
INSERT INTO resource (name, code, type, parent_id, permission_key, module_code) VALUES
  ('系统设置', 'system', 'MODULE', NULL, 'system:menu', 'system'),
  ('用户管理', 'menu_user_management', 'MENU', 1, 'system:user:menu', 'system');

-- 设置父资源关系（将孤儿资源纳入树形结构）
UPDATE resource SET parent_id = 2 WHERE code IN ('page_user_list', 'elem_user_view', 'elem_user_create');
```

---

#### 第 5 步：运行阶段 - 资源配置

**操作人员**：admin 或被授权的**系统管理员**

**工作内容**：
- 在资源管理页面对 MODULE、MENU 进行增删改查
- 对其他资源（PAGE、ELEMENT、API）编辑其父资源和展示顺序
- 设置完成后，用户通过"刷新菜单"按钮实时按需更新菜单栏

**权限规则**：

| 资源类型 | 可修改名称 | 可修改父资源 | 父资源类型限制 |
|----------|------------|--------------|----------------|
| MODULE | ✅ | ✅（嵌套模块） | MODULE/null |
| MENU | ✅ | ✅ | MODULE |
| PAGE | ✅ | ✅ | MODULE/MENU |
| ELEMENT | ✅ | ❌ 系统维护 | PAGE |
| API | ✅ | ✅ | MODULE/MENU/PAGE/ELEMENT/API |

---

#### 第 6 步：运行阶段 - Admin 验证

**操作人员**：admin 用户

**工作内容**：
- admin 登录系统，验证是否具备全部权限
- 如果发现 admin 不能访问某项资源，则是数据配置错误
- 更新数据库相关数据修复

**验收标准**：
- admin 用户可访问所有菜单、页面、按钮、API
- 如有问题，直接修正数据库配置

---

#### 第 7 步：运行阶段 - 权限分配

**操作人员**：admin 或被授权的**系统管理员**

**工作内容**：
- 在角色基本权限配置页面配置角色权限
- 在角色缺省权限配置页面配置缺省权限
- 在角色完整权限配置页面灵活配置权限集
- 在用户权限配置页面对用户权限集进行配置

**配置页面**：

| 页面名称 | 路径 | 功能 |
|---------|------|------|
| 角色基本权限配置 | `/system/role-basic-permission` | 配置所有角色共享的基础权限 |
| 角色缺省权限配置 | `/system/role-default-permission` | 按角色类型配置缺省权限 |
| 角色完整权限配置 | `/system/role-permission` | 完整配置单个角色的权限集 |
| 用户权限配置 | `/system/user-permission` | 配置单个用户的权限（可覆盖角色） |

---

#### 第 8 步：运行阶段 - 用户验证

**操作人员**：非 admin 用户

**工作内容**：
- 非 admin 用户登录，验证是否具备分配的权限
- 如果发现不能访问某项资源，则是数据配置错误
- 更新数据库相关数据修复

**验收标准**：
- 用户可访问已授权的菜单、页面、按钮、API
- 用户不可访问未授权的资源
- 如有问题，修正权限配置

---

## 三、资源与权限模型

### 3.1 核心概念澄清

**问题**：合并 resource 表和 permission 表后，"权限"如何体现？

**答案**：资源与权限分离设计

```
┌─────────────────────────────────────────────────────────────────┐
│                      权限体系架构                                │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  【资源定义层】- 什么是可授予的权限？                            │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  resource 表（资源表）                                     │   │
│  │  - id=1, name="用户列表", type=PAGE,                      │   │
│  │    permission_key="system:user:list:page"  ← 权限码       │   │
│  │  - id=2, name="新建用户", type=ELEMENT,                   │   │
│  │    permission_key="system:user:create:button"             │   │
│  └─────────────────────────────────────────────────────────┘   │
│                           ↓                                     │
│  【权限关系层】- 谁拥有什么权限？                                │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  permission 表（角色 - 资源关联表）                     │   │
│  │  - role_id=1, resource_id=1, is_basic=true               │   │
│  │    ↑ 这行记录就是"权限"：角色 1 拥有资源 1 的访问权          │   │
│  │                                                          │   │
│  │  user_permission_adjust 表（用户权限调整表）                │   │
│  │  - user_id=100, resource_id=1, adjust_type=ADD           │   │
│  │    ↑ 这行记录也是"权限"：用户 100 被授予资源 1 的访问权      │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
│  【总结】                                                       │
│  - 资源（resource）：权限的载体（什么是可授予的）               │
│  - 关联关系（permission）：权限的体现（谁拥有什么）          │
│  - permission_key：资源的唯一权限码标识（用于后端注解）         │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 3.2 现实世界类比

```
┌─────────────────────────────────────────────────────────────────┐
│                    现实世界类比                                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  resource 表 = 博物馆的展品清单                                  │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  - 展品 001: 蒙娜丽莎（可参观）                           │   │
│  │  - 展品 002: 断臂维纳斯（可参观）                         │   │
│  │  - 展品 003: 后厅珍品（需特殊权限）                       │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
│  permission 表 = 门票类型                                    │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  - VIP 票：可参观 展品 001, 002, 003                       │   │
│  │  - 普通票：可参观 展品 001, 002                           │   │
│  │  - 学生票：可参观 展品 001                                │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
│  用户权限 = 游客实际持有的门票 + 特殊许可                       │
│                                                                 │
│  【"权限"在哪里？】                                             │
│  → 不在展品清单里，而在"门票类型"和"特殊许可"里！               │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 3.3 资源层级结构（完整网状结构）

```
                    ┌────────────────────────────────────────┐
                    │          MODULE（模块）                 │
                    │    父资源：MODULE 或 NULL（顶级）        │
                    └────────────────────────────────────────┘
                                      ↑
                    ┌─────────────────┴─────────────────┐
                    │                                   │
                    ↓                                   ↓
    ┌───────────────────────────┐       ┌───────────────────────────┐
    │   MODULE（子模块）         │       │      MENU（菜单）          │
    │   父资源：MODULE           │       │   父资源：MODULE          │
    └───────────────────────────┘       └───────────────────────────┘
                    ↑                                   ↑
                    └──────────────┬────────────────────┘
                                   ↓
                    ┌───────────────────────────┐
                    │      PAGE（页面）          │
                    │   父资源：MENU/MODULE      │
                    └───────────────────────────┘
                                   ↑
                    ┌───────────────────────────┐
                    │   ELEMENT（操作元素）       │
                    │   父资源：PAGE             │
                    └───────────────────────────┘
                                   ↑
                    ┌───────────────────────────┐
                    │      PAGE（子页面）        │
                    │   父资源：ELEMENT          │
                    └───────────────────────────┘
                                   ↑
                    ┌───────────────────────────┐
                    │      API（接口）           │
                    │   父资源：                │
                    │   MODULE/MENU/PAGE/       │
                    │   ELEMENT/API（任意）      │
                    └───────────────────────────┘
                                   ↑
                    ┌───────────────────────────┐
                    │      API（子接口）         │
                    │   父资源：API（调用链）     │
                    └───────────────────────────┘
```

**API 的特殊性**：
- **API 的父资源可以是任意类型**：MODULE、MENU、PAGE、ELEMENT、API
- API 可以调用其他 API（形成 API 调用链）
- API 是权限的最终承载对象（所有权限检查最终落实到 API 权限）

**⚠️ 循环引用防止**：
- 当 API 的父资源设置为另一个 API 时，必须验证**不能形成循环引用**
- 示例：`API-A → API-B → API-C → API-A` ❌ 不允许
- 验证逻辑：设置父资源前，检查新父资源的所有祖先是否包含当前资源

### 3.4 资源类型定义

| 类型 | 代码 | 说明 | 父资源类型 | 示例 |
|------|------|------|------------|------|
| MODULE | `MODULE` | 功能模块 | MODULE/null | 系统设置、零部件管理 |
| MENU | `MENU` | 导航菜单 | MODULE | 用户管理、角色管理 |
| PAGE | `PAGE` | 页面 | MENU/MODULE | 用户列表、用户详情 |
| ELEMENT | `ELEMENT` | 操作元素 | PAGE | 查看详情、新建用户 |
| API | `API` | 接口权限 | MODULE/MENU/PAGE/ELEMENT/API | 获取用户列表 API |

**注**：已取消 `PERMISSION` 资源类型，避免混淆。权限通过 `permission` 表管理。

---

## 四、权限码命名规范

### 4.1 权限码格式

```
module:resource:action:type
```

### 4.2 组成部分

| 部分 | 说明 | 示例 |
|------|------|------|
| `module` | 模块编码 | `system`, `auth`, `user`, `part` |
| `resource` | 资源类型 | `user`, `role`, `permission`, `document`, `component` |
| `action` | 操作类型 | `list`, `view`, `create`, `edit`, `delete`, `export`, `import`, `assign` |
| `type` | 权限类型 | `page`, `button`, `menu`, `api` |

### 4.3 权限类型说明

| type | 说明 | 示例 | 访问时机 |
|------|------|------|----------|
| `page` | 页面访问权限 | `system:user:list:page` | 路由守卫检查 |
| `button` | 按钮操作权限 | `system:user:create:button` | 按钮显示控制 |
| `menu` | 菜单显示权限 | `system:user:menu` | 侧边栏菜单过滤 |
| `api` | API 接口权限 | `system:user:list:api` | 后端拦截器检查 |

### 4.4 完整示例

```
# 用户管理模块
system:user:menu                    # 用户管理菜单
system:user:list:page               # 用户列表页面
system:user:view:button             # 查看用户按钮
system:user:create:button           # 新建用户按钮
system:user:create:page             # 用户创建页面
system:user:edit:button             # 编辑用户按钮
system:user:delete:button           # 删除用户按钮
system:user:export:button           # 导出用户按钮
system:user:list:api                # 获取用户列表 API

# 角色管理模块
system:role:menu                    # 角色管理菜单
system:role:list:page               # 角色列表页面
system:role:create:button           # 新建角色按钮
system:role:permission:button       # 分配权限按钮
system:role:list:api                # 获取角色列表 API

# 零部件管理模块
part:component_type:menu            # 零部件种类菜单
part:component_type:list:page       # 零部件种类列表页面
part:component_type:create:button   # 新建零部件种类按钮
part:component_type:list:api        # 获取零部件种类列表 API
```

### 4.5 API 权限码映射规则

后端 `@ApiPermission` 注解的权限码必须与 `resource` 表中的 `permission_key` 完全一致。

**示例**：
```java
// 注解中的权限码必须与数据库中的 permission_key 一致
@ApiPermission("system:user:list:api")
@GetMapping("/list")
public Result<List<User>> list() { ... }
```

**数据库**：
```sql
INSERT INTO resource (name, code, type, permission_key, module_code)
VALUES ('获取用户列表 API', 'api_user_list', 'API', 'system:user:list:api', 'system');
```

---

## 五、数据库表结构

### 5.1 核心表总览

| 表名 | 用途 | 关键字段 | 说明 |
|------|------|----------|------|
| `resource` | 资源表 | id, name, code, type, parent_id, permission_key, module_code | 存储所有资源（MODULE/MENU/PAGE/ELEMENT/API） |
| `permission` | 角色 - 资源关联表 | role_id, resource_id, is_basic, is_default, is_effective | **权限关系**：角色拥有哪些资源 |
| `user_permission_adjust` | 用户资源调整表 | user_id, resource_id, adjust_type (ADD/REMOVE) | 用户权限调整记录 |
| `user_role` | 用户 - 角色关联表 | user_id, role_id | 用户与角色的关联 |
| `user` | 用户表 | id, username, real_name, company_id | 用户信息 |
| `role` | 角色表 | id, role_name, role_code, type | 角色定义 |

### 5.2 resource 表结构（资源定义）

```sql
CREATE TABLE resource (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(100) NOT NULL,        -- 资源名称
    code                VARCHAR(50) NOT NULL UNIQUE,  -- 资源编码
    type                VARCHAR(20) NOT NULL,         -- MODULE/MENU/PAGE/ELEMENT/API
    parent_id           BIGINT REFERENCES resource(id), -- 父资源 ID（可为 NULL）
    permission_key      VARCHAR(200) UNIQUE,          -- 权限码（唯一）
    module_code         VARCHAR(50) NOT NULL,         -- 所属模块编码
    sort_order          INTEGER DEFAULT 0,            -- 排序号
    is_visible          BOOLEAN DEFAULT true,         -- 是否可见（MENU 专用）
    is_system_protected BOOLEAN DEFAULT false,        -- 是否系统保护（MENU 专用）
    status              SMALLINT DEFAULT 1,           -- 1=启用，0=禁用
    deleted             BOOLEAN DEFAULT FALSE,        -- 软删除标记
    deleted_at          TIMESTAMP,                    -- 删除时间
    deleted_by          BIGINT,                       -- 删除人
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**资源类型说明**：

| 类型 | 代码 | 说明 | 父资源类型 |
|------|------|------|------------|
| MODULE | `MODULE` | 功能模块 | MODULE/null |
| MENU | `MENU` | 导航菜单 | MODULE |
| PAGE | `PAGE` | 页面 | MENU/MODULE |
| ELEMENT | `ELEMENT` | 操作元素 | PAGE |
| API | `API` | 接口权限 | MODULE/MENU/PAGE/ELEMENT/API |

**索引设计**：
```sql
-- 基础索引
CREATE INDEX idx_resource_type ON resource(type);
CREATE INDEX idx_resource_parent_id ON resource(parent_id);
CREATE INDEX idx_resource_module_code ON resource(module_code);
CREATE UNIQUE INDEX idx_resource_permission_key ON resource(permission_key) WHERE permission_key IS NOT NULL;
CREATE UNIQUE INDEX idx_resource_code ON resource(code) WHERE deleted = FALSE;

-- 软删除资源查询优化
CREATE INDEX idx_resource_deleted ON resource(deleted);
```

### 5.3 permission 表结构（权限关系）

**注意**: 实际实现中表名为 `permission`，字段 `permission_id` 实际指向 `resource.id`。

```sql
CREATE TABLE permission (
    id                  BIGSERIAL PRIMARY KEY,
    role_id             BIGINT NOT NULL REFERENCES roles(id),      -- 角色 ID
    permission_id       BIGINT NOT NULL REFERENCES resource(id),   -- 资源 ID（实际指向 resource.id）
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (role_id, permission_id)
);
```

**字段说明**:
- `permission_id`: 实际指向 `resource.id`，表示角色拥有的资源权限

**生效规则**：

| is_basic | is_default | is_effective | 说明 |
|----------|------------|--------------|------|
| true | - | true | 基本权限，必须生效，不可移除 |
| false | true | true | 缺省权限，生效状态 |
| false | true | false | 缺省权限但被移除 |
| false | false | true | 非缺省权限，手动添加 |
| false | false | false | 非缺省权限，手动移除 |

**角色最终权限计算**：
```sql
-- 角色的有效权限 = 所有 is_effective = true 的资源
SELECT resource_id FROM permission 
WHERE role_id = ? AND is_effective = true;
```

**索引设计**：
```sql
CREATE INDEX idx_permission_role ON permission(role_id);
CREATE INDEX idx_permission_effective ON permission(role_id, is_effective);
CREATE INDEX idx_permission_resource ON permission(resource_id);
```

### 5.4 user_permission_adjust 表结构（用户权限调整）

```sql
CREATE TABLE user_permission_adjust (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL REFERENCES user(id),       -- 用户 ID
    resource_id         BIGINT NOT NULL REFERENCES resource(id),   -- 资源 ID
    adjust_type         VARCHAR(10) NOT NULL,                      -- ADD/REMOVE
    reason              VARCHAR(500),                              -- 调整原因
    adjusted_by         BIGINT,                                    -- 调整人
    adjusted_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, resource_id, adjust_type)
);
```

**调整规则**：
- 用户从角色继承的**基本权限**不能被减少（REMOVE 操作无效）
- 非基本权限可以灵活调整（ADD/REMOVE）

**用户最终权限计算**：
```
用户最终权限 = 
    (从所有角色继承的有效权限并集)
    + (user_permission_adjust 中 ADD 的资源)
    - (user_permission_adjust 中 REMOVE 的资源，但排除角色基本权限)
```

**索引设计**：
```sql
CREATE INDEX idx_user_adjust_user ON user_permission_adjust(user_id);
CREATE INDEX idx_user_adjust_type ON user_permission_adjust(user_id, adjust_type);
```

### 5.5 user_role 表结构

```sql
CREATE TABLE user_role (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL REFERENCES user(id),
    role_id             BIGINT NOT NULL REFERENCES role(id),
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, role_id)
);
```

### 5.6 软删除机制

**级联软删除触发器**：
```sql
CREATE OR REPLACE FUNCTION cascade_soft_delete()
RETURNS TRIGGER AS $$
BEGIN
    -- 软删除所有子资源（递归）
    WITH RECURSIVE child_resources AS (
        SELECT id FROM resource WHERE parent_id = OLD.id AND deleted = FALSE
        UNION ALL
        SELECT r.id FROM resource r
        INNER JOIN child_resources cr ON r.parent_id = cr.id
        WHERE r.deleted = FALSE
    )
    UPDATE resource
    SET deleted = TRUE, deleted_at = NOW(), deleted_by = OLD.deleted_by
    WHERE id IN (SELECT id FROM child_resources);
    
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

-- 创建触发器
CREATE TRIGGER trg_resource_before_delete
    BEFORE UPDATE OF deleted ON resource
    FOR EACH ROW
    WHEN (NEW.deleted = TRUE AND OLD.deleted = FALSE)
    EXECUTE FUNCTION cascade_soft_delete();
```

**恢复资源触发器**：
```sql
CREATE OR REPLACE FUNCTION cascade_restore()
RETURNS TRIGGER AS $$
BEGIN
    -- 恢复时检查父资源是否已恢复
    IF NEW.deleted = FALSE AND NEW.parent_id IS NOT NULL THEN
        DECLARE
            parent_deleted BOOLEAN;
        BEGIN
            SELECT deleted INTO parent_deleted FROM resource WHERE id = NEW.parent_id;
            IF parent_deleted = TRUE THEN
                RAISE EXCEPTION '无法恢复资源：父资源仍处于删除状态';
            END IF;
        END;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
```

---

## 六、两种管理员角色设计

### 6.1 角色定义

| 角色 | 代码 | 用户 | 权限来源 | 说明 |
|------|------|------|----------|------|
| **超级管理员** | `ROLE_SUPER_ADMIN` | admin（唯一） | 系统预置，自动拥有全部权限 | 新功能缺省授权对象，不受权限限制 |
| **系统管理员** | `ROLE_SYSTEM_ADMIN` | 可由 admin 创建多个 | 由 admin 分配权限 | 日常运维角色，权限取决于 admin 配置 |

### 6.2 权限分配策略

```
┌─────────────────────────────────────────────────────────────────┐
│                    权限分配策略                                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  新功能开发完成                                                  │
│     │                                                           │
│     ▼                                                           │
│  自动分配给 ROLE_SUPER_ADMIN (admin 用户)                        │
│     │                                                           │
│     ▼                                                           │
│  admin 验证功能可访问                                            │
│     │                                                           │
│     ▼                                                           │
│  admin 决定是否分配给 ROLE_SYSTEM_ADMIN                          │
│     │                                                           │
│     ▼                                                           │
│  配置角色缺省权限（按角色类型）                                  │
│     │                                                           │
│     ▼                                                           │
│  分配给具体用户                                                  │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 6.3 数据库配置

```sql
-- 创建超级管理员角色（仅 admin 用户）
INSERT INTO role (role_name, role_code, type, company_id)
VALUES ('超级管理员', 'ROLE_SUPER_ADMIN', 'SYSTEM', NULL);

-- 创建系统管理员角色（可由 admin 分配）
INSERT INTO role (role_name, role_code, type, company_id)
VALUES ('系统管理员', 'ROLE_SYSTEM_ADMIN', 'SYSTEM', NULL);

-- admin 用户关联超级管理员角色
INSERT INTO user_role (user_id, role_id)
VALUES (1, (SELECT id FROM role WHERE role_code = 'ROLE_SUPER_ADMIN'));
```

---

## 七、数据范围权限设计

### 7.1 数据范围类型

| 类型 | 代码 | 说明 | SQL 过滤示例 |
|------|------|------|-------------|
| **全部数据** | `ALL` | 可访问全部数据 | 无 WHERE 条件 |
| **本公司** | `COMPANY` | 仅限本公司数据 | `WHERE company_id = :userCompanyId` |
| **本部门** | `DEPT` | 仅限本部门数据 | `WHERE dept_id = :userDeptId` |
| **本部门及下级** | `DEPT_AND_SUB` | 本部门及下级部门数据 | `WHERE dept_id IN (:userDeptIds)` |
| **仅本人** | `SELF` | 仅限本人创建/负责的数据 | `WHERE created_by = :userId` |
| **自定义** | `CUSTOM` | 按规则自定义数据范围 | `WHERE id IN (:customIds)` |

### 7.2 数据范围权限层级

```
┌─────────────────────────────────────────────────────────────────┐
│                    数据范围权限层级                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  角色缺省数据范围                                                │
│  (role_data_scope_default)                                       │
│         │                                                        │
│         ▼                                                        │
│  角色数据范围调整                                                │
│  (role_data_scope_adjust: ADD/REMOVE)                            │
│         │                                                        │
│         ▼                                                        │
│  角色最终数据范围                                                │
│  = 缺省数据范围 + 调整 (ADD) - 调整 (REMOVE)                      │
│         │                                                        │
│         ▼                                                        │
│  用户继承数据范围                                                │
│  (从所有角色继承的数据范围并集)                                   │
│         │                                                        │
│         ▼                                                        │
│  用户数据范围调整                                                │
│  (user_data_scope_adjust: ADD/REMOVE)                            │
│         │                                                        │
│         ▼                                                        │
│  用户最终数据范围                                                │
│  = 继承数据范围 + 用户调整 (ADD) - 用户调整 (REMOVE)              │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 7.3 简化设计：按模块配置数据范围

**问题**：为每个资源配置数据范围过于复杂，数据量会爆炸。

**简化方案**：按模块配置数据范围

```sql
CREATE TABLE role_data_scope (
    id                  BIGSERIAL PRIMARY KEY,
    role_id             BIGINT NOT NULL REFERENCES role(id),       -- 角色 ID
    module_code         VARCHAR(50) NOT NULL,                      -- 模块编码
    resource_type       VARCHAR(20),                               -- 资源类型（可选，为空表示模块下所有资源）
    scope_type          VARCHAR(20) NOT NULL,                      -- ALL/COMPANY/DEPT/DEPT_AND_SUB/SELF/CUSTOM
    is_default          BOOLEAN DEFAULT true,                      -- 是否缺省数据范围
    is_effective        BOOLEAN DEFAULT true,                      -- 是否生效
    custom_rule         JSONB,                                     -- 自定义规则（JSON 格式）
    adjust_reason       VARCHAR(500),                              -- 调整原因
    adjusted_by         BIGINT,                                    -- 调整人
    adjusted_at         TIMESTAMP,                                 -- 调整时间
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (role_id, module_code, resource_type)
);
```

**自定义规则格式**：
```json
{
  "type": "work_area_ids",
  "operator": "IN",
  "values": [1, 2, 3],
  "description": "仅允许访问指定的作业区"
}
```

### 7.4 用户数据范围表

```sql
CREATE TABLE user_data_scope (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL REFERENCES user(id),       -- 用户 ID
    module_code         VARCHAR(50) NOT NULL,                      -- 模块编码
    resource_type       VARCHAR(20),                               -- 资源类型
    scope_type          VARCHAR(20) NOT NULL,                      -- ALL/COMPANY/DEPT/DEPT_AND_SUB/SELF/CUSTOM
    source_type         VARCHAR(20) DEFAULT 'inherited',           -- inherited/direct
    is_effective        BOOLEAN DEFAULT true,                      -- 是否生效
    custom_rule         JSONB,                                     -- 自定义规则
    adjust_reason       VARCHAR(500),                              -- 调整原因
    adjusted_by         BIGINT,                                    -- 调整人
    adjusted_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, module_code, resource_type)
);
```

### 7.5 数据范围计算逻辑

**角色最终数据范围**：
```sql
SELECT module_code, resource_type, scope_type, custom_rule
FROM role_data_scope
WHERE role_id = ? AND is_effective = true;
```

**用户继承数据范围（从所有角色）**：
```sql
SELECT DISTINCT rds.module_code, rds.resource_type, rds.scope_type, rds.custom_rule
FROM user_role ur
JOIN role_data_scope rds ON ur.role_id = rds.role_id
WHERE ur.user_id = ? AND rds.is_effective = true;
```

**用户最终数据范围**：
```
用户最终数据范围 = 
    (从所有角色继承的有效数据范围并集)
    + (user_data_scope 中 ADD 的数据范围)
    - (user_data_scope 中 REMOVE 的数据范围)
```

### 7.6 数据范围 SQL 注入示例

```java
@Service
public class DataScopeService {
    
    /**
     * 生成数据范围 WHERE 条件
     */
    public String generateWhereClause(Long userId, String moduleCode) {
        // 获取用户对该模块的数据范围
        DataScope scope = getUserDataScope(userId, moduleCode);
        
        if (scope == null) {
            return "1=0"; // 无权限访问
        }
        
        switch (scope.getScopeType()) {
            case "ALL":
                return "1=1"; // 无限制
            case "COMPANY":
                User user = userService.getById(userId);
                return "t.company_id = " + user.getCompanyId();
            case "DEPT":
                return "t.dept_id = " + user.getDeptId();
            case "DEPT_AND_SUB":
                List<Long> deptIds = deptService.getSubDeptIds(user.getDeptId());
                return "t.dept_id IN (" + String.join(",", deptIds) + ")";
            case "SELF":
                return "t.created_by = " + userId;
            case "CUSTOM":
                // 解析 custom_rule JSON
                CustomRule rule = parseCustomRule(scope.getCustomRule());
                return generateCustomWhere(rule);
            default:
                return "1=0";
        }
    }
}
```

---

## 八、权限继承与冲突处理

### 8.1 权限继承规则

**问题**：子资源是否自动继承父资源的权限？

**设计决策**：**不继承**

| 资源类型 | 继承规则 | 说明 |
|----------|----------|------|
| MODULE | 不继承 | 拥有模块权限不代表拥有子资源权限 |
| MENU | 不继承 | 拥有菜单权限不代表拥有页面权限 |
| PAGE | 不继承 | 拥有页面权限不代表拥有按钮权限 |
| ELEMENT | 不继承 | 拥有按钮权限不代表拥有子页面权限 |
| API | 不继承 | 拥有 API 权限不代表拥有子 API 权限 |

**推荐实践**：配置角色权限时，直接配置到最细粒度（ELEMENT/API 层级）

**示例**：
```sql
-- 推荐：直接配置到 ELEMENT 层级
INSERT INTO permission (role_id, resource_id, is_basic, is_effective)
VALUES 
  (1, 101, true, true),  -- 用户列表页面
  (1, 102, true, true),  -- 查看详情按钮
  (1, 103, true, true),  -- 新建用户按钮
  (1, 104, true, true);  -- 编辑用户按钮

-- 不推荐：只配置到 MENU 层级
INSERT INTO permission (role_id, resource_id, is_basic, is_effective)
VALUES (1, 1, true, true);  -- 用户管理菜单（子资源权限不明确）
```

### 8.2 权限冲突处理规则

**问题**：用户同时拥有多个角色，角色之间的权限冲突如何处理？

**场景**：
- 角色 A 授予"用户管理"权限
- 角色 B 移除"用户管理"权限
- 用户最终是否有权限？

**优先级顺序**：
1. 用户直接权限（user_permission_adjust） > 角色权限
2. 用户 REMOVE 操作 > 角色 ADD 操作（除非是角色基本权限）
3. 角色基本权限（is_basic=true）不可被移除

**计算公式**：
```
用户最终权限 = 
    (所有角色的有效权限并集)
    - (user_permission_adjust 中 REMOVE 的权限，排除角色基本权限)
    + (user_permission_adjust 中 ADD 的权限)
```

**SQL 实现**：
```sql
WITH user_roles AS (
    SELECT role_id FROM user_role WHERE user_id = ?
),
role_permissions AS (
    SELECT rr.resource_id
    FROM permission rr
    WHERE rr.role_id IN (SELECT role_id FROM user_roles)
      AND rr.is_effective = true
),
role_basic_permissions AS (
    SELECT rr.resource_id
    FROM permission rr
    WHERE rr.role_id IN (SELECT role_id FROM user_roles)
      AND rr.is_basic = true
),
user_remove AS (
    SELECT resource_id
    FROM user_permission_adjust
    WHERE user_id = ? AND adjust_type = 'REMOVE'
),
user_add AS (
    SELECT resource_id
    FROM user_permission_adjust
    WHERE user_id = ? AND adjust_type = 'ADD'
)
-- 最终权限 = 角色权限 - 用户 REMOVE(排除基本权限) + 用户 ADD
SELECT resource_id FROM role_permissions
WHERE resource_id NOT IN (
    SELECT resource_id FROM user_remove
    WHERE resource_id NOT IN (SELECT resource_id FROM role_basic_permissions)
)
UNION
SELECT resource_id FROM user_add;
```

---

## 九、后端实现方案

### 9.1 权限注解

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasPermission(#permission)")
public @interface ApiPermission {
    String value(); // 权限代码，如 "system:user:list:api"
}
```

### 9.2 Controller 示例

```java
@RestController
@RequestMapping("/api/user")
@ApiPermission("system:user:list:api")
public class UserController {
    
    @GetMapping("/list")
    @ApiPermission("system:user:list:api")
    public Result<Page<UserDTO>> queryUsers(
            @ModelAttribute UserQueryRequest query,
            @RequestAttribute("userId") Long operatorId) {
        Page<UserDTO> page = userService.queryUsers(query, operatorId);
        return Result.success(page);
    }
    
    @PostMapping
    @ApiPermission("system:user:create:api")
    public Result<UserDTO> createUser(
            @RequestAttribute("userId") Long operatorId,
            @Valid @RequestBody CreateUserRequest request) {
        UserDTO user = userService.createUser(request, operatorId);
        return Result.success(user);
    }
}
```

### 9.3 权限拦截器

```java
@Component
public class PermissionInterceptor implements HandlerInterceptor {
    
    @Autowired
    private PermissionService permissionService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                            HttpServletResponse response, 
                            Object handler) {
        UserContext user = SecurityContextHolder.getCurrentUser();
        
        // admin 用户跳过权限检查
        if (user.isSuperAdmin()) {
            return true;
        }
        
        String apiPath = request.getRequestURI();
        String method = request.getMethod();
        
        boolean hasPermission = permissionService.hasApiPermission(
            user, apiPath, method);
        
        if (!hasPermission) {
            response.setStatus(403);
            response.getWriter().write("无权限访问");
            return false;
        }
        
        return true;
    }
}
```

### 9.4 循环引用防止（通用设计）

**适用范围**：所有允许设置父资源的资源类型（MODULE、MENU、PAGE、ELEMENT、API）

**后端验证逻辑**：
```java
@Service
public class ResourceService {
    
    /**
     * 设置父资源前验证是否会导致循环引用（通用方法）
     */
    @Transactional
    public void updateParent(Long resourceId, Long newParentId) {
        if (newParentId == null) {
            permissionMapper.updateParent(resourceId, null);
            return;
        }
        
        // 1. 获取当前资源信息
        Resource currentResource = resourceMapper.selectById(resourceId);
        
        // 2. 验证父资源类型是否符合规则
        validateParentType(currentResource.getType(), newParentId);
        
        // 3. 检查是否会导致循环引用
        if (wouldCreateCycle(resourceId, newParentId)) {
            throw new RuntimeException(
                "无法设置父资源：会导致循环引用（" + 
                getResourcePath(resourceId) + " → " + 
                getResourcePath(newParentId) + " → ... → " + 
                getResourcePath(resourceId) + "）"
            );
        }
        
        // 4. 更新父资源
        resourceMapper.updateParent(resourceId, newParentId);
        
        // 5. 清除缓存
        permissionCacheService.evictAllMenuCache();
    }
    
    /**
     * 验证父资源类型是否符合规则
     */
    private void validateParentType(String resourceType, Long newParentId) {
        Resource newParent = resourceMapper.selectById(newParentId);
        String parentType = newParent.getType();
        
        switch (resourceType) {
            case "MODULE":
                if (!"MODULE".equals(parentType)) {
                    throw new RuntimeException("MODULE 的父资源只能是 MODULE");
                }
                break;
            case "MENU":
                if (!"MODULE".equals(parentType)) {
                    throw new RuntimeException("MENU 的父资源只能是 MODULE");
                }
                break;
            case "PAGE":
                if (!Arrays.asList("MENU", "MODULE").contains(parentType)) {
                    throw new RuntimeException("PAGE 的父资源只能是 MENU 或 MODULE");
                }
                break;
            case "ELEMENT":
                if (!"PAGE".equals(parentType)) {
                    throw new RuntimeException("ELEMENT 的父资源只能是 PAGE");
                }
                break;
            case "API":
                // API 的父资源可以是任意类型
                break;
        }
    }
    
    /**
     * 检查设置新父资源是否会导致循环引用（递归检查）
     */
    public boolean wouldCreateCycle(Long resourceId, Long newParentId) {
        if (resourceId.equals(newParentId)) {
            return true;
        }
        
        Set<Long> ancestors = getAllAncestors(newParentId);
        return ancestors.contains(resourceId);
    }
    
    /**
     * 获取资源的所有祖先（递归向上遍历，带深度限制）
     */
    private Set<Long> getAllAncestors(Long resourceId) {
        Set<Long> ancestors = new HashSet<>();
        Long currentId = resourceId;
        int depth = 0;
        final int MAX_DEPTH = 100;
        
        while (currentId != null && depth < MAX_DEPTH) {
            Resource r = resourceMapper.selectById(currentId);
            if (r == null || r.getParentId() == null) {
                break;
            }
            
            if (ancestors.contains(r.getParentId())) {
                break; // 防止死循环
            }
            
            ancestors.add(r.getParentId());
            currentId = r.getParentId();
            depth++;
        }
        
        if (depth >= MAX_DEPTH) {
            throw new RuntimeException("资源层级超过最大深度限制（100），可能存在循环引用");
        }
        
        return ancestors;
    }
}
```

---

## 十、前端实现方案

### 10.1 权限指令

```javascript
// directives/permission.js
export default {
  mounted(el, binding) {
    const { value } = binding;
    const userStore = useUserStore();
    const permissions = userStore.permissions || [];
    
    if (value && !permissions.includes(value)) {
      el.parentNode && el.parentNode.removeChild(el);
    }
  }
};

// 使用
// <el-button v-permission="'system:user:create:button'">创建用户</el-button>
```

### 10.2 路由守卫

```javascript
router.beforeEach((to, from, next) => {
  const userStore = useUserStore();
  const menus = userStore.menus || [];
  const menuCode = to.meta.menuCode;
  
  // admin 用户跳过权限检查
  if (userStore.userInfo.isSuperAdmin) {
    next();
    return;
  }
  
  // 个人中心始终可访问
  if (menuCode === 'profile') {
    next();
    return;
  }
  
  // 检查菜单权限
  const hasPermission = menus.some(m => m.menuCode === menuCode);
  
  if (!hasPermission) {
    next({ name: 'Home' });
    return;
  }
  
  next();
});
```

### 10.3 刷新菜单功能

```javascript
// stores/user.js
export const useUserStore = defineStore('user', () => {
  const userInfo = ref({});
  const permissions = ref([]);
  const menus = ref([]);
  
  /**
   * 刷新菜单（权限变更后调用）
   */
  async function refreshMenus() {
    const res = await getMyMenus();
    const newMenus = res.data || [];
    userInfo.value = { ...userInfo.value, menus: newMenus };
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value));
  }
  
  /**
   * 刷新权限
   */
  async function refreshPermissions() {
    const res = await getMyPermissions();
    permissions.value = res.data || [];
  }
  
  return { userInfo, permissions, menus, refreshMenus, refreshPermissions };
});
```

---

## 十一、权限缓存机制

### 11.1 缓存结构

```
# 用户权限缓存
user:permission:{userId} → Set<Long> resourceIds, TTL: 30 minutes

# 角色权限缓存
role:permission:{roleId} → Set<Long> resourceIds, TTL: 60 minutes

# 用户菜单缓存
user:menus:{userId} → Array<menu>, TTL: 30 minutes

# 数据范围缓存
user:data_scope:{userId}:{moduleCode} → scope_type, TTL: 30 minutes
```

### 11.2 缓存策略

| 操作 | 缓存处理 |
|------|----------|
| 用户登录 | 写入用户权限缓存 + 菜单缓存 + 数据范围缓存 |
| 权限查询 | 先查缓存，未命中则查数据库 |
| 角色权限变更 | 清除角色缓存 + 该角色下所有用户缓存 |
| 用户权限变更 | 清除该用户缓存 |
| 资源结构变更 | 清除所有用户菜单缓存 |
| 手动刷新 | 强制重新加载并更新缓存 |

### 11.3 缓存服务

```java
@Service
public class PermissionCacheService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 获取用户权限缓存
     */
    public Set<Long> getUserPermissions(Long userId) {
        String key = "user:permission:" + userId;
        Set<Long> cached = (Set<Long>) redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return cached;
        }
        
        Set<Long> permissions = permissionService.loadUserPermissions(userId);
        redisTemplate.opsForValue().set(key, permissions, 30, TimeUnit.MINUTES);
        return permissions;
    }
    
    /**
     * 清除用户权限缓存
     */
    public void evictUserPermission(Long userId) {
        String key = "user:permission:" + userId;
        redisTemplate.delete(key);
    }
    
    /**
     * 清除角色权限缓存及相关用户缓存
     */
    public void evictRolePermission(Long roleId) {
        String roleKey = "role:permission:" + roleId;
        redisTemplate.delete(roleKey);
        
        List<Long> userIds = userService.getUserIdsByRole(roleId);
        for (Long userId : userIds) {
            evictUserPermission(userId);
        }
    }
    
    /**
     * 清除所有用户菜单缓存（资源结构变更时）
     */
    public void evictAllMenuCache() {
        Set<String> keys = redisTemplate.keys("user:menus:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
```

---

## 十二、审计日志设计

### 12.1 权限变更审计日志

```sql
CREATE TABLE permission_audit_log (
    id                  BIGSERIAL PRIMARY KEY,
    operation_type      VARCHAR(20) NOT NULL,  -- CREATE/UPDATE/DELETE/ASSIGN/REVOKE
    target_type         VARCHAR(20) NOT NULL,  -- ROLE/USER/RESOURCE
    target_id           BIGINT NOT NULL,
    target_name         VARCHAR(100),          -- 目标名称（冗余，便于查询）
    operator_id         BIGINT NOT NULL,
    operator_name       VARCHAR(100),          -- 操作人姓名
    old_value           JSONB,                 -- 变更前数据
    new_value           JSONB,                 -- 变更后数据
    reason              VARCHAR(500),          -- 变更原因
    ip_address          VARCHAR(50),           -- 操作 IP
    user_agent          VARCHAR(500),          -- 操作设备
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX idx_permission_audit_target ON permission_audit_log(target_type, target_id);
CREATE INDEX idx_permission_audit_operator ON permission_audit_log(operator_id);
CREATE INDEX idx_permission_audit_created ON permission_audit_log(created_at);
```

### 12.2 数据范围变更审计日志

```sql
CREATE TABLE data_scope_audit_log (
    id                  BIGSERIAL PRIMARY KEY,
    operation_type      VARCHAR(20) NOT NULL,  -- CREATE/UPDATE/DELETE
    target_type         VARCHAR(20) NOT NULL,  -- ROLE/USER
    target_id           BIGINT NOT NULL,
    module_code         VARCHAR(50),           -- 模块编码
    resource_type       VARCHAR(20),           -- 资源类型
    operator_id         BIGINT NOT NULL,
    operator_name       VARCHAR(100),
    old_scope_type      VARCHAR(20),
    new_scope_type      VARCHAR(20),
    old_custom_rule     JSONB,
    new_custom_rule     JSONB,
    reason              VARCHAR(500),
    ip_address          VARCHAR(50),
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX idx_data_scope_audit_target ON data_scope_audit_log(target_type, target_id);
CREATE INDEX idx_data_scope_audit_created ON data_scope_audit_log(created_at);
```

### 12.3 审计日志记录示例

**Java 代码示例**：
```java
@Service
public class PermissionAuditService {
    
    @Autowired
    private PermissionAuditLogMapper auditLogMapper;
    
    /**
     * 记录权限分配日志
     */
    public void logPermissionAssign(Long roleId, Long resourceId, String operation, Long operatorId) {
        Role role = roleMapper.selectById(roleId);
        Resource resource = resourceMapper.selectById(resourceId);
        
        PermissionAuditLog log = new PermissionAuditLog();
        log.setOperationType(operation); // ASSIGN/REVOKE
        log.setTargetType("ROLE");
        log.setTargetId(roleId);
        log.setTargetName(role.getRoleName());
        log.setOperatorId(operatorId);
        log.setOperatorName(userService.getUserName(operatorId));
        log.setOldValue(buildOldValue(roleId, resourceId));
        log.setNewValue(buildNewValue(roleId, resourceId));
        log.setReason("角色权限配置");
        
        auditLogMapper.insert(log);
    }
}
```

---

## 十三、实施步骤

### 13.1 Phase 1：数据库迁移（1 天）

**工作内容**：
- [ ] 创建 `resource` 表（从旧表迁移）
- [ ] 创建 `permission` 表（从旧 role_permission 迁移）
- [ ] 创建 `user_permission_adjust` 表（从旧 user_permission_adjust 迁移）
- [ ] 创建 `role_data_scope` 和 `user_data_scope` 表
- [ ] 创建审计日志表
- [ ] 创建软删除触发器
- [ ] 创建循环引用检查触发器
- [ ] 创建 `ROLE_SUPER_ADMIN` 和 `ROLE_SYSTEM_ADMIN` 角色
- [ ] 将 admin 用户关联到 `ROLE_SUPER_ADMIN`

**迁移脚本示例**：
```sql
-- 1. 创建新 resource 表
CREATE TABLE resource_new (...);

-- 2. 从旧表迁移数据
INSERT INTO resource_new (id, name, code, type, parent_id, permission_key, module_code, ...)
SELECT id, name, code, type, parent_id, permission_key, module_code, ...
FROM permission; -- 旧表名

-- 3. 创建 permission 表
CREATE TABLE permission (...);

-- 4. 从旧表迁移数据
INSERT INTO permission (role_id, resource_id, is_basic, is_default, is_effective, ...)
SELECT role_id, permission_id, is_basic, is_default, is_effective, ...
FROM role_permission; -- 旧表名

-- 5. 删除旧表
DROP TABLE IF EXISTS permission;
DROP TABLE IF EXISTS role_permission;

-- 6. 重命名新表
ALTER TABLE resource_new RENAME TO resource;
```

---

### 13.2 Phase 2：后端服务开发（2 天）

**工作内容**：
- [ ] 开发资源扫描器（自动从代码提取 API、页面路由）
- [ ] 开发权限码生成器（自动生成唯一权限码）
- [ ] 开发孤儿资源管理 API
- [ ] 开发批量设置父资源 API
- [ ] 完善权限缓存服务
- [ ] 添加"刷新菜单"API
- [ ] 实现数据范围 SQL 注入服务
- [ ] 实现审计日志服务

**API 清单**：
| 方法 | 端点 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/resource/orphaned` | 获取孤儿资源 | `system:resource:view:api` |
| PUT | `/api/resource/batch-parent` | 批量设置父资源 | `system:resource:edit:api` |
| POST | `/api/resource/scan` | 扫描代码生成资源 | `system:resource:create:api` |
| POST | `/api/permission/check-cycle` | 检查循环引用 | `system:resource:edit:api` |
| POST | `/api/menu/refresh` | 刷新菜单缓存 | `system:menu:refresh:api` |

---

### 13.3 Phase 3：前端页面开发（2 天）

**工作内容**：
- [ ] 资源管理页面添加"孤儿资源"筛选视图
- [ ] 添加批量设置父资源功能（拖拽或批量选择）
- [ ] 在全局导航栏添加"刷新菜单"按钮
- [ ] 完善权限配置页面（角色基本权限、缺省权限、完整权限）
- [ ] 添加数据范围配置页面
- [ ] 添加审计日志查看页面
- [ ] 添加循环引用检查 UI 提示

---

### 13.4 Phase 4：权限缓存优化（1 天）

**工作内容**：
- [ ] 实现 Redis 权限缓存（TTL: 30 分钟）
- [ ] 实现权限变更时自动失效缓存
- [ ] 实现手动刷新按钮（强制更新缓存）
- [ ] 性能测试（目标：< 50ms）

---

### 13.5 Phase 5：测试验证（1 天）

**测试场景**：

| 场景 | 预期结果 | 验证方法 |
|------|----------|----------|
| 新功能开发 | 资源无父资源，admin 可访问 | 创建测试功能，验证 admin 访问 |
| 权限码生成 | 权限码唯一，符合规范 | 检查生成的权限码 |
| 父资源设置 | 可批量设置，菜单实时更新 | 设置父资源后刷新菜单 |
| 循环引用检查 | 阻止循环引用设置 | 尝试设置循环父资源 |
| 权限分配 | 角色/用户权限正确生效 | 分配权限后验证用户访问 |
| 缓存性能 | 响应时间 < 50ms | 压测验证 |
| 软删除 | 级联删除子资源 | 删除父资源，验证子资源状态 |
| 审计日志 | 记录所有权限变更 | 查看审计日志表 |

---

### 13.6 总计划

| 阶段 | 工作内容 | 预计时间 | 状态 |
|------|----------|----------|------|
| Phase 1 | 数据库迁移 | 1 天 | 待开始 |
| Phase 2 | 后端服务开发 | 2 天 | 待开始 |
| Phase 3 | 前端页面开发 | 2 天 | 待开始 |
| Phase 4 | 权限缓存优化 | 1 天 | 待开始 |
| Phase 5 | 测试验证 | 1 天 | 待开始 |
| **合计** | | **7 天** | |

---

## 附录

### A. 相关文件位置

| 文件类型 | 路径 |
|----------|------|
| 后端代码 | `/root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/backend/src/main/java/` |
| 前端代码 | `/root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/frontend/src/` |
| 数据库迁移 | `/root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/backend/src/main/resources/db/migration/` |
| 文档 | `/root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/backend/docs/` |

### B. 术语表

| 术语 | 说明 |
|------|------|
| **资源** | 系统中可访问的对象（MODULE/MENU/PAGE/ELEMENT/API） |
| **权限** | 角色/用户对资源的访问权（通过 permission 表体现） |
| **权限码** | 资源的唯一标识符，格式：`module:resource:action:type` |
| **孤儿资源** | 无父资源的资源（parent_id = NULL），开发阶段产生 |
| **超级管理员** | 系统预置的最高权限角色，仅 admin 用户 |
| **系统管理员** | 可由 admin 创建和授权的运维角色 |
| **刷新菜单** | 手动触发菜单缓存更新，无需退出系统 |
| **基本权限** | 角色必须拥有的权限，不可移除（is_basic = true） |
| **缺省权限** | 角色创建时自动分配的权限（is_default = true） |
| **生效权限** | 当前有效的权限（is_effective = true） |
| **数据范围** | 用户可访问的数据范围（ALL/COMPANY/DEPT/SELF/CUSTOM） |

---

## 设计变更说明（V4.1 → V4.3）

### 核心变更

| 方面 | V4.1 | V4.3 | 改进 |
|------|------|------|------|
| **表命名** | permission 表 | resource 表 | 语义清晰，资源与权限分离 |
| **权限体现** | 混淆在表名中 | permission 表 | 权限关系明确 |
| **数据范围** | 按资源配置 | 按模块配置 | 简化设计，降低数据量 |
| **审计日志** | 无 | 完整审计日志表 | 可追溯，符合安全要求 |
| **软删除** | 无 | 级联软删除机制 | 数据安全，支持恢复 |
| **权限继承** | 未说明 | 明确不继承 | 避免歧义 |
| **冲突处理** | 未说明 | 完整优先级规则 | 多角色场景清晰 |

### 资源与权限分离设计

**V4.1 问题**：
- 表名 `permission` 存储的是资源，语义混淆
- "权限"概念不清晰

**V4.3 解决**：
- `resource` 表：存储资源定义（什么是可授予的）
- `permission` 表：存储权限关系（谁拥有什么）
- 权限体现在关联表中，而非资源表中

---

**文档版本**：V4.3  
**创建日期**：2026-04-04  
**状态**：待柳生审核  
**下次更新**：根据审核意见修订
