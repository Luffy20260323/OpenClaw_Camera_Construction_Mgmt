# 菜单为空问题排查总结

**排查时间**: 2026-04-04 15:10

---

## 问题现象
admin 登录后左侧菜单为空

---

## 排查结果

### ✅ 后端正常

1. **数据库有数据**: 30+ 个 MODULE 和 MENU 记录
2. **API 返回正常**: `/api/resources/menu-tree` 返回完整菜单树
3. **登录接口**: 返回 `menus` 字段包含完整菜单列表

### ❌ 前端问题

**SidebarMenu.vue 的权限判断已修复**:
```javascript
// ✅ 已修复：使用常量
import { ROLE_SUPER_ADMIN } from '@/constants/roles'
if (userStore.roles?.includes(ROLE_SUPER_ADMIN)) { return true }
```

---

## 可能原因

### 原因 1: 登录时菜单未正确加载

**检查点**:
```javascript
// stores/user.js - login 方法
const { accessToken, refreshToken, userInfo, menus } = res.data

// 检查 menus 是否为空
console.log('[Login] 登录响应数据:', { userInfo, menus })
```

**解决**: 查看浏览器控制台登录时的日志

---

### 原因 2: 前端缓存问题

**检查点**:
```javascript
// localStorage 中是否有旧的菜单数据
console.log(localStorage.getItem('userInfo'))
```

**解决**:
1. 清除浏览器缓存（Ctrl+Shift+Delete）
2. 清除 localStorage: `localStorage.clear()`
3. 重新登录

---

### 原因 3: SidebarMenu 组件未正确接收菜单数据

**检查点**:
```javascript
// SidebarMenu.vue
const userMenus = ref(userStore.menus || [])

// 检查 userStore.menus 是否有值
console.log('userStore.menus:', userStore.menus)
```

**解决**: 在浏览器控制台检查 Pinia store 状态

---

## 调试步骤

### 1. 清除所有缓存

**浏览器**:
```
F12 -> Application -> Storage -> Clear site data
或 Ctrl+Shift+Delete
```

**Redis**（可选）:
```bash
redis-cli
> KEYS user:*
> DEL <keys>
```

### 2. 重新登录并查看控制台

**打开浏览器控制台 (F12)**，登录后查看：

```javascript
// 1. 检查登录响应
// 在 stores/user.js 的 login 方法中有 console.log

// 2. 检查 localStorage
console.log('userInfo:', JSON.parse(localStorage.getItem('userInfo')))

// 3. 检查 Pinia store
console.log('menus:', window.$pinia.state.value.user.menus)

// 4. 检查 SidebarMenu 组件
// 在组件中添加 console.log
```

### 3. 预期结果

```json
{
  "menus": [
    {
      "id": 1,
      "name": "系统设置",
      "code": "system",
      "type": "MODULE",
      "children": [...]
    },
    ...
  ]
}
```

---

## 临时测试方案

### 方案 1: 禁用权限检查

**SidebarMenu.vue**:
```javascript
const hasMenuPermission = (menu) => {
  // 临时禁用权限检查
  return true
}
```

如果菜单显示了，说明是权限判断问题。

### 方案 2: 手动设置菜单

**浏览器控制台**:
```javascript
// 手动设置菜单数据
const menus = [{id:1,name:'测试菜单',code:'test',path:'/test',children:[]}]
localStorage.setItem('userInfo', JSON.stringify({
  ...JSON.parse(localStorage.getItem('userInfo')),
  menus: menus
}))
location.reload()
```

---

## 下一步

1. **清除缓存并重新登录**
2. **查看浏览器控制台日志**
3. **检查 `userStore.menus` 是否有值**
4. **如果仍为空，检查登录 API 响应**

---

**状态**: 🔍 需要前端调试确认
