#!/bin/bash
#===============================================================================
# 前端 Docker 镜像构建脚本
# 优化流程：宿主机编译 + Docker 复制（避免重复下载 npm 依赖）
#===============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "📦 摄像头生命周期管理系统 - 前端 Docker 构建"
echo "============================================="
echo ""

# 步骤 1：在宿主机上构建（利用本地 npm 缓存）
echo "🔨 步骤 1/3: 在宿主机上构建 Vue 项目..."
echo "   （利用本地 npm 缓存，避免重复下载依赖）"
echo ""

# 检查 node_modules 是否存在，不存在则安装
if [ ! -d "node_modules" ]; then
    echo "📥 首次构建，安装依赖..."
    npm install
else
    echo "✅ node_modules 已存在，跳过依赖安装"
fi

# 构建项目
npm run build

# 检查构建结果
if [ ! -d "dist" ]; then
    echo "❌ 错误：未找到构建输出的 dist 目录"
    exit 1
fi

echo "✅ 构建完成：dist/"
echo ""

# 步骤 2：构建 Docker 镜像
echo "🐳 步骤 2/3: 构建 Docker 镜像..."
echo ""

docker build -t camera-frontend:latest .

echo "✅ Docker 镜像构建完成"
echo ""

# 步骤 3：显示镜像信息
echo "📊 步骤 3/3: 镜像信息"
echo ""
docker images camera-frontend

echo ""
echo "============================================="
echo "✨ 构建成功！"
echo ""
echo "下一步操作："
echo "  启动服务：docker-compose up -d frontend"
echo "  查看日志：docker-compose logs -f frontend"
echo "  重启服务：docker-compose restart frontend"
echo ""
