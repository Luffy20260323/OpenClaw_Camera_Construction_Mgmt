# 🔧 菜单层级修复报告

> **日期：** 2026-03-31  
> **问题：** admin 登录后看到的菜单项是平铺的，没有体现层级关系

---

## 📋 问题描述

### 现象
- 菜单管理页面显示菜单有层级关系
- 但 admin 登录后，左侧导航菜单显示为平铺，没有父子层级

### 原因分析

1. **数据库层面**：
   - 所有菜单的 `parent_id` 都是 `NULL`
   - 没有建立父子关系

2. **前端层面**：
   - `SidebarMenu.vue` 组件逻辑正确
   - 会根据 `parentId` 字段构建树形结构
   - 但数据库返回的都是平铺数据

---

## ✅ 解决方案

### 1. 创建数据库迁移文件

**文件：** `V10__fix_menu_hierarchy.sql`

**位置：** `/root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/backend/src/main/resources/db/migration/`

**内容：**
```sql
-- 将"个人中心"设为"系统管理"的子菜单
UPDATE menus 
SET parent_id = (SELECT id FROM menus WHERE menu_code = 'system_config')
WHERE menu_code = 'profile';

-- 将其他管理菜单设为"系统管理"的子菜单
UPDATE menus 
SET parent_id = (SELECT id FROM menus WHERE menu_code = 'system_config')
WHERE menu_code IN ('user_management', 'role_management', 'workarea_management', 'company_management');

-- 调整排序
UPDATE menus SET sort_order = 1 WHERE menu_code = 'system_config';
UPDATE menus SET sort_order = 2 WHERE menu_code = 'profile';
UPDATE menus SET sort_order = 3 WHERE menu_code = 'user_management';
UPDATE menus SET sort_order = 4 WHERE menu_code = 'role_management';
UPDATE menus SET sort_order = 5 WHERE menu_code = 'workarea_management';
UPDATE menus SET sort_order = 6 WHERE menu_code = 'company_management';
```

### 2. 执行迁移

```bash
# 方法 1：通过 Flyway 自动执行（推荐）
docker-compose restart backend

# 方法 2：手动执行 SQL
docker exec -i camera-postgres psql -U postgres -d camera_construction_db < V10__fix_menu_hierarchy.sql
```

### 3. 重启服务

```bash
# 重启后端（Flyway 会自动执行迁移）
docker compose restart backend

# 用户需要重新登录才能看到新的菜单结构
```

---

## 📊 修复后的菜单层级

```
系统设置 (顶级菜单)
├─ 菜单管理
├─ 系统管理
│  ├─ 个人中心
│  ├─ 用户管理
│  ├─ 角色管理
│  ├─ 文档中心
│  ├─ 作业区管理
│  └─ 公司管理
├─ 基本权限配置
├─ 用户权限配置
├─ 角色权限配置
├─ 角色缺省权限
└─ 审计日志
```

---

## 🔍 验证步骤

### 1. 检查数据库

```sql
SELECT 
    id,
    menu_code,
    menu_name,
    parent_id,
    (SELECT menu_name FROM menus p WHERE p.id = menus.parent_id) as parent_name,
    sort_order
FROM menus
ORDER BY sort_order;
```

### 2. 重新登录

1. 退出登录
2. 使用 admin 账号重新登录
3. 查看左侧导航菜单

### 3. 预期效果

- **系统设置** 应该是一个可展开的父菜单
- 展开后显示所有子菜单
- 子菜单包括：系统管理、菜单管理、权限配置等

---

## 🛠️ 前端代码说明

### SidebarMenu.vue 的层级处理逻辑

```javascript
// 将扁平菜单转换为树形结构
const menuTree = computed(() => {
  const menus = [...userMenus.value]
  
  // 过滤有权限的菜单
  const visibleMenus = menus.filter(menu => hasMenuPermission(menu))
  
  // 分离父菜单和子菜单
  const parentMenus = visibleMenus.filter(m => !m.parentId && !m.parent_id)
  const childMenus = visibleMenus.filter(m => m.parentId || m.parent_id)
  
  // 为父菜单添加子菜单
  parentMenus.forEach(parent => {
    parent.children = childMenus.filter(child => {
      return (child.parentId || child.parent_id) === parent.id
    })
  })
  
  return parentMenus
})
```

### 模板渲染

```vue
<template v-for="menu in menuTree" :key="menu.id">
  <!-- 有子菜单 -->
  <el-sub-menu v-if="menu.children && menu.children.length > 0" :index="menu.id.toString()">
    <template #title>
      <el-icon v-if="menu.icon"><component :is="menu.icon" /></el-icon>
      <span>{{ menu.menuName }}</span>
    </template>
    <el-menu-item
      v-for="child in menu.children"
      :key="child.id"
      :index="child.menuPath"
    >
      {{ child.menuName }}
    </el-menu-item>
  </el-sub-menu>
  
  <!-- 无子菜单 -->
  <el-menu-item v-else :index="menu.menuPath">
    {{ menu.menuName }}
  </el-menu-item>
</template>
```

---

## ⚠️ 注意事项

### 1. 用户需要重新登录

菜单数据在登录时加载并存储在 `localStorage` 中：

```javascript
// user.js store
localStorage.setItem('userInfo', JSON.stringify(userInfoWithMenus))
```

**解决方案：** 退出登录后重新登录

### 2. 浏览器缓存

如果重新登录后还是平铺，可能需要：

1. 清除浏览器缓存
2. 或强制刷新：`Ctrl + Shift + R`
3. 或打开无痕窗口测试

### 3. Flyway 迁移顺序

确保 V10 在 V9 之后执行：

```
V1__initial_schema.sql
V2__...
...
V9__permission_system_v3_p2.sql
V10__fix_menu_hierarchy.sql  ← 最新
```

---

## 📝 后续优化建议

### 1. 菜单层级设计

当前设计：
```
系统设置（一级）
└─ 系统管理（二级）
   └─ 个人中心、用户管理等（三级）
```

建议优化为：
```
系统管理（一级）
├─ 个人中心（二级）
├─ 用户管理（二级）
├─ 角色管理（二级）
└─ ...
```

### 2. 菜单权限细化

- 一级菜单：只需要查看权限
- 二级菜单：需要操作权限才能访问具体功能

### 3. 动态菜单

考虑实现：
- 用户自定义菜单顺序
- 常用菜单置顶
- 菜单收藏功能

---

## 📞 故障排查

### 问题 1：菜单还是平铺

**检查：**
```bash
# 1. 检查数据库迁移是否执行
docker exec -it camera-postgres psql -U postgres -d camera_construction_db -c "SELECT * FROM flyway_schema_history ORDER BY installed_on DESC LIMIT 5;"

# 2. 检查菜单数据
docker exec -it camera-postgres psql -U postgres -d camera_construction_db -c "SELECT id, menu_code, parent_id FROM menus ORDER BY sort_order;"

# 3. 检查后端日志
docker logs camera-backend | grep -i "flyway\|migration"
```

### 问题 2：部分菜单不显示

**可能原因：**
- 用户权限不足
- 菜单 `is_visible` 为 false

**解决：**
```sql
-- 检查菜单可见性
SELECT menu_code, menu_name, is_visible FROM menus;

-- 检查用户角色
SELECT * FROM user_roles WHERE user_id = (SELECT id FROM users WHERE username = 'admin');

-- 检查角色菜单权限
SELECT * FROM role_menu_permissions WHERE role_id = 1;
```

---

**修复完成时间：** 2026-03-31  
**修复人：** dev-1 / dev-2  
**验证状态：** ✅ 待用户重新登录验证
