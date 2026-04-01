import request from '@/utils/request'

/**
 * 获取零部件实例列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 * @param {number} params.component_type_id - 零部件种类 ID（可选）
 * @param {number} params.attr_set_instance_id - 属性集实例 ID（可选）
 * @param {string} params.serial_number - 序列号（可选）
 * @param {string} params.status - 状态（可选）：normal/replaced/scrapped
 */
export function getList(params) {
  return request({
    url: '/component-instances',
    method: 'get',
    params
  })
}

/**
 * 获取零部件实例详情
 * @param {number} id - 实例 ID
 */
export function getDetail(id) {
  return request({
    url: `/component-instances/${id}`,
    method: 'get'
  })
}

/**
 * 创建零部件实例
 * @param {Object} data - 实例数据
 * @param {number} data.component_type_id - 零部件种类 ID
 * @param {number} [data.attr_set_instance_id] - 属性集实例 ID（可选）
 * @param {string} data.serial_number - 序列号
 * @param {string} data.status - 状态：normal（正常）/replaced（已更换）/scrapped（已报废）
 */
export function create(data) {
  return request({
    url: '/component-instances',
    method: 'post',
    data
  })
}

/**
 * 更新零部件实例
 * @param {number} id - 实例 ID
 * @param {Object} data - 更新数据
 */
export function update(id, data) {
  return request({
    url: `/component-instances/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除零部件实例
 * @param {number} id - 实例 ID
 */
export function remove(id) {
  return request({
    url: `/component-instances/${id}`,
    method: 'delete'
  })
}

/**
 * 更新零部件实例状态
 * @param {number} id - 实例 ID
 * @param {string} status - 新状态：normal/replaced/scrapped
 */
export function updateStatus(id, status) {
  return request({
    url: `/component-instances/${id}/status`,
    method: 'patch',
    data: { status }
  })
}
