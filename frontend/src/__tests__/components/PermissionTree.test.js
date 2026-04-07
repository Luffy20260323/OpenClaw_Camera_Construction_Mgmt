import { describe, it, expect, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import PermissionTree from '@/components/PermissionTree.vue'

describe('PermissionTree', () => {
  let pinia

  beforeEach(() => {
    pinia = createPinia()
    setActivePinia(pinia)
  })

  const mockTreeData = [
    {
      id: 1,
      name: '系统管理',
      code: 'system',
      type: 'MODULE',
      children: [
        {
          id: 2,
          name: '用户管理',
          code: 'system:user',
          type: 'MENU',
          children: [
            {
              id: 3,
              name: '用户列表',
              code: 'system:user:list',
              type: 'PAGE'
            },
            {
              id: 4,
              name: '创建用户',
              code: 'system:user:create',
              type: 'ELEMENT'
            }
          ]
        }
      ]
    }
  ]

  it('渲染树形数据', () => {
    const wrapper = mount(PermissionTree, {
      props: {
        treeData: mockTreeData
      },
      global: {
        plugins: [pinia]
      }
    })

    expect(wrapper.find('.permission-tree').exists()).toBe(true)
    expect(wrapper.text()).toContain('系统管理')
  })

  it('显示类型标签', async () => {
    const wrapper = mount(PermissionTree, {
      props: {
        treeData: mockTreeData,
        showTypeTag: true
      },
      global: {
        plugins: [pinia]
      }
    })

    // 展开所有节点
    await wrapper.vm.expandAll()
    
    expect(wrapper.text()).toContain('模块')
    expect(wrapper.text()).toContain('菜单')
  })

  it('显示资源编码', () => {
    const wrapper = mount(PermissionTree, {
      props: {
        treeData: mockTreeData,
        showCode: true
      },
      global: {
        plugins: [pinia]
      }
    })

    expect(wrapper.text()).toContain('system')
    expect(wrapper.text()).toContain('system:user')
  })

  it('处理节点选中事件', async () => {
    const wrapper = mount(PermissionTree, {
      props: {
        treeData: mockTreeData,
        checkedKeys: [1]
      },
      global: {
        plugins: [pinia]
      }
    })

    // 验证初始选中状态
    expect(wrapper.props('checkedKeys')).toEqual([1])
  })

  it('暴露正确的方法', () => {
    const wrapper = mount(PermissionTree, {
      props: {
        treeData: mockTreeData
      },
      global: {
        plugins: [pinia]
      }
    })

    expect(wrapper.vm.getCheckedKeys).toBeDefined()
    expect(wrapper.vm.getCheckedNodes).toBeDefined()
    expect(wrapper.vm.setCheckedKeys).toBeDefined()
    expect(wrapper.vm.expandAll).toBeDefined()
    expect(wrapper.vm.collapseAll).toBeDefined()
  })
})
