/**
 * 权限与 API、菜单映射配置
 * 
 * 格式：
 * {
 *   '权限代码': {
 *     apis: ['GET /api/xxx', 'POST /api/xxx'],
 *     menus: ['菜单名称']
 *   }
 * }
 */
export const permissionMapping = {
  // ===== 认证模块 =====
  'auth:login': {
    apis: ['POST /api/auth/login'],
    menus: []
  },
  'auth:logout': {
    apis: ['POST /api/auth/logout'],
    menus: []
  },
  'auth:captcha:config': {
    apis: ['GET /api/auth/captcha/config'],
    menus: []
  },
  'auth:captcha:image': {
    apis: ['GET /api/auth/captcha/image'],
    menus: []
  },
  'auth:captcha:get': {
    apis: ['GET /api/auth/captcha'],
    menus: []
  },
  'auth:captcha:sms': {
    apis: ['POST /api/auth/captcha/sms'],
    menus: []
  },
  'auth:refresh': {
    apis: ['POST /api/auth/refresh'],
    menus: []
  },
  'auth:validate': {
    apis: ['POST /api/auth/validate'],
    menus: []
  },

  // ===== 用户模块 =====
  'user:profile:view': {
    apis: ['GET /api/user/profile'],
    menus: ['个人中心']
  },
  'user:profile:edit': {
    apis: ['PUT /api/user/profile'],
    menus: ['个人中心']
  },
  'user:password:change': {
    apis: ['PUT /api/user/password'],
    menus: ['个人中心']
  },
  'user:password:reset': {
    apis: ['PUT /api/user/{id}/password/reset'],
    menus: ['用户管理']
  },
  'user:register': {
    apis: ['POST /api/user/register'],
    menus: []
  },
  'user:create': {
    apis: ['POST /api/user'],
    menus: ['用户管理']
  },
  'user:import:json': {
    apis: ['POST /api/user/import/json'],
    menus: ['用户管理']
  },
  'user:import:template': {
    apis: ['GET /api/user/import/template'],
    menus: ['用户管理']
  },
  'user:import:excel': {
    apis: ['POST /api/user/import/excel'],
    menus: ['用户管理']
  },
  'user:approve': {
    apis: ['PUT /api/user/{id}/approve'],
    menus: ['用户管理']
  },
  'user:list': {
    apis: ['GET /api/user', 'GET /api/user/list'],
    menus: ['用户管理']
  },
  'user:pending:view': {
    apis: ['GET /api/user/pending'],
    menus: ['用户管理']
  },
  'user:view': {
    apis: ['GET /api/user/{id}'],
    menus: ['用户管理']
  },
  'user:edit': {
    apis: ['PUT /api/user/{id}'],
    menus: ['用户管理']
  },
  'user:delete': {
    apis: ['DELETE /api/user/{id}'],
    menus: ['用户管理']
  },

  // ===== 角色模块 =====
  'role:list': {
    apis: ['GET /api/role', 'GET /api/permission/roles'],
    menus: ['角色管理']
  },
  'role:view': {
    apis: ['GET /api/role/{id}'],
    menus: ['角色管理']
  },
  'role:create': {
    apis: ['POST /api/role'],
    menus: ['角色管理']
  },
  'role:edit': {
    apis: ['PUT /api/role/{id}'],
    menus: ['角色管理']
  },
  'role:delete': {
    apis: ['DELETE /api/role/{id}'],
    menus: ['角色管理']
  },

  // ===== 作业区模块 =====
  'workarea:list': {
    apis: ['GET /api/workarea'],
    menus: ['作业区管理']
  },
  'workarea:view': {
    apis: ['GET /api/workarea/{id}'],
    menus: ['作业区管理']
  },
  'workarea:create': {
    apis: ['POST /api/workarea'],
    menus: ['作业区管理']
  },
  'workarea:edit': {
    apis: ['PUT /api/workarea/{id}'],
    menus: ['作业区管理']
  },
  'workarea:delete': {
    apis: ['DELETE /api/workarea/{id}'],
    menus: ['作业区管理']
  },

  // ===== 公司模块 =====
  'company:list': {
    apis: ['GET /api/company'],
    menus: ['公司管理']
  },
  'company:view': {
    apis: ['GET /api/company/{id}'],
    menus: ['公司管理']
  },
  'company:create': {
    apis: ['POST /api/company'],
    menus: ['公司管理']
  },
  'company:edit': {
    apis: ['PUT /api/company/{id}'],
    menus: ['公司管理']
  },
  'company:delete': {
    apis: ['DELETE /api/company/{id}'],
    menus: ['公司管理']
  },
  'company:type:list': {
    apis: ['GET /api/company/type'],
    menus: ['公司管理']
  },

  // ===== 菜单模块 =====
  'menu:my:view': {
    apis: ['GET /api/menu/my'],
    menus: []
  },
  'menu:all:view': {
    apis: ['GET /api/menu/all'],
    menus: ['系统设置']
  },
  'menu:user:view': {
    apis: ['GET /api/menu/user-permissions/{userId}'],
    menus: ['用户权限配置']
  },
  'menu:user:edit': {
    apis: ['PUT /api/menu/user-permission'],
    menus: ['角色权限配置']
  },
  'menu:user:delete': {
    apis: ['DELETE /api/menu/user-permission'],
    menus: ['用户权限配置']
  },
  'menu:user:batch': {
    apis: ['PUT /api/menu/user-permissions/batch'],
    menus: ['用户权限配置']
  },

  // ===== 系统配置模块 =====
  'system:config:view': {
    apis: ['GET /api/system/config'],
    menus: ['系统管理', '系统设置']
  },
  'system:config:edit': {
    apis: ['PUT /api/system/config'],
    menus: ['系统管理']
  },

  // ===== 权限管理模块 =====
  'permission:role:manage': {
    apis: ['GET /api/permission/role/{roleId}', 'PUT /api/permission/role/{roleId}'],
    menus: ['角色权限配置']
  },
  'permission:user:manage': {
    apis: ['GET /api/permission/user/{userId}', 'PUT /api/permission/user/{userId}'],
    menus: ['用户权限配置']
  },
  'permission:edit': {
    apis: ['PUT /api/permission/{id}'],
    menus: ['角色权限配置']
  },

  // ===== 审计日志模块 =====
  'audit_log': {
    apis: ['GET /api/permission/audit-logs'],
    menus: ['审计日志']
  }
}

/**
 * 根据权限代码获取关联信息
 */
export function getPermissionInfo(permissionCode) {
  return permissionMapping[permissionCode] || { apis: [], menus: [] }
}

/**
 * 获取所有模块分组
 */
export const permissionModules = {
  'auth': { name: '认证模块', icon: 'Lock' },
  'user': { name: '用户模块', icon: 'User' },
  'role': { name: '角色模块', icon: 'UserFilled' },
  'workarea': { name: '作业区模块', icon: 'Location' },
  'company': { name: '公司模块', icon: 'Office' },
  'menu': { name: '菜单模块', icon: 'Menu' },
  'system': { name: '系统配置', icon: 'Setting' },
  'permission': { name: '权限管理', icon: 'Key' },
  'audit_log': { name: '审计日志', icon: 'Document' }
}