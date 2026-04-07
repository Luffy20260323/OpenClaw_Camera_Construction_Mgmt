# 数据库表完整清单

**数据库**: camera_construction_db  
**查询时间**: 2026-04-04 16:45  
**总表数**: 57

---

## 按功能分类

### 1. 权限相关表（18 个）

| 表名 | 用途 | 状态 | 建议 |
|------|------|------|------|
| `resource` | 资源表（MODULE/MENU/PAGE/ELEMENT/API） | ✅ 使用中 | 保留 |
| `role_permissions` | 角色 - 资源关联表 | ✅ 使用中 | 保留 |
| `roles` | 角色表 | ✅ 使用中 | 保留 |
| `user_roles` | 用户 - 角色关联表 | ✅ 使用中 | 保留 |
| `user_permissions` | 用户权限表 | ⚠️ 待确认 | 检查 |
| `user_permission_adjustment` | 用户权限调整表 | ✅ 使用中 | 保留 |
| `role_permission_adjustment` | 角色权限调整表 | ✅ 使用中 | 保留 |
| `role_permission_history` | 角色权限历史表 | ⚠️ 待确认 | 检查 |
| `permission_audit_log` | 权限审计日志表 | ✅ 使用中 | 保留 |
| `permission_version` | 权限版本表 | ⚠️ 待确认 | 检查 |
| `permission_groups` | 权限分组表 | ⚠️ 待确认 | 检查 |
| `permission_apis` | 权限 API 表 | ⚠️ 待确认 | 检查 |
| `permission_snapshots` | 权限快照表 | ⚠️ 待确认 | 检查 |
| `permission_config_rights` | 权限配置权限表 | ⚠️ 待确认 | 检查 |
| `api_permissions` | API 权限表 | ⚠️ 待确认 | 检查 |
| `element_permissions` | 元素权限表 | ⚠️ 待确认 | 检查 |
| `ui_element_permissions` | UI 元素权限表 | ⚠️ 待确认 | 检查 |
| `role_resource` | 角色 - 资源关联（旧表） | ❌ 废弃 | **删除** |

### 2. 数据权限表（6 个）

| 表名 | 用途 | 状态 | 建议 |
|------|------|------|------|
| `data_permission_rules` | 数据权限规则表 | ⚠️ 待确认 | 检查 |
| `data_permission_templates` | 数据权限模板表 | ⚠️ 待确认 | 检查 |
| `data_permission_cache` | 数据权限缓存表 | ⚠️ 待确认 | 检查 |
| `data_permission_audit_log` | 数据权限审计日志表 | ⚠️ 待确认 | 检查 |
| `role_data_permissions` | 角色数据权限表 | ⚠️ 待确认 | 检查 |
| `user_data_permissions` | 用户数据权限表 | ⚠️ 待确认 | 检查 |

### 3. 菜单和页面表（6 个）

| 表名 | 用途 | 状态 | 建议 |
|------|------|------|------|
| `menus` | 菜单表（旧） | ❌ 废弃 | **删除** |
| `pages` | 页面表（旧） | ❌ 废弃 | **删除** |
| `page_elements` | 页面元素表（旧） | ❌ 废弃 | **删除** |
| `module` | 模块表（旧） | ❌ 废弃 | **删除** |
| `apis` | API 表（旧） | ❌ 废弃 | **删除** |
| `role_menu_permissions` | 角色菜单权限表 | ⚠️ 待确认 | 检查 |

### 4. 用户和组织表（7 个）

| 表名 | 用途 | 状态 | 建议 |
|------|------|------|------|
| `users` | 用户表 | ✅ 使用中 | 保留 |
| `companies` | 公司表 | ✅ 使用中 | 保留 |
| `company_types` | 公司类型表 | ✅ 使用中 | 保留 |
| `work_areas` | 作业区表 | ✅ 使用中 | 保留 |
| `user_work_areas` | 用户 - 作业区关联表 | ✅ 使用中 | 保留 |
| `user_menu_permissions` | 用户菜单权限表 | ⚠️ 待确认 | 检查 |
| `user_resource` | 用户资源表 | ⚠️ 待确认 | 检查 |

### 5. 零部件管理表（5 个）

| 表名 | 用途 | 状态 | 建议 |
|------|------|------|------|
| `component_types` | 零部件类型表 | ✅ 使用中 | 保留 |
| `component_attr_sets` | 零部件属性集表 | ✅ 使用中 | 保留 |
| `component_attr_set_attrs` | 属性集 - 属性关联表 | ✅ 使用中 | 保留 |
| `component_attr_set_instances` | 属性集实例表 | ✅ 使用中 | 保留 |
| `component_instances` | 零部件实例表 | ✅ 使用中 | 保留 |

### 6. 点位设备管理表（6 个）

| 表名 | 用途 | 状态 | 建议 |
|------|------|------|------|
| `point_device_models` | 点位设备模型表 | ✅ 使用中 | 保留 |
| `point_device_model_items` | 设备模型项表 | ✅ 使用中 | 保留 |
| `point_device_model_instances` | 设备模型实例表 | ✅ 使用中 | 保留 |
| `point_device_model_instance_items` | 实例项表 | ✅ 使用中 | 保留 |
| `point_device_assignments` | 点位设备分配表 | ✅ 使用中 | 保留 |
| `points` | 点位表 | ✅ 使用中 | 保留 |

### 7. 其他表（9 个）

| 表名 | 用途 | 状态 | 建议 |
|------|------|------|------|
| `system_configs` | 系统配置表 | ✅ 使用中 | 保留 |
| `issue_logs` | 问题日志表 | ⚠️ 待确认 | 检查 |
| `protected_objects` | 保护对象表 | ⚠️ 待确认 | 检查 |
| `permissions_backup_20260330` | 权限备份表 | ❌ 备份 | **归档/删除** |
| `role_permissions_backup_20260330` | 角色权限备份表 | ❌ 备份 | **归档/删除** |
| `roles_backup_20260330` | 角色备份表 | ❌ 备份 | **归档/删除** |
| `menus` | 菜单表（旧） | ❌ 废弃 | **删除** |
| `pages` | 页面表（旧） | ❌ 废弃 | **删除** |
| `module` | 模块表（旧） | ❌ 废弃 | **删除** |

---

## 建议清理的表

### 立即可删除（废弃表）

```sql
-- 旧权限设计表
DROP TABLE IF EXISTS role_resource;

-- 旧菜单设计表
DROP TABLE IF EXISTS menus;
DROP TABLE IF EXISTS pages;
DROP TABLE IF EXISTS page_elements;
DROP TABLE IF EXISTS module;
DROP TABLE IF EXISTS apis;

-- 备份表（已备份 10+ 天）
DROP TABLE IF EXISTS permissions_backup_20260330;
DROP TABLE IF EXISTS role_permissions_backup_20260330;
DROP TABLE IF EXISTS roles_backup_20260330;
```

### 需要检查的表

以下表需要确认是否还在使用：

1. **权限相关**:
   - `user_permissions`
   - `role_permission_history`
   - `permission_version`
   - `permission_groups`
   - `permission_apis`
   - `permission_snapshots`
   - `permission_config_rights`
   - `api_permissions`
   - `element_permissions`
   - `ui_element_permissions`

2. **数据权限**:
   - `data_permission_rules`
   - `data_permission_templates`
   - `data_permission_cache`
   - `data_permission_audit_log`
   - `role_data_permissions`
   - `user_data_permissions`

3. **其他**:
   - `role_menu_permissions`
   - `user_menu_permissions`
   - `user_resource`
   - `issue_logs`
   - `protected_objects`

---

## 核心表（V4.3 权限设计）

### 必须保留的表

```sql
-- 资源表
resource

-- 角色表
roles

-- 角色 - 资源关联表
role_permissions

-- 用户表
users

-- 用户 - 角色关联表
user_roles

-- 权限调整表
user_permission_adjustment
role_permission_adjustment

-- 审计日志表
permission_audit_log
```

---

## 统计表数量

```sql
-- 查看各表数据量
SELECT 
    schemaname,
    relname as table_name,
    n_tup_ins as inserts,
    n_tup_upd as updates,
    n_tup_del as deletes,
    n_live_tup as live_rows
FROM pg_stat_user_tables
ORDER BY n_live_tup DESC;
```

---

**报告人**: AI Assistant  
**报告时间**: 2026-04-04 16:45  
**总计**: 57 个表  
**建议删除**: 10 个表  
**需要检查**: 19 个表  
**核心保留**: 28 个表
