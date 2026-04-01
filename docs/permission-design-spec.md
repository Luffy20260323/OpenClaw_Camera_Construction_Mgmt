# 权限管理与设计规范

> 文档版本：v1.0  
> 最后更新：2026-03-31  
> 状态：设计评审中

---

## 目录

1. [设计原则](#1-设计原则)
2. [核心概念](#2-核心概念)
3. [资源模型](#3-资源模型)
4. [角色模型](#4-角色模型)
5. [权限集合](#5-权限集合)
6. [数据库设计](#6-数据库设计)
7. [权限计算逻辑](#7-权限计算逻辑)
8. [前后端校验机制](#8-前后端校验机制)
9. [数据权限](#9-数据权限)
10. [权限数据生成规范](#10-权限数据生成规范)
11. [操作流程](#11-操作流程)

---

## 1. 设计原则

### 1.1 核心思想

**菜单即权限，权限即资源，资源天然形成树形结构。**

采用 **"资源树 + 角色 + 策略"** 的混合模型，基于 RBAC（Role-Based Access Control）扩展。

### 1.2 设计原则

| 原则 | 说明 |
|------|------|
| 资源统一抽象 | 系统中所有需要权限控制的"事物"统一抽象为资源 |
| 树形层级结构 | 资源通过 parent_id 形成树形结构，支持继承 |
| 前端控制显示 | 前端控制菜单/按钮的显示，提升用户体验 |
| 后端控制执行 | 后端必须独立校验 API 权限，确保安全 |
| 权限可追溯 | 所有权限变更记录审计日志，便于追溯 |
| 基本权限保障 | 任何角色都具备基本权限，防止权限丢失 |

---

## 2. 核心概念

### 2.1 资源（Resource）

系统中所有需要权限控制的对象统一抽象为资源。

### 2.2 角色（Role）

权限的集合载体，用户通过角色获得权限。

### 2.3 权限集合分类

| 集合类型 | 说明 | 来源 | 可修改者 |
|---------|------|------|----------|
| **基本权限集合** | 最基础的权限，保证用户能登录/退出系统 | 系统初始数据 | 仅 admin 用户 |
| **缺省权限集合** | 角色创建时预设的权限 | 系统初始数据 / 角色定义 | admin 或授权的系统管理员 |
| **完整权限集合** | 角色当前实际拥有的权限 | 基本权限 + 缺省权限 + 调整 | 通过调整包管理 |

**权限集合关系**：
```
基本权限集合 ⊂ 缺省权限集合 ⊂ 完整权限集合
```

### 2.4 权限调整包

管理员对角色权限的增减记录，用于：
- 记录权限变更
- 计算最终权限
- 权限审计追溯

---

## 3. 资源模型

### 3.1 资源类型层级

```
资源类型：
├── 模块（Module）        —— 一级菜单
│   ├── 菜单（Menu）      —— 二级菜单/页面入口
│   │   ├── 页面（Page）  —— 独立页面（可选）
│   │   │   ├── 元素（Element）  —— 按钮、Tab、表格列等
│   │   │   └── API（API）        —— 接口，可挂载在元素或页面下
│   │   └── API（API）    —— 直接挂在菜单下的 API（如导出、统计）
│   └── 独立权限（Permission）    —— 无 UI 的后台能力
```

### 3.2 资源类型说明

| 类型 | 编码 | 说明 | 示例 |
|------|------|------|------|
| 模块 | `MODULE` | 一级菜单，业务模块入口 | 用户管理、订单管理 |
| 菜单 | `MENU` | 二级菜单，页面入口 | 用户列表、角色管理 |
| 页面 | `PAGE` | 独立页面 | 用户详情页 |
| 元素 | `ELEMENT` | 页面内可操作元素 | 新增按钮、删除按钮、导出按钮 |
| API | `API` | 后端接口 | `POST /api/users`、`GET /api/orders/export` |
| 独立权限 | `PERMISSION` | 无 UI 的后台能力 | 数据同步任务、定时任务执行 |

### 3.3 资源关键字段

| 字段 | 类型 | 说明 | 示例 |
|------|------|------|------|
| `id` | BIGINT | 主键 | 1 |
| `name` | VARCHAR(100) | 资源名称 | 用户管理 |
| `code` | VARCHAR(50) | 资源编码 | `user:manage` |
| `type` | ENUM | 资源类型 | `MODULE`, `MENU`, `PAGE`, `ELEMENT`, `API`, `PERMISSION` |
| `parent_id` | BIGINT | 父资源ID | NULL（根节点） |
| `permission_key` | VARCHAR(100) | 权限标识（唯一） | `user:export`, `order:delete` |
| `uri_pattern` | VARCHAR(255) | URI 匹配模式 | `/api/users/**` |
| `method` | VARCHAR(10) | HTTP 方法 | `GET`, `POST`, `PUT`, `DELETE` |
| `sort_order` | INT | 排序号 | 1 |
| `status` | TINYINT | 状态 | 1=启用, 0=禁用 |
| `created_at` | DATETIME | 创建时间 | |
| `updated_at` | DATETIME | 更新时间 | |

### 3.4 permission_key 命名规范

```
格式：<模块>:<动作>[:<子动作>]

示例：
- user:view          # 查看用户
- user:create        # 创建用户
- user:update        # 更新用户
- user:delete        # 删除用户
- user:export        # 导出用户
- order:view         # 查看订单
- order:export:excel # 导出订单Excel
```

---

## 4. 角色模型

### 4.1 角色分类

| 类型 | 说明 | 示例 |
|------|------|------|
| **系统角色** | 系统内置角色，不可删除 | admin（超级管理员） |
| **默认角色** | 系统预设角色，可修改权限 | 普通用户、部门管理员 |
| **自定义角色** | 管理员创建的角色 | 项目经理、数据分析师 |

### 4.2 特殊角色：admin

- 系统初始创建，具备所有权限
- 权限不可修改
- 可创建其他系统管理员用户
- 可授权其他系统管理员管理角色权限

### 4.3 角色权限管理矩阵

| 操作 | admin | 授权的系统管理员 | 普通管理员 |
|------|-------|-----------------|-----------|
| 管理基本权限集合 | ✅ | ❌ | ❌ |
| 管理缺省权限集合 | ✅ | ✅ | ❌ |
| 调整角色权限 | ✅ | ✅ | ❌ |
| 调整用户权限 | ✅ | ✅ | ❌ |
| 创建自定义角色 | ✅ | ✅ | ❌ |

---

## 5. 权限集合

### 5.1 基本权限集合（Basic Permission Set）

**定义**：所有角色必须具备的最小权限集合。

**包含内容**：
- 登录系统
- 退出系统
- 查看个人信息
- 修改个人密码

**管理规则**：
- 由系统初始数据提供
- 只能由 admin 用户修改
- 新建角色自动分配基本权限
- 基本权限不可从角色中删除

### 5.2 缺省权限集合（Default Permission Set）

**定义**：角色创建时预设的权限集合。

**管理规则**：
- 系统内置角色：由系统初始数据定义
- 自定义角色：创建时可指定，或从模板复制
- 可由 admin 或授权的系统管理员修改

### 5.3 权限调整包（Permission Adjustment Package）

**定义**：管理员对角色权限的增减记录。

**作用**：
1. 记录当前生效的权限调整
2. 计算角色最终权限
3. 权限管理界面展示调整来源

**计算公式**：
```
角色完整权限 = 基本权限集合 ∪ 缺省权限集合 + 权限调整包
            = (role_permissions WHERE type='basic' OR type='default')
            ∪ (adjustments WHERE action='ADD')
            - (adjustments WHERE action='REMOVE')
```

---

## 6. 数据库设计

### 6.1 资源表（resource）

```sql
CREATE TABLE resource (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(100) NOT NULL COMMENT '资源名称',
    code            VARCHAR(50) NOT NULL COMMENT '资源编码',
    type            VARCHAR(20) NOT NULL COMMENT '资源类型：MODULE/MENU/PAGE/ELEMENT/API/PERMISSION',
    parent_id       BIGINT DEFAULT NULL COMMENT '父资源ID',
    permission_key  VARCHAR(100) DEFAULT NULL COMMENT '权限标识',
    uri_pattern     VARCHAR(255) DEFAULT NULL COMMENT 'URI匹配模式',
    method          VARCHAR(10) DEFAULT NULL COMMENT 'HTTP方法：GET/POST/PUT/DELETE',
    icon            VARCHAR(100) DEFAULT NULL COMMENT '图标',
    path            VARCHAR(255) DEFAULT NULL COMMENT '前端路由路径',
    component       VARCHAR(255) DEFAULT NULL COMMENT '前端组件路径',
    sort_order      INT DEFAULT 0 COMMENT '排序号',
    status          TINYINT DEFAULT 1 COMMENT '状态：1=启用, 0=禁用',
    is_basic        TINYINT DEFAULT 0 COMMENT '是否基本权限：1=是, 0=否',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_permission_key (permission_key),
    UNIQUE KEY uk_code (code),
    INDEX idx_parent_id (parent_id),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源表';
```

### 6.2 角色表（role）

```sql
CREATE TABLE role (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(50) NOT NULL COMMENT '角色名称',
    code            VARCHAR(50) NOT NULL COMMENT '角色编码',
    type            VARCHAR(20) NOT NULL COMMENT '角色类型：SYSTEM/DEFAULT/CUSTOM',
    description     VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
    status          TINYINT DEFAULT 1 COMMENT '状态：1=启用, 0=禁用',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';
```

### 6.3 角色-资源关联表（role_permission）

```sql
CREATE TABLE role_permission (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id         BIGINT NOT NULL COMMENT '角色ID',
    resource_id     BIGINT NOT NULL COMMENT '资源ID',
    permission_type VARCHAR(20) NOT NULL COMMENT '权限类型：basic/default',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_by      BIGINT NOT NULL COMMENT '创建人ID',
    
    UNIQUE KEY uk_role_resource (role_id, resource_id),
    INDEX idx_role_id (role_id),
    INDEX idx_resource_id (resource_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-资源关联表';
```

### 6.4 权限调整表（role_permission_adjustment）

```sql
CREATE TABLE role_permission_adjustment (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id         BIGINT NOT NULL COMMENT '角色ID',
    resource_id     BIGINT NOT NULL COMMENT '资源ID',
    action          VARCHAR(10) NOT NULL COMMENT '操作类型：ADD/REMOVE',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_by      BIGINT NOT NULL COMMENT '操作人ID',
    
    UNIQUE KEY uk_role_resource (role_id, resource_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限调整表';
```

### 6.5 权限变更审计表（permission_audit_log）

```sql
CREATE TABLE permission_audit_log (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    target_type     VARCHAR(20) NOT NULL COMMENT '目标类型：ROLE/USER',
    target_id       BIGINT NOT NULL COMMENT '目标ID',
    resource_id     BIGINT NOT NULL COMMENT '资源ID',
    action          VARCHAR(10) NOT NULL COMMENT '操作类型：GRANT/REVOKE',
    source_type     VARCHAR(50) NOT NULL COMMENT '来源类型：BASIC/DEFAULT/ADJUSTMENT/USER_DIRECT',
    operator_id     BIGINT NOT NULL COMMENT '操作人ID',
    operated_at     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    remark          VARCHAR(255) DEFAULT NULL COMMENT '备注',
    
    INDEX idx_target (target_type, target_id),
    INDEX idx_operator (operator_id),
    INDEX idx_operated_at (operated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限变更审计表';
```

### 6.6 用户表（user）

```sql
CREATE TABLE user (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    username        VARCHAR(50) NOT NULL COMMENT '用户名',
    password        VARCHAR(255) NOT NULL COMMENT '密码（加密）',
    real_name       VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    email           VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    phone           VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    status          TINYINT DEFAULT 1 COMMENT '状态：1=启用, 0=禁用',
    is_admin        TINYINT DEFAULT 0 COMMENT '是否超级管理员：1=是, 0=否',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_username (username),
    INDEX idx_email (email),
    INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

### 6.7 用户-角色关联表（user_role）

```sql
CREATE TABLE user_role (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT NOT NULL COMMENT '用户ID',
    role_id         BIGINT NOT NULL COMMENT '角色ID',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_by      BIGINT NOT NULL COMMENT '创建人ID',
    
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-角色关联表';
```

### 6.8 用户权限调整表（user_permission_adjustment）

```sql
CREATE TABLE user_permission_adjustment (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT NOT NULL COMMENT '用户ID',
    resource_id     BIGINT NOT NULL COMMENT '资源ID',
    action          VARCHAR(10) NOT NULL COMMENT '操作类型：ADD/REMOVE',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_by      BIGINT NOT NULL COMMENT '操作人ID',
    expire_at       DATETIME DEFAULT NULL COMMENT '过期时间（临时权限）',
    
    UNIQUE KEY uk_user_resource (user_id, resource_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户权限调整表';
```

---

## 7. 权限计算逻辑

### 7.1 角色权限计算

**计算公式**：
```
角色完整权限 = role_permissions（basic + default） 
            + role_permission_adjustments（ADD）
            - role_permission_adjustments（REMOVE）
```

**SQL 实现**：
```sql
-- 获取角色的完整权限列表
WITH role_base AS (
    -- 基本权限 + 缺省权限（role_permissions 表包含两种类型）
    SELECT resource_id
    FROM role_permission
    WHERE role_id = :roleId
),
role_adj_add AS (
    -- 调整增加的权限
    SELECT resource_id
    FROM role_permission_adjustment
    WHERE role_id = :roleId AND action = 'ADD'
),
role_adj_remove AS (
    -- 调整移除的权限
    SELECT resource_id
    FROM role_permission_adjustment
    WHERE role_id = :roleId AND action = 'REMOVE'
)
SELECT DISTINCT r.*
FROM (
    -- 基础权限
    SELECT resource_id FROM role_base
    UNION
    -- 增加的权限
    SELECT resource_id FROM role_adj_add
) combined
JOIN resource r ON r.id = combined.resource_id
WHERE r.id NOT IN (SELECT resource_id FROM role_adj_remove);
```

### 7.2 用户权限计算

**计算公式**：
```
用户完整权限 = ∑(各角色的完整权限) 
            + user_permission_adjustments（ADD）
            - user_permission_adjustments（REMOVE）
```

**SQL 实现**：
```sql
-- 获取用户的最终权限列表
WITH user_roles AS (
    -- 用户的所有角色
    SELECT role_id FROM user_role WHERE user_id = :userId
),
-- 计算每个角色的完整权限
role_complete_permissions AS (
    -- 各角色的基础权限（basic + default）
    SELECT rp.resource_id
    FROM role_permission rp
    WHERE rp.role_id IN (SELECT role_id FROM user_roles)
    
    UNION
    
    -- 各角色调整增加的权限
    SELECT rpa.resource_id
    FROM role_permission_adjustment rpa
    WHERE rpa.role_id IN (SELECT role_id FROM user_roles) 
      AND rpa.action = 'ADD'
),
role_removed AS (
    -- 各角色调整移除的权限
    SELECT rpa.resource_id
    FROM role_permission_adjustment rpa
    WHERE rpa.role_id IN (SELECT role_id FROM user_roles) 
      AND rpa.action = 'REMOVE'
),
user_adj_add AS (
    -- 用户个人增加的权限
    SELECT resource_id
    FROM user_permission_adjustment
    WHERE user_id = :userId AND action = 'ADD'
      AND (expire_at IS NULL OR expire_at > NOW())
),
user_adj_remove AS (
    -- 用户个人移除的权限
    SELECT resource_id
    FROM user_permission_adjustment
    WHERE user_id = :userId AND action = 'REMOVE'
      AND (expire_at IS NULL OR expire_at > NOW())
)
SELECT DISTINCT r.*
FROM (
    -- 角色继承的权限（减去角色移除的）
    SELECT resource_id FROM role_complete_permissions
    WHERE resource_id NOT IN (SELECT resource_id FROM role_removed)
    
    UNION
    
    -- 用户个人增加的权限
    SELECT resource_id FROM user_adj_add
) combined
JOIN resource r ON r.id = combined.resource_id
WHERE r.id NOT IN (SELECT resource_id FROM user_adj_remove);
```

### 7.3 权限判断流程

```
用户请求 → 从 Token 获取用户ID
        → 查询用户角色列表
        → 计算用户完整权限列表（缓存）
        → 解析请求 URL + Method
        → 匹配资源表中的 API 记录
        → 判断资源ID是否在权限列表中
        → 返回结果
```

---

## 8. 前后端校验机制

### 8.1 前端权限控制

**登录时获取权限**：
```javascript
// 用户登录成功后
const userPermissions = await fetchUserPermissions(userId);
// 返回格式：{ resourceIds: [1,2,3,...], permissionKeys: ['user:view', 'user:create', ...] }
// 存储到 Vuex/Pinia 或 localStorage
```

**菜单渲染控制**：
```vue
<template>
  <sidebar-menu :menu="filteredMenus" />
</template>

<script setup>
const filteredMenus = computed(() => {
  return menus.filter(menu => hasPermission(menu.permissionKey));
});

function hasPermission(key) {
  return userStore.permissionKeys.includes(key);
}
</script>
```

**按钮渲染控制**：
```vue
<template>
  <button v-if="hasPermission('user:create')">新增用户</button>
  <button v-if="hasPermission('user:delete')">删除</button>
</template>
```

**自定义指令**：
```javascript
// permission.js
app.directive('permission', {
  mounted(el, binding) {
    const permission = binding.value;
    if (!hasPermission(permission)) {
      el.parentNode?.removeChild(el);
    }
  }
});

// 使用
<button v-permission="'user:create'">新增用户</button>
```

### 8.2 后端权限校验

**AOP 注解方式**：
```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    String value();  // permission_key
}

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @RequirePermission("user:create")
    @PostMapping
    public Result createUser(@RequestBody UserDTO user) {
        // ...
    }
    
    @RequirePermission("user:delete")
    @DeleteMapping("/{id}")
    public Result deleteUser(@PathVariable Long id) {
        // ...
    }
}
```

**拦截器实现**：
```java
@Component
public class PermissionInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                            HttpServletResponse response, 
                            Object handler) {
        // 1. 获取当前用户
        Long userId = getCurrentUserId(request);
        
        // 2. 获取请求的 URL 和 Method
        String uri = request.getRequestURI();
        String method = request.getMethod();
        
        // 3. 匹配资源
        Resource resource = resourceService.findByUriAndMethod(uri, method);
        if (resource == null) {
            return true; // 未配置权限的资源默认放行
        }
        
        // 4. 检查权限
        if (!permissionService.hasPermission(userId, resource.getId())) {
            response.setStatus(403);
            return false;
        }
        
        return true;
    }
}
```

### 8.3 权限缓存策略

**Redis 缓存**：
```
// 用户权限缓存
Key: user:permission:{userId}
Value: Set<Long> resourceIds
TTL: 30 minutes

// 权限版本号
Key: permission:version:{userId}
Value: Long (递增)
TTL: 无
```

**缓存更新时机**：
- 用户角色变更
- 角色权限变更
- 用户权限调整
- 权限版本号递增

---

## 9. 数据权限

### 9.1 数据范围类型

| 范围类型 | 编码 | 说明 |
|---------|------|------|
| 仅本人数据 | `SELF` | 只能查看/操作自己创建的数据 |
| 本部门数据 | `DEPT` | 可查看/操作本部门的数据 |
| 本部门及下级 | `DEPT_AND_SUB` | 可查看/操作本部门及下级部门的数据 |
| 全部数据 | `ALL` | 可查看/操作所有数据 |

### 9.2 数据权限表设计

```sql
CREATE TABLE role_data_scope (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id         BIGINT NOT NULL COMMENT '角色ID',
    resource_id     BIGINT NOT NULL COMMENT '资源ID',
    scope_type      VARCHAR(20) NOT NULL COMMENT '范围类型：SELF/DEPT/DEPT_AND_SUB/ALL',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_role_resource (role_id, resource_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色数据权限表';
```

### 9.3 数据权限实现

**MyBatis 拦截器方式**：
```java
@Intercepts({
    @Signature(type = Executor.class, method = "query", 
               args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
public class DataPermissionInterceptor implements Interceptor {
    
    @Override
    public Object intercept(Invocation invocation) {
        // 1. 获取当前用户的数据权限范围
        DataScope scope = userDataScopeService.getUserScope(userId, resourceKey);
        
        // 2. 根据 scope_type 追加 SQL 条件
        String scopeSql = buildScopeSql(scope);
        
        // 3. 修改原始 SQL
        // ...
    }
    
    private String buildScopeSql(DataScope scope) {
        switch (scope.getScopeType()) {
            case "SELF":
                return " AND created_by = " + scope.getUserId();
            case "DEPT":
                return " AND dept_id = " + scope.getDeptId();
            case "DEPT_AND_SUB":
                return " AND dept_id IN (SELECT id FROM dept WHERE path LIKE '" + scope.getDeptPath() + "%')";
            case "ALL":
                return ""; // 无限制
        }
    }
}
```

---

## 10. 权限数据生成规范

### 10.1 已完成功能的权限梳理

**步骤**：
1. 整理现有功能模块
2. 按"模块-菜单-页面-元素-API"层级建立资源树
3. 为每个资源分配 permission_key
4. 配置 API 的 uri_pattern 和 method
5. 录入系统初始化脚本

**示例**：
```
用户管理模块（user-module）
├── 用户列表菜单（user-menu）
│   ├── 用户列表页面（user-list-page）
│   │   ├── 新增按钮（user-create-btn）→ permission_key: user:create
│   │   ├── 编辑按钮（user-edit-btn）→ permission_key: user:update
│   │   ├── 删除按钮（user-delete-btn）→ permission_key: user:delete
│   │   ├── 导出按钮（user-export-btn）→ permission_key: user:export
│   │   └── API：GET /api/users → permission_key: user:view
│   │   └── API：POST /api/users → permission_key: user:create
│   │   └── API：PUT /api/users/{id} → permission_key: user:update
│   │   └── API：DELETE /api/users/{id} → permission_key: user:delete
│   │   └── API：GET /api/users/export → permission_key: user:export
```

### 10.2 新功能开发流程

**强制流程**：
```
需求分析 → 权限设计 → 权限数据录入 → 功能开发 → 权限校验测试
```

**权限设计模板**：

| 资源名称 | 资源编码 | 类型 | 父资源 | permission_key | uri_pattern | method |
|---------|---------|------|--------|----------------|-------------|--------|
| 订单管理 | order-module | MODULE | - | - | - | - |
| 订单列表 | order-menu | MENU | order-module | order:view | - | - |
| 新增订单 | order-create-btn | ELEMENT | order-menu | order:create | - | - |
| 创建订单API | - | API | order-create-btn | order:create | POST /api/orders | POST |

---

## 11. 操作流程

### 11.1 创建自定义角色

```
1. 管理员进入角色管理页面
2. 点击"新建角色"
3. 填写角色名称、编码、描述
4. 系统自动分配基本权限集合
5. 管理员选择缺省权限
6. 保存角色
7. 记录操作审计日志
```

### 11.2 调整角色权限

```
1. 管理员进入角色详情页
2. 查看当前权限树（标记：基本/缺省/调整）
3. 勾选/取消勾选权限节点
4. 系统判断：
   - 添加权限 → 写入 role_permission_adjustment (action=ADD)
   - 移除权限 → 写入 role_permission_adjustment (action=REMOVE)
   - 基本权限不可移除
5. 清除相关用户权限缓存
6. 记录操作审计日志
```

### 11.3 调整用户权限

```
1. 管理员进入用户详情页
2. 查看用户当前权限（来源：角色继承 + 个人调整）
3. 额外授权 → 写入 user_permission_adjustment (action=ADD)
4. 移除权限 → 写入 user_permission_adjustment (action=REMOVE)
5. 可设置临时权限（过期时间）
6. 清除用户权限缓存
7. 记录操作审计日志
```

---

## 附录

### A. 初始化数据脚本

```sql
-- 基本权限资源
INSERT INTO resource (name, code, type, permission_key, is_basic) VALUES
('登录', 'login', 'PERMISSION', 'system:login', 1),
('退出', 'logout', 'PERMISSION', 'system:logout', 1),
('查看个人信息', 'profile:view', 'PERMISSION', 'profile:view', 1),
('修改个人密码', 'profile:password', 'PERMISSION', 'profile:password', 1);

-- 系统角色
INSERT INTO role (name, code, type) VALUES
('超级管理员', 'admin', 'SYSTEM'),
('普通用户', 'user', 'DEFAULT');

-- admin 角色拥有所有权限（通过程序动态管理）
-- user 角色拥有基本权限
INSERT INTO role_permission (role_id, resource_id, permission_type, created_by)
SELECT 2, id, 'basic', 1 FROM resource WHERE is_basic = 1;
```

### B. 权限管理 API 设计

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/roles` | GET | 获取角色列表 |
| `/api/roles` | POST | 创建角色 |
| `/api/roles/{id}` | PUT | 更新角色 |
| `/api/roles/{id}` | DELETE | 删除角色 |
| `/api/roles/{id}/permissions` | GET | 获取角色权限树 |
| `/api/roles/{id}/permissions` | PUT | 更新角色权限 |
| `/api/users/{id}/permissions` | GET | 获取用户权限树 |
| `/api/users/{id}/permissions` | PUT | 更新用户权限 |
| `/api/resources/tree` | GET | 获取资源树 |
| `/api/audit-logs` | GET | 获取权限审计日志 |

---

> 文档维护：权限管理模块开发团队  
> 最后审核：待审核