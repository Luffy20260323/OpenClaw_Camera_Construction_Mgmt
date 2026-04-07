import request from '@/utils/request'

/**
 * 获取当前用户的数据权限信息
 */
export function getCurrentDataPermission() {
  return request({
    url: '/data-permission/current',
    method: 'get'
  })
}

/**
 * 获取指定用户的数据权限信息
 */
export function getUserDataPermission(userId) {
  return request({
    url: `/data-permission/user/${userId}`,
    method: 'get'
  })
}

/**
 * 设置用户数据权限
 */
export function setUserDataPermission(userId, data) {
  return request({
    url: `/data-permission/user/${userId}`,
    method: 'post',
    params: data
  })
}

/**
 * 获取角色数据权限信息
 */
export function getRoleDataPermission(roleId) {
  return request({
    url: `/data-permission/role/${roleId}`,
    method: 'get'
  })
}

/**
 * 设置角色数据权限
 */
export function setRoleDataPermission(roleId, data) {
  return request({
    url: `/data-permission/role/${roleId}`,
    method: 'post',
    params: data
  })
}

/**
 * 获取数据范围类型选项
 */
export function getDataScopeOptions() {
  return [
    { value: 'SELF', label: '仅本人数据', description: '只能查看/操作自己创建的数据' },
    { value: 'DEPT', label: '本部门数据', description: '可查看/操作本部门的数据' },
    { value: 'DEPT_AND_SUB', label: '本部门及下级', description: '可查看/操作本部门及下级部门的数据' },
    { value: 'ALL', label: '全部数据', description: '可查看/操作所有数据' }
  ]
}

/**
 * 构建请求参数
 */
export function buildRequestParams(scopeType, deptId, deptIds) {
  const params = { scopeType }
  
  if (scopeType === 'DEPT' && deptId) {
    params.deptId = deptId
  }
  
  if (scopeType === 'DEPT_AND_SUB' && deptIds) {
    params.deptIds = deptIds
  }
  
  return params
}
