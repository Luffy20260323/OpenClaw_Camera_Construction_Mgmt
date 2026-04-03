<template>
  <AdminLayout>
    <div class="permission-copy">
      <el-card class="header-card">
        <div class="page-header">
          <h2>权限复制</h2>
          <p class="description">将源角色的权限快速复制到其他角色，支持批量操作</p>
        </div>
      </el-card>

      <el-card class="content-card">
        <!-- 步骤条 -->
        <el-steps :active="currentStep" finish-status="success" align-center style="margin-bottom: 30px">
          <el-step title="选择源角色" description="选择要复制权限的角色" />
          <el-step title="选择目标角色" description="选择要应用权限的角色" />
          <el-step title="确认复制" description="确认并执行复制操作" />
        </el-steps>

        <!-- 步骤 1：选择源角色 -->
        <div v-show="currentStep === 0" class="step-content">
          <el-form label-width="120px">
            <el-form-item label="源角色">
              <el-select
                v-model="sourceRoleId"
                placeholder="请选择源角色"
                filterable
                style="width: 400px"
                @change="loadSourcePermissions"
              >
                <el-option
                  v-for="role in roleList"
                  :key="role.id"
                  :label="`${role.role_name} (${role.role_code})`"
                  :value="role.id"
                  :disabled="role.role_code === 'admin'"
                />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-alert
                v-if="sourceRoleId"
                title="提示"
                type="info"
                :closable="false"
                show-icon
              >
                <p>已选择：<strong>{{ getRoleName(sourceRoleId) }}</strong></p>
                <p>权限数量：<strong>{{ sourcePermissionCount }}</strong> 个</p>
              </el-alert>
            </el-form-item>
          </el-form>
        </div>

        <!-- 步骤 2：选择目标角色 -->
        <div v-show="currentStep === 1" class="step-content">
          <el-form label-width="120px">
            <el-form-item label="目标角色">
              <el-checkbox-group v-model="targetRoleIds">
                <div class="role-checkbox-group">
                  <el-checkbox
                    v-for="role in availableTargetRoles"
                    :key="role.id"
                    :label="role.id"
                    :value="role.id"
                    border
                  >
                    <div class="role-checkbox-content">
                      <div class="role-name">{{ role.role_name }}</div>
                      <div class="role-code">{{ role.role_code }}</div>
                    </div>
                  </el-checkbox>
                </div>
              </el-checkbox-group>
            </el-form-item>
            <el-form-item>
              <el-alert
                v-if="targetRoleIds.length > 0"
                title="已选择"
                type="success"
                :closable="false"
                show-icon
              >
                <p>已选择 <strong>{{ targetRoleIds.length }}</strong> 个角色</p>
                <p>{{ targetRoleIds.map(id => getRoleName(id)).join('、') }}</p>
              </el-alert>
            </el-form-item>
          </el-form>
        </div>

        <!-- 步骤 3：确认 -->
        <div v-show="currentStep === 2" class="step-content">
          <el-alert
            title="确认信息"
            type="warning"
            :closable="false"
            show-icon
            style="margin-bottom: 20px"
          >
            <p><strong>源角色：</strong>{{ getRoleName(sourceRoleId) }}</p>
            <p><strong>权限数量：</strong>{{ sourcePermissionCount }} 个</p>
            <p><strong>目标角色：</strong></p>
            <ul>
              <li v-for="id in targetRoleIds" :key="id">
                {{ getRoleName(id) }}
              </li>
            </ul>
            <p style="margin-top: 10px">
              <strong>注意：</strong>此操作将覆盖目标角色的现有权限！
            </p>
          </el-alert>
        </div>

        <!-- 操作按钮 -->
        <div class="step-actions">
          <el-button @click="prevStep" :disabled="currentStep === 0">
            上一步
          </el-button>
          <el-button
            v-if="currentStep < 2"
            type="primary"
            @click="nextStep"
            :disabled="!canNextStep"
          >
            下一步
          </el-button>
          <el-button
            v-else
            type="success"
            @click="executeCopy"
            :loading="copying"
          >
            确认复制
          </el-button>
        </div>
      </el-card>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import AdminLayout from '@/layouts/AdminLayout.vue'

const roleList = ref([])
const sourceRoleId = ref(null)
const targetRoleIds = ref([])
const sourcePermissionCount = ref(0)
const currentStep = ref(0)
const copying = ref(false)

// 可用目标角色（排除已选源角色和管理员）
const availableTargetRoles = computed(() => {
  return roleList.value.filter(role => 
    role.id !== sourceRoleId.value && role.role_code !== 'admin'
  )
})

// 是否可以进入下一步
const canNextStep = computed(() => {
  if (currentStep.value === 0) {
    return sourceRoleId.value !== null
  } else if (currentStep.value === 1) {
    return targetRoleIds.value.length > 0
  }
  return true
})

// 获取角色名称
const getRoleName = (roleId) => {
  const role = roleList.value.find(r => r.id === roleId)
  return role ? `${role.role_name} (${role.role_code})` : '未知'
}

// 加载角色列表
const loadRoles = async () => {
  try {
    const response = await request.get('/role/list')
    if (response.data) {
      roleList.value = response.data
    }
  } catch (error) {
    console.error('加载角色列表失败:', error)
  }
}

// 加载源角色权限
const loadSourcePermissions = async () => {
  if (!sourceRoleId.value) return
  
  try {
    const response = await request.get(`/permission/copy/role/${sourceRoleId.value}`)
    if (response.data) {
      sourcePermissionCount.value = response.data.length
    }
  } catch (error) {
    console.error('加载源角色权限失败:', error)
    sourcePermissionCount.value = 0
  }
}

// 上一步
const prevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

// 下一步
const nextStep = () => {
  if (currentStep.value < 2 && canNextStep.value) {
    currentStep.value++
  }
}

// 执行复制
const executeCopy = async () => {
  try {
    await ElMessageBox.confirm(
      `确定将 ${getRoleName(sourceRoleId.value)} 的权限复制到 ${targetRoleIds.value.length} 个角色？此操作将覆盖目标角色的现有权限！`,
      '确认复制',
      { type: 'warning' }
    )
    
    copying.value = true
    
    const response = await request.post('/permission/copy/role', {
      sourceRoleId: sourceRoleId.value,
      targetRoleIds: targetRoleIds.value
    })
    
    if (response.data) {
      const result = response.data
      ElMessage.success(`成功复制 ${result.totalCopied} 个权限到 ${result.roleResults.length} 个角色`)
      
      // 重置表单
      resetForm()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '复制失败')
    }
  } finally {
    copying.value = false
  }
}

// 重置表单
const resetForm = () => {
  sourceRoleId.value = null
  targetRoleIds.value = []
  sourcePermissionCount.value = 0
  currentStep.value = 0
}

onMounted(() => {
  loadRoles()
})
</script>

<style scoped>
.permission-copy {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px;
  font-size: 20px;
  font-weight: 600;
}

.page-header .description {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.content-card {
  margin-bottom: 20px;
}

.step-content {
  min-height: 300px;
  padding: 20px 0;
}

.role-checkbox-group {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 12px;
}

.role-checkbox-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.role-name {
  font-weight: 500;
  color: #303133;
}

.role-code {
  font-size: 12px;
  color: #909399;
  font-family: monospace;
}

.step-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}
</style>
