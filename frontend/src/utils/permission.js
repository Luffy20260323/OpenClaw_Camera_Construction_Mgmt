import { useUserStore } from '@/stores/user'

/**
 * 检查用户是否有指定菜单的查看权限
 * @param {string} menuCode - 菜单编码
 * @returns {boolean} - 是否有权限
 */
export function hasMenuPermission(menuCode) {
  const userStore = useUserStore()
  const menus = userStore.menus || []
  return menus.some(m => {
    const code = typeof m === 'string' ? m : m.menuCode
    return code === menuCode
  })
}

/**
 * 检查用户是否有指定菜单的操作权限
 * @param {string} menuCode - 菜单编码
 * @returns {boolean} - 是否有操作权限
 */
export function hasMenuOperatePermission(menuCode) {
  const userStore = useUserStore()
  const menus = userStore.menus || []
  const menu = menus.find(m => {
    const code = typeof m === 'string' ? m : m.menuCode
    return code === menuCode
  })
  
  if (!menu || typeof menu === 'string') return false
  return menu.userCanOperate !== false
}

/**
 * 检查用户是否有指定权限点
 * @param {string} permission - 权限点编码
 * @returns {boolean} - 是否有权限
 */
export function hasPermission(permission) {
  const userStore = useUserStore()
  const permissions = userStore.permissions || []
  return permissions.includes(permission)
}

/**
 * 检查用户是否有任一权限点
 * @param {string[]} permissions - 权限点列表
 * @returns {boolean} - 是否有任一权限
 */
export function hasAnyPermission(permissions) {
  return permissions.some(p => hasPermission(p))
}

/**
 * 检查用户是否有所有权限点
 * @param {string[]} permissions - 权限点列表
 * @returns {boolean} - 是否有所有权限
 */
export function hasAllPermissions(permissions) {
  return permissions.every(p => hasPermission(p))
}

/**
 * Vue 指令：检查菜单权限
 * 用法：v-menu-permission="'user_management'"
 */
export const menuPermissionDirective = {
  mounted(el, binding) {
    const { value } = binding
    if (!hasMenuPermission(value)) {
      el.parentNode && el.parentNode.removeChild(el)
    }
  }
}

/**
 * Vue 指令：检查操作权限
 * 用法：v-menu-operate="'user_management'"
 */
export const menuOperateDirective = {
  mounted(el, binding) {
    const { value } = binding
    if (!hasMenuOperatePermission(value)) {
      el.parentNode && el.parentNode.removeChild(el)
    }
  }
}

/**
 * Vue 指令：检查权限点
 * 用法：v-permission="'user:create'" 或 v-permission="['user:create', 'user:edit']"
 * 支持通配符：v-permission="'system:user:*:button'"
 */
export const permissionDirective = {
  mounted(el, binding) {
    const { value } = binding
    const userStore = useUserStore()
    const permissions = userStore.permissions || []
    
    if (!value) return
    
    // 支持单个权限或权限数组
    const requiredPermissions = Array.isArray(value) ? value : [value]
    
    // 检查是否有任一权限（支持通配符匹配）
    const hasPermission = requiredPermissions.some(requiredPerm => {
      // 精确匹配
      if (permissions.includes(requiredPerm)) {
        return true
      }
      
      // 通配符匹配（支持 *）
      if (requiredPerm.includes('*')) {
        const pattern = requiredPerm.replace(/\*/g, '.*')
        const regex = new RegExp(`^${pattern}$`)
        return permissions.some(perm => regex.test(perm))
      }
      
      // 层级权限匹配（兼容旧格式）
      if (requiredPerm.split(':').length === 2) {
        const [module, action] = requiredPerm.split(':')
        return permissions.some(perm => {
          const parts = perm.split(':')
          if (parts.length >= 3) {
            return parts[0] === module && parts[2] === action
          } else if (parts.length === 2) {
            return parts[0] === module && parts[1] === action
          }
          return false
        })
      }
      
      return false
    })
    
    if (!hasPermission) {
      el.parentNode && el.parentNode.removeChild(el)
    }
  }
}
