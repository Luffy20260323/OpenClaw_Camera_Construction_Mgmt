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

### 用户注册联动测试 (12 条) ✅

| 编号 | 用例名称 | 文件 |
|------|----------|------|
| UI-REG-001 | 选择甲方公司 - 角色联动更新 | registration.spec.ts |
| UI-REG-002 | 选择乙方公司 - 角色联动更新 | registration-advanced.spec.ts |
| UI-REG-003 | 选择监理公司 - 角色联动更新 | registration-advanced.spec.ts |
| UI-REG-004 | 切换公司 - 角色列表刷新 | registration.spec.ts |
| UI-REG-005 | 选择甲方作业区角色 - 显示作业区 | registration.spec.ts |
| UI-REG-006 | 选择甲方非作业区角色 - 不显示作业区 | registration.spec.ts |
| UI-REG-007 | 选择乙方角色 - 不显示作业区 | registration.spec.ts |
| UI-REG-008 | 选择监理角色 - 不显示作业区 | registration-advanced.spec.ts |
| UI-REG-009 | 切换角色 - 作业区框动态显示/隐藏 | registration.spec.ts |
| UI-REG-010 | 未选择公司 - 角色下拉框状态 | registration-advanced.spec.ts |
| UI-REG-011 | 作业区角色判断逻辑 | registration-advanced.spec.ts |
| UI-REG-012 | 多选角色 - 作业区逻辑 | registration-advanced.spec.ts |

### 数据隔离测试 (12 条) ✅

| 编号 | 用例名称 | 文件 |
|------|----------|------|
| TC-ISO-001 | 甲方管理员 - 公司列表验证 | isolation.spec.ts |
| TC-ISO-002 | 甲方管理员 - 角色列表验证 | isolation.spec.ts |
| TC-ISO-003 | 乙方管理员 - 公司列表验证 | isolation-advanced.spec.ts |
| TC-ISO-004 | 乙方管理员 - 角色列表验证 | isolation-advanced.spec.ts |
| TC-ISO-005 | 监理管理员 - 公司列表验证 | isolation-advanced.spec.ts |
| TC-ISO-006 | 监理管理员 - 角色列表验证 | isolation-advanced.spec.ts |
| TC-ISO-007 | 系统管理员 - 公司列表验证 | isolation-advanced.spec.ts |
| TC-ISO-008 | 系统管理员 - 角色联动验证 | isolation-advanced.spec.ts |
| TC-ISO-009 | API 越权测试 - 创建乙方用户 | isolation.spec.ts |
| TC-ISO-010 | API 越权测试 - 创建甲方用户 | isolation-advanced.spec.ts |
| TC-ISO-011 | API 越权测试 - 创建乙方用户 | isolation-advanced.spec.ts |
| TC-ISO-012 | 作业区数据隔离验证 | isolation.spec.ts |

### 匿名注册配置测试 (10 条) ✅

| 编号 | 用例名称 | 文件 |
|------|----------|------|
| TC-ANON-001 | 登录页面公司列表过滤 | anonymous.spec.ts |
| TC-ANON-002 | 修改配置后列表更新 | anonymous-advanced.spec.ts |
| TC-ANON-003 | 禁止匿名注册的公司不显示 | anonymous.spec.ts |
| TC-ANON-004 | 所有公司禁止时的边界情况 | anonymous-advanced.spec.ts |
| TC-ANON-005 | API 测试 - 注册时公司权限验证 | anonymous-advanced.spec.ts |
| TC-ANON-006 | 系统保护公司 - 配置字段状态 | anonymous.spec.ts |
| TC-ANON-007 | 系统保护公司 - API 修改配置 | anonymous-advanced.spec.ts |
| TC-ANON-008 | 系统保护公司 - 普通管理员也无法修改 | anonymous-advanced.spec.ts |
| TC-ANON-009 | 普通公司 - 允许修改配置 | anonymous.spec.ts |
| TC-ANON-010 | 系统保护公司 - 数据库直接修改验证 | anonymous-advanced.spec.ts |

### 界面与体验测试 (5 条) ✅

| 编号 | 用例名称 | 文件 |
|------|----------|------|
| UI-001 | 响应式布局测试 | ui-experience.spec.ts |
| UI-002 | 移动端适配测试 | ui-experience.spec.ts |
| UI-003 | 功能一致性测试 | ui-experience.spec.ts |
| UI-004 | 页面加载性能 | ui-experience.spec.ts |
| UI-005 | 无障碍访问测试 | ui-experience.spec.ts |

### 系统配置测试 (5 条) ✅

| 编号 | 用例名称 | 文件 |
|------|----------|------|
| SYS-002-001 | 配置为"不要验证码" | system-config.spec.ts |
| SYS-002-002 | 配置为"图形验证码" | system-config.spec.ts |
| SYS-002-003 | 配置为"手机短信验证码" | system-config.spec.ts |
| SYS-002-004 | 验证码配置切换测试 | system-config.spec.ts |
| SYS-002-005 | 系统保护配置项验证 | system-config.spec.ts |

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
