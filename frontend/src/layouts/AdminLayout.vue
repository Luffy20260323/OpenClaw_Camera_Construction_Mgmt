<template>
  <el-container class="admin-layout" :class="{ 'sidebar-right': sidebarConfig.position === 'RIGHT' }">
    <!-- 侧边栏（支持左右位置） -->
    <el-aside 
      v-show="isSidebarVisible"
      :style="sidebarStyle"
      class="sidebar"
    >
      <SidebarMenu :is-collapse="isCollapse" @menu-select="handleMenuSelect" @collapse-toggle="toggleCollapse" />
    </el-aside>

    <el-container class="main-wrapper" direction="vertical">
      <!-- 顶部导航栏 -->
      <el-header class="header">
        <div class="header-left">
          <span class="app-title">视频监控点位施工项目管理系统</span>
        </div>
        <div class="header-right">
          <!-- 返回首页按钮 -->
          <el-button text @click="goHome" title="返回首页">
            <el-icon><HomeFilled /></el-icon>
          </el-button>
          
          <!-- 用户信息下拉菜单 -->
          <el-dropdown @command="handleCommand" class="user-dropdown" :key="menuKey">
            <span class="user-info">
              <el-avatar :size="32" icon="User" />
              <span class="username">{{ userStore.realName || userStore.username }}</span>
              <span class="user-roles">{{ displayRoles }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <!-- 个人中心（始终显示） -->
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                
                <!-- 退出登录 -->
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="main-content">
        <router-view />
      </el-main>

      <!-- 底部版权声明 -->
      <el-footer class="footer">
        <p>© 2026 北京其点技术服务有限公司 版权所有</p>
      </el-footer>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { computed, ref, onMounted, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessageBox, ElMessage } from 'element-plus'
import { ArrowDown, HomeFilled, Expand, Fold } from '@element-plus/icons-vue'
import SidebarMenu from '@/components/SidebarMenu.vue'

const router = useRouter()
const userStore = useUserStore()

// 侧边栏配置
const sidebarConfig = ref({
  position: 'LEFT', // LEFT 或 RIGHT
  mode: 'FIXED' // FIXED 或 COLLAPSIBLE
})

// 侧边栏折叠状态（用户手动隐藏）
const isSidebarHidden = ref(false)

// 侧边栏宽度折叠状态（COLLAPSIBLE 模式下的窄/宽）
const isCollapse = ref(false)

// 计算侧边栏是否可折叠（宽度变化）
const isCollapsible = computed(() => {
  return sidebarConfig.value.mode === 'COLLAPSIBLE'
})

// 计算侧边栏实际显示状态
const isSidebarVisible = computed(() => {
  // 用户手动隐藏时，不显示侧边栏
  if (isSidebarHidden.value) {
    return false
  }
  // FIXED 模式始终显示（除非用户隐藏）
  if (sidebarConfig.value.mode === 'FIXED') {
    return true
  }
  // COLLAPSIBLE 模式：显示但可能宽度窄
  return true
})

// 计算侧边栏样式（order 已移除，由 CSS flex-direction: row-reverse 处理右侧布局）
const sidebarStyle = computed(() => {
  const baseStyle = {
    transition: 'width 0.3s, opacity 0.3s'
  }
  // 用户手动隐藏时，完全隐藏侧边栏
  if (isSidebarHidden.value) {
    baseStyle.width = '0px'
    baseStyle.opacity = '0'
    baseStyle.overflow = 'hidden'
    return baseStyle
  }
  // 正常显示状态
  if (sidebarConfig.value.mode === 'COLLAPSIBLE' && isCollapse.value) {
    baseStyle.width = '64px'  // 折叠模式下的窄宽度
  } else {
    baseStyle.width = '240px'  // 正常宽度
  }
  baseStyle.opacity = '1'
  return baseStyle
})

// 加载侧边栏配置（从后端 API 加载，localStorage 作为降级方案）
const loadSidebarConfig = async () => {
  try {
    const token = localStorage.getItem('accessToken')
    const response = await fetch('/api/system/config/sidebar', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    const result = await response.json()
    if (result.success && result.data) {
      // 从后端加载配置
      sidebarConfig.value = {
        position: result.data.sidebarPosition || 'LEFT',
        mode: result.data.sidebarMode || 'FIXED'
      }
      // 同步到 localStorage 作为缓存
      localStorage.setItem('sidebarConfig', JSON.stringify(sidebarConfig.value))
      console.log('[AdminLayout] 从后端加载侧边栏配置:', sidebarConfig.value)
    } else {
      // 后端返回失败，使用 localStorage 降级
      loadSidebarConfigFromLocal()
    }
  } catch (error) {
    console.error('[AdminLayout] 从后端加载侧边栏配置失败:', error)
    // 使用 localStorage 降级
    loadSidebarConfigFromLocal()
  }
  
  // 根据配置初始化折叠状态
  // 加载用户手动隐藏状态
  const savedHidden = localStorage.getItem('sidebarHidden')
  if (savedHidden !== null) {
    isSidebarHidden.value = savedHidden === 'true'
  }
  
  // COLLAPSIBLE 模式下的宽度折叠状态
  if (sidebarConfig.value.mode === 'COLLAPSIBLE') {
    const savedCollapse = localStorage.getItem('sidebarCollapsed')
    if (savedCollapse !== null) {
      isCollapse.value = savedCollapse === 'true'
    }
  } else {
    isCollapse.value = false
  }
}

// 从 localStorage 加载配置（降级方案）
const loadSidebarConfigFromLocal = () => {
  const savedConfig = localStorage.getItem('sidebarConfig')
  if (savedConfig) {
    try {
      sidebarConfig.value = JSON.parse(savedConfig)
    } catch (e) {
      console.error('解析侧边栏配置失败:', e)
      sidebarConfig.value = { position: 'LEFT', mode: 'FIXED' }
    }
  }
}

// 保存侧边栏配置到 localStorage
const saveSidebarConfig = () => {
  localStorage.setItem('sidebarConfig', JSON.stringify(sidebarConfig.value))
}

// 监听配置变化不再自动保存（改为通过 API 保存）
// watch(sidebarConfig, () => {
//   saveSidebarConfig()
// }, { deep: true })

// 切换侧边栏折叠状态（根据模式有不同行为）
const toggleCollapse = () => {
  if (sidebarConfig.value.mode === 'COLLAPSIBLE') {
    // COLLAPSIBLE 模式：切换宽度（240px ↔ 64px）
    isCollapse.value = !isCollapse.value
    localStorage.setItem('sidebarCollapsed', String(isCollapse.value))
  } else {
    // FIXED 模式：完全隐藏/显示侧边栏
    isSidebarHidden.value = !isSidebarHidden.value
    localStorage.setItem('sidebarHidden', String(isSidebarHidden.value))
  }
}

// 更新侧边栏位置（调用后端 API 保存）
const updateSidebarPosition = async (position) => {
  try {
    const token = localStorage.getItem('accessToken')
    const response = await fetch('/api/system/config/sidebar/position', {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ position })
    })
    const result = await response.json()
    if (result.success) {
      sidebarConfig.value.position = position
      localStorage.setItem('sidebarConfig', JSON.stringify(sidebarConfig.value))
      ElMessage.success(`侧边栏已切换到${position === 'LEFT' ? '左侧' : '右侧'}`)
    } else {
      ElMessage.error(result.message || '保存配置失败')
    }
  } catch (error) {
    console.error('[AdminLayout] 更新侧边栏位置失败:', error)
    ElMessage.error('保存配置失败：网络错误')
  }
}

// 更新侧边栏模式（调用后端 API 保存）
const updateSidebarMode = async (mode) => {
  try {
    const token = localStorage.getItem('accessToken')
    const response = await fetch('/api/system/config/sidebar/mode', {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ mode })
    })
    const result = await response.json()
    if (result.success) {
      sidebarConfig.value.mode = mode
      localStorage.setItem('sidebarConfig', JSON.stringify(sidebarConfig.value))
      if (mode === 'FIXED') {
        isCollapse.value = false
        localStorage.removeItem('sidebarCollapsed')
      }
      ElMessage.success(`侧边栏模式已切换为${mode === 'FIXED' ? '固定显示' : '可折叠'}`)
    } else {
      ElMessage.error(result.message || '保存配置失败')
    }
  } catch (error) {
    console.error('[AdminLayout] 更新侧边栏模式失败:', error)
    ElMessage.error('保存配置失败：网络错误')
  }
}

// 暴露方法供外部调用（用于系统设置页面）
const setSidebarConfig = async (config) => {
  if (config.position) {
    await updateSidebarPosition(config.position)
  }
  if (config.mode) {
    await updateSidebarMode(config.mode)
  }
}

// 定义暴露的事件
const emit = defineEmits(['config-change'])

// 暴露方法给父组件
defineExpose({
  setSidebarConfig,
  updateSidebarPosition,
  updateSidebarMode,
  getSidebarConfig: () => sidebarConfig.value
})

// 处理菜单选择
const handleMenuSelect = (menuPath) => {
  // SidebarMenu 已经处理了路由跳转，这里可以添加额外逻辑
}

// 菜单分组定义（用于下拉菜单显示顺序）
const menuGroupConfig = [
  {
    key: 'management',
    divided: true,
    codes: ['user_management', 'role_management', 'workarea_management', 'company_management']
  },
  {
    key: 'system',
    divided: true,
    codes: ['system_config', 'menu_management', 'base_permission', 'user_permission', 'user_permission_detail', 'role_permission', 'role_default_permission', 'system_audit', 'permission_audit_log', 'system_docs']
  }
]

// 从 userStore 获取用户菜单列表
const userMenus = computed(() => {
  const menus = userStore.menus || []
  // 转换为统一格式
  return menus.map(m => {
    if (typeof m === 'string') {
      return { menuCode: m, menu_code: m }
    }
    return {
      menuCode: m.menuCode || m.menu_code,
      menu_code: m.menuCode || m.menu_code,
      menu_name: m.menuName || m.menu_name,
      menu_path: m.menuPath || m.menu_path
    }
  })
})

// 获取菜单详细信息（从 localStorage 中获取）
const menuDetails = ref([])

// 加载菜单详细信息
const loadMenuDetails = async () => {
  try {
    const token = localStorage.getItem('accessToken')
    const response = await fetch('/api/menu/all', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    const result = await response.json()
    if (result.success && result.data) {
      menuDetails.value = result.data
    }
  } catch (error) {
    console.error('加载菜单详情失败:', error)
  }
}

// 检查用户是否有某个菜单权限
const hasMenuPermission = (menuCode) => {
  return userMenus.value.some(m => m.menu_code === menuCode || m.menuCode === menuCode)
}

// 获取菜单路径
const getMenuPath = (menuCode) => {
  // 先从详细信息中查找
  const detail = menuDetails.value.find(m => m.menu_code === menuCode)
  if (detail && detail.menu_path) {
    return detail.menu_path
  }
  
  // 使用默认路径映射
  const defaultPaths = {
    'profile': '/user/profile',
    'user_management': '/user/management',
    'role_management': '/role',
    'workarea_management': '/workarea',
    'company_management': '/company',
    'system_config': '/system/config',
    'menu_management': '/system/menu',
    'base_permission': '/system/base-permission',
    'user_permission': '/system/user-permission',
    'user_permission_detail': '/system/user-permission-detail',
    'role_permission': '/system/role-permission',
    'role_default_permission': '/system/role-default',
    'system_audit': '/system/audit-log',
    'permission_audit_log': '/system/permission-audit-log',
    'system_docs': '/system/docs'
  }
  
  return defaultPaths[menuCode] || '/'
}

// 构建动态菜单分组
const menuGroups = computed(() => {
  return menuGroupConfig.map(group => {
    // 过滤出用户有权限的菜单
    const items = group.codes
      .filter(code => hasMenuPermission(code))
      .map(code => {
        // 从详细信息中获取菜单名称
        const detail = menuDetails.value.find(m => m.menu_code === code)
        return {
          menu_code: code,
          menu_name: detail?.menu_name || getDefaultMenuName(code)
        }
      })
    
    return {
      key: group.key,
      divided: group.divided && items.length > 0,
      items
    }
  }).filter(group => group.items.length > 0)
})

// 默认菜单名称映射
const getDefaultMenuName = (menuCode) => {
  const defaultNames = {
    'user_management': '用户管理',
    'role_management': '角色管理',
    'workarea_management': '作业区管理',
    'company_management': '公司管理',
    'system_config': '系统配置',
    'menu_management': '菜单管理',
    'base_permission': '基本权限配置',
    'user_permission': '用户权限配置',
    'user_permission_detail': '用户权限查看',
    'role_permission': '角色权限配置',
    'role_default_permission': '角色缺省权限',
    'system_audit': '审计日志',
    'permission_audit_log': '权限审计日志',
    'system_docs': '文档中心'
  }
  return defaultNames[menuCode] || menuCode
}

// 显示角色
const displayRoles = computed(() => {
  const roles = userStore.roles || []
  if (roles.length === 0) return ''
  return `· ${roles[0]}`
})

// 菜单 key，用于强制重新渲染
const menuKey = computed(() => {
  const userInfo = userStore.userInfo
  const roles = userInfo?.roles?.join(',') || 'none'
  const companyTypeId = userInfo?.companyTypeId || 'none'
  const menuCount = userMenus.value.length
  return `menu-${roles}-${companyTypeId}-${menuCount}-${menuDetails.value.length}`
})

// 返回首页
const goHome = () => {
  router.push('/')
}

// 处理下拉菜单命令
const handleCommand = async (command) => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('userInfo')
      ElMessage.success('已退出登录')
      router.push('/login')
    } catch {
      // 取消退出
    }
  } else if (command === 'profile') {
    router.push('/user/profile')
  } else {
    // 动态菜单跳转
    const path = getMenuPath(command)
    router.push(path)
  }
}

// 组件挂载时加载菜单详情和侧边栏配置
onMounted(() => {
  loadMenuDetails()
  loadSidebarConfig()
})
</script>

<style lang="scss">
.admin-layout {
  height: 100vh;
  display: flex !important;
  flex-direction: row !important;
  overflow: hidden;
  
  // 侧边栏在右侧时的布局（只反转外层 sidebar 和 main-wrapper 的顺序）
  &.sidebar-right {
    flex-direction: row-reverse !important;
    
    // 确保 main-wrapper 内部仍保持垂直布局
    .main-wrapper {
      flex-direction: column !important;
    }
  }
  
  // main-wrapper 内部始终保持垂直布局（header -> main -> footer）
  .main-wrapper {
    display: flex !important;
    flex-direction: column !important;
    flex: 1;
    overflow: hidden;
    min-width: 0; // 防止 flex 子元素溢出
  }
  
  .sidebar {
    flex-shrink: 0;
    background-color: #304156;
    overflow: hidden;
  }
  
  .header {
    flex-shrink: 0;
    height: 60px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    padding: 0 24px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    position: sticky;
    top: 0;
    z-index: 100;
    
    .header-left {
      display: flex;
      align-items: center;
      gap: 12px;
      color: #fff;
      
      .app-title {
        font-size: 18px;
        font-weight: 600;
      }
    }
    
    .header-right {
      display: flex;
      align-items: center;
      gap: 16px;
      
      .el-button {
        color: #fff;
        border-color: transparent;
        background: rgba(255, 255, 255, 0.2);
        
        &:hover {
          background: rgba(255, 255, 255, 0.3);
        }
      }
      
      .user-dropdown {
        margin-left: 8px;
      }
      
      .user-info {
        display: flex;
        align-items: center;
        gap: 8px;
        cursor: pointer;
        color: #fff;
        
        .username {
          font-size: 14px;
        }
        
        .user-roles {
          font-size: 12px;
          opacity: 0.8;
        }
      }
    }
  }
  
  .main-content {
    flex: 1;
    min-height: 0;
    background: #f5f7fa;
    padding: 24px;
    overflow-y: auto;
  }
  
  .footer {
    flex-shrink: 0;
    height: 48px;
    background: #fff;
    border-top: 1px solid #e4e7ed;
    padding: 12px 24px;
    text-align: center;
    color: #909399;
    font-size: 13px;
    position: sticky;
    bottom: 0;
    
    p {
      margin: 0;
    }
  }
}
</style>