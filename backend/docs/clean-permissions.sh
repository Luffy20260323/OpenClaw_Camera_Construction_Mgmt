#!/bin/bash

# Camera 项目权限数据清理脚本
# 用法：./clean-permissions.sh

set -e

# 数据库配置
DB_HOST="localhost"
DB_PORT="5432"
DB_NAME="camera_construction_db"
DB_USER="postgres"
DB_PASSWORD="uBUu8wZ50COo/1bhJwBpAIP/PKOFKutUnqdGH1dp3yw="

echo "========================================"
echo "Camera 项目权限数据清理"
echo "========================================"
echo ""
echo "⚠️  警告：此操作将清空以下表的数据："
echo "   - permissions"
echo "   - api_permissions"
echo "   - ui_element_permissions"
echo "   - role_permissions"
echo "   - role_menu_permissions"
echo ""
echo "数据库：${DB_NAME}"
echo ""

# 询问确认
read -p "确定要继续吗？(y/N): " -n 1 -r
echo ""
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "❌ 操作已取消"
    exit 0
fi

echo ""
echo "🗑️  开始清理数据..."
echo ""

PGPASSWORD="${DB_PASSWORD}" psql -h ${DB_HOST} -p ${DB_PORT} -U ${DB_USER} -d ${DB_NAME} <<EOF
-- 清空权限相关表（使用 TRUNCATE CASCADE 级联删除）
TRUNCATE TABLE role_menu_permissions RESTART IDENTITY CASCADE;
TRUNCATE TABLE role_permissions RESTART IDENTITY CASCADE;
TRUNCATE TABLE ui_element_permissions RESTART IDENTITY CASCADE;
TRUNCATE TABLE api_permissions RESTART IDENTITY CASCADE;
TRUNCATE TABLE permissions RESTART IDENTITY CASCADE;

-- 验证清空结果
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
echo "✅ 数据清理完成！"
echo "========================================"
echo ""
echo "现在可以执行 permissions.sql 脚本："
echo "./execute-permissions.sh"
