import { defineStore } from 'pinia'
import {
  getResourceTree,
  getResourceList,
  getResourceDetail,
  createResource,
  updateResource,
  deleteResource,
  batchDeleteResources,
  getOrphanResources,
  getResourceStats,
  validateResourceCode,
  refreshResourceCache,
  validateResourceIntegrity
} from '@/api/resource'
import { ElMessage } from 'element-plus'

export const useResourceStore = defineStore('resource', {
  state: () => ({
    // 资源树
    resourceTree: [],
    // 资源列表（分页）
    resourceList: [],
    // 孤儿资源
    orphanResources: [],
    // 资源统计
    resourceStats: {},
    // 加载状态
    loading: false,
    // 选中资源
    selectedResource: null,
    // 搜索条件
    searchParams: {
      keyword: '',
      type: '',
      parentId: null
    }
  }),

  getters: {
    // 获取资源总数
    totalCount: (state) => {
      return state.resourceStats.total || 0
    },
    // 按类型筛选资源
    resourcesByType: (state) => (type) => {
      if (!type) return state.resourceTree
      const filterTree = (nodes) => {
        return nodes.reduce((acc, node) => {
          if (node.type === type) {
            acc.push({ ...node, children: filterTree(node.children || []) })
          } else if (node.children && node.children.length > 0) {
            const filteredChildren = filterTree(node.children)
            if (filteredChildren.length > 0) {
              acc.push({ ...node, children: filteredChildren })
            }
          }
          return acc
        }, [])
      }
      return filterTree(state.resourceTree)
    },
    // 获取资源类型选项
    resourceTypeOptions: () => [
      { value: 'MODULE', label: '模块' },
      { value: 'MENU', label: '菜单' },
      { value: 'PAGE', label: '页面' },
      { value: 'ELEMENT', label: '元素' },
      { value: 'API', label: 'API' },
      { value: 'PERMISSION', label: '权限' }
    ]
  },

  actions: {
    // 加载资源树
    async loadResourceTree(params) {
      this.loading = true
      try {
        const res = await getResourceTree(params)
        this.resourceTree = res.data || []
        return this.resourceTree
      } catch (error) {
        console.error('加载资源树失败:', error)
        ElMessage.error('加载资源树失败')
        throw error
      } finally {
        this.loading = false
      }
    },

    // 加载资源列表（分页）
    async loadResourceList(params) {
      this.loading = true
      try {
        const res = await getResourceList(params)
        this.resourceList = res.data || { records: [], total: 0 }
        return this.resourceList
      } catch (error) {
        console.error('加载资源列表失败:', error)
        ElMessage.error('加载资源列表失败')
        throw error
      } finally {
        this.loading = false
      }
    },

    // 加载孤儿资源
    async loadOrphanResources() {
      this.loading = true
      try {
        const res = await getOrphanResources()
        this.orphanResources = res.data || []
        return this.orphanResources
      } catch (error) {
        console.error('加载孤儿资源失败:', error)
        ElMessage.error('加载孤儿资源失败')
        throw error
      } finally {
        this.loading = false
      }
    },

    // 加载资源统计
    async loadResourceStats() {
      try {
        const res = await getResourceStats()
        this.resourceStats = res.data || {}
        return this.resourceStats
      } catch (error) {
        console.error('加载资源统计失败:', error)
        throw error
      }
    },

    // 获取资源详情
    async loadResourceDetail(id) {
      try {
        const res = await getResourceDetail(id)
        this.selectedResource = res.data
        return this.selectedResource
      } catch (error) {
        console.error('加载资源详情失败:', error)
        throw error
      }
    },

    // 创建资源
    async createResource(data) {
      try {
        const res = await createResource(data)
        ElMessage.success('创建资源成功')
        // 刷新资源树
        await this.loadResourceTree()
        return res.data
      } catch (error) {
        console.error('创建资源失败:', error)
        ElMessage.error('创建资源失败：' + error.message)
        throw error
      }
    },

    // 更新资源
    async updateResource(id, data) {
      try {
        const res = await updateResource(id, data)
        ElMessage.success('更新资源成功')
        // 刷新资源树
        await this.loadResourceTree()
        return res.data
      } catch (error) {
        console.error('更新资源失败:', error)
        ElMessage.error('更新资源失败：' + error.message)
        throw error
      }
    },

    // 删除资源
    async deleteResource(id) {
      try {
        await deleteResource(id)
        ElMessage.success('删除资源成功')
        // 刷新资源树
        await this.loadResourceTree()
      } catch (error) {
        console.error('删除资源失败:', error)
        ElMessage.error('删除资源失败：' + error.message)
        throw error
      }
    },

    // 批量删除资源
    async batchDeleteResources(ids) {
      try {
        await batchDeleteResources(ids)
        ElMessage.success('批量删除成功')
        // 刷新资源树
        await this.loadResourceTree()
      } catch (error) {
        console.error('批量删除失败:', error)
        ElMessage.error('批量删除失败：' + error.message)
        throw error
      }
    },

    // 验证资源编码
    async validateResourceCode(code, excludeId) {
      try {
        const res = await validateResourceCode(code, excludeId)
        return res.data
      } catch (error) {
        console.error('验证资源编码失败:', error)
        return { valid: false, message: error.message }
      }
    },

    // 刷新资源缓存
    async refreshResourceCache() {
      try {
        await refreshResourceCache()
        ElMessage.success('刷新资源缓存成功')
        // 刷新资源树
        await this.loadResourceTree()
      } catch (error) {
        console.error('刷新资源缓存失败:', error)
        ElMessage.error('刷新资源缓存失败：' + error.message)
        throw error
      }
    },

    // 资源完整性校验
    async validateResourceIntegrity() {
      try {
        const res = await validateResourceIntegrity()
        return res.data
      } catch (error) {
        console.error('资源完整性校验失败:', error)
        throw error
      }
    },

    // 设置搜索条件
    setSearchParams(params) {
      this.searchParams = { ...this.searchParams, ...params }
    },

    // 清空搜索条件
    clearSearchParams() {
      this.searchParams = {
        keyword: '',
        type: '',
        parentId: null
      }
    }
  }
})
