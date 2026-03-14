import { defineStore } from 'pinia'
import { login, logout } from '@/api/auth'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('accessToken') || '',
    refreshToken: localStorage.getItem('refreshToken') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}')
  }),
  
  getters: {
    isLoggedIn: (state) => !!state.token,
    username: (state) => state.userInfo.username || '',
    realName: (state) => state.userInfo.realName || '',
    roles: (state) => state.userInfo.roles || [],
    permissions: (state) => state.userInfo.permissions || [],
    companyId: (state) => state.userInfo.companyId || null,
    companyTypeId: (state) => state.userInfo.companyTypeId || null
  },
  
  actions: {
    // 登录
    async login(loginForm) {
      try {
        const res = await login(loginForm)
        const { accessToken, refreshToken, userInfo } = res.data
        
        this.token = accessToken
        this.refreshToken = refreshToken
        this.userInfo = userInfo
        
        // 保存到 localStorage
        localStorage.setItem('accessToken', accessToken)
        localStorage.setItem('refreshToken', refreshToken)
        localStorage.setItem('userInfo', JSON.stringify(userInfo))
        
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
    }
  }
})
