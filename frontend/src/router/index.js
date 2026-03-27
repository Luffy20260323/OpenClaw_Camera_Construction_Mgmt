import { createRouter, createWebHistory } from 'vue-router'

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
    meta: { title: '首页', requiresAuth: true }
  },
  {
    path: '/system/config',
    name: 'SystemConfig',
    component: () => import('@/views/system/Config.vue'),
    meta: { title: '系统配置', requiresAuth: true }
  },
  {
    path: '/system/user-permission',
    name: 'UserPermission',
    component: () => import('@/views/system/UserPermission.vue'),
    meta: { title: '用户权限配置', requiresAuth: true }
  },
  {
    path: '/user/profile',
    name: 'UserProfile',
    component: () => import('@/views/user/Profile.vue'),
    meta: { title: '个人中心', requiresAuth: true }
  },
  {
    path: '/user/management',
    name: 'UserManagement',
    component: () => import('@/views/user/Management.vue'),
    meta: { title: '用户管理', requiresAuth: true }
  },
  {
    path: '/company',
    name: 'CompanyList',
    component: () => import('@/views/company/CompanyList.vue'),
    meta: { title: '公司管理', requiresAuth: true }
  },
  {
    path: '/workarea',
    name: 'WorkAreaList',
    component: () => import('@/views/workarea/WorkAreaList.vue'),
    meta: { title: '作业区管理', requiresAuth: true }
  },
  {
    path: '/role',
    name: 'RoleList',
    component: () => import('@/views/role/RoleList.vue'),
    meta: { title: '角色管理', requiresAuth: true }
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

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 视频监控点位施工项目管理系统` : '视频监控点位施工项目管理系统'
  
  // 检查是否需要登录
  const token = localStorage.getItem('accessToken')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
