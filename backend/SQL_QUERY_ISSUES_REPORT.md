# SQL 查询引用不存在表的问题报告

**检查时间**: 2026-04-04 17:55  
**状态**: 🔴 严重问题

---

## 问题汇总

发现 **8 个已删除表** 仍在代码中被引用，涉及 **多个文件** 和 **数十处 SQL 查询**。

| 已删除表 | 引用文件数 | 引用次数 | 严重程度 |
|----------|-----------|----------|----------|
| `role_resource` | 3 | 10+ | 🔴 严重 |
| `permissions` | 2 | 4+ | 🔴 严重 |
| `role_data_permissions` | 1 | 5+ | 🔴 严重 |
| `user_data_permissions` | 1 | 4+ | 🔴 严重 |
| `role_menu_permissions` | 1 | 1+ | 🔴 严重 |
| `user_menu_permissions` | 2 | 3+ | 🔴 严重 |
| `role_default_permissions` | 2 | 3+ | 🔴 严重 |
| `menus` | 2 | 2+ | 🔴 严重 |

---

## 详细问题清单

### 1. role_resource 表（已删除）❌

**影响文件**: 3 个

#### PermissionController.java (4 处)
```java
// 第 142 行
String permSql = "SELECT permission_id FROM role_resource WHERE role_id = ?";

// 第 267 行
String oldPermSql = "SELECT ARRAY_AGG(permission_id) FROM role_resource WHERE role_id = ?";

// 第 271 行
jdbcTemplate.update("DELETE FROM role_resource WHERE role_id = ?", roleId);

// 第 344 行
String permSql = "SELECT permission_id FROM role_resource WHERE role_id = ?";
```

#### UserPermissionController.java (1 处)
```java
// 第 155 行
FROM role_resource rp
```

#### RolePermissionMapper.java (5 处)
```java
// 第 20 行
@Select("SELECT * FROM role_resource WHERE role_id = #{roleId}")

// 第 26 行
@Select("SELECT * FROM role_resource WHERE role_id = #{roleId} AND permission_type = 'basic'")

// 第 32 行
@Select("SELECT * FROM role_resource WHERE role_id = #{roleId} AND permission_type = 'default'")

// 第 38 行
@Select("SELECT role_id FROM role_resource WHERE resource_id = #{resourceId}")

// 第 44 行
@Select("SELECT COUNT(*) > 0 FROM role_resource WHERE role_id = #{roleId} AND resource_id = #{resourceId}")
```

**修复方案**: 替换为 `permission` 表

---

### 2. permissions 表（已删除）❌

**影响文件**: 2 个

#### PermissionServiceImpl.java (1 处)
```java
// 第 140 行
String sql = "SELECT id, permission_code, permission_name FROM permissions ORDER BY id";
```

#### PermissionController.java (3 处)
```java
// 第 45 行
String sql = "SELECT id, permission_code, permission_name, description, group_code, is_base, permission_type, module_code FROM permissions ORDER BY id";

// 第 88 行
String permSql = "SELECT permission_code, permission_name FROM permissions WHERE id = ?";

// 第 328 行
"FROM permissions ORDER BY resource_type, action";
```

**修复方案**: 这些方法可能不再需要，需要检查是否还在使用

---

### 3. role_data_permissions 表（已删除）❌

**影响文件**: 1 个

#### DataPermissionServiceImpl.java (5 处)
```java
// 第 94 行
String sql = "SELECT data_scope_type FROM role_data_permissions WHERE role_id = ?";

// 第 105 行
String sql = "SELECT dept_id FROM role_data_permissions WHERE role_id = ?";

// 第 116 行
String sql = "SELECT dept_ids FROM role_data_permissions WHERE role_id = ?";

// 第 164 行
String checkSql = "SELECT id FROM role_data_permissions WHERE role_id = ?";

// 第 242 行
FROM role_data_permissions rdp
```

**修复方案**: 替换为 `role_data_scope` 表

---

### 4. user_data_permissions 表（已删除）❌

**影响文件**: 1 个

#### DataPermissionServiceImpl.java (4 处)
```java
// 第 44 行
String sql = "SELECT data_scope_type FROM user_data_permissions WHERE user_id = ?";

// 第 61 行
String sql = "SELECT dept_id FROM user_data_permissions WHERE user_id = ?";

// 第 72 行
String sql = "SELECT dept_ids FROM user_data_permissions WHERE user_id = ?";

// 第 130 行
String checkSql = "SELECT id FROM user_data_permissions WHERE user_id = ?";
```

**修复方案**: 替换为 `user_data_scope` 表

---

### 5. role_menu_permissions 表（已删除）❌

**影响文件**: 1 个

#### MenuPermissionFilter.java (1 处)
```java
// 第 201 行
SELECT rmp.can_view FROM role_menu_permissions rmp
```

**修复方案**: 需要检查是否有替代表

---

### 6. user_menu_permissions 表（已删除）❌

**影响文件**: 2 个

#### MenuController.java (2 处)
```java
// 第 63 行
FROM user_menu_permissions

// 第 160 行
String sql = "DELETE FROM user_menu_permissions WHERE user_id = ? AND menu_id = ?";
```

#### MenuPermissionFilter.java (1 处)
```java
// 第 190 行
SELECT can_view FROM user_menu_permissions ump
```

**修复方案**: 需要检查是否有替代表

---

### 7. role_default_permissions 表（已删除）❌

**影响文件**: 2 个

#### PermissionController.java (2 处)
```java
// 第 167 行
String permSql = "SELECT permission_id FROM role_default_permissions WHERE role_id = ?";

// 第 206 行
jdbcTemplate.update("DELETE FROM role_default_permissions WHERE role_id = ?", roleId);
```

#### UserPermissionController.java (1 处)
```java
// 第 191 行
FROM role_default_permissions rdp
```

**修复方案**: 需要检查是否有替代表

---

### 8. menus 表（已删除）❌

**影响文件**: 2 个

#### MenuController.java (1 处)
```java
// 第 55 行
FROM menus
```

#### MenuMapper.java (1 处)
```java
// 第 16 行
@Select("SELECT m.* FROM menus m " +
```

**修复方案**: 替换为 `resource` 表（type='MENU'）

---

## 修复优先级

### P0 - 立即修复（核心功能）

1. ✅ `role_resource` → `permission`
2. ✅ `role_data_permissions` → `role_data_scope`
3. ✅ `user_data_permissions` → `user_data_scope`
4. ✅ `menus` → `resource`

### P1 - 高优先级（重要功能）

5. ⚠️ `role_menu_permissions` - 需要确认替代方案
6. ⚠️ `user_menu_permissions` - 需要确认替代方案
7. ⚠️ `role_default_permissions` - 需要确认替代方案

### P2 - 中优先级（辅助功能）

8. ⚠️ `permissions` - 可能不再需要

---

## 修复计划

### 第一批：权限相关（role_resource → permission）

**文件**:
- `PermissionController.java`
- `UserPermissionController.java`
- `RolePermissionMapper.java`

### 第二批：数据范围相关

**文件**:
- `DataPermissionServiceImpl.java`

### 第三批：菜单相关

**文件**:
- `MenuController.java`
- `MenuMapper.java`
- `MenuPermissionFilter.java`

### 第四批：其他

**文件**:
- `PermissionController.java` (permissions 表相关)
- `PermissionServiceImpl.java`

---

## 影响评估

### 当前系统状态

- 🔴 **严重**: 多个核心功能使用已删除的表
- 🔴 **风险**: 运行时可能抛出 SQL 异常
- 🔴 **影响**: 权限验证、菜单加载、数据范围等功能

### 需要测试的功能

1. 用户登录
2. 菜单加载
3. 权限验证
4. 角色权限配置
5. 用户权限配置
6. 数据范围配置

---

## 总结

| 项目 | 数量 |
|------|------|
| 已删除表 | 8 个 |
| 影响文件 | 9 个 |
| SQL 查询 | 30+ 处 |
| 严重程度 | 🔴 严重 |

**建议**: 立即修复 P0 级别问题，然后逐步修复其他问题。

---

**报告人**: AI Assistant  
**报告时间**: 2026-04-04 17:55  
**状态**: 🔴 需要立即修复
