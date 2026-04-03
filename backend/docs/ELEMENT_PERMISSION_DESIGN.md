# ELEMENT 权限设计文档

## 概述

本文档描述 Camera 项目中 ELEMENT 层级的权限设计方案，采用 **RBAC + 缓存 + 扁平化权限码** 的综合方案。

## 设计目标

1. **细粒度控制**：支持到按钮、链接等操作元素级别的权限控制
2. **高性能**：通过 Redis 缓存实现 < 50ms 的权限响应时间
3. **易维护**：扁平化的权限码格式，便于理解和维护
4. **向后兼容**：兼容现有的权限体系，支持渐进式迁移

## 权限码命名规范

### 格式

```
module:resource:action:type
```

### 组成部分

| 部分 | 说明 | 示例 |
|------|------|------|
| `module` | 模块编码 | `system`, `auth`, `user` |
| `resource` | 资源类型 | `user`, `role`, `permission`, `document` |
| `action` | 操作类型 | `list`, `view`, `create`, `edit`, `delete`, `export`, `import` |
| `type` | 权限类型 | `page`, `button`, `menu`, `api` |

### 权限类型说明

| type | 说明 | 示例 |
|------|------|------|
| `page` | 页面访问权限 | `system:user:list:page` |
| `button` | 按钮操作权限 | `system:user:create:button` |
| `menu` | 菜单显示权限 | `system:user:menu` |
| `api` | API 接口权限 | `system:user:create:api` |

### 完整示例

```
# 用户管理模块
system:user:menu                    # 用户管理菜单
system:user:list:page               # 用户列表页面
system:user:list:view:button        # 查看用户按钮
system:user:create:button           # 新建用户按钮
system:user:create:page             # 用户创建页面
system:user:edit:button             # 编辑用户按钮
system:user:edit:page               # 用户编辑页面
system:user:delete:button           # 删除用户按钮
system:user:export:button           # 导出用户按钮

# 角色管理模块
system:role:menu                    # 角色管理菜单
system:role:list:page               # 角色列表页面
system:role:create:button           # 新建角色按钮
system:role:permission:button       # 分配权限按钮

# 资源管理模块
system:resource:menu                # 资源管理菜单
system:resource:list:page           # 资源列表页面
system:resource:create:button       # 新建资源按钮
system:resource:edit:button         # 编辑资源按钮

# 文档管理模块
system:document:menu                # 文档管理菜单
system:document:list:page           # 文档列表页面
system:document:upload:button       # 上传文档按钮
system:document:download:button     # 下载文档按钮
```

## 数据库设计

### 资源层级结构

```
用户管理 (MENU, id=3)
└── 用户列表 (PAGE, id=20)
    ├── 查看详情按钮 (ELEMENT, id=201)
    │   └── 用户详情 (PAGE, id=21)
    ├── 新建用户按钮 (ELEMENT, id=202)
    │   └── 用户创建 (PAGE, id=22)
    └── 编辑用户按钮 (ELEMENT, id=203)
        └── 用户编辑 (PAGE, id=23)
```

### resource 表结构

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR(100) | 资源名称 |
| code | VARCHAR(50) | 资源编码 |
| type | VARCHAR(20) | 资源类型：MODULE/MENU/PAGE/ELEMENT/API/PERMISSION |
| parent_id | BIGINT | 父资源 ID |
| permission_key | VARCHAR(100) | 权限标识（唯一） |
| module_code | VARCHAR(50) | 所属模块编码 |
| sort_order | INT | 排序号 |
| status | SMALLINT | 状态：1=启用，0=禁用 |

## 后端实现

### API 端点

| 方法 | 端点 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/element/list` | 获取 ELEMENT 列表 | `system:element:view` |
| GET | `/api/element/tree` | 获取 ELEMENT 树 | `system:element:view` |
| GET | `/api/element/page/{pageId}` | 获取某页面的 ELEMENT | `system:element:view` |
| POST | `/api/element` | 创建 ELEMENT | `system:element:create` |
| PUT | `/api/element/{id}` | 更新 ELEMENT | `system:element:edit` |
| DELETE | `/api/element/{id}` | 删除 ELEMENT | `system:element:delete` |

### 权限注解

```java
@ApiPermission("system:element:view")
@GetMapping("/list")
public Result<List<ElementDTO>> getElementList(...) {
    // ...
}

@ApiPermission("system:element:create")
@PostMapping
public Result<ElementDTO> createElement(@RequestBody ElementDTO dto) {
    // ...
}
```

## 前端实现

### 权限指令

```vue
<!-- 单个权限 -->
<el-button v-permission="'system:user:create:button'">
  新建用户
</el-button>

<!-- 多个权限（任一即可） -->
<el-button v-permission="['system:user:edit:button', 'system:user:delete:button']">
  操作
</el-button>

<!-- 通配符匹配 -->
<el-button v-permission="'system:user:*:button'">
  用户相关操作
</el-button>
```

### 权限码自动生成

前端在创建 ELEMENT 时，根据以下规则自动生成权限码：

```javascript
function generatePermissionKey(name, moduleCode) {
  const action = extractAction(name);  // 从名称提取：view/create/edit/delete
  const resource = extractResource(name);  // 从名称提取：user/role/document
  return `${moduleCode}:${resource}:${action}:button`;
}
```

## 缓存设计

### 缓存结构

```
# 用户权限缓存
user:permission:{userId} → Set<Long> resourceIds, TTL: 30 minutes

# 角色权限缓存
role:permission:{roleId} → Set<Long> resourceIds, TTL: 60 minutes
```

### 缓存策略

1. **读操作**：
   - 先查 Redis 缓存
   - 缓存未命中则计算并写入
   - 目标响应时间：< 50ms

2. **写操作**：
   - 权限变更后清除相关缓存
   - 用户权限变更 → 清除 `user:permission:{userId}`
   - 角色权限变更 → 清除 `role:permission:{roleId}` + 该角色下所有用户的缓存

### 缓存清除时机

| 操作 | 清除的缓存 |
|------|-----------|
| 用户角色变更 | `user:permission:{userId}` |
| 角色权限调整 | `role:permission:{roleId}` + 该角色下所有用户缓存 |
| 用户权限调整 | `user:permission:{userId}` |
| ELEMENT 创建/更新/删除 | 所有用户权限缓存（可选，根据重要性决定） |

## 数据库迁移

### V27: 补充 ELEMENT 资源

- 为现有 PAGE 添加 ELEMENT（按钮、链接等）
- ELEMENT 的 `parent_id` 指向所属 PAGE
- 子 PAGE 的 `parent_id` 指向触发它的 ELEMENT

### V28: 更新权限关联

- 将现有 PERMISSION 与 ELEMENT 关联
- 更新 `permission_key` 为新格式

## 验收标准

- [x] 数据库包含完整的 ELEMENT 层级数据
- [x] ELEMENT API 正常工作
- [x] 前端页面可配置 ELEMENT
- [x] `v-permission` 指令正确控制 ELEMENT 显示
- [x] 权限缓存命中，响应时间 < 50ms

## 相关文件

### 后端

- `backend/src/main/resources/db/migration/V27__add_element_resources.sql`
- `backend/src/main/resources/db/migration/V28__link_permission_element.sql`
- `backend/src/main/java/com/qidian/camera/module/resource/dto/ElementDTO.java`
- `backend/src/main/java/com/qidian/camera/module/resource/dto/ElementTreeDTO.java`
- `backend/src/main/java/com/qidian/camera/module/resource/service/ElementService.java`
- `backend/src/main/java/com/qidian/camera/module/resource/controller/ElementController.java`

### 前端

- `frontend/src/views/system/ElementList.vue`
- `frontend/src/directives/permission.js`

## 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| V1.0 | 2026-04-02 | 初始版本，完成 ELEMENT 层级优化 |
