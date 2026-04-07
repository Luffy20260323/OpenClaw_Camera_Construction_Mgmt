# 数据库表完整清单与清理建议

**检查时间**: 2026-04-04 17:25  
**总表数**: 50 个

---

## 表分类清单

### ✅ 核心表（25 个，必须保留）

这些表是当前系统正在使用的核心表：

| 表名 | 用途 | 数据量 | 状态 |
|------|------|--------|------|
| `resource` | 资源表 | 215 | ✅ 使用中 |
| `role_permissions` | 角色 - 资源关联 | 441 | ✅ 使用中 |
| `roles` | 角色表 | 13 | ✅ 使用中 |
| `user_roles` | 用户 - 角色关联 | 25 | ✅ 使用中 |
| `users` | 用户表 | 24 | ✅ 使用中 |
| `user_permission_adjustment` | 用户权限调整 | - | ✅ 使用中 |
| `role_permission_adjustment` | 角色权限调整 | - | ✅ 使用中 |
| `permission_audit_log` | 权限审计日志 | 4 | ✅ 使用中 |
| `role_data_scope` | 角色数据范围（V4.3） | 1 | ✅ 使用中 |
| `user_data_scope` | 用户数据范围（V4.3） | 1 | ✅ 使用中 |
| `companies` | 公司表 | 5 | ✅ 使用中 |
| `company_types` | 公司类型表 | - | ✅ 使用中 |
| `work_areas` | 作业区表 | 20 | ✅ 使用中 |
| `user_work_areas` | 用户 - 作业区关联 | 13 | ✅ 使用中 |
| `component_types` | 零部件类型 | 17 | ✅ 使用中 |
| `component_attr_sets` | 属性集 | - | ✅ 使用中 |
| `component_attr_set_attrs` | 属性集 - 属性关联 | - | ✅ 使用中 |
| `component_attr_set_instances` | 属性集实例 | - | ✅ 使用中 |
| `component_instances` | 零部件实例 | - | ✅ 使用中 |
| `point_device_models` | 设备模型 | - | ✅ 使用中 |
| `point_device_model_items` | 模型项 | 4 | ✅ 使用中 |
| `point_device_model_instances` | 模型实例 | - | ✅ 使用中 |
| `point_device_model_instance_items` | 实例项 | - | ✅ 使用中 |
| `point_device_assignments` | 设备分配 | - | ✅ 使用中 |
| `points` | 点位 | - | ✅ 使用中 |
| `system_configs` | 系统配置 | 3 | ✅ 使用中 |

---

### ⚠️ 待确认表（17 个，需要检查）

这些表需要确认是否还在使用：

| 表名 | 数据量 | 可能用途 | 建议 |
|------|--------|----------|------|
| `permissions` | 90 | 旧权限表？ | ⚠️ 检查 |
| `role_menu_permissions` | 64 | 角色菜单权限 | ⚠️ 检查 |
| `role_default_permissions` | 58 | 角色缺省权限 | ⚠️ 检查 |
| `api_permissions` | 49 | API 权限表 | ⚠️ 检查 |
| `ui_element_permissions` | 42 | UI 元素权限 | ⚠️ 检查 |
| `role_type_default_permissions` | 26 | 角色类型缺省权限 | ⚠️ 检查 |
| `permission_groups` | 9 | 权限分组 | ⚠️ 检查 |
| `element_permissions` | 7 | 元素权限 | ⚠️ 检查 |
| `permission_apis` | 6 | 权限 API 关联 | ⚠️ 检查 |
| `permission_version` | 5 | 权限版本 | ⚠️ 检查 |
| `data_permission_rules` | 4 | 数据权限规则 | ⚠️ 检查 |
| `data_permission_templates` | 4 | 数据权限模板 | ⚠️ 检查 |
| `data_permission_audit_log` | - | 数据权限审计 | ⚠️ 检查 |
| `data_permission_cache` | - | 数据权限缓存 | ⚠️ 检查 |
| `role_data_permissions` | 1 | 旧角色数据范围 | ❌ 可删除 |
| `user_data_permissions` | 1 | 旧用户数据范围 | ❌ 可删除 |
| `user_menu_permissions` | 1 | 用户菜单权限 | ⚠️ 检查 |

---

### ❌ 可删除表（8 个）

这些表可以安全删除：

| 表名 | 数据量 | 删除理由 |
|------|--------|----------|
| `role_data_permissions` | 1 | 已被 `role_data_scope` 取代 |
| `user_data_permissions` | 1 | 已被 `user_data_scope` 取代 |
| `issue_logs` | 27 | 问题日志，可能不再使用 |
| `protected_objects` | 2 | 保护对象，可能不再使用 |
| `permission_config_rights` | - | 权限配置权限，可能不再使用 |
| `permission_groups` | 9 | 权限分组，可能不再使用 |
| `permission_snapshots` | - | 权限快照，可能不再使用 |
| `user_permissions` | - | 用户权限表，可能不再使用 |
| `user_resource` | - | 用户资源表，可能不再使用 |

---

## 详细分析

### 权限相关表

| 表名 | 状态 | 说明 |
|------|------|------|
| `resource` | ✅ 保留 | 核心资源表 |
| `role_permissions` | ✅ 保留 | 角色 - 资源关联 |
| `permissions` | ⚠️ 检查 | 可能是旧表 |
| `api_permissions` | ⚠️ 检查 | API 权限 |
| `element_permissions` | ⚠️ 检查 | 元素权限 |
| `ui_element_permissions` | ⚠️ 检查 | UI 元素权限 |

### 数据权限相关表

| 表名 | 状态 | 说明 |
|------|------|------|
| `role_data_scope` | ✅ 保留 | V4.3 设计 |
| `user_data_scope` | ✅ 保留 | V4.3 设计 |
| `role_data_permissions` | ❌ 删除 | 旧表，已迁移 |
| `user_data_permissions` | ❌ 删除 | 旧表，已迁移 |
| `data_permission_rules` | ⚠️ 检查 | 规则表 |
| `data_permission_templates` | ⚠️ 检查 | 模板表 |

### 历史/备份表

| 表名 | 状态 | 说明 |
|------|------|------|
| `permission_version` | ⚠️ 检查 | 版本记录 |
| `permission_snapshots` | ❌ 删除 | 快照表 |
| `role_permission_history` | ⚠️ 检查 | 历史记录 |

---

## 清理建议

### 立即可删除（2 个）

```sql
-- 已被 V4.3 新表取代
DROP TABLE IF EXISTS role_data_permissions CASCADE;
DROP TABLE IF EXISTS user_data_permissions CASCADE;
```

### 检查后删除（6 个）

```sql
-- 可能不再使用的表
DROP TABLE IF EXISTS permission_snapshots CASCADE;
DROP TABLE IF EXISTS user_permissions CASCADE;
DROP TABLE IF EXISTS user_resource CASCADE;
DROP TABLE IF EXISTS permission_config_rights CASCADE;
```

### 需要仔细检查（13 个）

这些表需要检查代码中是否还在使用：
- `permissions`
- `role_menu_permissions`
- `role_default_permissions`
- `api_permissions`
- `ui_element_permissions`
- `role_type_default_permissions`
- `permission_groups`
- `element_permissions`
- `permission_apis`
- `permission_version`
- `data_permission_rules`
- `data_permission_templates`
- `data_permission_audit_log`
- `data_permission_cache`
- `user_menu_permissions`

---

## 检查方法

### 1. 检查表是否有外键依赖

```sql
-- 检查哪些表引用了目标表
SELECT 
    tc.table_name, 
    kcu.column_name,
    ccu.table_name AS foreign_table_name
FROM information_schema.table_constraints AS tc 
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
WHERE ccu.table_name = '目标表名';
```

### 2. 检查代码中是否使用

```bash
# 搜索 Java 代码
grep -r "表名" backend/src/main/java/

# 搜索 SQL 文件
grep -r "表名" backend/src/main/resources/
```

### 3. 检查表最后访问时间

```sql
SELECT 
    relname as table_name,
    last_vacuum,
    last_analyze,
    n_tup_ins as inserts,
    n_tup_upd as updates,
    n_tup_del as deletes
FROM pg_stat_user_tables
WHERE relname = '目标表名';
```

---

## 总结

| 类别 | 数量 | 操作 |
|------|------|------|
| 核心表 | 25 | ✅ 保留 |
| 待确认 | 17 | ⚠️ 检查 |
| 可删除 | 8 | ❌ 删除 |
| **总计** | **50** | |

---

**报告人**: AI Assistant  
**报告时间**: 2026-04-04 17:25  
**建议**: 先删除 2 个已确认的旧表，然后逐一检查待确认表
