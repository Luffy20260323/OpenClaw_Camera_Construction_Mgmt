import request from '@/utils/request'

/**
 * 获取点位设备模型列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 * @param {string} params.keyword - 搜索关键词
 * @param {number} params.componentTypeId - 零部件种类 ID
 */
export function getList(params) {
  return request({
    url: '/point-device-models',
    method: 'get',
    params
  })
}

/**
 * 获取点位设备模型详情
 * @param {number} id - 模型 ID
 */
export function getDetail(id) {
  return request({
    url: `/point-device-models/${id}`,
    method: 'get'
  })
}

/**
 * 创建点位设备模型
 * @param {Object} data - 模型数据
 * @param {string} data.name - 模型名称
 * @param {string} data.code - 模型编码
 * @param {number} data.componentTypeId - 零部件种类 ID
 * @param {string} data.description - 描述
 * @param {number} data.sequence_no - 显示序号
 * @param {Array} data.items - 模型项列表
 */
export function create(data) {
  return request({
    url: '/point-device-models',
    method: 'post',
    data
  })
}

/**
 * 更新点位设备模型
 * @param {number} id - 模型 ID
 * @param {Object} data - 更新数据
 */
export function update(id, data) {
  return request({
    url: `/point-device-models/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除点位设备模型
 * @param {number} id - 模型 ID
 */
export function remove(id) {
  return request({
    url: `/point-device-models/${id}`,
    method: 'delete'
  })
}

/**
 * 获取零部件种类列表（用于选择器）
 */
export function getComponentTypes() {
  return request({
    url: '/component-types',
    method: 'get',
    params: { size: 1000 }
  })
}
