<template>
  <el-container class="admin-layout" :class="{ 'sidebar-right': sidebarConfig.position === 'RIGHT' }">
    <!-- 侧边栏（支持左右位置） -->
    <el-aside 
      v-show="isSidebarVisible"
      :style="sidebarStyle"
      class="sidebar"
    >
      <SidebarMenu :is-collapse="isCollapse" @menu-select="handleMenuSelect" />
    </el-aside>

    <el-container class="main-wrapper">
      <!-- 顶部导航栏 -->
      <el-header class="header">
        <div class="header-left">
          <!-- 折叠/展开按钮（仅在可折叠模式下显示） -->
          <el-button 
            v-if="isCollapsible"
            text 
            @click="toggleCollapse" 
            class="collapse-btn"
            :title="isCollapse ? '展开侧边栏' : '折叠侧边栏'"
          >
            <el-icon>
              <component :is="isCollapse ? 'Expand' : 'Fold'" />
            </el-icon>
          </el-button>
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
        <slot></slot>
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

// 侧边栏折叠状态
const isCollapse = ref(false)

// 计算侧边栏是否可折叠
const isCollapsible = computed(() => {
  return sidebarConfig.value.mode === 'COLLAPSIBLE'
})

// 计算侧边栏实际显示状态
const isSidebarVisible = computed(() => {
  if (sidebarConfig.value.mode === 'FIXED') {
    return true
  }
  return !isCollapse.value
})

// 计算侧边栏样式
const sidebarStyle = computed(() => {
  const baseStyle = {
    order: sidebarConfig.value.position === 'RIGHT' ? 1 : 0,
    transition: 'width 0.3s, opacity 0.3s'
  }
  if (!isSidebarVisible.value) {
    baseStyle.width = '0px'
    baseStyle.opacity = '0'
    baseStyle.overflow = 'hidden'
  } else {
    baseStyle.width = isCollapse.value ? '64px' : '240px'
    baseStyle.opacity = '1'
  }
  return baseStyle
})

// 加载侧边栏配置（优先从 localStorage 读取）
const loadSidebarConfig = () => {
  const savedConfig = localStorage.getItem('sidebarConfig')
  if (savedConfig) {
    try {
      sidebarConfig.value = JSON.parse(savedConfig)
    } catch (e) {
      console.error('解析侧边栏配置失败:', e)
      sidebarConfig.value = { position: 'LEFT', mode: 'FIXED' }
    }
  }
  
  // 根据配置初始化折叠状态
  if (sidebarConfig.value.mode === 'COLLAPSIBLE') {
    // 可折叠模式下，从 localStorage 读取折叠状态
    const savedCollapse = localStorage.getItem('sidebarCollapsed')
    if (savedCollapse !== null) {
      isCollapse.value = savedCollapse === 'true'
    }
  } else {
    // 固定模式下，不折叠
    isCollapse.value = false
  }
}

// 保存侧边栏配置到 localStorage
const saveSidebarConfig = () => {
  localStorage.setItem('sidebarConfig', JSON.stringify(sidebarConfig.value))
}

// 监听配置变化，保存到 localStorage
watch(sidebarConfig, () => {
  saveSidebarConfig()
}, { deep: true })

// 切换折叠状态
const toggleCollapse = () => {
  if (sidebarConfig.value.mode === 'FIXED') {
    ElMessage.warning('固定模式下不可折叠侧边栏')
    return
  }
  isCollapse.value = !isCollapse.value
  localStorage.setItem('sidebarCollapsed', String(isCollapse.value))
}

// 更新侧边栏位置
const updateSidebarPosition = (position) => {
  sidebarConfig.value.position = position
  ElMessage.success(`侧边栏已切换到${position === 'LEFT' ? '左侧' : '右侧'}`)
}

// 更新侧边栏模式
const updateSidebarMode = (mode) => {
  sidebarConfig.value.mode = mode
  if (mode === 'FIXED') {
    isCollapse.value = false
    localStorage.removeItem('sidebarCollapsed')
  }
  ElMessage.success(`侧边栏模式已切换为${mode === 'FIXED' ? '固定显示' : '可折叠'}`)
}

// 暴露方法供外部调用（用于系统设置页面）
const setSidebarConfig = (config) => {
  if (config.position) {
    updateSidebarPosition(config.position)
  }
  if (config.mode) {
    updateSidebarMode(config.mode)
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
  console.log('[AdminLayout] 菜单选择:', menuPath)
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
    codes: ['system_config', 'menu_management', 'base_permission', 'user_permission', 'user_permission_detail', 'role_permission', 'role_default_permission', 'audit_log', 'permission_audit_log', 'system_docs']
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
    'audit_log': '/system/audit-log',
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
    'audit_log': '审计日志',
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

<style scoped lang="scss">
.admin-layout {
  height: 100vh;
  display: flex;
  flex-direction: row;
  overflow: hidden;
  
  // 侧边栏在右侧时的布局
  &.sidebar-right {
    flex-direction: row-reverse;
    
    .main-wrapper {
      flex-direction: row-reverse;
    }
  }
  
  .main-wrapper {
    display: flex;
    flex-direction: column;
    flex: 1;
    overflow: hidden;
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
      
      .collapse-btn {
        color: #fff;
        font-size: 18px;
        padding: 8px;
        
        &:hover {
          background-color: rgba(255, 255, 255, 0.1);
        }
      }
      
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