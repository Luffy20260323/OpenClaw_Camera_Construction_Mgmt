# 数据库表清理报告

**执行时间**: 2026-04-04 16:50  
**操作**: 删除废弃表

---

## 已删除的表（9 个）

### 旧设计表（6 个）

1. ✅ `role_resource` - 旧角色 - 资源关联表
   - 已被 `role_permissions` 取代
   - **影响**: 删除了 4 个视图（v_user_permissions 等）

2. ✅ `menus` - 旧菜单表
   - 已被 `resource` 表取代
   - **影响**: 删除了 2 个外键约束

3. ✅ `pages` - 旧页面表
   - 已被 `resource` 表取代

4. ✅ `page_elements` - 旧页面元素表
   - 已被 `resource` 表取代

5. ✅ `module` - 旧模块表
   - 已被 `resource` 表取代

6. ✅ `apis` - 旧 API 表
   - 已被 `resource` 表取代

### 备份表（3 个）

7. ✅ `permissions_backup_20260330` - 权限备份表（2026-03-30）
8. ✅ `role_permissions_backup_20260330` - 角色权限备份表
9. ✅ `roles_backup_20260330` - 角色备份表

---

## 删除影响

### 级联删除的依赖对象

**删除 `role_resource` 时**:
- ❌ 视图 `v_user_permissions` 被删除
- ❌ 视图 `v_role_permission_tree` 被删除
- ❌ 视图 `v_permission_matrix` 被删除
- ❌ 视图 `v_user_permission_detail` 被删除

**删除 `menus` 时**:
- ❌ 外键 `role_menu_permissions_menu_id_fkey` 被删除
- ❌ 外键 `user_menu_permissions_menu_id_fkey` 被删除

**删除 `pages` 时**:
- ❌ 外键 `page_elements_page_id_fkey` 被删除

**删除 `apis` 时**:
- ❌ 外键 `permission_apis_api_id_fkey` 被删除

---

## 当前表清单（48 个）

### 权限相关（13 个）

| 表名 | 用途 | 状态 |
|------|------|------|
| `resource` | 资源表 | ✅ |
| `role_permissions` | 角色 - 资源关联 | ✅ |
| `roles` | 角色表 | ✅ |
| `user_roles` | 用户 - 角色关联 | ✅ |
| `user_permissions` | 用户权限表 | ⚠️ |
| `user_permission_adjustment` | 用户权限调整 | ✅ |
| `role_permission_adjustment` | 角色权限调整 | ✅ |
| `role_permission_history` | 角色权限历史 | ⚠️ |
| `permission_audit_log` | 权限审计日志 | ✅ |
| `permission_version` | 权限版本 | ⚠️ |
| `permission_groups` | 权限分组 | ⚠️ |
| `permission_apis` | 权限 API | ⚠️ |
| `permission_snapshots` | 权限快照 | ⚠️ |

### 数据权限（6 个）

| 表名 | 用途 | 状态 |
|------|------|------|
| `data_permission_rules` | 数据权限规则 | ⚠️ |
| `data_permission_templates` | 数据权限模板 | ⚠️ |
| `data_permission_cache` | 数据权限缓存 | ⚠️ |
| `data_permission_audit_log` | 数据权限审计 | ⚠️ |
| `role_data_permissions` | 角色数据权限 | ⚠️ |
| `user_data_permissions` | 用户数据权限 | ⚠️ |

### 用户和组织（7 个）

| 表名 | 用途 | 状态 |
|------|------|------|
| `users` | 用户表 | ✅ |
| `companies` | 公司表 | ✅ |
| `company_types` | 公司类型表 | ✅ |
| `work_areas` | 作业区表 | ✅ |
| `user_work_areas` | 用户 - 作业区关联 | ✅ |
| `user_menu_permissions` | 用户菜单权限 | ⚠️ |
| `user_resource` | 用户资源 | ⚠️ |

### 零部件管理（5 个）

| 表名 | 用途 | 状态 |
|------|------|------|
| `component_types` | 零部件类型 | ✅ |
| `component_attr_sets` | 属性集 | ✅ |
| `component_attr_set_attrs` | 属性集 - 属性关联 | ✅ |
| `component_attr_set_instances` | 属性集实例 | ✅ |
| `component_instances` | 零部件实例 | ✅ |

### 点位设备管理（6 个）

| 表名 | 用途 | 状态 |
|------|------|------|
| `point_device_models` | 设备模型 | ✅ |
| `point_device_model_items` | 模型项 | ✅ |
| `point_device_model_instances` | 模型实例 | ✅ |
| `point_device_model_instance_items` | 实例项 | ✅ |
| `point_device_assignments` | 设备分配 | ✅ |
| `points` | 点位 | ✅ |

### 其他（11 个）

| 表名 | 用途 | 状态 |
|------|------|------|
| `system_configs` | 系统配置 | ✅ |
| `role_menu_permissions` | 角色菜单权限 | ⚠️ |
| `role_type_default_permissions` | 角色类型缺省权限 | ⚠️ |
| `role_default_permissions` | 角色缺省权限 | ⚠️ |
| `api_permissions` | API 权限 | ⚠️ |
| `element_permissions` | 元素权限 | ⚠️ |
| `ui_element_permissions` | UI 元素权限 | ⚠️ |
| `permission_config_rights` | 权限配置权限 | ⚠️ |
| `issue_logs` | 问题日志 | ⚠️ |
| `protected_objects` | 保护对象 | ⚠️ |

---

## 统计

| 项目 | 数量 |
|------|------|
| 删除前表数 | 57 |
| 删除后表数 | 48 |
| 删除表数 | 9 |
| 核心表（使用中） | 约 25 个 |
| 待确认表 | 约 23 个 |

---

## 下一步建议

### 需要检查的表（23 个）

这些表需要确认是否还在使用：

1. **权限相关**: `user_permissions`, `role_permission_history`, `permission_version`, `permission_groups`, `permission_apis`, `permission_snapshots`
2. **数据权限**: 所有 6 个 data_permission 相关表
3. **其他**: `user_menu_permissions`, `user_resource`, `role_menu_permissions`, `role_type_default_permissions`, `role_default_permissions`, `api_permissions`, `element_permissions`, `ui_element_permissions`, `permission_config_rights`, `issue_logs`, `protected_objects`

### 检查方法

```sql
-- 检查表是否有数据
SELECT 'table_name' as table_name, COUNT(*) as row_count FROM table_name;

-- 检查表最后访问时间
SELECT schemaname, relname, last_vacuum, last_analyze
FROM pg_stat_user_tables
WHERE relname = 'table_name';
```

---

**执行人**: AI Assistant  
**执行时间**: 2026-04-04 16:50  
**状态**: ✅ 完成
