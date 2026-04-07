<template>
    <div class="point-device-model-instance-page">
      <!-- 页面头部 -->
      <div class="page-header">
        <div class="header-left">
          <h1>📍 点位设备模型实例管理</h1>
          <p class="subtitle">管理基于设备模型创建的具体点位实例</p>
        </div>
        <div class="header-right">
          <el-button 
            type="primary" 
            @click="handleCreate"
            v-permission="'system:point-device-model-instance:create'"
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
          placeholder="搜索实例名称或位置..."
          prefix-icon="Search"
          clearable
          style="width: 280px"
          @keyup.enter="handleSearch"
        />
        <el-select
          v-model="modelFilter"
          placeholder="设备模型"
          clearable
          style="width: 200px; margin-left: 12px"
          @change="handleSearch"
        >
          <el-option
            v-for="model in modelOptions"
            :key="model.id"
            :label="model.name"
            :value="model.id"
          />
        </el-select>
        <el-select
          v-model="workAreaFilter"
          placeholder="作业区"
          clearable
          style="width: 180px; margin-left: 12px"
          @change="handleSearch"
        >
          <el-option
            v-for="area in workAreaOptions"
            :key="area.id"
            :label="area.name"
            :value="area.id"
          />
        </el-select>
        <el-button type="primary" @click="handleSearch" style="margin-left: 12px">
          查询
        </el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>

      <!-- 表格区域 -->
      <div class="table-container" v-loading="loading">
        <el-table :data="tableData" border stripe style="width: 100%">
          <el-table-column type="expand">
            <template #default="{ row }">
              <div class="expand-content">
                <h4>实例项列表</h4>
                <el-table :data="row.items || []" border size="small" style="margin-top: 12px">
                  <el-table-column prop="sequence_no" label="序号" width="80" align="center" />
                  <el-table-column prop="component_type_name" label="零部件种类" min-width="150" />
                  <el-table-column prop="component_instance_serial" label="零部件实例序列号" min-width="180" />
                  <el-table-column prop="quantity" label="数量" width="100" align="center" />
                  <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
                  <el-table-column label="操作" width="100" align="center" v-if="row.status === 'active'">
                    <template #default="scope">
                      <el-button
                        type="primary"
                        size="small"
                        @click="handleEditItem(row, scope.$index)"
                        v-permission="'system:point-device-model-instance:edit'"
                      >
                        编辑
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column type="index" label="序号" width="60" align="center" />
          
          <el-table-column prop="name" label="实例名称" min-width="180" />
          
          <el-table-column prop="model_name" label="设备模型" min-width="180" />
          
          <el-table-column prop="work_area_name" label="作业区" min-width="150" />
          
          <el-table-column prop="location" label="安装位置" min-width="200" show-overflow-tooltip />
          
          <el-table-column prop="item_count" label="实例项数量" width="100" align="center">
            <template #default="{ row }">
              <el-tag size="small" type="info">{{ row.item_count || 0 }}</el-tag>
            </template>
          </el-table-column>
          
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)" size="small">
                {{ getStatusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="created_at" label="创建时间" width="180" />
          
          <el-table-column label="操作" width="220" fixed="right" align="center">
            <template #default="{ row }">
              <el-button
                type="primary"
                size="small"
                @click="handleEdit(row)"
                v-permission="'system:point-device-model-instance:edit'"
              >
                编辑
              </el-button>
              
              <el-button
                type="danger"
                size="small"
                @click="handleDelete(row)"
                v-permission="'system:point-device-model-instance:delete'"
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
        :title="dialogMode === 'create' ? '新增点位设备模型实例' : '编辑点位设备模型实例'"
        width="900px"
        @close="handleDialogClose"
      >
        <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-width="120px"
        >
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="设备模型" prop="modelId">
                <el-select 
                  v-model="formData.modelId" 
                  placeholder="请选择设备模型" 
                  style="width: 100%"
                  @change="handleModelChange"
                  :disabled="dialogMode === 'edit'"
                >
                  <el-option
                    v-for="model in modelOptions"
                    :key="model.id"
                    :label="model.name"
                    :value="model.id"
                  />
                </el-select>
                <div class="form-tip" v-if="dialogMode === 'create'">选择模型后自动生成实例项</div>
              </el-form-item>
            </el-col>
            
            <el-col :span="12">
              <el-form-item label="作业区" prop="workAreaId">
                <el-select v-model="formData.workAreaId" placeholder="请选择作业区" style="width: 100%">
                  <el-option
                    v-for="area in workAreaOptions"
                    :key="area.id"
                    :label="area.name"
                    :value="area.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="实例名称" prop="name">
                <el-input v-model="formData.name" placeholder="如：1 号监控杆实例" />
              </el-form-item>
            </el-col>
            
            <el-col :span="12">
              <el-form-item label="安装位置" prop="location">
                <el-input v-model="formData.location" placeholder="如：东门入口处" />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-form-item label="描述" prop="description">
            <el-input
              v-model="formData.description"
              type="textarea"
              :rows="2"
              placeholder="简要描述该实例的用途"
            />
          </el-form-item>
          
          <el-form-item label="状态" prop="status" v-if="dialogMode === 'edit'">
            <el-select v-model="formData.status" placeholder="请选择状态" style="width: 100%">
              <el-option label="启用" value="active" />
              <el-option label="停用" value="inactive" />
            </el-select>
          </el-form-item>
          
          <el-divider />
          
          <el-form-item label="实例项配置">
            <div class="items-manager">
              <div class="items-header">
                <span>配置该实例包含的零部件（根据模型自动生成）</span>
              </div>
              
              <el-table :data="formData.items" border style="margin-top: 12px">
                <el-table-column label="序号" width="80" align="center">
                  <template #default="{ $index }">
                    <span>{{ $index + 1 }}</span>
                  </template>
                </el-table-column>
                
                <el-table-column label="零部件种类" min-width="180">
                  <template #default="{ $index }">
                    <span>{{ formData.items[$index].componentTypeName || '-' }}</span>
                  </template>
                </el-table-column>
                
                <el-table-column label="要求数量" width="100" align="center">
                  <template #default="{ $index }">
                    <span>{{ formData.items[$index].quantity || 1 }}</span>
                  </template>
                </el-table-column>
                
                <el-table-column label="零部件实例" min-width="200">
                  <template #default="{ $index }">
                    <el-select
                      v-model="formData.items[$index].componentInstanceId"
                      placeholder="请选择零部件实例"
                      size="small"
                      style="width: 100%"
                      clearable
                      :disabled="dialogMode === 'create'"
                    >
                      <el-option
                        v-for="instance in getComponentInstanceOptions(formData.items[$index].componentTypeId)"
                        :key="instance.id"
                        :label="instance.serial_number"
                        :value="instance.id"
                      />
                    </el-select>
                  </template>
                </el-table-column>
                
                <el-table-column label="备注" min-width="150">
                  <template #default="{ $index }">
                    <el-input
                      v-model="formData.items[$index].remark"
                      placeholder="可选备注"
                      size="small"
                    />
                  </template>
                </el-table-column>
              </el-table>
              
              <div class="items-footer" v-if="formData.items.length === 0">
                <el-empty description="暂无实例项，请先选择设备模型" :image-size="80" />
              </div>
            </div>
          </el-form-item>
        </el-form>
        
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            确定
          </el-button>
        </template>
      </el-dialog>

      <!-- 编辑实例项对话框 -->
      <el-dialog
        v-model="editItemDialogVisible"
        title="编辑实例项"
        width="500px"
        @close="handleEditItemDialogClose"
      >
        <el-form
          ref="editItemFormRef"
          :model="editItemData"
          :rules="editItemRules"
          label-width="120px"
        >
          <el-form-item label="零部件种类">
            <span>{{ currentEditItem.componentTypeName }}</span>
          </el-form-item>
          
          <el-form-item label="要求数量">
            <span>{{ currentEditItem.quantity }}</span>
          </el-form-item>
          
          <el-form-item label="零部件实例" prop="componentInstanceId">
            <el-select
              v-model="editItemData.componentInstanceId"
              placeholder="请选择零部件实例"
              style="width: 100%"
              clearable
            >
              <el-option
                v-for="instance in getComponentInstanceOptions(currentEditItem.componentTypeId)"
                :key="instance.id"
                :label="instance.serial_number"
                :value="instance.id"
              />
            </el-select>
            <div class="form-tip">选择已创建的零部件实例进行关联</div>
          </el-form-item>
          
          <el-form-item label="备注" prop="remark">
            <el-input
              v-model="editItemData.remark"
              type="textarea"
              :rows="3"
              placeholder="可选备注信息"
            />
          </el-form-item>
        </el-form>
        
        <template #footer>
          <el-button @click="editItemDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleEditItemSubmit" :loading="editItemSubmitting">
            确定
          </el-button>
        </template>
      </el-dialog>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { 
  getList, 
  getDetail, 
  create, 
  update, 
  remove, 
  updateItem,
  getModelOptions,
  getWorkAreaOptions
} from '@/api/pointDeviceModelInstance'
import { getList as getComponentInstanceList } from '@/api/componentInstance'

// 表格数据
const tableData = ref([])
const loading = ref(false)

// 搜索条件
const searchKeyword = ref('')
const modelFilter = ref(null)
const workAreaFilter = ref(null)

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

// 编辑实例项对话框
const editItemDialogVisible = ref(false)
const editItemSubmitting = ref(false)
const editItemFormRef = ref(null)
const currentEditInstance = ref(null)
const currentEditItem = ref({})

// 表单数据
const formData = reactive({
  id: null,
  modelId: null,
  workAreaId: null,
  name: '',
  location: '',
  description: '',
  status: 'active',
  items: []
})

// 编辑实例项数据
const editItemData = reactive({
  componentInstanceId: null,
  remark: ''
})

// 下拉选项
const modelOptions = ref([])
const workAreaOptions = ref([])
const componentInstanceOptions = ref([])

// 表单验证规则
const formRules = {
  modelId: [
    { required: true, message: '请选择设备模型', trigger: 'change' }
  ],
  workAreaId: [
    { required: true, message: '请选择作业区', trigger: 'change' }
  ],
  name: [
    { required: true, message: '请输入实例名称', trigger: 'blur' },
    { max: 100, message: '长度不能超过 100 个字符', trigger: 'blur' }
  ],
  location: [
    { required: true, message: '请输入安装位置', trigger: 'blur' },
    { max: 200, message: '长度不能超过 200 个字符', trigger: 'blur' }
  ]
}

// 编辑实例项验证规则
const editItemRules = {
  componentInstanceId: [
    { required: true, message: '请选择零部件实例', trigger: 'change' }
  ]
}

// 获取状态标签
const getStatusLabel = (status) => {
  const map = {
    active: '启用',
    inactive: '停用'
  }
  return map[status] || status
}

// 获取状态类型
const getStatusType = (status) => {
  const map = {
    active: 'success',
    inactive: 'info'
  }
  return map[status] || 'info'
}

// 加载设备模型选项
const loadModelOptions = async () => {
  try {
    const res = await getModelOptions()
    if (res.code === 200 && res.data) {
      modelOptions.value = res.data.items || res.data.list || []
    }
  } catch (error) {
    console.error('加载设备模型选项失败:', error)
  }
}

// 加载作业区选项
const loadWorkAreaOptions = async () => {
  try {
    const res = await getWorkAreaOptions()
    if (res.code === 200 && res.data) {
      workAreaOptions.value = res.data.items || res.data.list || []
    }
  } catch (error) {
    console.error('加载作业区选项失败:', error)
  }
}

// 加载零部件实例选项
const loadComponentInstanceOptions = async () => {
  try {
    const res = await getComponentInstanceList({ page: 1, size: 1000, status: 'normal' })
    if (res.code === 200 && res.data) {
      componentInstanceOptions.value = res.data.items || res.data.list || []
    }
  } catch (error) {
    console.error('加载零部件实例选项失败:', error)
  }
}

// 根据零部件种类 ID 获取选项
const getComponentInstanceOptions = (componentTypeId) => {
  if (!componentTypeId) return []
  return componentInstanceOptions.value.filter(
    item => item.component_type_id === componentTypeId
  )
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
    
    if (modelFilter.value) {
      params.modelId = modelFilter.value
    }
    
    if (workAreaFilter.value) {
      params.workAreaId = workAreaFilter.value
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
  modelFilter.value = null
  workAreaFilter.value = null
  pagination.page = 1
  loadData()
}

// 新增
const handleCreate = () => {
  dialogMode.value = 'create'
  // 重置表单
  formData.id = null
  formData.modelId = null
  formData.workAreaId = null
  formData.name = ''
  formData.location = ''
  formData.description = ''
  formData.status = 'active'
  formData.items = []
  
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
      formData.modelId = data.modelId
      formData.workAreaId = data.workAreaId
      formData.name = data.name
      formData.location = data.location || ''
      formData.description = data.description || ''
      formData.status = data.status || 'active'
      formData.items = (data.items || []).map(item => ({
        id: item.id,
        componentTypeId: item.componentTypeId,
        componentTypeName: item.componentTypeName,
        quantity: item.quantity || 1,
        componentInstanceId: item.componentInstanceId,
        remark: item.remark || ''
      }))
      
      dialogVisible.value = true
    }
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败')
  }
}

// 设备模型变化时自动生成实例项
const handleModelChange = async (modelId) => {
  if (!modelId) {
    formData.items = []
    return
  }
  
  try {
    const res = await getDetail(0) // 这里应该调用获取模型详情的接口，但 API 中只有实例详情
    // 临时方案：从 modelOptions 中找到模型信息
    const selectedModel = modelOptions.value.find(m => m.id === modelId)
    if (selectedModel && selectedModel.items) {
      formData.items = selectedModel.items.map(item => ({
        componentTypeId: item.componentTypeId,
        componentTypeName: item.componentTypeName,
        quantity: item.quantity || 1,
        componentInstanceId: null,
        remark: ''
      }))
    }
  } catch (error) {
    console.error('加载模型项失败:', error)
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
        modelId: formData.modelId,
        workAreaId: formData.workAreaId,
        name: formData.name,
        location: formData.location,
        description: formData.description,
        status: formData.status,
        items: formData.items.map(item => ({
          componentTypeId: item.componentTypeId,
          componentInstanceId: item.componentInstanceId || null,
          quantity: item.quantity,
          remark: item.remark
        }))
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
      `确定要删除实例"${row.name}"吗？删除后不可恢复！`,
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

// 编辑实例项
const handleEditItem = (row, itemIndex) => {
  currentEditInstance.value = row
  const item = row.items[itemIndex]
  currentEditItem.value = {
    id: item.id,
    index: itemIndex,
    componentTypeId: item.componentTypeId,
    componentTypeName: item.componentTypeName,
    quantity: item.quantity,
    componentInstanceId: item.componentInstanceId,
    remark: item.remark
  }
  
  editItemData.componentInstanceId = item.componentInstanceId
  editItemData.remark = item.remark || ''
  
  editItemDialogVisible.value = true
}

// 提交编辑实例项
const handleEditItemSubmit = async () => {
  if (!editItemFormRef.value) return
  
  await editItemFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    editItemSubmitting.value = true
    try {
      const data = {
        componentInstanceId: editItemData.componentInstanceId,
        remark: editItemData.remark
      }
      
      const res = await updateItem(
        currentEditInstance.value.id,
        currentEditItem.value.id,
        data
      )
      
      if (res.code === 200) {
        ElMessage.success('更新成功')
        editItemDialogVisible.value = false
        // 刷新当前行数据
        loadData()
      }
    } catch (error) {
      console.error('更新实例项失败:', error)
      // 错误消息已在 request 拦截器中显示
    } finally {
      editItemSubmitting.value = false
    }
  })
}

// 关闭对话框
const handleDialogClose = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 关闭编辑实例项对话框
const handleEditItemDialogClose = () => {
  if (editItemFormRef.value) {
    editItemFormRef.value.resetFields()
  }
  currentEditInstance.value = null
  currentEditItem.value = {}
}

// 组件挂载时加载数据
onMounted(() => {
  loadModelOptions()
  loadWorkAreaOptions()
  loadComponentInstanceOptions()
  loadData()
})
</script>

<style scoped lang="scss">
.point-device-model-instance-page {
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
    
    .expand-content {
      padding: 16px;
      
      h4 {
        margin: 0 0 12px 0;
        font-size: 14px;
        color: #606266;
      }
    }
  }
  
  .form-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
  }
  
  .items-manager {
    width: 100%;
    
    .items-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      span {
        font-size: 14px;
        color: #606266;
      }
    }
    
    .items-footer {
      padding: 20px;
      text-align: center;
    }
  }
}
</style>
