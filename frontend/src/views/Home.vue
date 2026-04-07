<template>
    <div class="home-container">
      <!-- 欢迎卡片 -->
      <el-card class="welcome-card">
        <template #header>
          <div class="card-header">
            <span>欢迎使用</span>
          </div>
        </template>
        
        <div class="welcome-content">
          <h2>👋 欢迎，{{ userStore.realName || userStore.username }}！</h2>
          <p class="welcome-text">视频监控点位施工项目管理系统 - 项目交付过程管理和最终结算管理平台</p>
          
          <!-- 统计卡片 -->
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
          
          <!-- 快捷操作 -->
          <div class="quick-actions">
            <h3>快捷操作</h3>
            <el-space wrap>
              <PermButton v-if="hasMenuPermission('user_management')" permission="user:create" type="primary" @click="goTo('/user/management')">
                <el-icon><User /></el-icon> 用户管理
              </PermButton>
              <PermButton v-if="hasMenuPermission('workarea_management')" permission="workarea:list" type="success" @click="goTo('/workarea')">
                <el-icon><Location /></el-icon> 作业区管理
              </PermButton>
              <PermButton v-if="hasMenuPermission('company_management')" permission="company:list" type="warning" @click="goTo('/company')">
                <el-icon><Box /></el-icon> 公司管理
              </PermButton>
              <PermButton v-if="hasMenuPermission('system_config')" permission="system:config:view" type="info" @click="goTo('/system/config')">
                <el-icon><Setting /></el-icon> 系统配置
              </PermButton>
              <PermButton v-if="hasMenuPermission('user_permission')" permission="menu:user:view" type="primary" @click="goTo('/system/user-permission')">
                <el-icon><Setting /></el-icon> 用户权限
              </PermButton>
              <PermButton v-if="hasMenuPermission('role_permission')" permission="menu:user:edit" type="danger" @click="goTo('/system/role-permission')">
                <el-icon><Setting /></el-icon> 角色权限
              </PermButton>
            </el-space>
          </div>
          
          <!-- 用户信息 -->
          <div class="user-info-section">
            <h3>用户信息</h3>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="用户名">{{ userStore.username }}</el-descriptions-item>
              <el-descriptions-item label="姓名">{{ userStore.realName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="邮箱">{{ userStore.userInfo?.email || '-' }}</el-descriptions-item>
              <el-descriptions-item label="手机号">{{ userStore.userInfo?.phone || '-' }}</el-descriptions-item>
              <el-descriptions-item label="角色" :span="2">
                <el-tag v-for="role in userStore.roles" :key="role" size="small" style="margin-right: 8px;">
                  {{ role }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </div>
      </el-card>
    </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { User, Location, Box, Setting } from '@element-plus/icons-vue'
import PermButton from '@/components/PermButton.vue'

const router = useRouter()
const userStore = useUserStore()

// 检查是否有菜单权限
const hasMenuPermission = (menuCode) => {
  const menus = userStore.menus || []
  return menus.some(m => {
    const code = typeof m === 'string' ? m : (m?.menuCode || '')
    return code === menuCode
  })
}

// 导航到指定页面
const goTo = (path) => {
  router.push(path)
}
</script>

<style scoped lang="scss">
.home-container {
  max-width: 1200px;
  margin: 0 auto;
  
  .welcome-card {
    margin-bottom: 20px;
    
    .card-header {
      font-size: 16px;
      font-weight: 600;
    }
    
    .welcome-content {
      h2 {
        margin: 0 0 10px 0;
        color: #303133;
      }
      
      .welcome-text {
        color: #909399;
        margin-bottom: 30px;
      }
      
      .stats-row {
        margin-bottom: 30px;
        
        .el-statistic {
          text-align: center;
        }
      }
      
      .quick-actions {
        margin-bottom: 30px;
        
        h3 {
          margin: 0 0 15px 0;
          color: #303133;
          font-size: 16px;
        }
        
        .el-button {
          min-width: 120px;
        }
      }
      
      .user-info-section {
        h3 {
          margin: 0 0 15px 0;
          color: #303133;
          font-size: 16px;
        }
      }
    }
  }
}
</style>
