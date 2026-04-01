# 权限体系设计方案 V3（最终版）

_最后更新：2026-03-30_

---

## 1. 核心概念

### 1.1 权限定义

**一个功能 = 一个权限**

```
菜单 ──→ URL ──→ 页面 ──→ 元素 ──→ 功能(权限)

示例：
用户管理 → /user/management → 用户列表页
                            ├── [新增] 按钮 → user:create:action
                            ├── [编辑] 按钮 → user:edit:action
                            ├── [删除] 按钮 → user:delete:action
                            └── [列表] 表格 → user:list:view
```

### 1.2 权限类型

| 类型 | 说明 | 示例 |
|------|------|------|
| `VIEW` | 查看类权限（只读） | user:list:view, role:list:view |
| `ACTION` | 操作类权限（增删改） | user:create:action, user:delete:action |

### 1.3 权限编码规范

```
格式：{模块}:{实体}:{操作类型}

模块：user, role, workarea, company, system, permission, auth...
实体：profile, list, detail, config...
操作类型：view, action

示例：
auth:login:action          - 登录系统
auth:logout:action         - 退出系统
user:profile:view          - 查看个人资料
user:profile:edit:action   - 编辑个人资料
user:list:view             - 查看用户列表
user:create:action         - 创建用户
```

### 1.4 权限集合分级

```
基本权限集合（Base Permission Set）
├── 所有角色必备的权限
├── 系统内置，不可删除
└── 示例：登录、退出、修改个人信息

缺省权限集合（Default Permission Set）
├── 角色创建时的默认权限
├── 包含基本权限集合
└── 可由上级角色配置

实际权限（Actual Permission）
├── 用户最终拥有的权限
├── 可由上级角色单独调整
└── 不超过调整者自身权限
```

### 1.5 角色层级与管理关系

```
角色层级（Level 0 最高，Level 4 最低）

系统管理员 [Level 0]
├── 权限：全部
├── 数据范围：全部
└── 可管理：所有角色
    │
    ├─→ 甲方管理员 [Level 1]
    │   ├── 权限：甲方管理相关（由系统管理员授权）
    │   ├── 数据范围：甲方相关数据
    │   └── 可管理：甲方下属角色
    │       │
    │       ├─→ 甲方项目经理 [Level 2]
    │       │   └── 权限：项目管理相关
    │       │
    │       └─→ 甲方普通用户 [Level 2]
    │           └── 权限：基本权限
    │
    ├─→ 乙方管理员 [Level 1]
    │   ├── 权限：乙方管理相关
    │   └── 可管理：乙方下属角色
    │
    └─→ 监理方管理员 [Level 1]
        ├── 权限：监理管理相关
        └── 可管理：监理方下属角色

权限约束：下级权限 ⊆ 上级权限
```

---

## 2. 角色层级关系图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           角色层级关系树                                      │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  系统管理员 (ROLE_SYSTEM_ADMIN) [Level 0]                                    │
│  └── 权限：全部 51 个权限                                                    │
│  └── 数据范围：全部数据                                                      │
│      │                                                                       │
│      ├──────────────────────────────────────────────────────────────┐       │
│      │                                                              │       │
│      ↓                                                              ↓       │
│  甲方管理员 [Level 1]                                        乙方管理员 [L1]  │
│  └── 权限：约 20 个                                          └── 权限：约 15个│
│  └── 数据：甲方相关                                           └── 数据：乙方  │
│      │                                                          │           │
│      ├─────→ 甲方项目经理 [L2]                                  ├─────→ ...   │
│      │         └── 权限：约 10 个                               │             │
│      │                                                          │             │
│      ├─────→ 甲方作业区项目经理 [L3]                            └─────→ ...   │
│      │         └── 权限：约 8 个                                              │
│      │             │                                                         │
│      │             ├─────→ 甲方作业区项目主管 [L4]                           │
│      │             │         └── 权限：约 4 个                               │
│      │             │                                                         │
│      │             └─────→ 甲方作业区项目经办人 [L4]                         │
│      │                       └── 权限：约 3 个                               │
│      │                                                                       │
│      ├─────→ 甲方项目经办人 [L3]                                             │
│      │         └── 权限：约 5 个                                             │
│      │                                                                       │
│      └─────→ 甲方普通用户 [L2]                                               │
│                └── 权限：基本权限                                            │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 3. 数据库表设计

### 3.1 权限分组表

```sql
CREATE TABLE permission_groups (
    id BIGSERIAL PRIMARY KEY,
    group_code VARCHAR(50) NOT NULL UNIQUE,
    group_name VARCHAR(200) NOT NULL,
    group_description TEXT,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 3.2 权限表（扩展）

```sql
ALTER TABLE permissions ADD COLUMN IF NOT EXISTS permission_type VARCHAR(20) DEFAULT 'ACTION';
ALTER TABLE permissions ADD COLUMN IF NOT EXISTS module_code VARCHAR(50);
ALTER TABLE permissions ADD COLUMN IF NOT EXISTS entity_code VARCHAR(50);
ALTER TABLE permissions ADD COLUMN IF NOT EXISTS group_code VARCHAR(50);
ALTER TABLE permissions ADD COLUMN IF NOT EXISTS is_base BOOLEAN DEFAULT FALSE;
ALTER TABLE permissions ADD COLUMN IF NOT EXISTS sort_order INT DEFAULT 0;
```

### 3.3 角色表（扩展层级）

```sql
ALTER TABLE roles ADD COLUMN IF NOT EXISTS role_level INT DEFAULT 0;
ALTER TABLE roles ADD COLUMN IF NOT EXISTS managed_by_role_id BIGINT REFERENCES roles(id);

COMMENT ON COLUMN roles.role_level IS '角色层级（0最高）';
COMMENT ON COLUMN roles.managed_by_role_id IS '管理此角色的上级角色';
```

### 3.4 API 接口表

```sql
CREATE TABLE apis (
    id BIGSERIAL PRIMARY KEY,
    http_method VARCHAR(10) NOT NULL,
    api_path VARCHAR(500) NOT NULL,
    api_name VARCHAR(200),
    api_description TEXT,
    module_code VARCHAR(50),
    is_deprecated BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(http_method, api_path)
);
```

### 3.5 权限-API 关联表

```sql
CREATE TABLE permission_apis (
    id BIGSERIAL PRIMARY KEY,
    permission_id BIGINT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    api_id BIGINT NOT NULL REFERENCES apis(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(permission_id, api_id)
);
```

### 3.6 页面表

```sql
CREATE TABLE pages (
    id BIGSERIAL PRIMARY KEY,
    page_code VARCHAR(100) NOT NULL UNIQUE,
    page_name VARCHAR(200) NOT NULL,
    page_path VARCHAR(500),
    page_component VARCHAR(500),
    parent_page_id BIGINT REFERENCES pages(id),
    page_description TEXT,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 3.7 页面元素表

```sql
CREATE TABLE page_elements (
    id BIGSERIAL PRIMARY KEY,
    page_id BIGINT NOT NULL REFERENCES pages(id) ON DELETE CASCADE,
    element_code VARCHAR(100) NOT NULL,
    element_name VARCHAR(200) NOT NULL,
    element_type VARCHAR(50),
    target_page_id BIGINT REFERENCES pages(id),
    element_selector VARCHAR(500),
    element_description TEXT,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(page_id, element_code)
);
```

### 3.8 元素-权限关联表

```sql
CREATE TABLE element_permissions (
    id BIGSERIAL PRIMARY KEY,
    element_id BIGINT NOT NULL REFERENCES page_elements(id) ON DELETE CASCADE,
    permission_id BIGINT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(element_id, permission_id)
);
```

### 3.9 角色缺省权限表

```sql
CREATE TABLE role_default_permissions (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id BIGINT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    is_inherited BOOLEAN DEFAULT FALSE,
    configured_by BIGINT REFERENCES users(id),
    configured_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(role_id, permission_id)
);
```

### 3.10 数据权限模板表

```sql
CREATE TABLE data_permission_templates (
    id BIGSERIAL PRIMARY KEY,
    template_code VARCHAR(100) NOT NULL UNIQUE,
    template_name VARCHAR(200) NOT NULL,
    data_scope_type VARCHAR(50) NOT NULL,
    company_id BIGINT,
    workarea_ids TEXT,
    template_description TEXT,
    usage_count INT DEFAULT 0,
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 3.11 用户数据权限表

```sql
CREATE TABLE user_data_permissions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    template_id BIGINT REFERENCES data_permission_templates(id),
    data_scope_type VARCHAR(50),
    company_id BIGINT,
    workarea_ids TEXT,
    is_fixed BOOLEAN DEFAULT FALSE,
    source VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id)
);
```

### 3.12 保护对象表

```sql
CREATE TABLE protected_objects (
    id BIGSERIAL PRIMARY KEY,
    object_type VARCHAR(50) NOT NULL,
    object_id BIGINT NOT NULL,
    object_name VARCHAR(200),
    protection_rules TEXT,
    protected_by VARCHAR(50) DEFAULT 'system',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(object_type, object_id)
);
```

### 3.13 权限配置权表

```sql
CREATE TABLE permission_config_rights (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    can_config_base BOOLEAN DEFAULT FALSE,
    can_config_default BOOLEAN DEFAULT FALSE,
    can_reset_default BOOLEAN DEFAULT FALSE,
    can_adjust_user BOOLEAN DEFAULT FALSE,
    granted_by BIGINT REFERENCES users(id),
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id)
);
```

### 3.14 权限审计日志表

```sql
CREATE TABLE permission_audit_log (
    id BIGSERIAL PRIMARY KEY,
    operator_id BIGINT NOT NULL,
    operator_name VARCHAR(100),
    operation_type VARCHAR(50) NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id BIGINT NOT NULL,
    target_name VARCHAR(200),
    permission_id BIGINT,
    permission_code VARCHAR(100),
    permission_name VARCHAR(200),
    old_value TEXT,
    new_value TEXT,
    change_reason TEXT,
    ip_address VARCHAR(50),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 3.15 权限快照表

```sql
CREATE TABLE permission_snapshots (
    id BIGSERIAL PRIMARY KEY,
    snapshot_name VARCHAR(200) NOT NULL,
    snapshot_type VARCHAR(50) NOT NULL,
    target_type VARCHAR(50),
    target_id BIGINT,
    target_name VARCHAR(200),
    permissions TEXT,
    snapshot_description TEXT,
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 4. 数据权限规则

```
甲方作业区用户角色：
├── 数据范围：关联的作业区
└── 约束：不可调整 (is_fixed = TRUE)

甲方非作业区用户角色：
├── 数据范围：本公司管辖范围内全部作业区
└── 约束：不可调整 (is_fixed = TRUE)

非甲方作业区用户角色：
├── 数据范围：系统管理员配置 或 甲方任务关联
└── 约束：可调整 (is_fixed = FALSE)

系统管理员：
├── 数据范围：功能权限下的全部数据
└── 约束：无限制
```

---

## 5. 权限配置权规则

```
admin（缺省系统管理员）：
├── ✅ 配置基本权限集合
├── ✅ 配置角色缺省权限集合
├── ✅ 授权其他系统管理员配置权
└── ✅ 调整用户权限（无限制）

被授权的系统管理员：
├── ✅ 配置角色缺省权限集合（如果被授权）
├── ✅ 恢复角色缺省权限集合（如果被授权）
├── ✅ 调整用户权限（不超过自身权限）
└── ❌ 配置基本权限集合

非系统管理员：
└── ❌ 无任何配置权
```

---

## 6. 表结构总览

```
【核心表】（扩展）
├── permissions          权限表（+ permission_type, is_base 等）
├── roles                角色表（+ role_level, managed_by_role_id）
├── users                用户表
├── user_roles           用户角色关联表
└── permission_groups    权限分组表（新增）

【API 关联】（新增）
├── apis                 API 接口表
└── permission_apis      权限-API 关联表

【页面关联】（新增）
├── pages                页面表
├── page_elements        页面元素表
└── element_permissions  元素-权限关联表

【权限分配】
├── role_default_permissions  角色缺省权限集合表
├── role_permissions     角色权限表
└── user_permissions     用户权限表

【数据权限】（新增）
├── data_permission_templates  数据权限模板表（共享）
└── user_data_permissions      用户数据权限表

【审计与保护】（新增）
├── permission_audit_log 权限审计日志表
├── protected_objects    保护对象表
├── permission_config_rights 权限配置权表
└── permission_snapshots  权限快照表

总计：19 张表
```

---

## 7. 实施阶段

### 阶段 1：核心功能（P0 - 立即实施）

| 功能 | 说明 | 表 |
|------|------|-----|
| 权限分组 | 按模块分组管理 | permission_groups |
| 权限-API 关联 | 明确功能与 API 关系 | apis, permission_apis |
| 基本权限集合 | 所有角色必备权限 | permissions.is_base |
| 角色层级 | 管理关系约束 | roles.role_level, managed_by_role_id |
| 审计日志 | 所有变更可追溯 | permission_audit_log |
| 保护对象 | 核心数据保护 | protected_objects |

### 阶段 2：增强功能（P1 - 近期实施）

| 功能 | 说明 | 表 |
|------|------|-----|
| 页面元素权限 | 细粒度控制 | pages, page_elements |
| 权限配置权 | 分权管理 | permission_config_rights |
| 缺省权限集合 | 角色默认权限 | role_default_permissions |
| 权限快照 | 回滚和对比 | permission_snapshots |

### 阶段 3：数据权限（P2 - 后续实施）

| 功能 | 说明 | 表 |
|------|------|-----|
| 数据权限模板 | 共享复用 | data_permission_templates |
| 用户数据权限 | 数据范围控制 | user_data_permissions |
| 数据权限自动注入 | SQL 自动拼接 | 应用层实现 |

---

## 8. 迁移方案

### 步骤 1：备份数据

```sql
CREATE TABLE permissions_backup_20260330 AS SELECT * FROM permissions;
CREATE TABLE role_permissions_backup_20260330 AS SELECT * FROM role_permissions;
CREATE TABLE roles_backup_20260330 AS SELECT * FROM roles;
```

### 步骤 2：确保 admin 全部权限

```sql
UPDATE users SET status = 'ACTIVE' WHERE username = 'admin';
UPDATE roles SET role_level = 0 WHERE role_code = 'ROLE_SYSTEM_ADMIN';
```

### 步骤 3：创建新表结构

执行本文档中的表创建脚本。

### 步骤 4：admin 登录后重建

1. 配置基本权限集合
2. 设置角色层级关系
3. 为各角色配置缺省权限
4. 录入 API 和页面数据

---

## 9. 开发流程规范

```
1️⃣ 功能设计（权限设计）
   ├── 确定权限编码：{模块}:{实体}:{类型}
   ├── 确定权限类型：VIEW / ACTION
   ├── 确定所属分组
   └── INSERT INTO permissions (...)

2️⃣ API 设计
   ├── 确定 HTTP 方法、路径
   ├── 关联到权限
   └── INSERT INTO apis (...) + permission_apis (...)

3️⃣ 菜单设计
   └── INSERT INTO menus (...)

4️⃣ 页面设计
   └── INSERT INTO pages (...)

5️⃣ 元素设计
   ├── 确定按钮、对话框等元素
   ├── 关联到权限
   └── INSERT INTO page_elements (...) + element_permissions

6️⃣ 权限分配
   └── INSERT INTO role_default_permissions (...)

7️⃣ 代码实现
```

---

## 10. 性能优化建议

### 权限缓存

```java
// Redis 缓存
// Key: user:permissions:{userId}
// Value: Set<String> permissionCodes
// TTL: 30分钟
```

### 权限预编译

```java
// 应用启动时加载所有权限-API 映射到内存
Map<String, Set<String>> apiPermissionMap;
```

### 数据权限注入

```java
// MyBatis 拦截器自动注入数据权限条件
// 自动拼接 WHERE workarea_id IN (...)
```

---

## 11. 前端权限控制

### 权限指令

```javascript
// Vue 权限指令
app.directive('permission', {
  mounted(el, binding) {
    const permission = binding.value
    const permissions = useUserStore().permissions
    if (!permissions.includes(permission)) {
      el.parentNode?.removeChild(el)
    }
  }
})

// 使用
<el-button v-permission="'user:create:action'">新增</el-button>
```

### 权限组件

```vue
<PermissionWrapper permission="user:delete:action">
  <el-button type="danger">删除</el-button>
</PermissionWrapper>
```