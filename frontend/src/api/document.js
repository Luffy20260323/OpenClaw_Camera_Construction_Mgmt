import request from '@/utils/request'

/**
 * 获取文档列表
 * @param {string} category - 文档分类
 */
export function getDocumentList(category) {
  return request({
    url: '/system/docs',
    method: 'get',
    params: { category }
  })
}

/**
 * 搜索文档
 * @param {string} keyword - 搜索关键词
 */
export function searchDocuments(keyword) {
  return request({
    url: '/system/docs/search',
    method: 'get',
    params: { keyword }
  })
}

/**
 * 获取文档详情
 * @param {number} docId - 文档 ID
 */
export function getDocumentDetail(docId) {
  return request({
    url: `/system/docs/${docId}`,
    method: 'get'
  })
}

/**
 * 获取文档版本历史
 * @param {number} docId - 文档 ID
 */
export function getDocumentVersions(docId) {
  return request({
    url: `/system/docs/${docId}/versions`,
    method: 'get'
  })
}

/**
 * 上传文档
 * @param {FormData} formData - 包含文件和其他信息的 FormData
 */
export function uploadDocument(formData) {
  return request({
    url: '/system/docs',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 更新文档信息
 * @param {number} docId - 文档 ID
 * @param {Object} data - 更新数据
 */
export function updateDocument(docId, data) {
  return request({
    url: `/system/docs/${docId}`,
    method: 'put',
    data
  })
}

/**
 * 删除文档
 * @param {number} docId - 文档 ID
 */
export function deleteDocument(docId) {
  return request({
    url: `/system/docs/${docId}`,
    method: 'delete'
  })
}

/**
 * 获取 Markdown 文档内容（渲染为 HTML）
 * @param {string} filename - 文件名
 */
export function getMarkdownContent(filename) {
  return request({
    url: `/system/docs/markdown/${encodeURIComponent(filename)}`,
    method: 'get'
  })
}

/**
 * 查看文档（HTML）
 * @param {string} filename - 文件名
 */
export function viewDocument(filename) {
  return request({
    url: `/system/docs/view/${encodeURIComponent(filename)}`,
    method: 'get',
    responseType: 'blob'
  })
}

/**
 * 下载文档
 * @param {string} filename - 文件名
 */
export function downloadDocument(filename) {
  return request({
    url: `/system/docs/download/${encodeURIComponent(filename)}`,
    method: 'get',
    responseType: 'blob'
  })
}
