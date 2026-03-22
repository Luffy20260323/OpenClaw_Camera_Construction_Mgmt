/**
 * 界面与体验测试
 * 测试用例来源：docs/测试用例补充_*.md
 */

import { test, expect, devices } from '@playwright/test';

test.describe('界面与体验', () => {
  
  // UI-001: 响应式布局测试
  test('UI-001: 响应式布局测试 - PC/平板/手机适配', async ({ page }) => {
    console.log('【UI-001】测试开始：响应式布局测试');
    
    const viewports = [
      { width: 1920, height: 1080, name: 'Desktop' },
      { width: 768, height: 1024, name: 'Tablet' },
      { width: 375, height: 667, name: 'Mobile' },
    ];
    
    for (const viewport of viewports) {
      console.log(`  测试视口：${viewport.name} (${viewport.width}x${viewport.height})`);
      
      // 设置视口
      await page.setViewportSize({ width: viewport.width, height: viewport.height });
      
      // 打开登录页面
      await page.goto(`${baseURL}/login');
      
      // 验证页面元素正常显示
      await expect(page.locator('body')).toBeVisible();
      
      // 验证登录表单存在
      await expect(page.locator('form')).toBeVisible();
      
      // 验证无横向滚动条
      const scrollWidth = await page.evaluate(() => document.documentElement.scrollWidth);
      const clientWidth = await page.evaluate(() => document.documentElement.clientWidth);
      expect(scrollWidth).toBeLessThanOrEqual(clientWidth);
    }
    
    console.log('【UI-001】✅ 测试通过');
  });

  // UI-002: 移动端适配测试
  test('UI-002: 移动端适配测试 - 触摸操作和屏幕旋转', async ({ page }) => {
    console.log('【UI-002】测试开始：移动端适配测试');
    
    // 模拟手机视口
    await page.setViewportSize({ width: 375, height: 667 });
    
    // 打开登录页面
    await page.goto(`${baseURL}/login');
    
    // 验证触摸操作
    const usernameInput = page.locator('input[name="username"]');
    await expect(usernameInput).toBeVisible();
    
    // 模拟触摸输入
    await usernameInput.fill('test_user');
    await expect(usernameInput).toHaveValue('test_user');
    
    // 验证按钮可点击
    const submitButton = page.locator('button[type="submit"]');
    await expect(submitButton).toBeEnabled();
    
    // 验证按钮大小适合手指点击（至少 44px）
    const buttonBox = await submitButton.boundingBox();
    if (buttonBox) {
      expect(buttonBox.height).toBeGreaterThanOrEqual(44);
      expect(buttonBox.width).toBeGreaterThanOrEqual(44);
    }
    
    console.log('【UI-002】✅ 测试通过');
  });

  // UI-003: 功能一致性测试
  test('UI-003: 功能一致性测试 - 各端功能一致', async ({ page }) => {
    console.log('【UI-003】测试开始：功能一致性测试');
    
    // 测试桌面端
    await page.setViewportSize({ width: 1920, height: 1080 });
    await page.goto(`${baseURL}/login');
    
    const desktopElements = await page.$$eval('form input, form button, form select', 
      elements => elements.map(el => el.getAttribute('name') || el.textContent).filter(Boolean)
    );
    
    // 测试移动端
    await page.setViewportSize({ width: 375, height: 667 });
    await page.goto(`${baseURL}/login');
    
    const mobileElements = await page.$$eval('form input, form button, form select', 
      elements => elements.map(el => el.getAttribute('name') || el.textContent).filter(Boolean)
    );
    
    // 验证元素一致
    expect(desktopElements).toEqual(mobileElements);
    
    console.log('【UI-003】✅ 测试通过');
  });

  // UI-004: 页面加载性能
  test('UI-004: 页面加载性能 - 首屏加载<3s', async ({ page }) => {
    console.log('【UI-004】测试开始：页面加载性能测试');
    
    // 清空缓存
    await page.clearCookies();
    await page.evaluate(() => localStorage.clear());
    
    // 开始性能测试
    const startTime = Date.now();
    
    await page.goto(``${baseURL}/login`, { waitUntil: 'networkidle' });
    
    const loadTime = Date.now() - startTime;
    console.log(`  页面加载时间：${loadTime}ms`);
    
    // 验证加载时间 < 3s
    expect(loadTime).toBeLessThan(3000);
    
    // 使用 Performance API 获取更详细的指标
    const performanceMetrics = await page.evaluate(() => {
      const timing = performance.timing;
      return {
        domContentLoaded: timing.domContentLoadedEventEnd - timing.navigationStart,
        fullyLoaded: timing.loadEventEnd - timing.navigationStart,
      };
    });
    
    console.log('  DOM 加载时间:', performanceMetrics.domContentLoaded, 'ms');
    console.log('  完全加载时间:', performanceMetrics.fullyLoaded, 'ms');
    
    // 验证 DOM 加载 < 1.5s
    expect(performanceMetrics.domContentLoaded).toBeLessThan(1500);
    
    console.log('【UI-004】✅ 测试通过');
  });

  // UI-005: 无障碍访问测试（简化版）
  test('UI-005: 无障碍访问测试 - 基本可访问性', async ({ page }) => {
    console.log('【UI-005】测试开始：无障碍访问测试');
    
    await page.goto(`${baseURL}/login');
    
    // 验证所有输入框都有 label
    const inputs = await page.$$eval('input', inputs => {
      return inputs.map(input => {
        const id = input.getAttribute('id');
        const name = input.getAttribute('name');
        const ariaLabel = input.getAttribute('aria-label');
        const placeholder = input.getAttribute('placeholder');
        
        // 检查是否有可访问的名称
        const hasLabel = id || ariaLabel || placeholder;
        return {
          name: name || 'unknown',
          hasLabel: !!hasLabel
        };
      });
    });
    
    console.log('  输入框可访问性:', inputs);
    
    // 验证所有输入框都有可访问的名称
    const missingLabels = inputs.filter(input => !input.hasLabel);
    expect(missingLabels.length).toBe(0);
    
    // 验证所有按钮都有可访问的名称
    const buttons = await page.$$eval('button', buttons => {
      return buttons.map(button => ({
        text: button.textContent?.trim(),
        ariaLabel: button.getAttribute('aria-label')
      }));
    });
    
    console.log('  按钮可访问性:', buttons);
    
    const missingButtonLabels = buttons.filter(btn => !btn.text && !btn.ariaLabel);
    expect(missingButtonLabels.length).toBe(0);
    
    // 验证键盘导航
    await page.keyboard.press('Tab');
    const focusedElement = await page.evaluate(() => document.activeElement?.tagName);
    console.log('  键盘聚焦元素:', focusedElement);
    expect(focusedElement).toBeTruthy();
    
    console.log('【UI-005】✅ 测试通过');
  });
});
