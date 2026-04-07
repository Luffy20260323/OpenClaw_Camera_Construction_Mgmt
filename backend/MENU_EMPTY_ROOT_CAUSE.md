# 菜单为空问题 - 根本原因分析

**分析时间**: 2026-04-04 15:40

---

## 测试结果

### 数据库
- ✅ 46 个资源（17 MODULE + 29 MENU）
- ✅ 2 个 MODULE 的 `parent_id=NULL`（顶级模块）
- ✅ 所有 MENU 都有 `parent_id`（指向 MODULE）

### API 返回
- ❌ 只返回 2 个顶级 MODULE
- ❌ `children` 字段为空或 null

### 前端
- ✅ `SidebarMenu.vue` 逻辑正确
- ✅ `buildMenuTree()` 能处理扁平列表或树形结构

---

## 根本原因

**后端 `buildTree()` 方法的递归逻辑问题**：

```java
private List<ResourceTreeNode> buildTree(List<Resource> resources, Long parentId) {
    return resources.stream()
        .filter(r -> Objects.equals(r.getParentId(), parentId))  // ← 问题在这里！
        .map(r -> {
            ResourceTreeNode node = new ResourceTreeNode();
            // ... 设置字段
            node.setChildren(buildTree(resources, r.getId()));  // ← 递归查找子节点
            return node;
        })
        .collect(Collectors.toList());
}
```

**问题分析**:

1. `findMenuTree()` 返回 46 个资源
2. `buildTree(resources, null)` 过滤出 2 个 `parent_id=NULL` 的 MODULE
3. 对每个 MODULE 递归调用 `buildTree(resources, module.id)`
4. **应该**找到所有 `parent_id=module.id` 的 MENU
5. **但实际**没有找到子节点！

**可能原因**:

1. `Objects.equals()` 比较失败（类型不匹配）
2. `Resource.getParentId()` 返回 null（即使数据库有值）
3. MyBatis 映射问题（`parent_id` 未映射到 `parentId`）

---

## 解决方案

### 方案 1: 修复 `buildTree()` 方法

**修改 ResourceService.java**:

```java
private List<ResourceTreeNode> buildTree(List<Resource> resources, Long parentId) {
    return resources.stream()
        .filter(r -> {
            // 修复 null 比较问题
            if (parentId == null) {
                return r.getParentId() == null;
            }
            return parentId.equals(r.getParentId());
        })
        .map(r -> {
            ResourceTreeNode node = new ResourceTreeNode();
            node.setId(r.getId());
            node.setName(r.getName());
            node.setCode(r.getCode());
            node.setType(r.getType());
            node.setPermissionKey(r.getPermissionKey());
            node.setIcon(r.getIcon());
            node.setPath(r.getPath());
            node.setSortOrder(r.getSortOrder());
            node.setIsBasic(r.getIsBasic());
            node.setIsVisible(r.getIsVisible());
            
            // 递归查找子节点
            List<ResourceTreeNode> children = buildTree(resources, r.getId());
            if (!children.isEmpty()) {
                node.setChildren(children);
            }
            
            return node;
        })
        .sorted(Comparator.comparing(ResourceTreeNode::getSortOrder))
        .collect(Collectors.toList());
}
```

---

### 方案 2: 改用 SQL 直接返回树形结构

**修改 ResourceMapper.java**:

```java
@Select("SELECT * FROM resource WHERE type IN ('MODULE', 'MENU') AND status = 1 ORDER BY parent_id, sort_order")
List<Resource> findMenuTree();
```

**修改 ResourceService.java**:

```java
public List<ResourceTreeNode> getMenuTree() {
    List<Resource> allResources = resourceMapper.findMenuTree();
    
    // 分离 MODULE 和 MENU
    List<Resource> modules = allResources.stream()
        .filter(r -> "MODULE".equals(r.getType()))
        .collect(Collectors.toList());
    
    List<Resource> menus = allResources.stream()
        .filter(r -> "MENU".equals(r.getType()))
        .collect(Collectors.toList());
    
    // 构建 MODULE 树
    List<ResourceTreeNode> moduleTree = modules.stream()
        .filter(m -> m.getParentId() == null)  // 顶级 MODULE
        .map(m -> {
            ResourceTreeNode node = convertToNode(m);
            // 查找该 MODULE 下的所有 MENU
            List<ResourceTreeNode> menuChildren = menus.stream()
                .filter(menu -> menu.getParentId() != null && menu.getParentId().equals(m.getId()))
                .map(this::convertToNode)
                .sorted(Comparator.comparing(ResourceTreeNode::getSortOrder))
                .collect(Collectors.toList());
            if (!menuChildren.isEmpty()) {
                node.setChildren(menuChildren);
            }
            return node;
        })
        .collect(Collectors.toList());
    
    return moduleTree;
}

private ResourceTreeNode convertToNode(Resource r) {
    ResourceTreeNode node = new ResourceTreeNode();
    node.setId(r.getId());
    node.setName(r.getName());
    node.setCode(r.getCode());
    node.setType(r.getType());
    node.setPermissionKey(r.getPermissionKey());
    node.setIcon(r.getIcon());
    node.setPath(r.getPath());
    node.setSortOrder(r.getSortOrder());
    node.setIsBasic(r.getIsBasic());
    node.setIsVisible(r.getIsVisible());
    return node;
}
```

---

### 方案 3: 前端直接使用扁平数据

**修改 SidebarMenu.vue**:

不修改后端，前端直接使用返回的树形结构，而不是再次调用 `buildMenuTree()`。

```javascript
// 不使用 buildMenuTree，直接使用后端返回的树
const menuTree = computed(() => {
  const menus = userStore.menus || []
  
  // 如果后端返回的是树形结构，直接使用
  if (menus.length > 0 && menus[0].children !== undefined) {
    return menus
  }
  
  // 否则自己构建树
  return buildMenuTree(menus, null)
})
```

---

## 推荐方案

**推荐方案 2**，原因：

1. ✅ 逻辑清晰，易于维护
2. ✅ 性能更好（SQL 排序）
3. ✅ 不依赖递归
4. ✅ 明确处理 MODULE 和 MENU 的关系

---

## 下一步

### 立即修复

1. **修改 ResourceService.java** - 实现方案 2
2. **重新编译后端**
3. **重启容器**
4. **测试菜单 API**

### 验证

```bash
curl http://localhost:8080/api/resources/menu-tree \
  -H "Authorization: Bearer <token>" | jq '.data | length'

# 预期：> 2（应该返回所有顶级 MODULE 及其子 MENU）
```

---

**状态**: ⏳ 等待修复  
**优先级**: 🔴 高
