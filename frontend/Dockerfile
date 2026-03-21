#===============================================================================
# 摄像头生命周期管理系统 - 前端 Dockerfile
# 基于 Node.js 构建 + Nginx 运行
#===============================================================================

#-------------------------------------------------------------------------------
# 阶段 1：构建
#-------------------------------------------------------------------------------
FROM node:20-alpine AS builder

WORKDIR /build

# 复制 package 文件（利用 Docker 缓存）
COPY package*.json ./

# 安装依赖（包括 devDependencies，因为构建需要 vite）
RUN npm ci && \
    echo "=== Checking vite installation ===" && \
    ls -la node_modules/.bin/vite && \
    ./node_modules/.bin/vite --version

# 复制源代码
COPY . .

# 构建参数（可通过 --build-arg 传递）
ARG VITE_API_BASE_URL=/api
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL

# 构建项目
RUN npm run build

#-------------------------------------------------------------------------------
# 阶段 2：运行（Nginx）
#-------------------------------------------------------------------------------
FROM nginx:alpine

# 复制自定义 Nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 从构建阶段复制构建产物
COPY --from=builder /build/dist /usr/share/nginx/html

# 创建日志目录
RUN mkdir -p /var/log/nginx && \
    chown -R nginx:nginx /var/log/nginx && \
    touch /var/log/nginx/access.log && \
    touch /var/log/nginx/error.log

# 暴露端口
EXPOSE 80

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost/ || exit 1

# Nginx 在前台运行
CMD ["nginx", "-g", "daemon off;"]
