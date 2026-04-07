#!/bin/bash

# =====================================================
# 日志系统手动测试（需要输入正确密码）
# =====================================================

BASE_URL="http://localhost:8080"

echo "========================================"
echo "日志系统测试"
echo "========================================"
echo ""
echo "请输入 admin 用户的密码:"
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
    echo "❌ 获取 token 失败，请检查密码"
    exit 1
fi
echo "✅ Token 获取成功"
echo ""

# 查看当前配置
echo "2. 查看当前日志配置..."
curl -s "${BASE_URL}/api/admin/logs/config" \
  -H "Authorization: Bearer ${TOKEN}" | jq '.data.globalLogLevel'
echo ""

# 启用 DEBUG 模式
echo "3. 启用 DEBUG 模式..."
curl -s -X POST "${BASE_URL}/api/admin/logs/debug/enable" \
  -H "Authorization: Bearer ${TOKEN}" | jq '.data.globalLogLevel, .data.message'
echo ""

# 为 admin 用户设置会话日志
echo "4. 为 admin 用户设置会话日志级别..."
curl -s -X PUT "${BASE_URL}/api/admin/logs/level/session?userId=1&level=DEBUG&expireMinutes=60" \
  -H "Authorization: Bearer ${TOKEN}" | jq '.data.message'
echo ""

# 测试菜单 API
echo "5. 测试菜单 API..."
RESULT=$(curl -s "${BASE_URL}/api/resources/menu-tree" \
  -H "Authorization: Bearer ${TOKEN}" | jq '.data | length')
echo "菜单数量：${RESULT}"
echo ""

# 查看日志
echo "6. 查看最近的菜单日志..."
LOG_FILE="/root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/backend/logs/menu.log"
if [ -f "$LOG_FILE" ]; then
    echo "最近 10 行菜单日志:"
    tail -10 "$LOG_FILE"
else
    echo "⚠️  日志文件不存在，等待 5 秒后重试..."
    sleep 5
    if [ -f "$LOG_FILE" ]; then
        tail -10 "$LOG_FILE"
    else
        echo "❌ 日志文件仍未生成"
        echo ""
        echo "查看容器日志:"
        docker logs camera-backend --tail 30 2>&1 | grep -i "menu\|debug" | tail -10
    fi
fi
echo ""

echo "========================================"
echo "测试完成！"
echo "========================================"
echo ""
echo "查看实时日志:"
echo "  docker logs -f camera-backend 2>&1 | grep -i menu"
echo ""
echo "或查看日志文件:"
echo "  tail -f /root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/backend/logs/menu.log"
echo ""
echo "禁用 DEBUG 模式:"
echo "  curl -X POST ${BASE_URL}/api/admin/logs/debug/disable -H \"Authorization: Bearer <token>\""
echo ""
