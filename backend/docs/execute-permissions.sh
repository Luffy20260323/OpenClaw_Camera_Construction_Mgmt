#!/bin/bash

# Camera 项目权限 SQL 脚本执行脚本
# 用法：./execute-permissions.sh

set -e

# 数据库配置
DB_HOST="localhost"
DB_PORT="5432"
DB_NAME="camera_construction_db"
DB_USER="postgres"
DB_PASSWORD="uBUu8wZ50COo/1bhJwBpAIP/PKOFKutUnqdGH1dp3yw="

# SQL 脚本路径
SQL_SCRIPT="/root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/backend/docs/permissions.sql"

echo "========================================"
echo "Camera 项目权限 SQL 脚本执行"
echo "========================================"
echo ""
echo "数据库：${DB_NAME}"
echo "SQL 脚本：${SQL_SCRIPT}"
echo ""

# 检查 SQL 脚本是否存在
if [ ! -f "$SQL_SCRIPT" ]; then
    echo "❌ 错误：SQL 脚本不存在！"
    exit 1
fi

echo "✅ SQL 脚本存在"
echo ""

# 执行 SQL 脚本
echo "🚀 开始执行 SQL 脚本..."
echo ""

PGPASSWORD="${DB_PASSWORD}" psql -h ${DB_HOST} -p ${DB_PORT} -U ${DB_USER} -d ${DB_NAME} -f "${SQL_SCRIPT}"

echo ""
echo "========================================"
echo "✅ SQL 脚本执行完成！"
echo "========================================"
echo ""

# 验证执行结果
echo "📊 验证执行结果..."
echo ""

PGPASSWORD="${DB_PASSWORD}" psql -h ${DB_HOST} -p ${DB_PORT} -U ${DB_USER} -d ${DB_NAME} <<EOF
SELECT 'permissions' AS table_name, COUNT(*) AS count FROM permissions
UNION ALL
SELECT 'api_permissions', COUNT(*) FROM api_permissions
UNION ALL
SELECT 'ui_element_permissions', COUNT(*) FROM ui_element_permissions
UNION ALL
SELECT 'role_permissions', COUNT(*) FROM role_permissions
UNION ALL
SELECT 'role_menu_permissions', COUNT(*) FROM role_menu_permissions;
EOF

echo ""
echo "========================================"
echo "🎉 验证完成！"
echo "========================================"
