# Camera 项目权限设计方案 V4.1

**版本**：V4.1  
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
| **V4.1** | **2026-04-04** | **合并 resource 表和 permission 表** | **柳生/AI** |

---

## 目录

1. [核心理念](#一核心理念)
2. [权限驱动开发流程（8 步）](#二权限驱动开发流程 8 步)
3. [资源模型架构](#三资源模型架构)
4. [权限码命名规范](#四权限码命名规范)
5. [数据库表结构](#五数据库表结构)
6. [两种管理员角色设计](#六两种管理员角色设计)
7. [数据范围权限设计](#七数据范围权限设计)
8. [后端实现方案](#八后端实现方案)
9. [前端实现方案](#九前端实现方案)
10. [权限缓存机制](#十权限缓存机制)
11. [实施步骤](#十一实施步骤)

---

## 一、核心理念

### 1.1 核心原则

**开发与权限配置分离**：新功能开发时先完成功能和不带父资源的资源收集，开发完成后再通过配置建立权限层级关系。

### 1.2 设计目标

| 目标 | 说明 |
|------|------|
| **开发友好** | 新功能开发不受权限配置影响，可独立进行 |
| **配置灵活** | 开发完成后，管理员可灵活配置权限层级和分配 |
| **实时生效** | 权限变更后无需退出系统，实时刷新菜单 |
| **高性能** | 权限验证响应时间 < 50ms（缓存命中） |
| **可追溯** | 所有权限配置操作记录审计日志 |
| **简洁性** | 资源即权限，一张表管理，降低复杂度 |

### 1.3 关键设计原则

1. **资源孤儿状态**：新功能开发阶段，资源无父资源，便于收集和管理
2. **admin 超级权限**：admin 用户始终拥有全部权限，否则是数据配置错误
3. **菜单显示逻辑**：用户是否具备菜单下关联资源的权限决定菜单是否显示
4. **权限缓存机制**：用户权限数据缓存 + 手动/自动刷新机制
5. **资源权限一体化**：每个资源自带权限码，resource 表和 permission 表合并为一张表

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
- 实时更新 `permission` 表（增删改），资源此时**无父资源**（孤儿状态）
- 资源类型包括：API、PAGE、ELEMENT

**产出**：
- 功能代码
- 权限数据（孤儿状态，parent_id = NULL）

**示例**：
```sql
-- 开发阶段插入的权限（无父资源）
INSERT INTO permission (name, code, type, parent_id, permission_key, module_code) VALUES
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
- 权限码直接存储在 `permission` 表的 `permission_key` 字段

**权限码格式**：
```
module:resource:action:type
```

**示例**：
```sql
-- 权限码在插入时直接生成
INSERT INTO permission (name, code, type, permission_key, module_code) VALUES
  ('用户列表页面', 'page_user_list', 'PAGE', 'system:user:list:page', 'system'),
  ('查看用户详情按钮', 'elem_user_view', 'ELEMENT', 'system:user:view:button', 'system'),
  ('新建用户按钮', 'elem_user_create', 'ELEMENT', 'system:user:create:button', 'system'),
  ('获取用户列表 API', 'api_user_list', 'API', 'system:user:list:api', 'system');
```

---

#### 第 3 步：开发阶段 - Admin 授权

**工作内容**：
- 将新生成的权限全部分配给 admin 用户
- 操作 `role_permission` 表
- 确保 admin 用户登录后可访问新功能

**两种管理员角色设计**：

| 角色类型 | 用户 | 权限来源 | 说明 |
|---------|------|----------|------|
| **超级管理员** | admin（唯一） | 系统预置，自动拥有全部权限 | 新功能缺省授权对象 |
| **系统管理员** | 可由 admin 创建/授权 | 由 admin 分配权限 | 日常运维角色 |

**示例**：
```sql
-- 分配给超级管理员角色（role_id=1）
INSERT INTO role_permission (role_id, permission_id, is_basic, is_effective)
SELECT 1, id, true, true FROM permission WHERE module_code = 'system';
```

---

#### 第 4 步：开发阶段 - 资源树构建

**工作内容**：
- 生成菜单项（MENU）资源、模块（MODULE）资源
- 将这两类资源写入 `permission` 表
- 在系统运行时提供资源维护页面
- 管理员可将菜单/模块设置为第 1 步中资源的父资源

**层级规则**：
- MENU 的父资源只能是 MODULE
- MODULE 的父资源只能是 MODULE（支持嵌套模块）
- API 的父资源可以是 MODULE/MENU/PAGE/ELEMENT/API 中的任何一种

**示例**：
```sql
-- 创建模块和菜单
INSERT INTO permission (name, code, type, parent_id, permission_key, module_code) VALUES
  ('系统设置', 'system', 'MODULE', NULL, 'system:menu', 'system'),
  ('用户管理', 'menu_user_management', 'MENU', 1, 'system:user:menu', 'system');

-- 设置父资源关系（将孤儿资源纳入树形结构）
UPDATE permission SET parent_id = 2 WHERE code IN ('page_user_list', 'elem_user_view', 'elem_user_create');
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

## 三、资源模型架构

### 3.1 资源层级结构（完整网状结构）

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
- 示例：`/api/user/list` API 的父资源可以是 `用户列表页面 (PAGE)`，也可以是 `用户管理菜单 (MENU)`

**⚠️ 循环引用防止**：
- 当 API 的父资源设置为另一个 API 时，必须验证**不能形成循环引用**
- 示例：`API-A → API-B → API-C → API-A` ❌ 不允许
- 验证逻辑：设置父资源前，检查新父资源的所有祖先是否包含当前资源

### 3.2 资源类型定义

| 类型 | 代码 | 说明 | 父资源类型 | 示例 |
|------|------|------|------------|------|
| MODULE | `MODULE` | 功能模块 | MODULE/null | 系统设置、零部件管理 |
| MENU | `MENU` | 导航菜单 | MODULE | 用户管理、角色管理 |
| PAGE | `PAGE` | 页面 | MENU/MODULE | 用户列表、用户详情 |
| ELEMENT | `ELEMENT` | 操作元素 | PAGE | 查看详情、新建用户 |
| API | `API` | 接口权限 | MODULE/MENU/PAGE/ELEMENT/API | 获取用户列表 API |

**注**：已取消 `PERMISSION` 资源类型，避免混淆。权限通过 `permission` 表和 `role_permission` 表管理。

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

---

## 五、数据库表结构

### 5.1 核心表（简化设计）

| 表名 | 用途 | 关键字段 | 说明 |
|------|------|----------|------|
| `permission` | 权限/资源表 | id, name, code, type, parent_id, permission_key, module_code | **合并原 resource 表和 permission 表** |
| `role_permission` | 角色 - 权限关联表 | role_id, permission_id, is_basic, is_default, is_effective | 角色权限配置，含生效状态 |
| `user_role` | 用户 - 角色关联表 | user_id, role_id | 用户与角色的关联 |
| `user_permission_adjust` | 用户权限调整表 | user_id, permission_id, adjust_type (ADD/REMOVE) | 用户权限调整记录 |
| `user` | 用户表 | id, username, real_name, company_id | 用户信息 |
| `role` | 角色表 | id, role_name, role_code, type | 角色定义 |

### 5.2 permission 表结构（核心表）

```sql
CREATE TABLE permission (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(100) NOT NULL,        -- 资源/权限名称
    code                VARCHAR(50) NOT NULL UNIQUE,  -- 资源编码
    type                VARCHAR(20) NOT NULL,         -- MODULE/MENU/PAGE/ELEMENT/API
    parent_id           BIGINT REFERENCES permission(id), -- 父资源 ID（可为 NULL）
    permission_key      VARCHAR(200) UNIQUE,          -- 权限码（唯一）
    module_code         VARCHAR(50) NOT NULL,         -- 所属模块编码
    sort_order          INTEGER DEFAULT 0,            -- 排序号
    is_visible          BOOLEAN DEFAULT true,         -- 是否可见（MENU 专用）
    is_system_protected BOOLEAN DEFAULT false,        -- 是否系统保护（MENU 专用）
    status              SMALLINT DEFAULT 1,           -- 1=启用，0=禁用
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

**设计优势**：
1. **简化架构**：资源和权限一体化，减少表关联
2. **直观映射**：一个资源对应一个权限码，易于理解
3. **维护方便**：只需维护一张表，降低数据不一致风险
4. **查询高效**：减少 JOIN 操作，提升查询性能

### 5.3 role_permission 表结构

```sql
CREATE TABLE role_permission (
    id                  BIGSERIAL PRIMARY KEY,
    role_id             BIGINT NOT NULL REFERENCES role(id),       -- 角色 ID
    permission_id       BIGINT NOT NULL REFERENCES permission(id), -- 权限 ID
    is_basic            BOOLEAN DEFAULT false,                     -- 是否基本权限（必须生效）
    is_default          BOOLEAN DEFAULT false,                     -- 是否缺省权限
    is_effective        BOOLEAN DEFAULT true,                      -- 是否生效
    adjust_reason       VARCHAR(500),                              -- 调整原因
    adjusted_by         BIGINT,                                    -- 调整人
    adjusted_at         TIMESTAMP,                                 -- 调整时间
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (role_id, permission_id)
);
```

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
-- 角色的有效权限 = 所有 is_effective = true 的权限
SELECT permission_id FROM role_permission 
WHERE role_id = ? AND is_effective = true;
```

### 5.4 user_permission_adjust 表结构

```sql
CREATE TABLE user_permission_adjust (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL REFERENCES user(id),       -- 用户 ID
    permission_id       BIGINT NOT NULL REFERENCES permission(id), -- 权限 ID
    adjust_type         VARCHAR(10) NOT NULL,                      -- ADD/REMOVE
    reason              VARCHAR(500),                              -- 调整原因
    adjusted_by         BIGINT,                                    -- 调整人
    adjusted_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, permission_id, adjust_type)
);
```

**调整规则**：
- 用户从角色继承的**基本权限**不能被减少（REMOVE 操作无效）
- 非基本权限可以灵活调整（ADD/REMOVE）

**用户最终权限计算**：
```
用户最终权限 = 
    (从所有角色继承的有效权限) 
    + (user_permission_adjust 中 ADD 的权限) 
    - (user_permission_adjust 中 REMOVE 的权限，但排除基本权限)
```

### 5.5 索引设计

```sql
-- permission 表索引
CREATE INDEX idx_permission_type ON permission(type);
CREATE INDEX idx_permission_parent_id ON permission(parent_id);
CREATE INDEX idx_permission_module_code ON permission(module_code);
CREATE UNIQUE INDEX idx_permission_key ON permission(permission_key) WHERE permission_key IS NOT NULL;
CREATE UNIQUE INDEX idx_permission_code ON permission(code);

-- role_permission 表索引
CREATE INDEX idx_role_permission_role ON role_permission(role_id);
CREATE INDEX idx_role_permission_effective ON role_permission(role_id, is_effective);

-- user_permission_adjust 表索引
CREATE INDEX idx_user_adjust_user ON user_permission_adjust(user_id);
CREATE INDEX idx_user_adjust_type ON user_permission_adjust(user_id, adjust_type);

-- user_role 表索引
CREATE INDEX idx_user_role_user ON user_role(user_id);
CREATE INDEX idx_user_role_role ON user_role(role_id);
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
│  (role.data_scope_default)                                       │
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

### 7.3 数据库表结构

#### role_data_scope 表（角色数据范围）

```sql
CREATE TABLE role_data_scope (
    id                  BIGSERIAL PRIMARY KEY,
    role_id             BIGINT NOT NULL REFERENCES role(id),       -- 角色 ID
    resource_id         BIGINT NOT NULL REFERENCES permission(id), -- 资源 ID
    scope_type          VARCHAR(20) NOT NULL,                      -- ALL/COMPANY/DEPT/DEPT_AND_SUB/SELF/CUSTOM
    is_default          BOOLEAN DEFAULT true,                      -- 是否缺省数据范围
    is_effective        BOOLEAN DEFAULT true,                      -- 是否生效
    custom_rule         TEXT,                                      -- 自定义规则（JSON 格式）
    adjust_reason       VARCHAR(500),                              -- 调整原因
    adjusted_by         BIGINT,                                    -- 调整人
    adjusted_at         TIMESTAMP,                                 -- 调整时间
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (role_id, resource_id)
);
```

**字段说明**：
- `is_default = true`：角色的缺省数据范围（创建角色时自动分配）
- `is_effective = false`：数据范围被调整（移除）
- `custom_rule`：自定义数据范围的规则（JSON 格式）

**自定义规则示例**：
```json
{
  "type": "work_area_ids",
  "operator": "IN",
  "values": [1, 2, 3]
}
```

#### user_data_scope 表（用户数据范围）

```sql
CREATE TABLE user_data_scope (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL REFERENCES user(id),       -- 用户 ID
    resource_id         BIGINT NOT NULL REFERENCES permission(id), -- 资源 ID
    scope_type          VARCHAR(20) NOT NULL,                      -- ALL/COMPANY/DEPT/DEPT_AND_SUB/SELF/CUSTOM
    source_type         VARCHAR(20) DEFAULT 'inherited',           -- inherited/direct
    is_effective        BOOLEAN DEFAULT true,                      -- 是否生效
    custom_rule         TEXT,                                      -- 自定义规则（JSON 格式）
    adjust_reason       VARCHAR(500),                              -- 调整原因
    adjusted_by         BIGINT,                                    -- 调整人
    adjusted_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, resource_id)
);
```

**字段说明**：
- `source_type = 'inherited'`：从角色继承的数据范围
- `source_type = 'direct'`：直接分配给用户的数据范围
- `is_effective = false`：用户数据范围被调整（移除）

### 7.4 数据范围计算逻辑

#### 角色最终数据范围

```sql
-- 角色的有效数据范围
SELECT resource_id, scope_type, custom_rule
FROM role_data_scope
WHERE role_id = ? 
  AND is_effective = true;
```

#### 用户继承数据范围（从所有角色）

```sql
-- 用户从所有角色继承的数据范围（并集）
SELECT DISTINCT rds.resource_id, rds.scope_type, rds.custom_rule
FROM user_role ur
JOIN role_data_scope rds ON ur.role_id = rds.role_id
WHERE ur.user_id = ? 
  AND rds.is_effective = true;
```

#### 用户最终数据范围

```
用户最终数据范围 = 
    (从所有角色继承的有效数据范围)
    + (user_data_scope 中 ADD 的数据范围)
    - (user_data_scope 中 REMOVE 的数据范围)
```

### 7.5 数据范围调整规则

| 调整类型 | 角色数据范围 | 用户数据范围 |
|----------|-------------|-------------|
| **扩大范围** | ✅ 允许（如：SELF → COMPANY） | ✅ 允许 |
| **缩小范围** | ✅ 允许（如：ALL → DEPT） | ✅ 允许 |
| **自定义规则** | ✅ 允许（需 JSON 规则） | ✅ 允许 |
| **移除范围** | ✅ 允许（设置 is_effective = false） | ✅ 允许 |

### 7.6 数据范围 SQL 注入示例

```java
@Service
public class DataScopeService {
    
    /**
     * 生成数据范围 WHERE 条件
     */
    public String generateWhereClause(Long userId, Long resourceId) {
        // 获取用户对该资源的数据范围
        UserDataScope scope = getUserDataScope(userId, resourceId);
        
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

### 7.7 数据范围配置页面

| 页面名称 | 路径 | 功能 |
|---------|------|------|
| 角色数据范围配置 | `/system/role-data-scope` | 配置角色的缺省数据范围和调整 |
| 用户数据范围配置 | `/system/user-data-scope` | 配置用户的数据范围（可覆盖角色） |

---

### 7.8 循环引用防止（通用设计）

**适用范围**：
- **所有允许设置父资源的资源类型**：MODULE、MENU、PAGE、ELEMENT、API
- 任何资源类型都可能形成循环引用

**循环引用场景示例**：

```
# 场景 1：MODULE 循环
MODULE-A (parent: MODULE-B)
MODULE-B (parent: MODULE-C)
MODULE-C (parent: MODULE-A)  ← 循环 ❌

# 场景 2：MENU 循环（通过 MODULE）
MODULE-A (parent: NULL)
  └── MENU-B (parent: MODULE-A)
MODULE-C (parent: MENU-B)  ← 不允许，MENU 不能作为 MODULE 的父资源 ✅

# 场景 3：PAGE 循环（通过 ELEMENT）
PAGE-A (parent: MENU-X)
  └── ELEMENT-B (parent: PAGE-A)
      └── PAGE-C (parent: ELEMENT-B)
PAGE-C (parent: PAGE-A)  ← 如果允许，形成循环 ❌

# 场景 4：API 循环
API-A (parent: API-B)
API-B (parent: API-C)
API-C (parent: API-A)  ← 循环 ❌
```

**后端验证逻辑（通用）**：
```java
@Service
public class PermissionService {
    
    /**
     * 设置父资源前验证是否会导致循环引用（通用方法）
     * 适用于所有资源类型：MODULE、MENU、PAGE、ELEMENT、API
     */
    @Transactional
    public void updateParent(Long resourceId, Long newParentId) {
        if (newParentId == null) {
            // 设置为顶级资源，无需验证
            permissionMapper.updateParent(resourceId, null);
            return;
        }
        
        // 1. 获取当前资源信息
        Permission currentResource = permissionMapper.selectById(resourceId);
        if (currentResource == null) {
            throw new RuntimeException("资源不存在：" + resourceId);
        }
        
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
        permissionMapper.updateParent(resourceId, newParentId);
        
        // 5. 清除缓存
        permissionCacheService.evictAllMenuCache();
    }
    
    /**
     * 验证父资源类型是否符合规则
     */
    private void validateParentType(String resourceType, Long newParentId) {
        Permission newParent = permissionMapper.selectById(newParentId);
        if (newParent == null) {
            throw new RuntimeException("父资源不存在：" + newParentId);
        }
        
        String parentType = newParent.getType();
        
        // 类型匹配规则
        switch (resourceType) {
            case "MODULE":
                // MODULE 的父资源只能是 MODULE 或 NULL
                if (!"MODULE".equals(parentType)) {
                    throw new RuntimeException(
                        "MODULE 类型的父资源只能是 MODULE 类型，当前为：" + parentType
                    );
                }
                break;
            case "MENU":
                // MENU 的父资源只能是 MODULE
                if (!"MODULE".equals(parentType)) {
                    throw new RuntimeException(
                        "MENU 类型的父资源只能是 MODULE 类型，当前为：" + parentType
                    );
                }
                break;
            case "PAGE":
                // PAGE 的父资源可以是 MENU 或 MODULE
                if (!Arrays.asList("MENU", "MODULE").contains(parentType)) {
                    throw new RuntimeException(
                        "PAGE 类型的父资源只能是 MENU 或 MODULE 类型，当前为：" + parentType
                    );
                }
                break;
            case "ELEMENT":
                // ELEMENT 的父资源只能是 PAGE
                if (!"PAGE".equals(parentType)) {
                    throw new RuntimeException(
                        "ELEMENT 类型的父资源只能是 PAGE 类型，当前为：" + parentType
                    );
                }
                break;
            case "API":
                // API 的父资源可以是任意类型（MODULE/MENU/PAGE/ELEMENT/API）
                // 无需类型限制，但仍需检查循环引用
                break;
            default:
                throw new RuntimeException("未知资源类型：" + resourceType);
        }
    }
    
    /**
     * 检查设置新父资源是否会导致循环引用（递归检查）
     */
    public boolean wouldCreateCycle(Long resourceId, Long newParentId) {
        // 如果新父资源就是当前资源，直接拒绝
        if (resourceId.equals(newParentId)) {
            return true;
        }
        
        // 遍历新父资源的所有祖先，检查是否包含当前资源
        Set<Long> ancestors = getAllAncestors(newParentId);
        return ancestors.contains(resourceId);
    }
    
    /**
     * 获取资源的所有祖先（递归向上遍历）
     * 返回的祖先集合用于循环引用检查
     */
    private Set<Long> getAllAncestors(Long resourceId) {
        Set<Long> ancestors = new HashSet<>();
        Long currentId = resourceId;
        int depth = 0;
        final int MAX_DEPTH = 100; // 防止数据库已有循环引用导致死循环
        
        while (currentId != null && depth < MAX_DEPTH) {
            Permission p = permissionMapper.selectById(currentId);
            if (p == null || p.getParentId() == null) {
                break;
            }
            
            // 防止数据库中已有循环引用导致死循环
            if (ancestors.contains(p.getParentId())) {
                break;
            }
            
            ancestors.add(p.getParentId());
            currentId = p.getParentId();
            depth++;
        }
        
        if (depth >= MAX_DEPTH) {
            throw new RuntimeException(
                "检测异常：资源层级超过最大深度限制（" + MAX_DEPTH + "），可能存在循环引用"
            );
        }
        
        return ancestors;
    }
    
    /**
     * 获取资源路径（用于错误提示）
     */
    public String getResourcePath(Long resourceId) {
        List<String> path = new ArrayList<>();
        Long currentId = resourceId;
        int depth = 0;
        final int MAX_DEPTH = 100;
        
        while (currentId != null && depth < MAX_DEPTH) {
            Permission p = permissionMapper.selectById(currentId);
            if (p == null) {
                break;
            }
            
            path.add(p.getName() + "(" + p.getType() + ")");
            
            if (p.getParentId() == null) {
                break;
            }
            
            // 防止死循环
            if (path.stream().filter(s -> s.contains(p.getName())).count() > 1) {
                path.add("... (循环引用)");
                break;
            }
            
            currentId = p.getParentId();
            depth++;
        }
        
        Collections.reverse(path);
        return String.join(" → ", path);
    }
    
    /**
     * 获取循环引用路径（用于错误提示）
     */
    public String getCyclePath(Long resourceId, Long newParentId) {
        List<String> path = new ArrayList<>();
        Long currentId = newParentId;
        
        path.add(getResourceName(resourceId));
        path.add(getResourceName(newParentId));
        
        while (currentId != null) {
            Permission p = permissionMapper.selectById(currentId);
            if (p == null || p.getParentId() == null) {
                break;
            }
            
            path.add(getResourceName(p.getParentId()));
            
            if (p.getParentId().equals(resourceId)) {
                break;
            }
            
            currentId = p.getParentId();
        }
        
        return String.join(" → ", path);
    }
    
    private String getResourceName(Long resourceId) {
        Permission p = permissionMapper.selectById(resourceId);
        return p != null ? p.getName() + "(" + p.getType() + ")" : "未知";
    }
}
```

**前端交互设计（通用）**：
```vue
<!-- components/ParentResourceSelector.vue -->
<template>
  <div class="parent-selector">
    <el-select 
      v-model="selectedParent" 
      @change="validateParent"
      :loading="loading"
      placeholder="请选择父资源"
      clearable
    >
      <el-option
        v-for="item in availableParents"
        :key="item.id"
        :label="item.name"
        :value="item.id"
        :disabled="item.disabled"
      >
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>{{ item.name }}</span>
          <span style="font-size: 12px; color: #909399;">
            {{ getTypeLabel(item.type) }}
          </span>
        </div>
        <div v-if="item.disabled" style="color: #f56c6c; font-size: 12px; margin-top: 4px;">
          <el-icon><Warning /></el-icon>
          {{ item.disabledReason }}
        </div>
      </el-option>
    </el-select>
    
    <!-- 当前选择的父资源路径预览 -->
    <div v-if="selectedParent && parentPath" class="path-preview">
      <el-alert
        title="父资源路径"
        type="info"
        :closable="false"
        show-icon
      >
        <template #default>
          <div class="path-chain">
            <span v-for="(node, index) in parentPath" :key="index">
              <el-tag size="small" :type="getNodeTagType(node.type)">
                {{ node.name }}
              </el-tag>
              <span v-if="index < parentPath.length - 1" class="arrow">→</span>
            </span>
          </div>
        </template>
      </el-alert>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { getPermissionTree, checkCycle } from '@/api/permission';

const props = defineProps({
  currentResourceId: Number,  // 当前资源 ID
  currentResourceType: String // 当前资源类型
});

const emit = defineEmits(['update:modelValue']);

const selectedParent = ref(null);
const availableParents = ref([]);
const loading = ref(false);
const parentPath = ref([]);

// 资源类型标签
const typeLabels = {
  MODULE: '模块',
  MENU: '菜单',
  PAGE: '页面',
  ELEMENT: '元素',
  API: '接口'
};

// 获取类型标签
const getTypeLabel = (type) => typeLabels[type] || type;

// 获取标签颜色
const getNodeTagType = (type) => {
  const typeMap = {
    MODULE: 'danger',
    MENU: 'warning',
    PAGE: 'success',
    ELEMENT: 'info',
    API: 'primary'
  };
  return typeMap[type] || 'info';
};

// 加载可选父资源
const loadAvailableParents = async () => {
  loading.value = true;
  try {
    const res = await getPermissionTree();
    const allPermissions = flattenTree(res.data);
    
    availableParents.value = allPermissions
      .filter(p => p.id !== props.currentResourceId) // 排除自己
      .map(p => {
        const validation = validateParentType(props.currentResourceType, p.type);
        return {
          ...p,
          disabled: !validation.allowed || validation.wouldCreateCycle,
          disabledReason: validation.reason
        };
      });
  } finally {
    loading.value = false;
  }
};

// 验证父资源类型是否符合规则
const validateParentType = (currentType, parentType) => {
  const rules = {
    MODULE: { allowedTypes: ['MODULE'], message: 'MODULE 的父资源只能是 MODULE' },
    MENU: { allowedTypes: ['MODULE'], message: 'MENU 的父资源只能是 MODULE' },
    PAGE: { allowedTypes: ['MENU', 'MODULE'], message: 'PAGE 的父资源只能是 MENU 或 MODULE' },
    ELEMENT: { allowedTypes: ['PAGE'], message: 'ELEMENT 的父资源只能是 PAGE' },
    API: { allowedTypes: ['MODULE', 'MENU', 'PAGE', 'ELEMENT', 'API'], message: '' }
  };
  
  const rule = rules[currentType];
  const allowed = rule.allowedTypes.includes(parentType);
  
  return {
    allowed,
    reason: allowed ? '' : rule.message
  };
};

// 检查是否会导致循环引用
const checkWouldCreateCycle = async (newParentId) => {
  try {
    const res = await checkCycle({
      resourceId: props.currentResourceId,
      newParentId: newParentId
    });
    return {
      wouldCreateCycle: res.data.wouldCreateCycle,
      cyclePath: res.data.cyclePath
    };
  } catch (error) {
    console.error('循环引用检查失败:', error);
    return { wouldCreateCycle: false, cyclePath: '' };
  }
};

// 验证父资源选择
const validateParent = async (newParentId) => {
  if (!newParentId) {
    parentPath.value = [];
    emit('update:modelValue', null);
    return;
  }
  
  // 检查循环引用
  const cycleCheck = await checkWouldCreateCycle(newParentId);
  
  if (cycleCheck.wouldCreateCycle) {
    ElMessage.error({
      message: '选择此项会导致循环引用：' + cycleCheck.cyclePath,
      duration: 5000
    });
    selectedParent.value = null;
    parentPath.value = [];
    emit('update:modelValue', null);
    return;
  }
  
  // 获取父资源路径预览
  parentPath.value = await getParentPath(newParentId);
  
  emit('update:modelValue', newParentId);
};

// 获取父资源路径（用于预览）
const getParentPath = async (resourceId) => {
  const res = await $get(`/api/permission/${resourceId}/path`);
  return res.data.path || [];
};

// 监听当前资源变化
watch(() => props.currentResourceId, () => {
  loadAvailableParents();
}, { immediate: true });

// 初始化
loadAvailableParents();
</script>

<style scoped>
.parent-selector {
  width: 100%;
}

.path-preview {
  margin-top: 10px;
}

.path-chain {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.arrow {
  color: #909399;
  font-weight: bold;
}
</style>
```

**验证 API**：
```java
@RestController
@RequestMapping("/api/permission")
public class PermissionController {
    
    @Autowired
    private PermissionService permissionService;
    
    /**
     * 检查设置父资源是否会导致循环引用
     * 适用于所有资源类型
     */
    @PostMapping("/check-cycle")
    public Result<Map<String, Object>> checkCycle(
            @RequestBody CheckCycleRequest request) {
        
        boolean wouldCreateCycle = permissionService.wouldCreateCycle(
            request.getResourceId(), 
            request.getNewParentId()
        );
        
        Map<String, Object> result = new HashMap<>();
        result.put("wouldCreateCycle", wouldCreateCycle);
        
        if (wouldCreateCycle) {
            String cyclePath = permissionService.getCyclePath(
                request.getResourceId(), 
                request.getNewParentId()
            );
            result.put("cyclePath", cyclePath);
        }
        
        return Result.success(result);
    }
    
    /**
     * 获取资源的父资源路径（用于前端预览）
     */
    @GetMapping("/{id}/path")
    public Result<List<Map<String, Object>>> getResourcePath(@PathVariable Long id) {
        String pathStr = permissionService.getResourcePath(id);
        
        // 解析路径字符串为结构化数据
        List<Map<String, Object>> path = parsePathString(pathStr);
        
        return Result.success(path);
    }
    
    private List<Map<String, Object>> parsePathString(String pathStr) {
        // 解析 "MODULE-A → MENU-B → PAGE-C" 格式
        // 返回 [{name: "MODULE-A", type: "MODULE"}, ...]
        List<Map<String, Object>> result = new ArrayList<>();
        if (pathStr == null || pathStr.isEmpty()) {
            return result;
        }
        
        String[] parts = pathStr.split(" → ");
        for (String part : parts) {
            String[] nameAndType = part.split("\\(");
            if (nameAndType.length == 2) {
                Map<String, Object> node = new HashMap<>();
                node.put("name", nameAndType[0]);
                node.put("type", nameAndType[1].replace(")", ""));
                result.add(node);
            }
        }
        return result;
    }
}
```

**数据库触发器（最后一道防线）**：
```sql
-- 添加触发器，防止循环引用（数据库层面的最后一道防线）
CREATE OR REPLACE FUNCTION check_permission_cycle()
RETURNS TRIGGER AS $$
DECLARE
    ancestor RECORD;
    visited BIGINT[] := ARRAY[]::BIGINT[];
    current_id BIGINT := NEW.parent_id;
    depth INTEGER := 0;
    max_depth INTEGER := 100;
BEGIN
    -- 遍历祖先，检查是否包含当前资源
    WHILE current_id IS NOT NULL AND depth < max_depth LOOP
        -- 检查是否已访问过（防止死循环）
        IF current_id = ANY(visited) THEN
            RAISE EXCEPTION 
                '循环引用检测：检测到数据库中存在循环引用 (visited=%)', 
                array_length(visited, 1);
        END IF;
        
        -- 检查是否指向自己
        IF current_id = NEW.id THEN
            RAISE EXCEPTION 
                '循环引用检测：资源不能将自己设为父资源 (id=%)', 
                NEW.id;
        END IF;
        
        visited := array_append(visited, current_id);
        
        SELECT parent_id INTO current_id 
        FROM permission 
        WHERE id = current_id;
        
        depth := depth + 1;
    END LOOP;
    
    IF depth >= max_depth THEN
        RAISE EXCEPTION 
            '循环引用检测：资源层级超过最大深度限制 (%)，可能存在循环引用', 
            max_depth;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 创建触发器（适用于所有资源类型）
CREATE TRIGGER trg_permission_before_update
    BEFORE INSERT OR UPDATE ON permission
    FOR EACH ROW
    WHEN (NEW.parent_id IS NOT NULL)
    EXECUTE FUNCTION check_permission_cycle();
```

**前端批量设置父资源时的验证**：
```vue
<!-- 批量设置父资源对话框 -->
<template>
  <el-dialog title="批量设置父资源" v-model="dialogVisible">
    <div class="batch-set-parent">
      <p>已选择 {{ selectedResources.length }} 个资源：</p>
      <ul class="resource-list">
        <li v-for="r in selectedResources" :key="r.id">
          {{ r.name }} ({{ r.type }})
        </li>
      </ul>
      
      <el-form :model="form" label-width="120px">
        <el-form-item label="父资源">
          <ParentResourceSelector
            v-model="form.newParentId"
            :current-resource-id="null"
            :current-resource-type="null"
            :multiple="true"
            :exclude-resource-ids="selectedResources.map(r => r.id)"
          />
        </el-form-item>
      </el-form>
      
      <!-- 预览变更 -->
      <div v-if="form.newParentId" class="change-preview">
        <el-alert title="变更预览" type="warning" :closable="false">
          <ul>
            <li v-for="r in selectedResources" :key="r.id">
              {{ r.name }} → {{ newParentName }}
            </li>
          </ul>
        </el-alert>
      </div>
    </div>
    
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleBatchSet" :loading="batchLoading">
        确认设置
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
const handleBatchSet = async () => {
  batchLoading.value = true;
  try {
    // 批量验证每个资源是否会导致循环引用
    const validationPromises = selectedResources.value.map(r =>
      checkCycle({ resourceId: r.id, newParentId: form.value.newParentId })
    );
    
    const validationResults = await Promise.all(validationPromises);
    
    // 检查是否有循环引用
    const hasCycle = validationResults.some(r => r.data.wouldCreateCycle);
    if (hasCycle) {
      ElMessage.error('批量设置失败：部分资源会导致循环引用');
      return;
    }
    
    // 执行批量设置
    await $put('/api/permission/batch-parent', {
      resourceIds: selectedResources.value.map(r => r.id),
      newParentId: form.value.newParentId
    });
    
    ElMessage.success('批量设置父资源成功');
    dialogVisible.value = false;
    emit('refresh');
  } catch (error) {
    ElMessage.error('批量设置失败：' + error.message);
  } finally {
    batchLoading.value = false;
  }
};
</script>
```

---

## 八、两种管理员角色设计

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

## 八、后端实现方案

### 7.1 权限注解

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasPermission(#permission)")
public @interface ApiPermission {
    String value(); // 权限代码，如 "system:user:list:api"
}
```

### 7.2 Controller 示例

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

### 7.3 权限拦截器

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

### 7.4 权限服务

```java
@Service
public class PermissionService {
    
    /**
     * 获取权限树形结构
     */
    public List<PermissionDTO> getPermissionTree() {
        List<Permission> allPermissions = permissionMapper.selectAll();
        return buildTree(allPermissions, null);
    }
    
    /**
     * 获取孤儿权限（无父资源）
     */
    public List<PermissionDTO> getOrphanedPermissions() {
        List<Permission> permissions = permissionMapper.selectOrphaned();
        return PermissionConverter.toDTOList(permissions);
    }
    
    /**
     * 批量设置父资源
     */
    @Transactional
    public void batchSetParent(List<Long> permissionIds, Long parentId) {
        for (Long id : permissionIds) {
            permissionMapper.updateParent(id, parentId);
        }
        // 清除权限缓存
        permissionCacheService.evictAll();
    }
}
```

---

## 九、前端实现方案

### 8.1 权限指令

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

### 8.2 路由守卫

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

### 8.3 刷新菜单功能

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

### 8.4 权限管理页面

```vue
<!-- views/system/PermissionList.vue -->
<template>
  <div class="permission-list">
    <!-- 筛选孤儿资源 -->
    <el-checkbox v-model="showOrphaned">显示孤儿资源</el-checkbox>
    
    <!-- 权限树形表格 -->
    <el-table :data="filteredPermissions" row-key="id" :tree-props="{ children: 'children' }">
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="type" label="类型" />
      <el-table-column prop="permissionKey" label="权限码" />
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button @click="handleEdit(row)">编辑</el-button>
          <el-button @click="handleSetParent(row)">设置父资源</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 刷新菜单按钮 -->
    <el-button type="primary" @click="handleRefreshMenu">刷新菜单</el-button>
  </div>
</template>

<script setup>
import { useUserStore } from '@/stores/user';

const userStore = useUserStore();

const handleRefreshMenu = async () => {
  await userStore.refreshMenus();
  ElMessage.success('侧边栏菜单已刷新');
};
</script>
```

---

## 十、权限缓存机制

### 9.1 缓存结构

```
# 用户权限缓存
user:permission:{userId} → Set<Long> permissionIds, TTL: 30 minutes

# 角色权限缓存
role:permission:{roleId} → Set<Long> permissionIds, TTL: 60 minutes

# 用户菜单缓存
user:menus:{userId} → Array<menu>, TTL: 30 minutes
```

### 9.2 缓存策略

| 操作 | 缓存处理 |
|------|----------|
| 用户登录 | 写入用户权限缓存 + 菜单缓存 |
| 权限查询 | 先查缓存，未命中则查数据库 |
| 角色权限变更 | 清除角色缓存 + 该角色下所有用户缓存 |
| 用户权限变更 | 清除该用户缓存 |
| 资源结构变更 | 清除所有用户菜单缓存 |
| 手动刷新 | 强制重新加载并更新缓存 |

### 9.3 缓存服务

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
        
        // 未命中，从数据库加载
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
        
        // 清除该角色下所有用户的缓存
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

## 十一、实施步骤

### 10.1 Phase 1：数据库迁移（1 天）

**工作内容**：
- [ ] 创建 `permission` 表（合并原 resource 和 permission 表）
- [ ] 迁移现有数据到新表结构
- [ ] 创建 `role_type_default_permissions` 表（角色类型缺省权限）
- [ ] 创建 `ROLE_SUPER_ADMIN` 和 `ROLE_SYSTEM_ADMIN` 角色
- [ ] 将 admin 用户关联到 `ROLE_SUPER_ADMIN`

**验收标准**：
- 数据库包含两种管理员角色
- admin 用户拥有超级管理员角色
- `permission` 表包含完整的 MODULE/MENU/PAGE/ELEMENT/API 数据

---

### 10.2 Phase 2：后端服务开发（2 天）

**工作内容**：
- [ ] 开发权限扫描器（自动从代码提取 API、页面路由）
- [ ] 开发权限码生成器（自动生成唯一权限码）
- [ ] 开发孤儿权限管理 API
- [ ] 开发批量设置父资源 API
- [ ] 完善权限缓存服务
- [ ] 添加"刷新菜单"API

**API 清单**：
| 方法 | 端点 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/permission/orphaned` | 获取孤儿权限 | `system:permission:view:api` |
| PUT | `/api/permission/batch-parent` | 批量设置父资源 | `system:permission:edit:api` |
| POST | `/api/permission/scan` | 扫描代码生成权限 | `system:permission:create:api` |
| POST | `/api/permission/generate` | 生成权限码 | `system:permission:create:api` |
| POST | `/api/menu/refresh` | 刷新菜单缓存 | `system:menu:refresh:api` |

---

### 10.3 Phase 3：前端页面开发（2 天）

**工作内容**：
- [ ] 权限管理页面添加"孤儿权限"筛选视图
- [ ] 添加批量设置父资源功能（拖拽或批量选择）
- [ ] 在全局导航栏添加"刷新菜单"按钮
- [ ] 完善权限配置页面（角色基本权限、缺省权限、完整权限）
- [ ] 添加权限码自动生成 UI

---

### 10.4 Phase 4：权限缓存优化（1 天）

**工作内容**：
- [ ] 实现 Redis 权限缓存（TTL: 30 分钟）
- [ ] 实现权限变更时自动失效缓存
- [ ] 实现手动刷新按钮（强制更新缓存）
- [ ] 性能测试（目标：< 50ms）

---

### 10.5 Phase 5：测试验证（1 天）

**测试场景**：

| 场景 | 预期结果 | 验证方法 |
|------|----------|----------|
| 新功能开发 | 权限无父资源，admin 可访问 | 创建测试功能，验证 admin 访问 |
| 权限码生成 | 权限码唯一，符合规范 | 检查生成的权限码 |
| 父资源设置 | 可批量设置，菜单实时更新 | 设置父资源后刷新菜单 |
| 权限分配 | 角色/用户权限正确生效 | 分配权限后验证用户访问 |
| 缓存性能 | 响应时间 < 50ms | 压测验证 |

---

### 10.6 总计划

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
| **孤儿权限** | 无父资源的权限（parent_id = NULL），开发阶段产生 |
| **超级管理员** | 系统预置的最高权限角色，仅 admin 用户 |
| **系统管理员** | 可由 admin 创建和授权的运维角色 |
| **权限码** | 唯一标识权限的字符串，格式：`module:resource:action:type` |
| **刷新菜单** | 手动触发菜单缓存更新，无需退出系统 |
| **基本权限** | 角色必须拥有的权限，不可移除（`is_basic = true`） |
| **缺省权限** | 角色创建时自动分配的权限（`is_default = true`） |
| **生效权限** | 当前有效的权限（`is_effective = true`） |

---

## 设计变更说明（V4.0 → V4.1）

### 核心变更

**V4.0 设计**：
- `resource` 表：存储资源（MODULE/MENU/PAGE/ELEMENT/API）
- `permission` 表：存储权限定义
- `permission_resource` 表：关联权限和资源

**V4.1 设计**：
- **合并为一张表**：`permission` 表同时存储资源和权限
- 每个资源自带 `permission_key` 字段
- 通过 `role_permission` 表直接关联角色和权限

### 变更优势

| 方面 | V4.0 | V4.1 | 改进 |
|------|------|------|------|
| 表数量 | 3 张（resource + permission + permission_resource） | 1 张（permission） | **减少 2 张** |
| 查询复杂度 | 需要 JOIN 3 张表 | 单表查询 | **简化** |
| 数据一致性 | 需维护 3 张表关联 | 单表自关联 | **提升** |
| 理解难度 | 资源和权限分离 | 资源即权限 | **降低** |
| 维护成本 | 高 | 低 | **降低 50%** |

### 迁移路径

```sql
-- 1. 创建新 permission 表（合并后）
CREATE TABLE permission_new (...);

-- 2. 从旧 resource 表迁移数据
INSERT INTO permission_new (id, name, code, type, parent_id, permission_key, module_code, ...)
SELECT id, name, code, type, parent_id, permission_key, module_code, ...
FROM resource;

-- 3. 删除旧表
DROP TABLE resource;
DROP TABLE permission;
DROP TABLE permission_resource;

-- 4. 重命名新表
ALTER TABLE permission_new RENAME TO permission;
```

---

**文档版本**：V4.1  
**创建日期**：2026-04-04  
**状态**：待柳生审核  
**下次更新**：根据审核意见修订
