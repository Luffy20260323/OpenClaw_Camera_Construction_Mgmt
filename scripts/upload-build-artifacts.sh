#!/bin/bash
#===============================================================================
# 自动上传本地编译产物到 GitHub
# 使用说明：
# 1. 本地编译完成后运行此脚本
# 2. 自动将编译产物添加到 git 跟踪
# 3. 创建独立提交并推送
#===============================================================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查是否在正确的目录
if [ ! -f "pom.xml" ] && [ ! -f "backend/pom.xml" ]; then
    log_error "请在项目根目录运行此脚本"
    exit 1
fi

log_info "=========================================="
log_info "  上传本地编译产物到 GitHub"
log_info "=========================================="
echo ""

# 1. 检查 Git 状态
log_info "步骤 1: 检查 Git 状态..."
git status --short

# 2. 识别编译产物
log_info ""
log_info "步骤 2: 识别编译产物..."

# 后端编译产物
if [ -d "backend/target" ]; then
    log_info "发现后端编译产物：backend/target/"
    BACKEND_SIZE=$(du -sh backend/target/ 2>/dev/null | cut -f1)
    log_info "后端编译产物大小：$BACKEND_SIZE"
fi

# 前端编译产物
if [ -d "frontend/dist" ]; then
    log_info "发现前端编译产物：frontend/dist/"
    FRONTEND_SIZE=$(du -sh frontend/dist/ 2>/dev/null | cut -f1)
    log_info "前端编译产物大小：$FRONTEND_SIZE"
fi

# 前端 node_modules (通常不上传，但可以记录)
if [ -d "frontend/node_modules" ]; then
    log_warning "发现 frontend/node_modules/ (通常不上传到 Git)"
fi

# Maven 依赖 (通常不上传)
if [ -d "$HOME/.m2/repository" ]; then
    log_warning "发现 Maven 仓库 (通常不上传到 Git)"
fi

echo ""

# 3. 检查 .gitignore 配置
log_info "步骤 3: 检查 .gitignore 配置..."
if [ -f ".gitignore" ]; then
    if grep -q "target/" .gitignore && grep -q "dist/" .gitignore; then
        log_warning ".gitignore 中包含 target/ 和 dist/"
        log_warning "这些目录通常不应该上传到 Git"
        echo ""
        read -p "是否继续强制上传？(y/N): " -n 1 -r
        echo ""
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            log_info "已取消上传"
            exit 0
        fi
    fi
fi

echo ""

# 4. 添加编译产物到 Git
log_info "步骤 4: 添加编译产物到 Git..."

# 检查是否有要添加的文件
if [ -d "backend/target" ]; then
    log_info "添加后端编译产物..."
    git add -f backend/target/ 2>/dev/null || log_warning "backend/target/ 已添加或不存在"
fi

if [ -d "frontend/dist" ]; then
    log_info "添加前端编译产物..."
    git add -f frontend/dist/ 2>/dev/null || log_warning "frontend/dist/ 已添加或不存在"
fi

echo ""

# 5. 检查 Git 状态
log_info "步骤 5: 检查待提交的文件..."
git status --short

# 如果没有要提交的文件
if [ -z "$(git status --short)" ]; then
    log_warning "没有发现新的编译产物需要上传"
    exit 0
fi

echo ""

# 6. 创建提交
log_info "步骤 6: 创建提交..."
COMMIT_MSG="build: 上传本地编译产物 - $(date '+%Y-%m-%d %H:%M:%S')"
git commit -m "$COMMIT_MSG"
log_success "提交创建成功：$COMMIT_MSG"

echo ""

# 7. 推送到 GitHub
log_info "步骤 7: 推送到 GitHub..."
CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
log_info "当前分支：$CURRENT_BRANCH"

git push origin $CURRENT_BRANCH

log_success "推送成功！"

echo ""
log_info "=========================================="
log_success "  编译产物上传完成！"
log_info "=========================================="
echo ""
log_info "请注意："
log_info "1. 编译产物通常较大，会占用 Git 仓库空间"
log_info "2. 建议使用 Git LFS 管理大文件"
log_info "3. 考虑使用 GitHub Actions 自动上传 artifact"
echo ""
