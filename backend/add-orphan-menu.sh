#!/bin/bash

# 执行孤儿资源管理菜单的 SQL 脚本
# 用法：./add-orphan-menu.sh

DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-5432}
DB_NAME=${DB_NAME:-camera_construction_db}
DB_USER=${DB_USERNAME:-postgres}
DB_PASSWORD=${DB_PASSWORD:-}

SQL_FILE="/root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/backend/src/main/resources/db/migration/V20260404__add_orphan_resource_menu.sql"

echo "========================================"
echo "添加孤儿资源管理菜单"
echo "========================================"
echo "数据库：${DB_NAME}"
echo "主机：${DB_HOST}:${DB_PORT}"
echo "用户：${DB_USER}"
echo "SQL 文件：${SQL_FILE}"
echo "========================================"

if [ ! -f "$SQL_FILE" ]; then
    echo "错误：SQL 文件不存在！"
    exit 1
fi

# 执行 SQL
export PGPASSWORD="${DB_PASSWORD}"
psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d "${DB_NAME}" -f "${SQL_FILE}"

if [ $? -eq 0 ]; then
    echo "========================================"
    echo "✅ SQL 执行成功！"
    echo "========================================"
    echo ""
    echo "下一步操作："
    echo "1. admin 用户重新登录"
    echo "2. 刷新菜单（点击右上角刷新按钮）"
    echo "3. 访问：系统管理 → 资源管理 → 孤儿资源管理"
    echo ""
else
    echo "========================================"
    echo "❌ SQL 执行失败！"
    echo "========================================"
    exit 1
fi
