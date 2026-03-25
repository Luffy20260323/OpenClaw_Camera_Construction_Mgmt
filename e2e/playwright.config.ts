import { defineConfig, devices } from '@playwright/test';

/**
 * Playwright 测试配置
 * 摄像头生命周期管理系统 - 前端自动化测试
 * 
 * 环境变量说明：
 * - BASE_URL: 前端基础 URL (默认：http://localhost:8080)
 * - FRONTEND_URL: 前端 URL (与 BASE_URL 相同，用于测试脚本)
 * - CI: 是否在 CI 环境中运行
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
    // 基础 URL - 优先使用环境变量
    // 优先级：BASE_URL > FRONTEND_URL > 默认值
    baseURL: process.env.BASE_URL || process.env.FRONTEND_URL || 'http://localhost:8080',
    
    // API 请求基础 URL
    extraHTTPHeaders: {
      'Accept': 'application/json',
    },
    
    // 截图
    screenshot: 'only-on-failure',
    
    // 视频
    video: 'retain-on-failure',
    
    // 追踪
    trace: 'retain-on-failure',
  },
  
  // 为 API 请求配置 baseURL
  expect: {
    timeout: 5000
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
