import { createRouter, createWebHistory } from 'vue-router'

// 菜单编码与路由的映射关系
const MENU_CODE_MAP = {
  '/': 'home',
  '/home': 'home',
  '/system/config': 'system_config',
  '/system/user-permission': 'user_permission',
  '/user/profile': 'profile',
  '/user/management': 'user_management',
  '/company': 'company_management',
  '/workarea': 'workarea_management',
  '/role': 'role_management'
}

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { title: '首页', requiresAuth: true, menuCode: 'home' }
  },
  {
    path: '/system/config',
    name: 'SystemConfig',
    component: () => import('@/views/system/Config.vue'),
    meta: { title: '系统配置', requiresAuth: true, menuCode: 'system_config' }
  },
  {
    path: '/system/user-permission',
    name: 'UserPermission',
    component: () => import('@/views/system/UserPermission.vue'),
    meta: { title: '用户权限配置', requiresAuth: true, menuCode: 'user_permission' }
  },
  {
    path: '/user/profile',
    name: 'UserProfile',
    component: () => import('@/views/user/Profile.vue'),
    meta: { title: '个人中心', requiresAuth: true, menuCode: 'profile' }
  },
  {
    path: '/user/management',
    name: 'UserManagement',
    component: () => import('@/views/user/Management.vue'),
    meta: { title: '用户管理', requiresAuth: true, menuCode: 'user_management' }
  },
  {
    path: '/company',
    name: 'CompanyList',
    component: () => import('@/views/company/CompanyList.vue'),
    meta: { title: '公司管理', requiresAuth: true, menuCode: 'company_management' }
  },
  {
    path: '/workarea',
    name: 'WorkAreaList',
    component: () => import('@/views/workarea/WorkAreaList.vue'),
    meta: { title: '作业区管理', requiresAuth: true, menuCode: 'workarea_management' }
  },
  {
    path: '/role',
    name: 'RoleList',
    component: () => import('@/views/role/RoleList.vue'),
    meta: { title: '角色管理', requiresAuth: true, menuCode: 'role_management' }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '404' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫 - 增强权限验证
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 视频监控点位施工项目管理系统` : '视频监控点位施工项目管理系统'
  
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
        console.warn(`用户无权限访问 ${to.path}，重定向到首页`)
        next({ name: 'Home' })
        return
      }
    } catch (error) {
      console.error('路由权限检查失败:', error)
      // 出错时允许访问，避免死循环
      next()
      return
    }
  }
  
  next()
})

export default router
