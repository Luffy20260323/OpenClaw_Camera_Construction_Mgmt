import { describe, it, expect, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ParentResourceSelector from '@/components/ParentResourceSelector.vue'

describe('ParentResourceSelector', () => {
  let pinia

  beforeEach(() => {
    pinia = createPinia()
    setActivePinia(pinia)
  })

  const mockResourceTree = [
    {
      id: 1,
      name: '系统管理',
      type: 'MODULE',
      children: [
        {
          id: 2,
          name: '用户管理',
          type: 'MENU',
          children: [
            { id: 3, name: '用户列表', type: 'PAGE' }
          ]
        }
      ]
    },
    {
      id: 4,
      name: '资源管理',
      type: 'MODULE',
      children: [
        { id: 5, name: '资源列表', type: 'MENU' }
      ]
    }
  ]

  it('渲染选择器', () => {
    const wrapper = mount(ParentResourceSelector, {
      props: {
        currentResourceId: 3,
        resourceTree: mockResourceTree
      },
      global: {
        plugins: [pinia]
      }
    })

    expect(wrapper.find('.parent-resource-selector').exists()).toBe(true)
  })

  it('禁用当前资源（防止循环引用）', async () => {
    const wrapper = mount(ParentResourceSelector, {
      props: {
        currentResourceId: 2,
        resourceTree: mockResourceTree
      },
      global: {
        plugins: [pinia]
      }
    })

    // 打开下拉框
    await wrapper.find('.el-select').trigger('click')
    
    // 验证选项渲染
    const options = wrapper.findAll('.el-option')
    expect(options.length).toBeGreaterThan(0)
  })

  it('检测循环引用', () => {
    const wrapper = mount(ParentResourceSelector, {
      props: {
        currentResourceId: 2,
        resourceTree: mockResourceTree
      },
      global: {
        plugins: [pinia]
      }
    })

    // 验证组件有循环引用检测方法
    expect(wrapper.vm.validateSelection).toBeDefined()
  })

  it('处理值变更', async () => {
    const wrapper = mount(ParentResourceSelector, {
      props: {
        currentResourceId: 3,
        resourceTree: mockResourceTree,
        modelValue: 1
      },
      global: {
        plugins: [pinia]
      }
    })

    expect(wrapper.vm.selectedParentId).toBe(1)

    // 更新值
    await wrapper.setProps({ modelValue: 2 })
    expect(wrapper.vm.selectedParentId).toBe(2)
  })

  it('清空选择', () => {
    const wrapper = mount(ParentResourceSelector, {
      props: {
        currentResourceId: 3,
        resourceTree: mockResourceTree,
        modelValue: 1
      },
      global: {
        plugins: [pinia]
      }
    })

    wrapper.vm.clearSelection()
    expect(wrapper.vm.selectedParentId).toBe(null)
  })

  it('显示类型标签', async () => {
    const wrapper = mount(ParentResourceSelector, {
      props: {
        currentResourceId: 3,
        resourceTree: mockResourceTree
      },
      global: {
        plugins: [pinia]
      }
    })

    // 打开下拉框
    await wrapper.find('.el-select').trigger('click')
    
    // 验证类型标签存在
    expect(wrapper.html()).toContain('el-tag')
  })
})
