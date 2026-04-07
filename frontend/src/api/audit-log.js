import request from '@/utils/request'

/**
 * 审计日志 API 封装
 */

/**
 * 获取审计日志列表（分页）
 */
export function getAuditLogList(params) {
  return request({
    url: '/audit-logs',
    method: 'get',
    params
  })
}

/**
 * 获取审计日志详情
 */
export function getAuditLogDetail(id) {
  return request({
    url: `/audit-logs/${id}`,
    method: 'get'
  })
}

/**
 * 获取权限审计日志列表
 */
export function getPermissionAuditLogs(params) {
  return request({
    url: '/audit-logs/permission',
    method: 'get',
    params
  })
}

/**
 * 获取资源操作日志
 */
export function getResourceAuditLogs(resourceId, params) {
  return request({
    url: `/audit-logs/resource/${resourceId}`,
    method: 'get',
    params
  })
}

/**
 * 获取角色操作日志
 */
export function getRoleAuditLogs(roleId, params) {
  return request({
    url: `/audit-logs/role/${roleId}`,
    method: 'get',
    params
  })
}

/**
 * 获取用户操作日志
 */
export function getUserAuditLogs(userId, params) {
  return request({
    url: `/audit-logs/user/${userId}`,
    method: 'get',
    params
  })
}

/**
 * 导出审计日志 Excel
 */
export function exportAuditLogs(params) {
  return request({
    url: '/audit-logs/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

/**
 * 导出权限审计日志 Excel
 */
export function exportPermissionAuditLogs(params) {
  return request({
    url: '/audit-logs/permission/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

/**
 * 获取操作类型选项
 */
export function getOperationTypes() {
  return request({
    url: '/audit-logs/operation-types',
    method: 'get'
  })
}

/**
 * 获取目标类型选项
 */
export function getTargetTypes() {
  return request({
    url: '/audit-logs/target-types',
    method: 'get'
  })
}
