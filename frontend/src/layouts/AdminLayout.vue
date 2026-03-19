<template>
  <div class="admin-layout">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <div class="header-left">
        <el-icon :size="24"><VideoPlay /></el-icon>
        <span class="app-title">视频监控点位施工项目管理系统</span>
      </div>
      <div class="header-right">
        <!-- 返回首页按钮 -->
        <el-button text @click="goHome" title="返回首页">
          <el-icon><HomeFilled /></el-icon>
        </el-button>
        
        <!-- 用户信息下拉菜单 -->
        <el-dropdown @command="handleCommand" class="user-dropdown">
          <span class="user-info">
            <el-avatar :size="32" icon="User" />
            <span class="username">{{ userStore.realName || userStore.username }}</span>
            <span class="user-roles">{{ displayRoles }}</span>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item v-if="canManageUser" command="user" divided>用户管理</el-dropdown-item>
              <el-dropdown-item v-if="isSystemAdmin" command="role">角色管理</el-dropdown-item>
              <el-dropdown-item v-if="isSystemAdmin" command="workarea">作业区管理</el-dropdown-item>
              <el-dropdown-item v-if="isSystemAdmin" command="company">公司管理</el-dropdown-item>
              <el-dropdown-item v-if="isSystemAdmin" command="system" divided>系统管理</el-dropdown-item>
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
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessageBox, ElMessage } from 'element-plus'
import { VideoPlay, ArrowDown, HomeFilled } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

// 显示角色
const displayRoles = computed(() => {
  const roles = userStore.roles || []
  if (roles.length === 0) return ''
  return `· ${roles[0]}`
})

// 判断是否为系统管理员
const isSystemAdmin = computed(() => userStore.companyTypeId === 4)

// 判断是否有用户管理权限
const canManageUser = computed(() => userStore.isLoggedIn)

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
  } else if (command === 'user') {
    router.push('/user/management')
  } else if (command === 'role') {
    router.push('/role')
  } else if (command === 'workarea') {
    router.push('/workarea')
  } else if (command === 'company') {
    router.push('/company')
  } else if (command === 'system') {
    router.push('/system/config')
  }
}
</script>

<style scoped lang="scss">
.admin-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 0 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  
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
  background: #f5f7fa;
  padding: 24px;
}

.footer {
  background: #fff;
  border-top: 1px solid #e4e7ed;
  padding: 12px 24px;
  text-align: center;
  color: #909399;
  font-size: 13px;
  
  p {
    margin: 0;
  }
}
</style>
