import { defineStore } from 'pinia'
import {
  getRoleList,
  getAllRoles,
  getRoleDetail,
  createRole,
  updateRole,
  deleteRole,
  getRolePermissionTree,
  getRolePermissions,
  configureRolePermissions,
  getRoleDataScope,
  configureRoleDataScope,
  getRoleUsers,
  getRoleDefaultPermissions,
  configureRoleDefaultPermissions,
  copyRolePermissions
} from '@/api/role'
import { ElMessage } from 'element-plus'

export const useRoleStore = defineStore('role', {
  state: () => ({
    // 角色列表
    roleList: [],
    // 所有角色（不分页）
    allRoles: [],
    // 角色详情
    currentRole: null,
    // 角色权限树
    rolePermissionTree: [],
    // 角色数据权限
    roleDataScope: null,
    // 角色关联用户
    roleUsers: [],
    // 加载状态
    loading: false,
    // 搜索条件
    searchParams: {
      companyTypeId: null,
      keyword: ''
    }
  }),

  getters: {
    // 角色总数
    roleCount: (state) => state.roleList.length,
    // 获取角色类型选项
    companyTypeOptions: () => [
      { value: 1, label: '总包单位' },
      { value: 2, label: '分包单位' },
      { value: 3, label: '监理单位' },
      { value: 4, label: '建设单位' }
    ]
  },

  actions: {
    // 加载角色列表
    async loadRoleList(params) {
      this.loading = true
      try {
        const res = await getRoleList(params)
        this.roleList = res.data || { records: [], total: 0 }
        return this.roleList
      } catch (error) {
        console.error('加载角色列表失败:', error)
        ElMessage.error('加载角色列表失败')
        throw error
      } finally {
        this.loading = false
      }
    },

    // 加载所有角色
    async loadAllRoles() {
      this.loading = true
      try {
        const res = await getAllRoles()
        this.allRoles = res.data || []
        return this.allRoles
      } catch (error) {
        console.error('加载所有角色失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    // 获取角色详情
    async loadRoleDetail(id) {
      try {
        const res = await getRoleDetail(id)
        this.currentRole = res.data
        return this.currentRole
      } catch (error) {
        console.error('加载角色详情失败:', error)
        throw error
      }
    },

    // 创建角色
    async createRole(data) {
      try {
        const res = await createRole(data)
        ElMessage.success('创建角色成功')
        // 刷新角色列表
        await this.loadRoleList(this.searchParams)
        return res.data
      } catch (error) {
        console.error('创建角色失败:', error)
        ElMessage.error('创建角色失败：' + error.message)
        throw error
      }
    },

    // 更新角色
    async updateRole(id, data) {
      try {
        const res = await updateRole(id, data)
        ElMessage.success('更新角色成功')
        // 刷新角色列表
        await this.loadRoleList(this.searchParams)
        return res.data
      } catch (error) {
        console.error('更新角色失败:', error)
        ElMessage.error('更新角色失败：' + error.message)
        throw error
      }
    },

    // 删除角色
    async deleteRole(id) {
      try {
        await deleteRole(id)
        ElMessage.success('删除角色成功')
        // 刷新角色列表
        await this.loadRoleList(this.searchParams)
      } catch (error) {
        console.error('删除角色失败:', error)
        ElMessage.error('删除角色失败：' + error.message)
        throw error
      }
    },

    // 加载角色权限树
    async loadRolePermissionTree(roleId) {
      this.loading = true
      try {
        const res = await getRolePermissionTree(roleId)
        this.rolePermissionTree = res.data || []
        return this.rolePermissionTree
      } catch (error) {
        console.error('加载角色权限树失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    // 配置角色权限
    async configureRolePermissions(roleId, permissionIds, comment) {
      try {
        const res = await configureRolePermissions(roleId, { permissionIds, comment })
        ElMessage.success('配置角色权限成功')
        // 刷新权限树
        await this.loadRolePermissionTree(roleId)
        return res.data
      } catch (error) {
        console.error('配置角色权限失败:', error)
        ElMessage.error('配置角色权限失败：' + error.message)
        throw error
      }
    },

    // 加载角色数据权限
    async loadRoleDataScope(roleId) {
      try {
        const res = await getRoleDataScope(roleId)
        this.roleDataScope = res.data
        return this.roleDataScope
      } catch (error) {
        console.error('加载角色数据权限失败:', error)
        throw error
      }
    },

    // 配置角色数据权限
    async configureRoleDataScope(roleId, data) {
      try {
        const res = await configureRoleDataScope(roleId, data)
        ElMessage.success('配置角色数据权限成功')
        this.roleDataScope = res.data
        return res.data
      } catch (error) {
        console.error('配置角色数据权限失败:', error)
        ElMessage.error('配置角色数据权限失败：' + error.message)
        throw error
      }
    },

    // 加载角色关联用户
    async loadRoleUsers(roleId, params) {
      this.loading = true
      try {
        const res = await getRoleUsers(roleId, params)
        this.roleUsers = res.data || { records: [], total: 0 }
        return this.roleUsers
      } catch (error) {
        console.error('加载角色用户失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    // 加载角色缺省权限
    async loadRoleDefaultPermissions(roleId) {
      try {
        const res = await getRoleDefaultPermissions(roleId)
        return res.data || []
      } catch (error) {
        console.error('加载角色缺省权限失败:', error)
        throw error
      }
    },

    // 配置角色缺省权限
    async configureRoleDefaultPermissions(roleId, data) {
      try {
        const res = await configureRoleDefaultPermissions(roleId, data)
        ElMessage.success('配置角色缺省权限成功')
        return res.data
      } catch (error) {
        console.error('配置角色缺省权限失败:', error)
        ElMessage.error('配置角色缺省权限失败：' + error.message)
        throw error
      }
    },

    // 复制角色权限
    async copyRolePermissions(sourceRoleId, targetRoleId) {
      try {
        await copyRolePermissions(sourceRoleId, targetRoleId)
        ElMessage.success('复制权限成功')
        return true
      } catch (error) {
        console.error('复制角色权限失败:', error)
        ElMessage.error('复制角色权限失败：' + error.message)
        throw error
      }
    },

    // 设置搜索条件
    setSearchParams(params) {
      this.searchParams = { ...this.searchParams, ...params }
    },

    // 清空搜索条件
    clearSearchParams() {
      this.searchParams = {
        companyTypeId: null,
        keyword: ''
      }
    },

    // 清空角色数据
    clearRoleData() {
      this.currentRole = null
      this.rolePermissionTree = []
      this.roleDataScope = null
      this.roleUsers = []
    }
  }
})
