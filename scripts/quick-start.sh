#!/bin/bash
#===============================================================================
# 摄像头生命周期管理系统 - 快速启动脚本（Docker）
# 使用：./scripts/quick-start.sh
#===============================================================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

echo "=========================================="
echo "  摄像头生命周期管理系统 - 快速启动"
echo "=========================================="
echo ""

# 检查 Docker
if ! command -v docker &> /dev/null; then
    log_error "未找到 Docker，请先安装 Docker"
    exit 1
fi

if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    log_error "未找到 Docker Compose，请先安装 Docker Compose"
    exit 1
fi

# 检查 .env 文件
if [ ! -f ".env" ]; then
    log_info "创建 .env 配置文件..."
    cp .env.example .env
    
    # 生成随机 JWT 密钥
    JWT_SECRET=$(openssl rand -base64 32 2>/dev/null || head -c 32 /dev/urandom | base64)
    sed -i "s/^JWT_SECRET=.*/JWT_SECRET=$JWT_SECRET/" .env
    
    log_warning "请编辑 .env 文件修改以下配置："
    log_warning "  - DB_PASSWORD (数据库密码)"
    log_warning "  - MINIO_ACCESS_KEY 和 MINIO_SECRET_KEY (MinIO 密钥)"
    echo ""
    read -p "是否现在编辑 .env 文件？(y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        ${EDITOR:-nano} .env
    fi
fi

# 检查必要配置
log_info "检查配置..."
if grep -q "DB_PASSWORD=YourSecurePassword123!" .env; then
    log_warning "数据库密码仍为默认值，建议修改"
fi

if grep -q "JWT_SECRET=your-super-secret-jwt-key-change-this-in-production" .env; then
    log_warning "JWT 密钥仍为默认值，建议修改"
fi

# 启动服务
log_info "启动 Docker 服务..."
docker-compose up -d

# 等待服务启动
echo ""
log_info "等待服务启动..."
sleep 10

# 检查服务状态
log_info "检查服务状态..."
docker-compose ps

echo ""
log_success "系统启动完成！"
echo ""
echo "访问地址："
echo "  - 前端：http://localhost"
echo "  - 后端 API：http://localhost:8080/api"
echo "  - API 文档：http://localhost:8080/api/doc.html"
echo "  - MinIO 控制台：http://localhost:9001"
echo ""
echo "默认账号："
echo "  - 用户名：admin"
echo "  - 密码：Admin@2026"
echo ""
echo "查看日志：docker-compose logs -f"
echo "停止服务：docker-compose down"
echo ""
