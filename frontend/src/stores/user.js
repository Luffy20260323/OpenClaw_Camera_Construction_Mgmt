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
        
        // 调试：检查 menus 的 children
        console.log('[Login] menus 详细结构:')
        menus?.forEach((m, i) => {
          const childCount = m.children?.length || 0
          console.log(`  [${i}] code=${m.menuCode || m.code}, name=${m.menuName || m.name}, children=${childCount}`)
        })
        
        // userInfo.menus 是菜单编码数组（字符串数组），用于路由权限检查
        // menus 是完整的菜单树（对象数组），用于侧边栏渲染
        // 路由守卫期望 menus 是编码数组，所以使用 userInfo.menus
        const userInfoWithMenus = {
          ...userInfo,
          menus: userInfo.menus || [],  // 使用编码数组，不是菜单树
          menuTree: menus || []  // 保存菜单树供侧边栏使用
        }
        
        console.log('[Login] userInfoWithMenus.menuTree 长度:', userInfoWithMenus.menuTree?.length)
        console.log('[Login] userInfoWithMenus.menuTree 第一个元素的 children:', userInfoWithMenus.menuTree?.[0]?.children?.length)
        
        console.log('[Login] 保存的菜单编码:', userInfoWithMenus.menus)
        
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
        const menuTree = res.data || []
        
        console.log('[refreshMenus] 获取到新菜单数据:', menuTree)
        
        // 更新 userInfo.menuTree（完整菜单树）
        const userInfoWithMenus = {
          ...this.userInfo,
          menuTree
        }
        
        // 保存到 localStorage 和 state
        localStorage.setItem('userInfo', JSON.stringify(userInfoWithMenus))
        this.userInfo = userInfoWithMenus
        
        // 强制触发响应式更新
        this.$patch({
          userInfo: { ...userInfoWithMenus }
        })
        
        return menuTree
      } catch (error) {
        console.error('[refreshMenus] 刷新菜单失败:', error)
        throw error
      }
    }
  }
})
