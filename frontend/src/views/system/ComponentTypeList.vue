<template>
  <AdminLayout>
    <div class="component-type-page">
      <!-- 页面头部 -->
      <div class="page-header">
        <div class="header-left">
          <h1>🔧 零部件种类管理</h1>
          <p class="subtitle">管理监控杆、摄像头、光伏板等设备类型</p>
        </div>
        <div class="header-right">
          <el-button 
            type="primary" 
            @click="handleCreate"
            v-permission="'system:component:create'"
          >
            <el-icon><Plus /></el-icon>
            新增种类
          </el-button>
        </div>
      </div>

      <!-- 筛选栏 -->
      <div class="filter-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索种类名称或编码..."
          prefix-icon="Search"
          clearable
          style="width: 300px"
          @keyup.enter="handleSearch"
        />
        <el-select
          v-model="statusFilter"
          placeholder="状态筛选"
          clearable
          style="width: 150px; margin-left: 12px"
          @change="handleSearch"
        >
          <el-option label="启用" :value="true" />
          <el-option label="禁用" :value="false" />
        </el-select>
        <el-button type="primary" @click="handleSearch" style="margin-left: 12px">
          查询
        </el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>

      <!-- 表格区域 -->
      <div class="table-container" v-loading="loading">
        <el-table :data="tableData" border stripe style="width: 100%">
          <el-table-column type="index" label="序号" width="60" align="center" />
          
          <el-table-column prop="name" label="种类名称" min-width="150" />
          
          <el-table-column prop="code" label="种类编码" min-width="150" />
          
          <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
          
          <el-table-column prop="sequence_no" label="显示序号" width="100" align="center" />
          
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.is_active ? 'success' : 'danger'" size="small">
                {{ row.is_active ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="created_at" label="创建时间" width="180" />
          
          <el-table-column label="操作" width="280" fixed="right" align="center">
            <template #default="{ row }">
              <el-button
                type="primary"
                size="small"
                @click="handleEdit(row)"
                v-permission="'system:component:edit'"
              >
                编辑
              </el-button>
              
              <el-button
                size="small"
                @click="handleToggleStatus(row)"
                v-permission="'system:component:edit'"
              >
                {{ row.is_active ? '禁用' : '启用' }}
              </el-button>
              
              <el-button
                type="danger"
                size="small"
                @click="handleDelete(row)"
                v-permission="'system:component:delete'"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :page-sizes="[10, 20, 50, 100]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadData"
            @current-change="loadData"
          />
        </div>
      </div>

      <!-- 新增/编辑对话框 -->
      <el-dialog
        v-model="dialogVisible"
        :title="dialogMode === 'create' ? '新增零部件种类' : '编辑零部件种类'"
        width="500px"
        @close="handleDialogClose"
      >
        <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-width="100px"
        >
          <el-form-item label="种类名称" prop="name">
            <el-input v-model="formData.name" placeholder="如：监控杆、摄像头" />
          </el-form-item>
          
          <el-form-item label="种类编码" prop="code">
            <el-input v-model="formData.code" placeholder="如：MONITORING_POLE" :disabled="dialogMode === 'edit'" />
            <div class="form-tip">英文大写字母和下划线，编辑时不可修改</div>
          </el-form-item>
          
          <el-form-item label="描述" prop="description">
            <el-input
              v-model="formData.description"
              type="textarea"
              :rows="3"
              placeholder="简要描述该种类的用途"
            />
          </el-form-item>
          
          <el-form-item label="显示序号" prop="sequence_no">
            <el-input-number v-model="formData.sequence_no" :min="1" :max="9999" :step="10" />
            <div class="form-tip">数值越小越靠前，默认 10</div>
          </el-form-item>
          
          <el-form-item label="状态" prop="is_active">
            <el-switch v-model="formData.is_active" active-text="启用" inactive-text="禁用" />
          </el-form-item>
        </el-form>
        
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            确定
          </el-button>
        </template>
      </el-dialog>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import AdminLayout from '@/layouts/AdminLayout.vue'
import { getList, getDetail, create, update, remove, toggleStatus } from '@/api/componentType'

// 表格数据
const tableData = ref([])
const loading = ref(false)

// 搜索条件
const searchKeyword = ref('')
const statusFilter = ref(null)

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 对话框
const dialogVisible = ref(false)
const dialogMode = ref('create') // 'create' or 'edit'
const submitting = ref(false)
const formRef = ref(null)

// 表单数据
const formData = reactive({
  id: null,
  name: '',
  code: '',
  description: '',
  sequence_no: 10,
  is_active: true
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入种类名称', trigger: 'blur' },
    { max: 100, message: '长度不能超过 100 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入种类编码', trigger: 'blur' },
    { pattern: /^[A-Z_]+$/, message: '只能包含大写字母和下划线', trigger: 'blur' },
    { max: 50, message: '长度不能超过 50 个字符', trigger: 'blur' }
  ],
  sequence_no: [
    { required: true, message: '请输入显示序号', trigger: 'blur' }
  ]
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size
    }
    
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }
    
    if (statusFilter.value !== null) {
      params.is_active = statusFilter.value
    }
    
    const res = await getList(params)
    
    if (res.code === 200 && res.data) {
      tableData.value = res.data.items || res.data.list || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadData()
}

// 重置搜索
const resetSearch = () => {
  searchKeyword.value = ''
  statusFilter.value = null
  pagination.page = 1
  loadData()
}

// 新增
const handleCreate = () => {
  dialogMode.value = 'create'
  // 重置表单
  formData.id = null
  formData.name = ''
  formData.code = ''
  formData.description = ''
  formData.sequence_no = 10
  formData.is_active = true
  
  dialogVisible.value = true
}

// 编辑
const handleEdit = async (row) => {
  dialogMode.value = 'edit'
  
  try {
    const res = await getDetail(row.id)
    if (res.code === 200 && res.data) {
      const data = res.data
      formData.id = data.id
      formData.name = data.name
      formData.code = data.code
      formData.description = data.description || ''
      formData.sequence_no = data.sequence_no || 10
      formData.is_active = data.is_active !== false
      
      dialogVisible.value = true
    }
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败')
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      const data = {
        name: formData.name,
        code: formData.code,
        description: formData.description,
        sequence_no: formData.sequence_no,
        is_active: formData.is_active
      }
      
      let res
      if (dialogMode.value === 'create') {
        res = await create(data)
      } else {
        res = await update(formData.id, data)
      }
      
      if (res.code === 200) {
        ElMessage.success(dialogMode.value === 'create' ? '创建成功' : '更新成功')
        dialogVisible.value = false
        loadData()
      }
    } catch (error) {
      console.error('提交失败:', error)
      // 错误消息已在 request 拦截器中显示
    } finally {
      submitting.value = false
    }
  })
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除种类"${row.name}"吗？删除后不可恢复！`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const res = await remove(row.id)
    
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      // 错误消息已在 request 拦截器中显示
    }
  }
}

// 切换状态
const handleToggleStatus = async (row) => {
  const newStatus = !row.is_active
  const actionText = newStatus ? '启用' : '禁用'
  
  try {
    await ElMessageBox.confirm(
      `确定要${actionText}"${row.name}"吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const res = await toggleStatus(row.id, newStatus)
    
    if (res.code === 200) {
      ElMessage.success(`${actionText}成功`)
      row.is_active = newStatus
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('切换状态失败:', error)
      // 错误消息已在 request 拦截器中显示
    }
  }
}

// 关闭对话框
const handleDialogClose = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.component-type-page {
  height: 100%;
  
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    
    .header-left {
      h1 {
        margin: 0 0 8px 0;
        font-size: 24px;
        font-weight: 600;
        color: #303133;
      }
      
      .subtitle {
        margin: 0;
        font-size: 14px;
        color: #909399;
      }
    }
  }
  
  .filter-bar {
    display: flex;
    align-items: center;
    margin-bottom: 16px;
    padding: 16px;
    background: #fff;
    border-radius: 4px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  }
  
  .table-container {
    background: #fff;
    border-radius: 4px;
    padding: 16px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
    
    .pagination-container {
      margin-top: 16px;
      display: flex;
      justify-content: flex-end;
    }
  }
  
  .form-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
  }
}
</style>
