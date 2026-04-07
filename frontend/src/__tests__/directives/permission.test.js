import { describe, it, expect, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { defineComponent, h } from 'vue'
import { permissionDirective } from '@/utils/permission'

describe('权限指令', () => {
  let pinia

  beforeEach(() => {
    pinia = createPinia()
    setActivePinia(pinia)
  })

  const createTestComponent = (permissions, directiveValue) => {
    return defineComponent({
      directives: {
        permission: permissionDirective
      },
      template: '<div v-permission="value">test</div>',
      data() {
        return {
          value: directiveValue
        }
      },
      mounted() {
        // Mock user store permissions
        const { useUserStore } = require('@/stores/user')
        const store = useUserStore()
        store.permissions = permissions
      }
    })
  }

  it('有权限时显示元素', async () => {
    const wrapper = mount(createTestComponent(['user:create'], 'user:create'), {
      global: {
        plugins: [pinia]
      }
    })

    expect(wrapper.text()).toContain('test')
  })

  it('无权限时隐藏元素', async () => {
    const wrapper = mount(createTestComponent(['user:edit'], 'user:create'), {
      global: {
        plugins: [pinia]
      }
    })

    // 无权限时元素应该被移除
    expect(wrapper.text()).not.toContain('test')
  })

  it('支持权限数组（任一匹配）', async () => {
    const wrapper = mount(createTestComponent(
      ['user:edit', 'user:delete'],
      ['user:create', 'user:edit']
    ), {
      global: {
        plugins: [pinia]
      }
    })

    // user:edit 匹配，应该显示
    expect(wrapper.text()).toContain('test')
  })

  it('支持通配符匹配', async () => {
    const wrapper = mount(createTestComponent(
      ['system:user:create:button', 'system:user:edit:button'],
      'system:user:*:button'
    ), {
      global: {
        plugins: [pinia]
      }
    })

    // 通配符匹配，应该显示
    expect(wrapper.text()).toContain('test')
  })

  it('支持层级权限匹配', async () => {
    const wrapper = mount(createTestComponent(
      ['system:user:create:button'],
      'user:create'
    ), {
      global: {
        plugins: [pinia]
      }
    })

    // 层级匹配，应该显示
    expect(wrapper.text()).toContain('test')
  })
})
