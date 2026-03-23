# OpenClaw Camera Construction Management

飞书相机施工管理项目

## 项目说明

本项目用于管理和追踪相机施工进度，通过 OpenClaw 平台与飞书集成实现自动化管理。

## 功能特性

- 📷 相机施工状态追踪
- 📋 进度管理和报告
- 🔔 飞书通知集成
- 🤖 OpenClaw 自动化工作流

## 技术栈

- OpenClaw Platform
- Feishu (飞书) API
- GitHub Actions (CI/CD)

## 快速开始

### 环境要求

- Node.js >= 18
- OpenClaw CLI
- 飞书开发者账号

### 安装

```bash
git clone https://github.com/Luffy20260323/OpenClaw_Camera_Construction_Mgmt.git
cd OpenClaw_Camera_Construction_Mgmt
npm install
```

### 配置

复制环境变量配置文件并填写实际值：

```bash
cp .env.example .env
```

## 项目结构

```
├── src/                    # 源代码
├── docs/                   # 文档
├── scripts/                # 脚本工具
├── .github/workflows/      # CI/CD 配置
└── README.md
```

## 开发指南

### 本地开发

```bash
npm run dev
```

### 运行测试

```bash
npm test
```

### 构建

```bash
npm run build
```

## CI/CD

本项目使用 GitHub Actions 进行持续集成：

- ✅ 代码检查
- ✅ 自动测试
- ✅ 构建验证
- 🧹 自动清理旧的 workflow runs

## 贡献

欢迎提交 Issue 和 Pull Request！

## 许可证

MIT License

---

**迁移记录：** 2026-03-23 从 RichardQidian 账号迁移至 Luffy20260323
