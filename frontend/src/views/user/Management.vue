<template>
  <AdminLayout>
    <el-card class="filter-card">
        <template #header>
          <div class="card-header">
            <span>用户管理</span>
            <el-space>
              <el-button type="primary" @click="showCreateDialog">
                <el-icon><Plus /></el-icon>
                创建用户
              </el-button>
              <el-button @click="showBatchImportDialog">
                <el-icon><Upload /></el-icon>
                批量导入
              </el-button>
              <el-button @click="loadPendingApprovals">
                <el-icon><Bell /></el-icon>
                待审批 ({{ pendingCount }})
              </el-button>
            </el-space>
          </div>
        </template>

        <el-form :inline="true" :model="queryForm" class="filter-form">
          <el-form-item label="关键词">
            <el-input v-model="queryForm.keyword" placeholder="用户名/姓名/手机/邮箱/公司/作业区" clearable style="width: 220px" />
          </el-form-item>
          <el-form-item label="公司类型">
            <el-select v-model="queryForm.companyTypeId" placeholder="全部" clearable style="width: 120px">
              <el-option label="甲方" :value="1" />
              <el-option label="乙方" :value="2" />
              <el-option label="监理方" :value="3" />
            </el-select>
          </el-form-item>
          <el-form-item label="审批状态">
            <el-select v-model="queryForm.approvalStatus" placeholder="全部" clearable style="width: 120px">
              <el-option label="待审批" :value="0" />
              <el-option label="已通过" :value="1" />
              <el-option label="已拒绝" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadUsers">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <!-- 用户列表表格 -->
        <el-table :data="userList" v-loading="loading" style="width: 100%; margin-top: 20px;" :header-cell-style="{background: '#f5f7fa', color: '#606266', fontWeight: 'bold', border: '1px solid #dcdfe6'}" :cell-style="{border: '1px solid #e4e7ed'}">
          <el-table-column prop="username" label="用户名" width="90" />
          <el-table-column prop="realName" label="姓名" width="70" />
          <el-table-column prop="email" label="邮箱" width="150" />
          <el-table-column prop="phone" label="手机号" width="100" />
          <el-table-column prop="companyName" label="公司" width="120" />
          <el-table-column prop="companyTypeName" label="公司类型" width="91" />
          <el-table-column label="角色" min-width="105">
            <template #default="{ row }">
              <el-tag v-for="role in row.roleNames" :key="role" size="small" style="margin-right: 4px; margin-bottom: 4px;">
                {{ role }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="作业区" min-width="90">
            <template #default="{ row }">
              <el-tag v-if="row.workAreaNames && row.workAreaNames.length > 0" v-for="area in row.workAreaNames" :key="area" size="small" type="info" style="margin-right: 4px; margin-bottom: 4px; font-size: 12px;">
                {{ area }}
              </el-tag>
              <span v-else style="color: #999;">-</span>
            </template>
          </el-table-column>
          <el-table-column label="审批状态" width="80">
            <template #default="{ row }">
              <el-tag :type="getApprovalStatusType(row.approvalStatus)">
                {{ getApprovalStatusText(row.approvalStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                {{ row.status === 1 ? '正常' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="280" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="showEditDialog(row)">编辑</el-button>
              <el-button size="small" type="warning" @click="showResetPasswordDialog(row)">重置密码</el-button>
              <el-button size="small" type="danger" @click="confirmDelete(row)">删除</el-button>
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
          @size-change="loadUsers"
          @current-change="loadUsers"
          style="margin-top: 20px; justify-content: flex-end"
        />
      </el-card>
  </AdminLayout>
  
  <!-- 创建用户对话框 -->
  <el-dialog v-model="createDialogVisible" title="创建用户" width="600px">
    <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="100px">
      <el-form-item label="用户名" prop="username">
        <el-input v-model="createForm.username" placeholder="请输入用户名" />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input v-model="createForm.password" type="password" placeholder="请输入密码" />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input v-model="createForm.confirmPassword" type="password" placeholder="请确认密码" />
      </el-form-item>
      <el-form-item label="姓名" prop="realName">
        <el-input v-model="createForm.realName" placeholder="请输入姓名" />
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="createForm.email" placeholder="请输入邮箱" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="createForm.phone" placeholder="请输入手机号" maxlength="11" />
      </el-form-item>
      <el-form-item label="公司" prop="companyId">
        <el-select v-model="createForm.companyId" placeholder="请选择公司" style="width: 100%" :disabled="!isSystemAdmin" @change="handleCompanyChange">
          <el-option v-for="c in companyList" :key="c.id" :label="c.companyName" :value="c.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="角色" prop="roleIds">
        <el-select v-model="createForm.roleIds" multiple placeholder="请选择角色" style="width: 100%" @change="handleRoleChange">
          <el-option v-for="role in filteredRoleList" :key="role.id" :label="role.roleName" :value="role.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="作业区" prop="workAreaIds" v-if="showWorkAreaField">
        <el-select v-model="createForm.workAreaIds" multiple placeholder="请选择作业区" style="width: 100%">
          <el-option v-for="area in workAreaList" :key="area.id" :label="area.workAreaName" :value="area.id" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="createDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleCreate" :loading="creating">确定</el-button>
    </template>
  </el-dialog>
  
  <!-- 编辑用户对话框 -->
  <el-dialog v-model="editDialogVisible" title="编辑用户" width="600px">
    <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="100px">
      <el-form-item label="用户名">
        <el-input v-model="editForm.username" disabled />
      </el-form-item>
      <el-form-item label="公司">
        <el-input v-model="editForm.companyName" disabled />
      </el-form-item>
      <el-form-item label="姓名" prop="realName">
        <el-input v-model="editForm.realName" placeholder="请输入姓名" />
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="editForm.email" placeholder="请输入邮箱" />
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="editForm.phone" placeholder="请输入手机号" maxlength="11" />
      </el-form-item>
      <el-form-item label="角色">
        <el-select v-model="editForm.roleIds" multiple placeholder="请选择角色" style="width: 100%">
          <el-option v-for="role in filteredRoleList" :key="role.id" :label="role.roleName" :value="role.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="作业区" prop="workAreaIds" v-if="showEditWorkAreaField">
        <el-select v-model="editForm.workAreaIds" multiple placeholder="请选择作业区" style="width: 100%">
          <el-option v-for="area in workAreaList" :key="area.id" :label="area.workAreaName" :value="area.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="审批状态">
        <el-radio-group v-model="editForm.approvalStatus">
          <el-radio :label="1">已通过</el-radio>
          <el-radio :label="0">待审批</el-radio>
          <el-radio :label="2">已拒绝</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="editForm.status">
          <el-radio :label="1">正常</el-radio>
          <el-radio :label="0">禁用</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="editDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleEdit" :loading="editing">确定</el-button>
    </template>
  </el-dialog>
  
  <!-- 重置密码对话框 -->
  <el-dialog v-model="resetPasswordDialogVisible" title="重置密码" width="500px">
    <el-form ref="resetPasswordFormRef" :model="resetPasswordForm" :rules="resetPasswordRules" label-width="100px">
      <el-form-item label="用户名">
        <el-input v-model="resetPasswordForm.username" disabled />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="resetPasswordForm.newPassword" type="password" placeholder="请输入新密码" />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input v-model="resetPasswordForm.confirmPassword" type="password" placeholder="请确认密码" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="resetPasswordDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleResetPassword" :loading="resetting">确定</el-button>
    </template>
  </el-dialog>
  
  <!-- 批量导入对话框 -->
  <el-dialog v-model="batchImportDialogVisible" title="批量导入用户" width="700px">
    <el-tabs v-model="importTab">
      <el-tab-pane label="Excel 导入" name="excel">
        <el-upload ref="uploadRef" :auto-upload="false" :on-change="handleFileChange" :limit="1">
          <el-button type="primary">选择 Excel 文件</el-button>
          <template #tip>
            <div style="color: #999; margin-top: 8px;">
              <el-button type="text" @click="downloadTemplate">下载模板</el-button>
            </div>
          </template>
        </el-upload>
        <el-checkbox v-model="batchAutoApprove" style="margin-top: 16px;">自动审批通过</el-checkbox>
      </el-tab-pane>
    </el-tabs>
    <template #footer>
      <el-button @click="batchImportDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleExcelImport" :loading="importing">导入</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import AdminLayout from '@/layouts/AdminLayout.vue'
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, UploadFilled } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const creating = ref(false)
const approving = ref(false)
const importing = ref(false)
const pendingCount = ref(0)

// 判断是否为系统管理员
const isSystemAdmin = computed(() => {
  return userStore.userInfo?.roles?.includes('system_admin') || false
})

const queryForm = reactive({
  keyword: '',
  companyTypeId: null,
  approvalStatus: null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const userList = ref([])
const companyList = ref([])
const roleList = ref([])
const workAreaList = ref([])

// 判断创建用户时是否需要显示作业区选择字段（甲方公司 + 作业区角色）
const showWorkAreaField = computed(() => {
  // 首先检查是否为甲方公司
  if (!createForm.companyId) return false
  const selectedCompany = companyList.value.find(c => c.id === createForm.companyId)
  const companyTypeId = selectedCompany?.typeId || selectedCompany?.companyTypeId || null
  if (companyTypeId !== 1) return false
  
  // 检查是否选择了作业区角色（角色名称包含"作业区"）
  if (!createForm.roleIds || createForm.roleIds.length === 0) return false
  
  const hasWorkAreaRole = createForm.roleIds.some(roleId => {
    const role = roleList.value.find(r => r.id === roleId)
    return role && role.roleName.includes('作业区')
  })
  
  return hasWorkAreaRole
})

// 判断编辑用户时是否需要显示作业区选择字段（有作业区角色）
const showEditWorkAreaField = computed(() => {
  // 检查是否选择了作业区角色（角色名称包含"作业区"）
  if (!editForm.roleIds || editForm.roleIds.length === 0) return false
  
  const hasWorkAreaRole = editForm.roleIds.some(roleId => {
    const role = roleList.value.find(r => r.id === roleId)
    return role && role.roleName.includes('作业区')
  })
  
  return hasWorkAreaRole
})

// 过滤后的角色列表（只显示与用户公司类型匹配的角色）
const filteredRoleList = computed(() => {
  // 确定目标公司类型
  let targetCompanyTypeId = null
  
  // 编辑用户时，优先使用跟踪的公司类型变量
  if (editingUserCompanyTypeId.value) {
    targetCompanyTypeId = editingUserCompanyTypeId.value
    console.log('编辑用户 - 公司类型 ID:', targetCompanyTypeId)
  }
  // 创建用户时，使用已选择的公司类型
  else if (createForm.companyId) {
    // 根据选择的公司 ID 查找对应的公司类型
    const selectedCompany = companyList.value.find(c => c.id === createForm.companyId)
    console.log('创建用户 - 选中公司:', selectedCompany)
    if (selectedCompany) {
      // 尝试多个可能的字段名
      targetCompanyTypeId = selectedCompany.typeId || selectedCompany.companyTypeId || null
      console.log('创建用户 - 公司类型 ID:', targetCompanyTypeId, '公司列表:', companyList.value.length)
    }
  }
  // 其他情况使用当前用户的公司类型
  else {
    targetCompanyTypeId = userStore.userInfo?.companyTypeId
  }
  
  console.log('最终目标公司类型:', targetCompanyTypeId)
  
  if (!targetCompanyTypeId) {
    // 没有目标公司类型时，返回所有角色
    console.log('没有公司类型，返回所有角色')
    return roleList.value
  }
  
  // 过滤出与目标公司类型匹配的角色
  const filtered = roleList.value.filter(role => {
    const match = role.companyTypeId === targetCompanyTypeId
    console.log('角色:', role.roleName, 'companyTypeId:', role.companyTypeId, '匹配:', match)
    return match
  })
  
  console.log('角色过滤：目标公司类型=' + targetCompanyTypeId + ', 过滤前=' + roleList.value.length + ', 过滤后=' + filtered.length)
  
  return filtered
})

const createDialogVisible = ref(false)
const createFormRef = ref(null)
const editDialogVisible = ref(false)
const editFormRef = ref(null)
const editingUserCompanyTypeId = ref(null)  // 跟踪当前编辑用户的公司类型
const resetPasswordDialogVisible = ref(false)
const resetPasswordFormRef = ref(null)
const editing = ref(false)
const resetting = ref(false)
const deleting = ref(false)
const createForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  email: '',
  phone: '',
  companyId: null,
  roleIds: [],
  workAreaIds: [],
  autoApprove: true
})

const createRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '密码至少 6 位', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  companyId: [{ required: true, message: '请选择公司', trigger: 'change' }]
}

const editForm = reactive({
  userId: null,
  username: '',
  companyName: '',
  companyTypeId: null,
  realName: '',
  email: '',
  phone: '',
  gender: 0,
  approvalStatus: 1,
  rejectionReason: '',
  status: 1,
  roleIds: [],
  workAreaIds: []
})

const editRules = {
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

const resetPasswordForm = reactive({
  userId: null,
  username: '',
  newPassword: '',
  confirmPassword: ''
})

const resetPasswordRules = {
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, message: '密码至少 6 位', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认密码', trigger: 'blur' }]
}

const approveDialogVisible = ref(false)
const approveFormRef = ref(null)
const approveForm = reactive({
  userId: null,
  username: '',
  approved: true,
  rejectionReason: ''
})

const batchImportDialogVisible = ref(false)
const batchImportData = ref('')
const batchAutoApprove = ref(true)
const importTab = ref('excel')
const selectedFile = ref(null)
const uploadRef = ref(null)

const loadUsers = async () => {
  loading.value = true
  console.log('loadUsers 开始 - 用户信息:', userStore.userInfo)
  console.log('loadUsers - roles:', userStore.roles)
  console.log('loadUsers - companyId:', userStore.companyId)
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...queryForm
    }
    // 非系统管理员自动过滤公司类型
    const isSystemAdmin = userStore.roles?.includes("system_admin") || false
    console.log('loadUsers - isSystemAdmin:', isSystemAdmin)
    if (!isSystemAdmin && userStore.companyId) {
      params.companyId = userStore.companyId
      console.log('loadUsers - 添加 companyId 过滤:', userStore.companyId)
    }
    const res = await request({
      url: '/user/list',
      method: 'get',
      params
    })
    console.log('loadUsers - API 响应:', res.data)
    userList.value = res.data.records || []
    pagination.total = res.data.total || 0
    console.log('loadUsers - userList:', userList.value.length, '条记录')
  } catch (error) {
    console.error('loadUsers - 错误:', error)
    ElMessage.error('加载用户列表失败：' + error.message)
  } finally {
    loading.value = false
  }
}

const loadCompanies = async () => {
  try {
    const res = await request({
      url: '/company',
      method: 'get',
      params: { pageNum: 1, pageSize: 100 }
    })
    companyList.value = res.data.records || []
    console.log('公司列表数据:', companyList.value.map(c => ({id: c.id, name: c.companyName, typeId: c.typeId})))
  } catch (error) {
    console.error('加载公司列表失败:', error)
  }
}

const loadRoles = async () => {
  try {
    const res = await request({
      url: '/role',
      method: 'get',
      params: { pageNum: 1, pageSize: 100, companyTypeId: null, keyword: '' }
    })
    roleList.value = res.data.records || []
    console.log('角色列表数据:', roleList.value.map(r => ({id: r.id, name: r.roleName, companyTypeId: r.companyTypeId})))
  } catch (error) {
    console.error('加载角色列表失败:', error)
  }
}

const loadWorkAreas = async (companyId) => {
  try {
    const res = await request({
      url: '/workarea',
      method: 'get',
      params: { companyId: companyId, pageNum: 1, pageSize: 100 }
    })
    workAreaList.value = res.data.records || []
    console.log('作业区列表数据 (companyId=' + companyId + '):', workAreaList.value.map(a => ({id: a.id, name: a.workAreaName})))
  } catch (error) {
    console.error('加载作业区列表失败:', error)
    workAreaList.value = []
  }
}

const loadPendingApprovals = async () => {
  try {
    const res = await request({
      url: '/user/pending-approvals',
      method: 'get'
    })
    const pendingUsers = res.data || []
    pendingCount.value = pendingUsers.length
    if (pendingCount.value > 0) {
      ElMessage.info(`有 ${pendingCount.value} 个待审批用户，请使用筛选功能查看`)
    }
  } catch (error) {
    ElMessage.error('加载待审批列表失败：' + error.message)
  }
}

const showCreateDialog = async () => {
  // 确保公司列表和角色列表已加载
  if (companyList.value.length === 0) {
    await loadCompanies()
  }
  if (roleList.value.length === 0) {
    await loadRoles()
  }
  
  createDialogVisible.value = true
  
  // 非系统管理员默认公司为其归属公司，且公司字段禁用
  let defaultCompanyId = null
  if (!isSystemAdmin.value) {
    // 从用户信息中获取公司 ID
    defaultCompanyId = userStore.userInfo?.companyId || null
  }
  
  // 重置表单
  Object.assign(createForm, {
    username: '',
    password: '',
    confirmPassword: '',
    realName: '',
    email: '',
    phone: '',
    companyId: defaultCompanyId,
    roleIds: [],
    workAreaIds: [],
    autoApprove: true
  })
  
  // 重置作业区列表
  workAreaList.value = []
  
  // 如果默认选择了公司，加载对应的作业区
  if (defaultCompanyId) {
    loadWorkAreas(defaultCompanyId)
  }
}

// 处理公司选择变化，清空已选角色
const handleCompanyChange = (companyId) => {
  // 清空已选角色和作业区，因为公司类型可能变化
  createForm.roleIds = []
  createForm.workAreaIds = []
  workAreaList.value = []
  
  // 查找选中公司的类型 ID
  const selectedCompany = companyList.value.find(c => c.id === companyId)
  console.log('公司选择变化：companyId=', companyId, 'selectedCompany=', selectedCompany)
  
  // 只有甲方公司（companyTypeId=1）才需要选择作业区
  const companyTypeId = selectedCompany?.typeId || selectedCompany?.companyTypeId || null
  console.log('公司类型 ID:', companyTypeId)
  
  // 加载作业区列表（仅甲方公司）
  if (companyId && companyTypeId === 1) {
    loadWorkAreas(companyId)
  }
}

// 处理角色选择变化，判断是否需要加载作业区
const handleRoleChange = (roleIds) => {
  console.log('角色选择变化：roleIds=', roleIds)
  
  // 检查是否有作业区角色
  const hasWorkAreaRole = roleIds.some(roleId => {
    const role = roleList.value.find(r => r.id === roleId)
    return role && role.roleName.includes('作业区')
  })
  
  console.log('是否有作业区角色:', hasWorkAreaRole)
  
  // 如果有作业区角色且已选择公司，确保作业区列表已加载
  if (hasWorkAreaRole && createForm.companyId) {
    if (workAreaList.value.length === 0) {
      loadWorkAreas(createForm.companyId)
    }
  } else {
    // 如果没有作业区角色，清空作业区选择
    createForm.workAreaIds = []
  }
}

const handleCreate = async () => {
  if (!createFormRef.value) return
  await createFormRef.value.validate(async (valid) => {
    if (valid) {
      if (createForm.password !== createForm.confirmPassword) {
        ElMessage.error('两次输入的密码不一致')
        return
      }
      creating.value = true
      try {
        await request({
          url: '/user',
          method: 'post',
          data: createForm
        })
        ElMessage.success('创建成功')
        createDialogVisible.value = false
        loadUsers()
      } catch (error) {
        ElMessage.error('创建失败：' + error.message)
      } finally {
        creating.value = false
      }
    }
  })
}

const showEditDialog = async (user) => {
  // 确保角色列表已加载
  if (roleList.value.length === 0) {
    await loadRoles()
  }
  
  console.log('编辑用户 - 原始数据:', JSON.stringify(user, null, 2))
  console.log('编辑用户 - roleIds:', user.roleIds)
  console.log('编辑用户 - companyTypeId:', user.companyTypeId)
  console.log('编辑用户 - workAreaIds:', user.workAreaIds)
  console.log('编辑用户 - workAreaNames:', user.workAreaNames)
  console.log('编辑用户 - companyId:', user.companyId)
  
  // 先设置公司类型跟踪变量（在打开对话框之前）
  editingUserCompanyTypeId.value = user.companyTypeId || null
  console.log('编辑用户 - 设置后的跟踪变量:', editingUserCompanyTypeId.value)
  
  // 检查是否有作业区角色
  const hasWorkAreaRole = user.roleIds && user.roleIds.some(roleId => {
    const role = roleList.value.find(r => r.id === roleId)
    return role && role.roleName.includes('作业区')
  })
  
  console.log('编辑用户 - 是否有作业区角色:', hasWorkAreaRole)
  
  // 如果有作业区角色或用户已有作业区，先加载作业区列表
  if ((hasWorkAreaRole || (user.workAreaIds && user.workAreaIds.length > 0)) && user.companyId) {
    console.log('编辑用户 - 加载作业区列表，companyId:', user.companyId)
    await loadWorkAreas(user.companyId)
    console.log('编辑用户 - 作业区列表加载完成，数量:', workAreaList.value.length)
  }
  
  // 设置表单数据
  Object.assign(editForm, {
    userId: user.id,
    username: user.username,
    companyName: user.companyName || '',
    companyTypeId: user.companyTypeId || null,
    realName: user.realName,
    email: user.email || '',
    phone: user.phone ? String(user.phone) : '',  // 确保手机号是字符串格式
    gender: user.gender || 0,
    approvalStatus: user.approvalStatus,
    rejectionReason: user.rejectionReason || '',
    status: user.status,
    roleIds: user.roleIds || [],
    workAreaIds: user.workAreaIds ? [...user.workAreaIds] : []
  })
  
  console.log('编辑用户 - 角色列表:', roleList.value.length)
  console.log('编辑用户 - 过滤后的角色列表:', filteredRoleList.value.length)
  console.log('编辑用户 - editForm.roleIds:', editForm.roleIds)
  console.log('编辑用户 - editForm.workAreaIds:', editForm.workAreaIds)
  console.log('编辑用户 - workAreaList:', workAreaList.value.length)
  console.log('编辑用户 - showEditWorkAreaField:', showEditWorkAreaField.value)
  
  // 最后打开对话框
  editDialogVisible.value = true
}

const handleEdit = async () => {
  if (!editFormRef.value) return
  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      editing.value = true
      try {
        await request({
          url: `/user/${editForm.userId}`,
          method: 'put',
          data: {
            realName: editForm.realName,
            email: editForm.email,
            phone: editForm.phone,
            gender: editForm.gender,
            approvalStatus: editForm.approvalStatus,
            rejectionReason: editForm.rejectionReason,
            status: editForm.status,
            roleIds: editForm.roleIds,
            workAreaIds: editForm.workAreaIds
          }
        })
        ElMessage.success('更新成功')
        editDialogVisible.value = false
        loadUsers()
      } catch (error) {
        ElMessage.error('更新失败：' + error.message)
      } finally {
        editing.value = false
      }
    }
  })
}

const showResetPasswordDialog = (user) => {
  resetPasswordDialogVisible.value = true
  Object.assign(resetPasswordForm, {
    userId: user.id,
    username: user.username,
    newPassword: '',
    confirmPassword: ''
  })
}

const handleResetPassword = async () => {
  if (!resetPasswordFormRef.value) return
  await resetPasswordFormRef.value.validate(async (valid) => {
    if (valid) {
      if (resetPasswordForm.newPassword !== resetPasswordForm.confirmPassword) {
        ElMessage.error('两次输入的密码不一致')
        return
      }
      resetting.value = true
      try {
        await request({
          url: `/user/reset-password/${resetPasswordForm.userId}`,
          method: 'put',
          data: {
            newPassword: resetPasswordForm.newPassword,
            confirmPassword: resetPasswordForm.confirmPassword
          }
        })
        ElMessage.success('密码重置成功')
        resetPasswordDialogVisible.value = false
      } catch (error) {
        ElMessage.error('重置密码失败：' + error.message)
      } finally {
        resetting.value = false
      }
    }
  })
}

const confirmDelete = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${user.username}" 吗？此操作不可恢复！`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    deleting.value = true
    try {
      await request({
        url: `/user/${user.id}`,
        method: 'delete'
      })
      ElMessage.success('删除成功')
      loadUsers()
    } catch (error) {
      ElMessage.error('删除失败：' + error.message)
    } finally {
      deleting.value = false
    }
  } catch {
    // 用户取消
  }
}

const showApproveDialog = (user) => {
  approveDialogVisible.value = true
  approveForm.userId = user.id
  approveForm.username = user.username
  approveForm.approved = true
  approveForm.rejectionReason = ''
}

const handleApprove = async () => {
  approving.value = true
  try {
    await request({
      url: '/user/approve',
      method: 'post',
      data: {
        userId: approveForm.userId,
        approved: approveForm.approved,
        rejectionReason: approveForm.rejectionReason
      }
    })
    ElMessage.success('审批成功')
    approveDialogVisible.value = false
    loadUsers()
    loadPendingApprovals()
  } catch (error) {
    ElMessage.error('审批失败：' + error.message)
  } finally {
    approving.value = false
  }
}

const showBatchImportDialog = () => {
  batchImportDialogVisible.value = true
  batchImportData.value = ''
  batchAutoApprove.value = true
  importTab.value = 'excel'
  selectedFile.value = null
  if (uploadRef.value) {
    uploadRef.value.clearFiles()
  }
}

// 下载 Excel 模板
const downloadTemplate = async () => {
  try {
    const response = await request({
      url: '/user/import-template',
      method: 'get',
      responseType: 'blob'
    })
    
    const blob = new Blob([response], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = 'yonghu-daoru-moban.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
  } catch (error) {
    ElMessage.error('下载模板失败：' + error.message)
  }
}

// 处理文件选择
const handleFileChange = (file) => {
  selectedFile.value = file.raw
}

// Excel 文件导入
const handleExcelImport = async () => {
  if (!selectedFile.value) {
    ElMessage.error('请选择要上传的文件')
    return
  }
  
  importing.value = true
  try {
    const formData = new FormData()
    formData.append('file', selectedFile.value)
    formData.append('autoApprove', batchAutoApprove.value)
    
    const res = await request({
      url: '/user/batch-import-file',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    const result = res.data
    
    // 使用后端返回的详细结果
    const importResults = []
    
    if (result.results && result.results.length > 0) {
      // 后端返回了详细结果
      result.results.forEach(item => {
        importResults.push({
          rowNum: item.rowNum.toString(),
          username: item.username,
          status: item.success ? 'success' : 'failed',
          error: item.error || '-'
        })
      })
    } else if (result.errors && result.errors.length > 0) {
      // 兼容旧格式
      result.errors.forEach((error, index) => {
        const rowNumMatch = error.match(/第 (\d+) 行/)
        const rowNum = rowNumMatch ? rowNumMatch[1] : (index + 1).toString()
        const errorMsg = error.replace(/第\d+行失败：/, '')
        const parts = errorMsg.split(/[:：]/)
        const username = parts.length > 1 ? parts[parts.length - 1].trim() : `第${rowNum}行用户`
        
        importResults.push({
          rowNum,
          username,
          status: 'failed',
          error: errorMsg
        })
      })
    }
    
    // 部分成功时，不需要额外添加汇总行，直接显示所有用户的结果
    
    if (result.success > 0 && result.failed > 0) {
      // 部分成功
      ElMessage.success(`导入完成！成功：${result.success}，失败：${result.failed}`)
      showImportResultTable(importResults, '部分成功')
    } else if (result.success > 0) {
      // 全部成功
      ElMessage.success(`导入完成！成功：${result.success}`)
      batchImportDialogVisible.value = false
      loadUsers()
    } else {
      // 全部失败
      showImportResultTable(importResults, '全部失败')
    }
    
    if (result.success > 0) {
      loadUsers()
    }
  } catch (error) {
    ElMessage.error('导入失败：' + error.message)
  } finally {
    importing.value = false
  }
}

// 显示导入结果表格
const showImportResultTable = (results, title) => {
  console.log('导入结果数据:', results)
  
  if (!results || results.length === 0) {
    ElMessageBox.alert('导入失败，但没有获取到详细错误信息', `导入结果：${title}`, {
      confirmButtonText: '确定',
      type: 'warning'
    })
    return
  }
  
  const tableHtml = `
    <div style="max-height: 400px; overflow-y: auto;">
      <table style="width: 100%; border-collapse: collapse; font-size: 13px;">
        <thead>
          <tr style="background: #f5f7fa; position: sticky; top: 0;">
            <th style="border: 1px solid #dcdfe6; padding: 8px 12px; text-align: left;">行号</th>
            <th style="border: 1px solid #dcdfe6; padding: 8px 12px; text-align: left;">用户名</th>
            <th style="border: 1px solid #dcdfe6; padding: 8px 12px; text-align: center;">结果</th>
            <th style="border: 1px solid #dcdfe6; padding: 8px 12px; text-align: left;">失败原因</th>
          </tr>
        </thead>
        <tbody>
          ${results.map(item => `
            <tr style="${item.status === 'success' ? 'background: #f0f9eb;' : 'background: #fef0f0;'}">
              <td style="border: 1px solid #dcdfe6; padding: 8px 12px;">${item.rowNum}</td>
              <td style="border: 1px solid #dcdfe6; padding: 8px 12px; font-weight: ${item.status === 'success' ? 'bold' : 'normal'};">${item.username}</td>
              <td style="border: 1px solid #dcdfe6; padding: 8px 12px; text-align: center;">
                <span style="color: ${item.status === 'success' ? '#67c23a' : '#f56c6c'}; font-weight: bold;">
                  ${item.status === 'success' ? '✅ 成功' : '❌ 失败'}
                </span>
              </td>
              <td style="border: 1px solid #dcdfe6; padding: 8px 12px; color: ${item.status === 'success' ? '#909399' : '#f56c6c'};">
                ${item.error}
              </td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    </div>
  `
  
  ElMessageBox.alert(tableHtml, `导入结果：${title}`, {
    dangerouslyUseHTMLString: true,
    confirmButtonText: '确定',
    customClass: 'import-result-dialog',
    showClose: false
  })
}

const handleBatchImport = async () => {
  importing.value = true
  try {
    const users = JSON.parse(batchImportData.value)
    if (!Array.isArray(users)) {
      throw new Error('请输入有效的 JSON 数组')
    }
    await request({
      url: '/user/batch-import',
      method: 'post',
      data: {
        autoApprove: batchAutoApprove.value,
        users: users
      }
    })
    ElMessage.success('批量导入成功')
    batchImportDialogVisible.value = false
    loadUsers()
  } catch (error) {
    ElMessage.error('导入失败：' + error.message)
  } finally {
    importing.value = false
  }
}

const viewUser = (user) => {
  ElMessageBox.alert(
    `<div style="line-height: 2;">
      <strong>用户名：</strong>${user.username}<br/>
      <strong>姓名：</strong>${user.realName}<br/>
      <strong>邮箱：</strong>${user.email || '-'}<br/>
      <strong>手机：</strong>${user.phone || '-'}<br/>
      <strong>公司：</strong>${user.companyName}<br/>
      <strong>角色：</strong>${(user.roles || []).join(', ')}<br/>
      <strong>审批状态：</strong>${getApprovalStatusText(user.approvalStatus)}<br/>
      <strong>状态：</strong>${user.status === 1 ? '正常' : '禁用'}
    </div>`,
    '用户详情',
    { dangerouslyUseHTMLString: true }
  )
}

const resetQuery = () => {
  queryForm.keyword = ''
  queryForm.companyTypeId = null
  queryForm.approvalStatus = null
  pagination.pageNum = 1
  loadUsers()
}

const getApprovalStatusType = (status) => {
  const types = { 0: 'warning', 1: 'success', 2: 'danger' }
  return types[status] || 'info'
}

const getApprovalStatusText = (status) => {
  const texts = { 0: '待审批', 1: '已通过', 2: '已拒绝' }
  return texts[status] || '未知'
}

onMounted(() => {
  loadUsers()
  loadCompanies()
  loadRoles()
  loadPendingApprovals()
})
</script>

<style scoped lang="scss">
.user-management-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

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

  .filter-card {
    margin-bottom: 20px;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 16px;
      font-weight: 600;
    }

    .filter-form {
      margin-bottom: 0;
    }
  }

  .table-card {
    .card-header {
      font-size: 16px;
      font-weight: 600;
    }
  }
}
</style>