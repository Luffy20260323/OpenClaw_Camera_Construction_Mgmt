<template>
  <AdminLayout>
    <el-card class="box-card">
        <template #header>
          <div class="card-header">
            <span>公司管理</span>
            <el-button 
              v-if="canManageCompany"
              type="primary" 
              @click="showCreateDialog"
            >
              <el-icon><Plus /></el-icon>
              新建公司
            </el-button>
            <el-tag v-if="!canManageCompany" type="info">👁️ 只读模式</el-tag>
          </div>
        </template>

      <!-- 搜索筛选 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="公司类型">
          <el-select v-model="searchForm.typeId" placeholder="全部" clearable style="width: 200px">
            <el-option
              v-for="type in companyTypes"
              :key="type.id"
              :label="type.typeName"
              :value="type.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="公司名称/联系人"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 150px">
            <el-option label="正常" value="active" />
            <el-option label="禁用" value="inactive" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 公司列表 -->
      <el-table :data="companyList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="companyName" label="公司名称" min-width="200" />
        <el-table-column prop="typeName" label="公司类型" width="150">
          <template #default="scope">
            <el-tag :type="getTypeTag(scope.row.typeId)">
              {{ scope.row.typeName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="contactPerson" label="联系人" width="120" />
        <el-table-column prop="contactPhone" label="联系电话" width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'active' ? 'success' : 'danger'">
              {{ scope.row.status === 'active' ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="系统保护" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.isSystemProtected" type="warning" effect="plain">
              🔒 保护
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="允许匿名注册" width="120">
          <template #default="scope">
            <el-tag v-if="scope.row.allowAnonymousRegister" type="success" effect="plain">
              ✓ 允许
            </el-tag>
            <el-tag v-else type="info" effect="plain">
              ✗ 禁止
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button
              v-if="canManageCompany"
              type="primary"
              size="small"
              @click="showEditDialog(scope.row)"
              :disabled="scope.row.isSystemProtected"
            >
              编辑
            </el-button>
            <el-button
              v-if="canManageCompany && !scope.row.isSystemProtected"
              type="danger"
              size="small"
              @click="handleDelete(scope.row)"
            >
              删除
            </el-button>
            <span v-if="!canManageCompany" style="color: #999;">只读</span>
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
onMounted(() => {
  if (!userStore.isLoggedIn) {
    ElMessage.error('请先登录')
    router.push('/login')
    return
  }
  if (userStore.companyTypeId !== 4) {
    ElMessage.warning('您没有权限访问此页面')
    router.push('/')
    return
  }
})

// 判断是否为系统管理员
const canManageCompany = computed(() => {
  return userStore.companyTypeId === 4
})

const goBack = () => {
  router.push('/')
}

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref('create') // create | edit
const companyFormRef = ref(null)

// 搜索表单
const searchForm = reactive({
  typeId: null,
  keyword: '',
  status: ''
})

// 分页
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 公司列表
const companyList = ref([])

// 公司类型
const companyTypes = ref([])

// 表单数据
const companyForm = reactive({
  id: null,
  companyName: '',
  typeId: null,
  unifiedSocialCreditCode: '',
  contactPerson: '',
  contactPhone: '',
  contactEmail: '',
  address: '',
  description: '',
  status: 'active',
  allowAnonymousRegister: false,
  isSystemProtected: false
})

// 表单验证规则
const rules = {
  companyName: [
    { required: true, message: '请输入公司名称', trigger: 'blur' }
  ],
  typeId: [
    { required: true, message: '请选择公司类型', trigger: 'change' }
  ]
}

// 获取公司类型
const getCompanyTypes = async () => {
  try {
    const res = await request({
      url: '/company/types',
      method: 'get'
    })
    companyTypes.value = res.data
  } catch (error) {
    console.error('获取公司类型失败:', error)
  }
}

// 获取公司列表
const getCompanyList = async () => {
  loading.value = true
  try {
    const res = await request({
      url: '/company',
      method: 'get',
      params: {
        pageNum: pagination.pageNum,
        pageSize: pagination.pageSize,
        ...searchForm
      }
    })
    companyList.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    console.error('获取公司列表失败:', error)
    ElMessage.error('获取公司列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  getCompanyList()
}

// 重置搜索
const resetSearch = () => {
  searchForm.typeId = null
  searchForm.keyword = ''
  searchForm.status = ''
  handleSearch()
}

// 分页变化
const handleSizeChange = () => {
  getCompanyList()
}

const handlePageChange = () => {
  getCompanyList()
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
  Object.assign(companyForm, {
    id: row.id,
    companyName: row.companyName,
    typeId: row.typeId,
    unifiedSocialCreditCode: row.unifiedSocialCreditCode || '',
    contactPerson: row.contactPerson || '',
    contactPhone: row.contactPhone || '',
    contactEmail: row.contactEmail || '',
    address: row.address || '',
    description: row.description || '',
    status: row.status,
    allowAnonymousRegister: row.allowAnonymousRegister || false,
    isSystemProtected: row.isSystemProtected || false
  })
}

// 重置表单
const resetForm = () => {
  companyFormRef.value?.resetFields()
  Object.assign(companyForm, {
    id: null,
    companyName: '',
    typeId: null,
    unifiedSocialCreditCode: '',
    contactPerson: '',
    contactPhone: '',
    contactEmail: '',
    address: '',
    description: '',
    status: 'active',
    allowAnonymousRegister: false,
    isSystemProtected: false
  })
}

// 提交表单
const handleSubmit = async () => {
  if (!companyFormRef.value) return
  
  await companyFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (dialogMode.value === 'create') {
          await request({
            url: '/company',
            method: 'post',
            data: companyForm
          })
          ElMessage.success('创建成功')
        } else {
          await request({
            url: `/company/${companyForm.id}`,
            method: 'put',
            data: companyForm
          })
          ElMessage.success('更新成功')
        }
        
        dialogVisible.value = false
        getCompanyList()
      } catch (error) {
        console.error('提交失败:', error)
      } finally {
        submitting.value = false
      }
    }
  })
}

// 删除公司
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除公司"${row.companyName}"吗？此操作不可恢复！`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await request({
        url: `/company/${row.id}`,
        method: 'delete'
      })
      ElMessage.success('删除成功')
      getCompanyList()
    } catch (error) {
      console.error('删除失败:', error)
    }
  }).catch(() => {})
}

// 获取公司类型标签
const getTypeTag = (typeId) => {
  const typeMap = {
    1: '',      // 甲方
    2: 'success', // 乙方
    3: 'warning', // 监理
    4: 'danger'   // 软件所有者
  }
  return typeMap[typeId] || ''
}

// 计算对话框标题
const dialogTitle = dialogMode.value === 'create' ? '创建公司' : '编辑公司'

onMounted(() => {
  getCompanyTypes()
  getCompanyList()
})
</script>

<style scoped lang="scss">
.company-management {
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
    }

    .search-form {
      margin-bottom: 20px;
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
