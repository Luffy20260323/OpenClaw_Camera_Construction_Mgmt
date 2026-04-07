<template>
    <div class="component-attr-set-instance-list">
      <!-- 页面头部 -->
      <el-card class="header-card">
        <div class="page-header">
          <h2>属性集实例管理</h2>
          <p class="description">管理零部件属性集的具体实例，用于配置零部件的具体属性值</p>
        </div>
      </el-card>

      <el-card class="content-card">
        <!-- 工具栏 -->
        <div class="toolbar">
          <el-button 
            v-permission="['system:attr-instance:create']" 
            type="primary" 
            @click="handleCreate"
          >
            <el-icon><Plus /></el-icon>
            新增实例
          </el-button>
          
          <el-button @click="loadData">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
          
          <el-select
            v-model="filterForm.attrSetId"
            placeholder="选择属性集"
            clearable
            style="width: 200px; margin-left: 16px"
            @change="loadData"
          >
            <el-option
              v-for="item in attrSetOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
          
          <el-input
            v-model="filterForm.name"
            placeholder="搜索实例名称"
            clearable
            style="width: 250px; margin-left: 16px"
            @keyup.enter="loadData"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          
          <el-button type="primary" plain @click="loadData" style="margin-left: 8px">
            搜索
          </el-button>
        </div>

        <!-- 表格 -->
        <el-table
          :data="tableData"
          v-loading="loading"
          style="margin-top: 20px"
          border
          stripe
        >
          <el-table-column prop="id" label="ID" width="80" />
          
          <el-table-column prop="name" label="实例名称" min-width="200" />
          
          <el-table-column prop="attrSetName" label="属性集" min-width="150" />
          
          <el-table-column prop="componentTypeName" label="零部件种类" min-width="120" />
          
          <el-table-column label="属性值预览" min-width="300">
            <template #default="{ row }">
              <el-text type="info" size="small" truncated>
                {{ formatAttrValues(row.attrValues) }}
              </el-text>
            </template>
          </el-table-column>
          
          <el-table-column prop="createdBy" label="创建人" width="120" />
          
          <el-table-column label="创建时间" width="180">
            <template #default="{ row }">
              {{ formatDate(row.createdAt) }}
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <el-button
                v-permission="['system:attr-instance:view']"
                size="small"
                @click="handleView(row)"
              >
                查看
              </el-button>
              <el-button
                v-permission="['system:attr-instance:edit']"
                size="small"
                type="primary"
                @click="handleEdit(row)"
              >
                编辑
              </el-button>
              <el-button
                v-permission="['system:attr-instance:delete']"
                size="small"
                type="danger"
                @click="handleDelete(row)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadData"
            @current-change="loadData"
          />
        </div>
      </el-card>

      <!-- 新增/编辑对话框 -->
      <el-dialog
        v-model="dialogVisible"
        :title="dialogTitle"
        width="800px"
        destroy-on-close
        @closed="handleDialogClosed"
      >
        <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-width="120px"
        >
          <el-form-item label="属性集" prop="attrSetId">
            <el-select
              v-model="formData.attrSetId"
              placeholder="请选择属性集"
              style="width: 100%"
              :disabled="!!formData.id"
              @change="handleAttrSetChange"
            >
              <el-option
                v-for="item in attrSetOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="实例名称" prop="name">
            <el-input
              v-model="formData.name"
              placeholder="请输入实例名称"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
          
          <!-- 动态属性值表单 -->
          <el-divider content-position="left">属性值</el-divider>
          
          <el-form-item
            v-for="attr in currentAttrDefinitions"
            :key="attr.id"
            :label="attr.attrName"
            :prop="`attrValues.${attr.attrCode}`"
            :rules="getAttrRules(attr)"
          >
            <!-- 文本类型 -->
            <el-input
              v-if="attr.attrType === 'TEXT'"
              v-model="formData.attrValues[attr.attrCode]"
              :placeholder="`请输入${attr.attrName}`"
              :maxlength="500"
            />
            
            <!-- 数字类型 -->
            <el-input-number
              v-else-if="attr.attrType === 'NUMBER'"
              v-model="formData.attrValues[attr.attrCode]"
              :placeholder="`请输入${attr.attrName}`"
              :precision="2"
              :step="1"
              style="width: 100%"
            />
            
            <!-- 日期类型 -->
            <el-date-picker
              v-else-if="attr.attrType === 'DATE'"
              v-model="formData.attrValues[attr.attrCode]"
              type="date"
              :placeholder="`请选择${attr.attrName}`"
              value-format="YYYY-MM-DD"
              style="width: 100%"
            />
            
            <!-- 日期时间类型 -->
            <el-date-picker
              v-else-if="attr.attrType === 'DATETIME'"
              v-model="formData.attrValues[attr.attrCode]"
              type="datetime"
              :placeholder="`请选择${attr.attrName}`"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 100%"
            />
            
            <!-- 单选类型 -->
            <el-select
              v-else-if="attr.attrType === 'SELECT'"
              v-model="formData.attrValues[attr.attrCode]"
              :placeholder="`请选择${attr.attrName}`"
              style="width: 100%"
              clearable
            >
              <el-option
                v-for="opt in parseOptions(attr.options)"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </el-select>
            
            <!-- 多选类型 -->
            <el-select
              v-else-if="attr.attrType === 'MULTI_SELECT'"
              v-model="formData.attrValues[attr.attrCode]"
              :placeholder="`请选择${attr.attrName}`"
              style="width: 100%"
              multiple
              clearable
            >
              <el-option
                v-for="opt in parseOptions(attr.options)"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </el-select>
            
            <!-- 布尔类型 -->
            <el-switch
              v-else-if="attr.attrType === 'BOOLEAN'"
              v-model="formData.attrValues[attr.attrCode]"
              :active-value="true"
              :inactive-value="false"
            />
            
            <!-- 文件类型（预留） -->
            <el-upload
              v-else-if="attr.attrType === 'FILE'"
              action="/api/upload"
              :headers="uploadHeaders"
              :on-success="(res) => handleFileSuccess(res, attr.attrCode)"
              :limit="1"
            >
              <el-button type="primary">
                <el-icon><Upload /></el-icon>
                上传文件
              </el-button>
            </el-upload>
            
            <!-- 默认文本输入 -->
            <el-input
              v-else
              v-model="formData.attrValues[attr.attrCode]"
              :placeholder="`请输入${attr.attrName}`"
            />
            
            <!-- 单位显示 -->
            <span v-if="attr.unit" class="unit-label">{{ attr.unit }}</span>
            
            <!-- 必填标记 -->
            <span v-if="attr.isRequired" class="required-mark">（必填）</span>
            
            <!-- 默认值提示 -->
            <div v-if="attr.defaultValue" class="default-value-tip">
              默认值：{{ attr.defaultValue }}
            </div>
          </el-form-item>
        </el-form>
        
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
        </template>
      </el-dialog>

      <!-- 查看对话框 -->
      <el-dialog
        v-model="viewDialogVisible"
        title="查看实例详情"
        width="800px"
      >
        <el-descriptions :column="2" border>
          <el-descriptions-item label="ID">{{ viewData.id }}</el-descriptions-item>
          <el-descriptions-item label="实例名称">{{ viewData.name }}</el-descriptions-item>
          <el-descriptions-item label="属性集">{{ viewData.attrSetName }}</el-descriptions-item>
          <el-descriptions-item label="零部件种类">{{ viewData.componentTypeName }}</el-descriptions-item>
          <el-descriptions-item label="创建人">{{ viewData.createdBy }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDate(viewData.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ formatDate(viewData.updatedAt) }}</el-descriptions-item>
        </el-descriptions>
        
        <el-divider>属性值</el-divider>
        
        <el-descriptions :column="2" border>
          <el-descriptions-item
            v-for="attr in currentAttrDefinitions"
            :key="attr.id"
            :label="attr.attrName"
          >
            {{ formatAttrValue(viewData.attrValues?.[attr.attrCode], attr) }}
          </el-descriptions-item>
        </el-descriptions>
      </el-dialog>
    </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Refresh, Search, Upload } from '@element-plus/icons-vue'
import {
  getList,
  getDetail,
  create,
  update,
  remove,
  getAttrSetList,
  getAttrSetDetail
} from '@/api/componentAttrSetInstance'

// 表格数据
const loading = ref(false)
const tableData = ref([])

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 筛选表单
const filterForm = reactive({
  attrSetId: null,
  name: ''
})

// 属性集选项
const attrSetOptions = ref([])

// 对话框
const dialogVisible = ref(false)
const dialogTitle = computed(() => formData.id ? '编辑实例' : '新增实例')
const formRef = ref(null)
const submitting = ref(false)

// 当前属性集的属性定义
const currentAttrDefinitions = ref([])

// 表单数据
const formData = reactive({
  id: null,
  attrSetId: null,
  name: '',
  attrValues: {}
})

// 表单验证规则
const formRules = {
  attrSetId: [
    { required: true, message: '请选择属性集', trigger: 'change' }
  ],
  name: [
    { required: true, message: '请输入实例名称', trigger: 'blur' },
    { max: 200, message: '实例名称不能超过 200 个字符', trigger: 'blur' }
  ]
}

// 查看对话框
const viewDialogVisible = ref(false)
const viewData = ref({})

// 上传文件头
const uploadHeaders = computed(() => {
  const token = localStorage.getItem('accessToken')
  return {
    'Authorization': `Bearer ${token}`
  }
})

// 加载属性集列表
const loadAttrSetOptions = async () => {
  try {
    const res = await getAttrSetList()
    if (res.data) {
      attrSetOptions.value = res.data
    }
  } catch (error) {
    console.error('加载属性集列表失败:', error)
  }
}

// 加载表格数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      ...filterForm
    }
    const res = await getList(params)
    if (res.data) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 处理属性集变化
const handleAttrSetChange = async (attrSetId) => {
  if (!attrSetId) {
    currentAttrDefinitions.value = []
    formData.attrValues = {}
    return
  }
  
  try {
    const res = await getAttrSetDetail(attrSetId)
    if (res.data) {
      currentAttrDefinitions.value = res.data.attributes || []
      // 初始化属性值
      const initialValues = {}
      currentAttrDefinitions.value.forEach(attr => {
        if (attr.defaultValue) {
          initialValues[attr.attrCode] = attr.attrType === 'MULTI_SELECT' 
            ? (typeof attr.defaultValue === 'string' ? [attr.defaultValue] : attr.defaultValue)
            : attr.defaultValue
        } else {
          initialValues[attr.attrCode] = attr.attrType === 'MULTI_SELECT' ? [] : null
        }
      })
      formData.attrValues = initialValues
    }
  } catch (error) {
    console.error('加载属性定义失败:', error)
    ElMessage.error('加载属性定义失败')
  }
}

// 解析选项
const parseOptions = (options) => {
  if (!options) return []
  try {
    return typeof options === 'string' ? JSON.parse(options) : options
  } catch {
    return []
  }
}

// 获取属性验证规则
const getAttrRules = (attr) => {
  const rules = []
  if (attr.isRequired) {
    rules.push({ required: true, message: `请输入${attr.attrName}`, trigger: 'blur' })
  }
  return rules
}

// 处理文件上传成功
const handleFileSuccess = (response, attrCode) => {
  if (response.data && response.data.url) {
    formData.attrValues[attrCode] = response.data.url
    ElMessage.success('上传成功')
  }
}

// 格式化属性值显示
const formatAttrValues = (attrValues) => {
  if (!attrValues) return ''
  try {
    const entries = Object.entries(attrValues)
    if (entries.length === 0) return '无'
    return entries.slice(0, 3).map(([k, v]) => `${k}: ${v}`).join(', ') + (entries.length > 3 ? '...' : '')
  } catch {
    return ''
  }
}

// 格式化单个属性值
const formatAttrValue = (value, attr) => {
  if (value === null || value === undefined) return '-'
  if (attr.attrType === 'BOOLEAN') return value ? '是' : '否'
  if (attr.attrType === 'MULTI_SELECT' && Array.isArray(value)) return value.join(', ')
  if (attr.attrType === 'FILE') return `[文件] ${value}`
  return String(value)
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 新增
const handleCreate = () => {
  // 重置表单
  Object.assign(formData, {
    id: null,
    attrSetId: null,
    name: '',
    attrValues: {}
  })
  currentAttrDefinitions.value = []
  formRef.value?.clearValidate()
  dialogVisible.value = true
}

// 编辑
const handleEdit = async (row) => {
  try {
    const res = await getDetail(row.id)
    if (res.data) {
      const data = res.data
      Object.assign(formData, {
        id: data.id,
        attrSetId: data.attrSetId,
        name: data.name,
        attrValues: data.attrValues || {}
      })
      
      // 加载属性定义
      await handleAttrSetChange(data.attrSetId)
      
      dialogVisible.value = true
    }
  } catch (error) {
    console.error('加载详情失败:', error)
    ElMessage.error('加载详情失败')
  }
}

// 查看
const handleView = async (row) => {
  try {
    const res = await getDetail(row.id)
    if (res.data) {
      viewData.value = res.data
      // 加载属性定义用于展示
      await handleAttrSetChange(res.data.attrSetId)
      viewDialogVisible.value = true
    }
  } catch (error) {
    console.error('加载详情失败:', error)
    ElMessage.error('加载详情失败')
  }
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除实例"${row.name}"吗？`, '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await remove(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  await formRef.value.validate()
  
  submitting.value = true
  try {
    if (formData.id) {
      await update(formData.id, formData)
      ElMessage.success('更新成功')
    } else {
      await create(formData)
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error(formData.id ? '更新失败' : '创建失败')
  } finally {
    submitting.value = false
  }
}

// 对话框关闭后清理
const handleDialogClosed = () => {
  formRef.value?.resetFields()
  currentAttrDefinitions.value = []
}

// 初始化
onMounted(() => {
  loadAttrSetOptions()
  loadData()
})
</script>

<style scoped lang="scss">
.component-attr-set-instance-list {
  .header-card {
    margin-bottom: 20px;
    
    .page-header {
      h2 {
        margin: 0 0 8px 0;
        font-size: 20px;
        font-weight: 600;
      }
      
      .description {
        margin: 0;
        color: #909399;
        font-size: 14px;
      }
    }
  }
  
  .content-card {
    .toolbar {
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: 8px;
    }
    
    .pagination {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }
  
  .unit-label {
    margin-left: 8px;
    color: #909399;
    font-size: 12px;
  }
  
  .required-mark {
    color: #f56c6c;
    font-size: 12px;
    margin-left: 4px;
  }
  
  .default-value-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
  }
}
</style>
