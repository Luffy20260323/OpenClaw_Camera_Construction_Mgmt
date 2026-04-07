# P1 和 P2 问题清单

**检查时间**: 2026-04-04 18:05  
**状态**: 待修复

---

## P1 问题（高优先级）

### 1. role_menu_permissions 表（已删除）❌

**影响文件**: 3 个

#### MenuController.java (2 处)

```java
// 第 79 行 - 获取用户菜单权限
@Select("SELECT m.* FROM menus m " +
        "LEFT JOIN user_menu_permissions ump ON m.id = ump.menu_id AND ump.user_id = #{userId} " +
        "LEFT JOIN role_menu_permissions rmp ON m.id = rmp.menu_id " +
        "WHERE ump.can_view = true OR rmp.can_view = true")
List<Menu> selectUserMenus(Long userId);
```

**问题**: 
- 引用已删除的 `menus` 表
- 引用已删除的 `role_menu_permissions` 表
- 引用已删除的 `user_menu_permissions` 表

**修复方案**: 替换为基于 `resource` 表的查询

---

#### MenuMapper.java (2 处)

```java
// 第 17-18 行
@Select("SELECT m.* FROM menus m " +
        "LEFT JOIN user_menu_permissions ump ON m.id = ump.menu_id AND ump.user_id = #{userId} " +
        "LEFT JOIN role_menu_permissions rmp ON m.id = rmp.menu_id " +
        "WHERE ump.can_view = true OR rmp.can_view = true")
```

**修复方案**: 同上

---

#### MenuPermissionFilter.java (2 处)

```java
// 第 190 行 - 检查用户菜单权限
SELECT can_view FROM user_menu_permissions ump
WHERE ump.user_id = ? AND ump.menu_id = ?

// 第 201 行 - 检查角色菜单权限
SELECT rmp.can_view FROM role_menu_permissions rmp
WHERE rmp.role_id = ? AND rmp.menu_id = ?
```

**修复方案**: 使用 `permission` 表代替

---

#### 实体类

```java
// RoleMenuPermission.java
@TableName("role_menu_permissions")
public class RoleMenuPermission { ... }

// UserMenuPermission.java
@TableName("user_menu_permissions")
public class UserMenuPermission { ... }
```

**修复方案**: 删除或标记为废弃

---

### 2. user_menu_permissions 表（已删除）❌

**影响文件**: 2 个

#### MenuController.java (2 处)

```java
// 第 63 行 - 获取用户菜单权限
@Select("SELECT ump.can_view, ump.can_operate " +
        "FROM user_menu_permissions ump " +
        "WHERE ump.user_id = ? AND ump.menu_id = ?")
Map<String, Object> getUserMenuPermission(Long userId, Long menuId);

// 第 140 行 - 设置用户菜单权限
INSERT INTO user_menu_permissions (user_id, menu_id, can_view, can_operate, granted_by, granted_at)
VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)

// 第 160 行 - 删除用户菜单权限
DELETE FROM user_menu_permissions WHERE user_id = ? AND menu_id = ?
```

**修复方案**: 
- 方案 A: 使用 `user_data_scope` 表（按模块配置）
- 方案 B: 使用 `permission` 表（通用权限）
- 方案 C: 删除此功能，改用角色权限

---

### 3. role_default_permissions 表（已删除）❌

**影响文件**: 2 个

#### PermissionController.java (已注释)

```java
// 第 206 行 - 已注释
// jdbcTemplate.update("DELETE FROM role_default_permissions WHERE role_id = ?", roleId);
```

**状态**: ✅ 已临时修复（使用 `permission` 表代替）

---

#### UserPermissionController.java (注释引用)

```java
// 第 119 行 - 注释
* - DEFAULT: 缺省权限（来自 role_default_permissions）
```

**修复方案**: 更新注释说明当前从 `permission` 表获取

---

#### RoleDefaultPermission.java 实体类

```java
// 第 14 行
@TableName("role_default_permissions")
public class RoleDefaultPermission { ... }
```

**修复方案**: 删除或标记为废弃

---

## P2 问题（中优先级）

### 4. permissions 表（已删除）❌

**影响文件**: 2 个

#### PermissionServiceImpl.java (1 处)

```java
// 第 140 行
String sql = "SELECT id, permission_code, permission_name FROM permissions ORDER BY id";
```

**修复方案**: 
- 方案 A: 删除此方法（可能不再需要）
- 方案 B: 从 `resource` 表查询

---

#### PermissionController.java (3 处)

```java
// 第 45 行 - 获取所有权限列表
String sql = "SELECT id, permission_code, permission_name, description, group_code, " +
             "is_base, permission_type, module_code FROM permissions ORDER BY id";

// 第 88 行 - 获取权限详情
String permSql = "SELECT permission_code, permission_name FROM permissions WHERE id = ?";

// 第 328 行 - 获取权限分组
"FROM permissions ORDER BY resource_type, action";
```

**修复方案**: 
- 这些方法可能不再需要（`resource` 表已包含所有信息）
- 或者从 `resource` 表查询

---

## 修复建议

### P1 修复方案

#### 方案 A: 完全移除菜单权限功能

**理由**:
- 菜单权限已通过 `permission` 表实现
- 不需要单独的菜单权限表
- 简化设计

**行动**:
1. 删除 `MenuController` 中的菜单权限方法
2. 删除 `RoleMenuPermission` 和 `UserMenuPermission` 实体类
3. 更新 `MenuPermissionFilter` 使用 `permission` 表

---

#### 方案 B: 迁移到 resource 表

**理由**:
- 保留菜单权限功能
- 使用统一的资源管理

**行动**:
1. 将菜单权限数据迁移到 `permission` 表
2. 更新所有查询使用 `resource` 和 `permission` 表
3. 删除旧实体类

---

### P2 修复方案

#### permissions 表相关

**建议**: 删除相关方法

**理由**:
- `resource` 表已包含所有权限信息
- `permission` 表存储角色 - 资源关联
- 不需要单独的 `permissions` 表

**行动**:
1. 删除 `getAllPermissions()` 方法
2. 删除 `getPermission()` 方法
3. 删除 `getPermissionGroups()` 方法
4. 删除 `Permission` 实体类（如果不再使用）

---

## 影响评估

### P1 不修复的影响

| 功能 | 影响程度 | 说明 |
|------|----------|------|
| 菜单权限查询 | 🔴 严重 | 运行时 SQL 异常 |
| 用户菜单权限设置 | 🔴 严重 | 运行时 SQL 异常 |
| 菜单权限过滤 | 🔴 严重 | 运行时 SQL 异常 |

### P2 不修复的影响

| 功能 | 影响程度 | 说明 |
|------|----------|------|
| 获取所有权限列表 | 🟡 中等 | 可能不再使用 |
| 权限详情查询 | 🟡 中等 | 可能不再使用 |
| 权限分组查询 | 🟡 中等 | 可能不再使用 |

---

## 修复优先级

### P1 - 高优先级（立即修复）

1. ✅ `role_menu_permissions` → 使用 `permission` 表
2. ✅ `user_menu_permissions` → 使用 `permission` 表
3. ⚠️ `role_default_permissions` → 已临时修复

### P2 - 中优先级（可延后）

4. ⚠️ `permissions` 表相关方法 → 删除或从 `resource` 表查询

---

## 代码位置汇总

### P1 文件清单

| 文件 | 问题表 | 引用次数 |
|------|--------|----------|
| `MenuController.java` | role_menu_permissions, user_menu_permissions | 4 |
| `MenuMapper.java` | role_menu_permissions, user_menu_permissions | 2 |
| `MenuPermissionFilter.java` | role_menu_permissions, user_menu_permissions | 2 |
| `RoleMenuPermission.java` | role_menu_permissions | 1 (实体类) |
| `UserMenuPermission.java` | user_menu_permissions | 1 (实体类) |
| `PermissionController.java` | role_default_permissions | 1 (已注释) |
| `UserPermissionController.java` | role_default_permissions | 1 (注释) |
| `RoleDefaultPermission.java` | role_default_permissions | 1 (实体类) |

### P2 文件清单

| 文件 | 问题表 | 引用次数 |
|------|--------|----------|
| `PermissionController.java` | permissions | 3 |
| `PermissionServiceImpl.java` | permissions | 1 |

---

## 总结

| 优先级 | 问题表 | 影响文件数 | 引用次数 | 建议方案 |
|--------|--------|-----------|----------|----------|
| P1 | role_menu_permissions | 3 | 4 | 使用 permission 表 |
| P1 | user_menu_permissions | 2 | 4 | 使用 permission 表 |
| P1 | role_default_permissions | 2 | 2 | 已临时修复 |
| P2 | permissions | 2 | 4 | 删除或从 resource 查询 |

---

**报告人**: AI Assistant  
**报告时间**: 2026-04-04 18:05  
**状态**: 待修复
