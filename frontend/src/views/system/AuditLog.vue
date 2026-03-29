<template>
  <AdminLayout>
    <div class="audit-log-container">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>权限配置审计日志</span>
            <el-button type="primary" @click="loadAuditLogs">刷新</el-button>
          </div>
        </template>
        
        <!-- 筛选条件 -->
        <el-form :model="filters" inline>
          <el-form-item label="操作类型">
            <el-select v-model="filters.operationType" placeholder="全部" clearable style="width: 200px">
              <el-option label="配置角色权限" value="CONFIG_ROLE_PERMISSION" />
              <el-option label="回滚权限配置" value="ROLLBACK_ROLE_PERMISSION" />
              <el-option label="授予用户权限" value="GRANT_USER_PERMISSION" />
              <el-option label="撤销用户权限" value="REVOKE_USER_PERMISSION" />
            </el-select>
          </el-form-item>
          <el-form-item label="目标类型">
            <el-select v-model="filters.targetType" placeholder="全部" clearable style="width: 200px">
              <el-option label="角色" value="ROLE" />
              <el-option label="用户" value="USER" />
            </el-select>
          </el-form-item>
          <el-form-item label="操作人">
            <el-input v-model="filters.operatorName" placeholder="请输入操作人姓名" clearable style="width: 200px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="resetFilters">重置</el-button>
          </el-form-item>
        </el-form>
        
        <!-- 审计日志列表 -->
        <el-table :data="auditLogs" style="width: 100%" v-loading="loading">
          <el-table-column prop="createdAt" label="操作时间" width="180" />
          <el-table-column prop="operatorName" label="操作人" width="120" />
          <el-table-column prop="operationType" label="操作类型" width="160">
            <template #default="{ row }">
              <el-tag :type="getOperationTypeTag(row.operationType)">
                {{ formatOperationType(row.operationType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="targetType" label="目标类型" width="100">
            <template #default="{ row }">
              <el-tag :type="row.targetType === 'ROLE' ? 'success' : 'info'" size="small">
                {{ row.targetType === 'ROLE' ? '角色' : '用户' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="targetName" label="目标名称" width="150" />
          <el-table-column prop="changeDescription" label="变更说明" show-overflow-tooltip />
          <el-table-column prop="ipAddress" label="IP 地址" width="140" />
          <el-table-column label="详情" width="100" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link @click="viewDetail(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页 -->
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="loadAuditLogs"
          @size-change="loadAuditLogs"
          style="margin-top: 20px; justify-content: flex-end;"
        />
      </el-card>
      
      <!-- 详情对话框 -->
      <el-dialog v-model="detailVisible" title="审计日志详情" width="800px">
        <el-descriptions :column="2" border v-if="currentLog">
          <el-descriptions-item label="操作时间">{{ currentLog.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="操作人">{{ currentLog.operatorName }}</el-descriptions-item>
          <el-descriptions-item label="操作类型">{{ formatOperationType(currentLog.operationType) }}</el-descriptions-item>
          <el-descriptions-item label="目标类型">{{ currentLog.targetType === 'ROLE' ? '角色' : '用户' }}</el-descriptions-item>
          <el-descriptions-item label="目标名称">{{ currentLog.targetName }}</el-descriptions-item>
          <el-descriptions-item label="IP 地址">{{ currentLog.ipAddress }}</el-descriptions-item>
          <el-descriptions-item label="变更说明" :span="2">{{ currentLog.changeDescription || '-' }}</el-descriptions-item>
          <el-descriptions-item label="修改前权限" :span="2">
            <el-tag v-for="(perm, index) in parsePermissionIds(currentLog.permissionIdsBefore)" :key="index" size="small" style="margin: 2px;">
              {{ perm }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="修改后权限" :span="2">
            <el-tag v-for="(perm, index) in parsePermissionIds(currentLog.permissionIdsAfter)" :key="index" size="small" style="margin: 2px;">
              {{ perm }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
        <template #footer>
          <el-button @click="detailVisible = false">关闭</el-button>
        </template>
      </el-dialog>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAuditLogs } from '@/api/permission'
import AdminLayout from '@/layouts/AdminLayout.vue'

const loading = ref(false)
const auditLogs = ref([])
const detailVisible = ref(false)
const currentLog = ref(null)

const filters = reactive({
  operationType: '',
  targetType: '',
  operatorName: ''
})

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 加载审计日志
const loadAuditLogs = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      ...filters
    }
    
    const res = await getAuditLogs(params)
    const data = res.data
    
    auditLogs.value = data.content || []
    pagination.total = data.total || 0
  } catch (error) {
    ElMessage.error('加载审计日志失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadAuditLogs()
}

// 重置筛选
const resetFilters = () => {
  filters.operationType = ''
  filters.targetType = ''
  filters.operatorName = ''
  pagination.page = 1
  loadAuditLogs()
}

// 查看详情
const viewDetail = (row) => {
  currentLog.value = row
  detailVisible.value = true
}

// 格式化操作类型
const formatOperationType = (type) => {
  const map = {
    'CONFIG_ROLE_PERMISSION': '配置角色权限',
    'ROLLBACK_ROLE_PERMISSION': '回滚权限配置',
    'GRANT_USER_PERMISSION': '授予用户权限',
    'REVOKE_USER_PERMISSION': '撤销用户权限'
  }
  return map[type] || type
}

// 获取操作类型标签颜色
const getOperationTypeTag = (type) => {
  const map = {
    'CONFIG_ROLE_PERMISSION': 'primary',
    'ROLLBACK_ROLE_PERMISSION': 'warning',
    'GRANT_USER_PERMISSION': 'success',
    'REVOKE_USER_PERMISSION': 'danger'
  }
  return map[type] || 'info'
}

// 解析权限 ID 列表
const parsePermissionIds = (idsStr) => {
  if (!idsStr) return []
  try {
    // 去除方括号并分割
    return idsStr.replace(/[\[\]]/g, '').split(',').map(s => s.trim()).filter(s => s)
  } catch (e) {
    return []
  }
}

onMounted(() => {
  loadAuditLogs()
})
</script>

<style scoped lang="scss">
.audit-log-container {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  :deep(.el-form) {
    margin-bottom: 20px;
  }
  
  :deep(.el-pagination) {
    display: flex;
  }
}
</style>