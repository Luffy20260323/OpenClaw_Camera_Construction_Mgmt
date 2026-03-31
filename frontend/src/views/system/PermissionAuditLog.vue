<template>
  <AdminLayout>
    <div class="permission-audit-log-container">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>权限审计日志</span>
            <div class="header-actions">
              <el-button type="success" @click="exportAuditLogs" :loading="exporting">
                <el-icon><Download /></el-icon>
                导出 Excel
              </el-button>
              <el-button type="primary" @click="loadAuditLogs" :loading="loading">
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
            </div>
          </div>
        </template>
        
        <!-- 筛选条件 -->
        <el-card class="filter-card" shadow="never">
          <el-form :model="filters" inline label-width="80px">
            <el-form-item label="目标类型">
              <el-select v-model="filters.targetType" placeholder="全部" clearable style="width: 150px">
                <el-option label="角色" value="ROLE" />
                <el-option label="用户" value="USER" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="目标名称">
              <el-input 
                v-model="filters.targetName" 
                placeholder="请输入目标名称" 
                clearable 
                style="width: 200px"
                @keyup.enter="handleSearch"
              />
            </el-form-item>
            
            <el-form-item label="操作类型">
              <el-select v-model="filters.operationType" placeholder="全部" clearable style="width: 150px">
                <el-option label="授权" value="GRANT" />
                <el-option label="撤销" value="REVOKE" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="操作人">
              <el-input 
                v-model="filters.operatorName" 
                placeholder="请输入操作人" 
                clearable 
                style="width: 180px"
                @keyup.enter="handleSearch"
              />
            </el-form-item>
            
            <el-form-item label="时间范围">
              <el-date-picker
                v-model="dateRange"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 400px"
              />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="handleSearch">
                <el-icon><Search /></el-icon>
                查询
              </el-button>
              <el-button @click="resetFilters">
                <el-icon><RefreshLeft /></el-icon>
                重置
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
        
        <!-- 审计日志列表 -->
        <el-table 
          :data="auditLogs" 
          style="width: 100%" 
          v-loading="loading"
          :default-sort="{ prop: 'createdAt', order: 'descending' }"
        >
          <el-table-column prop="targetType" label="目标类型" width="100" sortable>
            <template #default="{ row }">
              <el-tag :type="row.targetType === 'ROLE' ? 'success' : 'info'" size="small">
                {{ row.targetType === 'ROLE' ? '角色' : '用户' }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="targetName" label="目标名称" width="150" sortable />
          
          <el-table-column prop="resourceName" label="资源名称" width="180" show-overflow-tooltip />
          
          <el-table-column prop="operationType" label="操作类型" width="100" sortable>
            <template #default="{ row }">
              <el-tag :type="row.operationType === 'GRANT' ? 'success' : 'danger'" size="small">
                {{ row.operationType === 'GRANT' ? '授权' : '撤销' }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="sourceType" label="来源类型" width="120">
            <template #default="{ row }">
              <el-tag size="small" effect="plain">
                {{ formatSourceType(row.sourceType) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="operatorName" label="操作人" width="120" sortable />
          
          <el-table-column prop="createdAt" label="操作时间" width="180" sortable>
            <template #default="{ row }">
              {{ formatDateTime(row.createdAt) }}
            </template>
          </el-table-column>
          
          <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
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
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Download, Refresh, Search, RefreshLeft } from '@element-plus/icons-vue'
import { getPermissionAuditLogs, exportPermissionAuditLogs } from '@/api/permission'
import AdminLayout from '@/layouts/AdminLayout.vue'

const loading = ref(false)
const exporting = ref(false)
const auditLogs = ref([])

const filters = reactive({
  targetType: '',
  targetName: '',
  operationType: '',
  operatorName: '',
  startTime: '',
  endTime: ''
})

const dateRange = ref([])

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  try {
    const date = new Date(dateTime)
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    })
  } catch (e) {
    return dateTime
  }
}

// 格式化来源类型
const formatSourceType = (type) => {
  const map = {
    'ROLE_PERMISSION': '角色权限',
    'USER_PERMISSION': '用户权限',
    'DEFAULT_PERMISSION': '缺省权限',
    'SYSTEM': '系统'
  }
  return map[type] || type
}

// 加载审计日志
const loadAuditLogs = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      ...filters
    }
    
    const res = await getPermissionAuditLogs(params)
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
  // 处理时间范围
  if (dateRange.value && dateRange.value.length === 2) {
    filters.startTime = dateRange.value[0]
    filters.endTime = dateRange.value[1]
  } else {
    filters.startTime = ''
    filters.endTime = ''
  }
  
  pagination.page = 1
  loadAuditLogs()
}

// 重置筛选
const resetFilters = () => {
  filters.targetType = ''
  filters.targetName = ''
  filters.operationType = ''
  filters.operatorName = ''
  filters.startTime = ''
  filters.endTime = ''
  dateRange.value = []
  pagination.page = 1
  loadAuditLogs()
}

// 导出 Excel
const exportAuditLogs = async () => {
  exporting.value = true
  try {
    // 处理时间范围
    if (dateRange.value && dateRange.value.length === 2) {
      filters.startTime = dateRange.value[0]
      filters.endTime = dateRange.value[1]
    } else {
      filters.startTime = ''
      filters.endTime = ''
    }
    
    const params = {
      ...filters
    }
    
    const response = await exportPermissionAuditLogs(params)
    
    // 创建下载链接
    const blob = new Blob([response], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `权限审计日志_${new Date().toISOString().slice(0, 10)}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败：' + error.message)
  } finally {
    exporting.value = false
  }
}

onMounted(() => {
  loadAuditLogs()
})
</script>

<style scoped lang="scss">
.permission-audit-log-container {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .header-actions {
      display: flex;
      gap: 12px;
    }
  }
  
  .filter-card {
    margin-bottom: 20px;
    
    :deep(.el-card__body) {
      padding: 16px;
    }
    
    :deep(.el-form) {
      margin-bottom: 0;
      
      .el-form-item {
        margin-bottom: 12px;
      }
    }
  }
  
  :deep(.el-table) {
    .el-table__header th {
      background-color: #f5f7fa;
      color: #606266;
      font-weight: 600;
    }
  }
  
  :deep(.el-pagination) {
    display: flex;
  }
}
</style>
