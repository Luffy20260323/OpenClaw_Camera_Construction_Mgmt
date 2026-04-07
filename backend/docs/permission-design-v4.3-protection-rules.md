# 权限系统保护原则

**版本**: V4.3  
**更新日期**: 2026-04-04  
**状态**: 强制执行

---

## 核心保护原则

### 原则 1：角色保护

| 角色 | Code | 保护级别 | 说明 |
|------|------|----------|------|
| **超级管理员** | `ROLE_SUPER_ADMIN` | 🔒🔒 完全保护 | 权限不可调整，始终拥有全部权限 |
| **系统管理员** | `ROLE_SYSTEM_ADMIN` | 🔒 部分保护 | 角色不可删除，权限可调整 |

**保护规则**:
1. ✅ 超级管理员角色：**禁止删除** + **禁止调整权限**
2. ✅ 系统管理员角色：**禁止删除**
3. ✅ 其他角色：可删除、可调整权限

---

### 原则 2：admin 用户保护

| 用户 | 保护级别 | 说明 |
|------|----------|------|
| **admin** | 🔒🔒🔒 完全保护 | 禁止删除、禁止修改角色、禁止调整权限 |

**保护规则**:
1. ✅ admin 用户：**禁止删除**
2. ✅ admin 用户：**禁止修改角色**
3. ✅ admin 用户：**禁止调整权限**
4. ✅ admin 用户：**始终拥有 ROLE_SUPER_ADMIN 角色**

---

### 原则 3：权限继承

```
超级管理员 (ROLE_SUPER_ADMIN)
├── 始终拥有全部权限（125+ 个）
├── 权限不可调整（前端禁止修改）
└── 仅 admin 用户拥有

系统管理员 (ROLE_SYSTEM_ADMIN)
├── 权限可配置
├── 角色不可删除
└── 多个用户可以拥有
```

---

## 数据库约束

### 1. 角色表约束

```sql
ALTER TABLE roles ADD CONSTRAINT chk_role_protection 
CHECK (
    -- 超级管理员和系统管理员角色必须标记为系统保护
    (role_code IN ('ROLE_SUPER_ADMIN', 'ROLE_SYSTEM_ADMIN') AND is_system_protected = true)
    OR
    (role_code NOT IN ('ROLE_SUPER_ADMIN', 'ROLE_SYSTEM_ADMIN'))
);
```

### 2. admin 用户约束

```sql
-- admin 用户必须拥有 ROLE_SUPER_ADMIN 角色
-- 通过触发器实现
CREATE OR REPLACE FUNCTION trg_admin_user_check()
RETURNS TRIGGER AS $$
BEGIN
    -- 如果是 admin 用户（user_id=1），确保拥有 ROLE_SUPER_ADMIN 角色
    IF NEW.user_id = 1 THEN
        IF NOT EXISTS (
            SELECT 1 FROM user_roles ur
            JOIN roles r ON ur.role_id = r.id
            WHERE ur.user_id = 1 AND r.role_code = 'ROLE_SUPER_ADMIN'
        ) THEN
            RAISE EXCEPTION 'admin 用户必须拥有 ROLE_SUPER_ADMIN 角色';
        END IF;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
```

---

## 前端实现规范

### 1. 角色列表页面

**受保护角色标识**:
```vue
<template>
  <el-table-column label="系统保护" width="100">
    <template #default="{ row }">
      <el-tag v-if="row.is_system_protected" type="warning" effect="plain">
        🔒 保护
      </el-tag>
      <span v-else>-</span>
    </template>
  </el-table-column>
</template>
```

**删除按钮禁用逻辑**:
```vue
<template>
  <el-button
    size="small"
    type="danger"
    @click="handleDelete(scope.row)"
    :disabled="scope.row.is_system_protected"
  >
    删除
  </el-button>
</template>

<script>
// 受保护角色禁止删除
const isProtectedRole = (role) => {
  return role && role.is_system_protected
}
</script>
```

---

### 2. 角色权限配置页面

**超级管理员权限禁止调整**:
```vue
<template>
  <el-card v-if="isSuperAdminRole">
    <template #header>
      <div class="card-header">
        <span>角色权限配置 - {{ selectedRole.roleName }}</span>
        <el-alert
          type="warning"
          title="超级管理员角色权限不可调整"
          description="超级管理员始终拥有系统全部权限"
          :closable="false"
          show-icon
        />
      </div>
    </template>
    
    <el-empty description="超级管理员角色始终拥有全部权限，无需配置" />
  </el-card>
  
  <PermissionTree
    v-else
    v-model="selectedPermissions"
    :tree-data="permissionTree"
  />
</template>

<script setup>
import { ROLE_SUPER_ADMIN } from '@/constants/roles'

// 判断是否为超级管理员角色
const isSuperAdminRole = computed(() => {
  return selectedRole.value?.role_code === ROLE_SUPER_ADMIN
})
</script>
```

---

### 3. 用户管理页面

**admin 用户保护**:
```vue
<template>
  <el-table-column label="操作" width="200" fixed="right">
    <template #default="{ row }">
      <el-button
        size="small"
        @click="handleEdit(scope.row)"
        :disabled="row.username === 'admin'"
      >
        编辑
      </el-button>
      
      <el-button
        size="small"
        type="danger"
        @click="handleDelete(scope.row)"
        :disabled="row.username === 'admin'"
      >
        删除
      </el-button>
    </template>
  </el-table-column>
</template>

<script setup>
// admin 用户禁止删除和修改
const isProtectedUser = (user) => {
  return user && user.username === 'admin'
}
</script>
```

**admin 用户角色禁止修改**:
```vue
<template>
  <el-form-item label="角色分配">
    <RoleSelector
      v-model="form.roleIds"
      :roles="availableRoles"
      multiple
      :disabled="isUserAdmin"
    />
    <el-alert
      v-if="isUserAdmin"
      type="warning"
      title="admin 用户角色不可修改"
      description="admin 用户始终拥有超级管理员角色"
      :closable="false"
      show-icon
      class="mt-2"
    />
  </el-form-item>
</template>

<script setup>
const isUserAdmin = computed(() => {
  return currentUser.value?.username === 'admin'
})
</script>
```

---

### 4. 用户权限配置页面

**admin 用户权限禁止调整**:
```vue
<template>
  <div v-if="isUserAdmin">
    <el-alert
      type="warning"
      title="admin 用户权限不可调整"
      description="admin 用户始终拥有系统全部权限"
      :closable="false"
      show-icon
    />
    
    <el-empty 
      description="admin 用户始终拥有全部权限，无需配置" 
      class="mt-4"
    />
  </div>
  
  <PermissionTree
    v-else
    v-model="selectedPermissions"
    :tree-data="permissionTree"
  />
</template>

<script setup>
const isUserAdmin = computed(() => {
  return currentUser.value?.username === 'admin'
})
</script>
```

---

## 后端实现规范

### 1. 角色删除接口

```java
/**
 * 删除角色
 * 受保护角色禁止删除
 */
@Transactional
public Result deleteRole(Long roleId) {
    Role role = roleMapper.selectById(roleId);
    
    // 检查是否为受保护角色
    if (role.getIsSystemProtected()) {
        return Result.error("系统保护角色，禁止删除");
    }
    
    roleMapper.deleteById(roleId);
    return Result.success();
}
```

### 2. 角色权限配置接口

```java
/**
 * 配置角色权限
 * 超级管理员角色禁止调整权限
 */
@Transactional
public Result configureRolePermissions(Long roleId, List<Long> permissionIds) {
    Role role = roleMapper.selectById(roleId);
    
    // 超级管理员角色禁止调整权限
    if (RoleConstants.ROLE_SUPER_ADMIN.equals(role.getRoleCode())) {
        return Result.error("超级管理员角色权限不可调整");
    }
    
    // 正常配置权限...
    permissionMapper.deleteByRoleId(roleId);
    for (Long permissionId : permissionIds) {
        permissionMapper.insert(roleId, permissionId);
    }
    
    return Result.success();
}
```

### 3. 用户删除接口

```java
/**
 * 删除用户
 * admin 用户禁止删除
 */
@Transactional
public Result deleteUser(Long userId) {
    User user = userMapper.selectById(userId);
    
    // admin 用户禁止删除
    if ("admin".equals(user.getUsername())) {
        return Result.error("admin 用户禁止删除");
    }
    
    userMapper.deleteById(userId);
    return Result.success();
}
```

### 4. 用户角色修改接口

```java
/**
 * 修改用户角色
 * admin 用户角色禁止修改
 */
@Transactional
public Result updateUserRoles(Long userId, List<Long> roleIds) {
    User user = userMapper.selectById(userId);
    
    // admin 用户角色禁止修改
    if ("admin".equals(user.getUsername())) {
        return Result.error("admin 用户角色不可修改");
    }
    
    userRoleMapper.deleteByUserId(userId);
    for (Long roleId : roleIds) {
        userRoleMapper.insert(userId, roleId);
    }
    
    return Result.success();
}
```

### 5. 用户权限配置接口

```java
/**
 * 配置用户权限
 * admin 用户权限禁止调整
 */
@Transactional
public Result configureUserPermissions(Long userId, List<Long> permissionIds) {
    User user = userMapper.selectById(userId);
    
    // admin 用户权限禁止调整
    if ("admin".equals(user.getUsername())) {
        return Result.error("admin 用户权限不可调整");
    }
    
    // 正常配置权限...
    userPermissionAdjustMapper.deleteByUserId(userId);
    for (Long permissionId : permissionIds) {
        userPermissionAdjustMapper.insert(userId, permissionId, "ADD");
    }
    
    return Result.success();
}
```

---

## 代码检查清单

### 前端检查项

- [ ] 角色列表页面：受保护角色删除按钮禁用
- [ ] 角色权限配置页面：超级管理员角色权限禁止调整
- [ ] 用户管理页面：admin 用户删除按钮禁用
- [ ] 用户管理页面：admin 用户编辑按钮禁用
- [ ] 用户管理页面：admin 用户角色分配禁用
- [ ] 用户权限配置页面：admin 用户权限禁止调整
- [ ] 所有提示信息清晰明确

### 后端检查项

- [ ] 角色删除接口：检查 is_system_protected
- [ ] 角色权限配置接口：检查 ROLE_SUPER_ADMIN
- [ ] 用户删除接口：检查 username = 'admin'
- [ ] 用户角色修改接口：检查 username = 'admin'
- [ ] 用户权限配置接口：检查 username = 'admin'
- [ ] 所有接口返回友好的错误信息

---

## 数据库验证

### 验证 SQL

```sql
-- 1. 验证受保护角色
SELECT role_code, role_name, is_system_protected
FROM roles
WHERE role_code IN ('ROLE_SUPER_ADMIN', 'ROLE_SYSTEM_ADMIN');

-- 预期结果：
-- ROLE_SUPER_ADMIN  | 超级管理员 | true
-- ROLE_SYSTEM_ADMIN | 系统管理员 | true

-- 2. 验证 admin 用户角色
SELECT u.username, r.role_code, r.role_name
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
WHERE u.username = 'admin';

-- 预期结果：
-- admin | ROLE_SUPER_ADMIN | 超级管理员

-- 3. 验证超级管理员权限数量
SELECT COUNT(*) as permission_count
FROM role_permissions rp
JOIN roles r ON rp.role_id = r.id
WHERE r.role_code = 'ROLE_SUPER_ADMIN';

-- 预期结果：125（或更多，取决于系统资源数量）
```

---

## 测试用例

### 测试 1：删除受保护角色

**操作**: 尝试删除 ROLE_SUPER_ADMIN 角色  
**预期**: ❌ 失败，提示"系统保护角色，禁止删除"

### 测试 2：调整超级管理员权限

**操作**: 尝试移除 ROLE_SUPER_ADMIN 的某个权限  
**预期**: ❌ 失败，提示"超级管理员角色权限不可调整"

### 测试 3：删除 admin 用户

**操作**: 尝试删除 username='admin' 的用户  
**预期**: ❌ 失败，提示"admin 用户禁止删除"

### 测试 4：修改 admin 用户角色

**操作**: 尝试移除 admin 用户的 ROLE_SUPER_ADMIN 角色  
**预期**: ❌ 失败，提示"admin 用户角色不可修改"

### 测试 5：调整 admin 用户权限

**操作**: 尝试移除 admin 用户的某个权限  
**预期**: ❌ 失败，提示"admin 用户权限不可调整"

---

## 违规处理

### 前端违规

**现象**: 用户可以点击删除受保护角色  
**处理**: 立即修复，添加禁用逻辑

### 后端违规

**现象**: 接口未检查保护逻辑  
**处理**: 立即修复，添加验证逻辑

### 数据违规

**现象**: admin 用户没有 ROLE_SUPER_ADMIN 角色  
**处理**: 立即修复数据，添加约束触发器

---

## 总结

### 保护级别

| 对象 | 保护级别 | 禁止操作 |
|------|----------|----------|
| ROLE_SUPER_ADMIN | 🔒🔒 完全保护 | 删除、调整权限 |
| ROLE_SYSTEM_ADMIN | 🔒 部分保护 | 删除 |
| admin 用户 | 🔒🔒🔒 完全保护 | 删除、修改角色、调整权限 |

### 实施原则

1. ✅ **前端禁用**: 受保护对象的操作按钮禁用
2. ✅ **后端验证**: 所有接口必须验证保护逻辑
3. ✅ **友好提示**: 清晰说明禁止原因
4. ✅ **数据约束**: 数据库层面添加约束

---

**文档版本**: V4.3  
**更新日期**: 2026-04-04  
**执行状态**: 强制执行
