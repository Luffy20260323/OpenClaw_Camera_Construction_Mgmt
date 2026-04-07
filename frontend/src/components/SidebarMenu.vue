<template>
  <div class="sidebar-content">
    <div class="logo" @click="toggleCollapse" :title="collapseTitle">
      <el-icon :size="32" class="logo-icon">
        <!-- 书本打开图标(展开状态) -->
        <svg v-if="!isCollapse" viewBox="0 0 24 24" fill="currentColor" width="32" height="32">
          <path d="M21 5c-1.11-.35-2.33-.5-3.5-.5-1.83 0-2.5.5-2.5.5v14c0 0 0.67-.5 2.5-.5 1.17 0 2.39.15 3.5.5V5z"/>
          <path d="M3 5c1.11-.35 2.33-.5 3.5-.5 1.83 0 2.5.5 2.5.5v14c0 0-0.67-.5-2.5-.5-1.17 0-2.39.15-3.5.5V5z" opacity="0.8"/>
          <path d="M12 6c-1.5 0-3 .5-3 .5v11c0 0 1.5-.5 3-.5s3 .5 3 .5v-11c0 0-1.5.5-3 .5z"/>
        </svg>
        <!-- 书本合拢图标(折叠状态) -->
        <svg v-else viewBox="0 0 24 24" fill="currentColor" width="32" height="32">
          <path d="M6 4h12v16H6z" opacity="0.3"/>
          <path d="M4 6h2v14H4z"/>
          <path d="M18 6h2v14h-2z"/>
          <path d="M8 6h8v1H8z"/>
          <path d="M8 8h8v1H8z"/>
          <path d="M8 10h8v1H8z"/>
        </svg>
      </el-icon>
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
            <!-- 二级菜单有子菜单(三级) -->
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
                :index="getMenuRoute(grandChild)"
                @click="handleMenuItemClick(grandChild)"
              >
                <el-icon v-if="grandChild.icon">
                  <component :is="grandChild.icon" />
                </el-icon>
                <template #title>{{ grandChild.menuName || grandChild.menu_name }}</template>
              </el-menu-item>
            </el-sub-menu>

            <!-- 二级菜单无子菜单 -->
            <el-menu-item
              v-else
              :index="getMenuRoute(child)"
              @click="handleMenuItemClick(child)"
            >
              <el-icon v-if="child.icon">
                <component :is="child.icon" />
              </el-icon>
              <template #title>{{ child.menuName || child.menu_name }}</template>
            </el-menu-item>
          </template>
        </el-sub-menu>

        <!-- 一级菜单无子菜单 -->
        <el-menu-item
          v-else
          :index="getMenuRoute(menu)"
          @click="handleMenuItemClick(menu)"
        >
          <el-icon v-if="menu.icon">
            <component :is="menu.icon" />
          </el-icon>
          <template #title>{{ menu.menuName || menu.menu_name }}</template>
        </el-menu-item>
      </template>
    </el-menu>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useResourceStore } from '@/stores/resource'
import { MENU_CODE_MAP } from '@/router/index.js'

const props = defineProps({
  isCollapse: {
    type: Boolean,
    default: false
  }
})

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const resourceStore = useResourceStore()

const emit = defineEmits(['menu-select', 'collapse-toggle'])

import { ROLE_SUPER_ADMIN } from '@/constants/roles'

// 刷新菜单状态
const isRefreshing = ref(false)

// 折叠按钮提示文字（显示刷新功能）
const collapseTitle = computed(() => {
  if (isRefreshing.value) {
    return '正在刷新菜单...'
  }
  return props.isCollapse ? '展开侧边栏（点击刷新菜单）' : '折叠侧边栏（点击刷新菜单）'
})

// 点击 logo 触发折叠 + 刷新菜单
const toggleCollapse = async () => {
  // 先触发折叠
  emit('collapse-toggle')
  
  // 然后刷新菜单
  if (isRefreshing.value) return
  
  isRefreshing.value = true
  try {
    // 刷新资源缓存和用户菜单
    await resourceStore.refreshResourceCache()
    await userStore.refreshMenus()
    
    console.log('[SidebarMenu] 菜单已刷新')
  } catch (error) {
    console.error('[SidebarMenu] 刷新菜单失败:', error)
  } finally {
    isRefreshing.value = false
  }
}

// 当前激活的菜单
const activeMenu = computed(() => {
  return route.path
})

// 获取用户菜单数据
const userMenus = computed(() => {
  return userStore.userInfo?.menuTree || []
})

// 直接使用后端返回的菜单树，只需过滤和递归处理权限
const menuTree = computed(() => {
  const menus = userMenus.value
  
  // 空数据时返回空数组
  if (!menus || menus.length === 0) {
    return []
  }
  
  // 过滤掉首页(首页不需要在侧边栏显示)
  // 同时保留原有的 children 结构
  const processMenus = (menuList) => {
    return menuList
      .filter(menu => {
        const menuCode = menu.menuCode || menu.menu_code
        if (menuCode === 'home') {
          return false
        }
        return hasMenuPermission(menu)
      })
      .map(menu => {
        // 如果有子菜单，递归处理
        if (menu.children && menu.children.length > 0) {
          return {
            ...menu,
            children: processMenus(menu.children)
          }
        }
        return menu
      })
      .filter(menu => {
        // 过滤掉没有子菜单且没有权限访问的 MODULE（MODULE 只是容器）
        const menuType = menu.type || 'MENU'
        if (menuType === 'MODULE' && (!menu.children || menu.children.length === 0)) {
          return false
        }
        return true
      })
  }
  
  return processMenus(menus)
})

// 检查用户是否有菜单权限
const hasMenuPermission = (menu) => {
  const permissions = userStore.permissions || []

  // 超级管理员拥有所有权限(使用常量)
  if (userStore.roles?.includes(ROLE_SUPER_ADMIN) || permissions.includes('*:*:*')) {
    return true
  }

  // 兼容后端返回的数据格式(requiredPermission 或 required_permission)
  const requiredPerm = menu.requiredPermission || menu.required_permission

  if (requiredPerm) {
    const requiredPerms = requiredPerm.split(',').map(p => p.trim())
    return requiredPerms.some(perm => permissions.includes(perm))
  }

  // 默认显示
  return true
}

// 辅助函数:获取菜单的 parentId(兼容驼峰和下划线)
const getParentId = (menu) => {
  return menu.parentId !== undefined ? menu.parentId : menu.parent_id
}

// 辅助函数:获取菜单的 sortOrder(兼容驼峰和下划线)
const getSortOrder = (menu) => {
  return menu.sortOrder !== undefined ? menu.sortOrder : menu.sort_order
}

// 递归构建菜单树(支持多级菜单)
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

// 获取菜单路由路径(通过 MENU 的 code 查找对应的 PAGE 资源的 path)
const getMenuRoute = (menu) => {
  const menuCode = menu.menuCode || menu.menu_code

  // 在用户菜单列表中查找该 MENU 下的 PAGE 资源
  const page = userMenus.value.find(m => {
    const parentId = getParentId(m)
    const type = m.type || 'MENU'  // 如果没有 type 字段,默认是 MENU
    return parentId === menu.id && type === 'PAGE'
  })

  // 如果找到 PAGE 资源,使用 PAGE 的 path
  if (page && page.path) {
    return page.path
  }

  // 降级方案:使用 code 映射
  // 在 MENU_CODE_MAP 中查找对应的路径(反向查找)
  for (const [path, code] of Object.entries(MENU_CODE_MAP)) {
    if (code === menuCode) {
      return path
    }
  }

  // 最后降级:返回唯一标识符（菜单ID），避免多个菜单使用相同 index
  // 使用 'menu_' + id 格式，与路由路径区分
  return 'menu_' + menu.id
}

// 处理菜单项点击
const handleMenuItemClick = (menu) => {
  const route = getMenuRoute(menu)
  // 跳过无效路由（以 'menu_' 开头的表示没有配置页面）
  if (route && route.startsWith('/')) {
    router.push(route)
    emit('menu-select', route)
  }
}

// 处理菜单选择(兼容 el-menu 的 select 事件)
const handleMenuSelect = (index, indexPath) => {
  // index 是菜单路由路径（以 '/' 开头）
  if (index && index.startsWith('/')) {
    router.push(index)
    emit('menu-select', index)
  }
  // 以 'menu_' 开头的表示没有配置页面，不执行跳转
  // 如果有子菜单,el-sub-menu 会自动处理展开/收起
}
</script>

<style scoped lang="scss">
.sidebar-content {
  background-color: #304156;
  height: 100%;
  overflow-x: hidden;

  .logo {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 60px;
    background-color: #2b3a4b;
    color: #fff;
    gap: 10px;
    cursor: pointer;
    user-select: none;
    transition: background-color 0.2s;

    &:hover {
      background-color: #3a4a5b;
    }

    .logo-icon {
      display: flex;
      align-items: center;
      justify-content: center;
      transition: transform 0.2s;
    }

    &:hover .logo-icon {
      transform: scale(1.05);
    }

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
