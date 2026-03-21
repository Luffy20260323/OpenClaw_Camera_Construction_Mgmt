# Docker 镜像同步指南

## 📦 需要的镜像列表

```yaml
postgres:16-alpine
redis:7-alpine
minio/minio:RELEASE.2024-01-01T16-36-33Z
nginx:alpine
node:20-alpine
```

---

## 方案一：手动推送（快速测试）

### 1. 注册阿里云账号
访问：https://cr.console.aliyun.com/

### 2. 创建命名空间
- 进入容器镜像服务
- 创建命名空间（如：`your-namespace`）

### 3. 创建镜像仓库
为每个镜像创建仓库：
- `postgres`
- `redis`
- `minio`
- `nginx`
- `node`

### 4. 本地推送镜像

```bash
# 登录阿里云
docker login registry.cn-hangzhou.aliyuncs.com
# 用户名：阿里云账号
# 密码：容器镜像服务密码

# 拉取官方镜像
docker pull postgres:16-alpine
docker pull redis:7-alpine
docker pull minio/minio:RELEASE.2024-01-01T16-36-33Z

# 重新打标签
docker tag postgres:16-alpine registry.cn-hangzhou.aliyuncs.com/your-namespace/postgres:16-alpine
docker tag redis:7-alpine registry.cn-hangzhou.aliyuncs.com/your-namespace/redis:7-alpine
docker tag minio/minio:RELEASE.2024-01-01T16-36-33Z registry.cn-hangzhou.aliyuncs.com/your-namespace/minio:RELEASE.2024-01-01T16-36-33Z

# 推送到阿里云
docker push registry.cn-hangzhou.aliyuncs.com/your-namespace/postgres:16-alpine
docker push registry.cn-hangzhou.aliyuncs.com/your-namespace/redis:7-alpine
docker push registry.cn-hangzhou.aliyuncs.com/your-namespace/minio:RELEASE.2024-01-01T16-36-33Z
```

### 5. 修改 docker-compose.yml

```yaml
services:
  postgres:
    image: registry.cn-hangzhou.aliyuncs.com/your-namespace/postgres:16-alpine
  redis:
    image: registry.cn-hangzhou.aliyuncs.com/your-namespace/redis:7-alpine
  minio:
    image: registry.cn-hangzhou.aliyuncs.com/your-namespace/minio:RELEASE.2024-01-01T16-36-33Z
```

---

## 方案二：GitHub Actions 自动同步（推荐）

### 创建工作流

创建 `.github/workflows/sync-docker-images.yml`：

```yaml
name: Sync Docker Images to ACR

on:
  workflow_dispatch:  # 手动触发
  schedule:
    - cron: '0 0 1 * *'  # 每月 1 号自动同步

jobs:
  sync-images:
    runs-on: ubuntu-latest
    steps:
      - name: Login to Docker Hub
        uses: docker/login-action@v3
        with:
          username: ${{ secrets.DOCKERHUB_USERNAME }}
          password: ${{ secrets.DOCKERHUB_TOKEN }}

      - name: Login to Aliyun ACR
        uses: docker/login-action@v3
        with:
          registry: registry.cn-hangzhou.aliyuncs.com
          username: ${{ secrets.ACR_USERNAME }}
          password: ${{ secrets.ACR_PASSWORD }}

      - name: Pull and Push postgres
        run: |
          docker pull postgres:16-alpine
          docker tag postgres:16-alpine registry.cn-hangzhou.aliyuncs.com/${{ secrets.ACR_NAMESPACE }}/postgres:16-alpine
          docker push registry.cn-hangzhou.aliyuncs.com/${{ secrets.ACR_NAMESPACE }}/postgres:16-alpine

      - name: Pull and Push redis
        run: |
          docker pull redis:7-alpine
          docker tag redis:7-alpine registry.cn-hangzhou.aliyuncs.com/${{ secrets.ACR_NAMESPACE }}/redis:7-alpine
          docker push registry.cn-hangzhou.aliyuncs.com/${{ secrets.ACR_NAMESPACE }}/redis:7-alpine

      - name: Pull and Push minio
        run: |
          docker pull minio/minio:RELEASE.2024-01-01T16-36-33Z
          docker tag minio/minio:RELEASE.2024-01-01T16-36-33Z registry.cn-hangzhou.aliyuncs.com/${{ secrets.ACR_NAMESPACE }}/minio:RELEASE.2024-01-01T16-36-33Z
          docker push registry.cn-hangzhou.aliyuncs.com/${{ secrets.ACR_NAMESPACE }}/minio:RELEASE.2024-01-01T16-36-33Z
```

### 配置 Secrets

在 GitHub 仓库设置中添加：
- `DOCKERHUB_USERNAME`: Docker Hub 用户名
- `DOCKERHUB_TOKEN`: Docker Hub Token
- `ACR_USERNAME`: 阿里云容器镜像服务用户名
- `ACR_PASSWORD`: 阿里云容器镜像服务密码
- `ACR_NAMESPACE`: 阿里云命名空间

---

## 方案三：使用国内镜像加速工具

### 使用 DaoCloud 镜像加速

```bash
# 配置 Docker daemon
cat > /etc/docker/daemon.json <<EOF
{
  "registry-mirrors": [
    "https://docker.m.daocloud.io",
    "https://docker.1panel.live"
  ]
}
EOF

# 重启 Docker
systemctl restart docker
```

**注意**：公共镜像加速器可能不稳定，建议长期使用方案一或二。

---

## 📊 推荐方案对比

| 方案 | 难度 | 稳定性 | 成本 | 推荐场景 |
|------|------|--------|------|----------|
| **手动推送** | ⭐⭐ | ⭐⭐⭐⭐⭐ | 免费 | 个人使用、快速测试 |
| **GitHub Actions** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | 免费 | 团队协作、长期维护 |
| **镜像加速器** | ⭐ | ⭐⭐ | 免费 | 临时使用、开发环境 |

---

## 🔧 快速开始（推荐手动推送）

### 1. 获取阿里云凭证
```bash
# 访问 https://cr.console.aliyun.com/
# 左侧菜单：访问凭证 → 固定密码
```

### 2. 执行推送脚本
```bash
#!/bin/bash
ALIYUN_USER="your-aliyun-username"
ALIYUN_PASS="your-aliyun-password"
ALIYUN_REGISTRY="registry.cn-hangzhou.aliyuncs.com"
NAMESPACE="your-namespace"

# 登录
docker login -u $ALIYUN_USER -p $ALIYUN_PASS $ALIYUN_REGISTRY

# 同步镜像
for image in "postgres:16-alpine" "redis:7-alpine" "nginx:alpine" "node:20-alpine"; do
    docker pull $image
    docker tag $image $ALIYUN_REGISTRY/$NAMESPACE/$image
    docker push $ALIYUN_REGISTRY/$NAMESPACE/$image
done

# MinIO 特殊处理
docker pull minio/minio:RELEASE.2024-01-01T16-36-33Z
docker tag minio/minio:RELEASE.2024-01-01T16-36-33Z $ALIYUN_REGISTRY/$NAMESPACE/minio:RELEASE.2024-01-01T16-36-33Z
docker push $ALIYUN_REGISTRY/$NAMESPACE/minio:RELEASE.2024-01-01T16-36-33Z
```

---

## ✅ 验证

```bash
# 删除本地镜像
docker rmi postgres:16-alpine

# 从阿里云拉取
docker pull registry.cn-hangzhou.aliyuncs.com/your-namespace/postgres:16-alpine

# 应该很快完成！
```

---

*最后更新：2026-03-21*
