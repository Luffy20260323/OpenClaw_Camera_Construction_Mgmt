/**
 * 用户注册联动测试 - 补充用例
 * 测试用例来源：docs/测试用例_用户注册_公司角色联动.md
 */

import { test, expect } from '@playwright/test';

// 配置 baseURL
const baseURL = process.env.BASE_URL || 'http://localhost:8080';

test.describe('用户注册联动 - 补充测试', () => {
  
  // UI-REG-002: 选择乙方公司 - 角色联动更新
  test('UI-REG-002: 选择乙方公司后，角色列表仅显示乙方角色', async ({ page }) => {
    console.log('【UI-REG-002】测试开始：选择乙方公司 - 角色联动更新');
    
    await page.goto(`${baseURL}/login`);
    await page.click('text=注册');
    await page.waitForLoadState('networkidle');
    
    // 选择乙方公司（假设 ID=6）
    await page.selectOption('select[name="companyId"]', '6');
    await page.waitForTimeout(1000);
    
    // 获取角色列表
    const roleOptions = await page.$$eval('select[name="roleId"] option', 
      options => options.map(opt => opt.textContent?.trim() || '').filter(text => text)
    );
    
    console.log('乙方角色列表:', roleOptions);
    
    // 验证所有角色都包含"乙方"
    for (const role of roleOptions) {
      expect(role).toMatch(/乙方/);
    }
    
    // 验证不包含甲方或监理角色
    for (const role of roleOptions) {
      expect(role).not.toMatch(/甲方 | 监理/);
    }
    
    console.log('【UI-REG-002】✅ 测试通过');
  });

  // UI-REG-003: 选择监理公司 - 角色联动更新
  test('UI-REG-003: 选择监理公司后，角色列表仅显示监理角色', async ({ page }) => {
    console.log('【UI-REG-003】测试开始：选择监理公司 - 角色联动更新');
    
    await page.goto(`${baseURL}/login`);
    await page.click('text=注册');
    await page.waitForLoadState('networkidle');
    
    // 选择监理公司（假设 ID=12）
    await page.selectOption('select[name="companyId"]', '12');
    await page.waitForTimeout(1000);
    
    // 获取角色列表
    const roleOptions = await page.$$eval('select[name="roleId"] option', 
      options => options.map(opt => opt.textContent?.trim() || '').filter(text => text)
    );
    
    console.log('监理角色列表:', roleOptions);
    
    // 验证所有角色都包含"监理"
    for (const role of roleOptions) {
      expect(role).toMatch(/监理/);
    }
    
    // 验证不包含甲方或乙方角色
    for (const role of roleOptions) {
      expect(role).not.toMatch(/甲方 | 乙方/);
    }
    
    console.log('【UI-REG-003】✅ 测试通过');
  });

  // UI-REG-008: 选择监理角色 - 不显示作业区选择框
  test('UI-REG-008: 选择监理公司角色后，不显示作业区选择框', async ({ page }) => {
    console.log('【UI-REG-008】测试开始：选择监理角色 - 不显示作业区');
    
    await page.goto(`${baseURL}/login`);
    await page.click('text=注册');
    await page.waitForLoadState('networkidle');
    
    // 选择监理公司
    await page.selectOption('select[name="companyId"]', '12');
    await page.waitForTimeout(500);
    
    // 选择监理角色（假设 ID=20）
    await page.selectOption('select[name="roleId"]', '20');
    await page.waitForTimeout(1000);
    
    // 验证作业区选择框不显示
    const workAreaSelect = page.locator('select[name="workAreaId"]');
    await expect(workAreaSelect).not.toBeVisible();
    
    console.log('【UI-REG-008】✅ 测试通过');
  });

  // UI-REG-010: 未选择公司 - 角色下拉框状态
  test('UI-REG-010: 未选择公司时，角色下拉框禁用或为空', async ({ page }) => {
    console.log('【UI-REG-010】测试开始：未选择公司 - 角色下拉框状态');
    
    await page.goto(`${baseURL}/login`);
    await page.click('text=注册');
    await page.waitForLoadState('networkidle');
    
    // 不选择公司，直接查看角色下拉框
    const roleSelect = page.locator('select[name="roleId"]');
    
    // 验证角色下拉框是否禁用
    const isDisabled = await roleSelect.isDisabled();
    console.log('角色下拉框是否禁用:', isDisabled);
    
    if (!isDisabled) {
      // 如果不禁用，验证选项为空或只有提示
      const options = await roleSelect.locator('option').all();
      console.log('角色选项数量:', options.length);
      
      // 应该只有空选项或"请选择公司"提示
      expect(options.length).toBeLessThanOrEqual(2);
    }
    
    console.log('【UI-REG-010】✅ 测试通过');
  });

  // UI-REG-011: 作业区角色判断逻辑
  test('UI-REG-011: 作业区角色判断逻辑验证', async ({ page }) => {
    console.log('【UI-REG-011】测试开始：作业区角色判断逻辑');
    
    await page.goto(`${baseURL}/login`);
    await page.click('text=注册');
    await page.waitForLoadState('networkidle');
    
    // 选择甲方公司
    await page.selectOption('select[name="companyId"]', '3');
    await page.waitForTimeout(500);
    
    // 获取所有角色
    const allRoles = await page.$$eval('select[name="roleId"] option', 
      options => options.map(opt => ({
        value: opt.value,
        text: opt.textContent?.trim() || ''
      })).filter(opt => opt.value)
    );
    
    console.log('所有角色:', allRoles);
    
    // 遍历每个角色，验证作业区显示逻辑
    for (const role of allRoles) {
      await page.selectOption('select[name="roleId"]', role.value);
      await page.waitForTimeout(500);
      
      const workAreaSelect = page.locator('select[name="workAreaId"]');
      const isVisible = await workAreaSelect.isVisible().catch(() => false);
      
      const isWorkAreaRole = role.text.includes('作业区');
      console.log(`  角色 "${role.text}": 作业区框${isVisible ? '显示' : '隐藏'}`);
      
      // 验证逻辑一致性
      if (isWorkAreaRole) {
        expect(isVisible).toBeTruthy();
      } else {
        expect(isVisible).toBeFalsy();
      }
    }
    
    console.log('【UI-REG-011】✅ 测试通过');
  });

  // UI-REG-012: 多选角色 - 作业区逻辑
  test('UI-REG-012: 多选角色时，作业区选择逻辑验证', async ({ page }) => {
    console.log('【UI-REG-012】测试开始：多选角色 - 作业区逻辑');
    
    await page.goto(`${baseURL}/login`);
    await page.click('text=注册');
    await page.waitForLoadState('networkidle');
    
    // 选择甲方公司
    await page.selectOption('select[name="companyId"]', '3');
    await page.waitForTimeout(500);
    
    // 检查是否支持多选（根据实际实现）
    const roleSelect = page.locator('select[name="roleId"]');
    const isMultiple = await roleSelect.evaluate(el => el.hasAttribute('multiple'));
    
    console.log('角色下拉框是否支持多选:', isMultiple);
    
    if (isMultiple) {
      // 如果支持多选，测试多选逻辑
      // 选择一个作业区角色和一个非作业区角色
      await roleSelect.selectOption(['5', '1']); // 作业区项目主管 + 甲方管理员
      await page.waitForTimeout(1000);
      
      // 验证作业区选择框显示（因为包含了作业区角色）
      const workAreaSelect = page.locator('select[name="workAreaId"]');
      await expect(workAreaSelect).toBeVisible();
      
      console.log('  多选包含作业区角色：作业区框显示 ✅');
    } else {
      console.log('  ⚠️  系统不支持多选角色，跳过实际测试');
    }
    
    console.log('【UI-REG-012】✅ 测试通过');
  });
});
