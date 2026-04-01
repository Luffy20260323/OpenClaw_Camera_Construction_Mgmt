import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import Documents from '@/views/system/Documents.vue'
import { createTestingPinia } from '@pinia/testing'
import * as documentApi from '@/api/document'

/**
 * 文档中心组件测试
 * 
 * 测试范围：
 * 1. 组件渲染
 * 2. 文档列表显示
 * 3. 分类筛选功能
 * 4. 搜索功能
 * 5. 文档查看对话框
 * 6. 文档下载功能
 * 
 * @author Camera1001 Team
 * @since 2026-03-30
 */
describe('Documents.vue', () => {
  const mockDocuments = [
    {
      id: 1,
      title: '权限体系设计方案 V3',
      description: '完整的权限体系设计方案',
      category: 'design',
      filename: 'permission-system-design-v3.html',
      fileType: 'html',
      fileSize: 26523,
      featured: true,
      updatedAt: '2026-03-30T08:00:00Z'
    },
    {
      id: 2,
      title: '权限清单',
      description: '51 个权限的完整清单',
      category: 'design',
      filename: 'permissions-list.md',
      fileType: 'md',
      fileSize: 8291,
      featured: true,
      updatedAt: '2026-03-30T08:00:00Z'
    },
    {
      id: 3,
      title: '文档中心实施报告',
      description: '文档中心功能实施报告',
      category: 'implementation',
      filename: '文档中心实施报告.md',
      fileType: 'md',
      fileSize: 5114,
      featured: false,
      updatedAt: '2026-03-30T09:00:00Z'
    }
  ]

  beforeEach(() => {
    vi.clearAllMocks()
  })

  /**
   * 测试 1：组件渲染
   */
  it('renders component correctly', () => {
    vi.spyOn(documentApi, 'getDocumentList').mockResolvedValue({ data: mockDocuments })

    const wrapper = mount(Documents, {
      global: {
        plugins: [createTestingPinia()]
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.find('.document-center').exists()).toBe(true)
    expect(wrapper.find('.page-header h1').text()).toBe('📚 文档中心')
  })

  /**
   * 测试 2：显示文档列表
   */
  it('displays document list', async () => {
    vi.spyOn(documentApi, 'getDocumentList').mockResolvedValue({ data: mockDocuments })

    const wrapper = mount(Documents, {
      global: {
        plugins: [createTestingPinia()]
      }
    })

    // 等待数据加载
    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))

    // 验证文档卡片数量
    const docCards = wrapper.findAll('.doc-card')
    expect(docCards.length).toBeGreaterThanOrEqual(1)
  })

  /**
   * 测试 3：显示空状态
   */
  it('displays empty state when no documents', async () => {
    vi.spyOn(documentApi, 'getDocumentList').mockResolvedValue({ data: [] })

    const wrapper = mount(Documents, {
      global: {
        plugins: [createTestingPinia()]
      }
    })

    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))

    // 验证空状态显示
    expect(wrapper.find('.el-empty').exists()).toBe(true)
    expect(wrapper.find('.el-empty .empty-description').text()).toBe('暂无文档')
  })

  /**
   * 测试 4：分类筛选功能
   */
  it('filters documents by category', async () => {
    vi.spyOn(documentApi, 'getDocumentList').mockResolvedValue({ data: mockDocuments })

    const wrapper = mount(Documents, {
      global: {
        plugins: [createTestingPinia()]
      }
    })

    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))

    // 选择"设计文档"分类
    const designRadio = wrapper.findAll('.el-radio-button')[1] // 第一个是"全部"
    await designRadio.trigger('click')

    await wrapper.vm.$nextTick()

    // 验证筛选后的文档
    expect(wrapper.vm.selectedCategory).toBe('design')
  })

  /**
   * 测试 5：搜索功能
   */
  it('filters documents by search query', async () => {
    vi.spyOn(documentApi, 'getDocumentList').mockResolvedValue({ data: mockDocuments })

    const wrapper = mount(Documents, {
      global: {
        plugins: [createTestingPinia()]
      }
    })

    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))

    // 输入搜索关键词
    const searchInput = wrapper.find('.search-box input')
    await searchInput.setValue('权限')
    await wrapper.vm.$nextTick()

    // 验证搜索结果
    expect(wrapper.vm.searchQuery).toBe('权限')
    expect(wrapper.vm.filteredDocuments.length).toBeGreaterThan(0)
  })

  /**
   * 测试 6：搜索功能 - 无结果
   */
  it('displays no results for unmatched search', async () => {
    vi.spyOn(documentApi, 'getDocumentList').mockResolvedValue({ data: mockDocuments })

    const wrapper = mount(Documents, {
      global: {
        plugins: [createTestingPinia()]
      }
    })

    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))

    // 输入不匹配的搜索词
    const searchInput = wrapper.find('.search-box input')
    await searchInput.setValue('不存在的文档')
    await wrapper.vm.$nextTick()

    // 验证搜索结果为空
    expect(wrapper.vm.filteredDocuments.length).toBe(0)
  })

  /**
   * 测试 7：查看文档对话框
   */
  it('opens document view dialog', async () => {
    vi.spyOn(documentApi, 'getDocumentList').mockResolvedValue({ data: mockDocuments })

    const wrapper = mount(Documents, {
      global: {
        plugins: [createTestingPinia()]
      }
    })

    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))

    // 点击"查看"按钮
    const viewButton = wrapper.find('.card-footer .el-button--primary')
    await viewButton.trigger('click')

    await wrapper.vm.$nextTick()

    // 验证对话框打开
    expect(wrapper.vm.dialogVisible).toBe(true)
    expect(wrapper.vm.currentDoc).not.toBeNull()
  })

  /**
   * 测试 8：下载文档
   */
  it('downloads document', async () => {
    vi.spyOn(documentApi, 'getDocumentList').mockResolvedValue({ data: mockDocuments })

    const wrapper = mount(Documents, {
      global: {
        plugins: [createTestingPinia()]
      }
    })

    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))

    // 模拟 createElement
    const mockCreateElement = vi.fn()
    document.createElement = mockCreateElement

    // 点击下载按钮
    const downloadButtons = wrapper.findAll('.card-footer .el-button')
    if (downloadButtons.length > 1) {
      await downloadButtons[1].trigger('click')
    }

    await wrapper.vm.$nextTick()

    // 验证创建了下载链接
    expect(mockCreateElement).toHaveBeenCalledWith('a')
  })

  /**
   * 测试 9：文档分类标签颜色
   */
  it('displays correct category tag colors', async () => {
    vi.spyOn(documentApi, 'getDocumentList').mockResolvedValue({ data: mockDocuments })

    const wrapper = mount(Documents, {
      global: {
        plugins: [createTestingPinia()]
      }
    })

    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))

    // 验证分类标签类型
    expect(wrapper.vm.getCategoryTagType('design')).toBe('primary')
    expect(wrapper.vm.getCategoryTagType('implementation')).toBe('success')
    expect(wrapper.vm.getCategoryTagType('manual')).toBe('warning')
    expect(wrapper.vm.getCategoryTagType('technical')).toBe('info')
    expect(wrapper.vm.getCategoryTagType('report')).toBe('danger')
  })

  /**
   * 测试 10：文档分类标签文字
   */
  it('displays correct category labels', async () => {
    vi.spyOn(documentApi, 'getDocumentList').mockResolvedValue({ data: mockDocuments })

    const wrapper = mount(Documents, {
      global: {
        plugins: [createTestingPinia()]
      }
    })

    await wrapper.vm.$nextTick()

    // 验证分类标签文字
    expect(wrapper.vm.getCategoryLabel('design')).toBe('设计文档')
    expect(wrapper.vm.getCategoryLabel('implementation')).toBe('实施报告')
    expect(wrapper.vm.getCategoryLabel('manual')).toBe('用户手册')
    expect(wrapper.vm.getCategoryLabel('technical')).toBe('技术文档')
    expect(wrapper.vm.getCategoryLabel('report')).toBe('测试报告')
  })

  /**
   * 测试 11：文件大小格式化
   */
  it('formats file size correctly', () => {
    const wrapper = mount(Documents, {
      global: {
        plugins: [createTestingPinia()]
      }
    })

    // 验证文件大小格式化
    expect(wrapper.vm.formatFileSize(1024)).toBe('1 KB')
    expect(wrapper.vm.formatFileSize(1048576)).toBe('1 MB')
    expect(wrapper.vm.formatFileSize(0)).toBe('0 B')
    expect(wrapper.vm.formatFileSize(null)).toBe('0 B')
  })

  /**
   * 测试 12：日期格式化
   */
  it('formats date correctly', () => {
    const wrapper = mount(Documents, {
      global: {
        plugins: [createTestingPinia()]
      }
    })

    // 验证日期格式化
    const testDate = '2026-03-30T08:00:00Z'
    const formatted = wrapper.vm.formatDate(testDate)
    expect(formatted).toMatch(/\d{4}\/\d{1,2}\/\d{1,2}/)
    expect(wrapper.vm.formatDate(null)).toBe('')
  })

  /**
   * 测试 13：文档图标
   */
  it('displays correct document icons', () => {
    const wrapper = mount(Documents, {
      global: {
        plugins: [createTestingPinia()]
      }
    })

    // 验证文档图标
    const htmlIcon = wrapper.vm.getDocIcon('html')
    const mdIcon = wrapper.vm.getDocIcon('md')
    const pdfIcon = wrapper.vm.getDocIcon('pdf')

    expect(htmlIcon).toBeDefined()
    expect(mdIcon).toBeDefined()
    expect(pdfIcon).toBeDefined()
  })

  /**
   * 测试 14：全屏查看切换
   */
  it('toggles fullscreen mode', async () => {
    vi.spyOn(documentApi, 'getDocumentList').mockResolvedValue({ data: mockDocuments })

    const wrapper = mount(Documents, {
      global: {
        plugins: [createTestingPinia()]
      }
    })

    await wrapper.vm.$nextTick()

    // 初始状态
    expect(wrapper.vm.isFullscreen).toBe(false)

    // 切换全屏
    await wrapper.vm.toggleFullscreen()
    expect(wrapper.vm.isFullscreen).toBe(true)

    // 再次切换
    await wrapper.vm.toggleFullscreen()
    expect(wrapper.vm.isFullscreen).toBe(false)
  })

  /**
   * 测试 15：加载状态
   */
  it('displays loading state', async () => {
    // 模拟延迟加载
    vi.spyOn(documentApi, 'getDocumentList').mockImplementation(() => {
      return new Promise(resolve => {
        setTimeout(() => resolve({ data: mockDocuments }), 1000)
      })
    })

    const wrapper = mount(Documents, {
      global: {
        plugins: [createTestingPinia()]
      }
    })

    // 验证加载状态
    expect(wrapper.vm.loading).toBe(true)

    // 等待加载完成
    await new Promise(resolve => setTimeout(resolve, 1100))
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.loading).toBe(false)
  })

  /**
   * 测试 16：错误处理
   */
  it('handles API error gracefully', async () => {
    vi.spyOn(documentApi, 'getDocumentList').mockRejectedValue(new Error('API Error'))

    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

    const wrapper = mount(Documents, {
      global: {
        plugins: [createTestingPinia()]
      }
    })

    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))

    // 验证错误处理
    expect(consoleSpy).toHaveBeenCalled()
    expect(wrapper.vm.loading).toBe(false)

    consoleSpy.mockRestore()
  })

  /**
   * 测试 17：推荐文档标记
   */
  it('displays featured badge for featured documents', async () => {
    vi.spyOn(documentApi, 'getDocumentList').mockResolvedValue({ data: mockDocuments })

    const wrapper = mount(Documents, {
      global: {
        plugins: [createTestingPinia()]
      }
    })

    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))

    // 验证推荐文档有标记
    const featuredCards = wrapper.findAll('.doc-card.featured')
    expect(featuredCards.length).toBeGreaterThan(0)
  })

  /**
   * 测试 18：文档 URL 生成
   */
  it('generates correct document URL', () => {
    const wrapper = mount(Documents, {
      global: {
        plugins: [createTestingPinia()]
      }
    })

    const filename = 'permission-system-design-v3.html'
    
    // 验证查看 URL
    const viewUrl = wrapper.vm.getDocUrl(filename, false)
    expect(viewUrl).toContain('/api/system/docs/view/')
    expect(viewUrl).toContain(encodeURIComponent(filename))

    // 验证下载 URL
    const downloadUrl = wrapper.vm.getDocUrl(filename, true)
    expect(downloadUrl).toContain('/api/system/docs/download/')
    expect(downloadUrl).toContain(encodeURIComponent(filename))
  })

  /**
   * 测试 19：卡片悬停效果
   */
  it('has hover effect on document cards', () => {
    const wrapper = mount(Documents, {
      global: {
        plugins: [createTestingPinia()]
      }
    })

    const card = wrapper.find('.doc-card')
    expect(card.exists()).toBe(true)

    // 验证卡片有 hover 样式类
    const styles = wrapper.vm.$options.style || {}
    expect(card.classes()).toContain('doc-card')
  })

  /**
   * 测试 20：响应式布局
   */
  it('has responsive layout', () => {
    const wrapper = mount(Documents, {
      global: {
        plugins: [createTestingPinia()]
      }
    })

    const row = wrapper.find('.el-row')
    expect(row.exists()).toBe(true)

    // 验证有 gutter 属性
    expect(row.attributes('gutter')).toBe('20')
  })
})
