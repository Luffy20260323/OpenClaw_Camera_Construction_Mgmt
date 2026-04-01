# Camera 项目权限清单

_最后更新：2026-03-29_

---

## 📋 权限总览

共 **52 个权限**，分为以下模块：

---

## 🔐 认证模块 (auth)

| 权限代码 | 权限名称 | 作用 | 关联 API | 关联菜单 |
|---------|---------|------|---------|---------|
| `auth:login` | 用户登录 | 允许用户登录系统 | `POST /api/auth/login` | - |
| `auth:logout` | 用户登出 | 允许用户登出系统 | `POST /api/auth/logout` | - |
| `auth:captcha:config` | 获取验证码配置 | 获取验证码配置信息 | `GET /api/auth/captcha/config` | - |
| `auth:captcha:image` | 获取验证码图片 | 获取图形验证码 | `GET /api/auth/captcha/image` | - |
| `auth:captcha:get` | 获取验证码 | 获取验证码 | `GET /api/auth/captcha` | - |
| `auth:captcha:sms` | 发送短信验证码 | 发送短信验证码 | `POST /api/auth/captcha/sms` | - |
| `auth:refresh` | 刷新令牌 | 刷新访问令牌 | `POST /api/auth/refresh` | - |
| `auth:validate` | 验证令牌 | 验证令牌有效性 | `POST /api/auth/validate` | - |

---

## 👤 用户模块 (user)

| 权限代码 | 权限名称 | 作用 | 关联 API | 关联菜单 |
|---------|---------|------|---------|---------|
| `user:profile:view` | 查看个人资料 | 查看自己的个人资料 | `GET /api/user/profile` | 个人中心 |
| `user:profile:edit` | 修改个人资料 | 修改自己的个人资料 | `PUT /api/user/profile` | 个人中心 |
| `user:password:change` | 修改密码 | 修改自己的密码 | `PUT /api/user/password` | 个人中心 |
| `user:password:reset` | 重置用户密码 | 重置其他用户的密码 | `PUT /api/user/{id}/password/reset` | 用户管理 |
| `user:register` | 用户注册 | 注册新用户 | `POST /api/user/register` | - |
| `user:create` | 创建用户 | 创建新用户（管理员） | `POST /api/user` | 用户管理 |
| `user:import:json` | 批量导入用户 (JSON) | 通过 JSON 批量导入用户 | `POST /api/user/import/json` | 用户管理 |
| `user:import:template` | 下载导入模板 | 下载用户导入模板 | `GET /api/user/import/template` | 用户管理 |
| `user:import:excel` | 批量导入用户 (Excel) | 通过 Excel 批量导入用户 | `POST /api/user/import/excel` | 用户管理 |
| `user:approve` | 审批用户 | 审批待审核用户 | `PUT /api/user/{id}/approve` | 用户管理 |
| `user:list` | 查询用户列表 | 查询用户列表 | `GET /api/user` | **用户管理** |
| `user:pending:view` | 查看待审批列表 | 查看待审批用户列表 | `GET /api/user/pending` | 用户管理 |
| `user:view` | 查看用户详情 | 查看用户详细信息 | `GET /api/user/{id}` | 用户管理 |
| `user:edit` | 编辑用户 | 编辑用户信息 | `PUT /api/user/{id}` | 用户管理 |
| `user:delete` | 删除用户 | 删除用户 | `DELETE /api/user/{id}` | 用户管理 |

---

## 👥 角色模块 (role)

| 权限代码 | 权限名称 | 作用 | 关联 API | 关联菜单 |
|---------|---------|------|---------|---------|
| `role:list` | 查询角色列表 | 查询角色列表 | `GET /api/role` | **角色管理** |
| `role:view` | 查看角色详情 | 查看角色详细信息 | `GET /api/role/{id}` | 角色管理 |
| `role:create` | 创建角色 | 创建新角色 | `POST /api/role` | 角色管理 |
| `role:edit` | 编辑角色 | 编辑角色信息 | `PUT /api/role/{id}` | 角色管理 |
| `role:delete` | 删除角色 | 删除角色 | `DELETE /api/role/{id}` | 角色管理 |

---

## 📍 作业区模块 (workarea)

| 权限代码 | 权限名称 | 作用 | 关联 API | 关联菜单 |
|---------|---------|------|---------|---------|
| `workarea:list` | 查询作业区列表 | 查询作业区列表 | `GET /api/workarea` | **作业区管理** |
| `workarea:view` | 查看作业区详情 | 查看作业区详细信息 | `GET /api/workarea/{id}` | 作业区管理 |
| `workarea:create` | 创建作业区 | 创建新作业区 | `POST /api/workarea` | 作业区管理 |
| `workarea:edit` | 编辑作业区 | 编辑作业区信息 | `PUT /api/workarea/{id}` | 作业区管理 |
| `workarea:delete` | 删除作业区 | 删除作业区 | `DELETE /api/workarea/{id}` | 作业区管理 |

---

## 🏢 公司模块 (company)

| 权限代码 | 权限名称 | 作用 | 关联 API | 关联菜单 |
|---------|---------|------|---------|---------|
| `company:list` | 查询公司列表 | 查询公司列表 | `GET /api/company` | **公司管理** |
| `company:view` | 查看公司详情 | 查看公司详细信息 | `GET /api/company/{id}` | 公司管理 |
| `company:create` | 创建公司 | 创建新公司 | `POST /api/company` | 公司管理 |
| `company:edit` | 编辑公司 | 编辑公司信息 | `PUT /api/company/{id}` | 公司管理 |
| `company:delete` | 删除公司 | 删除公司 | `DELETE /api/company/{id}` | 公司管理 |
| `company:type:list` | 查询公司类型 | 查询公司类型列表 | `GET /api/company/type` | 公司管理 |

---

## 📋 菜单模块 (menu)

| 权限代码 | 权限名称 | 作用 | 关联 API | 关联菜单 |
|---------|---------|------|---------|---------|
| `menu:my:view` | 查看我的菜单 | 查看当前用户的菜单 | `GET /api/menu/my` | - |
| `menu:all:view` | 查看所有菜单 | 查看所有菜单配置 | `GET /api/menu/all` | 系统设置 |
| `menu:user:view` | 查看用户菜单权限 | 查看用户的菜单权限 | `GET /api/menu/user-permissions/{userId}` | **用户权限配置** |
| `menu:user:edit` | 编辑用户菜单权限 | 编辑用户的菜单权限 | `PUT /api/menu/user-permission` | **角色权限配置** |
| `menu:user:delete` | 删除用户菜单权限 | 删除用户的菜单权限 | `DELETE /api/menu/user-permission` | 用户权限配置 |
| `menu:user:batch` | 批量编辑用户菜单权限 | 批量编辑用户菜单权限 | `PUT /api/menu/user-permissions/batch` | 用户权限配置 |

---

## ⚙️ 系统配置模块 (system)

| 权限代码 | 权限名称 | 作用 | 关联 API | 关联菜单 |
|---------|---------|------|---------|---------|
| `system:config:view` | 查看系统配置 | 查看系统配置 | `GET /api/system/config` | **系统管理**、**系统设置** |
| `system:config:edit` | 编辑系统配置 | 编辑系统配置 | `PUT /api/system/config` | 系统管理 |
| `system:docs:view` | 查看系统文档 | 查看文档中心 | `GET /api/system/docs` | **文档中心** |

---

## 🔒 权限管理模块 (permission)

| 权限代码 | 权限名称 | 作用 | 关联 API | 关联菜单 |
|---------|---------|------|---------|---------|
| `permission:role:manage` | 角色权限管理 | 管理角色权限配置 | `PUT /api/permission/role/{roleId}` | 角色权限配置 |
| `permission:user:manage` | 用户权限管理 | 管理用户权限配置 | `PUT /api/permission/user/{userId}` | 用户权限配置 |
| `permission:edit` | 编辑权限描述 | 编辑权限描述信息 | `PUT /api/permission/{id}` | 角色权限配置 |

---

## 📊 审计日志模块 (audit_log)

| 权限代码 | 权限名称 | 作用 | 关联 API | 关联菜单 |
|---------|---------|------|---------|---------|
| `audit_log` | 审计日志 | 查看权限配置审计日志 | `GET /api/permission/audit-logs` | **审计日志** |

---

## 🗺️ 菜单与权限映射

| 菜单 | 路径 | 所需权限 |
|------|------|---------|
| 个人中心 | `/user/profile` | `user:profile:view` |
| 用户管理 | `/user/management` | `user:list` |
| 角色管理 | `/role` | `role:list` |
| 作业区管理 | `/workarea` | `workarea:list` |
| 公司管理 | `/company` | `company:list` |
| 系统管理 | `/system/config` | `system:config:view` |
| 用户权限配置 | `/system/user-permission` | `menu:user:view` |
| 角色权限配置 | `/system/role-permission` | `menu:user:edit` |
| 审计日志 | `/system/audit-log` | `audit_log` |
| **文档中心** | `/system/docs` | `system:docs:view` |

---

## ⚠️ 特殊权限规则

1. **系统管理员角色** (`ROLE_SYSTEM_ADMIN`)：自动拥有所有权限，不可修改
2. **系统管理员用户** (`admin`)：自动拥有所有权限，不可修改
3. **权限管理权限**：`permission:role:manage`、`permission:user:manage`、`permission:edit` 需要特别授权

---

## 📝 权限命名规范

权限代码格式：`{模块}:{操作}`

- 模块：`auth`、`user`、`role`、`workarea`、`company`、`menu`、`system`、`permission`、`audit_log`
- 操作：`view`、`list`、`create`、`edit`、`delete`、`manage` 等