import { useUserStore } from '@/stores/user'

/**
 * 判断是否为系统管理员
 * 系统管理员的公司类型 ID 为 4
 */
export function isSystemAdmin() {
  const userStore = useUserStore()
  // 检查用户信息中的公司类型
  const companyTypeId = userStore.userInfo?.companyTypeId
  return companyTypeId === 4
}

/**
 * 检查是否有公司管理权限
 */
export function canManageCompany() {
  return isSystemAdmin()
}

/**
 * 检查是否有角色管理权限
 */
export function canManageRole() {
  return isSystemAdmin()
}

/**
 * 检查是否有作业区管理权限
 */
export function canManageWorkArea() {
  return isSystemAdmin()
}

/**
 * 检查是否有用户管理权限
 */
export function canManageUser() {
  // 所有管理员都可以管理用户（但只能管理本公司）
  const userStore = useUserStore()
  return userStore.isLoggedIn
}

export default {
  isSystemAdmin,
  canManageCompany,
  canManageRole,
  canManageWorkArea,
  canManageUser
}
