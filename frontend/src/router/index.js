import { createRouter, createWebHistory } from 'vue-router'

// 菜单编码与路由的映射关系
const MENU_CODE_MAP = {
  '/': 'home',
  '/home': 'home',
  '/system/config': 'system_config',
  '/system/menu': 'menu_management',
  '/system/base-permission': 'base_permission',
  '/system/user-permission': 'user_permission',
  '/system/user-permission-detail': 'user_permission_detail',
  '/system/role-permission': 'role_permission',
  '/system/role-default': 'role_default_permission',
  '/system/data-permission': 'data_permission',
  '/system/audit-log': 'audit_log',
  '/system/permission-audit-log': 'permission_audit_log',
  '/system/docs': 'system_docs',
  '/system/component-types': 'component_types',
  '/system/component-attr-sets': 'component_attr_sets',
  '/system/point-device-models': 'point_device_models',
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
    path: '/system/menu',
    name: 'MenuManagement',
    component: () => import('@/views/system/MenuManagement.vue'),
    meta: { title: '菜单管理', requiresAuth: true, menuCode: 'menu_management' }
  },
  {
    path: '/system/base-permission',
    name: 'BasePermission',
    component: () => import('@/views/system/BasePermission.vue'),
    meta: { title: '基本权限配置', requiresAuth: true, menuCode: 'base_permission' }
  },
  {
    path: '/system/user-permission',
    name: 'UserPermission',
    component: () => import('@/views/system/UserPermission.vue'),
    meta: { title: '用户权限配置', requiresAuth: true, menuCode: 'user_permission' }
  },
  {
    path: '/system/user-permission-detail',
    name: 'UserPermissionDetail',
    component: () => import('@/views/system/UserPermissionDetail.vue'),
    meta: { title: '用户权限查看', requiresAuth: true, menuCode: 'user_permission_detail' }
  },
  {
    path: '/system/role-permission',
    name: 'RolePermission',
    component: () => import('@/views/system/RolePermission.vue'),
    meta: { title: '角色权限配置', requiresAuth: true, menuCode: 'role_permission' }
  },
  {
    path: '/system/role-default',
    name: 'RoleDefaultPermission',
    component: () => import('@/views/system/RoleDefaultPermission.vue'),
    meta: { title: '角色缺省权限', requiresAuth: true, menuCode: 'role_default_permission' }
  },
  {
    path: '/system/data-permission',
    name: 'DataPermission',
    component: () => import('@/views/system/DataPermissionConfig.vue'),
    meta: { title: '数据权限配置', requiresAuth: true, menuCode: 'data_permission' }
  },
  {
    path: '/system/audit-log',
    name: 'AuditLog',
    component: () => import('@/views/system/AuditLog.vue'),
    meta: { title: '审计日志', requiresAuth: true, menuCode: 'audit_log' }
  },
  {
    path: '/system/permission-audit-log',
    name: 'PermissionAuditLog',
    component: () => import('@/views/system/PermissionAuditLog.vue'),
    meta: { title: '权限审计日志', requiresAuth: true, menuCode: 'permission_audit_log' }
  },
  {
    path: '/system/docs',
    name: 'SystemDocs',
    component: () => import('@/views/system/Documents.vue'),
    meta: { title: '文档中心', requiresAuth: true, menuCode: 'system_docs' }
  },
  {
    path: '/system/component-types',
    name: 'ComponentTypes',
    component: () => import('@/views/system/ComponentTypeList.vue'),
    meta: { title: '零部件种类管理', requiresAuth: true, menuCode: 'component_types' }
  },
  {
    path: '/system/component-attr-sets',
    name: 'ComponentAttrSets',
    component: () => import('@/views/system/ComponentAttrSetList.vue'),
    meta: { title: '零部件属性集管理', requiresAuth: true, menuCode: 'component_attr_sets' }
  },
  {
    path: '/system/point-device-models',
    name: 'PointDeviceModels',
    component: () => import('@/views/system/PointDeviceModelList.vue'),
    meta: { title: '点位设备模型管理', requiresAuth: true, menuCode: 'point_device_models' }
  },
  {
    path: '/system/resources',
    name: 'ResourceManagement',
    component: () => import('@/views/system/ResourceManagement.vue'),
    meta: { title: '资源管理', requiresAuth: true, menuCode: 'resource_management' }
  },
  {
    path: '/system/component-attr-set-instances',
    name: 'ComponentAttrSetInstanceList',
    component: () => import('@/views/system/ComponentAttrSetInstanceList.vue'),
    meta: { title: '属性集实例管理', requiresAuth: true, menuCode: 'component_attr_set_instances' }
  },
  {
    path: '/system/component-types',
    name: 'ComponentTypeList',
    component: () => import('@/views/system/ComponentTypeList.vue'),
    meta: { title: '零部件种类管理', requiresAuth: true, menuCode: 'component_type_management' }
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
