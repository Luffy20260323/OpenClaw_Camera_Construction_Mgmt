# Playwright E2E 测试快速指南

**版本**: v1.0  
**日期**: 2026-03-22  
**状态**: Proof of Concept (PoC)  

---

## 📦 已安装组件

- ✅ Playwright Test v1.40+
- ✅ TypeScript 支持
- ✅ Chromium 浏览器

---

## 📁 测试文件结构

```
e2e/
├── tests/
│   ├── registration.spec.ts    # 用户注册联动测试 (6 条)
│   ├── isolation.spec.ts       # 数据隔离测试 (4 条)
│   └── anonymous.spec.ts       # 匿名注册配置测试 (4 条)
├── playwright.config.ts        # Playwright 配置
└── README.md                   # 本文档
```

---

## 🚀 快速开始

### 1. 运行所有测试

```bash
cd /root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt

# 运行所有测试
npm run e2e
```

### 2. 运行特定测试文件

```bash
# 运行用户注册联动测试
npm run e2e -- e2e/tests/registration.spec.ts

# 运行数据隔离测试
npm run e2e -- e2e/tests/isolation.spec.ts

# 运行匿名注册配置测试
npm run e2e -- e2e/tests/anonymous.spec.ts
```

### 3. 运行特定测试用例

```bash
# 运行单个测试用例
npm run e2e -- --grep "UI-REG-001"

# 运行所有 UI-REG 开头的测试
npm run e2e -- --grep "UI-REG"
```

### 4. 有头模式（查看浏览器）

```bash
# 打开浏览器运行测试
npm run e2e:headed
```

### 5. 调试模式

```bash
# 调试模式（逐步执行）
npm run e2e:debug
```

### 6. 查看测试报告

```bash
# 生成并打开 HTML 报告
npx playwright show-report e2e/results/html
```

---

## 📊 测试用例清单

### 用户注册联动测试 (6 条)

| 编号 | 用例名称 | 状态 |
|------|----------|------|
| UI-REG-001 | 选择甲方公司 - 角色联动更新 | ✅ 已实现 |
| UI-REG-004 | 切换公司 - 角色列表刷新 | ✅ 已实现 |
| UI-REG-005 | 选择甲方作业区角色 - 显示作业区 | ✅ 已实现 |
| UI-REG-006 | 选择甲方非作业区角色 - 不显示作业区 | ✅ 已实现 |
| UI-REG-007 | 选择乙方角色 - 不显示作业区 | ✅ 已实现 |
| UI-REG-009 | 切换角色 - 作业区框动态显示/隐藏 | ✅ 已实现 |

### 数据隔离测试 (4 条)

| 编号 | 用例名称 | 状态 |
|------|----------|------|
| TC-ISO-001 | 甲方管理员 - 公司列表验证 | ✅ 已实现 |
| TC-ISO-002 | 甲方管理员 - 角色列表验证 | ✅ 已实现 |
| TC-ISO-009 | API 越权测试 - 创建乙方用户 | ✅ 已实现 |
| TC-ISO-012 | 作业区数据隔离验证 | ✅ 已实现 |

### 匿名注册配置测试 (4 条)

| 编号 | 用例名称 | 状态 |
|------|----------|------|
| TC-ANON-001 | 登录页面公司列表过滤 | ✅ 已实现 |
| TC-ANON-003 | 禁止匿名注册的公司不显示 | ✅ 已实现 |
| TC-ANON-006 | 系统保护公司配置字段禁用 | ✅ 已实现 |
| TC-ANON-009 | 普通公司允许修改配置 | ✅ 已实现 |

---

## ⚙️ 配置说明

### playwright.config.ts

```typescript
export default defineConfig({
  testDir: './e2e/tests',
  timeout: 30000,
  reporter: [
    ['html'],
    ['junit'],
    ['list']
  ],
  use: {
    baseURL: 'http://localhost',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },
  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } },
    { name: 'Mobile Chrome', use: { ...devices['Pixel 5'] } },
    { name: 'Mobile Safari', use: { ...devices['iPhone 12'] } },
  ],
});
```

---

## 🔧 测试数据准备

### 需要的测试账号

| 账号 | 用户名 | 密码 | 角色 |
|------|--------|------|------|
| 系统管理员 | admin | admin123 | 系统管理员 |
| 甲方管理员 | jifang_admin | password123 | 甲方管理员 |
| 乙方管理员 | yifang_admin | password123 | 乙方管理员 |

### 需要的测试数据

| 数据类型 | ID | 名称 | 说明 |
|----------|----|------|------|
| 甲方公司 | 3 | 测试甲方公司 | 允许匿名注册 |
| 乙方公司 | 6 | 测试乙方公司 | 禁止匿名注册 |
| 作业区 | - | - | 属于甲方公司 |

---

## 📝 运行示例

### 示例 1: 运行所有测试

```bash
$ npm run e2e

Running 14 tests using 1 worker

  ✓  registration.spec.ts:8:3 › 用户注册 - 公司角色联动 › UI-REG-001: 选择甲方公司 - 角色联动更新 (3.2s)
  ✓  registration.spec.ts:35:3 › 用户注册 - 公司角色联动 › UI-REG-005: 选择甲方作业区角色 - 显示作业区 (2.8s)
  ✓  registration.spec.ts:55:3 › 用户注册 - 公司角色联动 › UI-REG-006: 选择甲方非作业区角色 - 不显示作业区 (2.5s)
  ...

  14 passed (15.3s)
```

### 示例 2: 查看 HTML 报告

```bash
$ npx playwright show-report e2e/results/html

Opening HTML report in default browser...
```

---

## ⚠️ 注意事项

1. **后端服务必须运行**: 测试前确保后端服务在 `http://localhost:8080` 运行
2. **测试数据**: 确保测试账号和测试数据已准备
3. **浏览器**: 首次运行会自动下载 Chromium（约 100MB）
4. **超时设置**: 默认 30 秒，慢的测试可以调整

---

## 🎯 下一步计划

### 已完成 (PoC)
- ✅ Playwright 环境搭建
- ✅ 配置 TypeScript
- ✅ 14 条核心测试用例
- ✅ 测试报告生成

### 待完成
- ⏭️ 补充剩余 16 条测试用例
- ⏭️ 添加无障碍测试 (axe-core)
- ⏭️ 添加性能测试 (Lighthouse)
- ⏭️ CI/CD 集成 (GitHub Actions)

---

## 🔗 相关资源

- [Playwright 官方文档](https://playwright.dev/)
- [Playwright Test](https://playwright.dev/docs/test-intro)
- [测试用例设计文档](../e2e/PLAYWRIGHT-TEST-PLAN.md)

---

*文档创建时间：2026-03-22*  
*创建工具：OpenClaw AI Assistant*
