#!/bin/bash
#===============================================================================
# 后端 Docker 镜像构建脚本
# 优化流程：宿主机编译 + Docker 复制（避免重复下载 Maven 依赖）
#===============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "📦 摄像头生命周期管理系统 - 后端 Docker 构建"
echo "============================================="
echo ""

# 步骤 1：在宿主机上编译（利用本地 Maven 缓存）
echo "🔨 步骤 1/3: 在宿主机上编译 Java 项目..."
echo "   （利用本地 Maven 缓存，避免重复下载依赖）"
echo ""

mvn clean package -DskipTests -B

# 检查编译结果
JAR_FILE=$(ls target/*.jar 2>/dev/null | grep -v sources | grep -v javadoc | head -1)
if [ -z "$JAR_FILE" ]; then
    echo "❌ 错误：未找到编译生成的 jar 文件"
    exit 1
fi

echo "✅ 编译完成：$JAR_FILE"
echo ""

# 步骤 2：构建 Docker 镜像
echo "🐳 步骤 2/3: 构建 Docker 镜像..."
echo ""

docker build -t camera-backend:latest .

echo "✅ Docker 镜像构建完成"
echo ""

# 步骤 3：显示镜像信息
echo "📊 步骤 3/3: 镜像信息"
echo ""
docker images camera-backend

echo ""
echo "============================================="
echo "✨ 构建成功！"
echo ""
echo "下一步操作："
echo "  启动服务：docker-compose up -d backend"
echo "  查看日志：docker-compose logs -f backend"
echo "  重启服务：docker-compose restart backend"
echo ""
