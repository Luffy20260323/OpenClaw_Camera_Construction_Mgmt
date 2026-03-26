<template>
  <AdminLayout>
    <el-card class="box-card">
        <template #header>
          <div class="card-header">
            <span>作业区管理</span>
            <div style="display: flex; gap: 10px; align-items: center;">
              <el-tag v-if="!isSystemAdmin" type="info">👁️ 只读模式</el-tag>
              <el-button v-if="isSystemAdmin" type="primary" @click="showCreateDialog">
                <el-icon><Plus /></el-icon>
                新建作业区
              </el-button>
            </div>
          </div>
        </template>

      <!-- 搜索筛选 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="所属公司">
          <el-select v-model="searchForm.companyId" placeholder="全部" clearable style="width: 250px">
            <el-option
              v-for="company in companies"
              :key="company.id"
              :label="company.companyName"
              :value="company.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="作业区名称/负责人"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 作业区列表 -->
      <el-table :data="workAreaList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="workAreaName" label="作业区名称" min-width="180" />
        <el-table-column prop="workAreaCode" label="作业区编码" width="120" />
        <el-table-column prop="companyName" label="所属公司" min-width="180" />
        <el-table-column prop="leaderName" label="负责人" width="120" />
        <el-table-column prop="leaderPhone" label="联系电话" width="130" />
        <el-table-column prop="maxCapacity" label="最大容量" width="100" />
        <el-table-column label="系统保护" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.isSystemProtected" type="warning" effect="plain">
              🔒 保护
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button
              v-if="isSystemAdmin"
              size="small"
              @click="showEditDialog(scope.row)"
              :disabled="scope.row.isSystemProtected"
            >
              编辑
            </el-button>
            <el-button
              v-if="isSystemAdmin"
              size="small"
              type="danger"
              @click="handleDelete(scope.row)"
              :disabled="scope.row.isSystemProtected"
            >
              删除
            </el-button>
            <span v-else class="readonly-tip">只读</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
        style="margin-top: 20px; justify-content: flex-end"
      />

      <!-- 创建/编辑对话框 -->
      <el-dialog
        v-model="dialogVisible"
        :title="dialogTitle"
        width="600px"
        :close-on-click-modal="false"
        @closed="resetForm"
      >
        <el-form
          ref="workAreaFormRef"
          :model="workAreaForm"
          :rules="rules"
          label-width="120px"
        >
          <el-form-item label="作业区名称" prop="workAreaName">
            <el-input v-model="workAreaForm.workAreaName" placeholder="请输入作业区名称" />
          </el-form-item>
          
          <el-form-item label="作业区编码">
            <el-input v-model="workAreaForm.workAreaCode" placeholder="请输入作业区编码" />
          </el-form-item>
          
          <el-form-item label="所属公司" prop="companyId">
            <el-select v-model="workAreaForm.companyId" placeholder="请选择所属公司" style="width: 100%">
              <el-option
                v-for="company in companies"
                :key="company.id"
                :label="company.companyName"
                :value="company.id"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="负责人">
            <el-input v-model="workAreaForm.leaderName" placeholder="请输入负责人姓名" />
          </el-form-item>
          
          <el-form-item label="联系电话">
            <el-input v-model="workAreaForm.leaderPhone" placeholder="请输入联系电话" />
          </el-form-item>
          
          <el-form-item label="地理范围">
            <el-input v-model="workAreaForm.geographicRange" placeholder="请输入地理范围" type="textarea" :rows="2" />
          </el-form-item>
          
          <el-form-item label="最大容量">
            <el-input-number v-model="workAreaForm.maxCapacity" :min="1" :max="10000" style="width: 100%" />
          </el-form-item>
          
          <el-form-item label="描述">
            <el-input v-model="workAreaForm.description" placeholder="请输入描述" type="textarea" :rows="3" />
          </el-form-item>
        </el-form>
        
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
        </template>
      </el-dialog>
    </el-card>
  </AdminLayout>
</template>

<script setup>
import AdminLayout from '@/layouts/AdminLayout.vue'
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 权限验证：只有系统管理员可以访问
const checkPermissions = () => {
  // 确保用户信息已从 localStorage 加载
  if (!userStore.userInfo || Object.keys(userStore.userInfo).length === 0) {
    const savedUserInfo = localStorage.getItem('userInfo')
    if (savedUserInfo) {
      try {
        userStore.userInfo = JSON.parse(savedUserInfo)
      } catch (e) {
        console.error('解析用户信息失败:', e)
      }
    }
  }
  
  console.log('[WorkAreaList] checkPermissions - isLoggedIn:', userStore.isLoggedIn, 'companyTypeId:', userStore.companyTypeId)
  
  if (!userStore.isLoggedIn) {
    ElMessage.error('请先登录')
    router.push('/login')
    return false
  }
  if (userStore.companyTypeId !== 4) {
    ElMessage.warning('您没有权限访问此页面')
    router.push('/')
    return false
  }
  return true
}

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref('create')
const workAreaFormRef = ref(null)

// 权限判断
const isSystemAdmin = computed(() => {
  const val = userStore.companyTypeId === 4
  console.log('[WorkAreaList] isSystemAdmin:', val, 'companyTypeId:', userStore.companyTypeId)
  return val
})

const goBack = () => {
  router.push('/')
}

// 搜索表单
const searchForm = reactive({
  companyId: null,
  keyword: ''
})

// 分页
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 作业区列表
const workAreaList = ref([])

// 公司列表
const companies = ref([])

// 表单数据
const workAreaForm = reactive({
  id: null,
  workAreaName: '',
  workAreaCode: '',
  companyId: null,
  leaderName: '',
  leaderPhone: '',
  geographicRange: '',
  maxCapacity: 1000,
  description: ''
})

// 表单验证规则
const rules = {
  workAreaName: [
    { required: true, message: '请输入作业区名称', trigger: 'blur' }
  ],
  companyId: [
    { required: true, message: '请选择所属公司', trigger: 'change' }
  ]
}

// 获取公司列表（仅甲方公司）
const getCompanies = async () => {
  try {
    const res = await request({
      url: '/company',
      method: 'get',
      params: { pageNum: 1, pageSize: 100 }
    })
    // 只显示甲方公司（typeId=1）
    companies.value = res.data.records.filter(c => c.typeId === 1)
  } catch (error) {
    console.error('获取公司列表失败:', error)
  }
}

// 获取作业区列表
const getWorkAreaList = async () => {
  loading.value = true
  try {
    const res = await request({
      url: '/workarea',
      method: 'get',
      params: {
        pageNum: pagination.pageNum,
        pageSize: pagination.pageSize,
        ...searchForm
      }
    })
    workAreaList.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    console.error('获取作业区列表失败:', error)
    ElMessage.error('获取作业区列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  getWorkAreaList()
}

// 重置搜索
const resetSearch = () => {
  searchForm.companyId = null
  searchForm.keyword = ''
  handleSearch()
}

// 分页变化
const handleSizeChange = () => {
  getWorkAreaList()
}

const handlePageChange = () => {
  getWorkAreaList()
}

// 显示创建对话框
const showCreateDialog = () => {
  dialogMode.value = 'create'
  dialogVisible.value = true
}

// 显示编辑对话框
const showEditDialog = (row) => {
  dialogMode.value = 'edit'
  dialogVisible.value = true
  
  // 填充表单数据
  Object.assign(workAreaForm, {
    id: row.id,
    workAreaName: row.workAreaName,
    workAreaCode: row.workAreaCode || '',
    companyId: row.companyId,
    leaderName: row.leaderName || '',
    leaderPhone: row.leaderPhone || '',
    geographicRange: row.geographicRange || '',
    maxCapacity: row.maxCapacity || 1000,
    description: row.description || ''
  })
}

// 重置表单
const resetForm = () => {
  workAreaFormRef.value?.resetFields()
  Object.assign(workAreaForm, {
    id: null,
    workAreaName: '',
    workAreaCode: '',
    companyId: null,
    leaderName: '',
    leaderPhone: '',
    geographicRange: '',
    maxCapacity: 1000,
    description: ''
  })
}

// 提交表单
const handleSubmit = async () => {
  if (!workAreaFormRef.value) return
  
  await workAreaFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (dialogMode.value === 'create') {
          await request({
            url: '/workarea',
            method: 'post',
            data: workAreaForm
          })
          ElMessage.success('创建成功')
        } else {
          await request({
            url: `/workarea/${workAreaForm.id}`,
            method: 'put',
            data: workAreaForm
          })
          ElMessage.success('更新成功')
        }
        
        dialogVisible.value = false
        getWorkAreaList()
      } catch (error) {
        console.error('提交失败:', error)
      } finally {
        submitting.value = false
      }
    }
  })
}

// 删除作业区
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除作业区"${row.workAreaName}"吗？此操作不可恢复！`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await request({
        url: `/workarea/${row.id}`,
        method: 'delete'
      })
      ElMessage.success('删除成功')
      getWorkAreaList()
    } catch (error) {
      console.error('删除失败:', error)
    }
  }).catch(() => {})
}

// 计算对话框标题
const dialogTitle = dialogMode.value === 'create' ? '创建作业区' : '编辑作业区'

onMounted(() => {
  // 权限检查
  if (!checkPermissions()) {
    return
  }
  
  // 确保用户信息已加载
  if (!userStore.userInfo || Object.keys(userStore.userInfo).length === 0) {
    const savedUserInfo = localStorage.getItem('userInfo')
    if (savedUserInfo) {
      try {
        userStore.userInfo = JSON.parse(savedUserInfo)
      } catch (e) {
        console.error('解析用户信息失败:', e)
      }
    }
  }
  
  console.log('[WorkAreaList] onMounted - userInfo:', userStore.userInfo, 'companyTypeId:', userStore.companyTypeId)
  getCompanies()
  getWorkAreaList()
})
</script>

<style scoped lang="scss">
.workarea-management {
  min-height: 100vh;
  background: #f5f7fa;

  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    background: #fff;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    padding: 0 24px;
    margin-bottom: 20px;

    .header-left {
      display: flex;
      align-items: center;
      gap: 12px;
      color: #667eea;

      .app-title {
        font-size: 18px;
        font-weight: 600;
      }
    }
  }

  .main-content {
    padding: 0 24px 24px;

    .box-card {
      max-width: 1400px;
      margin: 0 auto;
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .header-actions {
        display: flex;
        align-items: center;
        gap: 10px;
        margin-left: auto;
      }
    }

    .search-form {
      margin-bottom: 20px;
    }

    .form-tip {
      font-size: 12px;
      color: #999;
      margin-top: 4px;
    }
  }
}

.footer {
  background: #fff;
  border-top: 1px solid #e4e7ed;
  padding: 12px 24px;
  text-align: center;
  color: #909399;
  font-size: 13px;
}

.footer p {
  margin: 0;
}
</style>
