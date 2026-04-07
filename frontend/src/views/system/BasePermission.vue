<template>
    <div class="base-permission-page">
      <div class="page-header">
        <h1>基本权限配置</h1>
        <p class="description">配置所有角色必备的基本权限，这些权限会自动分配给所有用户</p>
      </div>

      <!-- 当前基本权限统计 -->
      <div class="stats-card">
        <div class="stat-item">
          <span class="stat-label">基本权限数量</span>
          <span class="stat-value">{{ basePermissions.length }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">总权限数量</span>
          <span class="stat-value">{{ totalPermissions }}</span>
        </div>
      </div>

      <!-- 权限分组列表 -->
      <div class="permission-groups">
        <div v-for="group in permissionGroups" :key="group.group_code" class="group-section">
          <div class="group-header">
            <h2>{{ group.group_name }}</h2>
            <span class="group-count">
              {{ getGroupBaseCount(group.group_code) }} / {{ getGroupTotalCount(group.group_code) }} 已设为基本权限
            </span>
          </div>

          <div class="permission-list">
            <div v-for="permission in getGroupPermissions(group.group_code)" 
                 :key="permission.id" 
                 class="permission-item"
                 :class="{ 'is-base': permission.is_base }">
              <div class="permission-info">
                <span class="permission-code">{{ permission.permission_code }}</span>
                <span class="permission-name">{{ permission.permission_name }}</span>
                <span v-if="permission.description" class="permission-desc">{{ permission.description }}</span>
              </div>
              <div class="permission-action">
                <el-switch 
                  v-model="permission.is_base"
                  @change="toggleBasePermission(permission)"
                  :disabled="isSystemProtected(permission)"
                />
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 操作说明 -->
      <div class="help-section">
        <h3>说明</h3>
        <ul>
          <li>基本权限是所有用户必备的权限，无法被移除</li>
          <li>系统自动将基本权限分配给所有新创建的用户</li>
          <li>认证相关权限（登录、登出）默认为基本权限且不可修改</li>
          <li>修改基本权限配置后，新用户会自动获得更新的基本权限</li>
        </ul>
      </div>
    </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'

// 数据
const permissions = ref([])
const permissionGroups = ref([])
const loading = ref(false)

// 计算属性
const basePermissions = computed(() => permissions.value.filter(p => p.is_base))
const totalPermissions = computed(() => permissions.value.length)

// 获取权限分组
const getGroupPermissions = (groupCode) => {
  return permissions.value.filter(p => p.group_code === groupCode)
}

const getGroupBaseCount = (groupCode) => {
  return getGroupPermissions(groupCode).filter(p => p.is_base).length
}

const getGroupTotalCount = (groupCode) => {
  return getGroupPermissions(groupCode).length
}

// 判断是否系统保护权限（不可修改）
const isSystemProtected = (permission) => {
  // 登录和登出权限不可修改
  return permission.permission_code === 'auth:login' || permission.permission_code === 'auth:logout'
}

// 切换基本权限状态
const toggleBasePermission = async (permission) => {
  try {
    const response = await fetch(`/api/permission/${permission.id}/base`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
      },
      body: JSON.stringify({ is_base: permission.is_base })
    })

    const result = await response.json()
    if (result.success) {
      ElMessage.success(`已${permission.is_base ? '设为' : '取消'}基本权限`)
      // 记录审计日志
      logAudit(permission)
    } else {
      ElMessage.error(result.message || '操作失败')
      permission.is_base = !permission.is_base // 恢复原状态
    }
  } catch (error) {
    ElMessage.error('网络错误')
    permission.is_base = !permission.is_base // 恢复原状态
  }
}

// 记录审计日志
const logAudit = async (permission) => {
  try {
    await fetch('/api/permission/audit-log', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
      },
      body: JSON.stringify({
        action: 'UPDATE_BASE_PERMISSION',
        target_type: 'PERMISSION',
        target_id: permission.id,
        details: `将权限 ${permission.permission_code} 设为基本权限: ${permission.is_base}`
      })
    })
  } catch (error) {
    console.error('审计日志记录失败:', error)
  }
}

// 加载权限数据
const loadPermissions = async () => {
  loading.value = true
  try {
    const response = await fetch('/api/permission/list', {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
      }
    })
    const result = await response.json()
    if (result.success) {
      permissions.value = result.data.permissions || result.data
    }
  } catch (error) {
    ElMessage.error('加载权限数据失败')
  } finally {
    loading.value = false
  }
}

// 加载权限分组
const loadPermissionGroups = async () => {
  try {
    const response = await fetch('/api/permission/groups', {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
      }
    })
    const result = await response.json()
    if (result.success) {
      permissionGroups.value = result.data || []
    }
  } catch (error) {
    // 如果 API 不存在，使用默认分组
    permissionGroups.value = [
      { group_code: 'auth', group_name: '认证模块', sort_order: 1 },
      { group_code: 'user', group_name: '用户模块', sort_order: 2 },
      { group_code: 'role', group_name: '角色模块', sort_order: 3 },
      { group_code: 'workarea', group_name: '作业区模块', sort_order: 4 },
      { group_code: 'company', group_name: '公司模块', sort_order: 5 },
      { group_code: 'menu', group_name: '菜单模块', sort_order: 6 },
      { group_code: 'system', group_name: '系统配置模块', sort_order: 7 },
      { group_code: 'permission', group_name: '权限管理模块', sort_order: 8 },
      { group_code: 'audit', group_name: '审计日志模块', sort_order: 9 }
    ]
  }
}

onMounted(() => {
  loadPermissions()
  loadPermissionGroups()
})
</script>

<style scoped>
.base-permission-page {
  padding: 20px;
  max-width: 1200px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  font-size: 24px;
  color: #303133;
  margin-bottom: 8px;
}

.description {
  color: #909399;
  font-size: 14px;
}

.stats-card {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.stat-item {
  display: flex;
  flex-direction: column;
}

.stat-label {
  color: #909399;
  font-size: 12px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}

.permission-groups {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.group-section {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
}

.group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e4e7ed;
}

.group-header h2 {
  font-size: 18px;
  color: #303133;
}

.group-count {
  font-size: 12px;
  color: #909399;
}

.permission-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.permission-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-radius: 4px;
  background: #f5f7fa;
  transition: all 0.3s;
}

.permission-item.is-base {
  background: #ecf5ff;
  border-left: 3px solid #409eff;
}

.permission-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.permission-code {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
}

.permission-name {
  font-size: 12px;
  color: #909399;
}

.permission-desc {
  font-size: 12px;
  color: #c0c4cc;
}

.permission-action {
  display: flex;
  align-items: center;
}

.help-section {
  margin-top: 30px;
  padding: 20px;
  background: #fff9e6;
  border: 1px solid #ffc107;
  border-radius: 8px;
}

.help-section h3 {
  font-size: 16px;
  color: #e6a23c;
  margin-bottom: 12px;
}

.help-section ul {
  list-style: disc;
  padding-left: 20px;
  color: #909399;
  font-size: 14px;
}

.help-section li {
  margin-bottom: 8px;
}
</style>