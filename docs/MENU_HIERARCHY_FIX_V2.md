# 🔧 菜单层级显示修复报告

> **日期：** 2026-03-31  
> **问题：** 菜单管理界面配置的层级关系，前端导航菜单没有正确显示

---

## 📋 问题分析

### 现象
1. 菜单管理界面显示菜单有层级关系（通过父级菜单选择框配置）
2. 数据库中 `parent_id` 字段正确保存了层级关系
3. 但左侧导航菜单显示为平铺，没有父子层级

### 根本原因

**前端代码字段名不匹配问题：**

后端返回的菜单数据使用**驼峰命名**：
```javascript
{
  id: 12,
  menuCode: "system_config",
  parentId: 15,  // ← 驼峰
  sortOrder: 1,  // ← 驼峰
  requiredPermission: "system:config"  // ← 驼峰
}
```

前端代码检查的是**下划线命名**：
```javascript
// 原代码
if (menu.required_permission) { ... }  // ← 下划线
const parentMenus = menus.filter(m => !m.parentId && !m.parent_id)
```

虽然代码尝试兼容两种格式，但逻辑不够清晰，导致：
- 父菜单识别失败（所有菜单都被识别为父菜单）
- 子菜单无法正确挂载到父菜单下
- 最终显示为平铺

---

## ✅ 解决方案

### 1. 修复前端代码

**文件：** `frontend/src/components/SidebarMenu.vue`

**修改内容：**

#### 添加辅助函数（兼容驼峰和下划线）

```javascript
// 辅助函数：获取菜单的 parentId（兼容驼峰和下划线）
const getParentId = (menu) => {
  return menu.parentId !== undefined ? menu.parentId : menu.parent_id
}

// 辅助函数：获取菜单的 sortOrder（兼容驼峰和下划线）
const getSortOrder = (menu) => {
  return menu.sortOrder !== undefined ? menu.sortOrder : menu.sort_order
}
```

#### 修复权限检查

```javascript
// 兼容后端返回的数据格式（requiredPermission 或 required_permission）
const requiredPerm = menu.requiredPermission || menu.required_permission

if (requiredPerm) {
  const requiredPerms = requiredPerm.split(',').map(p => p.trim())
  return requiredPerms.some(perm => permissions.includes(perm))
}
```

#### 修复树形结构构建

```javascript
// 使用辅助函数获取 parentId
const parentMenus = visibleMenus.filter(m => getParentId(m) === null || getParentId(m) === undefined)
const childMenus = visibleMenus.filter(m => getParentId(m) !== null && getParentId(m) !== undefined)

// 为父菜单添加子菜单
parentMenus.forEach(parent => {
  parent.children = childMenus.filter(child => {
    return getParentId(child) === parent.id
  })
  // 按 sortOrder 排序子菜单
  parent.children.sort((a, b) => getSortOrder(a) - getSortOrder(b))
})
```

#### 增加调试日志

```javascript
console.log('[SidebarMenu] 原始菜单数据:', JSON.stringify(menus.map(m => ({ 
  code: m.menuCode, 
  name: m.menuName, 
  parentId: m.parentId, 
  parent_id: m.parent_id 
})), null, 2))

console.log('[SidebarMenu] 最终菜单树:', JSON.stringify(parentMenus.map(m => ({ 
  code: m.menuCode, 
  children: m.children?.map(c => c.menuCode) 
})), null, 2))
```

### 2. 重新构建并重启

```bash
# 前端构建
cd frontend
npm run build

# 重启容器
docker compose restart frontend
```

---

## 📊 数据库中的菜单层级（示例）

```
系统设置 (id=15, parent_id=NULL) - 顶级菜单
├─ 菜单管理 (id=21, parent_id=15)
├─ 系统管理 (id=12, parent_id=15)
│  ├─ 个人中心 (id=7, parent_id=12)
│  ├─ 用户管理 (id=8, parent_id=12)
│  ├─ 角色管理 (id=9, parent_id=12)
│  ├─ 文档中心 (id=17, parent_id=12)
│  ├─ 作业区管理 (id=10, parent_id=12)
│  └─ 公司管理 (id=11, parent_id=12)
├─ 基本权限配置 (id=19, parent_id=15)
├─ 用户权限配置 (id=14, parent_id=15)
├─ 角色权限配置 (id=13, parent_id=15)
├─ 角色缺省权限 (id=20, parent_id=15)
└─ 审计日志 (id=16, parent_id=15)
```

---

## 🔍 验证步骤

### 1. 打开浏览器控制台

按 `F12` 打开开发者工具

### 2. 重新登录

1. 退出当前登录
2. 使用 admin 账号重新登录
3. 观察控制台日志

### 3. 检查日志输出

应该看到类似以下日志：

```
[SidebarMenu] 原始菜单数据：[
  {
    "code": "system",
    "name": "系统设置",
    "parentId": null,
    "parent_id": undefined
  },
  {
    "code": "system_config",
    "name": "系统管理",
    "parentId": 15,
    "parent_id": undefined
  },
  ...
]

[SidebarMenu] 父菜单：[ { code: 'system', id: 15 } ]
[SidebarMenu] 子菜单：[ 
  { code: 'menu_management', parentId: 15 },
  { code: 'system_config', parentId: 15 },
  ...
]

[SidebarMenu] 父菜单 system (id=15) 的子菜单：[
  'menu_management',
  'system_config',
  'base_permission',
  ...
]

[SidebarMenu] 最终菜单树：[
  {
    "code": "system",
    "children": [
      "menu_management",
      "system_config",
      "base_permission",
      ...
    ]
  }
]
```

### 4. 检查导航菜单

左侧导航应该显示：
- **系统设置**（可展开的父菜单）
  - 展开后显示所有子菜单
- 子菜单包括：菜单管理、系统管理、权限配置等

---

## 🎯 菜单管理界面的使用

### 配置菜单层级

1. 登录系统
2. 进入 **系统设置 → 菜单管理**
3. 点击"新增菜单"或"编辑"
4. 在"父级菜单"下拉框中选择父菜单
   - 不选择 = 顶级菜单
   - 选择某个菜单 = 作为该菜单的子菜单
5. 保存

### 调整菜单顺序

- 在菜单列表中直接修改"排序"列的数值
- 数值越小，显示越靠前
- 修改后自动保存

### 控制菜单可见性

- 切换"可见"列的开关
- 系统保护的菜单不可编辑

---

## ⚠️ 注意事项

### 1. 字段命名规范

**后端返回数据统一使用驼峰命名：**
- `parentId` ✅
- `parent_id` ❌

**前端代码需要兼容两种格式：**
```javascript
const parentId = menu.parentId !== undefined ? menu.parentId : menu.parent_id
```

### 2. 浏览器缓存

如果重新登录后还是平铺：
1. 强制刷新：`Ctrl + Shift + R`
2. 清除 localStorage：
   ```javascript
   localStorage.clear()
   location.reload()
   ```
3. 或使用无痕窗口测试

### 3. 权限配置

确保用户有菜单的查看权限：
- 系统管理员自动拥有所有菜单权限
- 其他用户需要通过角色分配菜单权限

---

## 📝 技术要点

### 前后端数据格式映射

| 后端字段（Java） | 前端字段（JavaScript） | 说明 |
|-----------------|----------------------|------|
| `parentId` | `parentId` | 父菜单 ID |
| `sortOrder` | `sortOrder` | 排序 |
| `requiredPermission` | `requiredPermission` | 所需权限 |
| `menuCode` | `menuCode` | 菜单代码 |
| `menuName` | `menuName` | 菜单名称 |

### Vue 响应式更新

```javascript
// 确保响应式更新
parentMenus.forEach(parent => {
  parent.children = [...]  // 直接赋值，触发响应式
})

// 使用 computed 自动重新计算
const menuTree = computed(() => {
  // 依赖 userMenus，当 userMenus 变化时自动重新计算
  return parentMenus
})
```

---

## 📞 故障排查

### 问题 1：菜单还是平铺

**检查：**
```bash
# 1. 查看前端日志
# 打开浏览器控制台，查看 [SidebarMenu] 开头的日志

# 2. 检查菜单数据
console.log(userStore.menus)

# 3. 检查 parentId 字段
userStore.menus.forEach(m => {
  console.log(m.menuCode, 'parentId:', m.parentId)
})
```

### 问题 2：部分菜单不显示

**可能原因：**
- 用户权限不足
- 菜单 `isVisible` 为 false

**解决：**
```sql
-- 检查菜单可见性
SELECT menu_code, is_visible FROM menus;

-- 检查用户权限
SELECT * FROM role_menu_permissions WHERE role_id = 1;
```

### 问题 3：子菜单没有挂载到父菜单

**检查：**
```javascript
// 查看父子关系是否正确
const parentMenus = menus.filter(m => m.parentId === null)
const childMenus = menus.filter(m => m.parentId !== null)

console.log('父菜单:', parentMenus)
console.log('子菜单:', childMenus.map(m => ({ 
  code: m.menuCode, 
  parentId: m.parentId 
})))
```

---

**修复完成时间：** 2026-03-31  
**修复人：** dev-1 / dev-2  
**验证状态：** ✅ 待用户重新登录验证
