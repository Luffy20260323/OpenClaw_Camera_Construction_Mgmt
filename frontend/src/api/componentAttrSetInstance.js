import request from '@/utils/request'

/**
 * 获取属性集实例列表
 * @param {object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 * @param {number} params.attrSetId - 属性集 ID（可选）
 * @param {string} params.name - 实例名称（可选）
 * @returns {Promise}
 */
export function getList(params) {
  return request({
    url: '/api/component-attr-set-instances',
    method: 'get',
    params
  })
}

/**
 * 获取属性集实例详情
 * @param {number} id - 实例 ID
 * @returns {Promise}
 */
export function getDetail(id) {
  return request({
    url: `/api/component-attr-set-instances/${id}`,
    method: 'get'
  })
}

/**
 * 创建属性集实例
 * @param {object} data - 实例数据
 * @param {number} data.attrSetId - 属性集 ID
 * @param {string} data.name - 实例名称
 * @param {object} data.attrValues - 属性值 JSON
 * @returns {Promise}
 */
export function create(data) {
  return request({
    url: '/api/component-attr-set-instances',
    method: 'post',
    data
  })
}

/**
 * 更新属性集实例
 * @param {number} id - 实例 ID
 * @param {object} data - 实例数据
 * @param {number} data.attrSetId - 属性集 ID
 * @param {string} data.name - 实例名称
 * @param {object} data.attrValues - 属性值 JSON
 * @returns {Promise}
 */
export function update(id, data) {
  return request({
    url: `/api/component-attr-set-instances/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除属性集实例
 * @param {number} id - 实例 ID
 * @returns {Promise}
 */
export function remove(id) {
  return request({
    url: `/api/component-attr-set-instances/${id}`,
    method: 'delete'
  })
}

/**
 * 获取属性集详情（含属性定义）
 * @param {number} id - 属性集 ID
 * @returns {Promise}
 */
export function getAttrSetDetail(id) {
  return request({
    url: `/api/component-attr-sets/${id}`,
    method: 'get'
  })
}

/**
 * 获取属性集列表
 * @param {object} params - 查询参数
 * @param {number} params.componentTypeId - 零部件种类 ID（可选）
 * @returns {Promise}
 */
export function getAttrSetList(params) {
  return request({
    url: '/api/component-attr-sets',
    method: 'get',
    params
  })
}
