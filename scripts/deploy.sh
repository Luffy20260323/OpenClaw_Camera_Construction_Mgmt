#!/bin/bash
#===============================================================================
# 摄像头生命周期管理系统 - 一键部署脚本
# 用途：自动化部署前端、后端和 Nginx 配置
# 使用：./scripts/deploy.sh [OPTIONS]
#===============================================================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置变量
PROJECT_DIR="/root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt"
FRONTEND_DIR="$PROJECT_DIR/frontend"
BACKEND_DIR="$PROJECT_DIR/backend"
DEPLOY_DIR="$PROJECT_DIR/deploy"
NGINX_WWW_DIR="/var/www/html"
NGINX_SITES_AVAILABLE="/etc/nginx/sites-available"
NGINX_SITES_ENABLED="/etc/nginx/sites-enabled"

# 日志函数
log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# 检查是否以 root 运行
check_root() {
    if [ "$EUID" -ne 0 ]; then
        log_error "请使用 sudo 运行此脚本"
        exit 1
    fi
}

# 检查系统依赖
check_dependencies() {
    log_info "检查系统依赖..."
    
    local deps=("nginx" "node" "npm" "java")
    local missing=()
    
    for dep in "${deps[@]}"; do
        if ! command -v "$dep" &> /dev/null; then
            missing+=("$dep")
        fi
    done
    
    if [ ${#missing[@]} -ne 0 ]; then
        log_warning "缺少依赖：${missing[*]}"
        log_info "运行以下命令安装：apt update && apt install -y ${missing[*]}"
        read -p "是否现在安装？(y/n) " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            apt update && apt install -y "${missing[@]}"
        else
            log_error "请先安装缺少的依赖"
            exit 1
        fi
    fi
    
    log_success "系统依赖检查完成"
}

# 构建前端
build_frontend() {
    log_info "构建前端..."
    cd "$FRONTEND_DIR"
    
    npm install
    npm run build
    
    # 复制构建产物到 Nginx 目录
    log_info "部署前端文件到 $NGINX_WWW_DIR..."
    rm -rf "$NGINX_WWW_DIR"/*
    cp -r dist/* "$NGINX_WWW_DIR"/
    
    log_success "前端构建完成"
}

# 构建后端
build_backend() {
    log_info "构建后端..."
    cd "$BACKEND_DIR"
    
    mvn clean package -DskipTests
    
    log_success "后端构建完成"
}

# 部署 Nginx 配置
deploy_nginx() {
    local mode="${1:-http}"  # http 或 https
    
    log_info "部署 Nginx 配置（模式：$mode）..."
    
    # 创建 snippets 目录
    mkdir -p /etc/nginx/snippets
    
    # 复制 SSL 参数配置
    if [ -f "$DEPLOY_DIR/nginx-ssl-params.conf" ]; then
        cp "$DEPLOY_DIR/nginx-ssl-params.conf" /etc/nginx/snippets/ssl-params.conf
        log_info "SSL 参数配置已复制"
    fi
    
    # 根据模式选择配置
    if [ "$mode" = "https" ]; then
        cp "$DEPLOY_DIR/nginx-https.conf" "$NGINX_SITES_AVAILABLE/camera-system"
        log_info "HTTPS 配置已复制"
    else
        cp "$DEPLOY_DIR/nginx-http.conf" "$NGINX_SITES_AVAILABLE/camera-system"
        log_info "HTTP 配置已复制"
    fi
    
    # 创建符号链接
    ln -sf "$NGINX_SITES_AVAILABLE/camera-system" "$NGINX_SITES_ENABLED/camera-system"
    
    # 测试配置
    if nginx -t; then
        log_success "Nginx 配置测试通过"
        systemctl reload nginx
        log_success "Nginx 已重新加载"
    else
        log_error "Nginx 配置测试失败"
        exit 1
    fi
}

# 设置 HTTPS
setup_https() {
    local domain="${1:-camera1001.pipecos.cn}"
    local email="${2:-}"
    
    log_info "设置 HTTPS（域名：$domain）..."
    
    # 安装 Certbot
    if ! command -v certbot &> /dev/null; then
        log_info "安装 Certbot..."
        apt update && apt install -y certbot python3-certbot-nginx
    fi
    
    # 准备邮箱
    local certbot_opts="--nginx -d $domain --non-interactive --agree-tos"
    if [ -n "$email" ]; then
        certbot_opts="$certbot_opts --email $email"
    else
        read -p "请输入邮箱地址（用于证书通知）: " email
        certbot_opts="$certbot_opts --email $email"
    fi
    
    # 申请证书
    log_info "申请 Let's Encrypt 证书..."
    certbot $certbot_opts --redirect
    
    if [ $? -eq 0 ]; then
        log_success "证书申请成功"
        
        # 切换到 HTTPS 配置
        deploy_nginx "https"
        
        log_success "HTTPS 设置完成"
        log_info "证书有效期：90 天（自动续期）"
    else
        log_error "证书申请失败"
        exit 1
    fi
}

# 启动后端服务
start_backend() {
    log_info "启动后端服务..."
    cd "$BACKEND_DIR"
    
    # 检查是否已运行
    if pgrep -f "camera-lifecycle-system" > /dev/null; then
        log_warning "后端服务已在运行，重启中..."
        pkill -f "camera-lifecycle-system" || true
        sleep 2
    fi
    
    # 启动
    nohup java -jar target/*.jar > /var/log/camera-backend.log 2>&1 &
    
    sleep 3
    
    if pgrep -f "camera-lifecycle-system" > /dev/null; then
        log_success "后端服务已启动"
    else
        log_error "后端服务启动失败，查看日志：/var/log/camera-backend.log"
        exit 1
    fi
}

# 显示使用帮助
show_help() {
    cat << EOF
摄像头生命周期管理系统 - 部署脚本

用法：$0 [选项]

选项:
    -a, --all           完整部署（前端 + 后端 + Nginx HTTP）
    -f, --frontend      仅部署前端
    -b, --backend       仅构建后端
    -n, --nginx         仅部署 Nginx 配置
    -s, --https         设置 HTTPS（需要域名和邮箱）
    -d, --domain        域名（与 -s 配合使用）
    -e, --email         邮箱（与 -s 配合使用）
    -h, --help          显示帮助信息

示例:
    $0 --all                          # 完整部署（HTTP）
    $0 --https -d example.com -e admin@example.com  # 设置 HTTPS
    $0 --frontend                     # 仅更新前端

EOF
}

# 主函数
main() {
    local action=""
    local domain=""
    local email=""
    
    # 解析参数
    while [[ $# -gt 0 ]]; do
        case $1 in
            -a|--all)
                action="all"
                shift
                ;;
            -f|--frontend)
                action="frontend"
                shift
                ;;
            -b|--backend)
                action="backend"
                shift
                ;;
            -n|--nginx)
                action="nginx"
                shift
                ;;
            -s|--https)
                action="https"
                shift
                ;;
            -d|--domain)
                domain="$2"
                shift 2
                ;;
            -e|--email)
                email="$2"
                shift 2
                ;;
            -h|--help)
                show_help
                exit 0
                ;;
            *)
                log_error "未知选项：$1"
                show_help
                exit 1
                ;;
        esac
    done
    
    # 检查 root
    check_root
    
    # 执行操作
    case $action in
        all)
            log_info "开始完整部署..."
            check_dependencies
            build_frontend
            build_backend
            deploy_nginx "http"
            start_backend
            log_success "完整部署完成！"
            log_info "访问地址：http://$(hostname -I | awk '{print $1}')"
            ;;
        frontend)
            build_frontend
            systemctl reload nginx
            log_success "前端部署完成"
            ;;
        backend)
            build_backend
            log_success "后端构建完成"
            ;;
        nginx)
            deploy_nginx "http"
            log_success "Nginx 配置完成"
            ;;
        https)
            if [ -z "$domain" ]; then
                log_error "设置 HTTPS 需要指定域名（-d）"
                exit 1
            fi
            setup_https "$domain" "$email"
            ;;
        *)
            show_help
            ;;
    esac
}

# 执行
main "$@"
