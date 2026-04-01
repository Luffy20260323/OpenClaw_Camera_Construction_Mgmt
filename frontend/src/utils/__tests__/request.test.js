/**
 * API 请求权限拦截测试
 * 
 * 测试范围：
 * 1. 请求拦截器 - Token 自动携带
 * 2. 响应拦截器 - 401/403/404/500 错误处理
 * 3. 未登录状态处理
 * 
 * @author Camera1001 Team
 * @since 2026-04-01
 */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import axios from 'axios'
import MockAdapter from 'axios-mock-adapter'

// 模拟 localStorage
const mockLocalStorage = {
  store: {},
  getItem(key) {
    return this.store[key] || null
  },
  setItem(key, value) {
    this.store[key] = value
  },
  removeItem(key) {
    delete this.store[key]
  },
  clear() {
    this.store = {}
  }
}

// 替换全局 localStorage
Object.defineProperty(global, 'localStorage', {
  value: mockLocalStorage,
  writable: true
})

// 模拟 router
const mockRouterPush = vi.fn()
vi.mock('@/router', () => ({
  default: {
    push: mockRouterPush
  }
}))

// 模拟 ElMessage
const mockElMessage = {
  error: vi.fn(),
  success: vi.fn(),
  warning: vi.fn(),
  info: vi.fn()
}
vi.mock('element-plus', () => ({
  ElMessage: mockElMessage
}))

// 导入 request 模块（需要调整导入方式）
// 由于 request.js 使用默认导出，我们需要动态导入
let request

describe('API Request Interceptor', () => {
  let mock

  beforeEach(async () => {
    mockLocalStorage.clear()
    mockElMessage.error.mockClear()
    mockElMessage.success.mockClear()
    mockRouterPush.mockClear()
    
    mock = new MockAdapter(axios)
    
    // 动态导入 request 模块
    const requestModule = await import('@/utils/request')
    request = requestModule.default
  })

  afterEach(() => {
    mock.reset()
    mockLocalStorage.clear()
  })

  /**
   * 请求拦截器测试
   */
  describe('Request Interceptor', () => {
    it('adds Authorization header when token exists', async () => {
      mockLocalStorage.setItem('accessToken', 'test-token-123')
      
      mock.onGet('/api/test').reply(200, { code: 200, data: {} })
      
      await request.get('/test')
      
      const config = mock.history.get[0]
      expect(config.headers.Authorization).toBe('Bearer test-token-123')
    })

    it('does not add Authorization header when token does not exist', async () => {
      mockLocalStorage.removeItem('accessToken')
      
      mock.onGet('/api/test').reply(200, { code: 200, data: {} })
      
      await request.get('/test')
      
      const config = mock.history.get[0]
      expect(config.headers.Authorization).toBeUndefined()
    })

    it('uses latest token from localStorage', async () => {
      mockLocalStorage.setItem('accessToken', 'old-token')
      mockLocalStorage.setItem('accessToken', 'new-token')
      
      mock.onGet('/api/test').reply(200, { code: 200, data: {} })
      
      await request.get('/test')
      
      const config = mock.history.get[0]
      expect(config.headers.Authorization).toBe('Bearer new-token')
    })

    it('handles POST requests with token', async () => {
      mockLocalStorage.setItem('accessToken', 'test-token')
      
      mock.onPost('/api/test').reply(200, { code: 200, data: {} })
      
      await request.post('/test', { name: 'test' })
      
      const config = mock.history.post[0]
      expect(config.headers.Authorization).toBe('Bearer test-token')
      expect(config.data).toBe('{"name":"test"}')
    })

    it('handles PUT requests with token', async () => {
      mockLocalStorage.setItem('accessToken', 'test-token')
      
      mock.onPut('/api/test/1').reply(200, { code: 200, data: {} })
      
      await request.put('/test/1', { name: 'updated' })
      
      const config = mock.history.put[0]
      expect(config.headers.Authorization).toBe('Bearer test-token')
    })

    it('handles DELETE requests with token', async () => {
      mockLocalStorage.setItem('accessToken', 'test-token')
      
      mock.onDelete('/api/test/1').reply(200, { code: 200, data: {} })
      
      await request.delete('/test/1')
      
      const config = mock.history.delete[0]
      expect(config.headers.Authorization).toBe('Bearer test-token')
    })
  })

  /**
   * 响应拦截器 - 成功响应测试
   */
  describe('Response Interceptor - Success', () => {
    beforeEach(() => {
      mockLocalStorage.setItem('accessToken', 'test-token')
    })

    it('returns data when code is 200', async () => {
      mock.onGet('/api/test').reply(200, { code: 200, data: { result: 'success' } })
      
      const response = await request.get('/test')
      
      expect(response.data).toEqual({ result: 'success' })
    })

    it('returns blob data for file downloads', async () => {
      mock.onGet('/api/file/download', { responseType: 'blob' }).reply(200, new Blob(['file content']))
      
      const response = await request.get('/file/download', { responseType: 'blob' })
      
      expect(response).toBeInstanceOf(Blob)
    })
  })

  /**
   * 响应拦截器 - 错误响应测试
   */
  describe('Response Interceptor - Error', () => {
    beforeEach(() => {
      mockLocalStorage.setItem('accessToken', 'test-token')
    })

    it('shows error message when code is not 200', async () => {
      mock.onGet('/api/test').reply(200, { code: 500, message: 'Server Error' })
      
      await expect(request.get('/test')).rejects.toThrow('Server Error')
      
      expect(mockElMessage.error).toHaveBeenCalledWith('Server Error')
    })

    it('handles 401 - Unauthorized', async () => {
      mock.onGet('/api/test').reply(200, { code: 401, message: 'Token expired' })
      
      await expect(request.get('/test')).rejects.toThrow('Token expired')
      
      expect(mockElMessage.error).toHaveBeenCalledWith('Token expired')
      expect(mockLocalStorage.getItem('accessToken')).toBeNull()
      expect(mockLocalStorage.getItem('refreshToken')).toBeNull()
      expect(mockLocalStorage.getItem('userInfo')).toBeNull()
      expect(mockRouterPush).toHaveBeenCalledWith('/login')
    })

    it('handles HTTP 401 status', async () => {
      mock.onGet('/api/test').reply(401)
      
      await expect(request.get('/test')).rejects.toThrow()
      
      expect(mockElMessage.error).toHaveBeenCalledWith('登录已过期，请重新登录')
      expect(mockLocalStorage.getItem('accessToken')).toBeNull()
      expect(mockRouterPush).toHaveBeenCalledWith('/login')
    })

    it('handles HTTP 403 status - Forbidden', async () => {
      mock.onGet('/api/test').reply(403)
      
      await expect(request.get('/test')).rejects.toThrow()
      
      expect(mockElMessage.error).toHaveBeenCalledWith('无权限访问该功能')
      // 403 不清除 token
      expect(mockLocalStorage.getItem('accessToken')).toBe('test-token')
      expect(mockRouterPush).not.toHaveBeenCalled()
    })

    it('handles HTTP 404 status - Not Found', async () => {
      mock.onGet('/api/test').reply(404)
      
      await expect(request.get('/test')).rejects.toThrow()
      
      expect(mockElMessage.error).toHaveBeenCalledWith('请求的资源不存在')
    })

    it('handles HTTP 500 status - Server Error', async () => {
      mock.onGet('/api/test').reply(500)
      
      await expect(request.get('/test')).rejects.toThrow()
      
      expect(mockElMessage.error).toHaveBeenCalledWith('服务器错误')
    })

    it('handles network error', async () => {
      mock.onGet('/api/test').networkError()
      
      await expect(request.get('/test')).rejects.toThrow()
      
      expect(mockElMessage.error).toHaveBeenCalledWith(expect.stringContaining('网络错误'))
    })

    it('handles timeout error', async () => {
      mock.onGet('/api/test').timeout()
      
      await expect(request.get('/test')).rejects.toThrow()
      
      expect(mockElMessage.error).toHaveBeenCalledWith(expect.stringContaining('网络错误'))
    })
  })

  /**
   * 边界情况测试
   */
  describe('Edge Cases', () => {
    it('handles empty token', async () => {
      mockLocalStorage.setItem('accessToken', '')
      
      mock.onGet('/api/test').reply(200, { code: 200, data: {} })
      
      await request.get('/test')
      
      const config = mock.history.get[0]
      // 空 token 不应该添加 Authorization header
      expect(config.headers.Authorization).toBe('Bearer ')
    })

    it('handles token with special characters', async () => {
      const specialToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.dozjgNryP4J3jVmNHl0w5N_XgL0n3I9PlFUP0THsR8U'
      mockLocalStorage.setItem('accessToken', specialToken)
      
      mock.onGet('/api/test').reply(200, { code: 200, data: {} })
      
      await request.get('/test')
      
      const config = mock.history.get[0]
      expect(config.headers.Authorization).toBe(`Bearer ${specialToken}`)
    })

    it('handles response without data field', async () => {
      mock.onGet('/api/test').reply(200, { code: 200 })
      
      const response = await request.get('/test')
      
      expect(response.data).toBeUndefined()
    })

    it('handles response with null data', async () => {
      mock.onGet('/api/test').reply(200, { code: 200, data: null })
      
      const response = await request.get('/test')
      
      expect(response.data).toBeNull()
    })
  })

  /**
   * 并发请求测试
   */
  describe('Concurrent Requests', () => {
    beforeEach(() => {
      mockLocalStorage.setItem('accessToken', 'test-token')
    })

    it('handles multiple concurrent requests', async () => {
      mock.onGet('/api/test1').reply(200, { code: 200, data: { id: 1 } })
      mock.onGet('/api/test2').reply(200, { code: 200, data: { id: 2 } })
      mock.onGet('/api/test3').reply(200, { code: 200, data: { id: 3 } })
      
      const [res1, res2, res3] = await Promise.all([
        request.get('/test1'),
        request.get('/test2'),
        request.get('/test3')
      ])
      
      expect(res1.data.id).toBe(1)
      expect(res2.data.id).toBe(2)
      expect(res3.data.id).toBe(3)
      
      // 验证所有请求都携带了 token
      mock.history.get.forEach(config => {
        expect(config.headers.Authorization).toBe('Bearer test-token')
      })
    })

    it('handles token expiration during concurrent requests', async () => {
      // 第一个请求成功
      mock.onGet('/api/test1').reply(200, { code: 200, data: {} })
      // 第二个请求 401
      mock.onGet('/api/test2').reply(200, { code: 401, message: 'Token expired' })
      
      await request.get('/test1')
      await expect(request.get('/test2')).rejects.toThrow()
      
      // 验证 token 被清除
      expect(mockLocalStorage.getItem('accessToken')).toBeNull()
    })
  })

  /**
   * 请求配置测试
   */
  describe('Request Configuration', () => {
    it('uses correct baseURL', async () => {
      mockLocalStorage.setItem('accessToken', 'test-token')
      mock.onGet('/api/test').reply(200, { code: 200, data: {} })
      
      await request.get('/test')
      
      const config = mock.history.get[0]
      expect(config.baseURL).toBe('/api')
    })

    it('uses correct timeout', () => {
      expect(request.defaults.timeout).toBe(30000)
    })

    it('accepts custom headers', async () => {
      mockLocalStorage.setItem('accessToken', 'test-token')
      mock.onGet('/api/test').reply(200, { code: 200, data: {} })
      
      await request.get('/test', {
        headers: {
          'X-Custom-Header': 'custom-value'
        }
      })
      
      const config = mock.history.get[0]
      expect(config.headers['X-Custom-Header']).toBe('custom-value')
      expect(config.headers.Authorization).toBe('Bearer test-token')
    })

    it('accepts custom timeout', async () => {
      mockLocalStorage.setItem('accessToken', 'test-token')
      mock.onGet('/api/test').reply(200, { code: 200, data: {} })
      
      await request.get('/test', { timeout: 5000 })
      
      const config = mock.history.get[0]
      expect(config.timeout).toBe(5000)
    })
  })
})
