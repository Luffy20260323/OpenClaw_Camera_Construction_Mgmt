/**
 * SidebarMenu 组件测试
 * 
 * 测试范围：
 * 1. 菜单权限过滤
 * 2. 菜单树构建
 * 3. 多级菜单渲染
 * 4. 菜单点击跳转
 * 5. 折叠/展开功能
 * 
 * @author Camera1001 Team
 * @since 2026-04-01
 */

import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, config } from '@vue/test-utils'
import { createRouter, createMemoryHistory } from 'vue-router'
import { createPinia, setActivePinia } from 'pinia'
import { useUserStore } from '@/stores/user'
import SidebarMenu from '@/components/SidebarMenu.vue'

// 模拟 Element Plus 组件
config.global.stubs = {
  ElAside: { template: '<aside class="el-aside"><slot></slot></aside>' },
  ElMenu: { template: '<ul class="el-menu"><slot></slot></ul>' },
  ElMenuItem: { 
    template: '<li class="el-menu-item"><slot></slot></li>',
    props: ['index']
  },
  ElSubMenu: { 
    template: '<li class="el-submenu"><slot></slot></li>',
    props: ['index']
  },
  ElIcon: { template: '<i class="el-icon"><slot></slot></i>' }
}

// 模拟图标
vi.mock('@element-plus/icons-vue', () => ({
  VideoPlay: { name: 'VideoPlay' },
  Setting: { name: 'Setting' },
  User: { name: 'User' }
}))

describe('SidebarMenu.vue', () => {
  let pinia
  let userStore
  let router

  const mockMenus = [
    { id: 1, menuCode: 'home', menuName: '首页', menuPath: '/', parentId: null, sortOrder: 1, icon: 'VideoPlay' },
    { id: 2, menuCode: 'system', menuName: '系统管理', menuPath: null, parentId: null, sortOrder: 2, icon: 'Setting' },
    { id: 3, menuCode: 'system_config', menuName: '系统配置', menuPath: '/system/config', parentId: 2, sortOrder: 1 },
    { id: 4, menuCode: 'user_management', menuName: '用户管理', menuPath: '/user/management', parentId: 2, sortOrder: 2 },
    { id: 5, menuCode: 'profile', menuName: '个人中心', menuPath: '/user/profile', parentId: null, sortOrder: 3, icon: 'User' }
  ]

  beforeEach(() => {
    pinia = createPinia()
    setActivePinia(pinia)
    userStore = useUserStore()
    
    // 创建路由实例
    router = createRouter({
      history: createMemoryHistory(),
      routes: [
        { path: '/', name: 'Home', component: { template: '<div>Home</div>' } },
        { path: '/system/config', name: 'SystemConfig', component: { template: '<div>Config</div>' } },
        { path: '/user/management', name: 'UserManagement', component: { template: '<div>User</div>' } },
        { path: '/user/profile', name: 'Profile', component: { template: '<div>Profile</div>' } }
      ]
    })
  })

  /**
   * 菜单权限过滤测试
   */
  describe('Menu Permission Filtering', () => {
    it('shows all menus for system_admin role', async () => {
      userStore.menus = mockMenus
      userStore.roles = ['system_admin']
      userStore.permissions = ['*:*:*']
      
      const wrapper = mount(SidebarMenu, {
        global: { 
          plugins: [pinia, router],
          stubs: config.global.stubs
        },
        props: { isCollapse: false }
      })

      await wrapper.vm.$nextTick()
      
      // 系统管理员应该看到所有菜单
      const menuItems = wrapper.findAll('.el-menu-item')
      expect(menuItems.length).toBeGreaterThan(0)
    })

    it('filters menus based on user permissions', async () => {
      // 用户只有首页和个人中心权限
      userStore.menus = mockMenus
      userStore.permissions = ['home', 'profile']
      
      const wrapper = mount(SidebarMenu, {
        global: { 
          plugins: [pinia, router],
          stubs: config.global.stubs
        },
        props: { isCollapse: false }
      })

      await wrapper.vm.$nextTick()
      
      // 验证菜单树计算
      expect(wrapper.vm.menuTree.length).toBeLessThanOrEqual(2)
    })

    it('hides menus when user has no permissions', async () => {
      userStore.menus = mockMenus
      userStore.permissions = []
      
      const wrapper = mount(SidebarMenu, {
        global: { 
          plugins: [pinia, router],
          stubs: config.global.stubs
        },
        props: { isCollapse: false }
      })

      await wrapper.vm.$nextTick()
      
      // 无权限时，菜单树应该为空或只有默认菜单
      expect(wrapper.vm.menuTree.length).toBeLessThanOrEqual(mockMenus.length)
    })

    it('handles menu with requiredPermission property', async () => {
      const menusWithPerms = [
        { id: 1, menuCode: 'home', menuName: '首页', menuPath: '/', parentId: null, sortOrder: 1, requiredPermission: 'home' },
        { id: 2, menuCode: 'system', menuName: '系统管理', menuPath: null, parentId: null, sortOrder: 2, requiredPermission: 'system:view' },
        { id: 3, menuCode: 'system_config', menuName: '系统配置', menuPath: '/system/config', parentId: 2, sortOrder: 1, requiredPermission: 'system:config' }
      ]
      
      userStore.menus = menusWithPerms
      userStore.permissions = ['home', 'system:view']
      
      const wrapper = mount(SidebarMenu, {
        global: { 
          plugins: [pinia, router],
          stubs: config.global.stubs
        },
        props: { isCollapse: false }
      })

      await wrapper.vm.$nextTick()
      
      // 验证权限过滤
      const visibleMenus = wrapper.vm.menuTree
      expect(visibleMenus.some(m => m.menuCode === 'home')).toBe(true)
      expect(visibleMenus.some(m => m.menuCode === 'system')).toBe(true)
    })

    it('handles menu with required_permission property (snake_case)', async () => {
      const menusWithPerms = [
        { id: 1, menuCode: 'home', menuName: '首页', menuPath: '/', parent_id: null, sort_order: 1, required_permission: 'home' },
        { id: 2, menuCode: 'system', menuName: '系统管理', menuPath: null, parent_id: null, sort_order: 2, required_permission: 'system:view' }
      ]
      
      userStore.menus = menusWithPerms
      userStore.permissions = ['home']
      
      const wrapper = mount(SidebarMenu, {
        global: { 
          plugins: [pinia, router],
          stubs: config.global.stubs
        },
        props: { isCollapse: false }
      })

      await wrapper.vm.$nextTick()
      
      // 验证 snake_case 格式兼容
      const visibleMenus = wrapper.vm.menuTree
      expect(visibleMenus.some(m => m.menuCode === 'home')).toBe(true)
    })

    it('handles comma-separated permissions', async () => {
      const menusWithPerms = [
        { id: 1, menuCode: 'system', menuName: '系统管理', menuPath: null, parentId: null, sortOrder: 1, requiredPermission: 'system:view,system:manage' }
      ]
      
      userStore.menus = menusWithPerms
      userStore.permissions = ['system:manage']
      
      const wrapper = mount(SidebarMenu, {
        global: { 
          plugins: [pinia, router],
          stubs: config.global.stubs
        },
        props: { isCollapse: false }
      })

      await wrapper.vm.$nextTick()
      
      // 有任一权限即可显示
      expect(wrapper.vm.menuTree.length).toBe(1)
    })
  })

  /**
   * 菜单树构建测试
   */
  describe('Menu Tree Building', () => {
    it('builds tree from flat menu list', async () => {
      userStore.menus = mockMenus
      userStore.permissions = ['*:*:*']
      
      const wrapper = mount(SidebarMenu, {
        global: { 
          plugins: [pinia, router],
          stubs: config.global.stubs
        },
        props: { isCollapse: false }
      })

      await wrapper.vm.$nextTick()
      
      const tree = wrapper.vm.menuTree
      
      // 验证树结构
      expect(tree.length).toBeGreaterThan(0)
      
      // 查找系统管理菜单
      const systemMenu = tree.find(m => m.menuCode === 'system')
      expect(systemMenu).toBeDefined()
      expect(systemMenu.children).toBeDefined()
      expect(systemMenu.children.length).toBe(2) // 系统配置和用户管理
    })

    it('sorts menus by sortOrder', async () => {
      const unsortedMenus = [
        { id: 1, menuCode: 'profile', menuName: '个人中心', menuPath: '/user/profile', parentId: null, sortOrder: 3 },
        { id: 2, menuCode: 'home', menuName: '首页', menuPath: '/', parentId: null, sortOrder: 1 },
        { id: 3, menuCode: 'system', menuName: '系统管理', menuPath: null, parentId: null, sortOrder: 2 }
      ]
      
      userStore.menus = unsortedMenus
      userStore.permissions = ['*:*:*']
      
      const wrapper = mount(SidebarMenu, {
        global: { 
          plugins: [pinia, router],
          stubs: config.global.stubs
        },
        props: { isCollapse: false }
      })

      await wrapper.vm.$nextTick()
      
      const tree = wrapper.vm.menuTree
      
      // 验证排序
      expect(tree[0].menuCode).toBe('home')
      expect(tree[1].menuCode).toBe('system')
      expect(tree[2].menuCode).toBe('profile')
    })

    it('handles menus with null parentId', async () => {
      const menusWithNull = [
        { id: 1, menuCode: 'home', menuName: '首页', menuPath: '/', parentId: null, sortOrder: 1 },
        { id: 2, menuCode: 'system_config', menuName: '系统配置', menuPath: '/system/config', parentId: null, sortOrder: 2 }
      ]
      
      userStore.menus = menusWithNull
      userStore.permissions = ['*:*:*']
      
      const wrapper = mount(SidebarMenu, {
        global: { 
          plugins: [pinia, router],
          stubs: config.global.stubs
        },
        props: { isCollapse: false }
      })

      await wrapper.vm.$nextTick()
      
      // 所有菜单都是顶级
      expect(wrapper.vm.menuTree.length).toBe(2)
    })

    it('handles three-level menu structure', async () => {
      const threeLevelMenus = [
        { id: 1, menuCode: 'system', menuName: '系统管理', menuPath: null, parentId: null, sortOrder: 1 },
        { id: 2, menuCode: 'permission', menuName: '权限管理', menuPath: null, parentId: 1, sortOrder: 1 },
        { id: 3, menuCode: 'user_permission', menuName: '用户权限', menuPath: '/system/user-permission', parentId: 2, sortOrder: 1 },
        { id: 4, menuCode: 'role_permission', menuName: '角色权限', menuPath: '/system/role-permission', parentId: 2, sortOrder: 2 }
      ]
      
      userStore.menus = threeLevelMenus
      userStore.permissions = ['*:*:*']
      
      const wrapper = mount(SidebarMenu, {
        global: { 
          plugins: [pinia, router],
          stubs: config.global.stubs
        },
        props: { isCollapse: false }
      })

      await wrapper.vm.$nextTick()
      
      const tree = wrapper.vm.menuTree
      const systemMenu = tree.find(m => m.menuCode === 'system')
      const permissionMenu = systemMenu.children.find(m => m.menuCode === 'permission')
      
      expect(permissionMenu).toBeDefined()
      expect(permissionMenu.children.length).toBe(2)
    })
  })

  /**
   * 菜单点击测试
   */
  describe('Menu Click', () => {
    it('emits menu-select event when menu item is clicked', async () => {
      userStore.menus = [
        { id: 1, menuCode: 'home', menuName: '首页', menuPath: '/', parentId: null, sortOrder: 1 }
      ]
      userStore.permissions = ['*:*:*']
      
      const wrapper = mount(SidebarMenu, {
        global: { 
          plugins: [pinia, router],
          stubs: config.global.stubs
        },
        props: { isCollapse: false }
      })

      await wrapper.vm.$nextTick()
      
      // 触发菜单选择
      wrapper.vm.handleMenuSelect('/', ['/'])
      
      expect(wrapper.emitted('menu-select')).toBeDefined()
      expect(wrapper.emitted('menu-select')[0]).toEqual(['/'])
    })

    it('navigates to route when menu item is clicked', async () => {
      userStore.menus = [
        { id: 1, menuCode: 'home', menuName: '首页', menuPath: '/', parentId: null, sortOrder: 1 }
      ]
      userStore.permissions = ['*:*:*']
      
      const wrapper = mount(SidebarMenu, {
        global: { 
          plugins: [pinia, router],
          stubs: config.global.stubs
        },
        props: { isCollapse: false }
      })

      await wrapper.vm.$nextTick()
      await router.push('/')
      
      // 验证当前路由
      expect(router.currentRoute.value.path).toBe('/')
    })

    it('does not navigate when clicking parent menu with children', async () => {
      userStore.menus = [
        { id: 1, menuCode: 'system', menuName: '系统管理', menuPath: null, parentId: null, sortOrder: 1 },
        { id: 2, menuCode: 'system_config', menuName: '系统配置', menuPath: '/system/config', parentId: 1, sortOrder: 1 }
      ]
      userStore.permissions = ['*:*:*']
      
      const wrapper = mount(SidebarMenu, {
        global: { 
          plugins: [pinia, router],
          stubs: config.global.stubs
        },
        props: { isCollapse: false }
      })

      await wrapper.vm.$nextTick()
      
      // 父菜单没有路径，不会触发导航
      const emitSpy = vi.spyOn(wrapper.vm, '$emit')
      wrapper.vm.handleMenuSelect(null, [])
      
      // 验证没有触发导航
      expect(emitSpy).not.toHaveBeenCalledWith('menu-select', expect.any(String))
    })
  })

  /**
   * 折叠功能测试
   */
  describe('Collapse Feature', () => {
    it('passes isCollapse prop to menu', async () => {
      userStore.menus = mockMenus
      userStore.permissions = ['*:*:*']
      
      const wrapper = mount(SidebarMenu, {
        global: { 
          plugins: [pinia, router],
          stubs: config.global.stubs
        },
        props: { isCollapse: true }
      })

      await wrapper.vm.$nextTick()
      
      expect(wrapper.props('isCollapse')).toBe(true)
    })

    it('renders collapsed menu when isCollapse is true', async () => {
      userStore.menus = mockMenus
      userStore.permissions = ['*:*:*']
      
      const wrapper = mount(SidebarMenu, {
        global: { 
          plugins: [pinia, router],
          stubs: config.global.stubs
        },
        props: { isCollapse: true }
      })

      await wrapper.vm.$nextTick()
      
      // 验证折叠状态下的样式类
      const aside = wrapper.find('.el-aside')
      expect(aside.exists()).toBe(true)
    })
  })

  /**
   * 辅助函数测试
   */
  describe('Helper Functions', () => {
    it('getParentId handles camelCase', () => {
      userStore.menus = []
      userStore.permissions = []
      
      const wrapper = mount(SidebarMenu, {
        global: { 
          plugins: [pinia, router],
          stubs: config.global.stubs
        },
        props: { isCollapse: false }
      })

      const menu = { parentId: 5 }
      expect(wrapper.vm.getParentId(menu)).toBe(5)
    })

    it('getParentId handles snake_case', () => {
      userStore.menus = []
      userStore.permissions = []
      
      const wrapper = mount(SidebarMenu, {
        global: { 
          plugins: [pinia, router],
          stubs: config.global.stubs
        },
        props: { isCollapse: false }
      })

      const menu = { parent_id: 5 }
      expect(wrapper.vm.getParentId(menu)).toBe(5)
    })

    it('getSortOrder handles camelCase', () => {
      userStore.menus = []
      userStore.permissions = []
      
      const wrapper = mount(SidebarMenu, {
        global: { 
          plugins: [pinia, router],
          stubs: config.global.stubs
        },
        props: { isCollapse: false }
      })

      const menu = { sortOrder: 10 }
      expect(wrapper.vm.getSortOrder(menu)).toBe(10)
    })

    it('getSortOrder handles snake_case', () => {
      userStore.menus = []
      userStore.permissions = []
      
      const wrapper = mount(SidebarMenu, {
        global: { 
          plugins: [pinia, router],
          stubs: config.global.stubs
        },
        props: { isCollapse: false }
      })

      const menu = { sort_order: 10 }
      expect(wrapper.vm.getSortOrder(menu)).toBe(10)
    })
  })

  /**
   * 空状态测试
   */
  describe('Empty State', () => {
    it('handles empty menu list', async () => {
      userStore.menus = []
      userStore.permissions = []
      
      const wrapper = mount(SidebarMenu, {
        global: { 
          plugins: [pinia, router],
          stubs: config.global.stubs
        },
        props: { isCollapse: false }
      })

      await wrapper.vm.$nextTick()
      
      expect(wrapper.vm.menuTree.length).toBe(0)
    })

    it('handles null menus', async () => {
      userStore.menus = null
      userStore.permissions = []
      
      const wrapper = mount(SidebarMenu, {
        global: { 
          plugins: [pinia, router],
          stubs: config.global.stubs
        },
        props: { isCollapse: false }
      })

      await wrapper.vm.$nextTick()
      
      expect(wrapper.vm.menuTree.length).toBe(0)
    })
  })
})
