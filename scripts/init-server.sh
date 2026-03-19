#!/bin/bash
#===============================================================================
# 服务器初始化脚本 - 新服务器首次部署使用
# 使用：curl -sSL <script-url> | sudo bash
# 或：wget -O - <script-url> | sudo bash
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
echo "  摄像头生命周期管理系统 - 服务器初始化"
echo "=========================================="
echo ""

# 检查 root
if [ "$EUID" -ne 0 ]; then
    log_error "请使用 sudo 运行此脚本"
    exit 1
fi

# 1. 系统更新
log_info "更新系统..."
apt update && apt upgrade -y

# 2. 安装基础依赖
log_info "安装基础依赖..."
apt install -y \
    nginx \
    nodejs \
    npm \
    openjdk-17-jdk \
    maven \
    postgresql \
    postgresql-contrib \
    redis-server \
    git \
    curl \
    wget \
    certbot \
    python3-certbot-nginx \
    ufw

log_success "基础依赖安装完成"

# 3. 配置防火墙
log_info "配置防火墙..."
ufw --force enable
ufw allow 22/tcp    # SSH
ufw allow 80/tcp    # HTTP
ufw allow 443/tcp   # HTTPS
log_success "防火墙配置完成"

# 4. 配置 PostgreSQL
log_info "配置 PostgreSQL..."
systemctl enable postgresql
systemctl start postgresql
sudo -u postgres psql -c "ALTER USER postgres PASSWORD 'postgres';" || true
sudo -u postgres createdb camera_construction_db || true
log_success "PostgreSQL 配置完成"

# 5. 配置 Redis
log_info "配置 Redis..."
systemctl enable redis-server
systemctl start redis-server
log_success "Redis 配置完成"

# 6. 创建部署目录
log_info "创建部署目录..."
mkdir -p /var/www/html
mkdir -p /root/.openclaw/workspace
chown -R www-data:www-data /var/www/html
log_success "目录创建完成"

# 7. 克隆项目（如果未提供则跳过）
log_info "准备项目代码..."
if [ -d "/root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt" ]; then
    log_warning "项目已存在，跳过克隆"
else
    read -p "请输入 GitHub 仓库地址（留空跳过）: " REPO_URL
    if [ -n "$REPO_URL" ]; then
        git clone "$REPO_URL" /root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt
        log_success "项目克隆完成"
    fi
fi

# 8. 显示下一步操作
echo ""
log_success "服务器初始化完成！"
echo ""
echo "下一步操作："
echo "1. 克隆项目代码（如果未自动克隆）"
echo "   git clone <your-repo-url> /root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt"
echo ""
echo "2. 进入项目目录"
echo "   cd /root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt"
echo ""
echo "3. 运行部署脚本"
echo "   sudo ./scripts/deploy.sh --all"
echo ""
echo "4. 设置 HTTPS（可选）"
echo "   sudo ./scripts/setup-https.sh your-domain.com your-email@example.com"
echo ""
echo "5. 查看服务状态"
echo "   systemctl status nginx"
echo "   systemctl status postgresql"
echo "   systemctl status redis-server"
echo ""
