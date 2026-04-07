/**
 * 菜单权限指令
 * 使用方式：v-menu-permission="'system:resource:list:page'"
 * 
 * 用于控制菜单项的显示/隐藏
 * 根据用户的菜单权限判断是否显示该菜单项
 */
import { useUserStore } from '@/stores/user'

/**
 * 递归检查菜单权限
 * @param {Array} menus - 用户菜单列表
 * @param {string} menuCode - 目标菜单编码
 * @returns {boolean} 是否有权限
 */
function checkMenuPermission(menus, menuCode) {
  if (!menus || !Array.isArray(menus)) return false
  
  for (const menu of menus) {
    const code = typeof menu === 'string' ? menu : (menu.menuCode || '')
    if (code === menuCode) {
      return true
    }
    // 递归检查子菜单
    if (menu.children && menu.children.length > 0) {
      if (checkMenuPermission(menu.children, menuCode)) {
        return true
      }
    }
  }
  return false
}

export default {
  mounted(el, binding) {
    const { value } = binding
    const userStore = useUserStore()
    const menus = userStore.menus || []
    
    if (!value) {
      console.warn('v-menu-permission: 未提供菜单编码')
      return
    }
    
    // 检查菜单权限
    const hasPermission = checkMenuPermission(menus, value)
    
    if (!hasPermission) {
      // 无权限，移除元素
      el.parentNode && el.parentNode.removeChild(el)
    }
  },
  
  // 支持动态更新
  updated(el, binding) {
    const { value, oldValue } = binding
    
    if (value !== oldValue) {
      const userStore = useUserStore()
      const menus = userStore.menus || []
      
      const hasPermission = checkMenuPermission(menus, value)
      
      if (!hasPermission && el.parentNode) {
        el.parentNode.removeChild(el)
      }
    }
  }
}
