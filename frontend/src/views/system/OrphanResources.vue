<template>
    <div class="orphan-resources">
      <el-card class="header-card">
        <div class="page-header">
          <h2>孤儿资源管理</h2>
          <p class="description">
            管理没有父资源的孤儿资源。这些资源可能是因为父资源被删除或配置错误导致的。
            建议为这些资源指定合适的父资源，或确认它们是顶级资源。
          </p>
        </div>
      </el-card>

      <el-card class="content-card">
        <!-- 统计信息 -->
        <el-alert
          v-if="orphanResources.length > 0"
          type="warning"
          :title="`发现 ${orphanResources.length} 个孤儿资源`"
          description="请检查这些资源是否需要指定父资源，或确认它们是顶级资源。"
          show-icon
          class="mb-4"
        />
        <el-alert
          v-else
          type="success"
          title="资源结构完整"
          description="所有资源都有正确的父资源关联。"
          show-icon
          class="mb-4"
        />

        <!-- 工具栏 -->
        <div class="toolbar mb-4">
          <el-button type="primary" @click="handleRefresh">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
          <el-button @click="handleValidateIntegrity">
            <el-icon><CircleCheck /></el-icon>
            完整性校验
          </el-button>
          <el-button @click="handleBatchAssign">
            <el-icon><FolderAdd /></el-icon>
            批量指定父资源
          </el-button>
        </div>

        <!-- 孤儿资源列表 -->
        <el-table
          :data="orphanResources"
          v-loading="loading"
          stripe
          border
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="name" label="资源名称" min-width="200">
            <template #default="{ row }">
              <span class="resource-name">{{ row.name }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="code" label="资源编码" width="200">
            <template #default="{ row }">
              <span class="resource-code">{{ row.code }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="type" label="资源类型" width="120">
            <template #default="{ row }">
              <el-tag :type="getTypeTag(row.type)">
                {{ getTypeLabel(row.type) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="sort" label="排序" width="80" />
          <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="handleAssignParent(row)">
                <el-icon><FolderAdd /></el-icon>
                指定父资源
              </el-button>
              <el-button
                v-if="row.type === 'MODULE'"
                size="small"
                type="success"
                @click="handleMarkAsTop(row)"
              >
                <el-icon><Star /></el-icon>
                设为顶级
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 指定父资源对话框 -->
      <el-dialog
        v-model="dialogVisible"
        title="指定父资源"
        width="500px"
        @close="handleDialogClose"
      >
        <el-form :model="form" label-width="100px">
          <el-form-item label="当前资源">
            <el-input v-model="currentResource.name" disabled />
          </el-form-item>
          <el-form-item label="父资源" required>
            <ParentResourceSelector
              v-model="form.parentId"
              :current-resource-id="currentResource.id"
              :resource-tree="resourceTree"
              placeholder="请选择父资源（留空表示顶级资源）"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            确定
          </el-button>
        </template>
      </el-dialog>

      <!-- 批量指定父资源对话框 -->
      <el-dialog
        v-model="batchDialogVisible"
        title="批量指定父资源"
        width="500px"
        @close="handleBatchDialogClose"
      >
        <el-alert
          type="info"
          :title="`已选择 ${selectedIds.length} 个资源`"
          :closable="false"
          class="mb-4"
        />
        <el-form :model="batchForm" label-width="100px">
          <el-form-item label="父资源" required>
            <ParentResourceSelector
              v-model="batchForm.parentId"
              :current-resource-id="null"
              :resource-tree="resourceTree"
              placeholder="请选择父资源"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="batchDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleBatchSubmit" :loading="submitting">
            确定
          </el-button>
        </template>
      </el-dialog>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useResourceStore } from '@/stores/resource'
import ParentResourceSelector from '@/components/ParentResourceSelector.vue'

const resourceStore = useResourceStore()

const loading = ref(false)
const submitting = ref(false)
const orphanResources = ref([])
const resourceTree = ref([])
const selectedIds = ref([])

// 对话框
const dialogVisible = ref(false)
const batchDialogVisible = ref(false)

// 表单
const currentResource = ref({})
const form = reactive({
  parentId: null
})

const batchForm = reactive({
  parentId: null
})

// 获取类型标签
const getTypeLabel = (type) => {
  const labelMap = {
    MODULE: '模块',
    MENU: '菜单',
    PAGE: '页面',
    ELEMENT: '元素',
    API: 'API',
    PERMISSION: '权限'
  }
  return labelMap[type] || type
}

// 获取类型标签颜色
const getTypeTag = (type) => {
  const tagMap = {
    MODULE: 'warning',
    MENU: 'success',
    PAGE: 'primary',
    ELEMENT: 'info',
    API: 'danger',
    PERMISSION: 'warning'
  }
  return tagMap[type] || 'info'
}

// 加载孤儿资源
const loadOrphanResources = async () => {
  loading.value = true
  try {
    const data = await resourceStore.loadOrphanResources()
    orphanResources.value = data
  } catch (error) {
    console.error('加载孤儿资源失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载资源树
const loadResourceTree = async () => {
  try {
    const data = await resourceStore.loadResourceTree()
    resourceTree.value = data
  } catch (error) {
    console.error('加载资源树失败:', error)
  }
}

// 刷新
const handleRefresh = () => {
  loadOrphanResources()
  loadResourceTree()
  ElMessage.success('刷新成功')
}

// 完整性校验
const handleValidateIntegrity = async () => {
  try {
    const result = await resourceStore.validateResourceIntegrity()
    ElMessageBox.alert(
      `校验完成：${result.message || '资源结构完整'}`,
      '完整性校验结果',
      {
        type: result.hasIssues ? 'warning' : 'success'
      }
    )
  } catch (error) {
    console.error('完整性校验失败:', error)
    ElMessage.error('完整性校验失败')
  }
}

// 选择变更
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

// 指定父资源
const handleAssignParent = (row) => {
  currentResource.value = { ...row }
  form.parentId = row.parentId || null
  dialogVisible.value = true
}

// 设为顶级
const handleMarkAsTop = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定将 "${row.name}" 设为顶级资源吗？`,
      '确认操作',
      { type: 'warning' }
    )
    
    await resourceStore.updateResource(row.id, { parentId: null })
    await loadOrphanResources()
    ElMessage.success('设置成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('设置顶级资源失败:', error)
    }
  }
}

// 批量指定父资源
const handleBatchAssign = () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择资源')
    return
  }
  batchForm.parentId = null
  batchDialogVisible.value = true
}

// 提交单个
const handleSubmit = async () => {
  if (!form.parentId) {
    ElMessage.warning('请选择父资源')
    return
  }
  
  submitting.value = true
  try {
    await resourceStore.updateResource(currentResource.value.id, {
      parentId: form.parentId
    })
    dialogVisible.value = false
    await loadOrphanResources()
    ElMessage.success('更新成功')
  } catch (error) {
    console.error('更新失败:', error)
  } finally {
    submitting.value = false
  }
}

// 批量提交
const handleBatchSubmit = async () => {
  if (!batchForm.parentId) {
    ElMessage.warning('请选择父资源')
    return
  }
  
  submitting.value = true
  try {
    // 批量更新
    const promises = selectedIds.value.map(id =>
      resourceStore.updateResource(id, { parentId: batchForm.parentId })
    )
    await Promise.all(promises)
    
    batchDialogVisible.value = false
    await loadOrphanResources()
    ElMessage.success(`批量更新 ${selectedIds.value.length} 个资源成功`)
  } catch (error) {
    console.error('批量更新失败:', error)
  } finally {
    submitting.value = false
  }
}

// 对话框关闭
const handleDialogClose = () => {
  currentResource.value = {}
  form.parentId = null
}

const handleBatchDialogClose = () => {
  batchForm.parentId = null
  selectedIds.value = []
}

onMounted(() => {
  loadOrphanResources()
  loadResourceTree()
})
</script>

<style scoped lang="scss">
.orphan-resources {
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
    .toolbar {
      display: flex;
      gap: 12px;
    }
    
    .mb-4 {
      margin-bottom: 16px;
    }
    
    .resource-name {
      font-weight: 500;
    }
    
    .resource-code {
      font-size: 12px;
      color: #909399;
      font-family: monospace;
    }
  }
}
</style>
