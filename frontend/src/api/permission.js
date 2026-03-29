import request from '@/utils/request'

/**
 * 获取所有权限列表
 */
export function getAllPermissions() {
  return request({
    url: '/permission/list',
    method: 'get'
  })
}

/**
 * 获取所有角色列表
 */
export function getAllRoles() {
  return request({
    url: '/permission/roles',
    method: 'get'
  })
}

/**
 * 获取角色的权限配置
 */
export function getRolePermissions(roleId) {
  return request({
    url: `/permission/role/${roleId}`,
    method: 'get'
  })
}

/**
 * 配置角色权限
 */
export function updateRolePermissions(roleId, data) {
  return request({
    url: `/permission/role/${roleId}`,
    method: 'put',
    data: data  // 传递完整对象，包含 permissionIds 和 comment
  })
}

/**
 * 获取用户的权限列表
 */
export function getUserPermissions(userId) {
  return request({
    url: `/permission/user/${userId}`,
    method: 'get'
  })
}

/**
 * 获取权限矩阵
 */
export function getPermissionMatrix() {
  return request({
    url: '/permission/matrix',
    method: 'get'
  })
}

/**
 * 查询审计日志
 */
export function getAuditLogs(params) {
  return request({
    url: '/permission/audit-logs',
    method: 'get',
    params
  })
}

/**
 * 更新权限描述
 */
export function updatePermissionDescription(id, description) {
  return request({
    url: `/permission/${id}`,
    method: 'put',
    data: { description }
  })
}
