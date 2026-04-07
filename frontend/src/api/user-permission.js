import request from '@/utils/request'

/**
 * 用户权限管理 API 封装
 */

/**
 * 获取用户列表（分页）
 */
export function getUserList(params) {
  return request({
    url: '/users',
    method: 'get',
    params
  })
}

/**
 * 获取用户详情
 */
export function getUserDetail(id) {
  return request({
    url: `/users/${id}`,
    method: 'get'
  })
}

/**
 * 获取用户的权限列表（树形结构）
 */
export function getUserPermissionTree(userId) {
  return request({
    url: `/users/${userId}/permissions/tree`,
    method: 'get'
  })
}

/**
 * 获取用户的权限列表（扁平）
 */
export function getUserPermissions(userId) {
  return request({
    url: `/users/${userId}/permissions`,
    method: 'get'
  })
}

/**
 * 配置用户权限
 */
export function configureUserPermissions(userId, data) {
  return request({
    url: `/users/${userId}/permissions`,
    method: 'put',
    data
  })
}

/**
 * 获取用户数据权限配置
 */
export function getUserDataScope(userId) {
  return request({
    url: `/users/${userId}/data-scope`,
    method: 'get'
  })
}

/**
 * 配置用户数据权限
 */
export function configureUserDataScope(userId, data) {
  return request({
    url: `/users/${userId}/data-scope`,
    method: 'put',
    data
  })
}

/**
 * 获取用户的角色列表
 */
export function getUserRoles(userId) {
  return request({
    url: `/users/${userId}/roles`,
    method: 'get'
  })
}

/**
 * 为用户分配角色
 */
export function assignUserRole(userId, roleIds) {
  return request({
    url: `/users/${userId}/roles`,
    method: 'post',
    data: { roleIds }
  })
}

/**
 * 移除用户角色
 */
export function removeUserRole(userId, roleId) {
  return request({
    url: `/users/${userId}/roles/${roleId}`,
    method: 'delete'
  })
}

/**
 * 获取用户权限详情（包含继承自角色的权限）
 */
export function getUserPermissionDetail(userId) {
  return request({
    url: `/users/${userId}/permission-detail`,
    method: 'get'
  })
}

/**
 * 批量配置用户权限
 */
export function batchConfigureUserPermissions(data) {
  return request({
    url: '/users/permissions/batch',
    method: 'post',
    data
  })
}
