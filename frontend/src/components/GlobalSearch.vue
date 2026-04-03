<template>
  <div class="global-search">
    <el-autocomplete
      v-model="searchText"
      :fetch-suggestions="searchResources"
      placeholder="搜索资源（支持名称、编码、权限标识）"
      style="width: 400px"
      clearable
      @select="handleSelect"
      @keyup.enter="handleEnter"
    >
      <template #prefix>
        <el-icon><Search /></el-icon>
      </template>
      <template #default="{ item }">
        <div class="search-item">
          <div class="item-header">
            <span class="item-name">{{ item.name }}</span>
            <el-tag size="small" :type="getTypeTagType(item.type)">
              {{ getTypeLabel(item.type) }}
            </el-tag>
          </div>
          <div class="item-meta">
            <span class="item-code">{{ item.code }}</span>
            <span v-if="item.moduleCode" class="item-module">
              {{ getModuleLabel(item.moduleCode) }}
            </span>
          </div>
        </div>
      </template>
    </el-autocomplete>

    <!-- 搜索结果对话框 -->
    <el-dialog
      v-model="showResults"
      title="搜索结果"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-table :data="searchResults" stripe border>
        <el-table-column prop="name" label="名称" width="200">
          <template #default="{ row }">
            <span class="name-text">
              <el-icon v-if="row.icon" style="margin-right: 4px"><component :is="row.icon" /></el-icon>
              {{ row.name }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="getTypeTagType(row.type)">
              {{ getTypeLabel(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="moduleCode" label="模块" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.moduleCode" type="info" size="small">
              {{ getModuleLabel(row.moduleCode) }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="permissionKey" label="权限标识" min-width="200">
          <template #default="{ row }">
            <span v-if="row.permissionKey" class="code-text">{{ row.permissionKey }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="navigateToResource(row)">
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="showResults = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { Search } from '@element-plus/icons-vue'

const router = useRouter()

const searchText = ref('')
const showResults = ref(false)
const searchResults = ref([])
const allResources = ref([])

// 类型标签
const typeLabels = {
  MODULE: '模块',
  MENU: '菜单',
  PAGE: '页面',
  ELEMENT: '元素',
  API: 'API',
  PERMISSION: '权限'
}

const moduleLabels = {
  system: '系统设置',
  component: '零部件管理',
  auth: '认证管理',
  user: '用户管理',
  role: '角色管理',
  menu: '菜单管理',
  permission: '权限管理',
  base: '基础配置',
  audit: '审计日志',
  resource: '资源管理',
  config: '系统配置',
  company: '公司管理',
  workarea: '作业区管理',
  point: '点位管理'
}

const getTypeLabel = (type) => typeLabels[type] || type
const getModuleLabel = (code) => moduleLabels[code] || code || '未分类'

const getTypeTagType = (type) => {
  const colors = {
    MODULE: '',
    MENU: 'success',
    PAGE: 'warning',
    ELEMENT: 'info',
    API: 'danger',
    PERMISSION: 'warning'
  }
  return colors[type] || ''
}

// 加载所有资源
const loadAllResources = async () => {
  try {
    const response = await request.get('/resource/overview')
    if (response.data && response.data.tree) {
      // 扁平化树形数据
      const flatten = (nodes) => {
        const result = []
        for (const node of nodes) {
          result.push(node)
          if (node.children && node.children.length > 0) {
            result.push(...flatten(node.children))
          }
        }
        return result
      }
      allResources.value = flatten(response.data.tree)
    }
  } catch (error) {
    console.error('加载资源失败:', error)
  }
}

// 搜索资源
const searchResources = (queryString, cb) => {
  const results = queryString
    ? allResources.value.filter(createFilter(queryString))
    : allResources.value
  
  // 限制返回数量
  const limited = results.slice(0, 10)
  cb(limited.map(item => ({
    ...item,
    value: `${item.name} (${item.code})`
  })))
}

const createFilter = (queryString) => {
  return (resource) => {
    const query = queryString.toLowerCase()
    return (
      (resource.name && resource.name.toLowerCase().includes(query)) ||
      (resource.code && resource.code.toLowerCase().includes(query)) ||
      (resource.permissionKey && resource.permissionKey.toLowerCase().includes(query))
    )
  }
}

// 处理选择
const handleSelect = (item) => {
  navigateToResource(item)
}

// 处理回车
const handleEnter = () => {
  if (!searchText.value) return
  
  // 执行完整搜索
  const results = allResources.value.filter(createFilter(searchText.value))
  searchResults.value = results
  
  if (results.length === 0) {
    ElMessage.warning('未找到匹配的资源')
  } else {
    showResults.value = true
  }
}

// 导航到资源
const navigateToResource = (resource) => {
  // 跳转到资源管理页面并高亮该资源
  router.push({
    path: '/system/resources',
    query: { highlight: resource.id }
  })
  showResults.value = false
  searchText.value = ''
}

// 初始化
loadAllResources()
</script>

<style scoped>
.global-search {
  display: inline-block;
}

.search-item {
  padding: 8px 0;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.item-name {
  font-weight: 500;
  color: #303133;
}

.item-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #909399;
}

.item-code {
  font-family: monospace;
}

.name-text {
  display: flex;
  align-items: center;
  font-weight: 500;
}

.code-text {
  font-family: monospace;
  color: #666;
  font-size: 13px;
}
</style>
