/**
 * 权限工具函数测试
 * 
 * 测试范围：
 * 1. hasMenuPermission - 菜单权限检查
 * 2. hasMenuOperatePermission - 菜单操作权限检查
 * 3. hasPermission - 权限点检查
 * 4. hasAnyPermission - 任一权限检查
 * 5. hasAllPermissions - 所有权限检查
 * 
 * @author Camera1001 Team
 * @since 2026-04-01
 */

import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useUserStore } from '@/stores/user'
import { 
  hasMenuPermission, 
  hasMenuOperatePermission, 
  hasPermission,
  hasAnyPermission,
  hasAllPermissions 
} from '@/utils/permission'

describe('Permission Utils', () => {
  let userStore

  beforeEach(() => {
    setActivePinia(createPinia())
    userStore = useUserStore()
  })

  /**
   * hasMenuPermission 测试
   */
  describe('hasMenuPermission', () => {
    it('returns true when user has menu permission', () => {
      userStore.menus = ['home', 'system_config', 'user_management']
      expect(hasMenuPermission('system_config')).toBe(true)
    })

    it('returns false when user does not have menu permission', () => {
      userStore.menus = ['home', 'profile']
      expect(hasMenuPermission('user_management')).toBe(false)
    })

    it('handles menu objects with menuCode property', () => {
      userStore.menus = [
        { menuCode: 'home', menuName: '首页' },
        { menuCode: 'system_config', menuName: '系统配置' }
      ]
      expect(hasMenuPermission('system_config')).toBe(true)
      expect(hasMenuPermission('user_management')).toBe(false)
    })

    it('handles empty menus', () => {
      userStore.menus = []
      expect(hasMenuPermission('system_config')).toBe(false)
    })

    it('handles null/undefined menus', () => {
      userStore.menus = null
      expect(hasMenuPermission('system_config')).toBe(false)
      
      userStore.menus = undefined
      expect(hasMenuPermission('system_config')).toBe(false)
    })
  })

  /**
   * hasMenuOperatePermission 测试
   */
  describe('hasMenuOperatePermission', () => {
    it('returns true when user has operate permission', () => {
      userStore.menus = [
        { menuCode: 'user_management', userCanOperate: true },
        { menuCode: 'system_config', userCanOperate: false }
      ]
      expect(hasMenuOperatePermission('user_management')).toBe(true)
    })

    it('returns false when user does not have operate permission', () => {
      userStore.menus = [
        { menuCode: 'user_management', userCanOperate: false }
      ]
      expect(hasMenuOperatePermission('user_management')).toBe(false)
    })

    it('returns false when userCanOperate is not set', () => {
      userStore.menus = [
        { menuCode: 'user_management' }
      ]
      expect(hasMenuOperatePermission('user_management')).toBe(false)
    })

    it('returns false when menu not found', () => {
      userStore.menus = [
        { menuCode: 'home', userCanOperate: true }
      ]
      expect(hasMenuOperatePermission('user_management')).toBe(false)
    })

    it('returns false when menu is string type', () => {
      userStore.menus = ['home', 'system_config']
      expect(hasMenuOperatePermission('system_config')).toBe(false)
    })

    it('handles empty menus', () => {
      userStore.menus = []
      expect(hasMenuOperatePermission('system_config')).toBe(false)
    })
  })

  /**
   * hasPermission 测试
   */
  describe('hasPermission', () => {
    it('returns true when user has permission', () => {
      userStore.permissions = ['user:create', 'user:edit', 'user:delete']
      expect(hasPermission('user:create')).toBe(true)
    })

    it('returns false when user does not have permission', () => {
      userStore.permissions = ['user:view']
      expect(hasPermission('user:create')).toBe(false)
    })

    it('handles wildcard permission', () => {
      userStore.permissions = ['*:*:*']
      expect(hasPermission('user:create')).toBe(true)
      expect(hasPermission('system:config')).toBe(true)
    })

    it('handles empty permissions', () => {
      userStore.permissions = []
      expect(hasPermission('user:create')).toBe(false)
    })

    it('handles null/undefined permissions', () => {
      userStore.permissions = null
      expect(hasPermission('user:create')).toBe(false)
      
      userStore.permissions = undefined
      expect(hasPermission('user:create')).toBe(false)
    })
  })

  /**
   * hasAnyPermission 测试
   */
  describe('hasAnyPermission', () => {
    it('returns true when user has any of the permissions', () => {
      userStore.permissions = ['user:create', 'user:edit']
      expect(hasAnyPermission(['user:create', 'admin:manage'])).toBe(true)
    })

    it('returns false when user has none of the permissions', () => {
      userStore.permissions = ['user:view']
      expect(hasAnyPermission(['user:create', 'user:delete'])).toBe(false)
    })

    it('returns true with wildcard permission', () => {
      userStore.permissions = ['*:*:*']
      expect(hasAnyPermission(['user:create', 'admin:manage'])).toBe(true)
    })

    it('handles empty permission list', () => {
      userStore.permissions = ['user:create']
      expect(hasAnyPermission([])).toBe(false)
    })

    it('handles empty user permissions', () => {
      userStore.permissions = []
      expect(hasAnyPermission(['user:create', 'user:edit'])).toBe(false)
    })
  })

  /**
   * hasAllPermissions 测试
   */
  describe('hasAllPermissions', () => {
    it('returns true when user has all permissions', () => {
      userStore.permissions = ['user:create', 'user:edit', 'user:delete']
      expect(hasAllPermissions(['user:create', 'user:edit'])).toBe(true)
    })

    it('returns false when user missing some permissions', () => {
      userStore.permissions = ['user:create', 'user:view']
      expect(hasAllPermissions(['user:create', 'user:delete'])).toBe(false)
    })

    it('returns true with wildcard permission', () => {
      userStore.permissions = ['*:*:*']
      expect(hasAllPermissions(['user:create', 'user:edit', 'admin:manage'])).toBe(true)
    })

    it('returns true for empty permission list', () => {
      userStore.permissions = ['user:create']
      expect(hasAllPermissions([])).toBe(true)
    })

    it('handles empty user permissions', () => {
      userStore.permissions = []
      expect(hasAllPermissions(['user:create'])).toBe(false)
    })
  })
})
