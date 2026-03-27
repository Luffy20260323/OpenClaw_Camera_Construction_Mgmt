import request from '@/utils/request'

/**
 * 获取当前用户的菜单列表
 */
export function getMyMenus() {
  return request({
    url: '/menu/my-menus',
    method: 'get'
  })
}

/**
 * 获取所有菜单（系统管理员）
 */
export function getAllMenus() {
  return request({
    url: '/menu/all',
    method: 'get'
  })
}

/**
 * 获取指定用户的菜单权限详情
 */
export function getUserPermissions(userId) {
  return request({
    url: `/menu/user-permissions/${userId}`,
    method: 'get'
  })
}

/**
 * 更新用户菜单权限
 */
export function updateUserPermission(data) {
  return request({
    url: '/menu/user-permission',
    method: 'put',
    data
  })
}

/**
 * 删除用户自定义菜单权限
 */
export function deleteUserPermission(userId, menuId) {
  return request({
    url: '/menu/user-permission',
    method: 'delete',
    params: { userId, menuId }
  })
}

/**
 * 批量更新用户菜单权限
 */
export function batchUpdateUserPermissions(userId, permissions) {
  return request({
    url: `/menu/user-permissions/batch?userId=${userId}`,
    method: 'put',
    data: permissions
  })
}
