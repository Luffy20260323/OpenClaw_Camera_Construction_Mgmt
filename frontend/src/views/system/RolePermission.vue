<template>
  <AdminLayout>
    <div class="permission-management">
      <el-card class="header-card">
        <div class="page-header">
          <h2>角色权限配置</h2>
          <p class="description">为角色分配功能权限，系统管理员默认拥有所有权限且不可修改</p>
        </div>
      </el-card>

      <el-card class="content-card">
        <!-- 角色选择 -->
        <el-form :inline="true" class="role-select-form">
          <el-form-item label="选择角色">
            <el-select
              v-model="selectedRoleId"
              placeholder="请选择角色"
              filterable
              style="width: 300px"
              @change="loadRolePermissions"
            >
              <el-option
                v-for="role in roleList"
                :key="role.id"
                :label="`${role.role_name} (${role.role_code})`"
                :value="role.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadRolePermissions" :disabled="!selectedRoleId">
              加载权限
            </el-button>
          </el-form-item>
          <el-form-item>
            <el-button type="success" @click="savePermissions" :disabled="!selectedRoleId || !hasChanges || isProtectedRole">
              保存修改
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 角色信息展示 -->
        <div v-if="selectedRole" class="role-info">
          <el-descriptions title="角色信息" :column="3" border>
            <el-descriptions-item label="角色名称">{{ selectedRole.role_name }}</el-descriptions-item>
            <el-descriptions-item label="角色编码">{{ selectedRole.role_code }}</el-descriptions-item>
            <el-descriptions-item label="描述">{{ selectedRole.role_description || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 系统管理员提示 -->
        <el-alert
          v-if="isProtectedRole"
          title="系统管理员角色权限不可修改"
          type="warning"
          :closable="false"
          style="margin-top: 20px"
          show-icon
        >
          系统管理员角色默认拥有所有权限，不允许编辑。
        </el-alert>

        <!-- 权限列表表格 -->
        <el-table 
          :data="permissionList" 
          border 
          style="width: 100%; margin-top: 20px"
          v-loading="loading"
          row-key="id"
          :tree-props="{children: 'children'}"
        >
          <el-table-column label="分配状态" width="120" align="center">
            <template #default="{ row }">
              <!-- 模块级别（有子节点） -->
              <div v-if="row.children && row.children.length > 0" class="module-checkbox-wrapper">
                <el-checkbox
                  v-model="row.selected"
                  :indeterminate="getModuleIndeterminate(row)"
                  :disabled="isProtectedRole"
                  @change="handleModuleChange(row, $event)"
                  :class="{
                    'checkbox-all': isModuleAllSelected(row),
                    'checkbox-partial': getModuleIndeterminate(row)
                  }"
                />
              </div>
              <!-- 具体权限（叶子节点） -->
              <div v-else class="permission-checkbox-wrapper">
                <el-checkbox
                  v-model="row.selected"
                  :disabled="isProtectedRole"
                  @change="handlePermissionChange(row)"
                />
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="permissionCode" label="权限代码" width="200" />
          <el-table-column prop="permissionName" label="权限名称" width="200" />
          <el-table-column prop="description" label="描述">
            <template #default="{ row }">
              <span v-if="!row.children">{{ row.description || '-' }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
        </el-table>

        <!-- 权限说明 -->
        <el-alert
          title="权限说明"
          type="info"
          :closable="false"
          style="margin-top: 20px"
        >
          <ul>
            <li><strong>模块复选框</strong>：
              <span class="legend-item"><span class="legend-box legend-green"></span> 绿色背景 = 全部授权</span>
              <span class="legend-item"><span class="legend-box legend-yellow"></span> 黄色背景 = 部分授权</span>
              <span class="legend-item">空白 = 全部未授权</span>
            </li>
            <li><strong>操作方式</strong>：点击模块复选框可批量授权/取消授权，点击具体权限复选框可单独授权/取消</li>
            <li><strong>系统管理员角色</strong>：默认拥有所有权限，不允许修改</li>
            <li><strong>权限控制</strong>：角色权限管理和用户权限管理本身需要相应权限才能操作</li>
          </ul>
        </el-alert>
      </el-card>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import AdminLayout from '@/layouts/AdminLayout.vue'

import { getAllPermissions, getAllRoles, getRolePermissions, updateRolePermissions } from '@/api/permission'

const loading = ref(false)
const roleList = ref([])
const selectedRoleId = ref(null)
const selectedRole = ref(null)
const permissionList = ref([])
const originalPermissionIds = ref([])

// 受保护的角色列表（不允许编辑权限）
const PROTECTED_ROLES = ['ROLE_SYSTEM_ADMIN']

// 是否为受保护角色（系统管理员等）
const isProtectedRole = computed(() => {
  return selectedRole.value && PROTECTED_ROLES.includes(selectedRole.value.role_code)
})

// 检查是否有未保存的修改
const hasChanges = computed(() => {
  const currentIds = getAllSelectedIds()
  if (currentIds.length !== originalPermissionIds.value.length) return true
  const currentSet = new Set(currentIds)
  const originalSet = new Set(originalPermissionIds.value)
  return ![...currentSet].every(id => originalSet.has(id))
})

// 获取所有选中的权限ID
const getAllSelectedIds = () => {
  const ids = []
  permissionList.value.forEach(module => {
    if (module.children) {
      module.children.forEach(perm => {
        if (perm.selected) ids.push(perm.id)
      })
    }
  })
  return ids
}

// 判断模块是否全选
const isModuleAllSelected = (module) => {
  if (!module.children || module.children.length === 0) return false
  return module.children.every(perm => perm.selected)
}

// 判断模块是否部分选中
const getModuleIndeterminate = (module) => {
  if (!module.children || module.children.length === 0) return false
  const selectedCount = module.children.filter(perm => perm.selected).length
  return selectedCount > 0 && selectedCount < module.children.length
}

// 处理模块复选框变化
const handleModuleChange = (module, checked) => {
  if (!module.children) return
  module.children.forEach(perm => {
    perm.selected = checked
  })
}

// 处理具体权限复选框变化
const handlePermissionChange = () => {
  // 权限变化会自动更新
}

// 加载角色列表
const loadRoles = async () => {
  try {
    const res = await getAllRoles()
    roleList.value = res.data || []
  } catch (error) {
    console.error('加载角色列表失败:', error)
  }
}

// 加载所有权限
const loadAllPermissions = async () => {
  try {
    const res = await getAllPermissions()
    const grouped = groupPermissionsByModule(res.data || [])
    permissionList.value = grouped
  } catch (error) {
    console.error('加载权限列表失败:', error)
  }
}

// 按模块分组权限
const groupPermissionsByModule = (permissions) => {
  const modules = {
    'auth': { id: 'auth', permissionCode: '认证模块', permissionName: '认证模块', children: [], selected: false },
    'user': { id: 'user', permissionCode: '用户模块', permissionName: '用户模块', children: [], selected: false },
    'role': { id: 'role', permissionCode: '角色模块', permissionName: '角色模块', children: [], selected: false },
    'workarea': { id: 'workarea', permissionCode: '作业区模块', permissionName: '作业区模块', children: [], selected: false },
    'company': { id: 'company', permissionCode: '公司模块', permissionName: '公司模块', children: [], selected: false },
    'menu': { id: 'menu', permissionCode: '菜单模块', permissionName: '菜单模块', children: [], selected: false },
    'system': { id: 'system', permissionCode: '系统配置', permissionName: '系统配置', children: [], selected: false },
    'permission': { id: 'permission', permissionCode: '权限管理', permissionName: '权限管理', children: [], selected: false },
    'audit_log': { id: 'audit_log', permissionCode: '审计日志', permissionName: '审计日志', children: [], selected: false }
  }
  
  permissions.forEach(perm => {
    const moduleKey = perm.permissionCode.split(':')[0]
    if (modules[moduleKey]) {
      modules[moduleKey].children.push({
        ...perm,
        selected: false
      })
    }
  })
  
  return Object.values(modules).filter(m => m.children.length > 0)
}

// 加载角色权限
const loadRolePermissions = async () => {
  if (!selectedRoleId.value) return
  
  loading.value = true
  try {
    const role = roleList.value.find(r => r.id === selectedRoleId.value)
    selectedRole.value = role
    
    // 如果是受保护角色，自动全选所有权限
    if (PROTECTED_ROLES.includes(role.role_code)) {
      permissionList.value.forEach(module => {
        if (module.children) {
          module.children.forEach(perm => {
            perm.selected = true
          })
          module.selected = true
        }
      })
      originalPermissionIds.value = getAllSelectedIds()
      ElMessage.success('系统管理员角色拥有所有权限')
    } else {
      const res = await getRolePermissions(selectedRoleId.value)
      const permissionIds = res.data?.permissionIds || []
      originalPermissionIds.value = [...permissionIds]
      
      // 更新权限列表选中状态
      permissionList.value.forEach(module => {
        if (module.children) {
          module.children.forEach(perm => {
            perm.selected = permissionIds.includes(perm.id)
          })
          // 更新模块选中状态
          module.selected = module.children.every(perm => perm.selected)
        }
      })
      
      ElMessage.success('权限加载成功')
    }
  } catch (error) {
    console.error('加载权限失败:', error)
    ElMessage.error('加载权限失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 保存权限修改
const savePermissions = async () => {
  if (!selectedRoleId.value) return
  
  // 再次检查是否为受保护角色
  if (isProtectedRole.value) {
    ElMessage.warning('系统管理员角色权限不允许修改')
    return
  }
  
  try {
    await ElMessageBox.confirm('确定要保存权限修改吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const selectedIds = getAllSelectedIds()
    
    await updateRolePermissions(selectedRoleId.value, {
      permissionIds: selectedIds,
      comment: '通过角色权限配置页面修改'
    })
    
    ElMessage.success('权限保存成功')
    await loadRolePermissions()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('保存权限失败:', error)
      ElMessage.error('保存权限失败：' + error.message)
    }
  }
}

onMounted(() => {
  loadRoles()
  loadAllPermissions()
})
</script>

<style scoped lang="scss">
.permission-management {
  .header-card {
    margin-bottom: 20px;
    
    .page-header {
      h2 {
        margin: 0 0 8px 0;
        font-size: 20px;
        color: #333;
      }
      
      .description {
        margin: 0;
        color: #666;
        font-size: 14px;
      }
    }
  }
  
  .content-card {
    .role-select-form {
      margin-bottom: 20px;
    }
    
    .role-info {
      margin-bottom: 20px;
    }
  }
  
  // 模块复选框样式
  .module-checkbox-wrapper {
    display: flex;
    justify-content: center;
    align-items: center;
    
    :deep(.el-checkbox) {
      padding: 8px 12px;
      border-radius: 4px;
      margin: 0;
      
      &.checkbox-all {
        background-color: #f0f9eb;
        
        .el-checkbox__inner {
          background-color: #67c23a;
          border-color: #67c23a;
        }
      }
      
      &.checkbox-partial {
        background-color: #fdf6ec;
        
        .el-checkbox__inner {
          background-color: #e6a23c;
          border-color: #e6a23c;
        }
        
        .el-checkbox__inner::after {
          content: '';
          position: absolute;
          display: block;
          left: 4px;
          top: 7px;
          width: 6px;
          height: 2px;
          background-color: #fff;
          transform: none;
        }
      }
    }
  }
  
  .permission-checkbox-wrapper {
    display: flex;
    justify-content: center;
    align-items: center;
  }
  
  // 图例样式
  .legend-item {
    margin-right: 16px;
    display: inline-flex;
    align-items: center;
    gap: 4px;
  }
  
  .legend-box {
    display: inline-block;
    width: 16px;
    height: 16px;
    border-radius: 3px;
    border: 1px solid #dcdfe6;
    
    &.legend-green {
      background-color: #f0f9eb;
      border-color: #67c23a;
    }
    
    &.legend-yellow {
      background-color: #fdf6ec;
      border-color: #e6a23c;
    }
  }
}
</style>