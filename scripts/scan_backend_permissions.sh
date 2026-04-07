#!/bin/bash

echo "=== 后端权限使用情况扫描 ==="
echo ""

# 1. 扫描Controller文件
echo "1. 扫描Controller文件中的权限注解..."
echo ""

echo "找到的Controller文件:"
find backend/src -name "*Controller.java" | head -20

echo ""
echo "2. 统计Controller数量..."
controller_count=$(find backend/src -name "*Controller.java" | wc -l)
echo "Controller文件数量: $controller_count"

echo ""
echo "3. 扫描权限相关注解..."
echo "权限注解示例:"
grep -r "@PreAuthorize\|@RequiresPermissions\|@Permission" backend/src --include="*.java" | head -20

echo ""
echo "4. 扫描权限拦截器..."
echo "权限拦截器相关文件:"
find backend/src -name "*Permission*.java" -o -name "*Auth*.java" -o -name "*Security*.java" | head -10

echo ""
echo "5. 扫描API权限配置..."
echo "API权限配置相关代码:"
grep -r "permission\|Permission" backend/src/main/java/com/qidian/camera/module/auth --include="*.java" | head -10

echo ""
echo "=== 后端扫描完成 ==="