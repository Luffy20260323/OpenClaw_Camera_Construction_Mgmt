#!/bin/bash

# =====================================================
# 日志系统测试脚本
# =====================================================

BASE_URL="http://localhost:8080"
TOKEN=""

echo "========================================"
echo "日志分级系统测试"
echo "========================================"
echo ""

# 获取 admin token
echo "1. 获取 admin token..."
TOKEN=$(curl -s -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | \
  grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo "❌ 获取 token 失败，请检查 admin 用户密码"
    exit 1
fi
echo "✅ Token 获取成功"
echo ""

# 查看当前配置
echo "2. 查看当前日志配置..."
curl -s "${BASE_URL}/api/admin/logs/config" \
  -H "Authorization: Bearer ${TOKEN}" | jq '.'
echo ""

# 启用 DEBUG 模式
echo "3. 启用 DEBUG 模式..."
curl -s -X POST "${BASE_URL}/api/admin/logs/debug/enable" \
  -H "Authorization: Bearer ${TOKEN}" | jq '.'
echo ""

# 为 admin 用户设置会话日志
echo "4. 为 admin 用户设置会话日志级别..."
curl -s -X PUT "${BASE_URL}/api/admin/logs/level/session?userId=1&level=DEBUG&expireMinutes=60" \
  -H "Authorization: Bearer ${TOKEN}" | jq '.'
echo ""

# 设置包日志级别
echo "5. 设置关键包日志级别..."
curl -s -X PUT "${BASE_URL}/api/admin/logs/level/package?packageName=com.qidian.camera.module.auth&level=DEBUG" \
  -H "Authorization: Bearer ${TOKEN}" | jq '.'

curl -s -X PUT "${BASE_URL}/api/admin/logs/level/package?packageName=com.qidian.camera.module.menu&level=DEBUG" \
  -H "Authorization: Bearer ${TOKEN}" | jq '.'
echo ""

# 查看更新后的配置
echo "6. 查看更新后的配置..."
curl -s "${BASE_URL}/api/admin/logs/config" \
  -H "Authorization: Bearer ${TOKEN}" | jq '.data.globalLogLevel, .data.sessionConfigs, .data.packageConfigs'
echo ""

# 测试菜单 API
echo "7. 测试菜单 API（应产生 DEBUG 日志）..."
curl -s "${BASE_URL}/api/resources/menu-tree" \
  -H "Authorization: Bearer ${TOKEN}" | jq '.data | length'
echo ""

# 查看日志文件
echo "8. 查看最近的菜单日志..."
LOG_FILE="/root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/backend/logs/menu.log"
if [ -f "$LOG_FILE" ]; then
    echo "最近 10 行菜单日志:"
    tail -10 "$LOG_FILE"
else
    echo "⚠️  日志文件不存在，等待日志生成..."
    sleep 2
    if [ -f "$LOG_FILE" ]; then
        tail -10 "$LOG_FILE"
    else
        echo "❌ 日志文件仍未生成"
    fi
fi
echo ""

# 清理
echo "9. 清理配置..."
curl -s -X POST "${BASE_URL}/api/admin/logs/cleanup" \
  -H "Authorization: Bearer ${TOKEN}" | jq '.'
echo ""

echo "========================================"
echo "测试完成！"
echo "========================================"
echo ""
echo "查看实时日志:"
echo "  tail -f /root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/backend/logs/menu.log"
echo ""
echo "禁用 DEBUG 模式:"
echo "  curl -X POST ${BASE_URL}/api/admin/logs/debug/disable -H \"Authorization: Bearer ${TOKEN}\""
echo ""
