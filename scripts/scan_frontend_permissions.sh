#!/bin/bash

echo "=== 前端权限使用情况扫描 ==="
echo ""

# 1. 扫描v-permission指令使用
echo "1. 扫描v-permission指令使用情况..."
echo ""

echo "找到使用v-permission的文件:"
grep -r "v-permission" frontend/src --include="*.vue" --include="*.js" | head -20

echo ""
echo "2. 统计v-permission使用次数..."
v_permission_count=$(grep -r "v-permission" frontend/src --include="*.vue" --include="*.js" | wc -l)
echo "v-permission指令使用次数: $v_permission_count"

echo ""
echo "3. 提取权限码示例..."
echo "权限码示例:"
grep -r "v-permission=" frontend/src --include="*.vue" | sed 's/.*v-permission="\([^"]*\)".*/\1/' | sort | uniq | head -20

echo ""
echo "4. 扫描权限相关工具函数..."
echo "权限工具函数:"
grep -r "permission\|Permission" frontend/src/utils --include="*.js" | head -10

echo ""
echo "5. 扫描路由权限检查..."
echo "路由权限检查相关代码:"
grep -r "menuCode\|requiresAuth\|permission" frontend/src/router --include="*.js" | head -10

echo ""
echo "=== 前端扫描完成 ==="