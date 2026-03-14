#!/bin/bash
set -e

echo "🚀 开始部署前端..."

PROJECT_DIR="/var/www/Camera_Construction_Project_Mgmt"
FRONTEND_DIR="$PROJECT_DIR/frontend"

cd "$FRONTEND_DIR"

# 构建
echo "📦 构建前端..."
npm run build

# 重新加载 nginx
echo "🔄 重新加载 Nginx..."
sudo nginx -s reload

echo "✅ 部署完成！"
