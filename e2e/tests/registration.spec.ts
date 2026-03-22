/**
 * 用户注册 - 公司角色联动测试
 * 测试用例来源：docs/测试用例_用户注册_公司角色联动.md
 */

import { test, expect } from '@playwright/test';

// 配置 baseURL
const baseURL = process.env.BASE_URL || 'http://localhost:8080';
  const FRONTEND_URL = process.env.FRONTEND_URL || 'http://localhost:3000';

test.describe('用户注册 - 公司角色联动', () => {
  
  // UI-REG-001: 选择甲方公司 - 角色联动更新
  test('UI-REG-001: 选择甲方公司后，角色列表仅显示甲方角色', async ({ page }) => {
    console.log('【UI-REG-001】测试开始：选择甲方公司 - 角色联动更新');
    
    // 打开注册页面
    await page.goto(`${FRONTEND_URL}/login`);
    await page.click('text=注册');
    
    // 等待页面加载
    await page.waitForLoadState('networkidle');
    
    // 选择甲方公司（假设 ID=3 是测试甲方公司）
    await page.selectOption('select[name="companyId"]', '3');
    
    // 等待角色列表更新
    await page.waitForTimeout(1000);
    
    // 获取所有角色选项
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
    
    console.log('【UI-REG-001】✅ 测试通过');
  });

  // UI-REG-005: 选择甲方作业区角色 - 显示作业区选择框
  test('UI-REG-005: 选择甲方作业区角色后，显示作业区选择框', async ({ page }) => {
    console.log('【UI-REG-005】测试开始：选择甲方作业区角色 - 显示作业区');
    
    await page.goto(`${FRONTEND_URL}/login`);
    await page.click('text=注册');
    await page.waitForLoadState('networkidle');
    
    // 选择甲方公司
    await page.selectOption('select[name="companyId"]', '3');
    await page.waitForTimeout(500);
    
    // 选择作业区角色（假设 ID=5 是"作业区项目主管"）
    await page.selectOption('select[name="roleId"]', '5');
    await page.waitForTimeout(1000);
    
    // 验证作业区选择框显示
    const workAreaSelect = page.locator('select[name="workAreaId"]');
    await expect(workAreaSelect).toBeVisible();
    
    // 验证作业区列表不为空
    const workAreaOptions = await workAreaSelect.locator('option').all();
    expect(workAreaOptions.length).toBeGreaterThan(1);
    
    console.log('【UI-REG-005】✅ 测试通过');
  });

  // UI-REG-006: 选择甲方非作业区角色 - 不显示作业区选择框
  test('UI-REG-006: 选择甲方非作业区角色后，不显示作业区选择框', async ({ page }) => {
    console.log('【UI-REG-006】测试开始：选择甲方非作业区角色 - 不显示作业区');
    
    await page.goto(`${FRONTEND_URL}/login`);
    await page.click('text=注册');
    await page.waitForLoadState('networkidle');
    
    // 选择甲方公司
    await page.selectOption('select[name="companyId"]', '3');
    await page.waitForTimeout(500);
    
    // 选择非作业区角色（假设 ID=1 是"甲方管理员"）
    await page.selectOption('select[name="roleId"]', '1');
    await page.waitForTimeout(1000);
    
    // 验证作业区选择框不显示或不存在
    const workAreaSelect = page.locator('select[name="workAreaId"]');
    await expect(workAreaSelect).not.toBeVisible();
    
    console.log('【UI-REG-006】✅ 测试通过');
  });

  // UI-REG-007: 选择乙方角色 - 不显示作业区选择框
  test('UI-REG-007: 选择乙方公司角色后，不显示作业区选择框', async ({ page }) => {
    console.log('【UI-REG-007】测试开始：选择乙方角色 - 不显示作业区');
    
    await page.goto(`${FRONTEND_URL}/login`);
    await page.click('text=注册');
    await page.waitForLoadState('networkidle');
    
    // 选择乙方公司（假设 ID=6 是测试乙方公司）
    await page.selectOption('select[name="companyId"]', '6');
    await page.waitForTimeout(500);
    
    // 选择乙方角色（假设 ID=15 是"乙方施工队长"）
    await page.selectOption('select[name="roleId"]', '15');
    await page.waitForTimeout(1000);
    
    // 验证作业区选择框不显示
    const workAreaSelect = page.locator('select[name="workAreaId"]');
    await expect(workAreaSelect).not.toBeVisible();
    
    console.log('【UI-REG-007】✅ 测试通过');
  });

  // UI-REG-009: 切换角色 - 作业区框动态显示/隐藏
  test('UI-REG-009: 切换角色时，作业区选择框动态显示/隐藏', async ({ page }) => {
    console.log('【UI-REG-009】测试开始：切换角色 - 作业区框动态响应');
    
    await page.goto(`${FRONTEND_URL}/login`);
    await page.click('text=注册');
    await page.waitForLoadState('networkidle');
    
    // 选择甲方公司
    await page.selectOption('select[name="companyId"]', '3');
    await page.waitForTimeout(500);
    
    // 步骤 1: 选择作业区角色
    await page.selectOption('select[name="roleId"]', '5'); // 作业区项目主管
    await page.waitForTimeout(1000);
    
    // 验证作业区选择框显示
    const workAreaSelect = page.locator('select[name="workAreaId"]');
    await expect(workAreaSelect).toBeVisible();
    
    // 步骤 2: 切换到非作业区角色
    await page.selectOption('select[name="roleId"]', '1'); // 甲方管理员
    await page.waitForTimeout(1000);
    
    // 验证作业区选择框隐藏
    await expect(workAreaSelect).not.toBeVisible();
    
    // 步骤 3: 再次切换回作业区角色
    await page.selectOption('select[name="roleId"]', '5');
    await page.waitForTimeout(1000);
    
    // 验证作业区选择框重新显示
    await expect(workAreaSelect).toBeVisible();
    
    console.log('【UI-REG-009】✅ 测试通过');
  });

  // UI-REG-004: 切换公司 - 角色列表刷新
  test('UI-REG-004: 切换公司后，角色列表立即刷新', async ({ page }) => {
    console.log('【UI-REG-004】测试开始：切换公司 - 角色列表刷新');
    
    await page.goto(`${FRONTEND_URL}/login`);
    await page.click('text=注册');
    await page.waitForLoadState('networkidle');
    
    // 步骤 1: 选择甲方公司
    await page.selectOption('select[name="companyId"]', '3');
    await page.waitForTimeout(1000);
    
    // 获取甲方角色列表
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
    
    // 获取乙方角色列表
    const yifangRoles = await page.$$eval('select[name="roleId"] option', 
      options => options.map(opt => opt.textContent?.trim() || '').filter(text => text)
    );
    console.log('乙方角色:', yifangRoles);
    
    // 验证都是乙方角色
    for (const role of yifangRoles) {
      expect(role).toMatch(/乙方/);
    }
    
    // 验证不包含甲方角色
    for (const role of yifangRoles) {
      expect(role).not.toMatch(/甲方 | 监理/);
    }
    
    console.log('【UI-REG-004】✅ 测试通过');
  });
});
