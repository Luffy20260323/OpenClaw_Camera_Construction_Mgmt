# 菜单为空问题排查指南

## 问题现象
admin 登录后，左侧菜单栏为空

## 已修复的问题

### SidebarMenu.vue - 角色判断错误

**修改前**:
```javascript
// 系统管理员拥有所有权限
if (userStore.roles?.includes('system_admin') || permissions.includes('*:*:*')) {
  return true
}
```

**修改后**:
```javascript
// 超级管理员拥有所有权限
if (userStore.roles?.includes('ROLE_SUPER_ADMIN') || permissions.includes('*:*:*')) {
  return true
}
```

---

## 排查步骤

### 1. 检查登录响应

**打开浏览器控制台 (F12)**，登录后查看：

```javascript
// 检查 localStorage 中的用户信息
console.log('userInfo:', JSON.parse(localStorage.getItem('userInfo')))

// 检查 roles
console.log('roles:', JSON.parse(localStorage.getItem('userInfo')).roles)

// 检查 permissions
console.log('permissions:', JSON.parse(localStorage.getItem('userInfo')).permissions)

// 检查 menus
console.log('menus:', JSON.parse(localStorage.getItem('userInfo')).menus)
```

**预期结果**:
```json
{
  "roles": ["ROLE_SUPER_ADMIN"],
  "permissions": ["*:*:*", ...],  // 或有大量权限
  "menus": [...]  // 不应为空
}
```

---

### 2. 检查 Pinia Store

**在控制台执行**:
```javascript
// 检查 userStore
console.log('userStore.roles:', window.$pinia.state.value.user.roles)
console.log('userStore.permissions:', window.$pinia.state.value.user.permissions)
console.log('userStore.menus:', window.$pinia.state.value.user.menus)
```

---

### 3. 检查后端登录接口

**使用 Postman 或 curl 测试**:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"<your_password>"}'
```

**检查响应中的**:
- `userInfo.roles` - 应包含 `ROLE_SUPER_ADMIN`
- `userInfo.permissions` - 应包含权限列表
- `menus` - 应包含菜单列表

---

### 4. 检查菜单 API

```bash
curl http://localhost:8080/api/resources/menu-tree \
  -H "Authorization: Bearer <your_token>"
```

**预期**: 返回菜单树数据

---

## 可能的原因和解决方案

### 原因 1: 登录响应中没有 roles

**检查后端**: `AuthController.java` 或 `AuthService.java`

**解决**: 确保登录接口返回 userInfo.roles

---

### 原因 2: 菜单 API 返回空

**检查**: 后端 `ResourceService.getMenuTree()`

**解决**: 检查数据库中 resource 表的 is_visible 和 status 字段

---

### 原因 3: 前端路由守卫拦截

**检查**: `router/index.js` 中的 `beforeEach`

**解决**: 确保路由守卫没有错误拦截

---

### 原因 4: 缓存问题

**解决**:
```bash
# 清除 Redis 缓存
redis-cli
> KEYS user:menus:*
> DEL user:menus:1

# 清除浏览器缓存
# Ctrl+Shift+Delete 或 F12 -> Application -> Clear storage
```

---

## 快速测试

### 临时禁用权限检查

**SidebarMenu.vue**:
```javascript
const hasMenuPermission = (menu) => {
  // 临时禁用权限检查，测试菜单显示
  return true
}
```

如果菜单显示了，说明是权限判断问题。

---

## 验证修复

1. **清除浏览器缓存**
2. **重新登录 admin**
3. **检查控制台输出**
4. **查看左侧菜单是否显示**

---

**修复文件**: `SidebarMenu.vue`  
**修复内容**: 角色判断从 `system_admin` 改为 `ROLE_SUPER_ADMIN`  
**状态**: ✅ 已修复，等待测试验证
