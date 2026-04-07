# BUG 修复：admin 登录后菜单显示不全

**BUG ID**: BUG-001
**发现时间**: 2026-04-06 09:55
**修复时间**: 2026-04-06 10:00
**状态**: ✅ 已修复

---

## 问题描述

admin 登录后，侧边栏只显示"系统设置"和"零部件管理"两个顶级菜单项，其下的所有子菜单项都不见了。

**现象**：
- ✅ 顶级菜单显示：系统设置、零部件管理
- ❌ 子菜单全部缺失：如"用户权限配置"、"角色管理"、"零部件种类管理"等

---

## 问题根因

**错误的数据源引用**：`SidebarMenu.vue` 使用了错误的菜单数据源。

### 数据结构说明

登录响应包含两个菜单相关字段：

| 字段 | 类型 | 用途 |
|------|------|------|
| `userInfo.menus` | `string[]` | 菜单**编码数组**，用于路由权限检查 |
| `menus` (响应根字段) | `MenuDTO[]` | 完整**菜单树**，用于侧边栏渲染 |

### 错误代码

**`user.js` store**:
```javascript
getters: {
  menus: (state) => state.userInfo?.menus || []  // 返回编码数组！
}
```

**`SidebarMenu.vue`** (错误):
```javascript
// ❌ 错误：使用了 menus getter，得到的是字符串数组
const userMenus = ref(userStore.menus || [])
```

### 正确数据流

```
登录响应
  ├─ userInfo.menus → ["home", "system", "component_management", ...]  (编码数组)
  └─ menus → [{id:1, code:"system", children:[...]}, ...]  (菜单树对象数组)
      ↓
保存到 userInfo.menuTree
      ↓
SidebarMenu.vue 应该使用 userInfo.menuTree
```

---

## 修复方案

### 1. 修复 `SidebarMenu.vue`

**文件**: `frontend/src/components/SidebarMenu.vue`

**修改前**:
```javascript
const userMenus = ref(userStore.menus || [])

watch(
  () => userStore.menus,
  (newMenus) => {
    if (newMenus !== userMenus.value) {
      userMenus.value = newMenus || []
    }
  },
  { deep: false }
)
```

**修改后**:
```javascript
const userMenus = ref(userStore.userInfo?.menuTree || [])

watch(
  () => userStore.userInfo?.menuTree,
  (newMenuTree) => {
    if (newMenuTree !== userMenus.value) {
      userMenus.value = newMenuTree || []
    }
  },
  { deep: false }
)
```

### 2. 修复 `user.js` refreshMenus 方法

**文件**: `frontend/src/stores/user.js`

**修改前**:
```javascript
async refreshMenus() {
  const res = await getMyMenus()
  const menus = res.data || []
  
  const userInfoWithMenus = {
    ...this.userInfo,
    menus  // ❌ 错误：覆盖了 userInfo.menus（编码数组）
  }
  // ...
}
```

**修改后**:
```javascript
async refreshMenus() {
  const res = await getMyMenus()
  const menuTree = res.data || []
  
  const userInfoWithMenus = {
    ...this.userInfo,
    menuTree  // ✅ 正确：更新 userInfo.menuTree（菜单树）
  }
  // ...
}
```

---

## 验证结果

### 后端 API 验证
```bash
curl http://localhost:8080/api/resources/menu-tree
```

**返回结构**：
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "name": "系统设置",
      "code": "system",
      "type": "MODULE",
      "children": [
        {"id": 9, "name": "系统管理", "code": "system_config", ...},
        {"id": 143, "name": "认证管理", "code": "auth", ...},
        {"id": 144, "name": "组织管理", "code": "user", "children": [...]},
        ...
      ]
    },
    {
      "id": 2,
      "name": "零部件管理",
      "code": "component_management",
      "type": "MODULE",
      "children": [...]
    }
  ]
}
```

✅ 后端返回完整的菜单树结构

### 前端验证
- ✅ 重新构建前端：`npm run build`
- ✅ 重新部署容器：`docker compose up -d frontend`
- ✅ 容器状态：healthy

### 预期效果
用户重新登录后，侧边栏应显示：
- ✅ 系统设置
  - 系统管理
  - 认证管理
  - 组织管理
    - 公司管理
    - 作业区管理
  - 组织管理（user）
    - 用户管理
    - 用户权限配置
    - 用户权限详情
    - 个人中心
  - 角色管理
    - 角色管理
    - 基本权限配置
    - 角色权限配置
    - 角色缺省权限
  - 菜单管理
  - 权限管理
    - 数据权限配置
    - 权限审计日志
    - 角色类型缺省权限
    - ELEMENT 管理
  - 文档管理
    - 文档中心
  - 基础配置
  - 资源管理
    - 孤儿资源管理
    - 资源管理
  - 审计日志
  - 系统配置
  - 资源管理
- ✅ 零部件管理
  - 零部件类型管理
  - 零部件种类管理
  - 零部件属性集管理
  - 属性集实例管理
  - 零部件实例管理
  - 点位设备模型管理
  - 点位设备模型实例管理
  - 点位批量分配
  - 公司管理
  - 作业区管理
  - 点位管理

---

## 相关文件

### 修改的文件
- `frontend/src/components/SidebarMenu.vue` - 修复菜单数据源引用
- `frontend/src/stores/user.js` - 修复 refreshMenus 方法

### 相关文档
- `TASK_T028_REMOVE_MENU_PATH.md` - MENU 资源 path 字段移除任务（本次 BUG 的诱因）

---

## 经验教训

1. **数据结构命名清晰**：`menus` 和 `menuTree` 应该有更明确的区别
2. **类型检查**：应该在使用菜单数据时进行类型检查（数组 vs 对象数组）
3. **测试覆盖**：修改菜单相关代码后，应该验证侧边栏显示是否正常
4. **代码审查**：数据流变化需要仔细审查所有引用点

---

**模型**: qwen3.5-plus
