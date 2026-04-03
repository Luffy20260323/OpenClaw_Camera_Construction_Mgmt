<template>
  <AdminLayout>
    <div class="resource-validation">
      <el-card class="header-card">
        <div class="page-header">
          <h2>资源完整性检查</h2>
          <p class="description">检测孤儿资源、循环引用等数据完整性问题，确保资源数据健康。</p>
        </div>
      </el-card>

      <el-card class="content-card">
        <!-- 操作按钮 -->
        <div class="toolbar">
          <el-button type="primary" @click="runAllChecks" :loading="checking">
            <el-icon><Refresh /></el-icon>
            执行完整性检查
          </el-button>
          <el-button @click="checkOrphaned" :loading="checkingOrphaned">
            检测孤儿资源
          </el-button>
          <el-button @click="checkCircular" :loading="checkingCircular">
            检测循环引用
          </el-button>
        </div>

        <!-- 总体状态 -->
        <el-alert
          v-if="overallStatus"
          :title="overallStatus.message"
          :type="overallStatus.status === 'healthy' ? 'success' : 'warning'"
          :closable="false"
          show-icon
          style="margin-bottom: 20px"
        >
          <template #default>
            <div class="status-details">
              <span>孤儿资源：<strong>{{ orphanedCount }}</strong> 个</span>
              <span style="margin-left: 20px">循环引用：<strong>{{ circularCount }}</strong> 个</span>
            </div>
          </template>
        </el-alert>

        <!-- 孤儿资源列表 -->
        <el-card v-if="orphanedResources.length > 0" class="result-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>🔴 孤儿资源（{{ orphanedResources.length }}个）</span>
              <el-tag type="danger">需要修复</el-tag>
            </div>
          </template>
          <el-table :data="orphanedResources" stripe border>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="name" label="名称" width="200" />
            <el-table-column prop="type" label="类型" width="100">
              <template #default="{ row }">
                <el-tag size="small">{{ row.type }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="parentId" label="父资源 ID" width="100" />
            <el-table-column label="问题" min-width="200">
              <template #default="{ row }">
                <el-tag type="warning" size="small">
                  父资源不存在（期望父 ID: {{ row.parentId }}）
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <!-- 循环引用列表 -->
        <el-card v-if="circularReferences.length > 0" class="result-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>🟠 循环引用（{{ circularReferences.length }}个）</span>
              <el-tag type="warning">需要修复</el-tag>
            </div>
          </template>
          <el-table :data="circularReferences" stripe border>
            <el-table-column prop="resource.id" label="资源 ID" width="80" />
            <el-table-column prop="resource.name" label="名称" width="200" />
            <el-table-column label="循环路径" min-width="400">
              <template #default="{ row }">
                <div class="cycle-path">
                  <span v-for="(item, index) in row.path" :key="index">
                    {{ item }}
                    <span v-if="index < row.path.length - 1" class="arrow"> → </span>
                  </span>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <!-- 空状态 -->
        <el-empty v-if="!checking && orphanedResources.length === 0 && circularReferences.length === 0 && !overallStatus" description="点击按钮开始检查" />
      </el-card>
    </div>
  </AdminLayout>
</template>

<script setup>
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import AdminLayout from '@/layouts/AdminLayout.vue'

const userStore = useUserStore()

// 加载状态
const checking = ref(false)
const checkingOrphaned = ref(false)
const checkingCircular = ref(false)

// 检查结果
const overallStatus = ref(null)
const orphanedResources = ref([])
const circularReferences = ref([])
const orphanedCount = ref(0)
const circularCount = ref(0)

// 执行完整性检查
const runAllChecks = async () => {
  checking.value = true
  try {
    const response = await request.get('/resource/validation/check')
    if (response.data) {
      overallStatus.value = {
        status: response.data.status,
        message: response.data.message
      }
      orphanedCount.value = response.data.orphaned.count
      circularCount.value = response.data.circular.count
      
      orphanedResources.value = response.data.orphaned.resources || []
      circularReferences.value = response.data.circular.references || []
      
      if (response.data.status === 'healthy') {
        ElMessage.success('资源数据完整性良好！')
      } else {
        ElMessage.warning(`发现 ${orphanedCount.value + circularCount.value} 个问题，请查看下方详情`)
      }
    }
  } catch (error) {
    ElMessage.error('检查失败：' + (error.response?.data?.message || error.message))
    console.error(error)
  } finally {
    checking.value = false
  }
}

// 检测孤儿资源
const checkOrphaned = async () => {
  checkingOrphaned.value = true
  try {
    const response = await request.get('/resource/validation/orphaned')
    if (response.data) {
      orphanedCount.value = response.data.count
      orphanedResources.value = response.data.resources || []
      
      if (orphanedResources.value.length === 0) {
        ElMessage.success('未发现孤儿资源')
      } else {
        ElMessage.warning(`发现 ${orphanedCount.value} 个孤儿资源`)
      }
    }
  } catch (error) {
    ElMessage.error('检测失败：' + (error.response?.data?.message || error.message))
    console.error(error)
  } finally {
    checkingOrphaned.value = false
  }
}

// 检测循环引用
const checkCircular = async () => {
  checkingCircular.value = true
  try {
    const response = await request.get('/resource/validation/circular')
    if (response.data) {
      circularCount.value = response.data.count
      circularReferences.value = response.data.references || []
      
      if (circularReferences.value.length === 0) {
        ElMessage.success('未发现循环引用')
      } else {
        ElMessage.warning(`发现 ${circularCount.value} 个循环引用`)
      }
    }
  } catch (error) {
    ElMessage.error('检测失败：' + (error.response?.data?.message || error.message))
    console.error(error)
  } finally {
    checkingCircular.value = false
  }
}
</script>

<style scoped>
.resource-validation {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px;
  font-size: 20px;
  font-weight: 600;
}

.page-header .description {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.content-card {
  margin-bottom: 20px;
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
}

.status-details {
  display: flex;
  gap: 20px;
  margin-top: 10px;
  font-size: 14px;
}

.result-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.cycle-path {
  font-family: monospace;
  font-size: 13px;
  color: #666;
  line-height: 1.6;
}

.arrow {
  color: #999;
}
</style>
