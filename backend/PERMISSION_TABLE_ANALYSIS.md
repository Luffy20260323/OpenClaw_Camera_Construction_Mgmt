# 权限表设计与 V4.3 一致性说明

**分析时间**: 2026-04-04 16:35

---

## V4.3 设计 vs 当前实现

### V4.3 设计文档

```sql
-- permission 表（角色 - 资源关联）
CREATE TABLE permission (
    id                  BIGSERIAL PRIMARY KEY,
    role_id             BIGINT NOT NULL,  -- 角色 ID
    resource_id         BIGINT NOT NULL,  -- 资源 ID
    is_basic            BOOLEAN DEFAULT false,
    is_default          BOOLEAN DEFAULT false,
    is_effective        BOOLEAN DEFAULT true,
    ...
    UNIQUE (role_id, resource_id)
);
```

### 当前实现

```sql
-- role_permissions 表（角色 - 资源关联）
CREATE TABLE role_permissions (
    role_id       INTEGER NOT NULL,  -- 角色 ID
    permission_id INTEGER NOT NULL,  -- 实际指向 resource.id
    PRIMARY KEY (role_id, permission_id)
);
```

---

## 分析结果

### ✅ 功能一致

**`role_permissions.permission_id` 实际指向 `resource.id`**：

```sql
SELECT rp.role_id, rp.permission_id, r.id as resource_id, r.name
FROM role_permissions rp
JOIN resource r ON rp.permission_id = r.id
WHERE rp.role_id = 1
LIMIT 5;

-- 结果：
-- role_id | permission_id | resource_id | name
--    1    |       1       |      1      | 系统设置
--    1    |       2       |      2      | 零部件管理
--    1    |       3       |      3      | 角色缺省权限
```

**结论**: `role_permissions.permission_id` = `resource.id`

### ⚠️ 命名不一致

| 设计要素 | V4.3 设计 | 当前实现 | 说明 |
|----------|----------|----------|------|
| 表名 | `permission` | `role_permissions` | 功能相同 |
| 字段名 | `resource_id` | `permission_id` | 实际指向相同 |
| 关联关系 | role_id → resource_id | role_id → permission_id (→ resource.id) | 功能相同 |

---

## 建议

### 方案 1: 保持现状（推荐）

**理由**:
1. ✅ 功能完全一致
2. ✅ 系统已在运行
3. ✅ 数据完整（441 条记录）
4. ⚠️ 重命名会破坏现有代码和数据

**行动**:
- 更新 V4.3 文档，说明当前实现
- 在代码注释中说明 `permission_id` 实际指向 `resource.id`

---

### 方案 2: 重命名以符合 V4.3（不推荐）

**步骤**:
1. 创建新 `permission` 表
2. 迁移数据
3. 更新所有代码
4. 删除旧表

**风险**:
- ❌ 破坏现有系统
- ❌ 需要大量代码修改
- ❌ 数据迁移风险

---

## 当前实现的问题

### 问题 1: 有孤立记录

```sql
-- 6 条 permission_id 没有对应的 resource
SELECT COUNT(*) FROM role_permissions rp
LEFT JOIN resource r ON rp.permission_id = r.id
WHERE r.id IS NULL;

-- 结果：6
```

**建议**: 清理孤立记录

### 问题 2: 缺少 V4.3 的扩展字段

V4.3 设计有：
- `is_basic` - 基本权限
- `is_default` - 缺省权限
- `is_effective` - 是否生效

当前实现没有这些字段。

---

## 清理建议

### 删除废弃表

```sql
-- 1. role_resource（已被 role_permissions 取代）
-- 建议：删除

-- 2. role_permissions
-- 建议：保留，但重命名为 permission（可选）

-- 3. 备份表
-- 建议：删除或归档
DROP TABLE IF EXISTS permissions_backup_20260330;
DROP TABLE IF EXISTS role_permissions_backup_20260330;
DROP TABLE IF EXISTS roles_backup_20260330;
```

### 清理孤立记录

```sql
-- 清理 role_permissions 中的孤立记录
DELETE FROM role_permissions rp
WHERE NOT EXISTS (SELECT 1 FROM resource r WHERE r.id = rp.permission_id);
```

---

## 代码修改建议

### MenuService.java（已修复）

```java
// ✅ 当前实现（功能正确）
String sql = """
    SELECT DISTINCT r.permission_key 
    FROM role_permissions rp
    JOIN resource r ON rp.permission_id = r.id  -- permission_id 实际是 resource.id
    JOIN user_roles ur ON rp.role_id = ur.role_id
    WHERE ur.user_id = ? 
    AND r.permission_key IS NOT NULL
""";
```

### 建议添加注释

```java
/**
 * 获取用户的所有权限标识
 * 
 * 注意：role_permissions.permission_id 实际指向 resource.id
 * 这与 V4.3 设计的 permission(resource_id) 功能一致，只是字段名不同
 */
private Set<String> getUserPermissionKeys(Long userId) { ... }
```

---

## 总结

### 当前状态

| 方面 | 状态 | 说明 |
|------|------|------|
| 功能实现 | ✅ 正确 | 角色 - 资源关联正确 |
| 表名 | ⚠️ 不一致 | role_permissions vs permission |
| 字段名 | ⚠️ 不一致 | permission_id vs resource_id |
| 数据完整性 | ⚠️ 有孤立记录 | 6 条孤立记录 |
| 扩展字段 | ❌ 缺失 | 缺少 is_basic, is_default, is_effective |

### 建议行动

1. **保持现状** - 功能正确，无需大改
2. **清理孤立记录** - 提高数据质量
3. **添加代码注释** - 说明设计差异
4. **更新 V4.3 文档** - 记录实际实现

---

**报告人**: AI Assistant  
**报告时间**: 2026-04-04 16:35  
**建议**: 保持现状，功能正确
