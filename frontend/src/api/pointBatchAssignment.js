import request from '@/utils/request'

/**
 * 获取未分配的点位列表
 */
export function getAvailablePoints() {
  return request({
    url: '/point-batch-assignment/available-points',
    method: 'get'
  })
}

/**
 * 批量分配设备模型实例给点位
 * @param {Object} data - 分配数据
 * @param {number} data.modelInstanceId - 设备模型实例 ID
 * @param {Array<number>} data.pointIds - 待分配的点位 ID 列表
 */
export function batchAssign(data) {
  return request({
    url: '/point-batch-assignment',
    method: 'post',
    data
  })
}

/**
 * 获取点位设备配置
 * @param {number} pointId - 点位 ID
 */
export function getPointDeviceConfig(pointId) {
  return request({
    url: `/point-batch-assignment/point/${pointId}`,
    method: 'get'
  })
}

/**
 * 获取点位设备模型列表（用于选择器）
 */
export function getPointDeviceModels() {
  return request({
    url: '/point-device-models',
    method: 'get',
    params: { isActive: true }
  })
}
