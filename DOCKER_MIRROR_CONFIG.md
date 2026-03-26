# Docker 镜像加速器配置指南

## ✅ 当前配置

**阿里云企业用户镜像加速器**：
```
https://b1cyp3d8.mirror.aliyuncs.com
```

**配置文件**：`/etc/docker/daemon.json`

**状态**：✅ 已配置并生效

---

## 🔧 配置方法

### 方式一：修改 daemon.json（已配置）

```bash
cat > /etc/docker/daemon.json <<EOF
{
  "registry-mirrors": [
    "https://b1cyp3d8.mirror.aliyuncs.com",
    "https://docker.m.daocloud.io",
    "https://docker.1panel.live"
  ],
  "max-concurrent-downloads": 10,
  "max-download-attempts": 5
}
EOF

systemctl restart docker
```

### 方式二：使用阿里云脚本

```bash
# 阿里云官方配置脚本
curl -sSL https://get.daocloud.io/daotools/set_mirror.sh | sh -s https://b1cyp3d8.mirror.aliyuncs.com
```

### 方式三：手动配置（CentOS/RHEL）

```bash
# 创建配置文件
mkdir -p /etc/docker
cat > /etc/docker/daemon.json <<EOF
{
  "registry-mirrors": ["https://b1cyp3d8.mirror.aliyuncs.com"]
}
EOF

# 重载配置
systemctl daemon-reload
systemctl restart docker
```

---

## ✅ 验证配置

### 检查加速器是否生效

```bash
docker info | grep -A 5 "Registry Mirrors"
```

**输出示例**：
```
Registry Mirrors:
  https://b1cyp3d8.mirror.aliyuncs.com/
  https://docker.m.daocloud.io/
  https://docker.1panel.live/
```

### 测试拉取速度

```bash
# 删除本地镜像（如果有）
docker rmi nginx:latest

# 拉取镜像
time docker pull nginx:latest
```

---

## 📊 加速器对比

| 加速器 | 速度 | 稳定性 | 推荐度 |
|--------|------|--------|--------|
| **阿里云企业版** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| DaoCloud | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| 1Panel | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| 官方 Docker Hub | ⭐ | ⭐ | ❌ |

---

## 🎯 使用方法

### 配置后直接拉取（推荐）

```bash
# 配置加速器后，直接使用官方命令
docker pull nginx:latest
docker pull postgres:16-alpine
docker pull redis:7-alpine

# Docker 会自动使用加速器
```

### 查看拉取日志

```bash
docker pull nginx:latest
```

**输出示例**：
```
Trying to pull repository-1.docker.io...
Pulling from library/nginx
Digest: sha256:abc123...
Status: Downloaded newer image for nginx:latest
docker.io/library/nginx:latest
```

---

## ⚠️ 故障排查

### 问题 1：加速器不生效

```bash
# 检查配置
cat /etc/docker/daemon.json

# 重启 Docker
systemctl restart docker

# 验证
docker info | grep -A 5 "Registry Mirrors"
```

### 问题 2：拉取仍然超时

```bash
# 尝试多个加速器
cat > /etc/docker/daemon.json <<EOF
{
  "registry-mirrors": [
    "https://b1cyp3d8.mirror.aliyuncs.com",
    "https://docker.m.daocloud.io",
    "https://docker.1panel.live"
  ]
}
EOF
systemctl restart docker
```

### 问题 3：DNS 解析失败

```bash
# 修改 DNS
echo "nameserver 8.8.8.8" > /etc/resolv.conf
echo "nameserver 1.1.1.1" >> /etc/resolv.conf

# 重启 Docker
systemctl restart docker
```

---

## 📝 企业用户优势

作为阿里云企业用户，你的镜像加速器有以下优势：

### ✅ 专属加速器
- 独享带宽，不限速
- 更高的稳定性
- 优先技术支持

### ✅ 安全合规
- 通过安全扫描
- 符合企业合规要求
- 支持私有镜像仓库

### ✅ 成本优势
- 免费额度更高
- 企业折扣
- 按需付费

---

## 🔄 与其他方案对比

| 方案 | 速度 | 稳定性 | 成本 | 推荐场景 |
|------|------|--------|------|----------|
| **阿里云加速器** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | 免费 | 企业用户 ✅ |
| 公共代理 | ⭐⭐⭐ | ⭐⭐⭐ | 免费 | 临时使用 |
| 阿里云 ACR 推送 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | 免费 500MB | 生产环境 |
| GitHub Actions | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | 免费 | 自动化 CI |

---

## 💡 最佳实践

### 开发环境
```bash
# 使用加速器直接拉取
docker pull postgres:16-alpine
```

### 生产环境
```bash
# 推送到私有仓库 + 加速器拉取
docker push registry.cn-hangzhou.aliyuncs.com/mycompany/postgres:16-alpine
docker pull registry.cn-hangzhou.aliyuncs.com/mycompany/postgres:16-alpine
```

### CI/CD
```yaml
# GitHub Actions 使用加速器
- name: Pull images
  run: |
    docker pull postgres:16-alpine
    docker pull redis:7-alpine
```

---

## 📞 技术支持

- 阿里云工单：https://workorder.console.aliyun.com/
- 企业客服：95187 转 1
- 文档中心：https://help.aliyun.com/product/60278.html

---

**配置时间**: 2026-03-21 23:20  
**配置状态**: ✅ 已完成  
**加速器地址**: https://b1cyp3d8.mirror.aliyuncs.com
