<template>
  <div class="data-scope-adjustment-log">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>数据范围调整日志</span>
        </div>
      </template>
      
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="roleId" label="角色ID" width="100" />
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="moduleCode" label="模块编码" width="150" />
        <el-table-column prop="oldScope" label="原数据范围" />
        <el-table-column prop="newScope" label="新数据范围" />
        <el-table-column prop="adjustedBy" label="调整人" width="120" />
        <el-table-column prop="createdAt" label="调整时间" width="180" />
      </el-table>
      
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const fetchData = async () => {
  loading.value = true
  try {
    // TODO: 调用后端 API
    // const response = await api.getDataScopeAdjustmentLogs({ page: currentPage.value, size: pageSize.value })
    // tableData.value = response.data.content
    // total.value = response.data.total
    
    // 暂时使用空数据
    tableData.value = []
    total.value = 0
    ElMessage.info('数据范围调整日志功能开发中')
  } catch (error) {
    ElMessage.error('加载失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (size) => {
  pageSize.value = size
  fetchData()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchData()
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.data-scope-adjustment-log {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.el-pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>