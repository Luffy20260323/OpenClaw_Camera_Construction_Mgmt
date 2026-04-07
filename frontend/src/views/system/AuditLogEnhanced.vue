<template>
  <AdminLayout>
    <div class="audit-log-page">
      <el-card class="header-card">
        <div class="page-header">
          <h2>审计日志</h2>
          <p class="description">
            查看系统操作审计日志，追踪权限变更、资源操作等关键行为。
          </p>
        </div>
      </el-card>

      <el-card class="content-card">
        <!-- 搜索筛选 -->
        <el-form :model="searchForm" :inline="true" class="search-form">
          <el-form-item label="操作类型">
            <el-select
              v-model="searchForm.operationType"
              placeholder="全部"
              clearable
              style="width: 150px"
            >
              <el-option label="创建" value="CREATE" />
              <el-option label="更新" value="UPDATE" />
              <el-option label="删除" value="DELETE" />
              <el-option label="授权" value="GRANT" />
              <el-option label="撤销" value="REVOKE" />
              <el-option label="登录" value="LOGIN" />
              <el-option label="登出" value="LOGOUT" />
            </el-select>
          </el-form-item>

          <el-form-item label="目标类型">
            <el-select
              v-model="searchForm.targetType"
              placeholder="全部"
              clearable
              style="width: 120px"
            >
              <el-option label="用户" value="USER" />
              <el-option label="角色" value="ROLE" />
              <el-option label="资源" value="RESOURCE" />
              <el-option label="权限" value="PERMISSION" />
              <el-option label="菜单" value="MENU" />
            </el-select>
          </el-form-item>

          <el-form-item label="操作人">
            <el-input
              v-model="searchForm.operatorName"
              placeholder="操作人姓名/账号"
              clearable
              style="width: 150px"
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
              style="width: 360px"
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              查询
            </el-button>
            <el-button @click="handleReset">
              <el-icon><RefreshLeft /></el-icon>
              重置
            </el-button>
            <el-button @click="handleExport" :loading="exporting">
              <el-icon><Download /></el-icon>
              导出
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 日志列表 -->
        <el-table
          :data="logList"
          v-loading="loading"
          stripe
          border
          :default-sort="{ prop: 'createTime', order: 'descending' }"
        >
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="operationType" label="操作类型" width="100">
            <template #default="{ row }">
              <el-tag :type="getOperationTypeTag(row.operationType)">
                {{ getOperationTypeLabel(row.operationType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="targetType" label="目标类型" width="100">
            <template #default="{ row }">
              <el-tag :type="getTargetTypeTag(row.targetType)">
                {{ row.targetType }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="targetName" label="目标名称" min-width="150" show-overflow-tooltip />
          <el-table-column prop="operatorName" label="操作人" width="120" />
          <el-table-column prop="operatorIp" label="IP 地址" width="140" />
          <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
          <el-table-column prop="createTime" label="操作时间" width="170" sortable>
            <template #default="{ row }">
              {{ formatTime(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="handleViewDetail(row)">
                详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          class="pagination"
          @current-change="loadLogs"
          @size-change="loadLogs"
        />
      </el-card>

      <!-- 详情对话框 -->
      <el-dialog
        v-model="detailDialogVisible"
        title="审计日志详情"
        width="600px"
      >
        <el-descriptions :column="1" border v-if="currentLog">
          <el-descriptions-item label="日志 ID">{{ currentLog.id }}</el-descriptions-item>
          <el-descriptions-item label="操作类型">
            <el-tag :type="getOperationTypeTag(currentLog.operationType)">
              {{ getOperationTypeLabel(currentLog.operationType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="目标类型">{{ currentLog.targetType }}</el-descriptions-item>
          <el-descriptions-item label="目标 ID">{{ currentLog.targetId }}</el-descriptions-item>
          <el-descriptions-item label="目标名称">{{ currentLog.targetName }}</el-descriptions-item>
          <el-descriptions-item label="操作人">{{ currentLog.operatorName }}</el-descriptions-item>
          <el-descriptions-item label="操作人 ID">{{ currentLog.operatorId }}</el-descriptions-item>
          <el-descriptions-item label="IP 地址">{{ currentLog.operatorIp }}</el-descriptions-item>
          <el-descriptions-item label="操作时间">
            {{ formatTime(currentLog.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="备注">{{ currentLog.remark || '-' }}</el-descriptions-item>
          <el-descriptions-item label="详细数据">
            <el-input
              type="textarea"
              :model-value="JSON.stringify(currentLog.detailData, null, 2)"
              :rows="10"
              readonly
            />
          </el-descriptions-item>
        </el-descriptions>
      </el-dialog>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { usePermissionStore } from '@/stores/permission'
import AdminLayout from '@/layouts/AdminLayout.vue'

const permissionStore = usePermissionStore()

const loading = ref(false)
const exporting = ref(false)
const detailDialogVisible = ref(false)
const currentLog = ref(null)

// 搜索表单
const searchForm = reactive({
  operationType: '',
  targetType: '',
  operatorName: '',
  startTime: '',
  endTime: ''
})

const dateRange = ref([])

// 分页
const pagination = reactive({
  pageNum: 1,
  pageSize: 20,
  total: 0
})

// 日志列表
const logList = ref([])

// 获取操作类型标签
const getOperationTypeLabel = (type) => {
  const labelMap = {
    CREATE: '创建',
    UPDATE: '更新',
    DELETE: '删除',
    GRANT: '授权',
    REVOKE: '撤销',
    LOGIN: '登录',
    LOGOUT: '登出'
  }
  return labelMap[type] || type
}

const getOperationTypeTag = (type) => {
  const tagMap = {
    CREATE: 'success',
    UPDATE: 'primary',
    DELETE: 'danger',
    GRANT: 'warning',
    REVOKE: 'info',
    LOGIN: 'success',
    LOGOUT: 'info'
  }
  return tagMap[type] || 'info'
}

// 获取目标类型标签
const getTargetTypeTag = (type) => {
  const tagMap = {
    USER: 'primary',
    ROLE: 'success',
    RESOURCE: 'warning',
    PERMISSION: 'danger',
    MENU: 'info'
  }
  return tagMap[type] || 'info'
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 加载日志
const loadLogs = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...searchForm
    }
    
    const res = await permissionStore.loadAuditLogs(params)
    logList.value = res.records || []
    pagination.total = res.total || 0
  } catch (error) {
    console.error('加载审计日志失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  if (dateRange.value && dateRange.value.length === 2) {
    searchForm.startTime = dateRange.value[0]
    searchForm.endTime = dateRange.value[1]
  } else {
    searchForm.startTime = ''
    searchForm.endTime = ''
  }
  
  pagination.pageNum = 1
  loadLogs()
}

// 重置
const handleReset = () => {
  searchForm.operationType = ''
  searchForm.targetType = ''
  searchForm.operatorName = ''
  searchForm.startTime = ''
  searchForm.endTime = ''
  dateRange.value = []
  pagination.pageNum = 1
  loadLogs()
}

// 导出
const handleExport = async () => {
  exporting.value = true
  try {
    await permissionStore.exportPermissionAuditLogs({
      ...searchForm
    })
  } catch (error) {
    console.error('导出失败:', error)
  } finally {
    exporting.value = false
  }
}

// 查看详情
const handleViewDetail = (row) => {
  currentLog.value = row
  detailDialogVisible.value = true
}

onMounted(() => {
  loadLogs()
})
</script>

<style scoped lang="scss">
.audit-log-page {
  .header-card {
    margin-bottom: 16px;
    
    .page-header {
      h2 {
        margin: 0 0 8px 0;
        font-size: 20px;
        color: #303133;
      }
      
      .description {
        margin: 0;
        font-size: 14px;
        color: #606266;
      }
    }
  }
  
  .content-card {
    .search-form {
      margin-bottom: 16px;
    }
    
    .pagination {
      margin-top: 16px;
      justify-content: flex-end;
    }
  }
}
</style>
