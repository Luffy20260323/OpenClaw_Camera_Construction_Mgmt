<template>
    <div class="point-device-model-page">
      <!-- 页面头部 -->
      <div class="page-header">
        <div class="header-left">
          <h1>📍 点位设备模型管理</h1>
          <p class="subtitle">管理监控点位的设备配置模型</p>
        </div>
        <div class="header-right">
          <el-button 
            type="primary" 
            @click="handleCreate"
            v-permission="'system:point-device-model:create'"
          >
            <el-icon><Plus /></el-icon>
            新增模型
          </el-button>
        </div>
      </div>

      <!-- 筛选栏 -->
      <div class="filter-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索模型名称或编码..."
          prefix-icon="Search"
          clearable
          style="width: 300px"
          @keyup.enter="handleSearch"
        />
        <el-select
          v-model="componentTypeFilter"
          placeholder="零部件种类"
          clearable
          style="width: 200px; margin-left: 12px"
          @change="handleSearch"
        >
          <el-option
            v-for="type in componentTypes"
            :key="type.id"
            :label="type.name"
            :value="type.id"
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
                <h4>模型项列表</h4>
                <el-table :data="row.items || []" border size="small" style="margin-top: 12px">
                  <el-table-column prop="sequence_no" label="序号" width="80" align="center" />
                  <el-table-column prop="component_type_name" label="零部件种类" min-width="150" />
                  <el-table-column prop="quantity" label="数量" width="100" align="center" />
                  <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
                </el-table>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column type="index" label="序号" width="60" align="center" />
          
          <el-table-column prop="name" label="模型名称" min-width="180" />
          
          <el-table-column prop="code" label="模型编码" min-width="150" />
          
          <el-table-column prop="component_type_name" label="零部件种类" min-width="150" />
          
          <el-table-column prop="item_count" label="模型项数量" width="100" align="center">
            <template #default="{ row }">
              <el-tag size="small" type="info">{{ row.item_count || 0 }}</el-tag>
            </template>
          </el-table-column>
          
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
                v-permission="'system:point-device-model:edit'"
              >
                编辑
              </el-button>
              
              <el-button
                type="danger"
                size="small"
                @click="handleDelete(row)"
                v-permission="'system:point-device-model:delete'"
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
        :title="dialogMode === 'create' ? '新增点位设备模型' : '编辑点位设备模型'"
        width="800px"
        @close="handleDialogClose"
      >
        <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-width="120px"
        >
          <el-form-item label="模型名称" prop="name">
            <el-input v-model="formData.name" placeholder="如：标准监控杆模型 A" />
          </el-form-item>
          
          <el-form-item label="模型编码" prop="code">
            <el-input v-model="formData.code" placeholder="如：STD_MONITOR_POLE_A" :disabled="dialogMode === 'edit'" />
            <div class="form-tip">英文大写字母和下划线，编辑时不可修改</div>
          </el-form-item>
          
          <el-form-item label="零部件种类" prop="componentTypeId">
            <el-select v-model="formData.componentTypeId" placeholder="请选择零部件种类" style="width: 100%">
              <el-option
                v-for="type in componentTypes"
                :key="type.id"
                :label="type.name"
                :value="type.id"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="描述" prop="description">
            <el-input
              v-model="formData.description"
              type="textarea"
              :rows="3"
              placeholder="简要描述该模型的用途"
            />
          </el-form-item>
          
          <el-form-item label="显示序号" prop="sequence_no">
            <el-input-number v-model="formData.sequence_no" :min="1" :max="9999" :step="10" />
            <div class="form-tip">数值越小越靠前，默认 10</div>
          </el-form-item>
          
          <el-form-item label="状态" prop="is_active">
            <el-switch v-model="formData.is_active" active-text="启用" inactive-text="禁用" />
          </el-form-item>
          
          <el-divider />
          
          <el-form-item label="模型项配置">
            <div class="items-manager">
              <div class="items-header">
                <span>配置该模型包含的零部件及数量</span>
                <el-button type="primary" size="small" @click="addItem">
                  <el-icon><Plus /></el-icon>
                  添加模型项
                </el-button>
              </div>
              
              <el-table :data="formData.items" border style="margin-top: 12px">
                <el-table-column label="序号" width="80" align="center">
                  <template #default="{ $index }">
                    <span>{{ $index + 1 }}</span>
                  </template>
                </el-table-column>
                
                <el-table-column label="零部件种类" min-width="200">
                  <template #default="{ $index }">
                    <el-select
                      v-model="formData.items[$index].componentTypeId"
                      placeholder="请选择"
                      size="small"
                      style="width: 100%"
                      @change="handleItemComponentTypeChange($index)"
                    >
                      <el-option
                        v-for="type in componentTypes"
                        :key="type.id"
                        :label="type.name"
                        :value="type.id"
                      />
                    </el-select>
                  </template>
                </el-table-column>
                
                <el-table-column label="数量" width="120" align="center">
                  <template #default="{ $index }">
                    <el-input-number
                      v-model="formData.items[$index].quantity"
                      :min="1"
                      :max="9999"
                      size="small"
                      controls-position="right"
                    />
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
                
                <el-table-column label="操作" width="80" align="center">
                  <template #default="{ $index }">
                    <el-button
                      type="danger"
                      size="small"
                      text
                      @click="removeItem($index)"
                    >
                      删除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
              
              <div class="items-footer" v-if="formData.items.length === 0">
                <el-empty description="暂无模型项，请添加" :image-size="80" />
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
  getComponentTypes
} from '@/api/pointDeviceModel'

// 表格数据
const tableData = ref([])
const loading = ref(false)

// 零部件种类列表
const componentTypes = ref([])

// 搜索条件
const searchKeyword = ref('')
const componentTypeFilter = ref(null)

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
  componentTypeId: null,
  description: '',
  sequence_no: 10,
  is_active: true,
  items: []
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入模型名称', trigger: 'blur' },
    { max: 100, message: '长度不能超过 100 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入模型编码', trigger: 'blur' },
    { pattern: /^[A-Z_]+$/, message: '只能包含大写字母和下划线', trigger: 'blur' },
    { max: 50, message: '长度不能超过 50 个字符', trigger: 'blur' }
  ],
  componentTypeId: [
    { required: true, message: '请选择零部件种类', trigger: 'change' }
  ],
  sequence_no: [
    { required: true, message: '请输入显示序号', trigger: 'blur' }
  ]
}

// 加载零部件种类列表
const loadComponentTypes = async () => {
  try {
    const res = await getComponentTypes()
    if (res.code === 200 && res.data) {
      componentTypes.value = res.data.items || res.data.list || []
    }
  } catch (error) {
    console.error('加载零部件种类失败:', error)
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
      params.keyword = searchKeyword.value
    }
    
    if (componentTypeFilter.value) {
      params.componentTypeId = componentTypeFilter.value
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
  componentTypeFilter.value = null
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
  formData.componentTypeId = null
  formData.description = ''
  formData.sequence_no = 10
  formData.is_active = true
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
      formData.name = data.name
      formData.code = data.code
      formData.componentTypeId = data.componentTypeId
      formData.description = data.description || ''
      formData.sequence_no = data.sequence_no || 10
      formData.is_active = data.is_active !== false
      formData.items = (data.items || []).map(item => ({
        componentTypeId: item.componentTypeId,
        quantity: item.quantity || 1,
        remark: item.remark || ''
      }))
      
      dialogVisible.value = true
    }
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败')
  }
}

// 添加模型项
const addItem = () => {
  formData.items.push({
    componentTypeId: null,
    quantity: 1,
    remark: ''
  })
}

// 删除模型项
const removeItem = (index) => {
  formData.items.splice(index, 1)
}

// 处理模型项零部件种类变化
const handleItemComponentTypeChange = (index) => {
  const selectedItem = formData.items[index]
  const selectedType = componentTypes.value.find(t => t.id === selectedItem.componentTypeId)
  if (selectedType) {
    // 可以在这里添加额外逻辑
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    // 验证模型项
    if (formData.items.length === 0) {
      ElMessage.warning('请至少添加一个模型项')
      return
    }
    
    for (let i = 0; i < formData.items.length; i++) {
      if (!formData.items[i].componentTypeId) {
        ElMessage.warning(`请为第 ${i + 1} 个模型项选择零部件种类`)
        return
      }
    }
    
    submitting.value = true
    try {
      const data = {
        name: formData.name,
        code: formData.code,
        componentTypeId: formData.componentTypeId,
        description: formData.description,
        sequence_no: formData.sequence_no,
        is_active: formData.is_active,
        items: formData.items.map(item => ({
          componentTypeId: item.componentTypeId,
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
      `确定要删除模型"${row.name}"吗？删除后不可恢复！`,
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

// 关闭对话框
const handleDialogClose = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadComponentTypes()
  loadData()
})
</script>

<style scoped lang="scss">
.point-device-model-page {
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
