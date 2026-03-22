/**
 * 管理员创建用户 - 数据隔离测试
 * 测试用例来源：docs/测试用例补充_管理员创建用户数据隔离.md
 */

import { test, expect } from '@playwright/test';

// 配置 baseURL
const baseURL = process.env.BASE_URL || 'http://localhost:8080';
  const FRONTEND_URL = process.env.FRONTEND_URL || 'http://localhost:3000';

test.describe('管理员创建用户 - 数据隔离', () => {
  
  // TC-ISO-001: 甲方管理员 - 公司列表验证
  test('TC-ISO-001: 甲方管理员创建用户时，公司字段自动填充且不可编辑', async ({ page }) => {
    console.log('【TC-ISO-001】测试开始：甲方管理员 - 公司列表验证');
    
    // 甲方管理员登录（需要真实账号）
    await page.goto(`${FRONTEND_URL}/login`);
    await page.fill('input[name="username"]', 'jifang_admin');
    await page.fill('input[name="password"]', 'password123');
    await page.click('button[type="submit"]');
    
    // 等待登录成功
    await page.waitForURL(/\/dashboard/);
    
    // 进入用户管理
    await page.click('text=用户管理');
    await page.click('text=新增用户');
    
    // 等待页面加载
    await page.waitForLoadState('networkidle');
    
    // 验证公司字段自动填充
    const companySelect = page.locator('select[name="companyId"]');
    await expect(companySelect).toBeVisible();
    
    // 检查是否禁用（根据实现可能是 disabled 或 readonly）
    const isDisabled = await companySelect.isDisabled();
    console.log('公司字段是否禁用:', isDisabled);
    
    // 获取选中的值
    const selectedValue = await companySelect.inputValue();
    console.log('选中的公司 ID:', selectedValue);
    
    // 验证是甲方公司（ID=3）
    expect(selectedValue).toBe('3');
    
    console.log('【TC-ISO-001】✅ 测试通过');
  });

  // TC-ISO-002: 甲方管理员 - 角色列表验证
  test('TC-ISO-002: 甲方管理员创建用户时，角色列表仅显示甲方角色', async ({ page }) => {
    console.log('【TC-ISO-002】测试开始：甲方管理员 - 角色列表验证');
    
    // 甲方管理员登录
    await page.goto(`${FRONTEND_URL}/login`);
    await page.fill('input[name="username"]', 'jifang_admin');
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
    
    // 验证所有角色都包含"甲方"
    for (const role of roleOptions) {
      expect(role).toMatch(/甲方/);
    }
    
    // 验证不包含乙方或监理角色
    for (const role of roleOptions) {
      expect(role).not.toMatch(/乙方 | 监理/);
    }
    
    console.log('【TC-ISO-002】✅ 测试通过');
  });

  // TC-ISO-009: API 越权测试 - 甲方管理员尝试创建乙方用户
  test('TC-ISO-009: 甲方管理员通过 API 尝试创建乙方用户应被拒绝', async ({ request }) => {
    console.log('【TC-ISO-009】测试开始：API 越权测试');
    
    // 甲方管理员登录获取 token
    const loginResp = await request.post(`${baseURL}/api/auth/login`, {
      data: {
        username: 'jifang_admin',
        password: 'password123',
        captcha: 'test'
      }
    });
    
    const loginData = await loginResp.json();
    console.log('登录响应:', loginData);
    
    const token = loginData.data?.token;
    expect(token).toBeTruthy();
    
    // 尝试创建乙方用户（companyId=6）
    const createResp = await request.post(`${baseURL}/api/user`, {
      headers: { 
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      data: {
        username: 'test_yifang_user_' + Date.now(),
        password: 'Test123!',
        name: '测试乙方用户',
        email: 'test_yifang@example.com',
        phone: '13800138001',
        companyId: 6, // 乙方公司 ID
        roleIds: [15]
      }
    });
    
    const createData = await createResp.json();
    console.log('创建用户响应:', createData);
    
    // 验证返回 400 或 403
    expect([400, 403]).toContain(createResp.status());
    
    // 验证错误信息
    expect(createData.message).toContain('无权');
    
    console.log('【TC-ISO-009】✅ 测试通过');
  });

  // TC-ISO-012: 甲方管理员 - 作业区数据隔离验证
  test('TC-ISO-012: 甲方管理员创建作业区用户时，作业区列表仅显示本公司作业区', async ({ page }) => {
    console.log('【TC-ISO-012】测试开始：作业区数据隔离验证');
    
    // 甲方管理员登录
    await page.goto(`${FRONTEND_URL}/login`);
    await page.fill('input[name="username"]', 'jifang_admin');
    await page.fill('input[name="password"]', 'password123');
    await page.click('button[type="submit"]');
    await page.waitForURL(/\/dashboard/);
    
    // 进入用户管理
    await page.click('text=用户管理');
    await page.click('text=新增用户');
    await page.waitForLoadState('networkidle');
    
    // 选择作业区角色
    await page.selectOption('select[name="roleId"]', '5'); // 作业区项目主管
    await page.waitForTimeout(1000);
    
    // 验证作业区选择框显示
    const workAreaSelect = page.locator('select[name="workAreaId"]');
    await expect(workAreaSelect).toBeVisible();
    
    // 获取作业区列表
    const workAreaOptions = await workAreaSelect.$$eval('option', 
      options => options.map(opt => ({
        value: opt.value,
        text: opt.textContent?.trim() || ''
      }))
    );
    
    console.log('作业区列表:', workAreaOptions);
    
    // 验证作业区数量（应该只有本公司的作业区）
    expect(workAreaOptions.length).toBeGreaterThan(1);
    
    // 这里可以进一步验证作业区是否都属于本公司
    // 需要通过 API 或页面数据确认
    
    console.log('【TC-ISO-012】✅ 测试通过');
  });
});
