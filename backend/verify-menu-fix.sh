#!/bin/bash

# =====================================================
# 菜单修复验证脚本
# =====================================================

BASE_URL="http://localhost:8080"

echo "========================================"
echo "菜单修复验证"
echo "========================================"
echo ""
echo "请输入 admin 密码:"
read -s PASSWORD
echo ""
echo ""

# 获取 token
echo "1. 获取 admin token..."
TOKEN=$(curl -s -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"${PASSWORD}\"}" | \
  grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo "❌ Token 获取失败"
    exit 1
fi
echo "✅ Token 获取成功"
echo ""

# 测试菜单 API
echo "2. 测试菜单 API..."
MENU_COUNT=$(curl -s "${BASE_URL}/api/resources/menu-tree" \
  -H "Authorization: Bearer ${TOKEN}" | jq '.data | length')

echo "返回的 MODULE 数量：${MENU_COUNT}"
echo ""

if [ "$MENU_COUNT" -gt 2 ]; then
    echo "✅ 修复成功！返回了 ${MENU_COUNT} 个 MODULE"
else
    echo "❌ 仍然只返回 ${MENU_COUNT} 个 MODULE，修复失败"
fi
echo ""

# 显示第一个 MODULE 的详细信息
echo "3. 第一个 MODULE 详情:"
curl -s "${BASE_URL}/api/resources/menu-tree" \
  -H "Authorization: Bearer ${TOKEN}" | \
  jq '.data[0] | {id, name, code, childrenCount: (.children | length)}'
echo ""

# 检查 children
echo "4. 检查 children:"
FIRST_MODULE_CHILDREN=$(curl -s "${BASE_URL}/api/resources/menu-tree" \
  -H "Authorization: Bearer ${TOKEN}" | \
  jq '.data[0].children | length')

if [ "$FIRST_MODULE_CHILDREN" -gt 0 ]; then
    echo "✅ 第一个 MODULE 有 ${FIRST_MODULE_CHILDREN} 个子 MENU"
    echo ""
    echo "子 MENU 列表:"
    curl -s "${BASE_URL}/api/resources/menu-tree" \
      -H "Authorization: Bearer ${TOKEN}" | \
      jq '.data[0].children[] | {id, name, code}'
else
    echo "❌ 第一个 MODULE 没有子 MENU"
fi
echo ""

echo "========================================"
echo "验证完成"
echo "========================================"
echo ""
echo "预期结果:"
echo "  - MODULE 数量 > 2"
echo "  - 每个 MODULE 有 children"
echo "  - children 包含 MENU"
echo ""
