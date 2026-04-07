<template>
    <div class="role-default-permission-page">
      <div class="page-header">
        <h1>角色缺省权限配置</h1>
        <p class="description">配置角色创建时的默认权限集合，新用户分配该角色时自动获得缺省权限</p>
      </div>

      <!-- 角色选择 -->
      <div class="role-selector">
        <el-select v-model="selectedRoleId" placeholder="选择角色" @change="loadRoleDefaultPermissions" style="width: 300px">
          <el-option v-for="role in roles" :key="role.id" :label="role.role_name" :value="role.id">
            <span>{{ role.role_name }}</span>
            <span style="color: #909399; font-size: 12px; margin-left: 8px;">Level {{ role.role_level }}</span>
          </el-option>
        </el-select>
        
        <div v-if="selectedRole" class="role-info">
          <span class="role-level">层级: Level {{ selectedRole.role_level }}</span>
          <span class="role-permissions">
            缺省权限: {{ defaultPermissionCount }} 个
          </span>
        </div>
      </div>

      <!-- 权限配置面板 -->
      <div v-if="selectedRoleId" class="permission-config">
        <!-- 快捷操作 -->
        <div class="quick-actions">
          <el-button @click="selectAllInGroup" type="primary" size="small">全选当前分组</el-button>
          <el-button @click="clearAllInGroup" type="default" size="small">清空当前分组</el-button>
          <el-button @click="resetToBase" type="warning" size="small">重置为基本权限</el-button>
          <el-button @click="saveChanges" type="success" size="small" :loading="saving">保存配置</el-button>
        </div>

        <!-- 权限分组配置 -->
        <div class="permission-groups">
          <div v-for="group in permissionGroups" :key="group.group_code" class="group-section">
            <div class="group-header">
              <h2>{{ group.group_name }}</h2>
              <el-checkbox 
                :model-value="isGroupAllSelected(group.group_code)"
                @change="toggleGroup(group.group_code, $event)"
              >
                {{ getGroupSelectedCount(group.group_code) }} / {{ getGroupTotalCount(group.group_code) }}
              </el-checkbox>
            </div>

            <div class="permission-list">
              <div v-for="permission in getGroupPermissions(group.group_code)" 
                   :key="permission.id" 
                   class="permission-item"
                   :class="{ 
                     'is-selected': selectedPermissions.includes(permission.id),
                     'is-base': permission.is_base
                   }">
                <div class="permission-info">
                  <div class="permission-main">
                    <span class="permission-code">{{ permission.permission_code }}</span>
                    <span class="permission-name">{{ permission.permission_name }}</span>
                    <el-tag v-if="permission.is_base" type="success" size="small">基本权限</el-tag>
                  </div>
                  <span v-if="permission.description" class="permission-desc">{{ permission.description }}</span>
                </div>
                <div class="permission-action">
                  <el-checkbox 
                    v-model="selectedPermissions"
                    :value="permission.id"
                    :disabled="permission.is_base && !canRemoveBase"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 未选择角色提示 -->
      <div v-else class="empty-state">
        <el-empty description="请先选择一个角色进行配置" />
      </div>

      <!-- 操作说明 -->
      <div class="help-section">
        <h3>说明</h3>
        <ul>
          <li>缺省权限是角色创建时自动分配给用户的默认权限</li>
          <li>缺省权限必须包含基本权限（标记为"基本权限"的权限）</li>
          <li>用户最终权限 = 基本权限 + 角色缺省权限 + 用户额外权限</li>
          <li>系统管理员角色的缺省权限为全部权限，不可修改</li>
          <li>修改缺省权限不影响已有用户，仅影响新分配该角色的用户</li>
        </ul>
      </div>
    </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// 数据
const roles = ref([])
const permissions = ref([])
const permissionGroups = ref([])
const selectedRoleId = ref(null)
const selectedPermissions = ref([])
const saving = ref(false)
const currentGroupIndex = ref(0)

// 计算属性
const selectedRole = computed(() => roles.value.find(r => r.id === selectedRoleId.value))
const defaultPermissionCount = computed(() => selectedPermissions.value.length)
const canRemoveBase = computed(() => false) // 基本权限不可移除

// 获取分组权限
const getGroupPermissions = (groupCode) => {
  return permissions.value.filter(p => p.group_code === groupCode)
}

const getGroupSelectedCount = (groupCode) => {
  const groupPerms = getGroupPermissions(groupCode)
  return groupPerms.filter(p => selectedPermissions.value.includes(p.id)).length
}

const getGroupTotalCount = (groupCode) => {
  return getGroupPermissions(groupCode).length
}

const isGroupAllSelected = (groupCode) => {
  const groupPerms = getGroupPermissions(groupCode)
  return groupPerms.length > 0 && groupPerms.every(p => selectedPermissions.value.includes(p.id))
}

// 切换分组全选/取消
const toggleGroup = (groupCode, checked) => {
  const groupPerms = getGroupPermissions(groupCode)
  if (checked) {
    // 添加所有分组权限
    const newIds = groupPerms.map(p => p.id).filter(id => !selectedPermissions.value.includes(id))
    selectedPermissions.value.push(...newIds)
  } else {
    // 移除非基本权限
    const baseIds = groupPerms.filter(p => p.is_base).map(p => p.id)
    selectedPermissions.value = selectedPermissions.value.filter(id => 
      !groupPerms.map(p => p.id).includes(id) || baseIds.includes(id)
    )
  }
}

// 快捷操作
const selectAllInGroup = () => {
  const group = permissionGroups.value[currentGroupIndex.value]
  if (group) {
    toggleGroup(group.group_code, true)
  }
}

const clearAllInGroup = () => {
  const group = permissionGroups.value[currentGroupIndex.value]
  if (group) {
    toggleGroup(group.group_code, false)
  }
}

const resetToBase = async () => {
  try {
    await ElMessageBox.confirm('确定要重置为基本权限吗？这将清除所有非基本权限的缺省配置。', '确认', {
      type: 'warning'
    })
    
    // 只保留基本权限
    selectedPermissions.value = permissions.value.filter(p => p.is_base).map(p => p.id)
    ElMessage.success('已重置为基本权限')
  } catch {
    // 用户取消
  }
}

// 保存配置
const saveChanges = async () => {
  if (!selectedRoleId.value) return
  
  saving.value = true
  try {
    const response = await fetch(`/api/permission/role/${selectedRoleId.value}/default`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
      },
      body: JSON.stringify({ permission_ids: selectedPermissions.value })
    })

    const result = await response.json()
    if (result.success) {
      ElMessage.success('缺省权限配置已保存')
      // 记录审计日志
      logAudit()
    } else {
      ElMessage.error(result.message || '保存失败')
    }
  } catch (error) {
    ElMessage.error('网络错误')
  } finally {
    saving.value = false
  }
}

// 记录审计日志
const logAudit = async () => {
  try {
    await fetch('/api/permission/audit-log', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
      },
      body: JSON.stringify({
        action: 'UPDATE_ROLE_DEFAULT_PERMISSION',
        target_type: 'ROLE',
        target_id: selectedRoleId.value,
        details: `更新角色 ${selectedRole.value?.role_name} 的缺省权限，共 ${selectedPermissions.value.length} 个`
      })
    })
  } catch (error) {
    console.error('审计日志记录失败:', error)
  }
}

// 加载角色列表
const loadRoles = async () => {
  try {
    const response = await fetch('/api/permission/roles', {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
      }
    })
    const result = await response.json()
    if (result.success) {
      roles.value = result.data || []
      // 排除系统保护角色（不可修改）
      // 不硬编码角色 Code，根据 is_system_protected 字段判断
      roles.value = roles.value.filter(r => !r.is_system_protected)
    }
  } catch (error) {
    ElMessage.error('加载角色列表失败')
  }
}

// 加载权限列表
const loadPermissions = async () => {
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
    ElMessage.error('加载权限列表失败')
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

// 加载角色缺省权限
const loadRoleDefaultPermissions = async () => {
  if (!selectedRoleId.value) return
  
  try {
    const response = await fetch(`/api/permission/role/${selectedRoleId.value}/default`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
      }
    })
    const result = await response.json()
    if (result.success) {
      selectedPermissions.value = result.data?.permission_ids || []
      // 确保基本权限始终包含
      const baseIds = permissions.value.filter(p => p.is_base).map(p => p.id)
      baseIds.forEach(id => {
        if (!selectedPermissions.value.includes(id)) {
          selectedPermissions.value.push(id)
        }
      })
    }
  } catch (error) {
    ElMessage.error('加载角色缺省权限失败')
  }
}

onMounted(() => {
  loadRoles()
  loadPermissions()
  loadPermissionGroups()
})
</script>

<style scoped>
.role-default-permission-page {
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

.role-selector {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 20px;
}

.role-info {
  display: flex;
  gap: 16px;
  font-size: 14px;
  color: #606266;
}

.quick-actions {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.permission-config {
  margin-top: 20px;
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

.permission-item.is-selected {
  background: #ecf5ff;
  border-left: 3px solid #409eff;
}

.permission-item.is-base {
  background: #f0f9eb;
}

.permission-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.permission-main {
  display: flex;
  align-items: center;
  gap: 8px;
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

.empty-state {
  margin-top: 40px;
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