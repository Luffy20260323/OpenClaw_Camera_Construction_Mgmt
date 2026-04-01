import request from '@/utils/request'

/**
 * 获取零部件种类列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 * @param {string} params.keyword - 搜索关键词
 */
export function getList(params) {
  return request({
    url: '/component-types',
    method: 'get',
    params
  })
}

/**
 * 获取零部件种类详情
 * @param {number} id - 种类 ID
 */
export function getDetail(id) {
  return request({
    url: `/component-types/${id}`,
    method: 'get'
  })
}

/**
 * 创建零部件种类
 * @param {Object} data - 种类数据
 * @param {string} data.name - 种类名称
 * @param {string} data.code - 种类编码
 * @param {string} data.description - 描述
 * @param {number} data.sequence_no - 显示序号
 */
export function create(data) {
  return request({
    url: '/component-types',
    method: 'post',
    data
  })
}

/**
 * 更新零部件种类
 * @param {number} id - 种类 ID
 * @param {Object} data - 更新数据
 */
export function update(id, data) {
  return request({
    url: `/component-types/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除零部件种类
 * @param {number} id - 种类 ID
 */
export function remove(id) {
  return request({
    url: `/component-types/${id}`,
    method: 'delete'
  })
}

/**
 * 切换启用/禁用状态
 * @param {number} id - 种类 ID
 * @param {boolean} isActive - 是否启用
 */
export function toggleStatus(id, isActive) {
  return request({
    url: `/component-types/${id}/status`,
    method: 'patch',
    data: { is_active: isActive }
  })
}
