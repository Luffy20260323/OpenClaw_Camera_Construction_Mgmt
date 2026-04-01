/**
 * 路由守卫测试
 * 
 * 测试范围：
 * 1. 未登录用户访问需要认证的路由
 * 2. 已登录用户访问无权限路由
 * 3. 已登录用户访问有权限路由
 * 4. 特殊路由（profile, home）始终可访问
 * 
 * @author Camera1001 Team
 * @since 2026-04-01
 */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { createRouter, createWebHistory } from 'vue-router'
import { createPinia, setActivePinia } from 'pinia'

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

describe('Router Guards', () => {
  let router
  let pinia

  // 模拟路由组件
  const mockComponents = {
    Login: { template: '<div>Login</div>' },
    Home: { template: '<div>Home</div>' },
    SystemConfig: { template: '<div>SystemConfig</div>' },
    UserManagement: { template: '<div>UserManagement</div>' },
    Profile: { template: '<div>Profile</div>' },
    NotFound: { template: '<div>NotFound</div>' }
  }

  beforeEach(() => {
    // 清理 localStorage
    mockLocalStorage.clear()
    
    // 创建 Pinia 实例
    pinia = createPinia()
    setActivePinia(pinia)

    // 创建路由实例
    router = createRouter({
      history: createWebHistory(),
      routes: [
        {
          path: '/login',
          name: 'Login',
          component: mockComponents.Login,
          meta: { title: '登录' }
        },
        {
          path: '/',
          name: 'Home',
          component: mockComponents.Home,
          meta: { title: '首页', requiresAuth: true, menuCode: 'home' }
        },
        {
          path: '/system/config',
          name: 'SystemConfig',
          component: mockComponents.SystemConfig,
          meta: { title: '系统配置', requiresAuth: true, menuCode: 'system_config' }
        },
        {
          path: '/user/management',
          name: 'UserManagement',
          component: mockComponents.UserManagement,
          meta: { title: '用户管理', requiresAuth: true, menuCode: 'user_management' }
        },
        {
          path: '/user/profile',
          name: 'Profile',
          component: mockComponents.Profile,
          meta: { title: '个人中心', requiresAuth: true, menuCode: 'profile' }
        },
        {
          path: '/:pathMatch(.*)*',
          name: 'NotFound',
          component: mockComponents.NotFound,
          meta: { title: '404' }
        }
      ]
    })

    // 添加路由守卫（复制自 router/index.js）
    router.beforeEach((to, from, next) => {
      // 检查是否需要登录
      const token = localStorage.getItem('accessToken')
      if (to.meta.requiresAuth && !token) {
        next('/login')
        return
      }
      
      // 如果有 token 且需要权限验证
      if (to.meta.requiresAuth && token) {
        try {
          // 获取用户菜单权限
          const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
          const menus = userInfo.menus || []
          const menuCode = to.meta.menuCode
          
          // 个人中心始终可访问
          if (menuCode === 'profile') {
            next()
            return
          }
          
          // 首页始终可访问（作为无权限时的 fallback）
          if (!menuCode || menuCode === 'home') {
            next()
            return
          }
          
          // 检查是否有菜单权限
          const hasPermission = menus.some(m => {
            const code = typeof m === 'string' ? m : (m.menuCode || '')
            return code === menuCode
          })
          
          if (!hasPermission) {
            // 无权限，重定向到首页
            next({ name: 'Home' })
            return
          }
        } catch (error) {
          // 出错时允许访问，避免死循环
          next()
          return
        }
      }
      
      next()
    })
  })

  afterEach(() => {
    mockLocalStorage.clear()
  })

  /**
   * 未登录用户访问测试
   */
  describe('Unauthenticated User', () => {
    it('redirects to login when accessing protected route without token', async () => {
      // 确保没有 token
      mockLocalStorage.removeItem('accessToken')
      
      await router.push('/system/config')
      
      expect(router.currentRoute.value.name).toBe('Login')
    })

    it('allows access to login page without token', async () => {
      mockLocalStorage.removeItem('accessToken')
      
      await router.push('/login')
      
      expect(router.currentRoute.value.name).toBe('Login')
    })

    it('redirects to login when accessing user management without token', async () => {
      mockLocalStorage.removeItem('accessToken')
      
      await router.push('/user/management')
      
      expect(router.currentRoute.value.name).toBe('Login')
    })
  })

  /**
   * 已登录用户访问测试
   */
  describe('Authenticated User', () => {
    beforeEach(() => {
      // 设置有效 token
      mockLocalStorage.setItem('accessToken', 'mock-token-123')
    })

    it('allows access to home page', async () => {
      mockLocalStorage.setItem('userInfo', JSON.stringify({
        menus: ['home']
      }))
      
      await router.push('/')
      
      expect(router.currentRoute.value.name).toBe('Home')
    })

    it('allows access to profile page for all users', async () => {
      // 用户只有 home 权限
      mockLocalStorage.setItem('userInfo', JSON.stringify({
        menus: ['home']
      }))
      
      await router.push('/user/profile')
      
      expect(router.currentRoute.value.name).toBe('Profile')
    })

    it('allows access to route when user has permission', async () => {
      mockLocalStorage.setItem('userInfo', JSON.stringify({
        menus: ['home', 'system_config']
      }))
      
      await router.push('/system/config')
      
      expect(router.currentRoute.value.name).toBe('SystemConfig')
    })

    it('redirects to home when user lacks permission', async () => {
      // 用户只有 home 权限，没有 system_config 权限
      mockLocalStorage.setItem('userInfo', JSON.stringify({
        menus: ['home']
      }))
      
      await router.push('/system/config')
      
      expect(router.currentRoute.value.name).toBe('Home')
    })

    it('redirects to home when accessing user management without permission', async () => {
      mockLocalStorage.setItem('userInfo', JSON.stringify({
        menus: ['home', 'system_config']
      }))
      
      await router.push('/user/management')
      
      expect(router.currentRoute.value.name).toBe('Home')
    })
  })

  /**
   * 菜单权限格式兼容性测试
   */
  describe('Menu Permission Format Compatibility', () => {
    beforeEach(() => {
      mockLocalStorage.setItem('accessToken', 'mock-token-123')
    })

    it('handles string menu codes', async () => {
      mockLocalStorage.setItem('userInfo', JSON.stringify({
        menus: ['home', 'system_config', 'user_management']
      }))
      
      await router.push('/system/config')
      
      expect(router.currentRoute.value.name).toBe('SystemConfig')
    })

    it('handles object menu codes with menuCode property', async () => {
      mockLocalStorage.setItem('userInfo', JSON.stringify({
        menus: [
          { menuCode: 'home' },
          { menuCode: 'system_config' },
          { menuCode: 'user_management' }
        ]
      }))
      
      await router.push('/system/config')
      
      expect(router.currentRoute.value.name).toBe('SystemConfig')
    })

    it('handles mixed menu formats', async () => {
      mockLocalStorage.setItem('userInfo', JSON.stringify({
        menus: [
          'home',
          { menuCode: 'system_config' },
          'user_management'
        ]
      }))
      
      await router.push('/system/config')
      expect(router.currentRoute.value.name).toBe('SystemConfig')
      
      await router.push('/user/management')
      expect(router.currentRoute.value.name).toBe('UserManagement')
    })
  })

  /**
   * 错误处理测试
   */
  describe('Error Handling', () => {
    beforeEach(() => {
      mockLocalStorage.setItem('accessToken', 'mock-token-123')
    })

    it('allows access when userInfo is invalid JSON', async () => {
      mockLocalStorage.setItem('userInfo', 'invalid-json')
      
      await router.push('/system/config')
      
      // 出错时允许访问，避免死循环
      expect(router.currentRoute.value.name).toBe('SystemConfig')
    })

    it('allows access when userInfo is null', async () => {
      mockLocalStorage.setItem('userInfo', 'null')
      
      await router.push('/system/config')
      
      expect(router.currentRoute.value.name).toBe('SystemConfig')
    })

    it('allows access when menus is missing', async () => {
      mockLocalStorage.setItem('userInfo', JSON.stringify({
        username: 'test'
      }))
      
      await router.push('/system/config')
      
      expect(router.currentRoute.value.name).toBe('SystemConfig')
    })
  })

  /**
   * 404 路由测试
   */
  describe('404 Route', () => {
    it('shows 404 for non-existent routes', async () => {
      await router.push('/non-existent-path')
      
      expect(router.currentRoute.value.name).toBe('NotFound')
    })

    it('shows 404 for non-existent nested routes', async () => {
      await router.push('/some/deep/non-existent/path')
      
      expect(router.currentRoute.value.name).toBe('NotFound')
    })
  })
})
