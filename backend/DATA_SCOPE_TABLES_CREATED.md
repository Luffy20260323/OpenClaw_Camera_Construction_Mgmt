# 数据范围权限表创建报告（V4.3）

**创建时间**: 2026-04-04 17:20  
**状态**: ✅ 完成

---

## 已创建的表（2 个）

### 1. role_data_scope 表

```sql
CREATE TABLE role_data_scope (
    id                  BIGSERIAL PRIMARY KEY,
    role_id             BIGINT NOT NULL REFERENCES roles(id),
    module_code         VARCHAR(50) NOT NULL,              -- 模块编码
    resource_type       VARCHAR(20),                       -- 资源类型
    scope_type          VARCHAR(20) NOT NULL,              -- ALL/COMPANY/DEPT/DEPT_AND_SUB/SELF/CUSTOM
    is_default          BOOLEAN DEFAULT true,              -- 是否缺省
    is_effective        BOOLEAN DEFAULT true,              -- 是否生效
    custom_rule         JSONB,                             -- 自定义规则
    adjust_reason       VARCHAR(500),
    adjusted_by         BIGINT,
    adjusted_at         TIMESTAMP,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (role_id, module_code, resource_type)
);
```

**索引**:
- `idx_role_data_scope_role` - role_id
- `idx_role_data_scope_module` - module_code
- `idx_role_data_scope_scope_type` - scope_type

**数据量**: 1 条记录（已迁移）

---

### 2. user_data_scope 表

```sql
CREATE TABLE user_data_scope (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL REFERENCES users(id),
    module_code         VARCHAR(50) NOT NULL,              -- 模块编码
    resource_type       VARCHAR(20),                       -- 资源类型
    scope_type          VARCHAR(20) NOT NULL,              -- ALL/COMPANY/DEPT/DEPT_AND_SUB/SELF/CUSTOM
    source_type         VARCHAR(20) DEFAULT 'inherited',   -- inherited/direct
    is_effective        BOOLEAN DEFAULT true,              -- 是否生效
    custom_rule         JSONB,                             -- 自定义规则
    adjust_reason       VARCHAR(500),
    adjusted_by         BIGINT,
    adjusted_at         TIMESTAMP,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, module_code, resource_type)
);
```

**索引**:
- `idx_user_data_scope_user` - user_id
- `idx_user_data_scope_module` - module_code
- `idx_user_data_scope_scope_type` - scope_type

**数据量**: 1 条记录（已迁移）

---

## V4.3 设计 vs 旧设计

### 表名对比

| 设计 | 角色表 | 用户表 |
|------|--------|--------|
| V4.3 设计 | `role_data_scope` | `user_data_scope` |
| 旧设计 | `role_data_permissions` | `user_data_permissions` |

### 字段对比

| 方面 | V4.3 设计 | 旧设计 |
|------|----------|--------|
| 模块级别 | ✅ 按模块配置 | ❌ 全局配置 |
| 资源类型 | ✅ 可指定 | ❌ 无 |
| 生效状态 | ✅ `is_effective` | ❌ 无 |
| 缺省标记 | ✅ `is_default` | ❌ 无 |
| 来源类型 | ✅ `source_type` | ✅ `source` |
| 继承机制 | ❌ 简化 | ✅ `is_inherited` |

---

## 数据迁移

### 已迁移数据

| 源表 | 目标表 | 迁移记录数 |
|------|--------|------------|
| `role_data_permissions` | `role_data_scope` | 1 条 |
| `user_data_permissions` | `user_data_scope` | 1 条 |

### 迁移逻辑

**角色数据范围**:
```sql
INSERT INTO role_data_scope (role_id, module_code, scope_type, is_default, is_effective, custom_rule)
SELECT 
    role_id,
    'global' as module_code,  -- 全局配置
    data_scope_type,
    true, true,
    custom_scope
FROM role_data_permissions;
```

**用户数据范围**:
```sql
INSERT INTO user_data_scope (user_id, module_code, scope_type, source_type, is_effective, custom_rule)
SELECT 
    user_id,
    'global' as module_code,
    data_scope_type,
    CASE WHEN source = 'MANUAL' THEN 'direct' ELSE 'inherited' END,
    true,
    custom_scope
FROM user_data_permissions;
```

---

## 数据范围类型

| 类型 | 代码 | 说明 | SQL 示例 |
|------|------|------|----------|
| 全部数据 | `ALL` | 可访问全部数据 | `1=1` |
| 本公司 | `COMPANY` | 仅限本公司数据 | `company_id = :userCompanyId` |
| 本部门 | `DEPT` | 仅限本部门数据 | `dept_id = :userDeptId` |
| 本部门及下级 | `DEPT_AND_SUB` | 本部门及下级部门 | `dept_id IN (:userDeptIds)` |
| 仅本人 | `SELF` | 仅限本人创建的数据 | `created_by = :userId` |
| 自定义 | `CUSTOM` | 按规则自定义 | `WHERE custom_rule` |

---

## 自定义规则格式

```json
{
  "type": "work_area_ids",
  "operator": "IN",
  "values": [1, 2, 3],
  "description": "仅允许访问指定的作业区"
}
```

---

## 后续工作

### 需要更新的代码

1. **数据范围查询服务**
   - `DataPermissionService.java`
   - 修改为从 `role_data_scope` 和 `user_data_scope` 查询

2. **数据范围配置 API**
   - `RoleDataScopeController.java`
   - `UserDataScopeController.java`

3. **前端页面**
   - 角色数据范围配置页面
   - 用户数据范围配置页面

### 可选清理

旧表可以保留或标记为废弃：
- `role_data_permissions`
- `user_data_permissions`
- `data_permission_rules`

---

## 验证 SQL

```sql
-- 查看角色数据范围配置
SELECT 
    r.role_name,
    rds.module_code,
    rds.scope_type,
    rds.is_default,
    rds.is_effective
FROM role_data_scope rds
JOIN roles r ON rds.role_id = r.id;

-- 查看用户数据范围配置
SELECT 
    u.username,
    u.real_name,
    uds.module_code,
    uds.scope_type,
    uds.source_type,
    uds.is_effective
FROM user_data_scope uds
JOIN users u ON uds.user_id = u.id;
```

---

**执行人**: AI Assistant  
**执行时间**: 2026-04-04 17:20  
**状态**: ✅ 完成
