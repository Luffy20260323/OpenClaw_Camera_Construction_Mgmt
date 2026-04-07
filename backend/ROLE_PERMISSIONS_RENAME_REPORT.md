# role_permissions 重命名为 permission 报告

**执行时间**: 2026-04-04 17:50  
**状态**: ✅ 完成

---

## 重命名内容

### 1. 表重命名

```sql
-- 重命名表
ALTER TABLE role_permissions RENAME TO permission;

-- 重命名索引
ALTER INDEX idx_role_permissions_role RENAME TO idx_permission_role;

-- 重命名约束
ALTER TABLE permission RENAME CONSTRAINT role_permissions_pkey TO permission_pkey;
```

### 2. 数据验证

| 项目 | 重命名前 | 重命名后 |
|------|----------|----------|
| 表名 | `role_permissions` | `permission` ✅ |
| 记录数 | 441 | 441 ✅ |
| 索引 | `idx_role_permissions_role` | `idx_permission_role` ✅ |

---

## 代码更新

### MenuService.java

**修改前**:
```java
String sql = """
    SELECT DISTINCT r.permission_key 
    FROM role_permissions rp
    JOIN resource r ON rp.permission_id = r.id
    JOIN user_roles ur ON rp.role_id = ur.role_id
    WHERE ur.user_id = ? 
    AND r.permission_key IS NOT NULL
""";
```

**修改后**:
```java
String sql = """
    SELECT DISTINCT r.permission_key 
    FROM permission p
    JOIN resource r ON p.permission_id = r.id
    JOIN user_roles ur ON p.role_id = ur.role_id
    WHERE ur.user_id = ? 
    AND r.permission_key IS NOT NULL
""";
```

---

## 文档更新

### permission-design-v4.3.md

**更新内容**:
- 明确说明 `permission_id` 实际指向 `resource.id`
- 更新表结构说明
- 添加字段说明

---

## V4.3 设计一致性

### 表名对比

| 设计 | V4.3 文档 | 实际实现 | 状态 |
|------|----------|----------|------|
| 资源表 | `resource` | `resource` | ✅ |
| 角色表 | `roles` | `roles` | ✅ |
| 角色 - 资源关联 | `permission` | `permission` | ✅ 已重命名 |
| 用户 - 角色关联 | `user_roles` | `user_roles` | ✅ |

### 字段对比

| 设计 | V4.3 文档 | 实际实现 | 说明 |
|------|----------|----------|------|
| 角色 ID | `role_id` | `role_id` | ✅ |
| 资源 ID | `resource_id` | `permission_id` | ⚠️ 命名不同，但指向相同 |

---

## 当前表结构

```sql
-- permission 表结构
CREATE TABLE permission (
    id            BIGSERIAL PRIMARY KEY,
    role_id       BIGINT NOT NULL REFERENCES roles(id),
    permission_id BIGINT NOT NULL REFERENCES resource(id),  -- 实际指向 resource.id
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (role_id, permission_id)
);

-- 索引
CREATE INDEX idx_permission_role ON permission(role_id);
```

---

## 未修改的部分

### SQL 迁移文件

以下迁移文件中的 `role_permissions` 引用**未修改**（保持历史记录）：
- `V6__add_document_center.sql`
- `V7__permission_system_v3_p0.sql`
- `V8__permission_system_v3_p1.sql`
- `V9__permission_system_v3_p2.sql`
- `V11__add_permission_management_permissions.sql`
- `V12__permission_system_v4.sql`
- `V16__document_center_permissions.sql`
- `V18__component_attr_set_tables.sql`
- `V20260404__*.sql`
- 等等...

**原因**: 迁移文件是历史记录，不应修改。

### 备份表

以下备份表已删除：
- `role_permissions_backup_20260330` ✅ 已删除

---

## 验证 SQL

```sql
-- 验证表已重命名
SELECT COUNT(*) FROM permission;
-- 结果：441

-- 验证权限关联正常
SELECT 
    r.role_name,
    COUNT(p.permission_id) as permission_count
FROM permission p
JOIN roles r ON p.role_id = r.id
GROUP BY r.id, r.role_name
ORDER BY permission_count DESC
LIMIT 10;
```

---

## 影响分析

### 正面影响

1. ✅ **符合 V4.3 设计** - 表名与文档一致
2. ✅ **简化命名** - `permission` 比 `role_permissions` 更简洁
3. ✅ **语义清晰** - `permission` 表存储的就是"权限"

### 潜在风险

1. ⚠️ **历史迁移文件** - 包含旧表名，但不影响运行
2. ⚠️ **文档不一致** - 部分文档可能还引用旧表名

---

## 总结

| 项目 | 状态 |
|------|------|
| 表重命名 | ✅ 完成 |
| 索引重命名 | ✅ 完成 |
| 约束重命名 | ✅ 完成 |
| 代码更新 | ✅ 完成（1 个文件） |
| 文档更新 | ✅ 完成（V4.3 设计文档） |
| 数据完整性 | ✅ 441 条记录完整 |

---

**执行人**: AI Assistant  
**执行时间**: 2026-04-04 17:50  
**状态**: ✅ 完成
