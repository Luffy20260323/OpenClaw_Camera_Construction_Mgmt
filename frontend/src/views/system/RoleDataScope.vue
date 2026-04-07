<template>
  <AdminLayout>
    <div class="role-data-scope">
      <el-card class="header-card">
        <div class="page-header">
          <h2>角色数据范围配置</h2>
          <p class="description">
            配置角色的数据访问范围，控制角色用户可以查看和操作哪些数据。
          </p>
        </div>
      </el-card>

      <el-row :gutter="20">
        <!-- 左侧：角色列表 -->
        <el-col :span="10">
          <el-card class="role-list-card">
            <template #header>
              <div class="card-header">
                <span>角色列表</span>
                <el-input
                  v-model="roleSearchKeyword"
                  placeholder="搜索角色"
                  prefix-icon="Search"
                  clearable
                  style="width: 200px"
                />
              </div>
            </template>
            
            <el-table
              :data="filteredRoles"
              v-loading="loading"
              highlight-current-row
              @current-change="handleRoleSelect"
              @row-dblclick="handleRoleEdit"
            >
              <el-table-column prop="roleName" label="角色名称" min-width="120" />
              <el-table-column prop="roleCode" label="角色编码" width="150" />
              <el-table-column prop="companyTypeName" label="公司类型" width="100">
                <template #default="{ row }">
                  <el-tag size="small" :type="getCompanyTypeTag(row.companyTypeId)">
                    {{ row.companyTypeName }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
            
            <el-pagination
              v-model:current-page="rolePagination.pageNum"
              v-model:page-size="rolePagination.pageSize"
              :total="rolePagination.total"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next"
              class="mt-4"
              @current-change="loadRoleList"
              @size-change="loadRoleList"
            />
          </el-card>
        </el-col>

        <!-- 右侧：数据范围配置 -->
        <el-col :span="14">
          <el-card class="config-card" v-if="selectedRole">
            <template #header>
              <div class="card-header">
                <span>数据范围配置 - {{ selectedRole.roleName }}</span>
                <el-button type="primary" @click="handleSave" :loading="saving">
                  <el-icon><Check /></el-icon>
                  保存配置
                </el-button>
              </div>
            </template>

            <el-form :model="configForm" label-width="120px">
              <el-form-item label="角色信息">
                <el-descriptions :column="2" border>
                  <el-descriptions-item label="角色名称">
                    {{ selectedRole.roleName }}
                  </el-descriptions-item>
                  <el-descriptions-item label="角色编码">
                    {{ selectedRole.roleCode }}
                  </el-descriptions-item>
                  <el-descriptions-item label="公司类型" :span="2">
                    <el-tag :type="getCompanyTypeTag(selectedRole.companyTypeId)">
                      {{ selectedRole.companyTypeName }}
                    </el-tag>
                  </el-descriptions-item>
                  <el-descriptions-item label="描述" :span="2">
                    {{ selectedRole.roleDescription || '-' }}
                  </el-descriptions-item>
                </el-descriptions>
              </el-form-item>

              <el-divider>数据范围配置</el-divider>

              <el-form-item label="数据范围类型">
                <DataScopeSelector
                  v-model:scope-type="configForm.scopeType"
                  v-model:dept-id="configForm.deptId"
                  v-model:dept-ids="configForm.deptIds"
                  :dept-options="deptOptions"
                  :dept-tree="deptTree"
                  :disabled="!isSystemAdmin"
                  @change="handleScopeChange"
                />
              </el-form-item>

              <el-divider>自定义数据权限</el-divider>

              <el-form-item label="额外数据权限">
                <el-checkbox-group v-model="configForm.customDataPermissions">
                  <el-checkbox
                    v-for="perm in dataPermissionOptions"
                    :key="perm.value"
                    :label="perm.value"
                    :disabled="!isSystemAdmin"
                  >
                    {{ perm.label }}
                  </el-checkbox>
                </el-checkbox-group>
                <div class="form-tip">
                  选择后，该角色将拥有额外的数据访问权限
                </div>
              </el-form-item>

              <el-divider>配置说明</el-divider>

              <el-alert
                type="info"
                title="配置说明"
                :closable="false"
              >
                <ul>
                  <li><strong>仅本人数据：</strong>用户只能查看和操作自己创建的数据</li>
                  <li><strong>本部门数据：</strong>用户可以查看和操作本部门的数据</li>
                  <li><strong>本部门及下级：</strong>用户可以查看和操作本部门及下级部门的数据</li>
                  <li><strong>全部数据：</strong>用户可以查看和操作所有数据</li>
                </ul>
              </el-alert>
            </el-form>
          </el-card>

          <el-card class="empty-card" v-else>
            <el-empty description="请从左侧选择要配置的角色" />
          </el-card>
        </el-col>
      </el-row>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoleStore } from '@/stores/role'
import DataScopeSelector from '@/components/DataScopeSelector.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'
import { useUserStore } from '@/stores/user'

const roleStore = useRoleStore()
const userStore = useUserStore()

const loading = ref(false)
const saving = ref(false)
const roleSearchKeyword = ref('')

// 角色列表
const roles = ref([])
const rolePagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 选中的角色
const selectedRole = ref(null)

// 配置表单
const configForm = reactive({
  scopeType: 'SELF',
  deptId: null,
  deptIds: [],
  customDataPermissions: []
})

// 部门数据
const deptOptions = ref([])
const deptTree = ref([])

// 数据权限选项
const dataPermissionOptions = ref([
  { value: 'VIEW_ALL_DEPT', label: '查看跨部门数据' },
  { value: 'EDIT_ALL_DEPT', label: '编辑跨部门数据' },
  { value: 'EXPORT_DATA', label: '导出数据权限' },
  { value: 'ASSIGN_DATA', label: '分配数据权限' }
])

// 是否系统管理员
const isSystemAdmin = computed(() => {
  const roles = userStore.roles || []
  return roles.some(r => r.roleCode === 'SYSTEM_ADMIN')
})

// 过滤后的角色列表
const filteredRoles = computed(() => {
  if (!roleSearchKeyword.value) {
    return roles.value
  }
  const keyword = roleSearchKeyword.value.toLowerCase()
  return roles.value.filter(role =>
    role.roleName.toLowerCase().includes(keyword) ||
    role.roleCode.toLowerCase().includes(keyword)
  )
})

// 获取公司类型标签颜色
const getCompanyTypeTag = (typeId) => {
  const tagMap = {
    1: 'primary',
    2: 'success',
    3: 'warning',
    4: 'info'
  }
  return tagMap[typeId] || 'info'
}

// 加载角色列表
const loadRoleList = async () => {
  loading.value = true
  try {
    const res = await roleStore.loadRoleList({
      pageNum: rolePagination.pageNum,
      pageSize: rolePagination.pageSize
    })
    roles.value = res.records || []
    rolePagination.total = res.total || 0
  } catch (error) {
    console.error('加载角色列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 选择角色
const handleRoleSelect = async (role) => {
  if (!role) {
    selectedRole.value = null
    return
  }
  
  selectedRole.value = role
  
  // 加载角色数据范围配置
  loading.value = true
  try {
    const dataScope = await roleStore.loadRoleDataScope(role.id)
    
    if (dataScope) {
      configForm.scopeType = dataScope.scopeType || 'SELF'
      configForm.deptId = dataScope.deptId || null
      configForm.deptIds = dataScope.deptIds || []
      configForm.customDataPermissions = dataScope.customPermissions || []
    } else {
      // 默认值
      configForm.scopeType = 'SELF'
      configForm.deptId = null
      configForm.deptIds = []
      configForm.customDataPermissions = []
    }
  } catch (error) {
    console.error('加载角色数据范围失败:', error)
  } finally {
    loading.value = false
  }
}

// 双击编辑
const handleRoleEdit = (role) => {
  handleRoleSelect(role)
}

// 数据范围变更
const handleScopeChange = (data) => {
  console.log('数据范围变更:', data)
}

// 保存配置
const handleSave = async () => {
  if (!selectedRole.value) {
    ElMessage.warning('请选择角色')
    return
  }
  
  if (!isSystemAdmin.value) {
    ElMessage.error('无权限修改配置')
    return
  }
  
  saving.value = true
  try {
    await roleStore.configureRoleDataScope(selectedRole.value.id, {
      scopeType: configForm.scopeType,
      deptId: configForm.deptId,
      deptIds: configForm.deptIds,
      customPermissions: configForm.customDataPermissions
    })
    ElMessage.success('保存成功')
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadRoleList()
})
</script>

<style scoped lang="scss">
.role-data-scope {
  .header-card {
    margin-bottom: 16px;
    
    .page-header {
      h2 {
        margin: 0 0 8px 0;
        font-size: 20px;
        color: #303133;
      }
      
      .description {
        margin: 0;
        font-size: 14px;
        color: #606266;
      }
    }
  }
  
  .role-list-card,
  .config-card {
    height: calc(100vh - 280px);
    min-height: 500px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .mt-4 {
      margin-top: 16px;
    }
  }
  
  .empty-card {
    height: calc(100vh - 280px);
    min-height: 500px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .form-tip {
    margin-top: 8px;
    font-size: 12px;
    color: #909399;
  }
}
</style>
