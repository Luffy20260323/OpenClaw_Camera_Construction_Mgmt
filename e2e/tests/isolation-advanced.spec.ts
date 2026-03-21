/**
 * 管理员数据隔离测试 - 补充用例
 * 测试用例来源：docs/测试用例补充_管理员创建用户数据隔离.md
 */

import { test, expect } from '@playwright/test';

test.describe('管理员数据隔离 - 补充测试', () => {
  
  // TC-ISO-003: 乙方管理员 - 公司列表验证
  test('TC-ISO-003: 乙方管理员创建用户时，公司字段自动填充为乙方公司', async ({ page }) => {
    console.log('【TC-ISO-003】测试开始：乙方管理员 - 公司列表验证');
    
    // 乙方管理员登录
    await page.goto('/login');
    await page.fill('input[name="username"]', 'yifang_admin');
    await page.fill('input[name="password"]', 'password123');
    await page.click('button[type="submit"]');
    
    await page.waitForURL(/\/dashboard/);
    
    // 进入用户管理
    await page.click('text=用户管理');
    await page.click('text=新增用户');
    await page.waitForLoadState('networkidle');
    
    // 验证公司字段自动填充
    const companySelect = page.locator('select[name="companyId"]');
    await expect(companySelect).toBeVisible();
    
    const selectedValue = await companySelect.inputValue();
    console.log('选中的公司 ID:', selectedValue);
    
    // 验证是乙方公司（ID=6）
    expect(selectedValue).toBe('6');
    
    console.log('【TC-ISO-003】✅ 测试通过');
  });

  // TC-ISO-004: 乙方管理员 - 角色列表验证
  test('TC-ISO-004: 乙方管理员创建用户时，角色列表仅显示乙方角色', async ({ page }) => {
    console.log('【TC-ISO-004】测试开始：乙方管理员 - 角色列表验证');
    
    // 乙方管理员登录
    await page.goto('/login');
    await page.fill('input[name="username"]', 'yifang_admin');
    await page.fill('input[name="password"]', 'password123');
    await page.click('button[type="submit"]');
    await page.waitForURL(/\/dashboard/);
    
    // 进入用户管理
    await page.click('text=用户管理');
    await page.click('text=新增用户');
    await page.waitForLoadState('networkidle');
    
    // 获取角色列表
    const roleOptions = await page.$$eval('select[name="roleId"] option', 
      options => options.map(opt => opt.textContent?.trim() || '').filter(text => text)
    );
    
    console.log('角色列表:', roleOptions);
    
    // 验证所有角色都包含"乙方"
    for (const role of roleOptions) {
      expect(role).toMatch(/乙方/);
    }
    
    // 验证不包含甲方或监理角色
    for (const role of roleOptions) {
      expect(role).not.toMatch(/甲方 | 监理/);
    }
    
    console.log('【TC-ISO-004】✅ 测试通过');
  });

  // TC-ISO-005: 监理管理员 - 公司列表验证
  test('TC-ISO-005: 监理管理员创建用户时，公司字段自动填充为监理公司', async ({ page }) => {
    console.log('【TC-ISO-005】测试开始：监理管理员 - 公司列表验证');
    
    // 监理管理员登录
    await page.goto('/login');
    await page.fill('input[name="username"]', 'jianli_admin');
    await page.fill('input[name="password"]', 'password123');
    await page.click('button[type="submit"]');
    
    await page.waitForURL(/\/dashboard/);
    
    // 进入用户管理
    await page.click('text=用户管理');
    await page.click('text=新增用户');
    await page.waitForLoadState('networkidle');
    
    // 验证公司字段自动填充
    const companySelect = page.locator('select[name="companyId"]');
    const selectedValue = await companySelect.inputValue();
    console.log('选中的公司 ID:', selectedValue);
    
    // 验证是监理公司（ID=12）
    expect(selectedValue).toBe('12');
    
    console.log('【TC-ISO-005】✅ 测试通过');
  });

  // TC-ISO-006: 监理管理员 - 角色列表验证
  test('TC-ISO-006: 监理管理员创建用户时，角色列表仅显示监理角色', async ({ page }) => {
    console.log('【TC-ISO-006】测试开始：监理管理员 - 角色列表验证');
    
    // 监理管理员登录
    await page.goto('/login');
    await page.fill('input[name="username"]', 'jianli_admin');
    await page.fill('input[name="password"]', 'password123');
    await page.click('button[type="submit"]');
    await page.waitForURL(/\/dashboard/);
    
    // 进入用户管理
    await page.click('text=用户管理');
    await page.click('text=新增用户');
    await page.waitForLoadState('networkidle');
    
    // 获取角色列表
    const roleOptions = await page.$$eval('select[name="roleId"] option', 
      options => options.map(opt => opt.textContent?.trim() || '').filter(text => text)
    );
    
    console.log('角色列表:', roleOptions);
    
    // 验证所有角色都包含"监理"
    for (const role of roleOptions) {
      expect(role).toMatch(/监理/);
    }
    
    // 验证不包含甲方或乙方角色
    for (const role of roleOptions) {
      expect(role).not.toMatch(/甲方 | 乙方/);
    }
    
    console.log('【TC-ISO-006】✅ 测试通过');
  });

  // TC-ISO-007: 系统管理员 - 公司列表验证（对比）
  test('TC-ISO-007: 系统管理员创建用户时，可以选择任何公司', async ({ page }) => {
    console.log('【TC-ISO-007】测试开始：系统管理员 - 公司列表验证');
    
    // 系统管理员登录
    await page.goto('/login');
    await page.fill('input[name="username"]', 'admin');
    await page.fill('input[name="password"]', 'admin123');
    await page.click('button[type="submit"]');
    await page.waitForURL(/\/dashboard/);
    
    // 进入用户管理
    await page.click('text=用户管理');
    await page.click('text=新增用户');
    await page.waitForLoadState('networkidle');
    
    // 验证公司字段可选择
    const companySelect = page.locator('select[name="companyId"]');
    await expect(companySelect).toBeEnabled();
    
    // 获取公司列表
    const companyOptions = await companySelect.$$eval('option', 
      options => options.map(opt => ({
        value: opt.value,
        text: opt.textContent?.trim() || ''
      })).filter(opt => opt.value)
    );
    
    console.log('公司列表:', companyOptions);
    
    // 验证包含所有公司类型
    const companyTexts = companyOptions.map(c => c.text).join(',');
    expect(companyTexts).toMatch(/甲方/);
    expect(companyTexts).toMatch(/乙方/);
    expect(companyTexts).toMatch(/监理/);
    
    console.log('【TC-ISO-007】✅ 测试通过');
  });

  // TC-ISO-008: 系统管理员 - 角色联动验证（对比）
  test('TC-ISO-008: 系统管理员创建用户时，角色根据公司选择联动更新', async ({ page }) => {
    console.log('【TC-ISO-008】测试开始：系统管理员 - 角色联动验证');
    
    // 系统管理员登录
    await page.goto('/login');
    await page.fill('input[name="username"]', 'admin');
    await page.fill('input[name="password"]', 'admin123');
    await page.click('button[type="submit"]');
    await page.waitForURL(/\/dashboard/);
    
    // 进入用户管理
    await page.click('text=用户管理');
    await page.click('text=新增用户');
    await page.waitForLoadState('networkidle');
    
    // 步骤 1: 选择甲方公司
    await page.selectOption('select[name="companyId"]', '3');
    await page.waitForTimeout(1000);
    
    // 获取甲方角色
    const jiafangRoles = await page.$$eval('select[name="roleId"] option', 
      options => options.map(opt => opt.textContent?.trim() || '').filter(text => text)
    );
    console.log('甲方角色:', jiafangRoles);
    
    // 验证都是甲方角色
    for (const role of jiafangRoles) {
      expect(role).toMatch(/甲方/);
    }
    
    // 步骤 2: 切换到乙方公司
    await page.selectOption('select[name="companyId"]', '6');
    await page.waitForTimeout(1000);
    
    // 获取乙方角色
    const yifangRoles = await page.$$eval('select[name="roleId"] option', 
      options => options.map(opt => opt.textContent?.trim() || '').filter(text => text)
    );
    console.log('乙方角色:', yifangRoles);
    
    // 验证都是乙方角色
    for (const role of yifangRoles) {
      expect(role).toMatch(/乙方/);
    }
    
    console.log('【TC-ISO-008】✅ 测试通过');
  });

  // TC-ISO-010: API 越权测试 - 乙方管理员创建甲方用户
  test('TC-ISO-010: 乙方管理员通过 API 尝试创建甲方用户应被拒绝', async ({ request }) => {
    console.log('【TC-ISO-010】测试开始：乙方管理员 API 越权测试');
    
    // 乙方管理员登录
    const loginResp = await request.post('/api/auth/login', {
      data: {
        username: 'yifang_admin',
        password: 'password123',
        captcha: 'test'
      }
    });
    
    const loginData = await loginResp.json();
    const token = loginData.data?.token;
    expect(token).toBeTruthy();
    
    // 尝试创建甲方用户
    const createResp = await request.post('/api/user', {
      headers: { 
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      data: {
        username: 'test_jiafang_user_' + Date.now(),
        password: 'Test123!',
        companyId: 3, // 甲方公司 ID
        roleIds: [1]
      }
    });
    
    const createData = await createResp.json();
    console.log('创建用户响应:', createData);
    
    // 验证返回 400 或 403
    expect([400, 403]).toContain(createResp.status());
    expect(createData.message).toContain('无权');
    
    console.log('【TC-ISO-010】✅ 测试通过');
  });

  // TC-ISO-011: API 越权测试 - 监理管理员创建乙方用户
  test('TC-ISO-011: 监理管理员通过 API 尝试创建乙方用户应被拒绝', async ({ request }) => {
    console.log('【TC-ISO-011】测试开始：监理管理员 API 越权测试');
    
    // 监理管理员登录
    const loginResp = await request.post('/api/auth/login', {
      data: {
        username: 'jianli_admin',
        password: 'password123',
        captcha: 'test'
      }
    });
    
    const loginData = await loginResp.json();
    const token = loginData.data?.token;
    expect(token).toBeTruthy();
    
    // 尝试创建乙方用户
    const createResp = await request.post('/api/user', {
      headers: { 
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      data: {
        username: 'test_yifang_user_' + Date.now(),
        password: 'Test123!',
        companyId: 6, // 乙方公司 ID
        roleIds: [15]
      }
    });
    
    const createData = await createResp.json();
    console.log('创建用户响应:', createData);
    
    // 验证返回 400 或 403
    expect([400, 403]).toContain(createResp.status());
    expect(createData.message).toContain('无权');
    
    console.log('【TC-ISO-011】✅ 测试通过');
  });
});
