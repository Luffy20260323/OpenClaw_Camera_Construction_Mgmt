import request from '@/utils/request'

/**
 * 获取点位设备模型实例列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 * @param {string} params.keyword - 搜索关键词
 * @param {number} params.modelId - 点位设备模型 ID
 * @param {number} params.workAreaId - 作业区 ID
 */
export function getList(params) {
  return request({
    url: '/point-device-model-instances',
    method: 'get',
    params
  })
}

/**
 * 获取点位设备模型实例详情
 * @param {number} id - 实例 ID
 */
export function getDetail(id) {
  return request({
    url: `/point-device-model-instances/${id}`,
    method: 'get'
  })
}

/**
 * 创建点位设备模型实例
 * @param {Object} data - 实例数据
 * @param {number} data.modelId - 点位设备模型 ID
 * @param {number} data.workAreaId - 作业区 ID
 * @param {string} data.name - 实例名称
 * @param {string} data.location - 安装位置
 * @param {Array} data.items - 实例项列表
 */
export function create(data) {
  return request({
    url: '/point-device-model-instances',
    method: 'post',
    data
  })
}

/**
 * 更新点位设备模型实例
 * @param {number} id - 实例 ID
 * @param {Object} data - 更新数据
 */
export function update(id, data) {
  return request({
    url: `/point-device-model-instances/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除点位设备模型实例
 * @param {number} id - 实例 ID
 */
export function remove(id) {
  return request({
    url: `/point-device-model-instances/${id}`,
    method: 'delete'
  })
}

/**
 * 更新点位设备模型实例项
 * @param {number} instanceId - 实例 ID
 * @param {number} itemId - 实例项 ID
 * @param {Object} data - 更新数据
 * @param {number} data.componentInstanceId - 零部件实例 ID
 */
export function updateItem(instanceId, itemId, data) {
  return request({
    url: `/point-device-model-instances/${instanceId}/items/${itemId}`,
    method: 'put',
    data
  })
}

/**
 * 获取点位设备模型列表（用于选择器）
 */
export function getModelOptions() {
  return request({
    url: '/point-device-models',
    method: 'get',
    params: { size: 1000 }
  })
}

/**
 * 获取作业区列表（用于选择器）
 */
export function getWorkAreaOptions() {
  return request({
    url: '/workareas',
    method: 'get',
    params: { size: 1000 }
  })
}
