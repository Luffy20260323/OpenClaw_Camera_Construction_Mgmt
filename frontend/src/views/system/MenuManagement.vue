<template>
  <AdminLayout>
    <div class="menu-management-page">
      <div class="page-header">
        <h1>菜单管理</h1>
        <p class="description">管理系统菜单的显示顺序、层级和可见性</p>
      </div>

      <!-- 操作按钮 -->
      <div class="action-bar">
        <el-button type="primary" @click="showCreateDialog">
          <el-icon><Plus /></el-icon>
          新增菜单
        </el-button>
        <el-button @click="loadMenus">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>

      <!-- 菜单树形表格 -->
      <div class="menu-tree">
        <el-table
          :data="menuTree"
          row-key="id"
          :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
          default-expand-all
          border
          stripe
        >
          <el-table-column prop="menuName" label="菜单名称" width="200">
            <template #default="{ row }">
              <span :class="{ 'is-parent': !row.parentId }">
                <el-icon v-if="row.icon"><component :is="row.icon" /></el-icon>
                {{ row.menuName }}
              </span>
            </template>
          </el-table-column>
          
          <el-table-column prop="menuCode" label="菜单代码" width="180" />
          
          <el-table-column prop="menuPath" label="路径" width="200">
            <template #default="{ row }">
              <span v-if="row.menuPath">{{ row.menuPath }}</span>
              <span v-else class="no-path">（父级菜单）</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="sortOrder" label="排序" width="80" align="center">
            <template #default="{ row }">
              <el-input-number
                v-model="row.sortOrder"
                :min="0"
                :max="99"
                size="small"
                @change="updateSortOrder(row)"
              />
            </template>
          </el-table-column>
          
          <el-table-column prop="isVisible" label="可见" width="80" align="center">
            <template #default="{ row }">
              <el-switch
                v-model="row.isVisible"
                @change="updateVisibility(row)"
                :disabled="row.isSystemProtected"
              />
            </template>
          </el-table-column>
          
          <el-table-column prop="isSystemProtected" label="系统保护" width="80" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.isSystemProtected" type="danger" size="small">保护</el-tag>
              <el-tag v-else type="success" size="small">可编辑</el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="requiredPermission" label="所需权限" width="180">
            <template #default="{ row }">
              <span v-if="row.requiredPermission">{{ row.requiredPermission }}</span>
              <span v-else class="no-permission">（无需权限）</span>
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button 
                type="primary" 
                size="small" 
                text 
                @click="showEditDialog(row)"
                :disabled="row.isSystemProtected"
              >
                编辑
              </el-button>
              <el-button 
                type="danger" 
                size="small" 
                text 
                @click="deleteMenu(row)"
                :disabled="row.isSystemProtected || row.children?.length > 0"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 新增/编辑对话框 -->
      <el-dialog
        v-model="dialogVisible"
        :title="dialogType === 'create' ? '新增菜单' : '编辑菜单'"
        width="500px"
        @close="resetForm"
      >
        <el-form :model="menuForm" :rules="formRules" ref="formRef" label-width="100px">
          <el-form-item label="菜单名称" prop="menuName">
            <el-input v-model="menuForm.menuName" placeholder="请输入菜单名称" />
          </el-form-item>
          
          <el-form-item label="菜单代码" prop="menuCode">
            <el-input v-model="menuForm.menuCode" placeholder="请输入菜单代码（唯一标识）" />
          </el-form-item>
          
          <el-form-item label="菜单路径" prop="menuPath">
            <el-input v-model="menuForm.menuPath" placeholder="请输入菜单路径（如 /system/config）" />
          </el-form-item>
          
          <el-form-item label="父级菜单" prop="parentId">
            <el-select v-model="menuForm.parentId" placeholder="选择父级菜单（可选）" clearable>
              <el-option 
                v-for="menu in parentMenuOptions" 
                :key="menu.id" 
                :label="menu.menuName" 
                :value="menu.id"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="图标" prop="icon">
            <el-input v-model="menuForm.icon" placeholder="请输入图标名称（如 Setting）" />
          </el-form-item>
          
          <el-form-item label="排序" prop="sortOrder">
            <el-input-number v-model="menuForm.sortOrder" :min="0" :max="99" />
          </el-form-item>
          
          <el-form-item label="是否可见" prop="isVisible">
            <el-switch v-model="menuForm.isVisible" />
          </el-form-item>
          
          <el-form-item label="所需权限" prop="requiredPermission">
            <el-select v-model="menuForm.requiredPermission" placeholder="选择所需权限（可选）" clearable filterable>
              <el-option 
                v-for="perm in permissions" 
                :key="perm.permissionCode" 
                :label="`${perm.permissionName} (${perm.permissionCode})`" 
                :value="perm.permissionCode"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="描述" prop="description">
            <el-input v-model="menuForm.description" type="textarea" placeholder="请输入菜单描述" />
          </el-form-item>
        </el-form>
        
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
        </template>
      </el-dialog>

      <!-- 操作说明 -->
      <div class="help-section">
        <h3>使用说明</h3>
        <ul>
          <li><strong>排序</strong>：数值越小，菜单显示越靠前</li>
          <li><strong>父级菜单</strong>：设置菜单层级关系，父级菜单通常没有路径</li>
          <li><strong>可见性</strong>：控制菜单是否显示给用户</li>
          <li><strong>系统保护</strong>：标记为保护的菜单不可编辑和删除</li>
          <li><strong>所需权限</strong>：用户需要拥有该权限才能看到此菜单</li>
          <li><strong>修改后</strong>：用户需要重新登录才能看到菜单变化</li>
        </ul>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import AdminLayout from '@/layouts/AdminLayout.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'

// 数据
const menus = ref([])
const permissions = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogType = ref('create')
const submitting = ref(false)
const formRef = ref(null)

// 表单
const menuForm = ref({
  menuName: '',
  menuCode: '',
  menuPath: '',
  parentId: null,
  icon: '',
  sortOrder: 0,
  isVisible: true,
  requiredPermission: '',
  description: '',
  isSystemProtected: false
})

// 表单验证规则
const formRules = {
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  menuCode: [{ required: true, message: '请输入菜单代码', trigger: 'blur' }]
}

// 构建树形结构
const menuTree = computed(() => {
  // 先构建父子关系
  const menuMap = {}
  menus.value.forEach(menu => {
    menuMap[menu.id] = { ...menu, children: [] }
  })
  
  // 组装树
  const tree = []
  menus.value.forEach(menu => {
    const node = menuMap[menu.id]
    if (menu.parentId && menuMap[menu.parentId]) {
      menuMap[menu.parentId].children.push(node)
    } else {
      tree.push(node)
    }
  })
  
  // 按 sortOrder 排序
  const sortByOrder = (nodes) => {
    nodes.sort((a, b) => a.sortOrder - b.sortOrder)
    nodes.forEach(node => {
      if (node.children?.length > 0) {
        sortByOrder(node.children)
      }
    })
  }
  
  sortByOrder(tree)
  return tree
})

// 父级菜单选项（用于选择父级）
const parentMenuOptions = computed(() => {
  // 只返回顶层菜单作为父级选项
  return menus.value.filter(m => !m.parentId || m.parentId === null)
})

// 加载菜单列表
const loadMenus = async () => {
  loading.value = true
  try {
    const token = localStorage.getItem('accessToken')
    const response = await fetch('/api/menu/all', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    const result = await response.json()
    if (result.success) {
      menus.value = result.data || []
    } else {
      ElMessage.error(result.message || '加载失败')
    }
  } catch (error) {
    ElMessage.error('网络错误')
  } finally {
    loading.value = false
  }
}

// 加载权限列表（用于选择所需权限）
const loadPermissions = async () => {
  try {
    const token = localStorage.getItem('accessToken')
    const response = await fetch('/api/permission/list', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    const result = await response.json()
    if (result.success) {
      permissions.value = result.data?.permissions || result.data || []
    }
  } catch (error) {
    console.error('加载权限列表失败:', error)
  }
}

// 显示创建对话框
const showCreateDialog = () => {
  dialogType.value = 'create'
  resetForm()
  dialogVisible.value = true
}

// 显示编辑对话框
const showEditDialog = (menu) => {
  dialogType.value = 'edit'
  menuForm.value = {
    id: menu.id,
    menuName: menu.menuName,
    menuCode: menu.menuCode,
    menuPath: menu.menuPath || '',
    parentId: menu.parentId || null,
    icon: menu.icon || '',
    sortOrder: menu.sortOrder || 0,
    isVisible: menu.isVisible,
    requiredPermission: menu.requiredPermission || '',
    description: menu.description || '',
    isSystemProtected: menu.isSystemProtected
  }
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  menuForm.value = {
    menuName: '',
    menuCode: '',
    menuPath: '',
    parentId: null,
    icon: '',
    sortOrder: 0,
    isVisible: true,
    requiredPermission: '',
    description: '',
    isSystemProtected: false
  }
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  
  submitting.value = true
  try {
    const token = localStorage.getItem('accessToken')
    const url = dialogType.value === 'create' 
      ? '/api/menu' 
      : `/api/menu/${menuForm.value.id}`
    const method = dialogType.value === 'create' ? 'POST' : 'PUT'
    
    const response = await fetch(url, {
      method,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(menuForm.value)
    })
    
    const result = await response.json()
    if (result.success) {
      ElMessage.success(dialogType.value === 'create' ? '菜单创建成功' : '菜单更新成功')
      dialogVisible.value = false
      loadMenus()
      
      // 记录审计日志
      logAudit(dialogType.value === 'create' ? 'CREATE_MENU' : 'UPDATE_MENU', menuForm.value)
    } else {
      ElMessage.error(result.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('网络错误')
  } finally {
    submitting.value = false
  }
}

// 更新排序
const updateSortOrder = async (menu) => {
  try {
    const token = localStorage.getItem('accessToken')
    const response = await fetch(`/api/menu/${menu.id}/sort`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({ sortOrder: menu.sortOrder })
    })
    
    const result = await response.json()
    if (result.success) {
      ElMessage.success('排序更新成功')
      logAudit('UPDATE_MENU_SORT', menu)
    } else {
      ElMessage.error(result.message || '更新失败')
      loadMenus() // 恢复原数据
    }
  } catch (error) {
    ElMessage.error('网络错误')
    loadMenus()
  }
}

// 更新可见性
const updateVisibility = async (menu) => {
  try {
    const token = localStorage.getItem('accessToken')
    const response = await fetch(`/api/menu/${menu.id}/visibility`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({ isVisible: menu.isVisible })
    })
    
    const result = await response.json()
    if (result.success) {
      ElMessage.success(`菜单已${menu.isVisible ? '显示' : '隐藏'}`)
      logAudit('UPDATE_MENU_VISIBILITY', menu)
    } else {
      ElMessage.error(result.message || '更新失败')
      loadMenus()
    }
  } catch (error) {
    ElMessage.error('网络错误')
    loadMenus()
  }
}

// 删除菜单
const deleteMenu = async (menu) => {
  if (menu.isSystemProtected) {
    ElMessage.warning('系统保护的菜单不可删除')
    return
  }
  
  if (menu.children?.length > 0) {
    ElMessage.warning('请先删除子菜单')
    return
  }
  
  try {
    await ElMessageBox.confirm(`确定要删除菜单 "${menu.menuName}" 吗？`, '确认删除', {
      type: 'warning'
    })
    
    const token = localStorage.getItem('accessToken')
    const response = await fetch(`/api/menu/${menu.id}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    const result = await response.json()
    if (result.success) {
      ElMessage.success('菜单删除成功')
      loadMenus()
      logAudit('DELETE_MENU', menu)
    } else {
      ElMessage.error(result.message || '删除失败')
    }
  } catch {
    // 用户取消
  }
}

// 记录审计日志
const logAudit = async (action, menu) => {
  try {
    const token = localStorage.getItem('accessToken')
    await fetch('/api/permission/audit-log', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({
        action,
        targetType: 'MENU',
        targetId: menu.id,
        details: `菜单: ${menu.menuName} (${menu.menuCode})`
      })
    })
  } catch (error) {
    console.error('审计日志记录失败:', error)
  }
}

onMounted(() => {
  loadMenus()
  loadPermissions()
})
</script>

<style scoped>
.menu-management-page {
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

.action-bar {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.menu-tree {
  margin-bottom: 20px;
}

.is-parent {
  font-weight: 600;
  color: #303133;
}

.no-path, .no-permission {
  color: #c0c4cc;
  font-size: 12px;
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

.help-section strong {
  color: #606266;
}
</style>