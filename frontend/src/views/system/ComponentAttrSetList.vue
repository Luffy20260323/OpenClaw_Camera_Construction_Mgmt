<template>
    <div class="component-attr-set-page">
      <!-- 页面标题 -->
      <div class="page-header">
        <h1>零部件属性集管理</h1>
        <p class="description">管理零部件属性集及其属性定义</p>
      </div>

      <!-- 操作按钮 -->
      <div class="action-bar">
        <el-button 
          type="primary" 
          @click="showCreateDialog"
          v-permission="['system:attr-set:create']"
        >
          <el-icon><Plus /></el-icon>
          新增属性集
        </el-button>
        <el-button @click="loadData">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>

      <!-- 搜索过滤 -->
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="零部件种类">
            <el-select 
              v-model="searchForm.componentTypeId" 
              placeholder="全部"
              clearable
              style="width: 200px"
              @change="loadData"
            >
              <el-option
                v-for="type in componentTypes"
                :key="type.id"
                :label="type.name"
                :value="type.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="属性集名称">
            <el-input 
              v-model="searchForm.name" 
              placeholder="请输入名称"
              clearable
              style="width: 200px"
              @keyup.enter="loadData"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadData">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 属性集列表表格 -->
      <div class="data-table">
        <el-table
          :data="tableData"
          v-loading="loading"
          border
          stripe
          style="width: 100%"
        >
          <el-table-column prop="id" label="ID" width="80" />
          
          <el-table-column prop="componentTypeName" label="零部件种类" width="150" />
          
          <el-table-column prop="name" label="属性集名称" min-width="180" />
          
          <el-table-column prop="code" label="属性集编码" width="150" />
          
          <el-table-column prop="attrCount" label="属性数量" width="100" align="center">
            <template #default="{ row }">
              <el-tag size="small">{{ row.attrCount || 0 }}</el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="sequenceNo" label="序号" width="100" align="center">
            <template #default="{ row }">
              <el-input-number
                v-model="row.sequenceNo"
                :min="1"
                :max="999"
                size="small"
                controls-position="right"
                @change="handleSequenceNoChange(row)"
              />
            </template>
          </el-table-column>
          
          <el-table-column prop="isActive" label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.isActive ? 'success' : 'info'">
                {{ row.isActive ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="description" label="描述" min-width="150" show-overflow-tooltip />
          
          <el-table-column label="操作" width="280" fixed="right">
            <template #default="{ row }">
              <el-button 
                type="primary" 
                size="small" 
                text 
                @click="showDetailDialog(row)"
                v-permission="['system:attr-set:view']"
              >
                详情
              </el-button>
              <el-button 
                type="primary" 
                size="small" 
                text 
                @click="showEditDialog(row)"
                v-permission="['system:attr-set:edit']"
              >
                编辑
              </el-button>
              <el-button 
                type="danger" 
                size="small" 
                text 
                @click="handleDelete(row)"
                v-permission="['system:attr-set:delete']"
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

      <!-- 新增/编辑属性集对话框 -->
      <el-dialog
        v-model="dialogVisible"
        :title="dialogType === 'create' ? '新增属性集' : '编辑属性集'"
        width="900px"
        @close="resetForm"
        destroy-on-close
      >
        <el-form 
          :model="form" 
          :rules="formRules" 
          ref="formRef" 
          label-width="120px"
        >
          <el-form-item label="零部件种类" prop="componentTypeId">
            <el-select 
              v-model="form.componentTypeId" 
              placeholder="请选择零部件种类"
              style="width: 100%"
              :disabled="dialogType === 'edit'"
            >
              <el-option
                v-for="type in componentTypes"
                :key="type.id"
                :label="type.name"
                :value="type.id"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="属性集名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入属性集名称" />
          </el-form-item>
          
          <el-form-item label="属性集编码" prop="code">
            <el-input v-model="form.code" placeholder="请输入属性集编码（如 HK_POLE_9M）" />
          </el-form-item>
          
          <el-form-item label="显示序号" prop="sequenceNo">
            <el-input-number v-model="form.sequenceNo" :min="1" :max="999" />
          </el-form-item>
          
          <el-form-item label="是否启用" prop="isActive">
            <el-switch v-model="form.isActive" />
          </el-form-item>
          
          <el-form-item label="描述" prop="description">
            <el-input 
              v-model="form.description" 
              type="textarea" 
              :rows="3"
              placeholder="请输入描述信息"
            />
          </el-form-item>

          <!-- 属性定义管理 -->
          <el-divider />
          <el-form-item label="属性定义">
            <div class="attr-definition-section">
              <div class="attr-toolbar">
                <el-button type="primary" size="small" @click="addAttribute">
                  <el-icon><Plus /></el-icon>
                  添加属性
                </el-button>
              </div>

              <el-table 
                :data="form.attrs" 
                border 
                size="small"
                style="margin-top: 10px"
              >
                <el-table-column label="序号" width="60" align="center">
                  <template #default="{ $index }">
                    {{ $index + 1 }}
                  </template>
                </el-table-column>
                
                <el-table-column label="属性名称" width="150">
                  <template #default="{ row, $index }">
                    <el-input v-model="row.attrName" placeholder="如：厂商" size="small" />
                  </template>
                </el-table-column>
                
                <el-table-column label="属性编码" width="150">
                  <template #default="{ row, $index }">
                    <el-input v-model="row.attrCode" placeholder="如：manufacturer" size="small" />
                  </template>
                </el-table-column>
                
                <el-table-column label="属性类型" width="120">
                  <template #default="{ row, $index }">
                    <el-select v-model="row.attrType" size="small" style="width: 100%">
                      <el-option label="文本" value="TEXT" />
                      <el-option label="数字" value="NUMBER" />
                      <el-option label="日期" value="DATE" />
                      <el-option label="单选" value="SELECT" />
                      <el-option label="多选" value="MULTI_SELECT" />
                      <el-option label="文件" value="FILE" />
                    </el-select>
                  </template>
                </el-table-column>
                
                <el-table-column label="单位" width="100">
                  <template #default="{ row, $index }">
                    <el-input v-model="row.unit" placeholder="如：米" size="small" />
                  </template>
                </el-table-column>
                
                <el-table-column label="默认值" width="120">
                  <template #default="{ row, $index }">
                    <el-input v-model="row.defaultValue" size="small" />
                  </template>
                </el-table-column>
                
                <el-table-column label="必填" width="60" align="center">
                  <template #default="{ row, $index }">
                    <el-checkbox v-model="row.isRequired" />
                  </template>
                </el-table-column>
                
                <el-table-column label="选项值" min-width="150">
                  <template #default="{ row, $index }">
                    <el-input 
                      v-if="['SELECT', 'MULTI_SELECT'].includes(row.attrType)"
                      v-model="row.optionsText" 
                      placeholder="用逗号分隔，如：选项 1，选项 2，选项 3"
                      size="small"
                    />
                    <span v-else class="text-gray">-</span>
                  </template>
                </el-table-column>
                
                <el-table-column label="操作" width="80" align="center">
                  <template #default="{ row, $index }">
                    <el-button 
                      type="danger" 
                      size="small" 
                      text 
                      @click="removeAttribute($index)"
                    >
                      删除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-form-item>
        </el-form>

        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitting">
            确定
          </el-button>
        </template>
      </el-dialog>

      <!-- 详情对话框 -->
      <el-dialog
        v-model="detailDialogVisible"
        title="属性集详情"
        width="900px"
      >
        <el-descriptions :column="2" border>
          <el-descriptions-item label="ID">{{ detailData.id }}</el-descriptions-item>
          <el-descriptions-item label="零部件种类">{{ detailData.componentTypeName }}</el-descriptions-item>
          <el-descriptions-item label="属性集名称">{{ detailData.name }}</el-descriptions-item>
          <el-descriptions-item label="属性集编码">{{ detailData.code }}</el-descriptions-item>
          <el-descriptions-item label="显示序号">{{ detailData.sequenceNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="detailData.isActive ? 'success' : 'info'">
              {{ detailData.isActive ? '启用' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">{{ detailData.description || '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-divider />
        <h3>属性定义列表</h3>
        <el-table :data="detailData.attrs" border stripe>
          <el-table-column prop="attrName" label="属性名称" width="120" />
          <el-table-column prop="attrCode" label="属性编码" width="150" />
          <el-table-column prop="attrType" label="类型" width="100">
            <template #default="{ row }">
              <el-tag size="small">{{ row.attrType }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="unit" label="单位" width="80" />
          <el-table-column prop="defaultValue" label="默认值" width="100" />
          <el-table-column label="必填" width="60" align="center">
            <template #default="{ row }">
              <el-icon v-if="row.isRequired" color="#67c23a"><Check /></el-icon>
            </template>
          </el-table-column>
          <el-table-column prop="optionsText" label="选项值" min-width="150" />
          <el-table-column prop="sequenceNo" label="序号" width="60" align="center" />
        </el-table>
      </el-dialog>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Check } from '@element-plus/icons-vue'
import { 
  getList, 
  getDetail, 
  create, 
  update, 
  remove, 
  getComponentTypes,
  updateSequenceNo 
} from '@/api/componentAttrSet'

// 加载状态
const loading = ref(false)
const submitting = ref(false)

// 搜索表单
const searchForm = reactive({
  componentTypeId: null,
  name: ''
})

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 表格数据
const tableData = ref([])

// 零部件种类列表
const componentTypes = ref([])

// 对话框
const dialogVisible = ref(false)
const dialogType = ref('create') // create | edit
const detailDialogVisible = ref(false)

// 表单数据
const formRef = ref(null)
const form = reactive({
  id: null,
  componentTypeId: null,
  name: '',
  code: '',
  sequenceNo: 10,
  isActive: true,
  description: '',
  attrs: []
})

// 表单验证规则
const formRules = {
  componentTypeId: [
    { required: true, message: '请选择零部件种类', trigger: 'change' }
  ],
  name: [
    { required: true, message: '请输入属性集名称', trigger: 'blur' },
    { max: 100, message: '长度不能超过 100 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入属性集编码', trigger: 'blur' },
    { max: 50, message: '长度不能超过 50 个字符', trigger: 'blur' }
  ],
  sequenceNo: [
    { required: true, message: '请输入显示序号', trigger: 'blur' }
  ]
}

// 详情数据
const detailData = ref({})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await getList({
      page: pagination.page,
      size: pagination.size,
      componentTypeId: searchForm.componentTypeId,
      name: searchForm.name
    })
    
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载零部件种类
const loadComponentTypes = async () => {
  try {
    const res = await getComponentTypes()
    componentTypes.value = res.data?.records || []
  } catch (error) {
    console.error('加载零部件种类失败:', error)
  }
}

// 重置搜索
const resetSearch = () => {
  searchForm.componentTypeId = null
  searchForm.name = ''
  pagination.page = 1
  loadData()
}

// 显示新增对话框
const showCreateDialog = () => {
  dialogType.value = 'create'
  dialogVisible.value = true
}

// 显示编辑对话框
const showEditDialog = async (row) => {
  dialogType.value = 'edit'
  try {
    const res = await getDetail(row.id)
    const data = res.data
    
    form.id = data.id
    form.componentTypeId = data.componentTypeId
    form.name = data.name
    form.code = data.code
    form.sequenceNo = data.sequenceNo
    form.isActive = data.isActive
    form.description = data.description || ''
    form.attrs = (data.attrs || []).map(attr => ({
      ...attr,
      optionsText: attr.options ? (Array.isArray(attr.options) ? attr.options.join(',') : attr.options) : ''
    }))
    
    dialogVisible.value = true
  } catch (error) {
    console.error('加载详情失败:', error)
  }
}

// 显示详情对话框
const showDetailDialog = async (row) => {
  try {
    const res = await getDetail(row.id)
    detailData.value = {
      ...res.data,
      attrs: (res.data.attrs || []).map(attr => ({
        ...attr,
        optionsText: attr.options ? (Array.isArray(attr.options) ? attr.options.join(',') : attr.options) : ''
      }))
    }
    detailDialogVisible.value = true
  } catch (error) {
    console.error('加载详情失败:', error)
  }
}

// 重置表单
const resetForm = () => {
  formRef.value?.resetFields()
  form.id = null
  form.componentTypeId = null
  form.name = ''
  form.code = ''
  form.sequenceNo = 10
  form.isActive = true
  form.description = ''
  form.attrs = []
}

// 添加属性
const addAttribute = () => {
  form.attrs.push({
    attrName: '',
    attrCode: '',
    attrType: 'TEXT',
    unit: '',
    defaultValue: '',
    isRequired: false,
    optionsText: '',
    sequenceNo: form.attrs.length * 10 + 10
  })
}

// 删除属性
const removeAttribute = (index) => {
  form.attrs.splice(index, 1)
}

// 提交表单
const submitForm = async () => {
  try {
    await formRef.value?.validate()
    
    // 转换属性定义
    const attrs = form.attrs.map((attr, index) => ({
      attrName: attr.attrName,
      attrCode: attr.attrCode,
      attrType: attr.attrType,
      unit: attr.unit,
      defaultValue: attr.defaultValue,
      isRequired: attr.isRequired,
      options: ['SELECT', 'MULTI_SELECT'].includes(attr.attrType) && attr.optionsText 
        ? attr.optionsText.split(',').map(s => s.trim()).filter(s => s)
        : null,
      sequenceNo: index * 10 + 10
    }))
    
    const data = {
      componentTypeId: form.componentTypeId,
      name: form.name,
      code: form.code,
      sequenceNo: form.sequenceNo,
      isActive: form.isActive,
      description: form.description,
      attrs
    }
    
    submitting.value = true
    
    if (dialogType.value === 'create') {
      await create(data)
      ElMessage.success('创建成功')
    } else {
      await update(form.id, data)
      ElMessage.success('更新成功')
    }
    
    dialogVisible.value = false
    loadData()
  } catch (error) {
    if (error !== false) { // 排除表单验证失败
      console.error('提交失败:', error)
    }
  } finally {
    submitting.value = false
  }
}

// 删除属性集
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除属性集"${row.name}"吗？`, '确认删除', {
      type: 'warning'
    })
    
    await remove(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 更新显示序号
const handleSequenceNoChange = async (row) => {
  try {
    await updateSequenceNo(row.id, row.sequenceNo)
    ElMessage.success('序号更新成功')
  } catch (error) {
    console.error('更新序号失败:', error)
    row.sequenceNo = row.sequenceNo // 恢复原值
  }
}

// 初始化
onMounted(() => {
  loadComponentTypes()
  loadData()
})
</script>

<style scoped>
.component-attr-set-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0 0 8px 0;
  font-size: 24px;
  color: #303133;
}

.page-header .description {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.action-bar {
  margin-bottom: 16px;
}

.search-bar {
  margin-bottom: 16px;
  background: #f5f7fa;
  padding: 16px;
  border-radius: 4px;
}

.data-table {
  background: #fff;
  padding: 16px;
  border-radius: 4px;
}

.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.attr-definition-section {
  width: 100%;
}

.attr-toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 8px;
}

.text-gray {
  color: #909399;
}
</style>
