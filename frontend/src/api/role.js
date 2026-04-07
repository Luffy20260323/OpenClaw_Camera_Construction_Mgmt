import request from '@/utils/request'

/**
 * 角色管理 API 封装
 */

/**
 * 获取角色列表（分页）
 */
export function getRoleList(params) {
  return request({
    url: '/roles',
    method: 'get',
    params
  })
}

/**
 * 获取所有角色（不分页，用于下拉选择）
 */
export function getAllRoles() {
  return request({
    url: '/roles/all',
    method: 'get'
  })
}

/**
 * 获取角色详情
 */
export function getRoleDetail(id) {
  return request({
    url: `/roles/${id}`,
    method: 'get'
  })
}

/**
 * 创建角色
 */
export function createRole(data) {
  return request({
    url: '/roles',
    method: 'post',
    data
  })
}

/**
 * 更新角色
 */
export function updateRole(id, data) {
  return request({
    url: `/roles/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除角色
 */
export function deleteRole(id) {
  return request({
    url: `/roles/${id}`,
    method: 'delete'
  })
}

/**
 * 获取角色的权限列表（树形结构）
 */
export function getRolePermissionTree(roleId) {
  return request({
    url: `/roles/${roleId}/permissions/tree`,
    method: 'get'
  })
}

/**
 * 获取角色的权限列表（扁平）
 */
export function getRolePermissions(roleId) {
  return request({
    url: `/roles/${roleId}/permissions`,
    method: 'get'
  })
}

/**
 * 配置角色权限
 */
export function configureRolePermissions(roleId, data) {
  return request({
    url: `/roles/${roleId}/permissions`,
    method: 'put',
    data
  })
}

/**
 * 获取角色数据权限配置
 */
export function getRoleDataScope(roleId) {
  return request({
    url: `/roles/${roleId}/data-scope`,
    method: 'get'
  })
}

/**
 * 配置角色数据权限
 */
export function configureRoleDataScope(roleId, data) {
  return request({
    url: `/roles/${roleId}/data-scope`,
    method: 'put',
    data
  })
}

/**
 * 获取角色关联的用户列表
 */
export function getRoleUsers(roleId, params) {
  return request({
    url: `/roles/${roleId}/users`,
    method: 'get',
    params
  })
}

/**
 * 获取角色的缺省权限配置
 */
export function getRoleDefaultPermissions(roleId) {
  return request({
    url: `/roles/${roleId}/default-permissions`,
    method: 'get'
  })
}

/**
 * 配置角色缺省权限
 */
export function configureRoleDefaultPermissions(roleId, data) {
  return request({
    url: `/roles/${roleId}/default-permissions`,
    method: 'put',
    data
  })
}

/**
 * 复制角色权限到另一个角色
 */
export function copyRolePermissions(sourceRoleId, targetRoleId) {
  return request({
    url: '/roles/copy-permissions',
    method: 'post',
    data: { sourceRoleId, targetRoleId }
  })
}

/**
 * 调整角色权限
 * @param {number} roleId - 角色 ID
 * @param {object} data - 调整数据 { resourceId: Long, action: "ADD" | "REMOVE" }
 * @returns {Promise}
 */
export function adjustRolePermission(roleId, data) {
  return request({
    url: `/roles/${roleId}/permissions/adjust`,
    method: 'post',
    data: data
  })
}

/**
 * 批量调整角色权限
 * @param {number} roleId - 角色 ID
 * @param {Array} adjustments - 调整数据数组 [{ resourceId: Long, action: "ADD" | "REMOVE" }]
 * @returns {Promise}
 */
export function adjustRolePermissionBatch(roleId, adjustments) {
  return request({
    url: `/roles/${roleId}/permissions/adjust-batch`,
    method: 'post',
    data: adjustments
  })
}
