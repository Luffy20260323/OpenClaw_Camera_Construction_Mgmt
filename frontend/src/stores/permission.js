import { defineStore } from 'pinia'
import {
  getAllPermissions,
  getRolePermissions,
  updateRolePermissions,
  getPermissionMatrix,
  getAuditLogs,
  getPermissionAuditLogs,
  exportPermissionAuditLogs,
  updatePermissionDescription,
  adjustRolePermission
} from '@/api/permission'
import { ElMessage } from 'element-plus'

export const usePermissionStore = defineStore('permission', {
  state: () => ({
    // 所有权限列表
    allPermissions: [],
    // 权限树
    permissionTree: [],
    // 权限矩阵
    permissionMatrix: {},
    // 审计日志
    auditLogs: {
      records: [],
      total: 0
    },
    // 加载状态
    loading: false,
    // 选中的权限
    selectedPermission: null
  }),

  getters: {
    // 权限总数
    permissionCount: (state) => state.allPermissions.length,
    // 按模块分组权限
    permissionsByModule: (state) => {
      return state.allPermissions.reduce((acc, perm) => {
        const module = perm.module || 'other'
        if (!acc[module]) {
          acc[module] = []
        }
        acc[module].push(perm)
        return acc
      }, {})
    },
    // 获取权限类型选项
    permissionTypeOptions: () => [
      { value: 'BUTTON', label: '按钮' },
      { value: 'PAGE', label: '页面' },
      { value: 'API', label: 'API' },
      { value: 'DATA', label: '数据' }
    ]
  },

  actions: {
    // 加载所有权限
    async loadAllPermissions() {
      this.loading = true
      try {
        const res = await getAllPermissions()
        this.allPermissions = res.data || []
        return this.allPermissions
      } catch (error) {
        console.error('加载权限列表失败:', error)
        ElMessage.error('加载权限列表失败')
        throw error
      } finally {
        this.loading = false
      }
    },

    // 加载权限树
    async loadPermissionTree() {
      this.loading = true
      try {
        // 使用资源树的权限视图
        const res = await getAllPermissions()
        this.permissionTree = this.buildPermissionTree(res.data || [])
        return this.permissionTree
      } catch (error) {
        console.error('加载权限树失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    // 构建权限树（从扁平列表）
    buildPermissionTree(permissions) {
      const tree = []
      const map = new Map()

      // 先创建所有节点的映射
      permissions.forEach(perm => {
        map.set(perm.id, { ...perm, children: [] })
      })

      // 构建树形结构
      permissions.forEach(perm => {
        const node = map.get(perm.id)
        if (perm.parentId) {
          const parent = map.get(perm.parentId)
          if (parent) {
            parent.children.push(node)
          } else {
            tree.push(node)
          }
        } else {
          tree.push(node)
        }
      })

      return tree
    },

    // 加载权限矩阵
    async loadPermissionMatrix() {
      try {
        const res = await getPermissionMatrix()
        this.permissionMatrix = res.data || {}
        return this.permissionMatrix
      } catch (error) {
        console.error('加载权限矩阵失败:', error)
        throw error
      }
    },

    // 获取角色权限
    async loadRolePermissions(roleId) {
      this.loading = true
      try {
        const res = await getRolePermissions(roleId)
        return res.data || []
      } catch (error) {
        console.error('加载角色权限失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    // 更新角色权限
    async updateRolePermissions(roleId, permissionIds, comment) {
      try {
        const res = await updateRolePermissions(roleId, { permissionIds, comment })
        ElMessage.success('更新角色权限成功')
        return res.data
      } catch (error) {
        console.error('更新角色权限失败:', error)
        ElMessage.error('更新角色权限失败：' + error.message)
        throw error
      }
    },

    // 调整角色权限
    async adjustRolePermission(roleId, resourceId, action) {
      try {
        const res = await adjustRolePermission(roleId, { resourceId, action })
        ElMessage.success(`${action === 'ADD' ? '添加' : '移除'}权限成功`)
        return res.data
      } catch (error) {
        console.error('调整角色权限失败:', error)
        ElMessage.error('调整角色权限失败：' + error.message)
        throw error
      }
    },

    // 加载审计日志
    async loadAuditLogs(params) {
      this.loading = true
      try {
        const res = await getAuditLogs(params)
        this.auditLogs = res.data || { records: [], total: 0 }
        return this.auditLogs
      } catch (error) {
        console.error('加载审计日志失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    // 加载权限审计日志
    async loadPermissionAuditLogs(params) {
      this.loading = true
      try {
        const res = await getPermissionAuditLogs(params)
        this.auditLogs = res.data || { records: [], total: 0 }
        return this.auditLogs
      } catch (error) {
        console.error('加载权限审计日志失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    // 导出权限审计日志
    async exportPermissionAuditLogs(params) {
      try {
        const blob = await exportPermissionAuditLogs(params)
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `权限审计日志_${new Date().getTime()}.xlsx`
        link.click()
        window.URL.revokeObjectURL(url)
        ElMessage.success('导出成功')
      } catch (error) {
        console.error('导出权限审计日志失败:', error)
        ElMessage.error('导出失败：' + error.message)
        throw error
      }
    },

    // 更新权限描述
    async updatePermissionDescription(id, description) {
      try {
        await updatePermissionDescription(id, description)
        ElMessage.success('更新权限描述成功')
        // 刷新权限列表
        await this.loadAllPermissions()
      } catch (error) {
        console.error('更新权限描述失败:', error)
        ElMessage.error('更新权限描述失败：' + error.message)
        throw error
      }
    },

    // 清空权限数据
    clearPermissions() {
      this.allPermissions = []
      this.permissionTree = []
      this.permissionMatrix = {}
    }
  }
})
