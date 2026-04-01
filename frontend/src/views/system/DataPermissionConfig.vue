<template>
  <AdminLayout>
    <el-card class="box-card">
      <template #header>
        <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
          <span style="font-size: 16px; font-weight: 600;">数据权限配置</span>
          <el-tag type="info">📊 支持 SELF/DEPT/ALL 三种数据范围</el-tag>
        </div>
      </template>

      <!-- 配置类型切换 -->
      <el-tabs v-model="activeTab" style="margin-bottom: 20px;">
        <el-tab-pane label="角色数据权限" name="role">
          <el-alert
            title="角色数据权限"
            description="为角色配置数据访问范围，该角色下的所有用户将继承此权限"
            type="info"
            :closable="false"
            style="margin-bottom: 15px;"
          />
        </el-tab-pane>
        <el-tab-pane label="用户数据权限" name="user">
          <el-alert
            title="用户数据权限"
            description="为特定用户配置数据访问范围，优先级高于角色权限"
            type="info"
            :closable="false"
            style="margin-bottom: 15px;"
          />
        </el-tab-pane>
      </el-tabs>

      <!-- 角色数据权限配置 -->
      <div v-if="activeTab === 'role'">
        <!-- 角色选择 -->
        <el-form :inline="true" class="search-form">
          <el-form-item label="选择角色">
            <el-select v-model="selectedRoleId" placeholder="请选择角色" style="width: 300px" @change="loadRoleDataPermission">
              <el-option
                v-for="role in roleList"
                :key="role.id"
                :label="role.roleName"
                :value="role.id"
              />
            </el-select>
          </el-form-item>
        </el-form>

        <!-- 角色数据权限表单 -->
        <el-form
          v-if="selectedRoleId"
          ref="roleDataFormRef"
          :model="roleDataForm"
          label-width="120px"
          style="max-width: 600px;"
        >
          <el-form-item label="数据范围类型" required>
            <el-select v-model="roleDataForm.scopeType" placeholder="请选择数据范围" style="width: 100%;">
              <el-option
                v-for="opt in scopeOptions"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              >
                <div style="display: flex; justify-content: space-between; align-items: center;">
                  <span>{{ opt.label }}</span>
                  <el-tag size="small" type="info">{{ opt.description }}</el-tag>
                </div>
              </el-option>
            </el-select>
          </el-form-item>

          <el-form-item label="部门 ID" v-if="roleDataForm.scopeType === 'DEPT'">
            <el-input-number v-model="roleDataForm.deptId" :min="1" placeholder="请输入部门 ID" style="width: 100%;" />
          </el-form-item>

          <el-form-item label="部门 ID 列表" v-if="roleDataForm.scopeType === 'DEPT_AND_SUB'">
            <el-input v-model="roleDataForm.deptIds" placeholder="请输入部门 ID 列表，逗号分隔" />
            <div class="form-tip">多个部门 ID 使用英文逗号分隔，例如：1,2,3</div>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="submitRoleDataPermission" :loading="submitting">保存配置</el-button>
            <el-button @click="resetRoleForm">重置</el-button>
          </el-form-item>
        </el-form>

        <el-empty v-else description="请选择要配置的角色" />
      </div>

      <!-- 用户数据权限配置 -->
      <div v-if="activeTab === 'user'">
        <!-- 用户选择 -->
        <el-form :inline="true" class="search-form">
          <el-form-item label="选择用户">
            <el-select v-model="selectedUserId" placeholder="请选择用户" style="width: 300px" @change="loadUserDataPermission">
              <el-option
                v-for="user in userList"
                :key="user.id"
                :label="user.username + ' (' + user.realName + ')'"
                :value="user.id"
              />
            </el-select>
          </el-form-item>
        </el-form>

        <!-- 用户数据权限表单 -->
        <el-form
          v-if="selectedUserId"
          ref="userDataFormRef"
          :model="userDataForm"
          label-width="120px"
          style="max-width: 600px;"
        >
          <el-form-item label="数据范围类型" required>
            <el-select v-model="userDataForm.scopeType" placeholder="请选择数据范围" style="width: 100%;">
              <el-option
                v-for="opt in scopeOptions"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              >
                <div style="display: flex; justify-content: space-between; align-items: center;">
                  <span>{{ opt.label }}</span>
                  <el-tag size="small" type="info">{{ opt.description }}</el-tag>
                </div>
              </el-option>
            </el-select>
          </el-form-item>

          <el-form-item label="部门 ID" v-if="userDataForm.scopeType === 'DEPT'">
            <el-input-number v-model="userDataForm.deptId" :min="1" placeholder="请输入部门 ID" style="width: 100%;" />
          </el-form-item>

          <el-form-item label="部门 ID 列表" v-if="userDataForm.scopeType === 'DEPT_AND_SUB'">
            <el-input v-model="userDataForm.deptIds" placeholder="请输入部门 ID 列表，逗号分隔" />
            <div class="form-tip">多个部门 ID 使用英文逗号分隔，例如：1,2,3</div>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="submitUserDataPermission" :loading="submitting">保存配置</el-button>
            <el-button @click="resetUserForm">重置</el-button>
          </el-form-item>
        </el-form>

        <el-empty v-else description="请选择要配置的用户" />
      </div>
    </el-card>
  </AdminLayout>
</template>

<script setup>
import AdminLayout from '@/layouts/AdminLayout.vue'
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import {
  getRoleDataPermission,
  setRoleDataPermission,
  getUserDataPermission,
  setUserDataPermission,
  getDataScopeOptions
} from '@/api/dataPermission'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('role')
const submitting = ref(false)
const roleDataFormRef = ref(null)
const userDataFormRef = ref(null)

// 数据范围选项
const scopeOptions = computed(() => getDataScopeOptions())

// 角色列表
const roleList = ref([])
const selectedRoleId = ref(null)

// 用户列表
const userList = ref([])
const selectedUserId = ref(null)

// 角色数据权限表单
const roleDataForm = reactive({
  scopeType: 'SELF',
  deptId: null,
  deptIds: ''
})

// 用户数据权限表单
const userDataForm = reactive({
  scopeType: 'SELF',
  deptId: null,
  deptIds: ''
})

// 权限验证
const checkPermissions = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.error('请先登录')
    router.push('/login')
    return false
  }
  if (userStore.companyTypeId !== 4) {
    ElMessage.warning('您没有权限访问此页面')
    router.push('/')
    return false
  }
  return true
}

// 加载角色列表
const loadRoleList = async () => {
  try {
    const res = await request({
      url: '/role',
      method: 'get',
      params: { pageNum: 1, pageSize: 100 }
    })
    roleList.value = res.data.records || []
  } catch (error) {
    console.error('加载角色列表失败:', error)
  }
}

// 加载用户列表
const loadUserList = async () => {
  try {
    const res = await request({
      url: '/user/list',
      method: 'get',
      params: { pageNum: 1, pageSize: 100 }
    })
    userList.value = res.data.records || []
  } catch (error) {
    console.error('加载用户列表失败:', error)
  }
}

// 加载角色数据权限
const loadRoleDataPermission = async (roleId) => {
  if (!roleId) return
  
  try {
    const res = await getRoleDataPermission(roleId)
    const data = res.data
    
    roleDataForm.scopeType = data.scopeType || 'SELF'
    roleDataForm.deptId = data.deptId
    roleDataForm.deptIds = data.deptIds || ''
  } catch (error) {
    console.error('加载角色数据权限失败:', error)
    ElMessage.error('加载角色数据权限失败')
  }
}

// 加载用户数据权限
const loadUserDataPermission = async (userId) => {
  if (!userId) return
  
  try {
    const res = await getUserDataPermission(userId)
    const data = res.data
    
    userDataForm.scopeType = data.scopeType || 'SELF'
    userDataForm.deptId = data.deptId
    userDataForm.deptIds = data.deptIds || ''
  } catch (error) {
    console.error('加载用户数据权限失败:', error)
    ElMessage.error('加载用户数据权限失败')
  }
}

// 提交角色数据权限
const submitRoleDataPermission = async () => {
  if (!selectedRoleId.value) {
    ElMessage.warning('请选择角色')
    return
  }
  
  submitting.value = true
  try {
    const params = {
      scopeType: roleDataForm.scopeType
    }
    
    if (roleDataForm.scopeType === 'DEPT' && roleDataForm.deptId) {
      params.deptId = roleDataForm.deptId
    }
    
    if (roleDataForm.scopeType === 'DEPT_AND_SUB' && roleDataForm.deptIds) {
      params.deptIds = roleDataForm.deptIds
    }
    
    await setRoleDataPermission(selectedRoleId.value, params)
    ElMessage.success('保存成功')
  } catch (error) {
    console.error('保存角色数据权限失败:', error)
    ElMessage.error(error.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

// 提交用户数据权限
const submitUserDataPermission = async () => {
  if (!selectedUserId.value) {
    ElMessage.warning('请选择用户')
    return
  }
  
  submitting.value = true
  try {
    const params = {
      scopeType: userDataForm.scopeType
    }
    
    if (userDataForm.scopeType === 'DEPT' && userDataForm.deptId) {
      params.deptId = userDataForm.deptId
    }
    
    if (userDataForm.scopeType === 'DEPT_AND_SUB' && userDataForm.deptIds) {
      params.deptIds = userDataForm.deptIds
    }
    
    await setUserDataPermission(selectedUserId.value, params)
    ElMessage.success('保存成功')
  } catch (error) {
    console.error('保存用户数据权限失败:', error)
    ElMessage.error(error.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

// 重置角色表单
const resetRoleForm = () => {
  if (selectedRoleId.value) {
    loadRoleDataPermission(selectedRoleId.value)
  } else {
    roleDataForm.scopeType = 'SELF'
    roleDataForm.deptId = null
    roleDataForm.deptIds = ''
  }
}

// 重置用户表单
const resetUserForm = () => {
  if (selectedUserId.value) {
    loadUserDataPermission(selectedUserId.value)
  } else {
    userDataForm.scopeType = 'SELF'
    userDataForm.deptId = null
    userDataForm.deptIds = ''
  }
}

onMounted(() => {
  if (!checkPermissions()) {
    return
  }
  
  loadRoleList()
  loadUserList()
})
</script>

<style scoped lang="scss">
.data-permission-config {
  min-height: 100vh;
  background: #f5f7fa;

  .box-card {
    max-width: 1200px;
    margin: 0 auto;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 16px;
    font-weight: 600;
  }

  .search-form {
    margin-bottom: 20px;
  }

  .form-tip {
    font-size: 12px;
    color: #999;
    margin-top: 4px;
  }
}
</style>
