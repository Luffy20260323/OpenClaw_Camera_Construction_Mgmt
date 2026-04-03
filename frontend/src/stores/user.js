import { defineStore } from 'pinia'
import { login, logout } from '@/api/auth'
import { getMyMenus } from '@/api/menu'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('accessToken') || '',
    refreshToken: localStorage.getItem('refreshToken') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}')
  }),
  
  getters: {
    isLoggedIn: (state) => !!state.token,
    username: (state) => state.userInfo?.username || '',
    realName: (state) => state.userInfo?.realName || '',
    roles: (state) => state.userInfo?.roles || [],
    permissions: (state) => state.userInfo?.permissions || [],
    companyId: (state) => state.userInfo?.companyId || null,
    companyTypeId: (state) => state.userInfo?.companyTypeId || null,
    menus: (state) => state.userInfo?.menus || []
  },
  
  actions: {
    // 登录
    async login(loginForm) {
      try {
        const res = await login(loginForm)
        const { accessToken, refreshToken, userInfo, menus } = res.data
        
        console.log('[Login] 登录响应数据:', { userInfo, menus })
        
        // 将完整菜单数据添加到 userInfo 中（优先使用 menus，如果没有则用 userInfo.menus）
        const userInfoWithMenus = {
          ...userInfo,
          menus: menus || userInfo.menus || []
        }
        
        console.log('[Login] 保存的菜单数据:', userInfoWithMenus.menus)
        
        // 先保存到 localStorage
        localStorage.setItem('accessToken', accessToken)
        localStorage.setItem('refreshToken', refreshToken)
        localStorage.setItem('userInfo', JSON.stringify(userInfoWithMenus))
        
        // 再更新 state（确保顺序）
        this.token = accessToken
        this.refreshToken = refreshToken
        this.userInfo = userInfoWithMenus
        
        // 强制触发响应式更新
        this.$patch({
          userInfo: { ...userInfoWithMenus }
        })
        
        ElMessage.success('登录成功')
        return Promise.resolve(res)
      } catch (error) {
        ElMessage.error('登录失败：' + error.message)
        return Promise.reject(error)
      }
    },
    
    // 登出
    async logout() {
      try {
        await logout()
      } catch (error) {
        console.error('登出 API 调用失败:', error)
      } finally {
        this.resetState()
      }
    },
    
    // 重置状态
    resetState() {
      this.token = ''
      this.refreshToken = ''
      this.userInfo = {}
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('userInfo')
    },
    
    // 刷新菜单数据（用于资源管理修改后实时更新侧边栏）
    async refreshMenus() {
      if (!this.token) {
        console.warn('[refreshMenus] 未登录，无法刷新菜单')
        return
      }
      
      try {
        const res = await getMyMenus()
        const menus = res.data || []
        
        console.log('[refreshMenus] 获取到新菜单数据:', menus)
        
        // 更新 userInfo.menus
        const userInfoWithMenus = {
          ...this.userInfo,
          menus
        }
        
        // 保存到 localStorage 和 state
        localStorage.setItem('userInfo', JSON.stringify(userInfoWithMenus))
        this.userInfo = userInfoWithMenus
        
        // 强制触发响应式更新
        this.$patch({
          userInfo: { ...userInfoWithMenus }
        })
        
        return menus
      } catch (error) {
        console.error('[refreshMenus] 刷新菜单失败:', error)
        throw error
      }
    }
  }
})
