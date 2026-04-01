# 权限体系测试用例

> 文档版本：v1.0  
> 最后更新：2026-04-01  
> 状态：待执行  
> 关联文档：[permission-test-spec.md](./permission-test-spec.md)

---

## 目录

1. [资源管理测试用例](#1-资源管理测试用例)
2. [角色权限计算测试用例](#2-角色权限计算测试用例)
3. [用户权限计算测试用例](#3-用户权限计算测试用例)
4. [权限调整测试用例](#4-权限调整测试用例)
5. [API 权限校验测试用例](#5-API 权限校验测试用例)
6. [审计日志测试用例](#6-审计日志测试用例)
7. [缓存测试用例](#7-缓存测试用例)

---

## 1. 资源管理测试用例

### 1.1 资源创建

#### TC-01-001: 创建模块资源

**前置条件**：
- 用户已登录且具备资源管理权限
- 系统中不存在相同 code 的资源

**测试步骤**：
1. 调用 POST /api/resources 接口
2. 请求体包含：name="用户管理", code="user-module", type="MODULE", parentId=null
3. 提交创建请求

**预期结果**：
- 返回成功状态码 200
- 返回创建的资源对象，包含自动生成的 id
- 数据库中 resource 表新增一条记录
- resource.code 唯一性约束生效

**优先级**：P1

---

#### TC-01-002: 创建菜单资源（带父节点）

**前置条件**：
- 已存在模块资源（id=1）
- 用户具备资源管理权限

**测试步骤**：
1. 调用 POST /api/resources 接口
2. 请求体包含：name="用户列表", code="user-menu", type="MENU", parentId=1
3. 提交创建请求

**预期结果**：
- 返回成功状态码 200
- 返回的资源 parentId=1
- 资源树中该节点为模块资源的子节点

**优先级**：P1

---

#### TC-01-003: 创建 API 资源（带 URI 和 Method）

**前置条件**：
- 已存在菜单资源（id=2）
- 用户具备资源管理权限

**测试步骤**：
1. 调用 POST /api/resources 接口
2. 请求体包含：
   - name="创建用户 API"
   - code="user-create-api"
   - type="API"
   - parentId=2
   - permissionKey="user:create"
   - uriPattern="/api/users"
   - method="POST"
3. 提交创建请求

**预期结果**：
- 返回成功状态码 200
- uriPattern 和 method 正确保存
- permission_key 唯一性约束生效

**优先级**：P1

---

#### TC-01-004: 创建基本权限资源

**前置条件**：
- 用户具备资源管理权限（admin）

**测试步骤**：
1. 调用 POST /api/resources 接口
2. 请求体包含：name="登录", code="login", type="PERMISSION", permissionKey="system:login", isBasic=1
3. 提交创建请求

**预期结果**：
- 返回成功状态码 200
- is_basic=1 正确保存
- 该资源被标记为基本权限

**优先级**：P1

---

#### TC-01-005: 创建重复 code 的资源（失败场景）

**前置条件**：
- 已存在 code="user-module" 的资源

**测试步骤**：
1. 调用 POST /api/resources 接口
2. 请求体包含：code="user-module"（与已有资源重复）
3. 提交创建请求

**预期结果**：
- 返回错误状态码
- 错误信息提示"资源编码已存在"
- 数据库中无新记录

**优先级**：P1

---

#### TC-01-006: 创建重复 permission_key 的资源（失败场景）

**前置条件**：
- 已存在 permission_key="user:create" 的资源

**测试步骤**：
1. 调用 POST /api/resources 接口
2. 请求体包含：permissionKey="user:create"（与已有资源重复）
3. 提交创建请求

**预期结果**：
- 返回错误状态码（数据库唯一约束失败）
- 错误信息提示权限标识已存在
- 数据库中无新记录

**优先级**：P1

---

### 1.2 资源查询

#### TC-01-007: 获取资源树

**前置条件**：
- 系统中存在完整的资源树数据

**测试步骤**：
1. 调用 GET /api/resources/tree 接口
2. 无需认证或具备基础权限

**预期结果**：
- 返回成功状态码 200
- 返回树形结构数据
- 根节点 parentId=null
- 子节点正确关联到父节点
- 包含所有启用的资源

**优先级**：P1

---

#### TC-01-008: 获取菜单树（过滤非菜单资源）

**前置条件**：
- 系统中存在 MODULE 和 MENU 类型资源

**测试步骤**：
1. 调用 GET /api/resources/menu-tree 接口

**预期结果**：
- 返回成功状态码 200
- 只包含 MODULE 和 MENU 类型资源
- 不包含 PAGE、ELEMENT、API、PERMISSION 类型
- 树形结构正确

**优先级**：P1

---

#### TC-01-009: 获取基本权限资源

**前置条件**：
- 系统中存在 is_basic=1 的资源

**测试步骤**：
1. 调用 GET /api/resources/basic 接口

**预期结果**：
- 返回成功状态码 200
- 只返回 is_basic=1 的资源
- 包含登录、退出、查看个人信息、修改密码等基本权限

**优先级**：P1

---

#### TC-01-010: 获取单个资源详情

**前置条件**：
- 存在 id=1 的资源

**测试步骤**：
1. 调用 GET /api/resources/1 接口

**预期结果**：
- 返回成功状态码 200
- 返回完整的资源对象
- 所有字段值正确

**优先级**：P2

---

#### TC-01-011: 获取不存在的资源（失败场景）

**前置条件**：
- 系统中不存在 id=99999 的资源

**测试步骤**：
1. 调用 GET /api/resources/99999 接口

**预期结果**：
- 返回错误状态码
- 错误信息提示"资源不存在"

**优先级**：P2

---

### 1.3 资源更新

#### TC-01-012: 更新资源信息

**前置条件**：
- 存在 id=2 的资源
- 用户具备资源管理权限

**测试步骤**：
1. 调用 PUT /api/resources/2 接口
2. 请求体包含更新的 name、sortOrder 等字段
3. 提交更新请求

**预期结果**：
- 返回成功状态码 200
- 资源信息已更新
- updated_at 时间戳更新

**优先级**：P1

---

#### TC-01-013: 更新基本权限资源的 is_basic 字段

**前置条件**：
- 存在 is_basic=1 的资源
- 用户具备资源管理权限

**测试步骤**：
1. 调用 PUT /api/resources/{id} 接口
2. 请求体尝试修改 is_basic=0

**预期结果**：
- 根据业务规则决定是否允许
- 如允许：更新成功
- 如不允许：返回错误提示

**优先级**：P2

---

### 1.4 资源删除

#### TC-01-014: 删除普通资源

**前置条件**：
- 存在 id=10 的普通资源（is_basic=0）
- 用户具备资源管理权限

**测试步骤**：
1. 调用 DELETE /api/resources/10 接口

**预期结果**：
- 返回成功状态码 200
- 数据库中该资源记录被删除
- 子资源级联删除（如实现）

**优先级**：P1

---

#### TC-01-015: 删除基本权限资源（失败场景）

**前置条件**：
- 存在 is_basic=1 的资源（如 id=1 的登录资源）
- 用户具备资源管理权限

**测试步骤**：
1. 调用 DELETE /api/resources/1 接口

**预期结果**：
- 返回错误状态码
- 错误信息提示"基本权限资源不可删除"
- 数据库中记录未被删除

**优先级**：P1

---

#### TC-01-016: 删除不存在的资源（失败场景）

**前置条件**：
- 系统中不存在 id=99999 的资源

**测试步骤**：
1. 调用 DELETE /api/resources/99999 接口

**预期结果**：
- 返回错误状态码
- 错误信息提示"资源不存在"

**优先级**：P2

---

## 2. 角色权限计算测试用例

### 2.1 基本权限计算

#### TC-02-001: 新建角色自动分配基本权限

**前置条件**：
- 系统中存在基本权限资源（is_basic=1）
- 用户具备角色管理权限

**测试步骤**：
1. 创建新角色（code="test_role_new"）
2. 查询该角色的权限列表
3. 检查是否包含所有基本权限

**预期结果**：
- 角色创建成功
- 角色自动拥有所有基本权限
- role_permission 表中存在 basic 类型的记录

**优先级**：P1

---

#### TC-02-002: 角色基本权限始终存在

**前置条件**：
- 角色已分配基本权限
- 角色存在 REMOVE 调整记录尝试移除基本权限

**测试步骤**：
1. 调用 PermissionService.calculateRolePermissions(roleId)
2. 检查返回的权限集合

**预期结果**：
- 基本权限始终在权限集合中
- 即使有 REMOVE 调整记录，基本权限也不被移除

**优先级**：P1

---

#### TC-02-003: 基本权限不可通过调整移除

**前置条件**：
- 角色已分配基本权限
- 用户具备角色权限管理权限

**测试步骤**：
1. 调用 POST /api/roles/{roleId}/permissions/adjust 接口
2. 请求体包含：resourceId=基本权限资源 ID, action="REMOVE"
3. 提交调整请求

**预期结果**：
- 返回错误状态码
- 错误信息提示"基本权限不可调整"
- role_permission_adjustment 表中无新记录

**优先级**：P1

---

### 2.2 缺省权限计算

#### TC-02-004: 角色缺省权限正确计算

**前置条件**：
- 角色已分配缺省权限（permission_type='default'）
- 角色无权限调整记录

**测试步骤**：
1. 调用 PermissionService.calculateRolePermissions(roleId)
2. 检查返回的权限集合

**预期结果**：
- 返回的权限集合包含所有缺省权限
- 返回的权限集合包含所有基本权限
- 权限数量 = 基本权限数 + 缺省权限数

**优先级**：P1

---

#### TC-02-005: 角色缺省权限可通过调整移除

**前置条件**：
- 角色已分配缺省权限资源（resourceId=10）
- 用户具备角色权限管理权限

**测试步骤**：
1. 调用 POST /api/roles/{roleId}/permissions/adjust 接口
2. 请求体包含：resourceId=10, action="REMOVE"
3. 调用 calculateRolePermissions(roleId) 计算权限

**预期结果**：
- 调整记录创建成功（action=REMOVE）
- 计算结果中不包含 resourceId=10
- 基本权限仍然保留

**优先级**：P1

---

### 2.3 权限调整计算

#### TC-02-006: 角色权限 ADD 调整生效

**前置条件**：
- 角色未拥有资源 resourceId=20
- 用户具备角色权限管理权限

**测试步骤**：
1. 调用 POST /api/roles/{roleId}/permissions/adjust 接口
2. 请求体包含：resourceId=20, action="ADD"
3. 调用 calculateRolePermissions(roleId) 计算权限

**预期结果**：
- 调整记录创建成功（action=ADD）
- 计算结果中包含 resourceId=20

**优先级**：P1

---

#### TC-02-007: 角色权限 REMOVE 调整生效

**前置条件**：
- 角色已拥有资源 resourceId=11（缺省权限）
- 用户具备角色权限管理权限

**测试步骤**：
1. 调用 POST /api/roles/{roleId}/permissions/adjust 接口
2. 请求体包含：resourceId=11, action="REMOVE"
3. 调用 calculateRolePermissions(roleId) 计算权限

**预期结果**：
- 调整记录创建成功（action=REMOVE）
- 计算结果中不包含 resourceId=11

**优先级**：P1

---

#### TC-02-008: 角色权限 ADD 后再 REMOVE（抵消）

**前置条件**：
- 角色未拥有资源 resourceId=25
- 用户具备角色权限管理权限

**测试步骤**：
1. 调用 adjust 接口，resourceId=25, action="ADD"
2. 再次调用 adjust 接口，resourceId=25, action="REMOVE"
3. 调用 calculateRolePermissions(roleId) 计算权限

**预期结果**：
- 调整记录存在（action=REMOVE，或记录被删除恢复原状态）
- 计算结果中不包含 resourceId=25（因为原本没有）

**优先级**：P1

---

#### TC-02-009: 角色权限 REMOVE 后再 ADD（抵消）

**前置条件**：
- 角色已拥有资源 resourceId=12（缺省权限）
- 用户具备角色权限管理权限

**测试步骤**：
1. 调用 adjust 接口，resourceId=12, action="REMOVE"
2. 再次调用 adjust 接口，resourceId=12, action="ADD"
3. 调用 calculateRolePermissions(roleId) 计算权限

**预期结果**：
- 调整记录存在（action=ADD，或记录被删除恢复原状态）
- 计算结果中包含 resourceId=12（恢复拥有）

**优先级**：P1

---

#### TC-02-010: 重复 ADD 同一资源

**前置条件**：
- 角色未拥有资源 resourceId=30

**测试步骤**：
1. 调用 adjust 接口，resourceId=30, action="ADD"
2. 再次调用 adjust 接口，resourceId=30, action="ADD"
3. 检查调整记录和计算结果

**预期结果**：
- 调整记录只有一条（或更新现有记录）
- 计算结果中包含 resourceId=30
- 无重复记录

**优先级**：P2

---

### 2.4 权限树查询

#### TC-02-011: 获取角色权限树（标记状态）

**前置条件**：
- 角色已配置基本权限、缺省权限和调整记录
- 系统中存在完整的资源树

**测试步骤**：
1. 调用 GET /api/roles/{roleId}/permissions/tree 接口
2. 检查返回的权限树

**预期结果**：
- 返回树形结构
- 每个节点包含 status 字段
- status 值正确标记：basic/default/added/removed/none

**优先级**：P1

---

#### TC-02-012: 权限树状态标记验证

**前置条件**：
- 角色配置如下：
  - 基本权限：resourceId=1,2,3,4
  - 缺省权限：resourceId=10,11,12
  - ADD 调整：resourceId=20
  - REMOVE 调整：resourceId=11

**测试步骤**：
1. 调用 GET /api/roles/{roleId}/permissions/tree 接口
2. 检查各节点 status 字段

**预期结果**：
- resourceId=1,2,3,4 的 status="basic"
- resourceId=10,12 的 status="default"
- resourceId=11 的 status="removed"
- resourceId=20 的 status="added"
- 其他资源 status="none"

**优先级**：P1

---

## 3. 用户权限计算测试用例

### 3.1 单角色用户权限

#### TC-03-001: 单角色用户权限计算

**前置条件**：
- 用户 user1 只拥有 test_role_a
- test_role_a 配置：
  - 基本权限：1,2,3,4
  - 缺省权限：10,11,12
  - ADD 调整：20
  - REMOVE 调整：11

**测试步骤**：
1. 调用 PermissionService.calculateUserPermissions(user1Id)
2. 检查返回的权限集合

**预期结果**：
- 返回权限集合：{1,2,3,4,10,12,20}
- 包含所有基本权限
- 包含缺省权限（10,12）
- 包含 ADD 调整的权限（20）
- 不包含 REMOVE 调整的权限（11）

**优先级**：P1

---

#### TC-03-002: 单角色用户获取权限资源列表

**前置条件**：
- 用户 user1 已计算权限

**测试步骤**：
1. 调用 PermissionService.getUserPermissionResources(user1Id)
2. 检查返回的资源列表

**预期结果**：
- 返回 Resource 对象列表
- 列表数量与 calculateUserPermissions 结果一致
- 每个 Resource 对象包含完整信息

**优先级**：P1

---

#### TC-03-003: 单角色用户获取 permission_key 列表

**前置条件**：
- 用户 user1 已计算权限
- 资源已配置 permission_key

**测试步骤**：
1. 调用 PermissionService.getUserPermissionKeys(user1Id)
2. 检查返回的 permission_key 集合

**预期结果**：
- 返回 permission_key 字符串集合
- 只包含有 permission_key 的资源
- 无 null 值

**优先级**：P1

---

### 3.2 多角色用户权限

#### TC-03-004: 多角色用户权限合并（并集）

**前置条件**：
- 用户 user2 拥有 test_role_a 和 test_role_b
- test_role_a 权限：{1,2,3,4,10,12,20}（已计算调整）
- test_role_b 权限：{1,2,3,4,13,14,15}（已计算调整）

**测试步骤**：
1. 调用 PermissionService.calculateUserPermissions(user2Id)
2. 检查返回的权限集合

**预期结果**：
- 返回权限集合：{1,2,3,4,10,12,13,14,15,20}
- 基本权限不重复
- 两个角色的权限正确合并（并集）

**优先级**：P1

---

#### TC-03-005: 多角色权限冲突处理

**前置条件**：
- 用户 user2 拥有 test_role_a 和 test_role_b
- test_role_a 对 resourceId=50 有 REMOVE 调整
- test_role_b 对 resourceId=50 有 ADD 调整（或缺省拥有）

**测试步骤**：
1. 调用 PermissionService.calculateUserPermissions(user2Id)
2. 检查 resourceId=50 是否在权限集合中

**预期结果**：
- 根据业务规则：
  - 如任一角色拥有则拥有（宽松策略）
  - 或需要所有角色都拥有（严格策略）
- 行为符合设计文档定义

**优先级**：P2

---

### 3.3 用户个人权限调整

#### TC-03-006: 用户个人 ADD 权限

**前置条件**：
- 用户 user2 不拥有 resourceId=30
- 用户具备权限管理权限

**测试步骤**：
1. 调用用户权限调整接口（待实现）
2. 请求体：userId=user2Id, resourceId=30, action="ADD"
3. 调用 calculateUserPermissions(user2Id)

**预期结果**：
- user_permission_adjustment 表创建 ADD 记录
- 计算结果中包含 resourceId=30

**优先级**：P1

---

#### TC-03-007: 用户个人 REMOVE 权限（移除角色继承的）

**前置条件**：
- 用户 user2 从 test_role_b 继承 resourceId=13
- 用户具备权限管理权限

**测试步骤**：
1. 调用用户权限调整接口
2. 请求体：userId=user2Id, resourceId=13, action="REMOVE"
3. 调用 calculateUserPermissions(user2Id)

**预期结果**：
- user_permission_adjustment 表创建 REMOVE 记录
- 计算结果中不包含 resourceId=13
- 角色权限不受影响（其他拥有该角色的用户仍有权限）

**优先级**：P1

---

#### TC-03-008: 用户个人调整优先级验证

**前置条件**：
- 用户 user2 从角色继承 resourceId=14
- 用户个人对 resourceId=14 有 REMOVE 调整

**测试步骤**：
1. 调用 calculateUserPermissions(user2Id)
2. 检查 resourceId=14 是否在权限集合中

**预期结果**：
- 用户个人调整优先级高于角色继承
- resourceId=14 不在用户权限集合中

**优先级**：P1

---

### 3.4 临时权限

#### TC-03-009: 临时权限在有效期内生效

**前置条件**：
- 用户 user3 有临时权限调整记录
- resourceId=40, action="ADD", expireAt=未来时间

**测试步骤**：
1. 调用 calculateUserPermissions(user3Id)
2. 检查 resourceId=40 是否在权限集合中

**预期结果**：
- 临时权限在有效期内
- resourceId=40 在权限集合中

**优先级**：P1

---

#### TC-03-010: 临时权限过期后失效

**前置条件**：
- 用户 user3 有临时权限调整记录
- resourceId=40, action="ADD", expireAt=过去时间

**测试步骤**：
1. 调用 calculateUserPermissions(user3Id)
2. 检查 resourceId=40 是否在权限集合中

**预期结果**：
- 临时权限已过期
- resourceId=40 不在权限集合中
- Mapper 查询正确过滤过期记录

**优先级**：P1

---

### 3.5 用户权限判断

#### TC-03-011: 判断用户是否有指定 permission_key

**前置条件**：
- 用户 user1 拥有 resourceId=4（permission_key="user:create"）

**测试步骤**：
1. 调用 PermissionService.hasPermission(user1Id, "user:create")

**预期结果**：
- 返回 true

**优先级**：P1

---

#### TC-03-012: 判断用户是否无指定 permission_key

**前置条件**：
- 用户 user1 不拥有 resourceId=6（permission_key="user:delete"）

**测试步骤**：
1. 调用 PermissionService.hasPermission(user1Id, "user:delete")

**预期结果**：
- 返回 false

**优先级**：P1

---

#### TC-03-013: 判断用户是否有指定资源权限

**前置条件**：
- 用户 user1 拥有 resourceId=10

**测试步骤**：
1. 调用 PermissionService.hasResourcePermission(user1Id, 10L)

**预期结果**：
- 返回 true

**优先级**：P1

---

#### TC-03-014: 判断用户是否有 API 权限（URI + Method）

**前置条件**：
- 存在 API 资源：uriPattern="/api/users", method="POST", permission_key="user:create"
- 用户 user1 拥有该资源

**测试步骤**：
1. 调用 PermissionService.hasApiPermission(user1Id, "/api/users", "POST")

**预期结果**：
- 返回 true

**优先级**：P1

---

#### TC-03-015: 判断用户是否有 API 权限（未配置权限的 API）

**前置条件**：
- 不存在匹配 uriPattern="/api/test" 的资源

**测试步骤**：
1. 调用 PermissionService.hasApiPermission(user1Id, "/api/test", "GET")

**预期结果**：
- 返回 true（未配置权限的 API 默认放行）

**优先级**：P2

---

### 3.6 用户菜单树

#### TC-03-016: 获取用户菜单树（权限过滤）

**前置条件**：
- 用户 user1 拥有部分菜单权限
- 系统中存在完整菜单树

**测试步骤**：
1. 调用 GET /api/users/{userId}/menu-tree 接口
2. 检查返回的菜单树

**预期结果**：
- 返回的菜单树只包含用户有权限的节点
- 父节点即使无权限，如有子节点有权限也保留
- 树形结构正确

**优先级**：P1

---

#### TC-03-017: 用户菜单树过滤逻辑验证

**前置条件**：
- 菜单树结构：模块 A → 菜单 B → 页面 C → 按钮 D
- 用户只有按钮 D 的权限

**测试步骤**：
1. 调用 GET /api/users/{userId}/menu-tree 接口
2. 检查返回的树

**预期结果**：
- 返回的树包含：模块 A → 菜单 B → 页面 C → 按钮 D
- 即使父节点无权限，因子节点有权限而保留路径

**优先级**：P1

---

## 4. 权限调整测试用例

### 4.1 角色权限调整

#### TC-04-001: 调整角色权限 - ADD

**前置条件**：
- 角色 test_role_a 不拥有 resourceId=25
- 用户具备角色权限管理权限

**测试步骤**：
1. 调用 POST /api/roles/{roleId}/permissions/adjust
2. 请求体：{resourceId: 25, action: "ADD"}
3. 检查 role_permission_adjustment 表
4. 重新计算角色权限

**预期结果**：
- 返回成功
- 调整记录创建（action=ADD）
- 角色权限计算结果包含 resourceId=25
- 审计日志记录

**优先级**：P1

---

#### TC-04-002: 调整角色权限 - REMOVE

**前置条件**：
- 角色 test_role_a 拥有 resourceId=11（缺省权限）
- 用户具备角色权限管理权限

**测试步骤**：
1. 调用 POST /api/roles/{roleId}/permissions/adjust
2. 请求体：{resourceId: 11, action: "REMOVE"}
3. 检查 role_permission_adjustment 表
4. 重新计算角色权限

**预期结果**：
- 返回成功
- 调整记录创建（action=REMOVE）
- 角色权限计算结果不包含 resourceId=11
- 审计日志记录

**优先级**：P1

---

#### TC-04-003: 批量调整角色权限

**前置条件**：
- 角色 test_role_a 需要调整多个权限
- 用户具备角色权限管理权限

**测试步骤**：
1. 调用 POST /api/roles/{roleId}/permissions/adjust-batch
2. 请求体：[{resourceId: 25, action: "ADD"}, {resourceId: 11, action: "REMOVE"}, ...]
3. 检查调整记录
4. 重新计算角色权限

**预期结果**：
- 返回成功
- 所有调整记录创建成功
- 角色权限计算结果正确
- 审计日志记录所有调整

**优先级**：P1

---

#### TC-04-004: 调整已存在的调整记录（相同操作）

**前置条件**：
- 角色 test_role_a 对 resourceId=25 已有 ADD 调整记录

**测试步骤**：
1. 调用 adjust 接口，resourceId=25, action="ADD"
2. 检查调整记录

**预期结果**：
- 调整记录被删除（恢复原状态）
- 或保持 ADD 状态（根据实现）

**优先级**：P2

---

#### TC-04-005: 调整已存在的调整记录（相反操作）

**前置条件**：
- 角色 test_role_a 对 resourceId=25 已有 ADD 调整记录

**测试步骤**：
1. 调用 adjust 接口，resourceId=25, action="REMOVE"
2. 检查调整记录

**预期结果**：
- 调整记录更新为 REMOVE
- 或调整记录被删除（抵消）

**优先级**：P2

---

### 4.2 基本权限保护

#### TC-04-006: 尝试移除基本权限（失败）

**前置条件**：
- resourceId=1 是基本权限（is_basic=1）
- 用户具备角色权限管理权限

**测试步骤**：
1. 调用 POST /api/roles/{roleId}/permissions/adjust
2. 请求体：{resourceId: 1, action: "REMOVE"}

**预期结果**：
- 返回错误
- 错误信息提示"基本权限不可调整"
- 无调整记录创建

**优先级**：P1

---

### 4.3 用户权限调整

#### TC-04-007: 用户个人 ADD 权限

**前置条件**：
- 用户 user1 不拥有 resourceId=35
- 用户具备权限管理权限
- 用户权限调整接口已实现

**测试步骤**：
1. 调用用户权限调整接口
2. 请求体：{userId: user1Id, resourceId: 35, action: "ADD"}
3. 检查 user_permission_adjustment 表
4. 重新计算用户权限

**预期结果**：
- 返回成功
- 调整记录创建
- 用户权限包含 resourceId=35
- 审计日志记录

**优先级**：P1

---

#### TC-04-008: 用户个人 REMOVE 权限

**前置条件**：
- 用户 user1 拥有 resourceId=10（从角色继承）
- 用户具备权限管理权限

**测试步骤**：
1. 调用用户权限调整接口
2. 请求体：{userId: user1Id, resourceId: 10, action: "REMOVE"}
3. 检查 user_permission_adjustment 表
4. 重新计算用户权限

**预期结果**：
- 返回成功
- 调整记录创建
- 用户权限不包含 resourceId=10
- 角色权限不受影响

**优先级**：P1

---

#### TC-04-009: 用户临时权限设置

**前置条件**：
- 用户 user1 不拥有 resourceId=40
- 用户具备权限管理权限

**测试步骤**：
1. 调用用户权限调整接口
2. 请求体：{userId: user1Id, resourceId: 40, action: "ADD", expireAt: 未来时间}
3. 检查 user_permission_adjustment 表
4. 重新计算用户权限

**预期结果**：
- 返回成功
- 调整记录创建，包含 expire_at
- 用户权限包含 resourceId=40（有效期内）

**优先级**：P1

---

## 5. API 权限校验测试用例

### 5.1 接口权限拦截

#### TC-05-001: 有权限访问 API

**前置条件**：
- 用户 user1 拥有 permission_key="user:view"
- API 接口配置了权限校验

**测试步骤**：
1. 用户 user1 登录获取 Token
2. 调用 GET /api/users 接口
3. 携带有效 Token

**预期结果**：
- 返回成功状态码 200
- 返回用户列表数据

**优先级**：P1

---

#### TC-05-002: 无权限访问 API

**前置条件**：
- 用户 user1 不拥有 permission_key="user:delete"
- API 接口配置了权限校验

**测试步骤**：
1. 用户 user1 登录获取 Token
2. 调用 DELETE /api/users/{id} 接口
3. 携带有效 Token

**预期结果**：
- 返回 403 Forbidden
- 错误信息提示无权限

**优先级**：P1

---

#### TC-05-003: 未登录访问 API

**前置条件**：
- API 接口需要认证

**测试步骤**：
1. 不携带 Token
2. 调用 GET /api/users 接口

**预期结果**：
- 返回 401 Unauthorized
- 错误信息提示需要登录

**优先级**：P1

---

#### TC-05-004: Token 过期访问 API

**前置条件**：
- 用户 Token 已过期

**测试步骤**：
1. 携带过期 Token
2. 调用 GET /api/users 接口

**预期结果**：
- 返回 401 Unauthorized
- 错误信息提示 Token 过期

**优先级**：P1

---

### 5.2 权限注解校验

#### TC-05-005: @RequirePermission 注解生效

**前置条件**：
- Controller 方法标注 @RequirePermission("user:create")
- 用户 user1 拥有该权限

**测试步骤**：
1. 用户 user1 调用标注注解的接口

**预期结果**：
- 接口正常执行
- 权限校验通过

**优先级**：P1

---

#### TC-05-006: @RequirePermission 注解拦截

**前置条件**：
- Controller 方法标注 @RequirePermission("user:delete")
- 用户 user1 不拥有该权限

**测试步骤**：
1. 用户 user1 调用标注注解的接口

**预期结果**：
- 返回 403 Forbidden
- 接口方法未执行

**优先级**：P1

---

### 5.3 拦截器校验

#### TC-05-007: 拦截器 URI 匹配

**前置条件**：
- 存在 API 资源：uriPattern="/api/users/**", method="GET"
- 用户 user1 拥有该资源

**测试步骤**：
1. 用户 user1 调用 GET /api/users/123 接口

**预期结果**：
- 权限校验通过
- URI 模式匹配正确

**优先级**：P1

---

#### TC-05-008: 拦截器 Method 匹配

**前置条件**：
- 存在 API 资源：uriPattern="/api/users", method="POST"
- 用户 user1 拥有该资源
- 用户 user1 不拥有 GET /api/users 权限

**测试步骤**：
1. 用户 user1 调用 POST /api/users 接口
2. 用户 user1 调用 GET /api/users 接口

**预期结果**：
- POST 请求通过
- GET 请求被拦截（403）

**优先级**：P1

---

## 6. 审计日志测试用例

### 6.1 日志记录

#### TC-06-001: 角色权限调整记录审计日志

**前置条件**：
- 用户具备角色权限管理权限
- 审计日志功能已启用

**测试步骤**：
1. 调用角色权限调整接口
2. 查询 permission_audit_log 表

**预期结果**：
- 审计日志记录创建
- 包含：targetType=ROLE, targetId, resourceId, action, operatorId, operatedAt
- 信息完整准确

**优先级**：P1

---

#### TC-06-002: 用户权限调整记录审计日志

**前置条件**：
- 用户具备权限管理权限
- 审计日志功能已启用

**测试步骤**：
1. 调用用户权限调整接口
2. 查询 permission_audit_log 表

**预期结果**：
- 审计日志记录创建
- 包含：targetType=USER, targetId, resourceId, action, operatorId, operatedAt
- 信息完整准确

**优先级**：P1

---

#### TC-06-003: 资源创建记录审计日志

**前置条件**：
- 用户具备资源管理权限
- 审计日志功能已启用

**测试步骤**：
1. 调用资源创建接口
2. 查询 permission_audit_log 表

**预期结果**：
- 审计日志记录创建
- 包含：operationType=CREATE, targetType=PERMISSION
- 信息完整准确

**优先级**：P2

---

#### TC-06-004: 资源删除记录审计日志

**前置条件**：
- 用户具备资源管理权限
- 审计日志功能已启用

**测试步骤**：
1. 调用资源删除接口
2. 查询 permission_audit_log 表

**预期结果**：
- 审计日志记录创建
- 包含：operationType=DELETE, targetType=PERMISSION
- 信息完整准确

**优先级**：P2

---

### 6.2 日志查询

#### TC-06-005: 按目标查询审计日志

**前置条件**：
- 系统中存在审计日志数据

**测试步骤**：
1. 调用审计日志查询接口
2. 参数：targetType=ROLE, targetId=roleId

**预期结果**：
- 返回该角色的所有权限变更日志
- 支持分页
- 按时间倒序排列

**优先级**：P2

---

#### TC-06-006: 按操作人查询审计日志

**前置条件**：
- 系统中存在审计日志数据

**测试步骤**：
1. 调用审计日志查询接口
2. 参数：operatorId=operatorId

**预期结果**：
- 返回该操作人的所有操作日志
- 支持分页

**优先级**：P2

---

#### TC-06-007: 按时间范围查询审计日志

**前置条件**：
- 系统中存在审计日志数据

**测试步骤**：
1. 调用审计日志查询接口
2. 参数：startTime, endTime

**预期结果**：
- 返回指定时间范围内的日志
- 支持分页

**优先级**：P2

---

## 7. 缓存测试用例

### 7.1 缓存命中

#### TC-07-001: 用户权限缓存命中

**前置条件**：
- 用户 user1 的权限已缓存到 Redis
- 缓存未过期

**测试步骤**：
1. 调用 PermissionService.calculateUserPermissions(user1Id)
2. 检查是否查询数据库

**预期结果**：
- 从缓存返回结果
- 未执行数据库查询
- 返回结果正确

**优先级**：P1

---

#### TC-07-002: 角色权限缓存命中

**前置条件**：
- 角色 test_role_a 的权限已缓存到 Redis
- 缓存未过期

**测试步骤**：
1. 调用 PermissionService.calculateRolePermissions(roleId)
2. 检查是否查询数据库

**预期结果**：
- 从缓存返回结果
- 未执行数据库查询
- 返回结果正确

**优先级**：P1

---

### 7.2 缓存清除

#### TC-07-003: 用户权限变更后清除缓存

**前置条件**：
- 用户 user1 的权限已缓存
- 用户具备权限管理权限

**测试步骤**：
1. 调用用户权限调整接口
2. 检查缓存是否清除
3. 再次调用 calculateUserPermissions

**预期结果**：
- 权限调整后缓存被清除
- 再次计算时从数据库重新加载
- 缓存重新建立

**优先级**：P1

---

#### TC-07-004: 角色权限变更后清除相关用户缓存

**前置条件**：
- 角色 test_role_a 的权限已缓存
- 用户 user1 拥有 test_role_a，用户权限已缓存

**测试步骤**：
1. 调用角色权限调整接口
2. 检查角色缓存是否清除
3. 检查用户 user1 的缓存是否清除
4. 调用 calculateUserPermissions(user1Id)

**预期结果**：
- 角色缓存被清除
- 相关用户缓存被清除（TODO: 需实现）
- 再次计算时从数据库重新加载

**优先级**：P1

---

#### TC-07-005: 手动刷新用户权限缓存

**前置条件**：
- 用户 user1 的权限已缓存

**测试步骤**：
1. 调用 POST /api/users/{userId}/permissions/refresh-cache 接口

**预期结果**：
- 返回成功
- 用户权限缓存被清除
- 下次查询时重新加载

**优先级**：P2

---

### 7.3 缓存一致性

#### TC-07-006: 缓存与数据库一致性

**前置条件**：
- 用户 user1 的权限已缓存
- 直接修改数据库中的权限数据（绕过应用）

**测试步骤**：
1. 直接更新数据库中的 role_permission_adjustment 表
2. 调用 calculateUserPermissions(user1Id)
3. 检查结果

**预期结果**：
- 缓存未失效时返回旧数据（预期行为）
- 缓存 TTL 过期后返回新数据
- 或通过手动刷新获取新数据

**优先级**：P2

---

#### TC-07-007: 缓存 TTL 过期

**前置条件**：
- 用户 user1 的权限已缓存
- 缓存 TTL 设置为较短时间（如 30 秒）

**测试步骤**：
1. 等待缓存 TTL 过期
2. 调用 calculateUserPermissions(user1Id)

**预期结果**：
- 缓存过期
- 从数据库重新加载
- 缓存重新建立

**优先级**：P2

---

## 附录

### A. 测试数据清理脚本

```sql
-- 清理测试数据（测试后执行）
DELETE FROM permission_audit_log WHERE target_id IN (3, 4);  -- 测试角色
DELETE FROM user_permission_adjustment WHERE user_id IN (2, 3, 4);  -- 测试用户
DELETE FROM role_permission_adjustment WHERE role_id IN (3, 4);  -- 测试角色
DELETE FROM user_role WHERE user_id IN (2, 3, 4);
DELETE FROM role_permission WHERE role_id IN (3, 4);
DELETE FROM role WHERE id IN (3, 4);
DELETE FROM user WHERE id IN (2, 3, 4);
DELETE FROM resource WHERE id > 100;  -- 测试资源
```

### B. 测试执行检查清单

- [ ] 测试环境准备完成
- [ ] 测试数据初始化完成
- [ ] 所有 P1 测试用例执行通过
- [ ] P2 测试用例通过率 ≥ 95%
- [ ] 缺陷已记录并跟踪
- [ ] 测试报告已生成

---

> 文档维护：测试团队  
> 最后审核：待审核
