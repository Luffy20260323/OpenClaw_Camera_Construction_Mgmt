#!/bin/bash
echo "🚀 开始部署前端..."

# 构建
npm run build

# 复制到 nginx 目录
sudo cp -r dist/* /var/www/html/

# 重新加载 nginx
sudo nginx -s reload

echo "✅ 部署完成！"
