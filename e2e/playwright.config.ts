import { defineConfig, devices } from '@playwright/test';

/**
 * Playwright 测试配置
 * 摄像头生命周期管理系统 - 前端自动化测试
 */
export default defineConfig({
  // 测试目录
  testDir: './e2e/tests',
  
  // 超时设置
  timeout: 30000,
  expect: {
    timeout: 5000
  },
  
  // 失败重试次数
  retries: process.env.CI ? 2 : 0,
  
  // 并行执行
  workers: process.env.CI ? 1 : undefined,
  
  // 报告器
  reporter: [
    ['html', { outputFolder: 'e2e/results/html' }],
    ['junit', { outputFile: 'e2e/results/results.xml' }],
    ['list']
  ],
  
  // 共享配置
  use: {
    // 基础 URL
    baseURL: process.env.CI ? 'http://localhost:8080' : 'http://localhost',
    
    // 截图
    screenshot: 'only-on-failure',
    
    // 视频
    video: 'retain-on-failure',
    
    // 追踪
    trace: 'retain-on-failure',
  },
  
  // 浏览器项目
  projects: [
    {
      name: 'chromium',
      use: { 
        ...devices['Desktop Chrome'],
        viewport: { width: 1920, height: 1080 }
      },
    },
    {
      name: 'Mobile Chrome',
      use: { 
        ...devices['Pixel 5'],
      },
    },
    {
      name: 'Mobile Safari',
      use: { 
        ...devices['iPhone 12'],
      },
    },
  ],
  
  // Web 服务器（可选，用于自动启动后端）
  // webServer: {
  //   command: 'cd backend && java -jar target/camera-lifecycle-system-1.0.0-SNAPSHOT.jar',
  //   url: 'http://localhost:8080/api/health',
  //   reuseExistingServer: !process.env.CI,
  // },
});
