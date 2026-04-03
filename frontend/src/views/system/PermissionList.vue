<template>
  <AdminLayout>
    <div class="permission-list">
      <el-card class="header-card">
        <div class="page-header">
          <h2>资源（权限）列表</h2>
          <p class="description">查看系统所有资源（权限），按模块分类展示。仅系统管理员可查看。</p>
        </div>
      </el-card>

      <el-card class="content-card">
        <!-- 搜索和筛选 -->
        <div class="toolbar">
          <el-input
            v-model="searchText"
            placeholder="搜索权限名称/编码"
            style="width: 300px"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          
          <el-select
            v-model="selectedModule"
            placeholder="选择模块"
            style="width: 200px; margin-left: 16px"
            clearable
          >
            <el-option label="全部模块" value="" />
            <el-option v-for="mod in modules" :key="mod" :label="getModuleLabel(mod)" :value="mod" />
          </el-select>
          
          <el-button @click="loadPermissions" style="margin-left: 16px">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>

        <!-- 统计卡片 -->
        <div class="stats-row">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-value">{{ totalCount }}</div>
              <div class="stat-label">权限总数</div>
            </div>
          </el-card>
          <el-card class="stat-card" shadow="hover" v-for="mod in modulesWithCount" :key="mod.code">
            <div class="stat-content">
              <div class="stat-value">{{ mod.count }}</div>
              <div class="stat-label">{{ getModuleLabel(mod.code) }}</div>
            </div>
          </el-card>
        </div>

        <!-- 权限表格 -->
        <el-table
          :data="filteredPermissions"
          v-loading="loading"
          stripe
          border
          style="margin-top: 20px"
        >
          <el-table-column prop="permissionCode" label="权限编码" width="280">
            <template #default="{ row }">
              <span class="code-text">{{ row.permissionCode }}</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="permissionName" label="权限名称" width="180" />
          
          <el-table-column prop="moduleCode" label="模块" width="150">
            <template #default="{ row }">
              <el-tag :type="getModuleTagType(row.moduleCode)" size="small">
                {{ getModuleLabel(row.moduleCode) || '未分类' }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="permissionType" label="类型" width="100">
            <template #default="{ row }">
              <el-tag :type="getTypeTagType(row.permissionType)" size="small">
                {{ row.permissionType || 'ACTION' }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="description" label="描述" min-width="200" />
          
          <el-table-column prop="isBase" label="基本权限" width="100" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.isBase" type="success" size="small">是</el-tag>
              <el-tag v-else type="info" size="small">否</el-tag>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="filteredPermissions.length"
            :page-sizes="[20, 50, 100]"
            layout="total, sizes, prev, pager, next"
          />
        </div>
      </el-card>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import AdminLayout from '@/layouts/AdminLayout.vue'
import request from '@/utils/request'

// 数据
const loading = ref(false)
const permissions = ref([])
const searchText = ref('')
const selectedModule = ref('')
const currentPage = ref(1)
const pageSize = ref(50)

// 模块列表（根据实际权限数据动态生成）
const modules = computed(() => {
  const mods = new Set()
  permissions.value.forEach(p => {
    if (p.moduleCode) mods.add(p.moduleCode)
  })
  return Array.from(mods).sort()
})

// 模块统计
const modulesWithCount = computed(() => {
  const counts = {}
  permissions.value.forEach(p => {
    const mod = p.moduleCode || 'uncategorized'
    counts[mod] = (counts[mod] || 0) + 1
  })
  return Object.entries(counts).map(([code, count]) => ({ code, count })).sort((a, b) => b.count - a.count)
})

// 总数
const totalCount = computed(() => permissions.value.length)

// 过滤后的权限
const filteredPermissions = computed(() => {
  let result = permissions.value
  
  if (searchText.value) {
    const search = searchText.value.toLowerCase()
    result = result.filter(p => 
      p.permissionCode?.toLowerCase().includes(search) ||
      p.permissionName?.toLowerCase().includes(search)
    )
  }
  
  if (selectedModule.value) {
    result = result.filter(p => p.moduleCode === selectedModule.value)
  }
  
  return result
})

// 模块标签映射
const moduleLabels = {
  'system': '系统管理',
  'auth': '认证管理',
  'user': '用户管理',
  'role': '角色管理',
  'menu': '菜单管理',
  'permission': '权限管理',
  'company': '公司管理',
  'workarea': '作业区管理',
  'document': '文档管理',
  'audit': '审计管理'
}

const getModuleLabel = (code) => {
  return moduleLabels[code] || code || '未分类'
}

const getModuleTagType = (code) => {
  const types = {
    'system': 'primary',
    'auth': 'warning',
    'user': 'success',
    'role': 'info',
    'menu': '',
    'permission': 'danger',
    'company': 'success',
    'workarea': 'info'
  }
  return types[code] || 'info'
}

const getTypeTagType = (type) => {
  const types = {
    'VIEW': 'success',
    'ACTION': 'primary',
    'ADMIN': 'danger'
  }
  return types[type] || ''
}

// 加载权限列表
const loadPermissions = async () => {
  loading.value = true
  try {
    const response = await request.get('/permission/list')
    if (response.data) {
      permissions.value = response.data
    }
  } catch (error) {
    ElMessage.error('加载权限列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 初始化
onMounted(() => {
  loadPermissions()
})
</script>

<style scoped lang="scss">
.permission-list {
  .header-card {
    margin-bottom: 20px;
    
    .page-header {
      h2 {
        margin: 0 0 8px 0;
        font-size: 20px;
        font-weight: 600;
      }
      
      .description {
        margin: 0;
        color: #909399;
        font-size: 14px;
      }
    }
  }
  
  .content-card {
    .toolbar {
      display: flex;
      align-items: center;
      margin-bottom: 20px;
    }
    
    .stats-row {
      display: flex;
      gap: 16px;
      margin-bottom: 20px;
      
      .stat-card {
        width: 120px;
        
        .stat-content {
          text-align: center;
          
          .stat-value {
            font-size: 24px;
            font-weight: 600;
            color: #409eff;
          }
          
          .stat-label {
            font-size: 12px;
            color: #909399;
            margin-top: 4px;
          }
        }
      }
    }
    
    .code-text {
      font-family: monospace;
      color: #606266;
    }
    
    .pagination-wrapper {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }
}
</style>