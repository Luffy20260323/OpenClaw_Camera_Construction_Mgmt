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
