<template>
  <div class="home-container">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <div class="header-left">
        <el-icon :size="24"><VideoPlay /></el-icon>
        <span class="app-title">摄像头生命周期管理系统</span>
      </div>
      <div class="header-right">
        <!-- 返回首页按钮（在首页时隐藏） -->
        <el-button text @click="goHome" title="返回首页" style="display: none;">
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
              <el-dropdown-item v-if="isAdmin" command="user" divided>用户管理</el-dropdown-item>
              <el-dropdown-item v-if="isSystemAdmin" command="role">角色管理</el-dropdown-item>
              <el-dropdown-item v-if="isSystemAdmin" command="workarea">作业区管理</el-dropdown-item>
              <el-dropdown-item v-if="isSystemAdmin" command="company">公司管理</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    
    <!-- 主内容区 -->
    <el-main class="main-content">
      <el-card class="welcome-card">
        <template #header>
          <div class="card-header">
            <span>欢迎使用</span>
          </div>
        </template>
        
        <div class="welcome-content">
          <h1>👋 欢迎，{{ userStore.realName || userStore.username }}！</h1>
          <p>摄像头生命周期管理系统 - 项目交付过程管理和最终结算管理平台</p>
          
          <el-row :gutter="20" class="stats-row">
            <el-col :span="6">
              <el-statistic title="我的项目" :value="0" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="进行中" :value="0" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="待审核" :value="0" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="已完成" :value="0" />
            </el-col>
          </el-row>
          
          <div class="quick-actions">
            <h3>快捷操作</h3>
            <el-space wrap>
              <el-button type="primary" icon="Plus">新建项目</el-button>
              <el-button icon="Document">我的任务</el-button>
              <el-button icon="Bell">消息通知</el-button>
            </el-space>
          </div>
          
          <div class="user-info-section">
            <h3>用户信息</h3>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="用户名">{{ userStore.username }}</el-descriptions-item>
              <el-descriptions-item label="姓名">{{ userStore.realName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="邮箱">{{ userStore.userInfo.email || '-' }}</el-descriptions-item>
              <el-descriptions-item label="手机号">{{ userStore.userInfo.phone || '-' }}</el-descriptions-item>
              <el-descriptions-item label="角色" :span="2">
                <el-tag v-for="role in userStore.roles" :key="role" size="small" style="margin-right: 8px;">
                  {{ role }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </div>
      </el-card>
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

// 显示角色（只显示第一个角色）
const displayRoles = computed(() => {
  const roles = userStore.roles || []
  if (roles.length === 0) return ''
  return `· ${roles[0]}`
})

// 判断是否为系统管理员
const isSystemAdmin = computed(() => userStore.companyTypeId === 4)

// 判断是否为公司管理员（甲方管理员、乙方管理员、监理方管理员）
const isCompanyAdmin = computed(() => {
  const roles = userStore.roles || []
  return roles.includes('jiafang_admin') || 
         roles.includes('yifang_admin') || 
         roles.includes('jianli_admin')
})

// 判断是否是管理员（系统管理员或公司管理员）
const isAdmin = computed(() => isSystemAdmin.value || isCompanyAdmin.value)

// 返回首页
const goHome = () => {
  router.push('/home')
}

const handleCommand = async (command) => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      
      await userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/login')
    } catch {
      // 取消退出
    }
  } else if (command === 'profile') {
    router.push('/user/profile')
  } else if (command === 'user') {
    if (!isAdmin.value) {
      ElMessage.warning('您没有权限访问用户管理')
      return
    }
    router.push('/user/management')
  } else if (command === 'role') {
    if (!isSystemAdmin.value) {
      ElMessage.warning('您没有权限访问此功能')
      return
    }
    router.push('/role')
  } else if (command === 'workarea') {
    if (!isSystemAdmin.value) {
      ElMessage.warning('您没有权限访问此功能')
      return
    }
    router.push('/workarea')
  } else if (command === 'company') {
    if (!isSystemAdmin.value) {
      ElMessage.warning('您没有权限访问此功能')
      return
    }
    router.push('/company')
  }
}
</script>

<style scoped lang="scss">
.home-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
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
  padding: 24px;
  
  .welcome-card {
    max-width: 1200px;
    margin: 0 auto;
    
    .card-header {
      font-size: 16px;
      font-weight: 600;
    }
    
    .welcome-content {
      h1 {
        font-size: 28px;
        margin-bottom: 16px;
        color: #333;
      }
      
      > p {
        font-size: 16px;
        color: #666;
        margin-bottom: 32px;
      }
      
      .stats-row {
        margin-bottom: 32px;
        padding: 24px 0;
        background: #f9fafb;
        border-radius: 8px;
      }
      
      .quick-actions {
        margin-bottom: 32px;
        
        h3 {
          font-size: 16px;
          margin-bottom: 16px;
          color: #333;
        }
      }
      
      .user-info-section {
        h3 {
          font-size: 16px;
          margin-bottom: 16px;
          color: #333;
        }
      }
    }
  }
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
