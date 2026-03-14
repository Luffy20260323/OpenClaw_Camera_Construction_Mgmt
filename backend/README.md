# 摄像头生命周期管理系统 - 后端

> 基于 Spring Boot 3.2 + Java 17 的企业级项目管理系统

## 📋 项目简介

摄像头生命周期管理系统是一个聚焦**项目交付过程管理**和**最终结算管理**的多角色协作平台。系统支持甲方、乙方、监理三方协作，实现项目从开工到施工、验收、结算、运维的全生命周期管理。

## 🛠️ 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.4 | 后端框架 |
| Java | 17 | 开发语言 |
| MyBatis-Plus | 3.5.5 | ORM 框架 |
| PostgreSQL | 16.x | 数据库 |
| Redis | 7.x | 缓存 |
| MinIO | 8.5.9 | 文件存储 |
| JWT | 0.12.5 | 认证令牌 |
| Knife4j | 4.4.0 | API 文档 |

## 📁 项目结构

```
camera-lifecycle-system/
├── src/main/java/com/qidian/camera/
│   ├── CameraApplication.java          # 启动类
│   ├── config/                         # 配置类
│   │   ├── MybatisPlusConfig.java
│   │   ├── RedisConfig.java
│   │   └── SwaggerConfig.java
│   ├── module/                         # 业务模块
│   │   ├── auth/                       # 认证授权
│   │   ├── user/                       # 用户管理
│   │   ├── project/                    # 项目管理
│   │   └── ...                         # 其他模块
│   ├── common/                         # 公共模块
│   │   ├── response/                   # 统一响应
│   │   ├── exception/                  # 异常处理
│   │   └── constants/                  # 常量定义
│   └── infrastructure/                 # 基础设施
│       ├── entity/                     # 实体类
│       ├── dto/                        # 数据传输对象
│       └── mapper/                     # Mapper 接口
├── src/main/resources/
│   ├── application.yml                 # 主配置文件
│   ├── application-dev.yml             # 开发环境
│   └── application-prod.yml            # 生产环境
├── pom.xml                             # Maven 配置
└── README.md                           # 项目说明
```

## 🚀 快速开始

### 环境要求

- JDK 17+
- Maven 3.9+
- PostgreSQL 16+
- Redis 7+
- MinIO（可选）

### 安装步骤

1. **克隆项目**
```bash
git clone <repository-url>
cd camera-lifecycle-system
```

2. **配置数据库**

编辑 `src/main/resources/application-dev.yml`：
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/camera_construction_db
    username: camera_admin
    password: camera_admin_password
```

3. **配置 Redis**

编辑 `src/main/resources/application-dev.yml`：
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

4. **编译项目**
```bash
mvn clean install
```

5. **运行项目**
```bash
mvn spring-boot:run
```

6. **访问 API 文档**

启动成功后，访问：http://localhost:8080/api/doc.html

## 📖 API 文档

启动项目后，访问 Knife4j API 文档：
- **开发环境**：http://localhost:8080/api/doc.html
- **生产环境**：http://your-domain.com/api/doc.html

## 🔧 配置说明

### 环境切换

通过 `spring.profiles.active` 切换环境：

```bash
# 开发环境
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 生产环境
java -jar target/camera-lifecycle-system-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod
```

### 关键配置

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| server.port | 服务端口 | 8080 |
| spring.datasource.url | 数据库连接 | jdbc:postgresql://localhost:5432/camera_construction_db |
| spring.data.redis.host | Redis 主机 | localhost |
| jwt.secret | JWT 密钥 | camera-lifecycle-system-secret-key-2026 |
| jwt.expiration | JWT 过期时间 | 86400000 (24 小时) |

## 📊 数据库

数据库脚本位于：`/home/ubuntu/.openclaw/workspace/database/001_init_database.sql`

执行脚本初始化数据库：
```bash
sudo -u postgres psql -d camera_construction_db -f 001_init_database.sql
```

## 🧪 测试

运行单元测试：
```bash
mvn test
```

运行集成测试：
```bash
mvn verify
```

## 📝 开发规范

### 代码风格

- 遵循阿里巴巴 Java 开发手册
- 使用 Lombok 简化代码
- 统一使用 Result 封装响应结果

### Git 提交规范

```
feat: 新功能
fix: 修复 bug
docs: 文档更新
style: 代码格式调整
refactor: 重构代码
test: 测试相关
chore: 构建/工具链相关
```

## 📄 许可证

Apache License 2.0

## 👥 开发团队

北京其点技术服务有限公司

## 📞 联系方式

- 邮箱：support@qidian.com
- 官网：https://www.qidian.com

---

**版本**：v1.0.0  
**最后更新**：2026-03-10
