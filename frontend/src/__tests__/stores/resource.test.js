import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useResourceStore } from '@/stores/resource'

// Mock API
vi.mock('@/api/resource', () => ({
  getResourceTree: vi.fn(() => Promise.resolve({ data: [] })),
  getResourceList: vi.fn(() => Promise.resolve({ data: { records: [], total: 0 } })),
  getOrphanResources: vi.fn(() => Promise.resolve({ data: [] })),
  getResourceStats: vi.fn(() => Promise.resolve({ data: {} })),
  getResourceDetail: vi.fn(() => Promise.resolve({ data: {} })),
  createResource: vi.fn(() => Promise.resolve({ data: {} })),
  updateResource: vi.fn(() => Promise.resolve({ data: {} })),
  deleteResource: vi.fn(() => Promise.resolve({})),
  batchDeleteResources: vi.fn(() => Promise.resolve({})),
  validateResourceCode: vi.fn(() => Promise.resolve({ valid: true })),
  refreshResourceCache: vi.fn(() => Promise.resolve({})),
  validateResourceIntegrity: vi.fn(() => Promise.resolve({ hasIssues: false }))
}))

describe('useResourceStore', () => {
  let store
  let pinia

  beforeEach(() => {
    pinia = createPinia()
    setActivePinia(pinia)
    store = useResourceStore()
  })

  it('初始化状态', () => {
    expect(store.resourceTree).toEqual([])
    expect(store.resourceList).toEqual([])
    expect(store.orphanResources).toEqual([])
    expect(store.loading).toBe(false)
  })

  it('加载资源树', async () => {
    const mockData = [
      { id: 1, name: '系统管理', type: 'MODULE' }
    ]
    
    const { getResourceTree } = await import('@/api/resource')
    getResourceTree.mockResolvedValueOnce({ data: mockData })

    await store.loadResourceTree()
    
    expect(store.resourceTree).toEqual(mockData)
    expect(getResourceTree).toHaveBeenCalled()
  })

  it('加载资源统计', async () => {
    const mockStats = { total: 100, module: 10, menu: 20 }
    
    const { getResourceStats } = await import('@/api/resource')
    getResourceStats.mockResolvedValueOnce({ data: mockStats })

    await store.loadResourceStats()
    
    expect(store.resourceStats).toEqual(mockStats)
  })

  it('设置搜索参数', () => {
    store.setSearchParams({ keyword: 'test', type: 'MODULE' })
    
    expect(store.searchParams.keyword).toBe('test')
    expect(store.searchParams.type).toBe('MODULE')
  })

  it('清空搜索参数', () => {
    store.setSearchParams({ keyword: 'test' })
    store.clearSearchParams()
    
    expect(store.searchParams.keyword).toBe('')
    expect(store.searchParams.type).toBe('')
    expect(store.searchParams.parentId).toBe(null)
  })

  it('获取资源类型选项', () => {
    const options = store.resourceTypeOptions
    
    expect(options).toHaveLength(6)
    expect(options.map(o => o.value)).toContain('MODULE')
    expect(options.map(o => o.value)).toContain('MENU')
    expect(options.map(o => o.value)).toContain('PAGE')
  })
})
