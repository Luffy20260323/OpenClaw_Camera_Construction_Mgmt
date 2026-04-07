# Camera 项目权限设计方案 V4.0

**版本**：V4.0  
**创建日期**：2026-04-04  
**提出人**：柳生  
**状态**：待审核

---

## 📋 文档修订历史

| 版本 | 日期 | 修订内容 | 修订人 |
|------|------|----------|--------|
| V1.0 | 2026-03-27 | 初始权限控制方案（47 个权限，22 个角色） | AI |
| V2.0 | 2026-03-28 | 增加权限缓存、DataScope AOP | AI |
| V3.0 | 2026-04-02 | ELEMENT 层级权限设计 | AI |
| V4.0 | 2026-04-04 | 权限驱动开发流程规范（8 步流程） | 柳生/AI |

---

## 目录

1. [核心理念](#一核心理念)
2. [权限驱动开发流程（8 步）](#二权限驱动开发流程 8 步)
3. [资源模型架构](#三资源模型架构)
4. [权限码命名规范](#四权限码命名规范)
5. [数据库表结构](#五数据库表结构)
6. [两种管理员角色设计](#六两种管理员角色设计)
7. [后端实现方案](#七后端实现方案)
8. [前端实现方案](#八前端实现方案)
9. [权限缓存机制](#九权限缓存机制)
10. [实施步骤](#十实施步骤)

---

## 一、核心理念

### 1.1 核心原则

**开发与权限配置分离**：新功能开发时先完成功能和不带父资源的资源收集，开发完成后再通过配置建立权限层级关系。

### 1.2 设计目标

| 目标 | 说明 |
|------|------|
| **开发友好** | 新功能开发不受权限配置影响，可独立进行 |
| **配置灵活** | 开发完成后，管理员可灵活配置权限层级和分配 |
| **实时生效** | 权限变更后无需退出系统，实时刷新菜单 |
| **高性能** | 权限验证响应时间 < 50ms（缓存命中） |
| **可追溯** | 所有权限配置操作记录审计日志 |

### 1.3 关键设计原则

1. **资源孤儿状态**：新功能开发阶段，资源无父资源，便于收集和管理
2. **admin 超级权限**：admin 用户始终拥有全部权限，否则是数据配置错误
3. **菜单显示逻辑**：用户是否具备菜单下关联资源的权限决定菜单是否显示
4. **权限缓存机制**：用户权限数据缓存 + 手动/自动刷新机制

---

## 二、权限驱动开发流程（8 步）

### 2.1 流程总览

```
┌─────────────────────────────────────────────────────────────────┐
│                    权限驱动开发流程（8 步）                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  开发阶段（1-4）              运行阶段（5-8）                    │
│  ┌─────┐ ┌───── ┌─────┐ ─────┐   ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐ │
│  │Step1│→│Step2│→│Step3│→│Step4│   │Step5│→│Step6│→│Step7│→│Step8│ │
│  └───── └─────┘ ─────┘ └─────┘   └─────┘ └─────┘ └─────┘ └─────┘ │
│   功能开发  权限生成  admin 授权  资源树构建   资源配置   admin 验证  权限分配  用户验证  │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 详细步骤

#### 第 1 步：开发阶段 - 功能开发与资源收集

**工作内容**：
- 完成功能开发（页面 URL、布局、元素、数据库、API）
- 实时更新资源表（增删改），资源此时**无父资源**（孤儿状态）
- 资源类型包括：API、PAGE、ELEMENT

**产出**：
- 功能代码
- 资源数据（孤儿状态，parent_id = NULL）

**示例**：
```sql
-- 开发阶段插入的资源（无父资源）
INSERT INTO resource (name, code, type, parent_id, module_code) VALUES
  ('用户列表页面', 'page_user_list', 'PAGE', NULL, 'system'),
  ('查看详情按钮', 'elem_user_view', 'ELEMENT', NULL, 'system'),
  ('新建用户按钮', 'elem_user_create', 'ELEMENT', NULL, 'system'),
  ('获取用户列表 API', 'api_user_list', 'API', NULL, 'system');
```

---

#### 第 2 步：开发阶段 - 权限代码生成

**工作内容**：
- 为 PAGE、ELEMENT、API 生成权限代码
- 权限代码按编码规则生成，保证唯一性
- 将权限代码写入权限表

**权限码格式**：
```
module:resource:action:type
```

**示例**：
```sql
-- 生成的权限码
INSERT INTO permission (permission_key, name, resource_id) VALUES
  ('system:user:list:page', '用户列表页面访问', 101),
  ('system:user:view:button', '查看用户详情按钮', 102),
  ('system:user:create:button', '新建用户按钮', 103),
  ('system:user:list:api', '获取用户列表 API', 104);
```

---

#### 第 3 步：开发阶段 - Admin 授权

**工作内容**：
- 将新生成的权限全部分配给 admin 用户
- 操作角色 - 权限关系表、用户 - 权限关系表
- 确保 admin 用户登录后可访问新功能

**两种管理员角色设计**（待确认）：

| 角色类型 | 用户 | 权限来源 | 说明 |
|---------|------|----------|------|
| **超级管理员** | admin（唯一） | 系统预置，自动拥有全部权限 | 新功能缺省授权对象 |
| **系统管理员** | 可由 admin 创建/授权 | 由 admin 分配权限 | 日常运维角色 |

**示例**：
```sql
-- 分配给超级管理员角色
INSERT INTO role_permission (role_id, permission_id, is_basic, is_effective)
SELECT 1, id, true, true FROM permission WHERE resource_id IN (
    SELECT id FROM resource WHERE module_code = 'system'
);
```

---

#### 第 4 步：开发阶段 - 资源树构建

**工作内容**：
- 生成菜单项（MENU）资源、模块（MODULE）资源
- 将这两类资源写入资源库
- 在系统运行时提供资源维护页面
- 管理员可将菜单/模块设置为第 1 步中资源的父资源

**层级规则**：
- MENU 的父资源只能是 MODULE
- MODULE 的父资源只能是 MODULE（支持嵌套模块）
- API 的父资源可以是 MODULE/MENU/PAGE/ELEMENT/API 中的任何一种

**示例**：
```sql
-- 创建模块和菜单
INSERT INTO resource (name, code, type, parent_id, module_code) VALUES
  ('系统设置', 'system', 'MODULE', NULL, 'system'),
  ('用户管理', 'menu_user_management', 'MENU', 1, 'system');

-- 设置父资源关系（将孤儿资源纳入树形结构）
UPDATE resource SET parent_id = 2 WHERE code IN ('page_user_list', 'elem_user_view', 'elem_user_create');
```

---

#### 第 5 步：运行阶段 - 资源配置

**操作人员**：admin 或被授权的**系统管理员**

**工作内容**：
- 在资源管理页面对 MODULE、MENU 进行增删改查
- 对其他资源（PAGE、ELEMENT、API）编辑其父资源和展示顺序
- 设置完成后，用户通过"刷新菜单"按钮实时按需更新菜单栏

**权限规则**：

| 资源类型 | 可修改名称 | 可修改父资源 | 父资源类型限制 |
|----------|------------|--------------|----------------|
| MODULE | ✅ | ✅（嵌套模块） | MODULE/null |
| MENU | ✅ | ✅ | MODULE |
| PAGE | ✅ | ✅ | MODULE/MENU |
| ELEMENT | ✅ | ❌ 系统维护 | PAGE |
| API | ✅ | ✅ | MODULE/MENU/PAGE/ELEMENT/API |

---

#### 第 6 步：运行阶段 - Admin 验证

**操作人员**：admin 用户

**工作内容**：
- admin 登录系统，验证是否具备全部权限
- 如果发现 admin 不能访问某项资源，则是数据配置错误
- 更新数据库相关数据修复

**验收标准**：
- admin 用户可访问所有菜单、页面、按钮、API
- 如有问题，直接修正数据库配置

---

#### 第 7 步：运行阶段 - 权限分配

**操作人员**：admin 或被授权的**系统管理员**

**工作内容**：
- 在角色基本权限配置页面配置角色权限
- 在角色缺省权限配置页面配置缺省权限
- 在角色完整权限配置页面灵活配置权限集
- 在用户权限配置页面对用户权限集进行配置

**配置页面**：

| 页面名称 | 路径 | 功能 |
|---------|------|------|
| 角色基本权限配置 | `/system/role-basic-permission` | 配置所有角色共享的基础权限 |
| 角色缺省权限配置 | `/system/role-default-permission` | 按角色类型配置缺省权限 |
| 角色完整权限配置 | `/system/role-permission` | 完整配置单个角色的权限集 |
| 用户权限配置 | `/system/user-permission` | 配置单个用户的权限（可覆盖角色） |

---

#### 第 8 步：运行阶段 - 用户验证

**操作人员**：非 admin 用户

**工作内容**：
- 非 admin 用户登录，验证是否具备分配的权限
- 如果发现不能访问某项资源，则是数据配置错误
- 更新数据库相关数据修复

**验收标准**：
- 用户可访问已授权的菜单、页面、按钮、API
- 用户不可访问未授权的资源
- 如有问题，修正权限配置

---

## 三、资源模型架构

### 3.1 资源层级结构

```
MODULE（模块）
└── MODULE（子模块，可选）
    └── MENU（菜单）→ 用户在侧边栏看到
        └── PAGE（页面）→ 菜单对应的页面
            └── ELEMENT（元素）→ 页面上的按钮/字段
                └── PAGE（子页面）→ 触发后的详情页
                    └── API（接口）→ 数据接口
                        └── API（子接口）→ API 可调用其他 API
```

**API 的特殊性**：
- API 的父资源可以是 MODULE/MENU/PAGE/ELEMENT/API 中的任何一种
- API 可以调用其他 API（形成 API 调用链）
- API 是权限的最终承载对象（所有权限检查最终落实到 API 权限）

### 3.2 资源类型定义

| 类型 | 代码 | 说明 | 父资源类型 | 示例 |
|------|------|------|------------|------|
| MODULE | `MODULE` | 功能模块 | MODULE/null | 系统设置、零部件管理 |
| MENU | `MENU` | 导航菜单 | MODULE | 用户管理、角色管理 |
| PAGE | `PAGE` | 页面 | MENU/MODULE | 用户列表、用户详情 |
| ELEMENT | `ELEMENT` | 操作元素 | PAGE | 查看详情、新建用户 |
| API | `API` | 接口权限 | MODULE/MENU/PAGE/ELEMENT/API | 获取用户列表 API |

**注**：已取消 `PERMISSION` 资源类型，避免混淆。权限通过 `permission` 表和 `role_permission` 表管理。

---

## 四、权限码命名规范

### 4.1 权限码格式

```
module:resource:action:type
```

### 4.2 组成部分

| 部分 | 说明 | 示例 |
|------|------|------|
| `module` | 模块编码 | `system`, `auth`, `user`, `part` |
| `resource` | 资源类型 | `user`, `role`, `permission`, `document`, `component` |
| `action` | 操作类型 | `list`, `view`, `create`, `edit`, `delete`, `export`, `import`, `assign` |
| `type` | 权限类型 | `page`, `button`, `menu`, `api` |

### 4.3 权限类型说明

| type | 说明 | 示例 | 访问时机 |
|------|------|------|----------|
| `page` | 页面访问权限 | `system:user:list:page` | 路由守卫检查 |
| `button` | 按钮操作权限 | `system:user:create:button` | 按钮显示控制 |
| `menu` | 菜单显示权限 | `system:user:menu` | 侧边栏菜单过滤 |
| `api` | API 接口权限 | `system:user:list:api` | 后端拦截器检查 |

### 4.4 完整示例

```
# 用户管理模块
system:user:menu                    # 用户管理菜单
system:user:list:page               # 用户列表页面
system:user:view:button             # 查看用户按钮
system:user:create:button           # 新建用户按钮
system:user:create:page             # 用户创建页面
system:user:edit:button             # 编辑用户按钮
system:user:edit:page               # 用户编辑页面
system:user:delete:button           # 删除用户按钮
system:user:export:button           # 导出用户按钮
system:user:list:api                # 获取用户列表 API

# 角色管理模块
system:role:menu                    # 角色管理菜单
system:role:list:page               # 角色列表页面
system:role:create:button           # 新建角色按钮
system:role:permission:button       # 分配权限按钮
system:role:list:api                # 获取角色列表 API

# 零部件管理模块
part:component_type:menu            # 零部件种类菜单
part:component_type:list:page       # 零部件种类列表页面
part:component_type:create:button   # 新建零部件种类按钮
part:component_type:list:api        # 获取零部件种类列表 API
```

---

## 五、数据库表结构

### 5.1 核心表

| 表名 | 用途 | 关键字段 | 说明 |
|------|------|----------|------|
| `resource` | 资源表 | id, name, code, type, parent_id, module_code | 存储所有资源（MODULE/MENU/PAGE/ELEMENT/API） |
| `permission` | 权限表 | id, permission_key, name, resource_id | 权限定义，与资源关联 |
| `permission_resource` | 权限 - 资源关联表 | permission_id, resource_id | 记录权限与资源的关联关系 |
| `role_permission` | 角色 - 权限关联表 | role_id, permission_id, is_basic, is_default, is_effective | 角色权限配置，含生效状态 |
| `user_role` | 用户 - 角色关联表 | user_id, role_id | 用户与角色的关联 |
| `user_permission_adjust` | 用户权限调整表 | user_id, permission_id, adjust_type (ADD/REMOVE) | 用户权限调整记录 |
| `user` | 用户表 | id, username, real_name, company_id | 用户信息 |
| `role` | 角色表 | id, role_name, role_code, type | 角色定义 |

### 5.2 resource 表结构

```sql
CREATE TABLE resource (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(100) NOT NULL,        -- 资源名称
    code                VARCHAR(50) NOT NULL UNIQUE,  -- 资源编码
    type                VARCHAR(20) NOT NULL,         -- MODULE/MENU/PAGE/ELEMENT/API
    parent_id           BIGINT REFERENCES resource(id), -- 父资源 ID（可为 NULL）
    module_code         VARCHAR(50) NOT NULL,         -- 所属模块编码
    sort_order          INTEGER DEFAULT 0,            -- 排序号
    is_visible          BOOLEAN DEFAULT true,         -- 是否可见（MENU 专用）
    is_system_protected BOOLEAN DEFAULT false,        -- 是否系统保护（MENU 专用）
    status              SMALLINT DEFAULT 1,           -- 1=启用，0=禁用
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**资源类型说明**：

| 类型 | 代码 | 说明 | 父资源类型 |
|------|------|------|------------|
| MODULE | `MODULE` | 功能模块 | MODULE/null |
| MENU | `MENU` | 导航菜单 | MODULE |
| PAGE | `PAGE` | 页面 | MENU/MODULE |
| ELEMENT | `ELEMENT` | 操作元素 | PAGE |
| API | `API` | 接口权限 | MODULE/MENU/PAGE/ELEMENT/API |

### 5.3 permission 表结构

```sql
CREATE TABLE permission (
    id                  BIGSERIAL PRIMARY KEY,
    permission_key      VARCHAR(200) NOT NULL UNIQUE, -- 权限码（唯一）
    name                VARCHAR(100) NOT NULL,        -- 权限名称
    resource_id         BIGINT REFERENCES resource(id), -- 关联的资源 ID
    description         VARCHAR(500),                 -- 权限描述
    status              SMALLINT DEFAULT 1,           -- 1=启用，0=禁用
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 5.4 permission_resource 表结构

```sql
CREATE TABLE permission_resource (
    id                  BIGSERIAL PRIMARY KEY,
    permission_id       BIGINT NOT NULL REFERENCES permission(id), -- 权限 ID
    resource_id         BIGINT NOT NULL REFERENCES resource(id),   -- 资源 ID
    relation_type       VARCHAR(20) DEFAULT 'direct',              -- 关联类型：direct/indirect
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (permission_id, resource_id)
);
```

**用途**：记录权限与资源的关联关系，一个权限可关联多个资源。

### 5.5 role_permission 表结构

```sql
CREATE TABLE role_permission (
    id                  BIGSERIAL PRIMARY KEY,
    role_id             BIGINT NOT NULL REFERENCES role(id),       -- 角色 ID
    permission_id       BIGINT NOT NULL REFERENCES permission(id), -- 权限 ID
    is_basic            BOOLEAN DEFAULT false,                     -- 是否基本权限（必须生效）
    is_default          BOOLEAN DEFAULT false,                     -- 是否缺省权限
    is_effective        BOOLEAN DEFAULT true,                      -- 是否生效
    adjust_reason       VARCHAR(500),                              -- 调整原因
    adjusted_by         BIGINT,                                    -- 调整人
    adjusted_at         TIMESTAMP,                                 -- 调整时间
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (role_id, permission_id)
);
```

**生效规则**：

| is_basic | is_default | is_effective | 说明 |
|----------|------------|--------------|------|
| true | - | true | 基本权限，必须生效，不可移除 |
| false | true | true | 缺省权限，生效状态 |
| false | true | false | 缺省权限但被移除 |
| false | false | true | 非缺省权限，手动添加 |
| false | false | false | 非缺省权限，手动移除 |

**角色最终权限计算**：
```sql
-- 角色的有效权限 = 所有 is_effective = true 的权限
SELECT permission_id FROM role_permission 
WHERE role_id = ? AND is_effective = true;
```

### 5.6 user_permission_adjust 表结构

```sql
CREATE TABLE user_permission_adjust (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL REFERENCES user(id),       -- 用户 ID
    permission_id       BIGINT NOT NULL REFERENCES permission(id), -- 权限 ID
    adjust_type         VARCHAR(10) NOT NULL,                      -- ADD/REMOVE
    reason              VARCHAR(500),                              -- 调整原因
    adjusted_by         BIGINT,                                    -- 调整人
    adjusted_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, permission_id, adjust_type)
);
```

**调整规则**：
- 用户从角色继承的**基本权限**不能被减少（REMOVE 操作无效）
- 非基本权限可以灵活调整（ADD/REMOVE）

**用户最终权限计算**：
```
用户最终权限 = 
    (从所有角色继承的有效权限) 
    + (user_permission_adjust 中 ADD 的权限) 
    - (user_permission_adjust 中 REMOVE 的权限，但排除基本权限)
```

### 5.7 索引设计

```sql
-- resource 表索引
CREATE INDEX idx_resource_type ON resource(type);
CREATE INDEX idx_resource_parent_id ON resource(parent_id);
CREATE INDEX idx_resource_module_code ON resource(module_code);

-- permission 表索引
CREATE UNIQUE INDEX idx_permission_key ON permission(permission_key);
CREATE INDEX idx_permission_resource ON permission(resource_id);

-- role_permission 表索引
CREATE INDEX idx_role_permission_role ON role_permission(role_id);
CREATE INDEX idx_role_permission_effective ON role_permission(role_id, is_effective);

-- user_permission_adjust 表索引
CREATE INDEX idx_user_adjust_user ON user_permission_adjust(user_id);
CREATE INDEX idx_user_adjust_type ON user_permission_adjust(user_id, adjust_type);
```

---

## 六、两种管理员角色设计

### 6.1 角色定义

| 角色 | 代码 | 用户 | 权限来源 | 说明 |
|------|------|------|----------|------|
| **超级管理员** | `ROLE_SUPER_ADMIN` | admin（唯一） | 系统预置，自动拥有全部权限 | 新功能缺省授权对象，不受权限限制 |
| **系统管理员** | `ROLE_SYSTEM_ADMIN` | 可由 admin 创建多个 | 由 admin 分配权限 | 日常运维角色，权限取决于 admin 配置 |

### 6.2 权限分配策略

```
┌─────────────────────────────────────────────────────────────────┐
│                    权限分配策略                                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  新功能开发完成                                                  │
│     │                                                           │
│     ▼                                                           │
│  自动分配给 ROLE_SUPER_ADMIN (admin 用户)                        │
│     │                                                           │
│     ▼                                                           │
│  admin 验证功能可访问                                            │
│     │                                                           │
│     ▼                                                           │
│  admin 决定是否分配给 ROLE_SYSTEM_ADMIN                          │
│     │                                                           │
│     ▼                                                           │
│  配置角色缺省权限（按角色类型）                                  │
│     │                                                           │
│     ▼                                                           │
│  分配给具体用户                                                  │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 6.3 数据库配置

```sql
-- 创建超级管理员角色（仅 admin 用户）
INSERT INTO role (role_name, role_code, type, company_id)
VALUES ('超级管理员', 'ROLE_SUPER_ADMIN', 'SYSTEM', NULL);

-- 创建系统管理员角色（可由 admin 分配）
INSERT INTO role (role_name, role_code, type, company_id)
VALUES ('系统管理员', 'ROLE_SYSTEM_ADMIN', 'SYSTEM', NULL);

-- admin 用户关联超级管理员角色
INSERT INTO user_role (user_id, role_id)
VALUES (1, (SELECT id FROM role WHERE role_code = 'ROLE_SUPER_ADMIN'));
```

---

## 七、后端实现方案

### 7.1 权限注解

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasPermission(#permission)")
public @interface ApiPermission {
    String value(); // 权限代码，如 "system:user:list:api"
}
```

### 7.2 Controller 示例

```java
@RestController
@RequestMapping("/api/user")
@ApiPermission("system:user:list:api")
public class UserController {
    
    @GetMapping("/list")
    @ApiPermission("system:user:list:api")
    public Result<Page<UserDTO>> queryUsers(
            @ModelAttribute UserQueryRequest query,
            @RequestAttribute("userId") Long operatorId) {
        Page<UserDTO> page = userService.queryUsers(query, operatorId);
        return Result.success(page);
    }
    
    @PostMapping
    @ApiPermission("system:user:create:api")
    public Result<UserDTO> createUser(
            @RequestAttribute("userId") Long operatorId,
            @Valid @RequestBody CreateUserRequest request) {
        UserDTO user = userService.createUser(request, operatorId);
        return Result.success(user);
    }
}
```

### 7.3 权限拦截器

```java
@Component
public class PermissionInterceptor implements HandlerInterceptor {
    
    @Autowired
    private PermissionService permissionService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                            HttpServletResponse response, 
                            Object handler) {
        UserContext user = SecurityContextHolder.getCurrentUser();
        
        // admin 用户跳过权限检查
        if (user.isSuperAdmin()) {
            return true;
        }
        
        String apiPath = request.getRequestURI();
        String method = request.getMethod();
        
        boolean hasPermission = permissionService.hasApiPermission(
            user, apiPath, method);
        
        if (!hasPermission) {
            response.setStatus(403);
            response.getWriter().write("无权限访问");
            return false;
        }
        
        return true;
    }
}
```

### 7.4 资源服务

```java
@Service
public class ResourceService {
    
    /**
     * 获取资源树形结构
     */
    public List<ResourceDTO> getResourceTree() {
        List<Resource> allResources = resourceMapper.selectAll();
        return buildTree(allResources, null);
    }
    
    /**
     * 获取孤儿资源（无父资源）
     */
    public List<ResourceDTO> getOrphanedResources() {
        List<Resource> resources = resourceMapper.selectOrphaned();
        return ResourceConverter.toDTOList(resources);
    }
    
    /**
     * 批量设置父资源
     */
    @Transactional
    public void batchSetParent(List<Long> resourceIds, Long parentId) {
        for (Long id : resourceIds) {
            resourceMapper.updateParent(id, parentId);
        }
        // 清除权限缓存
        permissionCacheService.evictAll();
    }
}
```

---

## 八、前端实现方案

### 8.1 权限指令

```javascript
// directives/permission.js
export default {
  mounted(el, binding) {
    const { value } = binding;
    const userStore = useUserStore();
    const permissions = userStore.permissions || [];
    
    if (value && !permissions.includes(value)) {
      el.parentNode && el.parentNode.removeChild(el);
    }
  }
};

// 使用
// <el-button v-permission="'system:user:create:button'">创建用户</el-button>
```

### 8.2 路由守卫

```javascript
router.beforeEach((to, from, next) => {
  const userStore = useUserStore();
  const menus = userStore.menus || [];
  const menuCode = to.meta.menuCode;
  
  // admin 用户跳过权限检查
  if (userStore.userInfo.isSuperAdmin) {
    next();
    return;
  }
  
  // 个人中心始终可访问
  if (menuCode === 'profile') {
    next();
    return;
  }
  
  // 检查菜单权限
  const hasPermission = menus.some(m => m.menuCode === menuCode);
  
  if (!hasPermission) {
    next({ name: 'Home' });
    return;
  }
  
  next();
});
```

### 8.3 刷新菜单功能

```javascript
// stores/user.js
export const useUserStore = defineStore('user', () => {
  const userInfo = ref({});
  const permissions = ref([]);
  const menus = ref([]);
  
  /**
   * 刷新菜单（权限变更后调用）
   */
  async function refreshMenus() {
    const res = await getMyMenus();
    const newMenus = res.data || [];
    userInfo.value = { ...userInfo.value, menus: newMenus };
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value));
  }
  
  /**
   * 刷新权限
   */
  async function refreshPermissions() {
    const res = await getMyPermissions();
    permissions.value = res.data || [];
  }
  
  return { userInfo, permissions, menus, refreshMenus, refreshPermissions };
});
```

### 8.4 资源管理页面

```vue
<!-- views/system/ResourceList.vue -->
<template>
  <div class="resource-list">
    <!-- 筛选孤儿资源 -->
    <el-checkbox v-model="showOrphaned">显示孤儿资源</el-checkbox>
    
    <!-- 资源树形表格 -->
    <el-table :data="filteredResources" row-key="id" :tree-props="{ children: 'children' }">
      <el-table-column prop="name" label="资源名称" />
      <el-table-column prop="type" label="类型" />
      <el-table-column prop="permissionKey" label="权限码" />
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button @click="handleEdit(row)">编辑</el-button>
          <el-button @click="handleSetParent(row)">设置父资源</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 刷新菜单按钮 -->
    <el-button type="primary" @click="handleRefreshMenu">刷新菜单</el-button>
  </div>
</template>

<script setup>
import { useUserStore } from '@/stores/user';

const userStore = useUserStore();

const handleRefreshMenu = async () => {
  await userStore.refreshMenus();
  ElMessage.success('侧边栏菜单已刷新');
};
</script>
```

---

## 九、权限缓存机制

### 9.1 缓存结构

```
# 用户权限缓存
user:permission:{userId} → Set<Long> resourceIds, TTL: 30 minutes

# 角色权限缓存
role:permission:{roleId} → Set<Long> resourceIds, TTL: 60 minutes

# 用户菜单缓存
user:menus:{userId} → Array<menu>, TTL: 30 minutes
```

### 9.2 缓存策略

| 操作 | 缓存处理 |
|------|----------|
| 用户登录 | 写入用户权限缓存 + 菜单缓存 |
| 权限查询 | 先查缓存，未命中则查数据库 |
| 角色权限变更 | 清除角色缓存 + 该角色下所有用户缓存 |
| 用户权限变更 | 清除该用户缓存 |
| 资源结构变更 | 清除所有用户菜单缓存 |
| 手动刷新 | 强制重新加载并更新缓存 |

### 9.3 缓存服务

```java
@Service
public class PermissionCacheService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 获取用户权限缓存
     */
    public Set<Long> getUserPermissions(Long userId) {
        String key = "user:permission:" + userId;
        Set<Long> cached = (Set<Long>) redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return cached;
        }
        
        // 未命中，从数据库加载
        Set<Long> permissions = permissionService.loadUserPermissions(userId);
        redisTemplate.opsForValue().set(key, permissions, 30, TimeUnit.MINUTES);
        return permissions;
    }
    
    /**
     * 清除用户权限缓存
     */
    public void evictUserPermission(Long userId) {
        String key = "user:permission:" + userId;
        redisTemplate.delete(key);
    }
    
    /**
     * 清除角色权限缓存及相关用户缓存
     */
    public void evictRolePermission(Long roleId) {
        String roleKey = "role:permission:" + roleId;
        redisTemplate.delete(roleKey);
        
        // 清除该角色下所有用户的缓存
        List<Long> userIds = userService.getUserIdsByRole(roleId);
        for (Long userId : userIds) {
            evictUserPermission(userId);
        }
    }
    
    /**
     * 清除所有用户菜单缓存（资源结构变更时）
     */
    public void evictAllMenuCache() {
        Set<String> keys = redisTemplate.keys("user:menus:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
```

---

## 十、实施步骤

### 10.1 Phase 1：数据库准备（1 天）

**工作内容**：
- [ ] 创建 `role_type_default_permissions` 表（角色类型缺省权限）
- [ ] 创建 `ROLE_SUPER_ADMIN` 和 `ROLE_SYSTEM_ADMIN` 角色
- [ ] 将 admin 用户关联到 `ROLE_SUPER_ADMIN`
- [ ] 执行资源数据结构补齐脚本

**验收标准**：
- 数据库包含两种管理员角色
- admin 用户拥有超级管理员角色
- 资源表包含完整的 MODULE/MENU/PAGE/ELEMENT/API 数据

---

### 10.2 Phase 2：后端服务开发（2 天）

**工作内容**：
- [ ] 开发资源扫描器（自动从代码提取 API、页面路由）
- [ ] 开发权限码生成器（自动生成唯一权限码）
- [ ] 开发孤儿资源管理 API
- [ ] 开发批量设置父资源 API
- [ ] 完善权限缓存服务
- [ ] 添加"刷新菜单"API

**API 清单**：
| 方法 | 端点 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/resource/orphaned` | 获取孤儿资源 | `system:resource:view:api` |
| PUT | `/api/resource/batch-parent` | 批量设置父资源 | `system:resource:edit:api` |
| POST | `/api/resource/scan` | 扫描代码生成资源 | `system:resource:create:api` |
| POST | `/api/permission/generate` | 生成权限码 | `system:permission:create:api` |
| POST | `/api/menu/refresh` | 刷新菜单缓存 | `system:menu:refresh:api` |

---

### 10.3 Phase 3：前端页面开发（2 天）

**工作内容**：
- [ ] 资源管理页面添加"孤儿资源"筛选视图
- [ ] 添加批量设置父资源功能（拖拽或批量选择）
- [ ] 在全局导航栏添加"刷新菜单"按钮
- [ ] 完善权限配置页面（角色基本权限、缺省权限、完整权限）
- [ ] 添加权限码自动生成 UI

---

### 10.4 Phase 4：权限缓存优化（1 天）

**工作内容**：
- [ ] 实现 Redis 权限缓存（TTL: 30 分钟）
- [ ] 实现权限变更时自动失效缓存
- [ ] 实现手动刷新按钮（强制更新缓存）
- [ ] 性能测试（目标：< 50ms）

---

### 10.5 Phase 5：测试验证（1 天）

**测试场景**：

| 场景 | 预期结果 | 验证方法 |
|------|----------|----------|
| 新功能开发 | 资源无父资源，admin 可访问 | 创建测试功能，验证 admin 访问 |
| 权限码生成 | 权限码唯一，符合规范 | 检查生成的权限码 |
| 父资源设置 | 可批量设置，菜单实时更新 | 设置父资源后刷新菜单 |
| 权限分配 | 角色/用户权限正确生效 | 分配权限后验证用户访问 |
| 缓存性能 | 响应时间 < 50ms | 压测验证 |

---

### 10.6 总计划

| 阶段 | 工作内容 | 预计时间 | 状态 |
|------|----------|----------|------|
| Phase 1 | 数据库准备 | 1 天 | 待开始 |
| Phase 2 | 后端服务开发 | 2 天 | 待开始 |
| Phase 3 | 前端页面开发 | 2 天 | 待开始 |
| Phase 4 | 权限缓存优化 | 1 天 | 待开始 |
| Phase 5 | 测试验证 | 1 天 | 待开始 |
| **合计** | | **7 天** | |

---

## 附录

### A. 相关文件位置

| 文件类型 | 路径 |
|----------|------|
| 后端代码 | `/root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/backend/src/main/java/` |
| 前端代码 | `/root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/frontend/src/` |
| 数据库迁移 | `/root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/backend/src/main/resources/db/migration/` |
| 文档 | `/root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/backend/docs/` |

### B. 旧权限文档清理清单

以下文档将在新方案确认后删除，避免歧义：

- [ ] `权限控制方案.md` / `.html`
- [ ] `ELEMENT_PERMISSION_DESIGN.md`
- [ ] `PERMISSION_DYNAMIC_MAINTENANCE.md`
- [ ] `权限实现总结.md`
- [ ] `权限实现进度.md`
- [ ] `权限执行说明.md`
- [ ] `权限系统优化实施报告.md`
- [ ] `权限系统实施总结.md`
- [ ] `权限系统实施报告.md`
- [ ] `权限系统设计完整方案.html`
- [ ] `权限部署完成报告.md`
- [ ] `API_PERMISSION_INTERCEPTOR.md`

### C. 术语表

| 术语 | 说明 |
|------|------|
| **孤儿资源** | 无父资源的资源（parent_id = NULL），开发阶段产生 |
| **超级管理员** | 系统预置的最高权限角色，仅 admin 用户 |
| **系统管理员** | 可由 admin 创建和授权的运维角色 |
| **权限码** | 唯一标识权限的字符串，格式：`module:resource:action:type` |
| **刷新菜单** | 手动触发菜单缓存更新，无需退出系统 |
| **基本权限** | 角色必须拥有的权限，不可移除（`is_basic = true`） |
| **缺省权限** | 角色创建时自动分配的权限（`is_default = true`） |
| **生效权限** | 当前有效的权限（`is_effective = true`） |

---

**文档版本**：V4.0  
**创建日期**：2026-04-04  
**状态**：待柳生审核  
**下次更新**：根据审核意见修订
