<template>
  <AdminLayout>
    <div class="component-instance-page">
      <!-- 页面头部 -->
      <div class="page-header">
        <div class="header-left">
          <h1>📦 零部件实例管理</h1>
          <p class="subtitle">管理具体零部件实例，包括序列号、状态等信息</p>
        </div>
        <div class="header-right">
          <el-button 
            type="primary" 
            @click="handleCreate"
            v-permission="'system:component-instance:create'"
          >
            <el-icon><Plus /></el-icon>
            新增实例
          </el-button>
        </div>
      </div>

      <!-- 筛选栏 -->
      <div class="filter-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索序列号..."
          prefix-icon="Search"
          clearable
          style="width: 250px"
          @keyup.enter="handleSearch"
        />
        <el-select
          v-model="typeFilter"
          placeholder="零部件种类"
          clearable
          style="width: 180px; margin-left: 12px"
          @change="handleSearch"
        >
          <el-option
            v-for="item in typeOptions"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
        <el-select
          v-model="statusFilter"
          placeholder="状态筛选"
          clearable
          style="width: 120px; margin-left: 12px"
          @change="handleSearch"
        >
          <el-option label="正常" value="normal" />
          <el-option label="已更换" value="replaced" />
          <el-option label="已报废" value="scrapped" />
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
          
          <el-table-column prop="component_type_name" label="种类" min-width="150" />
          
          <el-table-column prop="attr_set_instance_name" label="属性集实例" min-width="200" show-overflow-tooltip />
          
          <el-table-column prop="serial_number" label="序列号" min-width="180" />
          
          <el-table-column label="状态" width="120" align="center">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)" size="small">
                {{ getStatusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="created_at" label="创建时间" width="180" />
          
          <el-table-column label="操作" width="320" fixed="right" align="center">
            <template #default="{ row }">
              <el-button
                type="primary"
                size="small"
                @click="handleEdit(row)"
                v-permission="'system:component-instance:edit'"
              >
                编辑
              </el-button>
              
              <el-dropdown @command="(command) => handleStatusChange(row, command)" v-permission="'system:component-instance:edit'">
                <el-button size="small">
                  切换状态<el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="normal">正常</el-dropdown-item>
                    <el-dropdown-item command="replaced">已更换</el-dropdown-item>
                    <el-dropdown-item command="scrapped">已报废</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              
              <el-button
                type="danger"
                size="small"
                @click="handleDelete(row)"
                v-permission="'system:component-instance:delete'"
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
        :title="dialogMode === 'create' ? '新增零部件实例' : '编辑零部件实例'"
        width="550px"
        @close="handleDialogClose"
      >
        <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-width="120px"
        >
          <el-form-item label="零部件种类" prop="component_type_id">
            <el-select
              v-model="formData.component_type_id"
              placeholder="请选择零部件种类"
              style="width: 100%"
              @change="handleTypeChange"
            >
              <el-option
                v-for="item in typeOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="属性集实例" prop="attr_set_instance_id">
            <el-select
              v-model="formData.attr_set_instance_id"
              placeholder="请选择属性集实例（可选）"
              style="width: 100%"
              clearable
              :disabled="!formData.component_type_id"
            >
              <el-option
                v-for="item in attrSetInstanceOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
            <div class="form-tip">关联该种类下的属性集实例，可不选</div>
          </el-form-item>
          
          <el-form-item label="序列号" prop="serial_number">
            <el-input v-model="formData.serial_number" placeholder="请输入唯一序列号" />
          </el-form-item>
          
          <el-form-item label="状态" prop="status">
            <el-select v-model="formData.status" placeholder="请选择状态" style="width: 100%">
              <el-option label="正常" value="normal" />
              <el-option label="已更换" value="replaced" />
              <el-option label="已报废" value="scrapped" />
            </el-select>
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
import { Plus, Search, ArrowDown } from '@element-plus/icons-vue'
import AdminLayout from '@/layouts/AdminLayout.vue'
import { getList, getDetail, create, update, remove, updateStatus } from '@/api/componentInstance'
import { getList as getComponentTypeList } from '@/api/componentType'
import { getList as getAttrSetInstanceList } from '@/api/componentAttrSetInstance'

// 表格数据
const tableData = ref([])
const loading = ref(false)

// 搜索条件
const searchKeyword = ref('')
const typeFilter = ref(null)
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
  component_type_id: null,
  attr_set_instance_id: null,
  serial_number: '',
  status: 'normal'
})

// 下拉选项
const typeOptions = ref([])
const attrSetInstanceOptions = ref([])

// 表单验证规则
const formRules = {
  component_type_id: [
    { required: true, message: '请选择零部件种类', trigger: 'change' }
  ],
  serial_number: [
    { required: true, message: '请输入序列号', trigger: 'blur' },
    { max: 100, message: '长度不能超过 100 个字符', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 获取状态标签
const getStatusLabel = (status) => {
  const map = {
    normal: '正常',
    replaced: '已更换',
    scrapped: '已报废'
  }
  return map[status] || status
}

// 获取状态类型
const getStatusType = (status) => {
  const map = {
    normal: 'success',
    replaced: 'warning',
    scrapped: 'danger'
  }
  return map[status] || 'info'
}

// 加载零部件种类列表
const loadTypeOptions = async () => {
  try {
    const res = await getComponentTypeList({ page: 1, size: 100 })
    if (res.code === 200 && res.data) {
      typeOptions.value = res.data.items || res.data.list || []
    }
  } catch (error) {
    console.error('加载种类列表失败:', error)
  }
}

// 加载属性集实例列表
const loadAttrSetInstanceOptions = async (componentTypeId = null) => {
  try {
    const params = { page: 1, size: 100 }
    if (componentTypeId) {
      params.component_type_id = componentTypeId
    }
    const res = await getAttrSetInstanceList(params)
    if (res.code === 200 && res.data) {
      attrSetInstanceOptions.value = res.data.items || res.data.list || []
    }
  } catch (error) {
    console.error('加载属性集实例列表失败:', error)
  }
}

// 种类变化时加载对应的属性集实例
const handleTypeChange = (value) => {
  if (value) {
    loadAttrSetInstanceOptions(value)
  } else {
    attrSetInstanceOptions.value = []
    formData.attr_set_instance_id = null
  }
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
      params.serial_number = searchKeyword.value
    }
    
    if (typeFilter.value) {
      params.component_type_id = typeFilter.value
    }
    
    if (statusFilter.value) {
      params.status = statusFilter.value
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
  typeFilter.value = null
  statusFilter.value = null
  pagination.page = 1
  loadData()
}

// 新增
const handleCreate = () => {
  dialogMode.value = 'create'
  // 重置表单
  formData.id = null
  formData.component_type_id = null
  formData.attr_set_instance_id = null
  formData.serial_number = ''
  formData.status = 'normal'
  
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
      formData.component_type_id = data.component_type_id
      formData.attr_set_instance_id = data.attr_set_instance_id || null
      formData.serial_number = data.serial_number
      formData.status = data.status || 'normal'
      
      // 加载属性集实例选项
      if (data.component_type_id) {
        await loadAttrSetInstanceOptions(data.component_type_id)
      }
      
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
        component_type_id: formData.component_type_id,
        attr_set_instance_id: formData.attr_set_instance_id || null,
        serial_number: formData.serial_number,
        status: formData.status
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
      `确定要删除序列号为"${row.serial_number}"的实例吗？删除后不可恢复！`,
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
const handleStatusChange = async (row, newStatus) => {
  if (newStatus === row.status) {
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要将序列号"${row.serial_number}"的状态改为"${getStatusLabel(newStatus)}"吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const res = await updateStatus(row.id, newStatus)
    
    if (res.code === 200) {
      ElMessage.success('状态更新成功')
      row.status = newStatus
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
  loadTypeOptions()
  loadData()
})
</script>

<style scoped lang="scss">
.component-instance-page {
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
