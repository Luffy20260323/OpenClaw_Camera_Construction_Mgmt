/**
 * 角色编码常量
 * 
 * 所有角色相关的编码都定义在这里，避免在代码中分散硬编码
 */

// 超级管理员角色编码
export const ROLE_SUPER_ADMIN = 'ROLE_SUPER_ADMIN'

// 系统管理员角色编码
export const ROLE_SYSTEM_ADMIN = 'ROLE_SYSTEM_ADMIN'

// 甲方管理员角色编码
export const ROLE_JIAFANG_ADMIN = 'ROLE_JIAFANG_ADMIN'

// 乙方管理员角色编码
export const ROLE_YIFANG_ADMIN = 'ROLE_YIFANG_ADMIN'

// 监理方管理员角色编码
export const ROLE_JIANLIFANG_ADMIN = 'ROLE_JIANLIFANG_ADMIN'

// 导出所有角色常量
export const RoleConstants = {
  SUPER_ADMIN: ROLE_SUPER_ADMIN,
  SYSTEM_ADMIN: ROLE_SYSTEM_ADMIN,
  JIAFANG_ADMIN: ROLE_JIAFANG_ADMIN,
  YIFANG_ADMIN: ROLE_YIFANG_ADMIN,
  JIANLIFANG_ADMIN: ROLE_JIANLIFANG_ADMIN
}

// 默认导出
export default RoleConstants
