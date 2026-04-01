#===============================================================================
# 摄像头生命周期管理系统 - 后端 Dockerfile
# 优化版：直接从宿主机复制已编译的 jar 包（避免每次重新下载 Maven 依赖）
#===============================================================================
# 使用说明：
# 1. 在宿主机上先编译：cd backend && mvn clean package -DskipTests
# 2. 然后构建 Docker：docker build -t camera-backend .
# 3. 或使用 docker-compose：docker-compose build backend
#===============================================================================

FROM eclipse-temurin:17-jre-alpine

# 设置工作目录
WORKDIR /app

# 创建非 root 用户（安全最佳实践）
RUN addgroup -g 1001 appgroup && \
    adduser -u 1001 -G appgroup -D appuser

# 从宿主机复制已编译的 jar 包（假设构建上下文包含 target/*.jar）
# 如果 jar 包名称固定，可以指定具体名称；否则使用通配符
COPY target/*.jar app.jar

# 创建日志目录
RUN mkdir -p /var/log && chown -R appuser:appgroup /var/log

# 切换到非 root 用户
USER appuser

# 暴露端口
EXPOSE 8080

# JVM 参数优化
ENV JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
