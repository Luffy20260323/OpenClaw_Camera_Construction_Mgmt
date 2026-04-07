<template>
    <div class="role-type-permissions">
      <!-- 页面标题 -->
      <el-card class="header-card">
        <div class="header-content">
          <h2>角色类型缺省权限配置</h2>
          <p>配置不同角色类型的缺省权限，新增角色时自动继承</p>
        </div>
      </el-card>

      <!-- 角色类型选择 -->
      <el-card class="filter-card">
        <el-form :inline="true" :model="filterForm">
          <el-form-item label="角色类型">
            <el-select v-model="filterForm.roleType" @change="loadPermissions">
              <el-option label="SYSTEM - 系统管理员" value="SYSTEM" />
              <el-option label="DEFAULT - 普通角色" value="DEFAULT" />
              <el-option label="PRESET - 预设角色" value="PRESET" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleBatchAdd">批量添加权限</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 权限列表 -->
      <el-card class="table-card">
        <template #header>
          <div class="card-header">
            <span>缺省权限列表（{{ permissions.length }} 项）</span>
            <el-button type="danger" :disabled="selectedIds.length === 0" @click="handleBatchDelete">
              批量删除（{{ selectedIds.length }}）
            </el-button>
          </div>
        </template>

        <el-table 
          :data="permissions" 
          style="width: 100%"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="resourceType" label="资源类型" width="100">
            <template #default="{ row }">
              <el-tag :type="getResourceTypeTag(row.resourceType)">
                {{ getResourceTypeText(row.resourceType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="resourceName" label="资源名称" min-width="180" />
          <el-table-column prop="permissionKey" label="权限码" min-width="250" />
          <el-table-column prop="moduleCode" label="模块" width="120" />
          <el-table-column prop="sortOrder" label="排序" width="80" />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 批量添加对话框 -->
      <el-dialog 
        v-model="batchAddDialogVisible" 
        title="批量添加权限"
        width="800px"
      >
        <el-form :model="batchAddForm">
          <el-form-item label="选择资源">
            <el-tree-select
              v-model="batchAddForm.resourceIds"
              :data="resourceTree"
              :props="{ children: 'children', label: 'name', value: 'id' }"
              multiple
              check-strictly
              placeholder="选择要添加的资源"
              style="width: 100%"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="batchAddDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitBatchAdd">确定</el-button>
        </template>
      </el-dialog>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

// 过滤表单
const filterForm = reactive({
  roleType: 'DEFAULT'
})

// 权限列表
const permissions = ref([])
const selectedIds = ref([])

// 批量添加对话框
const batchAddDialogVisible = ref(false)
const batchAddForm = reactive({
  resourceIds: []
})

// 资源树
const resourceTree = ref([])

// 加载权限列表
const loadPermissions = async () => {
  try {
    const res = await request.get(`/role-type-permissions/${filterForm.roleType}`)
    permissions.value = res.data || []
  } catch (error) {
    ElMessage.error('加载权限列表失败：' + error.message)
  }
}

// 加载资源树
const loadResourceTree = async () => {
  try {
    const res = await request.get('/resources/tree')
    resourceTree.value = res.data?.tree || []
  } catch (error) {
    ElMessage.error('加载资源树失败：' + error.message)
  }
}

// 资源类型标签颜色
const getResourceTypeTag = (type) => {
  const typeMap = {
    MODULE: 'info',
    MENU: 'warning',
    PAGE: 'success',
    ELEMENT: 'primary',
    API: 'danger',
    PERMISSION: 'info'
  }
  return typeMap[type] || 'info'
}

// 资源类型文本
const getResourceTypeText = (type) => {
  const typeMap = {
    MODULE: '模块',
    MENU: '菜单',
    PAGE: '页面',
    ELEMENT: '元素',
    API: '接口',
    PERMISSION: '权限'
  }
  return typeMap[type] || type
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

// 批量添加
const handleBatchAdd = () => {
  batchAddDialogVisible.value = true
}

// 提交批量添加
const submitBatchAdd = async () => {
  if (!batchAddForm.resourceIds || batchAddForm.resourceIds.length === 0) {
    ElMessage.warning('请选择至少一个资源')
    return
  }

  try {
    await request.post('/role-type-permissions', {
      roleType: filterForm.roleType,
      resourceIds: batchAddForm.resourceIds
    })
    ElMessage.success('添加成功')
    batchAddDialogVisible.value = false
    loadPermissions()
  } catch (error) {
    ElMessage.error('添加失败：' + error.message)
  }
}

// 删除
const handleDelete = (id) => {
  ElMessageBox.confirm('确认删除该缺省权限？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/role-type-permissions/${id}`)
      ElMessage.success('删除成功')
      loadPermissions()
    } catch (error) {
      ElMessage.error('删除失败：' + error.message)
    }
  })
}

// 批量删除
const handleBatchDelete = () => {
  ElMessageBox.confirm(`确认删除选中的 ${selectedIds.value.length} 项缺省权限？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.post('/role-type-permissions/batch-delete', {
        ids: selectedIds.value
      })
      ElMessage.success('删除成功')
      loadPermissions()
    } catch (error) {
      ElMessage.error('删除失败：' + error.message)
    }
  })
}

onMounted(() => {
  loadPermissions()
  loadResourceTree()
})
</script>

<style scoped>
.role-type-permissions {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.header-content h2 {
  margin: 0 0 10px 0;
  font-size: 20px;
}

.header-content p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.filter-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
