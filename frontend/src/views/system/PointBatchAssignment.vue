<template>
  <AdminLayout>
    <div class="point-batch-assignment-page">
      <!-- 页面头部 -->
      <div class="page-header">
        <div class="header-left">
          <h1>📍 点位批量分配</h1>
          <p class="subtitle">将设备模型实例批量分配给点位，实现快速配置</p>
        </div>
      </div>

      <!-- 分配操作区 -->
      <div class="assignment-section">
        <el-card class="assignment-card">
          <template #header>
            <div class="card-header">
              <span>🎯 批量分配设备</span>
            </div>
          </template>

          <el-form :model="assignmentForm" label-width="140px" size="default">
            <el-form-item label="选择设备模型实例">
              <el-select
                v-model="assignmentForm.modelInstanceId"
                placeholder="请选择设备模型实例"
                style="width: 400px"
                filterable
              >
                <el-option
                  v-for="model in modelList"
                  :key="model.id"
                  :label="model.name"
                  :value="model.id"
                >
                  <span style="float: left">{{ model.name }}</span>
                  <span style="float: right; color: #8492a6; font-size: 13px">{{ model.code }}</span>
                </el-option>
              </el-select>
              <span class="form-tip">选择要分配的设备模型实例</span>
            </el-form-item>

            <el-form-item label="选择待分配点位">
              <div class="point-selector">
                <div class="selector-toolbar">
                  <el-input
                    v-model="pointSearchKeyword"
                    placeholder="搜索点位名称或编码..."
                    prefix-icon="Search"
                    clearable
                    style="width: 300px"
                    @input="filterPoints"
                  />
                  <el-button @click="selectAll" :disabled="filteredPoints.length === 0">
                    全选
                  </el-button>
                  <el-button @click="clearSelection">清空选择</el-button>
                  <span class="selection-count">已选择 {{ selectedPointIds.length }} 个点位</span>
                </div>

                <el-table
                  :data="filteredPoints"
                  border
                  stripe
                  style="margin-top: 12px"
                  max-height="400"
                  @selection-change="handleSelectionChange"
                >
                  <el-table-column type="selection" width="55" align="center" />
                  <el-table-column type="index" label="序号" width="60" align="center" />
                  <el-table-column prop="pointCode" label="点位编码" width="150" />
                  <el-table-column prop="pointName" label="点位名称" min-width="200" />
                  <el-table-column prop="workAreaName" label="作业区" min-width="150" />
                  <el-table-column prop="companyName" label="公司" min-width="150" />
                  <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
                </el-table>
              </div>
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                @click="handleBatchAssign"
                :loading="assigning"
                :disabled="!assignmentForm.modelInstanceId || selectedPointIds.length === 0"
              >
                <el-icon><Check /></el-icon>
                执行分配
              </el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>

      <!-- 分配结果区 -->
      <div class="result-section" v-if="assignmentHistory.length > 0">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>📋 分配结果</span>
              <el-button size="small" @click="clearHistory" text>清空记录</el-button>
            </div>
          </template>

          <el-table :data="assignmentHistory" border stripe>
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="pointCode" label="点位编码" width="150" />
            <el-table-column prop="pointName" label="点位名称" min-width="180" />
            <el-table-column prop="modelName" label="设备模型" min-width="180" />
            <el-table-column prop="assignedByName" label="分配人" width="120" />
            <el-table-column prop="assignedAt" label="分配时间" width="180" />
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag type="success" size="small">成功</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Check, Search } from '@element-plus/icons-vue'
import AdminLayout from '@/layouts/AdminLayout.vue'
import { 
  getAvailablePoints, 
  batchAssign, 
  getPointDeviceModels,
  getPointDeviceConfig
} from '@/api/pointBatchAssignment'

// 设备模型列表
const modelList = ref([])

// 点位列表
const allPoints = ref([])
const filteredPoints = ref([])
const pointSearchKeyword = ref('')

// 已选择的点位 ID
const selectedPointIds = ref([])
const selectedPoints = ref([])

// 分配表单
const assignmentForm = reactive({
  modelInstanceId: null
})

// 分配状态
const assigning = ref(false)

// 分配历史
const assignmentHistory = ref([])

// 过滤点位
const filterPoints = () => {
  if (!pointSearchKeyword.value) {
    filteredPoints.value = allPoints.value
    return
  }
  
  const keyword = pointSearchKeyword.value.toLowerCase()
  filteredPoints.value = allPoints.value.filter(point => 
    point.pointName.toLowerCase().includes(keyword) ||
    point.pointCode.toLowerCase().includes(keyword)
  )
}

// 全选
const selectAll = () => {
  // 使用表格的 toggleAllSelection 方法
  // 这里我们通过设置 selectedPointIds 来实现
  selectedPointIds.value = filteredPoints.value.map(p => p.id)
  selectedPoints.value = [...filteredPoints.value]
}

// 清空选择
const clearSelection = () => {
  selectedPointIds.value = []
  selectedPoints.value = []
}

// 处理选择变化
const handleSelectionChange = (selection) => {
  selectedPointIds.value = selection.map(item => item.id)
  selectedPoints.value = selection
}

// 加载设备模型列表
const loadModelList = async () => {
  try {
    const res = await getPointDeviceModels()
    if (res.code === 200 && res.data) {
      modelList.value = res.data.items || res.data.list || []
    }
  } catch (error) {
    console.error('加载设备模型列表失败:', error)
  }
}

// 加载可用点位
const loadAvailablePoints = async () => {
  try {
    const res = await getAvailablePoints()
    if (res.code === 200 && res.data) {
      allPoints.value = res.data
      filteredPoints.value = res.data
    }
  } catch (error) {
    console.error('加载可用点位失败:', error)
    ElMessage.error('加载可用点位失败')
  }
}

// 执行批量分配
const handleBatchAssign = async () => {
  if (!assignmentForm.modelInstanceId) {
    ElMessage.warning('请选择设备模型实例')
    return
  }
  
  if (selectedPointIds.value.length === 0) {
    ElMessage.warning('请选择待分配的点位')
    return
  }
  
  try {
    assigning.value = true
    
    const res = await batchAssign({
      modelInstanceId: assignmentForm.modelInstanceId,
      pointIds: selectedPointIds.value
    })
    
    if (res.code === 200) {
      ElMessage.success(res.message || `成功分配 ${res.data} 个点位`)
      
      // 添加到历史记录
      const selectedModel = modelList.value.find(m => m.id === assignmentForm.modelInstanceId)
      selectedPoints.value.forEach(point => {
        assignmentHistory.value.unshift({
          pointCode: point.pointCode,
          pointName: point.pointName,
          modelName: selectedModel ? selectedModel.name : '未知模型',
          assignedByName: '当前用户',
          assignedAt: new Date().toLocaleString()
        })
      })
      
      // 重置表单并重新加载点位
      resetForm()
      await loadAvailablePoints()
    }
  } catch (error) {
    console.error('批量分配失败:', error)
    // 错误消息已在 request 拦截器中显示
  } finally {
    assigning.value = false
  }
}

// 重置表单
const resetForm = () => {
  assignmentForm.modelInstanceId = null
  clearSelection()
  pointSearchKeyword.value = ''
  filterPoints()
}

// 清空历史
const clearHistory = () => {
  assignmentHistory.value = []
}

// 组件挂载时加载数据
onMounted(() => {
  loadModelList()
  loadAvailablePoints()
})
</script>

<style scoped lang="scss">
.point-batch-assignment-page {
  height: 100%;
  
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    
    .header-left {
      h1 {
        margin: 0 0 8px 0;
        font-size: 24px;
        font-weight: 600;
        color: #303133;
      }
      
      .subtitle {
        margin: 0;
        font-size: 14px;
        color: #909399;
      }
    }
  }
  
  .assignment-section {
    margin-bottom: 24px;
    
    .assignment-card {
      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-weight: 600;
        font-size: 16px;
      }
      
      .form-tip {
        font-size: 12px;
        color: #909399;
        margin-left: 12px;
      }
      
      .point-selector {
        width: 100%;
        
        .selector-toolbar {
          display: flex;
          align-items: center;
          gap: 12px;
          
          .selection-count {
            margin-left: auto;
            font-size: 14px;
            color: #606266;
            
            strong {
              color: #409EFF;
            }
          }
        }
      }
    }
  }
  
  .result-section {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-weight: 600;
      font-size: 16px;
    }
  }
}
</style>
