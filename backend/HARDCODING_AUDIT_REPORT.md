# 硬编码全面排查报告

**排查时间**: 2026-04-04 15:05  
**排查范围**: 后端 Java + 前端 Vue/JS

---

## 排查结果总结

| 类型 | 数量 | 状态 | 说明 |
|------|------|------|------|
| **角色 Code 硬编码** | 0 处 | ✅ 全部使用常量 | 通过 RoleConstants/roles.js 管理 |
| **缓存 Key 常量** | 2 处 | ✅ 合理使用 | 在 Service 内部定义 |
| **操作类型字符串** | 5 处 | ⚠️ 建议常量 | 审计日志操作类型 |
| **权限码字符串** | 20+ 处 | ⚠️ 正常使用 | v-permission 指令需要 |
| **模块名称映射** | 2 处 | ✅ 合理使用 | 显示用途 |

---

## 详细分析

### ✅ 已正确使用常量的部分

#### 1. 角色 Code（100% 使用常量）

**后端**:
```java
// ✅ 正确：使用 RoleConstants
RoleConstants.ROLE_SUPER_ADMIN
RoleConstants.ROLE_SYSTEM_ADMIN
```

**使用位置**:
- `UserContext.java` ✅
- `PermissionServiceImpl.java` ✅
- `RolePermissionController.java` ✅

**前端**:
```javascript
// ✅ 正确：使用 roles.js 常量
import { ROLE_SUPER_ADMIN } from '@/constants/roles'
ROLE_SUPER_ADMIN
ROLE_SYSTEM_ADMIN
```

**使用位置**:
- `SidebarMenu.vue` ✅
- `Management.vue` ✅
- `RolePermission.vue` ✅

---

### ✅ 合理的内部常量

#### PermissionCacheService.java

```java
// ✅ 合理：缓存 Key 前缀常量
private static final String ROLE_PERMISSION_PREFIX = "role:permission:";
private static final long ROLE_PERMISSION_TTL_MINUTES = 60;
```

**说明**: 这些是内部使用的常量，已在类内部定义为 `static final`，符合规范。

---

### ⚠️ 建议改进的部分

#### 1. 审计日志操作类型

**文件**: `PermissionController.java`, `AuditLog.vue`

**当前代码**:
```java
// 后端
.operationType("UPDATE_ROLE_DEFAULT_PERMISSION")
.operationType("CONFIG_ROLE_PERMISSION")
```

```javascript
// 前端
'ROLE_PERMISSION': '角色权限',
'CONFIG_ROLE_PERMISSION': '配置角色权限',
'ROLLBACK_ROLE_PERMISSION': '回滚权限配置'
```

**建议**: 创建操作类型常量类

**改进方案**:

```java
// 后端：AuditLogConstants.java
public final class AuditLogConstants {
    public static final String CONFIG_ROLE_PERMISSION = "CONFIG_ROLE_PERMISSION";
    public static final String UPDATE_ROLE_DEFAULT_PERMISSION = "UPDATE_ROLE_DEFAULT_PERMISSION";
    public static final String ROLLBACK_ROLE_PERMISSION = "ROLLBACK_ROLE_PERMISSION";
}
```

```javascript
// 前端：auditLogTypes.js
export const AuditLogTypes = {
  CONFIG_ROLE_PERMISSION: 'CONFIG_ROLE_PERMISSION',
  UPDATE_ROLE_DEFAULT_PERMISSION: 'UPDATE_ROLE_DEFAULT_PERMISSION',
  ROLLBACK_ROLE_PERMISSION: 'ROLLBACK_ROLE_PERMISSION'
}

export const AuditLogLabels = {
  CONFIG_ROLE_PERMISSION: '配置角色权限',
  UPDATE_ROLE_DEFAULT_PERMISSION: '更新角色缺省权限',
  ROLLBACK_ROLE_PERMISSION: '回滚权限配置'
}
```

---

#### 2. 权限码字符串（v-permission 指令）

**文件**: 多个 Vue 组件

**示例**:
```vue
<el-button v-permission="'system:component:create'">新建</el-button>
```

**说明**: 这些**不是硬编码问题**，而是权限码的正常使用。

**原因**:
- 权限码需要在模板中直接使用
- 每个权限码都是唯一的业务标识
- 不适合抽象为常量（会导致常量爆炸）

**最佳实践**:
```vue
<!-- ✅ 推荐：直接在模板中使用 -->
<el-button v-permission="'system:user:create:button'">新建</el-button>

<!-- ❌ 不推荐：过度抽象 -->
<el-button v-permission="USER_CREATE_BUTTON">新建</el-button>
```

---

### ✅ 合理的模块名称映射

#### GlobalSearch.vue, ResourceList.vue

```javascript
system: '系统设置',
component: '零部件管理'
```

**说明**: 这些是**显示用途的映射**，用于将模块 Code 转换为显示名称，属于合理使用。

---

## 硬编码判断标准

### ✅ 不是硬编码（合理使用）

1. **常量定义**: `static final` 或 `export const`
2. **配置值**: 从配置文件或环境变量读取
3. **业务标识**: 权限码、菜单 Code 等唯一标识
4. **显示映射**: Code 到显示名称的映射
5. **内部常量**: 类内部定义的 `private static final`

### ❌ 是硬编码（需要改进）

1. **分散的字符串**: 同样的字符串在多处出现
2. **魔法数字**: 无注释的数字（如 `if (status == 1)`）
3. **可配置值**: 应该从配置读取的值
4. **业务规则**: 可能变化的业务逻辑值

---

## 改进建议

### 优先级 1：审计日志操作类型（建议实施）

**创建常量文件**:

```java
// backend/src/main/java/com/qidian/camera/module/auth/constant/AuditLogConstants.java
package com.qidian.camera.module.auth.constant;

/**
 * 审计日志操作类型常量
 */
public final class AuditLogConstants {
    // 角色权限相关
    public static final String CONFIG_ROLE_PERMISSION = "CONFIG_ROLE_PERMISSION";
    public static final String UPDATE_ROLE_DEFAULT_PERMISSION = "UPDATE_ROLE_DEFAULT_PERMISSION";
    public static final String ROLLBACK_ROLE_PERMISSION = "ROLLBACK_ROLE_PERMISSION";
    
    // 用户权限相关
    public static final String CONFIG_USER_PERMISSION = "CONFIG_USER_PERMISSION";
    public static final String ADJUST_USER_PERMISSION = "ADJUST_USER_PERMISSION";
    
    // 资源相关
    public static final String CREATE_RESOURCE = "CREATE_RESOURCE";
    public static final String UPDATE_RESOURCE = "UPDATE_RESOURCE";
    public static final String DELETE_RESOURCE = "DELETE_RESOURCE";
    
    private AuditLogConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
```

---

### 优先级 2：权限码常量（可选）

**如果某些权限码频繁使用**，可以考虑常量：

```javascript
// frontend/src/constants/permissions.js
// 用户管理权限
export const USER_CREATE = 'system:user:create:button'
export const USER_EDIT = 'system:user:edit:button'
export const USER_DELETE = 'system:user:delete:button'

// 角色管理权限
export const ROLE_CREATE = 'system:role:create:button'
export const ROLE_EDIT = 'system:role:edit:button'
export const ROLE_PERMISSION_CONFIG = 'system:role:permission:button'
```

**使用**:
```vue
<el-button v-permission="USER_CREATE">新建</el-button>
```

**注意**: 这会增加维护成本，**仅在权限码频繁复用时考虑**。

---

## 总结

### 当前状态

| 项目 | 硬编码数量 | 状态 |
|------|-----------|------|
| 角色 Code | 0 处 | ✅ 100% 使用常量 |
| 缓存 Key | 0 处 | ✅ 内部常量管理 |
| 操作类型 | 5 处 | ⚠️ 建议常量（优先级 1） |
| 权限码 | 20+ 处 | ✅ 正常使用（无需修改） |
| 模块映射 | 2 处 | ✅ 合理使用 |

### 总体评价

**✅ 良好**: 核心业务常量（角色 Code）已全部使用常量管理

**⚠️ 可改进**: 审计日志操作类型建议常量

**✅ 合理**: 权限码、模块映射等属于正常使用，无需修改

---

## 行动项

### 立即执行（无）
- 角色 Code 已全部使用常量 ✅

### 建议执行（可选）
- [ ] 创建 `AuditLogConstants.java`
- [ ] 创建 `auditLogTypes.js`
- [ ] 更新相关引用

### 无需执行
- 权限码字符串（正常使用）
- 模块名称映射（显示用途）

---

**排查人**: AI Assistant  
**排查时间**: 2026-04-04 15:05  
**结论**: ✅ 无严重硬编码问题，核心常量已规范管理
