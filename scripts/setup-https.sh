#!/bin/bash
#===============================================================================
# HTTPS 设置脚本 - 自动化申请和配置 Let's Encrypt 证书
# 使用：sudo ./scripts/setup-https.sh <域名> [邮箱]
#===============================================================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 配置
DEPLOY_DIR="/root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/deploy"
NGINX_SITES_AVAILABLE="/etc/nginx/sites-available"
NGINX_SITES_ENABLED="/etc/nginx/sites-enabled"

log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# 检查参数
if [ -z "$1" ]; then
    log_error "用法：$0 <域名> [邮箱]"
    log_error "示例：$0 camera1001.pipecos.cn admin@example.com"
    exit 1
fi

DOMAIN="$1"
EMAIL="${2:-}"

# 检查 root
if [ "$EUID" -ne 0 ]; then
    log_error "请使用 sudo 运行此脚本"
    exit 1
fi

log_info "开始设置 HTTPS"
log_info "域名：$DOMAIN"
[ -n "$EMAIL" ] && log_info "邮箱：$EMAIL"

# 1. 安装 Certbot
log_info "检查 Certbot..."
if ! command -v certbot &> /dev/null; then
    log_info "安装 Certbot..."
    apt update
    apt install -y certbot python3-certbot-nginx
    log_success "Certbot 安装完成"
else
    log_success "Certbot 已安装"
fi

# 2. 准备 Nginx 配置（HTTP 模式，用于证书验证）
log_info "准备 Nginx 配置..."
if [ -f "$DEPLOY_DIR/nginx-http.conf" ]; then
    # 临时修改 server_name
    sed "s/server_name .*/server_name $DOMAIN;/" "$DEPLOY_DIR/nginx-http.conf" > /tmp/nginx-http-temp.conf
    cp /tmp/nginx-http-temp.conf "$NGINX_SITES_AVAILABLE/camera-system"
    ln -sf "$NGINX_SITES_AVAILABLE/camera-system" "$NGINX_SITES_ENABLED/camera-system"
    nginx -t && systemctl reload nginx
    log_success "Nginx HTTP 配置完成"
fi

# 3. 申请证书
log_info "申请 Let's Encrypt 证书..."
CERTBOT_OPTS="--nginx -d $DOMAIN --non-interactive --agree-tos"

if [ -n "$EMAIL" ]; then
    CERTBOT_OPTS="$CERTBOT_OPTS --email $EMAIL"
else
    log_warning "未提供邮箱，Certbot 可能会提示输入"
fi

certbot $CERTBOT_OPTS --redirect

if [ $? -ne 0 ]; then
    log_error "证书申请失败"
    exit 1
fi

log_success "证书申请成功"

# 4. 切换到 HTTPS 配置
log_info "切换到 HTTPS 配置..."
if [ -f "$DEPLOY_DIR/nginx-https.conf" ]; then
    # 修改配置中的域名和证书路径
    sed -i "s|camera1001.pipecos.cn|$DOMAIN|g" "$DEPLOY_DIR/nginx-https.conf"
    sed -i "s|/etc/letsencrypt/live/camera1001.pipecos.cn|/etc/letsencrypt/live/$DOMAIN|g" "$DEPLOY_DIR/nginx-https.conf"
    
    cp "$DEPLOY_DIR/nginx-https.conf" "$NGINX_SITES_AVAILABLE/camera-https"
    
    # 移除旧配置
    rm -f "$NGINX_SITES_ENABLED/camera-system"
    
    # 启用新配置
    ln -sf "$NGINX_SITES_AVAILABLE/camera-https" "$NGINX_SITES_ENABLED/camera-https"
    
    # 测试并重启
    if nginx -t; then
        systemctl reload nginx
        log_success "HTTPS 配置已启用"
    else
        log_error "Nginx 配置测试失败"
        exit 1
    fi
fi

# 5. 验证
log_info "验证 HTTPS 配置..."
sleep 2
if curl -kI "https://$DOMAIN" &> /dev/null; then
    log_success "HTTPS 验证成功"
else
    log_warning "HTTPS 验证失败，请检查防火墙和 DNS 配置"
fi

# 6. 显示证书信息
log_info "证书信息:"
certbot certificates

echo ""
log_success "HTTPS 设置完成！"
echo ""
echo "访问地址："
echo "  HTTPS: https://$DOMAIN"
echo "  HTTP:  http://$DOMAIN (自动重定向到 HTTPS)"
echo ""
echo "证书有效期：90 天（自动续期）"
echo "手动续期命令：certbot renew"
echo ""
