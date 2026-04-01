#===============================================================================
# 摄像头生命周期管理系统 - 前端 Dockerfile
# 优化版：直接从宿主机复制已构建的 dist（避免每次重新下载 npm 依赖）
#===============================================================================
# 使用说明：
# 1. 在宿主机上先构建：cd frontend && npm install && npm run build
# 2. 然后构建 Docker：docker build -t camera-frontend .
# 3. 或使用 docker-compose：docker-compose build frontend
#===============================================================================

FROM nginx:alpine

# 直接写入 Nginx 配置
RUN echo 'server { \
    listen 80; \
    server_name localhost; \
    root /usr/share/nginx/html; \
    index index.html; \
    access_log /var/log/nginx/access.log; \
    error_log /var/log/nginx/error.log; \
    location / { try_files $uri $uri/ /index.html; } \
    location /api/ { \
        proxy_pass http://camera-backend:8080/api/; \
        proxy_http_version 1.1; \
        proxy_set_header Host $host; \
        proxy_set_header X-Real-IP $remote_addr; \
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for; \
    } \
    location /health { return 200 "healthy\n"; } \
}' > /etc/nginx/conf.d/default.conf

# 从宿主机复制已构建的 dist 目录（构建上下文应包含 dist/）
COPY dist /usr/share/nginx/html

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
