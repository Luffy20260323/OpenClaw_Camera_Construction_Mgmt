import request from '@/utils/request'

/**
 * 零部件属性集 API 模块
 * 对应后端接口：/api/component-attr-sets
 */

/**
 * 获取属性集列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 * @param {number} params.componentTypeId - 零部件种类 ID（可选）
 * @returns {Promise}
 */
export function getList(params) {
  return request({
    url: '/component-attr-sets',
    method: 'get',
    params
  })
}

/**
 * 获取属性集详情（包含属性定义列表）
 * @param {number} id - 属性集 ID
 * @returns {Promise}
 */
export function getDetail(id) {
  return request({
    url: `/component-attr-sets/${id}`,
    method: 'get'
  })
}

/**
 * 创建属性集
 * @param {Object} data - 属性集数据
 * @param {number} data.componentTypeId - 零部件种类 ID
 * @param {string} data.name - 属性集名称
 * @param {string} data.code - 属性集编码
 * @param {string} data.description - 描述
 * @param {number} data.sequenceNo - 显示序号
 * @param {Array} data.attrs - 属性定义列表
 * @returns {Promise}
 */
export function create(data) {
  return request({
    url: '/component-attr-sets',
    method: 'post',
    data
  })
}

/**
 * 更新属性集
 * @param {number} id - 属性集 ID
 * @param {Object} data - 属性集数据
 * @returns {Promise}
 */
export function update(id, data) {
  return request({
    url: `/component-attr-sets/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除属性集
 * @param {number} id - 属性集 ID
 * @returns {Promise}
 */
export function remove(id) {
  return request({
    url: `/component-attr-sets/${id}`,
    method: 'delete'
  })
}

/**
 * 获取所有零部件种类（用于下拉选择）
 * @returns {Promise}
 */
export function getComponentTypes() {
  return request({
    url: '/component-types',
    method: 'get',
    params: { size: 100 }
  })
}

/**
 * 更新属性集的显示序号
 * @param {number} id - 属性集 ID
 * @param {number} sequenceNo - 新的显示序号
 * @returns {Promise}
 */
export function updateSequenceNo(id, sequenceNo) {
  return request({
    url: `/component-attr-sets/${id}/sequence-no`,
    method: 'patch',
    data: { sequenceNo }
  })
}
