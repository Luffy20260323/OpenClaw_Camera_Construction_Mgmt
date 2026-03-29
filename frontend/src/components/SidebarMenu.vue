<template>
  <el-aside :width="isCollapse ? '64px' : '240px'" class="sidebar">
    <div class="logo">
      <el-icon :size="32"><VideoPlay /></el-icon>
      <span v-show="!isCollapse" class="logo-text">施工管理系统</span>
    </div>
    
    <el-menu
      :default-active="activeMenu"
      :collapse="isCollapse"
      :unique-opened="true"
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#409EFF"
      class="sidebar-menu"
      @select="handleMenuSelect"
    >
      <template v-for="menu in menuTree" :key="menu.id">
        <!-- 有子菜单 -->
        <el-sub-menu v-if="menu.children && menu.children.length > 0" :index="menu.id.toString()">
          <template #title>
            <el-icon v-if="menu.icon">
              <component :is="menu.icon" />
            </el-icon>
            <span>{{ menu.menuName || menu.menu_name }}</span>
          </template>
          <el-menu-item
            v-for="child in menu.children"
            :key="child.id"
            :index="child.menuPath || child.menu_path"
          >
            <el-icon v-if="child.icon">
              <component :is="child.icon" />
            </el-icon>
            <template #title>{{ child.menuName || child.menu_name }}</template>
          </el-menu-item>
        </el-sub-menu>
        
        <!-- 无子菜单 -->
        <el-menu-item v-else :index="menu.menuPath || menu.menu_path">
          <el-icon v-if="menu.icon">
            <component :is="menu.icon" />
          </el-icon>
          <template #title>{{ menu.menuName || menu.menu_name }}</template>
        </el-menu-item>
      </template>
    </el-menu>
  </el-aside>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { VideoPlay } from '@element-plus/icons-vue'

const props = defineProps({
  isCollapse: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['menu-select'])

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 当前激活的菜单
const activeMenu = computed(() => {
  return route.path
})

// 用户菜单列表
const userMenus = computed(() => {
  const menus = userStore.menus || []
  console.log('[SidebarMenu] 用户菜单数据:', JSON.stringify(menus, null, 2))
  return menus
})

// 检查用户是否有菜单权限
const hasMenuPermission = (menu) => {
  const permissions = userStore.permissions || []
  
  // 系统管理员拥有所有权限
  if (userStore.roles?.includes('system_admin') || permissions.includes('*:*:*')) {
    return true
  }
  
  // 如果菜单有 required_permission 字段，检查权限
  if (menu.required_permission) {
    const requiredPerms = menu.required_permission.split(',').map(p => p.trim())
    return requiredPerms.some(perm => permissions.includes(perm))
  }
  
  // 默认显示
  return true
}

// 将扁平菜单转换为树形结构
const menuTree = computed(() => {
  const menus = [...userMenus.value]
  console.log('[SidebarMenu] 菜单数量:', menus.length)
  
  // 先过滤有权限的菜单
  const visibleMenus = menus.filter(menu => hasMenuPermission(menu))
  console.log('[SidebarMenu] 过滤后可见菜单数量:', visibleMenus.length)
  
  // 兼容后端返回的数据格式（parentId 或 parent_id）
  const parentMenus = visibleMenus.filter(m => !m.parentId && !m.parent_id)
  const childMenus = visibleMenus.filter(m => m.parentId || m.parent_id)
  
  console.log('[SidebarMenu] 父菜单:', parentMenus.map(m => m.menuCode))
  console.log('[SidebarMenu] 子菜单:', childMenus.map(m => ({ code: m.menuCode, parentId: m.parentId || m.parent_id })))
  
  // 为父菜单添加子菜单
  parentMenus.forEach(parent => {
    const parentId = parent.id
    parent.children = childMenus.filter(child => {
      const childParentId = child.parentId || child.parent_id
      return childParentId === parentId
    })
    // 按 sortOrder 排序子菜单
    parent.children.sort((a, b) => (a.sortOrder || a.sort_order || 0) - (b.sortOrder || b.sort_order || 0))
    console.log('[SidebarMenu] 父菜单', parent.menuCode, '的子菜单:', parent.children.map(c => c.menuCode))
  })
  
  // 按 sortOrder 排序父菜单
  parentMenus.sort((a, b) => (a.sortOrder || a.sort_order || 0) - (b.sortOrder || b.sort_order || 0))
  
  return parentMenus
})

// 处理菜单选择
const handleMenuSelect = (index, indexPath) => {
  // index 是菜单路径
  if (index && index.startsWith('/')) {
    router.push(index)
    emit('menu-select', index)
  }
}
</script>

<style scoped lang="scss">
.sidebar {
  background-color: #304156;
  transition: width 0.3s;
  overflow-x: hidden;
  
  .logo {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 60px;
    background-color: #2b3a4b;
    color: #fff;
    gap: 10px;
    
    .logo-text {
      font-size: 16px;
      font-weight: 600;
      white-space: nowrap;
    }
  }
  
  .sidebar-menu {
    border-right: none;
    
    :deep(.el-menu-item) {
      &:hover {
        background-color: #263445 !important;
      }
      
      &.is-active {
        background-color: #409EFF !important;
        color: #fff !important;
      }
    }
    
    :deep(.el-sub-menu__title) {
      &:hover {
        background-color: #263445 !important;
      }
    }
  }
}
</style>
