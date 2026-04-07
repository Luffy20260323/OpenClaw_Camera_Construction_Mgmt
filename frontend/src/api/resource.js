import request from '@/utils/request'

/**
 * 资源管理 API 封装
 */

/**
 * 获取所有资源列表（树形结构）
 */
export function getResourceTree(params) {
  return request({
    url: '/resources/tree',
    method: 'get',
    params
  })
}

/**
 * 获取资源列表（分页）
 */
export function getResourceList(params) {
  return request({
    url: '/resources',
    method: 'get',
    params
  })
}

/**
 * 获取资源详情
 */
export function getResourceDetail(id) {
  return request({
    url: `/resources/${id}`,
    method: 'get'
  })
}

/**
 * 创建资源
 */
export function createResource(data) {
  return request({
    url: '/resources',
    method: 'post',
    data
  })
}

/**
 * 更新资源
 */
export function updateResource(id, data) {
  return request({
    url: `/resources/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除资源
 */
export function deleteResource(id) {
  return request({
    url: `/resources/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除资源
 */
export function batchDeleteResources(ids) {
  return request({
    url: '/resources/batch-delete',
    method: 'post',
    data: { ids }
  })
}

/**
 * 获取孤儿资源（没有父资源的资源）
 */
export function getOrphanResources() {
  return request({
    url: '/resources/orphans',
    method: 'get'
  })
}

/**
 * 获取资源类型统计
 */
export function getResourceStats() {
  return request({
    url: '/resources/stats',
    method: 'get'
  })
}

/**
 * 验证资源编码是否唯一
 */
export function validateResourceCode(code, excludeId) {
  return request({
    url: '/resources/validate-code',
    method: 'get',
    params: { code, excludeId }
  })
}

/**
 * 获取资源的权限列表
 */
export function getResourcePermissions(resourceId) {
  return request({
    url: `/resources/${resourceId}/permissions`,
    method: 'get'
  })
}

/**
 * 刷新资源菜单缓存
 */
export function refreshResourceCache() {
  return request({
    url: '/resources/refresh-cache',
    method: 'post'
  })
}

/**
 * 资源完整性校验
 */
export function validateResourceIntegrity() {
  return request({
    url: '/resources/validate-integrity',
    method: 'get'
  })
}
