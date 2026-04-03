<template>
  <AdminLayout>
    <div class="resource-list">
      <el-card class="header-card">
        <div class="page-header">
          <h2>资源管理</h2>
          <p class="description">管理系统所有资源（模块、菜单、页面、元素、API、权限），按层级结构展示。可编辑名称和显示顺序，可创建新模块。</p>
        </div>
      </el-card>

      <el-card class="content-card">
        <!-- 统计卡片 -->
        <div class="stats-row">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-value">{{ totalCount }}</div>
              <div class="stat-label">资源总数</div>
            </div>
          </el-card>
          <el-card class="stat-card" shadow="hover" v-for="stat in stats" :key="stat.type">
            <div class="stat-content">
              <div class="stat-value">{{ stat.totalCount }}</div>
              <div class="stat-label">{{ getTypeLabel(stat.type) }}</div>
            </div>
          </el-card>
        </div>

        <!-- 工具栏 -->
        <div class="toolbar">
          <el-input
            v-model="searchText"
            placeholder="搜索资源名称/编码"
            style="width: 300px"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          
          <el-select
            v-model="selectedType"
            placeholder="选择资源类型"
            style="width: 160px; margin-left: 12px"
            clearable
          >
            <el-option label="全部类型" value="" />
            <el-option v-for="t in resourceTypes" :key="t" :label="getTypeLabel(t)" :value="t" />
          </el-select>

          <el-button type="primary" @click="showCreateDialog" style="margin-left: 12px">
            <el-icon><Plus /></el-icon>
            新建模块
          </el-button>
          
          <el-button @click="loadResources" style="margin-left: 12px">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>

          <el-button @click="toggleExpandAll" style="margin-left: 12px">
            <el-icon><component :is="expandAll ? 'Fold' : 'Expand'" /></el-icon>
            {{ expandAll ? '收起全部' : '展开全部' }}
          </el-button>

          <el-button 
            v-if="selectedIds.length > 0" 
            type="danger" 
            plain
            @click="batchDelete"
            style="margin-left: 12px"
          >
            <el-icon><Delete /></el-icon>
            批量删除 ({{ selectedIds.length }})
          </el-button>
        </div>

        <!-- 资源树形表格 -->
        <div class="table-container">
          <el-table
            :data="paginatedResources"
            v-loading="loading"
            row-key="id"
            :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
            :default-expand-all="expandAll"
            stripe
            border
            :height="tableHeight"
            :max-height="tableMaxHeight"
            @selection-change="handleSelectionChange"
          >
            <el-table-column type="selection" width="55" :selectable="canSelect" />
          <el-table-column label="拖拽" width="60" align="center">
            <template #default>
              <el-icon class="drag-handle" style="cursor: move"><Rank /></el-icon>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="资源名称" width="220">
            <template #default="{ row }">
              <span class="name-text">
                <el-icon v-if="row.icon" style="margin-right: 4px"><component :is="row.icon" /></el-icon>
                {{ row.name }}
              </span>
            </template>
          </el-table-column>
          
          <el-table-column prop="type" label="类型" width="100">
            <template #default="{ row }">
              <el-tag :type="getTypeTagType(row.type)" size="small">
                {{ getTypeLabel(row.type) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="moduleCode" label="模块" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.moduleCode" type="info" size="small">
                {{ getModuleLabel(row.moduleCode) }}
              </el-tag>
              <span v-else class="empty-text">-</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="sortOrder" label="顺序" width="80" align="center">
            <template #default="{ row }">
              <span>{{ row.sortOrder }}</span>
            </template>
          </el-table-column>
          
          <el-table-column label="父资源" width="180">
            <template #default="{ row }">
              <span v-if="row.parentId" class="code-text">{{ getParentName(row.parentId) }}</span>
              <span v-else class="empty-text">-</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="path" label="路径" width="180">
            <template #default="{ row }">
              <span v-if="row.path" class="code-text">{{ row.path }}</span>
              <span v-else class="empty-text">-</span>
            </template>
          </el-table-column>
          
          <el-table-column label="可见" width="80" align="center" v-if="showMenuColumns">
            <template #default="{ row }">
              <el-switch
                v-if="row.type === 'MENU'"
                v-model="row.isVisible"
                @change="updateMenuVisibility(row)"
                size="small"
              />
              <span v-else class="empty-text">-</span>
            </template>
          </el-table-column>
          
          <el-table-column label="保护" width="80" align="center" v-if="showMenuColumns">
            <template #default="{ row }">
              <el-tag v-if="row.isSystemProtected" type="danger" size="small">保护</el-tag>
              <el-tag v-else-if="row.type === 'MENU'" type="success" size="small">可编辑</el-tag>
              <span v-else class="empty-text">-</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="permissionKey" label="权限标识" width="200">
            <template #default="{ row }">
              <span v-if="row.permissionKey" class="code-text">{{ row.permissionKey }}</span>
              <span v-else class="empty-text">-</span>
            </template>
          </el-table-column>
          
          <el-table-column label="关联 API" min-width="200">
            <template #default="{ row }">
              <template v-if="row.type !== 'API'">
                <span v-if="relatedApiCount[row.id] !== undefined" class="code-text">
                  {{ relatedApiCount[row.id] }} 个 API
                </span>
                <el-popover
                  v-else
                  placement="right"
                  :width="400"
                  trigger="click"
                >
                  <template #reference>
                    <el-button type="primary" link size="small" @click="loadRelatedApis(row.id)">
                      查看关联 API
                    </el-button>
                  </template>
                  <div class="related-apis-popover">
                    <div v-if="loadingRelatedApis" class="loading-text">加载中...</div>
                    <div v-else-if="!relatedApis[row.id] || relatedApis[row.id].length === 0" class="empty-text">
                      暂无关联 API
                    </div>
                    <el-table v-else :data="relatedApis[row.id]" size="small" :show-header="true">
                      <el-table-column prop="name" label="名称" min-width="120" />
                      <el-table-column prop="permissionKey" label="权限码" min-width="180">
                        <template #default="{ row: apiRow }">
                          <span class="code-text">{{ apiRow.permissionKey }}</span>
                        </template>
                      </el-table-column>
                    </el-table>
                  </div>
                </el-popover>
              </template>
              <span v-else class="empty-text">-</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="description" label="描述" min-width="150">
            <template #default="{ row }">
              <span v-if="row.description">{{ row.description }}</span>
              <span v-else class="empty-text">-</span>
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <!-- 所有资源类型都可以编辑名称 -->
              <el-button 
                type="primary" 
                size="small" 
                link 
                @click="showEditDialog(row)"
              >
                编辑
              </el-button>
              <!-- 只有没有子资源的模块可以删除 -->
              <el-button 
                v-if="row.type === 'MODULE' && (!row.children || row.children.length === 0)" 
                type="danger" 
                size="small" 
                link 
                @click="handleDelete(row)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页 -->
        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[20, 50, 100, 200]"
            :total="filteredResources.length"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
      </el-card>

      <!-- 编辑对话框 -->
      <el-dialog v-model="editDialogVisible" title="编辑资源" width="450px">
        <el-form :model="editForm" label-width="80px">
          <el-form-item label="资源类型">
            <el-tag :type="getTypeTagType(editForm.type)" size="small">{{ getTypeLabel(editForm.type) }}</el-tag>
          </el-form-item>
          <el-form-item label="资源名称">
            <el-input v-model="editForm.name" placeholder="请输入名称" />
          </el-form-item>
          <el-form-item label="父资源" v-if="canModifyParent">
            <el-select 
              v-model="editForm.parentId" 
              placeholder="选择父资源（可选）" 
              clearable 
              filterable
              style="width: 100%"
            >
              <el-option 
                v-for="m in availableParents" 
                :key="m.id" 
                :label="`${m.name} (${m.type})`" 
                :value="m.id"
                :disabled="m.id === editForm.id"
              >
                <span>{{ m.name }}</span>
                <el-tag size="small" style="margin-left: 8px">{{ getTypeLabel(m.type) }}</el-tag>
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item v-else-if="editForm.parentId" label="父资源">
            <span class="readonly-text">{{ getParentName(editForm.parentId) }}</span>
            <el-tag type="info" size="small" style="margin-left: 8px">不可修改</el-tag>
          </el-form-item>
          <el-form-item label="显示顺序">
            <el-input-number v-model="editForm.sortOrder" :min="0" :max="999" />
          </el-form-item>
          <el-form-item label="图标">
            <el-input v-model="editForm.icon" placeholder="Element Plus 图标名称" />
          </el-form-item>
          <el-form-item label="描述">
            <el-input v-model="editForm.description" type="textarea" :rows="2" placeholder="可选" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleEdit" :loading="editLoading">保存</el-button>
        </template>
      </el-dialog>

      <!-- 新建模块对话框 -->
      <el-dialog v-model="createDialogVisible" title="新建模块" width="450px">
        <el-form :model="createForm" label-width="80px">
          <el-form-item label="模块名称">
            <el-input v-model="createForm.name" placeholder="请输入模块名称" />
          </el-form-item>
          <el-form-item label="模块编码">
            <el-input v-model="createForm.code" placeholder="如：report, dashboard" />
          </el-form-item>
          <el-form-item label="父模块">
            <el-select v-model="createForm.parentId" placeholder="选择父模块（可选）" clearable style="width: 100%">
              <el-option label="顶级模块" :value="null" />
              <el-option 
                v-for="m in topLevelModules" 
                :key="m.id" 
                :label="m.name" 
                :value="m.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="显示顺序">
            <el-input-number v-model="createForm.sortOrder" :min="0" :max="999" />
          </el-form-item>
          <el-form-item label="图标">
            <el-input v-model="createForm.icon" placeholder="Element Plus 图标名称" />
          </el-form-item>
          <el-form-item label="描述">
            <el-input v-model="createForm.description" type="textarea" :rows="2" placeholder="可选" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleCreate" :loading="createLoading">创建</el-button>
        </template>
      </el-dialog>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Expand, Fold, Plus, Edit, Delete, Rank } from '@element-plus/icons-vue'
import Sortable from 'sortablejs'
import AdminLayout from '@/layouts/AdminLayout.vue'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { nextTick } from 'vue'
const userStore = useUserStore()

// 数据
const loading = ref(false)
const tableHeight = ref(600) // 表格固定高度，使表头固定且水平滚动条始终可见
const tableMaxHeight = ref(700) // 最大高度

// 分页
const currentPage = ref(1)
const pageSize = ref(50)
const resources = ref([])
const stats = ref([])
const moduleStats = ref([])
const searchText = ref('')
const selectedType = ref('')
const expandAll = ref(true)

// 批量选择
const selectedIds = ref([])
const selectedRows = ref([])

// 关联 API
const relatedApis = ref({}) // { [resourceId]: [apis] }
const relatedApiCount = ref({}) // { [resourceId]: count }
const loadingRelatedApis = ref(false)

// 编辑对话框
const editDialogVisible = ref(false)
const editForm = ref({})
const editLoading = ref(false)
const editParentType = ref(null) // 当前父资源类型，用于判断是否允许修改父资源

// 新建对话框
const createDialogVisible = ref(false)
const createForm = ref({
  name: '',
  code: '',
  parentId: null,
  sortOrder: 0,
  icon: '',
  description: ''
})
const createLoading = ref(false)

// 资源类型列表
const resourceTypes = ['MODULE', 'MENU', 'PAGE', 'ELEMENT', 'API', 'PERMISSION']

// 类型标签映射
const typeLabels = {
  MODULE: '模块',
  MENU: '菜单',
  PAGE: '页面',
  ELEMENT: '元素',
  API: 'API',
  PERMISSION: '权限'
}

// 判断是否可以修改父资源
// 规则：MODULE 类型可以修改；其他类型只有当前父资源是 MODULE 时才允许

// 可用的父资源列表（用于编辑时选择）
// 规则：排除自身和自身的子节点，避免循环引用
const availableParents = computed(() => {
  if (!editForm.value.id) return []
  
  const result = []
  const excludeIds = new Set([editForm.value.id])
  
  // 递归收集所有子节点 ID（需要排除）
  const collectChildren = (node) => {
    if (node.children) {
      for (const child of node.children) {
        excludeIds.add(child.id)
        collectChildren(child)
      }
    }
  }
  
  // 找到当前节点并收集其所有子节点
  const findAndCollect = (nodes) => {
    for (const node of nodes) {
      if (node.id === editForm.value.id) {
        collectChildren(node)
        return true
      }
      if (node.children) {
        if (findAndCollect(node.children)) return true
      }
    }
    return false
  }
  findAndCollect(resources.value)
  
  // 收集所有可用的父资源
  const traverse = (nodes) => {
    for (const node of nodes) {
      if (!excludeIds.has(node.id)) {
        result.push(node)
      }
      if (node.children && node.children.length > 0) {
        traverse(node.children)
      }
    }
  }
  traverse(resources.value)
  
  return result.sort((a, b) => {
    // MODULE 类型排在前面
    if (a.type === 'MODULE' && b.type !== 'MODULE') return -1
    if (a.type !== 'MODULE' && b.type === 'MODULE') return 1
    return (a.sortOrder || 0) - (b.sortOrder || 0)
  })
})

const canModifyParent = computed(() => {
  if (!editForm.value.type) return false
  // MODULE 类型可以修改父资源
  if (editForm.value.type === 'MODULE') return true
  // 其他类型只有当前父资源是 MODULE 时才允许
  return editParentType.value === 'MODULE'
})

// 模块标签映射
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

// 类型标签颜色
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

const getTypeLabel = (type) => typeLabels[type] || type
const getModuleLabel = (code) => moduleLabels[code] || code || '未分类'

// 资源总数
const totalCount = computed(() => {
  return stats.value.reduce((sum, s) => sum + s.totalCount, 0)
})

// 顶级模块列表（用于新建模块时选择父模块）
const topLevelModules = computed(() => {
  return resources.value.filter(r => r.type === 'MODULE' && !r.parentId)
})

// 所有模块列表（用于编辑时选择父模块）
const allModules = computed(() => {
  // 从树形数据中提取所有 MODULE
  const result = []
  const traverse = (nodes) => {
    for (const node of nodes) {
      if (node.type === 'MODULE') {
        result.push(node)
      }
      if (node.children && node.children.length > 0) {
        traverse(node.children)
      }
    }
  }
  traverse(resources.value)
  return result.sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
})

// 深拷贝树形数据
const deepCloneTree = (tree) => {
  if (!tree) return []
  return tree.map(node => ({
    ...node,
    children: node.children ? deepCloneTree(node.children) : []
  }))
}

// 是否显示菜单专用列
const showMenuColumns = computed(() => {
  return !selectedType.value || selectedType.value === 'MENU'
})

// 过滤后的资源树（保持树形结构）
// 扁平化树形数据（用于分页）
const flattenTree = (tree) => {
  const result = []
  const traverse = (nodes) => {
    for (const node of nodes) {
      result.push(node)
      if (node.children && node.children.length > 0) {
        traverse(node.children)
      }
    }
  }
  traverse(tree)
  return result
}

// 分页后的数据
const paginatedResources = computed(() => {
  const filtered = filteredResources.value
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filtered.slice(start, end)
})

const filteredResources = computed(() => {
  let tree = deepCloneTree(resources.value)
  
  // 搜索过滤（保持树形）
  if (searchText.value) {
    return filterTreeSearch(tree, searchText.value.toLowerCase())
  }
  
  // 类型过滤（保持树形，只显示匹配类型及其父节点）
  if (selectedType.value) {
    return filterTreeByType(tree, selectedType.value)
  }
  
  return tree
})

// 搜索过滤（保留匹配节点及其父节点）
const filterTreeSearch = (tree, keyword) => {
  return tree.filter(node => {
    const match = node.name?.toLowerCase().includes(keyword) ||
                  node.code?.toLowerCase().includes(keyword) ||
                  node.permissionKey?.toLowerCase().includes(keyword)
    
    if (match) return true
    
    if (node.children && node.children.length > 0) {
      const filteredChildren = filterTreeSearch(node.children, keyword)
      if (filteredChildren.length > 0) {
        node.children = filteredChildren
        return true
      }
    }
    
    return false
  })
}

// 类型过滤（保留匹配类型及其父节点）
const filterTreeByType = (tree, type) => {
  return tree.filter(node => {
    if (node.type === type) return true
    
    if (node.children && node.children.length > 0) {
      const filteredChildren = filterTreeByType(node.children, type)
      if (filteredChildren.length > 0) {
        node.children = filteredChildren
        return true
      }
    }
    
    return false
  })
}

// 获取父资源名称
const getParentName = (parentId) => {
  if (!parentId) return '-'
  const parent = findResourceById(resources.value, parentId)
  return parent ? `${parent.name} (${parent.type})` : `未知 (${parentId})`
}

// 分页事件处理
const handleSizeChange = () => {
  currentPage.value = 1 // 重置到第一页
}

const handleCurrentChange = () => {
  // 滚动到表格顶部
  const tableContainer = document.querySelector('.table-container')
  if (tableContainer) {
    tableContainer.scrollTop = 0
  }
}

// 加载资源
const loadResources = async () => {
  loading.value = true
  try {
    const response = await request.get('/resource/overview')
    if (response.data) {
      resources.value = response.data.tree || []
      stats.value = response.data.stats || []
      moduleStats.value = response.data.moduleStats || []
      
      // 预加载所有 MODULE 类型资源的关联 API 数量
      await preloadRelatedApisForModules()
    }
    ElMessage.success('资源数据已刷新')
    
    // 初始化拖拽排序
    nextTick(() => {
      initSortable()
    })
  } catch (error) {
    ElMessage.error('加载资源列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 预加载 MODULE 类型资源的关联 API 数量
const preloadRelatedApisForModules = async () => {
  const modules = getAllModules(resources.value)
  const promises = modules.map(async (module) => {
    try {
      const response = await request.get(`/resource/${module.id}/related-apis`)
      if (response.data && Array.isArray(response.data)) {
        relatedApis.value[module.id] = response.data
        relatedApiCount.value[module.id] = response.data.length
      }
    } catch (error) {
      console.error(`预加载模块 ${module.id} 的关联 API 失败:`, error)
    }
  })
  await Promise.all(promises)
}

// 递归获取所有 MODULE 类型资源
const getAllModules = (tree) => {
  const modules = []
  const traverse = (nodes) => {
    for (const node of nodes) {
      if (node.type === 'MODULE') {
        modules.push(node)
      }
      if (node.children && node.children.length > 0) {
        traverse(node.children)
      }
    }
  }
  traverse(tree)
  return modules
}

// 切换展开/收起
const toggleExpandAll = () => {
  expandAll.value = !expandAll.value
}

// 加载关联 API（仅在未加载过时调用）
const loadRelatedApis = async (resourceId) => {
  // 如果已加载过，直接返回
  if (relatedApis.value[resourceId]) {
    return
  }
  
  loadingRelatedApis.value = true
  try {
    const response = await request.get(`/resource/${resourceId}/related-apis`)
    if (response.data && Array.isArray(response.data)) {
      relatedApis.value[resourceId] = response.data
      relatedApiCount.value[resourceId] = response.data.length
    } else {
      relatedApis.value[resourceId] = []
      relatedApiCount.value[resourceId] = 0
    }
  } catch (error) {
    ElMessage.error('加载关联 API 失败')
    console.error(error)
    relatedApis.value[resourceId] = []
    relatedApiCount.value[resourceId] = 0
  } finally {
    loadingRelatedApis.value = false
  }
}

// 处理选择变化
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(row => row.id)
  selectedRows.value = selection
}

// 判断是否可以选中（只有没有子资源的 MODULE 可以删除，所以只允许选择这些）
const canSelect = (row) => {
  // 只有 MODULE 类型且没有子资源才可以被批量删除
  if (row.type !== 'MODULE') return false
  return !row.children || row.children.length === 0
}

// 批量删除
const batchDelete = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要删除的资源')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定删除选中的 ${selectedIds.value.length} 个资源？此操作不可恢复。`,
      '批量删除确认',
      { type: 'warning' }
    )
    
    const promises = selectedIds.value.map(id => request.delete(`/resource/${id}`))
    await Promise.all(promises)
    
    ElMessage.success(`成功删除 ${selectedIds.value.length} 个资源`)
    selectedIds.value = []
    selectedRows.value = []
    await loadResources()
    
    // 刷新侧边栏菜单
    try {
      await userStore.refreshMenus()
      ElMessage.success('侧边栏菜单已同步刷新')
    } catch (e) {
      console.warn('菜单刷新失败:', e)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '批量删除失败')
    }
  }
}

// 更新菜单可见性
const updateMenuVisibility = async (row) => {
  try {
    await request.put(`/resource/${row.id}`, {
      ...row,
      isVisible: row.isVisible
    })
    ElMessage.success('可见性已更新')
    
    // 刷新侧边栏菜单
    try {
      await userStore.refreshMenus()
    } catch (e) {
      console.warn('菜单刷新失败:', e)
    }
  } catch (error) {
    ElMessage.error('更新失败')
    row.isVisible = !row.isVisible
    console.error(error)
  }
}

// 拖拽排序相关
let sortableInstance = null

const initSortable = () => {
  const tbody = document.querySelector('.el-table__body > tbody')
  if (!tbody || sortableInstance) return
  
  sortableInstance = Sortable.create(tbody, {
    handle: '.drag-handle',
    animation: 150,
    ghostClass: 'sortable-ghost',
    chosenClass: 'sortable-chosen',
    dragClass: 'sortable-drag',
    onEnd: async (evt) => {
      const { oldIndex, newIndex } = evt
      if (oldIndex === newIndex) return
      
      // 获取当前展示的数据
      const currentData = filteredResources.value
      const draggedItem = currentData[oldIndex]
      const targetItem = currentData[newIndex]
      
      // 只能同级拖拽
      if (draggedItem.parentId !== targetItem.parentId) {
        ElMessage.warning('只能在同一层级内拖拽排序')
        await loadResources()
        return
      }
      
      try {
        // 更新排序号
        const newSortOrder = targetItem.sortOrder || 0
        await request.put(`/resource/${draggedItem.id}`, {
          ...draggedItem,
          sortOrder: newSortOrder
        })
        
        ElMessage.success('排序已更新')
        
        // 刷新列表
        await loadResources()
      } catch (error) {
        ElMessage.error('更新排序失败')
        console.error(error)
        await loadResources()
      }
    }
  })
}

// 显示编辑对话框
const showEditDialog = (row) => {
  editForm.value = {
    id: row.id,
    type: row.type,
    name: row.name,
    parentId: row.parentId,
    sortOrder: row.sortOrder,
    icon: row.icon || '',
    description: row.description || ''
  }
  
  // 查找当前父资源类型
  if (row.parentId) {
    const parent = findResourceById(resources.value, row.parentId)
    editParentType.value = parent ? parent.type : null
  } else {
    editParentType.value = null
  }
  
  editDialogVisible.value = true
}

// 从树形数据中查找资源
const findResourceById = (tree, id) => {
  for (const node of tree) {
    if (node.id === id) return node
    if (node.children && node.children.length > 0) {
      const found = findResourceById(node.children, id)
      if (found) return found
    }
  }
  return null
}

// 处理编辑
const handleEdit = async () => {
  editLoading.value = true
  try {
    const payload = {
      name: editForm.value.name,
      sortOrder: editForm.value.sortOrder,
      icon: editForm.value.icon,
      description: editForm.value.description
    }
    // 只有 MENU、PAGE、PERMISSION 可以修改 parentId
    if (['MENU', 'PAGE', 'PERMISSION'].includes(editForm.value.type)) {
      payload.parentId = editForm.value.parentId
    }
    await request.put(`/resource/${editForm.value.id}`, payload)
    ElMessage.success('更新成功')
    editDialogVisible.value = false
    await loadResources()
    
    // 如果修改了 MENU 或 MODULE，刷新侧边栏菜单
    if (['MODULE', 'MENU'].includes(editForm.value.type)) {
      try {
        await userStore.refreshMenus()
        ElMessage.success('侧边栏菜单已同步刷新')
      } catch (e) {
        console.warn('菜单刷新失败:', e)
      }
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '更新失败')
  } finally {
    editLoading.value = false
  }
}

// 显示新建对话框
const showCreateDialog = () => {
  createForm.value = {
    name: '',
    code: '',
    parentId: null,
    sortOrder: 0,
    icon: '',
    description: ''
  }
  createDialogVisible.value = true
}

// 处理创建
const handleCreate = async () => {
  if (!createForm.value.name) {
    ElMessage.warning('请输入模块名称')
    return
  }
  if (!createForm.value.code) {
    ElMessage.warning('请输入模块编码')
    return
  }
  
  createLoading.value = true
  try {
    // 生成权限标识
    const permissionKey = createForm.value.parentId 
      ? `module:${createForm.value.code}:view`
      : `${createForm.value.code}:view`
    
    await request.post('/resource', {
      name: createForm.value.name,
      code: createForm.value.code,
      type: 'MODULE',
      parentId: createForm.value.parentId,
      permissionKey: permissionKey,
      moduleCode: createForm.value.code,
      sortOrder: createForm.value.sortOrder,
      icon: createForm.value.icon,
      description: createForm.value.description
    })
    ElMessage.success('模块创建成功')
    createDialogVisible.value = false
    await loadResources()
    
    // 刷新侧边栏菜单
    try {
      await userStore.refreshMenus()
      ElMessage.success('侧边栏菜单已同步刷新')
    } catch (e) {
      console.warn('菜单刷新失败:', e)
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '创建失败')
  } finally {
    createLoading.value = false
  }
}

// 处理删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定删除模块 "${row.name}"？此操作不可恢复。`,
      '删除确认',
      { type: 'warning' }
    )
    
    await request.delete(`/resource/${row.id}`)
    ElMessage.success('删除成功')
    await loadResources()
    
    // 刷新侧边栏菜单
    try {
      await userStore.refreshMenus()
      ElMessage.success('侧边栏菜单已同步刷新')
    } catch (e) {
      console.warn('菜单刷新失败:', e)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

// 初始化
// 响应式表格高度
const updateTableHeight = () => {
  const windowHeight = window.innerHeight
  const headerHeight = 250 // 头部卡片 + 工具栏 + 分页等高度
  tableHeight.value = windowHeight - headerHeight
  tableMaxHeight.value = windowHeight - headerHeight + 100
}

onMounted(() => {
  loadResources()
  updateTableHeight()
  window.addEventListener('resize', updateTableHeight)
})

onUnmounted(() => {
  window.removeEventListener('resize', updateTableHeight)
})
</script>

<style scoped>
.resource-list {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
}

.page-header .description {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.table-container {
  margin-top: 20px;
}

/* 表格样式优化 */
:deep(.el-table) {
  font-size: 13px;
}

:deep(.el-table th) {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: 600;
}

/* 固定列阴影 */
:deep(.el-table__fixed) {
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
}

:deep(.el-table__fixed-right) {
  box-shadow: -2px 0 8px rgba(0, 0, 0, 0.1);
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

.stats-row {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  min-width: 120px;
}

.stat-content {
  text-align: center;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #409EFF;
}

.stat-label {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
}

.name-text {
  font-weight: 500;
}

.code-text {
  font-family: monospace;
  color: #666;
}

.empty-text {
}

.readonly-text {
  color: #666;
  font-size: 14px;

  color: #999;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.related-apis-popover {
  max-height: 400px;
  overflow-y: auto;
}

.related-apis-popover .loading-text,
.related-apis-popover .empty-text {
}

.readonly-text {
  color: #666;
  font-size: 14px;

  text-align: center;
  padding: 20px;
  color: #999;
}

.related-apis-popover :deep(.el-table) {
  font-size: 12px;
}

.drag-handle {
  color: #909399;
  cursor: move;
  font-size: 16px;
}

.drag-handle:hover {
  color: #409eff;
}

.sortable-ghost {
  opacity: 0.4;
  background-color: #f5f7fa;
}

.sortable-chosen {
  background-color: #ecf5ff;
}

.sortable-drag {
  opacity: 0.8;
}
</style>