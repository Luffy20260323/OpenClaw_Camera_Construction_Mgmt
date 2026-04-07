import { defineStore } from 'pinia'
import {
  getUserList,
  getUserDetail,
  getUserPermissionTree,
  getUserPermissions,
  configureUserPermissions,
  getUserDataScope,
  configureUserDataScope,
  getUserRoles,
  assignUserRole,
  removeUserRole,
  getUserPermissionDetail,
  batchConfigureUserPermissions
} from '@/api/user-permission'
import { ElMessage } from 'element-plus'

export const useUserPermissionStore = defineStore('userPermission', {
  state: () => ({
    // 用户列表
    userList: [],
    // 当前用户详情
    currentUser: null,
    // 用户权限树
    userPermissionTree: [],
    // 用户数据权限
    userDataScope: null,
    // 用户角色列表
    userRoles: [],
    // 用户权限详情
    userPermissionDetail: null,
    // 加载状态
    loading: false,
    // 搜索条件
    searchParams: {
      keyword: '',
      companyId: null,
      roleId: null
    }
  }),

  getters: {
    // 用户总数
    userCount: (state) => state.userList.length
  },

  actions: {
    // 加载用户列表
    async loadUserList(params) {
      this.loading = true
      try {
        const res = await getUserList(params)
        this.userList = res.data || { records: [], total: 0 }
        return this.userList
      } catch (error) {
        console.error('加载用户列表失败:', error)
        ElMessage.error('加载用户列表失败')
        throw error
      } finally {
        this.loading = false
      }
    },

    // 获取用户详情
    async loadUserDetail(id) {
      try {
        const res = await getUserDetail(id)
        this.currentUser = res.data
        return this.currentUser
      } catch (error) {
        console.error('加载用户详情失败:', error)
        throw error
      }
    },

    // 加载用户权限树
    async loadUserPermissionTree(userId) {
      this.loading = true
      try {
        const res = await getUserPermissionTree(userId)
        this.userPermissionTree = res.data || []
        return this.userPermissionTree
      } catch (error) {
        console.error('加载用户权限树失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    // 配置用户权限
    async configureUserPermissions(userId, permissionIds, comment) {
      try {
        const res = await configureUserPermissions(userId, { permissionIds, comment })
        ElMessage.success('配置用户权限成功')
        // 刷新权限树
        await this.loadUserPermissionTree(userId)
        return res.data
      } catch (error) {
        console.error('配置用户权限失败:', error)
        ElMessage.error('配置用户权限失败：' + error.message)
        throw error
      }
    },

    // 加载用户数据权限
    async loadUserDataScope(userId) {
      try {
        const res = await getUserDataScope(userId)
        this.userDataScope = res.data
        return this.userDataScope
      } catch (error) {
        console.error('加载用户数据权限失败:', error)
        throw error
      }
    },

    // 配置用户数据权限
    async configureUserDataScope(userId, data) {
      try {
        const res = await configureUserDataScope(userId, data)
        ElMessage.success('配置用户数据权限成功')
        this.userDataScope = res.data
        return res.data
      } catch (error) {
        console.error('配置用户数据权限失败:', error)
        ElMessage.error('配置用户数据权限失败：' + error.message)
        throw error
      }
    },

    // 加载用户角色
    async loadUserRoles(userId) {
      try {
        const res = await getUserRoles(userId)
        this.userRoles = res.data || []
        return this.userRoles
      } catch (error) {
        console.error('加载用户角色失败:', error)
        throw error
      }
    },

    // 分配用户角色
    async assignUserRole(userId, roleIds) {
      try {
        const res = await assignUserRole(userId, roleIds)
        ElMessage.success('分配角色成功')
        // 刷新用户角色
        await this.loadUserRoles(userId)
        return res.data
      } catch (error) {
        console.error('分配用户角色失败:', error)
        ElMessage.error('分配用户角色失败：' + error.message)
        throw error
      }
    },

    // 移除用户角色
    async removeUserRole(userId, roleId) {
      try {
        await removeUserRole(userId, roleId)
        ElMessage.success('移除角色成功')
        // 刷新用户角色
        await this.loadUserRoles(userId)
      } catch (error) {
        console.error('移除用户角色失败:', error)
        ElMessage.error('移除用户角色失败：' + error.message)
        throw error
      }
    },

    // 加载用户权限详情
    async loadUserPermissionDetail(userId) {
      this.loading = true
      try {
        const res = await getUserPermissionDetail(userId)
        this.userPermissionDetail = res.data
        return this.userPermissionDetail
      } catch (error) {
        console.error('加载用户权限详情失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    // 批量配置用户权限
    async batchConfigureUserPermissions(userIds, permissionIds, action) {
      try {
        const res = await batchConfigureUserPermissions({
          userIds,
          permissionIds,
          action // 'ADD' or 'REMOVE'
        })
        ElMessage.success('批量配置权限成功')
        return res.data
      } catch (error) {
        console.error('批量配置用户权限失败:', error)
        ElMessage.error('批量配置用户权限失败：' + error.message)
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
        keyword: '',
        companyId: null,
        roleId: null
      }
    },

    // 清空用户数据
    clearUserData() {
      this.currentUser = null
      this.userPermissionTree = []
      this.userDataScope = null
      this.userRoles = []
      this.userPermissionDetail = null
    }
  }
})
