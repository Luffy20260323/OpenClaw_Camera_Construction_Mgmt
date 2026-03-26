<template>
  <AdminLayout>
    <el-card class="box-card">
        <template #header>
          <div class="card-header">
            <span>角色管理</span>
            <div style="display: flex; gap: 10px; align-items: center;">
              <el-tag v-if="!isSystemAdmin" type="info">👁️ 只读模式</el-tag>
              <el-button v-if="isSystemAdmin" type="primary" @click="showCreateDialog">
                <el-icon><Plus /></el-icon>
                新建角色
              </el-button>
            </div>
          </div>
        </template>

      <!-- 搜索筛选 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="公司类型">
          <el-select v-model="searchForm.companyTypeId" placeholder="全部" clearable style="width: 200px">
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
            placeholder="角色名称/编码"
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

      <!-- 角色列表 -->
      <el-table :data="roleList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="roleName" label="角色名称" min-width="150" />
        <el-table-column prop="roleCode" label="角色编码" width="180" />
        <el-table-column prop="companyTypeName" label="公司类型" width="150">
          <template #default="scope">
            <el-tag :type="getCompanyTypeTag(scope.row.companyTypeId)">
              {{ scope.row.companyTypeName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="roleDescription" label="描述" min-width="200" show-overflow-tooltip />
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
        width="500px"
        :close-on-click-modal="false"
        @closed="resetForm"
      >
        <el-form
          ref="roleFormRef"
          :model="roleForm"
          :rules="rules"
          label-width="100px"
        >
          <el-form-item label="角色名称" prop="roleName">
            <el-input v-model="roleForm.roleName" placeholder="请输入角色名称" />
          </el-form-item>
          
          <el-form-item label="角色编码" prop="roleCode">
            <el-input v-model="roleForm.roleCode" placeholder="请输入角色编码" />
          </el-form-item>
          
          <el-form-item label="公司类型" prop="companyTypeId">
            <el-select v-model="roleForm.companyTypeId" placeholder="请选择公司类型" style="width: 100%">
              <el-option
                v-for="type in companyTypes"
                :key="type.id"
                :label="type.typeName"
                :value="type.id"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="描述">
            <el-input v-model="roleForm.roleDescription" placeholder="请输入角色描述" type="textarea" :rows="3" />
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
  
  console.log('[RoleList] checkPermissions - isLoggedIn:', userStore.isLoggedIn, 'companyTypeId:', userStore.companyTypeId)
  
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
const dialogMode = ref('create') // create | edit
const roleFormRef = ref(null)

// 权限判断
const isSystemAdmin = computed(() => {
  const val = userStore.companyTypeId === 4
  console.log('[RoleList] isSystemAdmin:', val, 'companyTypeId:', userStore.companyTypeId)
  return val
})

const goBack = () => {
  router.push('/')
}

// 搜索表单
const searchForm = reactive({
  companyTypeId: null,
  keyword: ''
})

// 分页
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 角色列表
const roleList = ref([])

// 公司类型列表
const companyTypes = ref([])

// 表单数据
const roleForm = reactive({
  id: null,
  roleName: '',
  roleCode: '',
  roleDescription: '',
  companyTypeId: null
})

// 表单验证规则
const rules = {
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' }
  ],
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' }
  ],
  companyTypeId: [
    { required: true, message: '请选择公司类型', trigger: 'change' }
  ]
}

// 获取公司类型列表
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

// 获取角色列表
const getRoleList = async () => {
  loading.value = true
  try {
    const res = await request({
      url: '/role',
      method: 'get',
      params: {
        pageNum: pagination.pageNum,
        pageSize: pagination.pageSize,
        ...searchForm
      }
    })
    roleList.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    console.error('获取角色列表失败:', error)
    ElMessage.error('获取角色列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  getRoleList()
}

// 重置搜索
const resetSearch = () => {
  searchForm.companyTypeId = null
  searchForm.keyword = ''
  handleSearch()
}

// 分页变化
const handleSizeChange = () => {
  getRoleList()
}

const handlePageChange = () => {
  getRoleList()
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
  Object.assign(roleForm, {
    id: row.id,
    roleName: row.roleName,
    roleCode: row.roleCode,
    roleDescription: row.roleDescription || '',
    companyTypeId: row.companyTypeId
  })
}

// 重置表单
const resetForm = () => {
  roleFormRef.value?.resetFields()
  Object.assign(roleForm, {
    id: null,
    roleName: '',
    roleCode: '',
    roleDescription: '',
    companyTypeId: null
  })
}

// 提交表单
const handleSubmit = async () => {
  if (!roleFormRef.value) return
  
  await roleFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (dialogMode.value === 'create') {
          await request({
            url: '/role',
            method: 'post',
            data: roleForm
          })
          ElMessage.success('创建成功')
        } else {
          await request({
            url: `/role/${roleForm.id}`,
            method: 'put',
            data: roleForm
          })
          ElMessage.success('更新成功')
        }
        
        dialogVisible.value = false
        getRoleList()
      } catch (error) {
        console.error('提交失败:', error)
      } finally {
        submitting.value = false
      }
    }
  })
}

// 删除角色
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除角色"${row.roleName}"吗？此操作不可恢复！`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await request({
        url: `/role/${row.id}`,
        method: 'delete'
      })
      ElMessage.success('删除成功')
      getRoleList()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error(error.message || '删除失败：' + (error.response?.data?.message || '未知错误'))
    }
  }).catch(() => {})
}

// 获取公司类型标签
const getCompanyTypeTag = (typeId) => {
  const typeMap = {
    1: '',      // 甲方
    2: 'success', // 乙方
    3: 'warning', // 监理
    4: 'danger'   // 软件所有者
  }
  return typeMap[typeId] || ''
}

// 计算对话框标题
const dialogTitle = dialogMode.value === 'create' ? '创建角色' : '编辑角色'

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
  
  console.log('[RoleList] onMounted - userInfo:', userStore.userInfo, 'companyTypeId:', userStore.companyTypeId)
  getCompanyTypes()
  getRoleList()
})
</script>

<style scoped lang="scss">
.role-management {
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
      font-size: 16px;
      font-weight: 600;

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
