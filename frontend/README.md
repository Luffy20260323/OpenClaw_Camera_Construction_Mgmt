# 摄像头生命周期管理系统 - PC 端 Web 应用

基于 Vue 3 + Element Plus 的前端应用。

## 技术栈

- **Vue 3** - 渐进式 JavaScript 框架
- **Vite** - 下一代前端构建工具
- **Element Plus** - Vue 3 组件库
- **Vue Router** - 官方路由管理器
- **Pinia** - Vue 状态管理
- **Axios** - HTTP 客户端

## 目录结构

```
pc-web/
├── src/
│   ├── api/              # API 接口
│   │   └── auth.js       # 认证相关接口
│   ├── assets/           # 静态资源
│   ├── components/       # 公共组件
│   ├── router/           # 路由配置
│   │   └── index.js
│   ├── stores/           # Pinia 状态管理
│   │   └── user.js       # 用户状态
│   ├── utils/            # 工具函数
│   │   └── request.js    # Axios 封装
│   ├── views/            # 页面组件
│   │   ├── Login.vue     # 登录页
│   │   ├── Home.vue      # 首页
│   │   └── NotFound.vue  # 404 页
│   ├── App.vue           # 根组件
│   └── main.js           # 入口文件
├── index.html
├── package.json
└── vite.config.js
```

## 快速开始

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

访问：http://localhost:3000

### 构建生产版本

```bash
npm run build
```

### 预览生产构建

```bash
npm run preview
```

## 功能特性

### ✅ 已完成

- [x] 登录页面
  - 用户名/密码登录
  - 表单验证
  - 记住登录状态
  - 美观的渐变背景
  
- [x] 首页
  - 用户信息展示
  - 角色和权限显示
  - 快捷操作入口
  - 数据统计卡片
  
- [x] 路由守卫
  - 未登录自动跳转
  - Token 验证
  
- [x] 状态管理
  - Pinia 用户状态
  - localStorage 持久化
  
- [x] API 封装
  - Axios 拦截器
  - Token 自动注入
  - 401 自动跳转

### 🚧 待开发

- [ ] 项目管理页面
- [ ] 点位管理页面
- [ ] 施工任务页面
- [ ] 审核管理页面
- [ ] 结算管理页面
- [ ] 系统管理页面

## 默认账号

- **用户名**: admin
- **密码**: Admin@2026

## API 代理

开发环境下，所有 `/api` 请求会代理到后端服务：

- 前端：http://localhost:3000
- 后端：http://localhost:8080

配置在 `vite.config.js` 中。

## 开发规范

### 组件命名

- 页面组件：大驼峰，如 `Login.vue`
- 公共组件：大驼峰，如 `UserCard.vue`

### 状态管理

- 使用 Pinia stores
- 按模块划分，如 `user.js`、`project.js`

### API 调用

- 统一在 `src/api/` 目录下
- 按业务模块划分文件

---

**版本**: v0.1  
**最后更新**: 2026-03-10
