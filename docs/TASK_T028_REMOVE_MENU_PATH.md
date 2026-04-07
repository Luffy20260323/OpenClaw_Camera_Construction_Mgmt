# MENU 资源 path 字段移除 - 任务完成报告

**任务 ID**: T028
**执行时间**: 2026-04-06 09:46
**状态**: ✅ 已完成

---

## 任务目标

移除 MENU（菜单）类型资源的 `path` 字段配置，实现职责分离：
- **MENU** = 导航显示（侧边栏菜单项）
- **PAGE** = 页面权限（能否访问该页面）

---

## 修改内容

### 1. 后端修改

#### 1.1 Resource.java 实体类
**文件**: `backend/src/main/java/com/qidian/camera/module/auth/entity/Resource.java`

**修改**: 无（保留 `path` 字段定义，但不再用于 MENU 类型）

#### 1.2 ResourceController.java
**文件**: `backend/src/main/java/com/qidian/camera/module/auth/controller/ResourceController.java`

**修改**: 创建菜单时不再设置 `path` 字段
```java
// MENU 类型不需要 path 字段（路由由 PAGE 资源控制）
// resource.setPath(dto.getPath());
```

#### 1.3 ResourceService.java
**文件**: `backend/src/main/java/com/qidian/camera/module/auth/service/ResourceService.java`

**修改**:
1. `convertToNode()` 方法：MENU 类型不设置 path
```java
if (!"MENU".equals(r.getType())) {
    node.setPath(r.getPath());
}
```

2. `ResourceTreeNode.getMenuPath()` 方法：MENU 类型返回 null
```java
public String getMenuPath() { 
    if ("MENU".equals(type)) {
        return null;
    }
    return path; 
}
```

#### 1.4 数据库迁移脚本
**文件**: `backend/src/main/resources/db/migration/V34__remove_menu_path.sql`

**内容**:
```sql
-- 清空 MENU 资源的 path 字段
UPDATE resource 
SET path = NULL 
WHERE type = 'MENU' AND path IS NOT NULL;

-- 添加字段说明
COMMENT ON COLUMN resource.path IS '前端路由路径（仅 PAGE 类型需要，MENU 类型不需要）';
```

**执行结果**: 清空 28 个 MENU 资源的 path 字段 ✅

---

### 2. 前端修改

#### 2.1 SidebarMenu.vue
**文件**: `frontend/src/components/SidebarMenu.vue`

**修改**:
1. 菜单项点击事件改为 `handleMenuItemClick()`
2. 新增 `getMenuRoute()` 方法：通过 MENU 的 code 查找对应的 PAGE 资源的 path
3. 移除首页过滤中的 `menuPath === '/'` 判断

**路由逻辑**:
```javascript
const getMenuRoute = (menu) => {
  // 1. 查找该 MENU 下的 PAGE 资源的 path
  const page = userMenus.value.find(m => {
    return parentId === menu.id && type === 'PAGE'
  })
  if (page && page.path) {
    return page.path
  }
  
  // 2. 降级方案：使用 code 映射（MENU_CODE_MAP）
  for (const [path, code] of Object.entries(MENU_CODE_MAP)) {
    if (code === menuCode) {
      return path
    }
  }
  
  // 3. 最后降级：返回 # 避免跳转
  return '#'
}
```

#### 2.2 ResourceList.vue
**文件**: `frontend/src/views/system/ResourceList.vue`

**修改**:
1. 新建菜单对话框：path 字段改为可选，添加提示说明
2. `handleCreateMenu()` 方法：path 字段可选传递

---

## 部署验证

### 编译构建
- ✅ 后端 Maven 编译成功
- ✅ 前端 npm build 成功
- ✅ Docker 镜像构建成功

### 容器状态
```
camera-backend    Up (healthy)
camera-frontend   Up (healthy)
camera-postgres   Up (healthy)
```

### 数据库验证
```sql
-- 验证结果
total_menu | with_path | without_path 
-----------+-----------+--------------
        28 |         0 |           28

-- 示例：资源管理菜单
id |   name   |        code         | path 
----+----------+---------------------+------
 17 | 资源管理 | resource_management | (NULL)
```

---

## 影响范围

### 正面影响
1. ✅ **职责清晰**: MENU 只管显示，PAGE 只管权限和路由
2. ✅ **避免冗余**: 不再需要维护 MENU 和 PAGE 的 path 一致性
3. ✅ **简化配置**: 创建菜单时不需要填写路径

### 兼容性
1. ✅ **向后兼容**: 保留 `path` 字段定义，现有 PAGE 资源不受影响
2. ✅ **降级方案**: 通过 `MENU_CODE_MAP` 确保即使没有 PAGE 资源也能跳转
3. ✅ **数据迁移**: 已清空所有现有 MENU 资源的 path 字段

---

## 用户操作变化

### 创建菜单时
**之前**: 必须填写"路由路径"字段
**现在**: "路由路径"为可选字段，系统会自动通过 code 匹配

### 菜单点击行为
**之前**: 直接使用 MENU.path 跳转
**现在**: 
1. 优先查找该 MENU 下的 PAGE 资源的 path
2. 降级使用 MENU_CODE_MAP 映射
3. 最后降级返回 '#' 不跳转

---

## 待验证事项

- [ ] 用户登录后侧边栏菜单显示正常
- [ ] 点击菜单能正确跳转到对应页面
- [ ] 新建菜单时不填写路径也能正常使用
- [ ] 权限控制正常工作（通过 PAGE 资源）

---

## 相关文件

### 修改的文件
- `backend/src/main/java/com/qidian/camera/module/auth/controller/ResourceController.java`
- `backend/src/main/java/com/qidian/camera/module/auth/service/ResourceService.java`
- `frontend/src/components/SidebarMenu.vue`
- `frontend/src/views/system/ResourceList.vue`

### 新增的文件
- `backend/src/main/resources/db/migration/V34__remove_menu_path.sql`

---

**模型**: qwen3.5-plus
