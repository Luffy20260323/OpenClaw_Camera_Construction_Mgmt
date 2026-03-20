#===============================================================================
# 摄像头生命周期管理系统 - 后端 Dockerfile
# 基于 Maven 的多阶段构建
#===============================================================================

#-------------------------------------------------------------------------------
# 阶段 1：构建
#-------------------------------------------------------------------------------
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /build

# 复制 Maven 配置文件（利用 Docker 缓存）
COPY pom.xml .

# 下载依赖（如果 pom.xml 未变化，使用缓存）
RUN mvn dependency:go-offline -B

# 复制源代码
COPY src ./src

# 构建项目（跳过测试）
RUN mvn clean package -DskipTests -B

#-------------------------------------------------------------------------------
# 阶段 2：运行
#-------------------------------------------------------------------------------
FROM eclipse-temurin:17-jre-alpine

# 设置工作目录
WORKDIR /app

# 创建非 root 用户（安全最佳实践）
RUN addgroup -g 1001 appgroup && \
    adduser -u 1001 -G appgroup -D appuser

# 从构建阶段复制 jar 包
COPY --from=builder /build/target/*.jar app.jar

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
