# Playwright 前端自动化测试方案

**版本**: v1.0  
**日期**: 2026-03-21  
**目标**: 将 34 条前端手动测试用例转为自动化测试  

---

## 📊 自动化覆盖率目标

| 模块 | 总用例 | 可自动化 | 自动化率 |
|------|--------|----------|----------|
| 界面与体验 | 5 | 4 | 80% |
| 用户注册联动 | 12 | 12 | 100% |
| 管理员数据隔离 | 12 | 10 | 83% |
| 匿名注册配置 | 5 | 4 | 80% |
| **总计** | **34** | **30** | **88%** |

---

## 🛠️ 技术栈

| 工具 | 用途 | 版本 |
|------|------|------|
| **Playwright** | 前端 E2E 测试 | v1.40+ |
| **TypeScript** | 测试脚本语言 | v5.0+ |
| **Jest/Playwright Test** | 测试运行器 | 内置 |
| **axe-core** | 无障碍测试 | v4.8+ |
| **Lighthouse** | 性能测试 | v11+ |
| **GitHub Actions** | CI 集成 | - |

---

## 📁 项目结构

```
OpenClaw_Camera_Construction_Mgmt/
├── e2e/                          # Playwright 测试目录
│   ├── tests/
│   │   ├── auth.spec.ts          # 认证测试
│   │   ├── registration.spec.ts  # 用户注册联动测试 (12 条)
│   │   ├── isolation.spec.ts     # 数据隔离测试 (10 条)
│   │   ├── anonymous.spec.ts     # 匿名注册测试 (4 条)
│   │   └── ui-experience.spec.ts # 界面体验测试 (4 条)
│   ├── fixtures/
│   │   └── test-data.ts          # 测试数据
│   ├── utils/
│   │   ├── auth-helper.ts        # 认证辅助
│   │   └── assertions.ts         # 自定义断言
│   └── playwright.config.ts      # Playwright 配置
├── package.json
└── .github/workflows/
    └── e2e-test.yml              # CI 工作流
```

---

## 🚀 快速开始

### 1. 安装依赖

```bash
cd /root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt

# 初始化 Node.js 项目（如没有）
npm init -y

# 安装 Playwright
npm install -D @playwright/test

# 安装浏览器
npx playwright install chromium

# 初始化 Playwright 配置
npx playwright init
```

### 2. 配置 playwright.config.ts

```typescript
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './e2e/tests',
  timeout: 30000,
  expect: {
    timeout: 5000
  },
  reporter: [
    ['html'],
    ['junit', { outputFile: 'e2e/results/results.xml' }],
    ['list']
  ],
  use: {
    baseURL: 'http://localhost',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
    {
      name: 'Mobile Chrome',
      use: { ...devices['Pixel 5'] },
    },
    {
      name: 'Mobile Safari',
      use: { ...devices['iPhone 12'] },
    },
  ],
});
```

---

## 📝 测试用例示例

### 示例 1: 用户注册 - 公司角色联动

```typescript
// e2e/tests/registration.spec.ts
import { test, expect } from '@playwright/test';

test.describe('用户注册 - 公司角色联动', () => {
  
  test('UI-REG-001: 选择甲方公司 - 角色联动更新', async ({ page }) => {
    // 打开注册页面
    await page.goto('/login');
    await page.click('text=注册');
    
    // 选择甲方公司
    await page.selectOption('select[name="companyId"]', '3'); // 测试甲方公司 ID
    
    // 验证角色列表仅显示甲方角色
    const roleOptions = await page.$$('select[name="roleId"] option');
    for (const option of roleOptions) {
      const text = await option.textContent();
      expect(text).toMatch(/甲方.*/);
      expect(text).not.toMatch(/乙方 | 监理/);
    }
  });

  test('UI-REG-005: 选择甲方作业区角色 - 显示作业区', async ({ page }) => {
    await page.goto('/login');
    await page.click('text=注册');
    
    // 选择甲方公司
    await page.selectOption('select[name="companyId"]', '3');
    
    // 选择作业区角色
    await page.selectOption('select[name="roleId"]', '5'); // 作业区项目主管
    
    // 验证作业区选择框显示
    const workAreaSelect = page.locator('select[name="workAreaId"]');
    await expect(workAreaSelect).toBeVisible();
    
    // 验证作业区列表仅显示本公司作业区
    const options = await workAreaSelect.locator('option').all();
    expect(options.length).toBeGreaterThan(1);
  });

  test('UI-REG-007: 选择乙方角色 - 不显示作业区', async ({ page }) => {
    await page.goto('/login');
    await page.click('text=注册');
    
    // 选择乙方公司
    await page.selectOption('select[name="companyId"]', '6');
    
    // 选择乙方角色
    await page.selectOption('select[name="roleId"]', '15');
    
    // 验证作业区选择框不显示
    const workAreaSelect = page.locator('select[name="workAreaId"]');
    await expect(workAreaSelect).not.toBeVisible();
  });

  test('UI-REG-009: 切换角色 - 作业区框动态显示/隐藏', async ({ page }) => {
    await page.goto('/login');
    await page.click('text=注册');
    await page.selectOption('select[name="companyId"]', '3');
    
    // 选择作业区角色
    await page.selectOption('select[name="roleId"]', '5');
    await expect(page.locator('select[name="workAreaId"]')).toBeVisible();
    
    // 切换到非作业区角色
    await page.selectOption('select[name="roleId"]', '1');
    await expect(page.locator('select[name="workAreaId"]')).not.toBeVisible();
  });
});
```

### 示例 2: 管理员数据隔离

```typescript
// e2e/tests/isolation.spec.ts
import { test, expect } from '@playwright/test';

test.describe('管理员创建用户 - 数据隔离', () => {
  
  test('TC-ISO-001: 甲方管理员 - 公司列表验证', async ({ page }) => {
    // 甲方管理员登录
    await page.goto('/login');
    await page.fill('input[name="username"]', 'jifang_admin');
    await page.fill('input[name="password"]', 'password123');
    await page.click('button[type="submit"]');
    
    // 进入用户管理
    await page.click('text=用户管理');
    await page.click('text=新增用户');
    
    // 验证公司字段自动填充且不可编辑
    const companySelect = page.locator('select[name="companyId"]');
    await expect(companySelect).toBeDisabled();
    
    const selectedValue = await companySelect.inputValue();
    expect(selectedValue).toBe('3'); // 甲方公司 ID
  });

  test('TC-ISO-009: API 越权测试 - 甲方管理员创建乙方用户', async ({ request }) => {
    // 甲方管理员登录获取 token
    const loginResp = await request.post('/api/auth/login', {
      data: {
        username: 'jifang_admin',
        password: 'password123',
        captcha: 'test'
      }
    });
    const token = (await loginResp.json()).data.token;
    
    // 尝试创建乙方用户
    const createResp = await request.post('/api/user', {
      headers: { 'Authorization': `Bearer ${token}` },
      data: {
        username: 'test_yifang_user',
        password: 'Test123!',
        companyId: 6, // 乙方公司 ID
        roleId: 15
      }
    });
    
    // 验证返回 403 或 400
    expect([400, 403]).toContain(createResp.status());
    
    const errorBody = await createResp.json();
    expect(errorBody.message).toContain('无权');
  });
});
```

### 示例 3: 匿名注册配置

```typescript
// e2e/tests/anonymous.spec.ts
import { test, expect } from '@playwright/test';

test.describe('匿名注册配置', () => {
  
  test('TC-ANON-001: 登录页面 - 只显示允许匿名注册的公司', async ({ page }) => {
    await page.goto('/login');
    await page.click('text=注册');
    
    // 获取公司列表
    const companyOptions = await page.$$('select[name="companyId"] option');
    
    for (const option of companyOptions) {
      const value = await option.getAttribute('value');
      if (value) {
        // 验证该公司 allowAnonymousRegister=true
        // (需要通过 API 或页面数据验证)
      }
    }
    
    // 验证禁止匿名注册的公司不在列表中
    const companyTexts = await page.$$eval('select[name="companyId"] option', 
      opts => opts.map(o => o.textContent));
    
    expect(companyTexts).not.toContain('禁止匿名注册的公司名');
  });

  test('TC-ANON-006: 系统保护公司 - 配置字段禁用', async ({ page }) => {
    // 系统管理员登录
    await page.goto('/login');
    await page.fill('input[name="username"]', 'admin');
    await page.fill('input[name="password"]', 'admin123');
    await page.click('button[type="submit"]');
    
    // 进入公司管理
    await page.click('text=公司管理');
    await page.click('text=北京其点技术服务有限公司 >> button:has-text("编辑")');
    
    // 验证匿名注册配置字段禁用
    const anonymousCheckbox = page.locator('input[name="allowAnonymousRegister"]');
    await expect(anonymousCheckbox).toBeDisabled();
    
    // 验证提示信息
    const hint = page.locator('text=系统保护公司');
    await expect(hint).toBeVisible();
  });
});
```

### 示例 4: 界面体验测试

```typescript
// e2e/tests/ui-experience.spec.ts
import { test, expect } from '@playwright/test';

test.describe('界面与体验', () => {
  
  test('UI-001: 响应式布局测试', async ({ page }) => {
    const viewports = [
      { width: 1920, height: 1080, name: 'Desktop' },
      { width: 768, height: 1024, name: 'Tablet' },
      { width: 375, height: 667, name: 'Mobile' },
    ];
    
    for (const viewport of viewports) {
      await page.setViewportSize({ width: viewport.width, height: viewport.height });
      await page.goto('/login');
      
      // 验证页面元素正常显示
      await expect(page.locator('body')).toBeVisible();
      
      // 验证无横向滚动条
      const scrollWidth = await page.evaluate(() => document.documentElement.scrollWidth);
      const clientWidth = await page.evaluate(() => document.documentElement.clientWidth);
      expect(scrollWidth).toBeLessThanOrEqual(clientWidth);
    }
  });

  test('UI-004: 页面加载性能', async ({ page }) => {
    // 使用 Performance API 测试加载时间
    await page.goto('/login');
    
    const performanceMetrics = await page.evaluate(() => {
      const timing = performance.timing;
      return {
        domContentLoaded: timing.domContentLoadedEventEnd - timing.navigationStart,
        fullyLoaded: timing.loadEventEnd - timing.navigationStart,
      };
    });
    
    // 验证加载时间 < 3s
    expect(performanceMetrics.fullyLoaded).toBeLessThan(3000);
  });

  test('UI-005: 无障碍访问测试', async ({ page }) => {
    await page.goto('/login');
    
    // 使用 axe-core 进行无障碍测试
    const axeResults = await page.evaluate(async () => {
      // @ts-ignore
      return await axe.run();
    });
    
    // 验证无严重无障碍问题
    expect(axeResults.violations.length).toBe(0);
  });
});
```

---

## 🔧 CI/CD 集成

### GitHub Actions 工作流

```yaml
# .github/workflows/e2e-test.yml
name: E2E Tests

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  e2e-test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v4
    
    - name: Setup Node.js
      uses: actions/setup-node@v4
      with:
        node-version: '18'
    
    - name: Install dependencies
      run: |
        npm ci
        npx playwright install chromium
    
    - name: Start backend service
      run: |
        cd backend
        nohup java -jar target/camera-lifecycle-system-1.0.0-SNAPSHOT.jar &
        sleep 30
    
    - name: Run Playwright tests
      run: npx playwright test --reporter=html
    
    - name: Upload test results
      if: always()
      uses: actions/upload-artifact@v4
      with:
        name: playwright-report
        path: playwright-report/
        retention-days: 30
```

---

## 📊 执行命令

```bash
# 运行所有测试
npx playwright test

# 运行特定测试文件
npx playwright test e2e/tests/registration.spec.ts

# 运行特定测试用例
npx playwright test --grep "UI-REG-001"

# 有头模式（查看浏览器）
npx playwright test --headed

# 生成 HTML 报告
npx playwright test --reporter=html
npx playwright show-report

# 调试模式
npx playwright test --debug
```

---

## 📈 预期效果

| 指标 | 当前 | 实施后 |
|------|------|--------|
| 前端测试自动化率 | 0% | 88% |
| 测试执行时间 | 手动 2-3 小时 | 自动 10-15 分钟 |
| 回归测试频率 | 每月 1 次 | 每次提交 |
| Bug 发现时机 | 上线后 | 开发阶段 |

---

## 🎯 实施计划

### 第 1 周：环境搭建
- [ ] 安装 Playwright
- [ ] 配置测试框架
- [ ] 编写基础测试（认证）

### 第 2-3 周：用例开发
- [ ] 用户注册联动测试 (12 条)
- [ ] 数据隔离测试 (10 条)
- [ ] 匿名注册测试 (4 条)

### 第 4 周：CI 集成
- [ ] 配置 GitHub Actions
- [ ] 集成测试报告
- [ ] 培训团队

---

## 🔗 相关资源

- [Playwright 官方文档](https://playwright.dev/)
- [Playwright Test](https://playwright.dev/docs/test-intro)
- [axe-core 无障碍测试](https://www.deque.com/axe/)
- [Lighthouse CI](https://github.com/GoogleChrome/lighthouse-ci)

---

*方案创建时间：2026-03-21*  
*创建工具：OpenClaw AI Assistant*
