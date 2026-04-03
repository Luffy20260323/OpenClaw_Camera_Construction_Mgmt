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
        <!-- 一级菜单有子菜单 -->
        <el-sub-menu v-if="menu.children && menu.children.length > 0" :index="menu.id.toString()">
          <template #title>
            <el-icon v-if="menu.icon">
              <component :is="menu.icon" />
            </el-icon>
            <span>{{ menu.menuName || menu.menu_name }}</span>
          </template>
          
          <!-- 二级菜单 -->
          <template v-for="child in menu.children" :key="child.id">
            <!-- 二级菜单有子菜单（三级） -->
            <el-sub-menu v-if="child.children && child.children.length > 0" :index="child.id.toString()">
              <template #title>
                <el-icon v-if="child.icon">
                  <component :is="child.icon" />
                </el-icon>
                <span>{{ child.menuName || child.menu_name }}</span>
              </template>
              
              <!-- 三级菜单 -->
              <el-menu-item
                v-for="grandChild in child.children"
                :key="grandChild.id"
                :index="grandChild.menuPath || grandChild.menu_path"
              >
                <el-icon v-if="grandChild.icon">
                  <component :is="grandChild.icon" />
                </el-icon>
                <template #title>{{ grandChild.menuName || grandChild.menu_name }}</template>
              </el-menu-item>
            </el-sub-menu>
            
            <!-- 二级菜单无子菜单 -->
            <el-menu-item v-else :index="child.menuPath || child.menu_path">
              <el-icon v-if="child.icon">
                <component :is="child.icon" />
              </el-icon>
              <template #title>{{ child.menuName || child.menu_name }}</template>
            </el-menu-item>
          </template>
        </el-sub-menu>
        
        <!-- 一级菜单无子菜单 -->
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
import { computed, ref, watch } from 'vue'
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

// 用户菜单列表（只读引用，避免不必要的重新计算）
const userMenus = ref(userStore.menus || [])

// 监听 store 变化，只在真正变化时更新
watch(
  () => userStore.menus,
  (newMenus) => {
    if (newMenus !== userMenus.value) {
      userMenus.value = newMenus || []
    }
  },
  { deep: false }
)

// 检查用户是否有菜单权限
const hasMenuPermission = (menu) => {
  const permissions = userStore.permissions || []
  
  // 系统管理员拥有所有权限
  if (userStore.roles?.includes('system_admin') || permissions.includes('*:*:*')) {
    return true
  }
  
  // 兼容后端返回的数据格式（requiredPermission 或 required_permission）
  const requiredPerm = menu.requiredPermission || menu.required_permission
  
  if (requiredPerm) {
    const requiredPerms = requiredPerm.split(',').map(p => p.trim())
    return requiredPerms.some(perm => permissions.includes(perm))
  }
  
  // 默认显示
  return true
}

// 辅助函数：获取菜单的 parentId（兼容驼峰和下划线）
const getParentId = (menu) => {
  return menu.parentId !== undefined ? menu.parentId : menu.parent_id
}

// 辅助函数：获取菜单的 sortOrder（兼容驼峰和下划线）
const getSortOrder = (menu) => {
  return menu.sortOrder !== undefined ? menu.sortOrder : menu.sort_order
}

// 递归构建菜单树（支持多级菜单）
const buildMenuTree = (menus, parentId = null) => {
  const result = []
  
  // 找到当前层级的菜单
  const currentLevelMenus = menus.filter(m => {
    const pid = getParentId(m)
    // 处理 null 和 undefined 的情况
    if (parentId === null || parentId === undefined) {
      return pid === null || pid === undefined
    }
    return pid === parentId
  })
  
  // 按 sortOrder 排序
  currentLevelMenus.sort((a, b) => getSortOrder(a) - getSortOrder(b))
  
  // 为每个菜单递归构建子菜单
  currentLevelMenus.forEach(menu => {
    const children = buildMenuTree(menus, menu.id)
    if (children.length > 0) {
      menu.children = children
    }
    result.push(menu)
  })
  
  return result
}

// 将扁平菜单转换为树形结构（支持多级）- 使用缓存避免重复计算
const menuTree = ref([])
const menuCacheKey = ref('')

// 监听菜单数据变化，只在真正变化时重新计算
watch(
  () => userMenus.value,
  (newMenus) => {
    // 生成缓存 key（只依赖 id 和 code，避免深度比较）
    const newCacheKey = newMenus.map(m => `${m.id}:${m.menuCode}`).join('|')
    
    // 只在菜单真正变化时重新计算
    if (newCacheKey !== menuCacheKey.value) {
      menuCacheKey.value = newCacheKey
      
      const visibleMenus = newMenus.filter(menu => hasMenuPermission(menu))
      menuTree.value = buildMenuTree(visibleMenus, null)
    }
  },
  { immediate: true, deep: false }
)

// 处理菜单选择
const handleMenuSelect = (index, indexPath) => {
  // index 是菜单路径
  // 只有当菜单没有子菜单时才跳转
  if (index && index.startsWith('/')) {
    // 查找当前菜单是否有子菜单
    const currentMenu = userMenus.value.find(m => {
      const menuPath = m.menuPath || m.menu_path
      return menuPath === index
    })
    
    // 检查是否有子菜单
    const hasChildren = userMenus.value.some(m => {
      const parentId = getParentId(m)
      return parentId === currentMenu?.id
    })
    
    // 如果没有子菜单，才跳转
    if (!hasChildren) {
      router.push(index)
      emit('menu-select', index)
    }
    // 如果有子菜单，el-sub-menu 会自动处理展开/收起
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
