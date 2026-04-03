# 权限动态维护机制

**文档版本：** V1.0  
**创建日期：** 2026-04-02  
**最后更新：** 2026-04-02

---

## 📋 问题背景

原有权限系统存在的问题：

1. **角色覆盖不完整**：硬编码 9 个预定义角色的权限，用户自定义角色没有缺省权限
2. **动态维护缺失**：新增/删除角色、用户时，权限数据不能自动维护
3. **缓存清理不完善**：部分操作后权限缓存未及时清除

---

## ✅ 解决方案

### 一、通用角色缺省权限机制

#### 设计原则

- **不硬编码具体角色**：按角色类型（SYSTEM/DEFAULT/PRESET）配置缺省权限
- **新角色自动继承**：创建角色时根据类型自动分配缺省权限
- **支持自定义扩展**：管理员可修改角色类型的缺省权限模板

#### 数据库设计

**表：`role_type_default_permissions`**

```sql
CREATE TABLE role_type_default_permissions (
    id BIGSERIAL PRIMARY KEY,
    role_type VARCHAR(20) NOT NULL,  -- SYSTEM/DEFAULT/PRESET
    resource_id BIGINT NOT NULL REFERENCES resource(id),
    permission_key VARCHAR(200),
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(role_type, resource_id)
);
```

**权限模板配置：**

| 角色类型 | 缺省权限范围 | 说明 |
|---------|------------|------|
| SYSTEM | 所有 ELEMENT + 所有资源 | 系统管理员 |
| DEFAULT | 基础查看权限（ELEMENT） | 普通角色 |
| PRESET | 可配置的预设权限 | 特殊角色 |

---

### 二、数据库触发器自动维护

#### 触发器列表

| 触发器 | 时机 | 作用 |
|-------|------|------|
| `trg_role_after_insert` | AFTER INSERT ON roles | 新增角色时自动分配缺省权限 |
| `trg_role_before_delete` | BEFORE DELETE ON roles | 删除角色时清理关联数据 |
| `trg_user_before_delete` | BEFORE DELETE ON users | 删除用户时清理关联数据 |
| `trg_user_role_change` | AFTER INSERT/DELETE/UPDATE ON user_roles | 用户角色变更时记录日志 |

#### 触发器逻辑

**新增角色触发器：**
```sql
CREATE TRIGGER trg_role_after_insert
    AFTER INSERT ON roles
    FOR EACH ROW
    EXECUTE FUNCTION trg_role_after_insert();

-- 函数逻辑：
-- 1. 根据 NEW.type 从 role_type_default_permissions 获取缺省资源
-- 2. 批量插入到 role_resource 表（permission_type='default'）
```

**删除角色触发器：**
```sql
CREATE TRIGGER trg_role_before_delete
    BEFORE DELETE ON roles
    FOR EACH ROW
    EXECUTE FUNCTION trg_role_before_delete();

-- 函数逻辑：
-- 1. DELETE FROM role_resource WHERE role_id = OLD.id
-- 2. DELETE FROM role_permission_adjustment WHERE role_id = OLD.id
-- 3. DELETE FROM user_roles WHERE role_id = OLD.id
```

---

### 三、后端服务层缓存清理

#### PermissionService 缓存管理

**缓存策略：**
- Redis Key: `user:permission:{userId}` (TTL: 30 分钟)
- Redis Key: `role:permission:{roleId}` (TTL: 60 分钟)

**缓存清理方法：**

| 方法 | 触发时机 | 清理范围 |
|------|---------|---------|
| `evictUserPermissionCache(userId)` | 用户角色变更、用户权限调整 | 单个用户 |
| `evictRolePermissionCache(roleId)` | 角色权限调整 | 角色 + 该角色下所有用户 |
| `evictResourcePermissionCache(resourceId, affectedRoleIds)` | 资源权限变更 | 相关角色 + 用户 |
| `evictUserPermissionCacheBatch(userIds)` | 批量用户角色变更 | 批量用户 |

#### RoleService 完善

**创建角色：**
```java
@Transactional
public RoleDTO createRole(CreateRoleRequest request) {
    // ... 创建角色
    role.setType(request.getType() != null ? request.getType() : "DEFAULT");
    roleMapper.insert(role);
    
    // 自动分配缺省权限
    assignDefaultPermissions(role);
    
    return getRoleById(role.getId());
}
```

**删除角色：**
```java
@Transactional
public void deleteRole(Long id) {
    // ... 检查
    permissionService.evictRolePermissionCache(id); // 清除缓存
    roleMapper.deleteById(id);
}
```

#### UserService 完善

**删除用户：**
```java
@Transactional
public void deleteUser(Long userId, Long operatorId) {
    // ... 删除关联数据
    permissionService.evictUserPermissionCache(userId); // 清除缓存
    userMapper.deleteById(userId);
}
```

**用户角色变更：**
```java
@Transactional
public void updateUserRoles(Long userId, List<Long> roleIds) {
    // 删除旧关联
    jdbcTemplate.update("DELETE FROM user_roles WHERE user_id = ?", userId);
    
    // 添加新关联
    for (Long roleId : roleIds) {
        jdbcTemplate.update("INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)", userId, roleId);
    }
    
    // 清除缓存
    permissionService.evictUserPermissionCache(userId);
}
```

---

## 📊 权限计算公式

### 完整公式

```
用户最终权限 = 
    ∑(各角色的完整权限) 
    + 用户调整 (ADD) 
    - 用户调整 (REMOVE)

其中：
角色的完整权限 = 
    基本权限 (basic) 
    + 缺省权限 (default) ← 从 role_type_default_permissions 自动分配
    + 角色调整 (ADD) 
    - 角色调整 (REMOVE)
```

### 视图：`v_user_permission_detail`

```sql
CREATE VIEW v_user_permission_detail AS
WITH user_roles AS (...),
     role_resources AS (...),
     role_adj_add AS (...),
     role_adj_remove AS (...),
     user_direct AS (...),
     user_adj_add AS (...),
     user_adj_remove AS (...),
     combined AS (...)
SELECT 
    c.user_id,
    c.resource_id,
    r.name as resource_name,
    r.code as resource_code,
    r.type as resource_type,
    r.permission_key,
    r.module_code,
    c.source as permission_source  -- ROLE / ROLE_ADJ_ADD / DIRECT / USER_ADJ_ADD
FROM combined c
JOIN resource r ON r.id = c.resource_id
WHERE c.resource_id NOT IN (...)  -- 排除 REMOVE 的权限
```

---

## 🔄 操作流程

### 场景 1：新增角色

```
1. 管理员创建角色（指定 type=DEFAULT）
   ↓
2. 触发器 trg_role_after_insert 执行
   ↓
3. 从 role_type_default_permissions 读取 DEFAULT 类型缺省权限
   ↓
4. 批量插入 role_resource 表
   ↓
5. 角色自动拥有缺省权限（无需手动配置）
```

### 场景 2：删除角色

```
1. 管理员删除角色
   ↓
2. 触发器 trg_role_before_delete 执行
   ↓
3. 清理 role_resource、role_permission_adjustment、user_roles
   ↓
4. PermissionService.evictRolePermissionCache 清除缓存
   ↓
5. 删除 roles 表记录
```

### 场景 3：新增用户

```
1. 管理员创建用户并分配角色
   ↓
2. 插入 user_roles 关联
   ↓
3. PermissionService.evictUserPermissionCache 清除缓存
   ↓
4. 用户自动拥有角色的所有权限
```

### 场景 4：删除用户

```
1. 管理员删除用户
   ↓
2. 触发器 trg_user_before_delete 执行
   ↓
3. 清理 user_resource、user_permission_adjustment、user_roles
   ↓
4. PermissionService.evictUserPermissionCache 清除缓存
   ↓
5. 删除 users 表记录
```

### 场景 5：用户角色变更

```
1. 管理员修改用户角色
   ↓
2. 删除旧 user_roles 记录
   ↓
3. 插入新 user_roles 记录
   ↓
4. PermissionService.evictUserPermissionCache 清除缓存
   ↓
5. 用户权限立即更新（下次查询从数据库重新计算）
```

---

## ✅ 验收标准

### 功能验收

| 场景 | 预期结果 | 验证 SQL |
|------|---------|---------|
| 新增角色 | 自动分配缺省权限 | `SELECT COUNT(*) FROM role_resource WHERE role_id = ? AND permission_type = 'default'` |
| 删除角色 | 清理所有关联数据 | `SELECT COUNT(*) FROM role_resource WHERE role_id = ?` (应为 0) |
| 新增用户 | 自动拥有角色权限 | `SELECT COUNT(*) FROM v_user_permission_detail WHERE user_id = ?` |
| 删除用户 | 清理所有关联数据 | `SELECT COUNT(*) FROM user_resource WHERE user_id = ?` (应为 0) |
| 用户角色变更 | 权限立即更新 | 对比变更前后的 `v_user_permission_detail` |

### 性能验收

| 指标 | 目标 | 验证方法 |
|------|------|---------|
| 权限查询响应时间 | < 50ms (缓存命中) | Redis GET `user:permission:{userId}` |
| 权限计算时间 | < 200ms (缓存未命中) | 执行 `v_user_permission_detail` 查询 |
| 缓存命中率 | > 90% | Redis INFO stats |

---

## 📝 部署步骤

### 1. 执行数据库迁移

```bash
cd /root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/backend

# 执行 V29 迁移
docker-compose exec backend psql -U camera -d camera -f /flyway/sql/V29__complete_element_permissions.sql
```

### 2. 编译后端代码

```bash
cd backend
mvn clean package -DskipTests
docker build -t camera-backend:latest .
docker-compose restart backend
```

### 3. 验证触发器

```sql
-- 测试新增角色触发器
INSERT INTO roles (role_name, role_code, type, company_id) 
VALUES ('测试角色', 'test_role', 'DEFAULT', 1);

-- 验证缺省权限已分配
SELECT COUNT(*) FROM role_resource WHERE role_id = (SELECT id FROM roles WHERE role_code = 'test_role');

-- 清理测试数据
DELETE FROM roles WHERE role_code = 'test_role';
```

### 4. 验证缓存清理

```sql
-- 查看缓存键
docker-compose exec redis redis-cli KEYS "user:permission:*"
docker-compose exec redis redis-cli KEYS "role:permission:*"
```

---

## 🔧 维护指南

### 修改角色类型缺省权限

```sql
-- 添加 DEFAULT 类型的缺省权限
INSERT INTO role_type_default_permissions (role_type, resource_id, permission_key)
SELECT 'DEFAULT', id, permission_key
FROM resource
WHERE type = 'ELEMENT'
AND permission_key = 'system:user:view:button';

-- 删除 DEFAULT 类型的缺省权限
DELETE FROM role_type_default_permissions
WHERE role_type = 'DEFAULT'
AND permission_key = 'system:user:view:button';
```

### 查看角色权限来源

```sql
-- 查看某角色的权限详情
SELECT 
    r.name as resource_name,
    r.type as resource_type,
    r.permission_key,
    rr.permission_type as source  -- basic/default
FROM role_resource rr
JOIN resource r ON r.id = rr.resource_id
WHERE rr.role_id = 1
ORDER BY r.type, r.permission_key;
```

### 查看用户权限来源

```sql
-- 查看某用户的权限详情
SELECT 
    r.name as resource_name,
    r.type as resource_type,
    r.permission_key,
    source as permission_source  -- ROLE/ROLE_ADJ_ADD/DIRECT/USER_ADJ_ADD
FROM v_user_permission_detail
WHERE user_id = 1
ORDER BY r.type, source;
```

---

## 📞 故障排查

### 问题 1：新创建的角色没有权限

**检查步骤：**
1. 确认角色 type 字段有值
2. 检查 `role_type_default_permissions` 表是否有对应类型的配置
3. 查看触发器是否正常工作

```sql
-- 检查角色类型
SELECT id, role_name, type FROM roles WHERE role_code = 'xxx';

-- 检查缺省权限模板
SELECT COUNT(*) FROM role_type_default_permissions WHERE role_type = 'DEFAULT';

-- 检查触发器
SELECT * FROM pg_trigger WHERE tgname = 'trg_role_after_insert';
```

### 问题 2：删除角色后权限未清理

**检查步骤：**
1. 检查触发器是否存在
2. 手动清理残留数据

```sql
-- 检查触发器
SELECT * FROM pg_trigger WHERE tgname = 'trg_role_before_delete';

-- 手动清理
DELETE FROM role_resource WHERE role_id = ?;
DELETE FROM role_permission_adjustment WHERE role_id = ?;
DELETE FROM user_roles WHERE role_id = ?;
```

### 问题 3：权限缓存未更新

**检查步骤：**
1. 检查 Redis 连接
2. 手动清除缓存

```bash
# 查看缓存
docker-compose exec redis redis-cli GET "user:permission:1"

# 清除缓存
docker-compose exec redis redis-cli DEL "user:permission:1"
```

---

**维护者：** 北京其点技术服务有限公司  
**联系方式：** support@qidian.com
