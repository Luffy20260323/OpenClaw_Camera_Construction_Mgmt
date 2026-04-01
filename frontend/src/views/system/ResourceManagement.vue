<template>
  <AdminLayout>
    <div class="resource-management">
      <el-card class="header-card">
        <div class="page-header">
          <h2>资源管理</h2>
          <p class="description">管理系统资源树（模块、菜单、元素、API），资源是权限控制的基础</p>
        </div>
      </el-card>

      <el-card class="content-card">
        <!-- 工具栏 -->
        <div class="toolbar">
          <el-button type="primary" @click="handleAddRoot">
            <el-icon><Plus /></el-icon>
            新增模块
          </el-button>
          <el-button @click="loadResourceTree">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
          <el-input
            v-model="filterText"
            placeholder="搜索资源名称/编码"
            style="width: 300px; margin-left: 16px"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <!-- 资源树 -->
        <el-tree
          ref="treeRef"
          :data="resourceTree"
          :props="treeProps"
          node-key="id"
          default-expand-all
          :filter-node-method="filterNode"
          :expand-on-click-node="false"
          v-loading="loading"
          style="margin-top: 20px"
        >
          <template #default="{ node, data }">
            <div class="tree-node">
              <div class="node-info">
                <!-- 类型图标 -->
                <el-tag :type="getTypeTagType(data.type)" size="small" style="margin-right: 8px">
                  {{ getTypeLabel(data.type) }}
                </el-tag>
                
                <!-- 名称 -->
                <span class="node-name">{{ data.name }}</span>
                
                <!-- 编码 -->
                <span class="node-code">{{ data.code }}</span>
                
                <!-- 权限标识 -->
                <el-tag v-if="data.permissionKey" type="info" size="small" style="margin-left: 8px">
                  {{ data.permissionKey }}
                </el-tag>
                
                <!-- 基本权限标记 -->
                <el-tag v-if="data.isBasic === 1" type="danger" size="small" style="margin-left: 8px">
                  基本权限
                </el-tag>
                
                <!-- API 信息 -->
                <span v-if="data.type === 'API'" class="api-info">
                  <el-tag type="warning" size="small">{{ data.method }}</el-tag>
                  <span class="uri">{{ data.uriPattern }}</span>
                </span>
              </div>
              
              <div class="node-actions">
                <el-button-group>
                  <el-button size="small" @click.stop="handleAddChild(data)" :disabled="!canAddChild(data.type)">
                    <el-icon><Plus /></el-icon>
                  </el-button>
                  <el-button size="small" @click.stop="handleEdit(data)">
                    <el-icon><Edit /></el-icon>
                  </el-button>
                  <el-button size="small" type="danger" @click.stop="handleDelete(data)" :disabled="data.isBasic === 1">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </el-button-group>
              </div>
            </div>
          </template>
        </el-tree>
      </el-card>

      <!-- 新增/编辑对话框 -->
      <el-dialog
        v-model="dialogVisible"
        :title="dialogTitle"
        width="600px"
        destroy-on-close
      >
        <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-width="100px"
        >
          <el-form-item label="资源名称" prop="name">
            <el-input v-model="formData.name" placeholder="请输入资源名称" />
          </el-form-item>
          
          <el-form-item label="资源编码" prop="code">
            <el-input v-model="formData.code" placeholder="请输入资源编码（唯一标识）" />
          </el-form-item>
          
          <el-form-item label="资源类型" prop="type">
            <el-select v-model="formData.type" placeholder="请选择资源类型" style="width: 100%" :disabled="!!formData.id">
              <el-option label="模块（MODULE）" value="MODULE" />
              <el-option label="菜单（MENU）" value="MENU" />
              <el-option label="页面（PAGE）" value="PAGE" />
              <el-option label="元素（ELEMENT）" value="ELEMENT" />
              <el-option label="API" value="API" />
              <el-option label="独立权限" value="PERMISSION" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="权限标识" prop="permissionKey">
            <el-input v-model="formData.permissionKey" placeholder="如：user:create, role:delete" />
          </el-form-item>
          
          <!-- API 类型特有字段 -->
          <template v-if="formData.type === 'API'">
            <el-form-item label="HTTP 方法" prop="method">
              <el-select v-model="formData.method" placeholder="请选择" style="width: 100%">
                <el-option label="GET" value="GET" />
                <el-option label="POST" value="POST" />
                <el-option label="PUT" value="PUT" />
                <el-option label="DELETE" value="DELETE" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="URI 模式" prop="uriPattern">
              <el-input v-model="formData.uriPattern" placeholder="如：/api/users, /api/users/{id}" />
            </el-form-item>
          </template>
          
          <!-- 菜单类型特有字段 -->
          <template v-if="formData.type === 'MENU' || formData.type === 'MODULE'">
            <el-form-item label="路由路径" prop="path">
              <el-input v-model="formData.path" placeholder="如：/admin/users" />
            </el-form-item>
            
            <el-form-item label="图标" prop="icon">
              <el-input v-model="formData.icon" placeholder="图标名称" />
            </el-form-item>
            
            <el-form-item label="组件路径" prop="component">
              <el-input v-model="formData.component" placeholder="如：@/views/user/List.vue" />
            </el-form-item>
          </template>
          
          <el-form-item label="排序号" prop="sortOrder">
            <el-input-number v-model="formData.sortOrder" :min="0" :max="999" />
          </el-form-item>
          
          <el-form-item label="是否基本权限" prop="isBasic">
            <el-switch
              v-model="formData.isBasic"
              :active-value="1"
              :inactive-value="0"
              active-text="是"
              inactive-text="否"
            />
            <div class="form-tip">基本权限所有角色必备，不可删除</div>
          </el-form-item>
        </el-form>
        
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
        </template>
      </el-dialog>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Refresh, Search } from '@element-plus/icons-vue'
import AdminLayout from '@/layouts/AdminLayout.vue'
import request from '@/utils/request'

// 树组件引用
const treeRef = ref(null)
const loading = ref(false)
const resourceTree = ref([])
const filterText = ref('')

// 树配置
const treeProps = {
  children: 'children',
  label: 'name'
}

// 类型标签
const getTypeLabel = (type) => {
  const labels = {
    'MODULE': '模块',
    'MENU': '菜单',
    'PAGE': '页面',
    'ELEMENT': '元素',
    'API': 'API',
    'PERMISSION': '权限'
  }
  return labels[type] || type
}

const getTypeTagType = (type) => {
  const types = {
    'MODULE': '',
    'MENU': 'success',
    'PAGE': 'info',
    'ELEMENT': 'warning',
    'API': 'danger',
    'PERMISSION': 'info'
  }
  return types[type] || ''
}

// 是否可以添加子节点
const canAddChild = (type) => {
  // API 和 PERMISSION 不能有子节点
  return type !== 'API' && type !== 'PERMISSION'
}

// 过滤节点
const filterNode = (value, data) => {
  if (!value) return true
  return data.name?.includes(value) || data.code?.includes(value)
}

// 监听搜索框
watch(filterText, (val) => {
  treeRef.value?.filter(val)
})

// 加载资源树
const loadResourceTree = async () => {
  loading.value = true
  try {
    const response = await request.get('/api/resources/tree')
    if (response.data) {
      resourceTree.value = response.data
    }
  } catch (error) {
    ElMessage.error('加载资源树失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 对话框
const dialogVisible = ref(false)
const dialogTitle = computed(() => formData.id ? '编辑资源' : '新增资源')
const formRef = ref(null)
const submitting = ref(false)

const formData = reactive({
  id: null,
  name: '',
  code: '',
  type: 'MODULE',
  parentId: null,
  permissionKey: '',
  uriPattern: '',
  method: '',
  icon: '',
  path: '',
  component: '',
  sortOrder: 0,
  isBasic: 0
})

const formRules = {
  name: [{ required: true, message: '请输入资源名称', trigger: 'blur' }],
  code: [
    { required: true, message: '请输入资源编码', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_-]+$/, message: '编码只能包含字母、数字、下划线和横线', trigger: 'blur' }
  ],
  type: [{ required: true, message: '请选择资源类型', trigger: 'change' }]
}

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    id: null,
    name: '',
    code: '',
    type: 'MODULE',
    parentId: null,
    permissionKey: '',
    uriPattern: '',
    method: '',
    icon: '',
    path: '',
    component: '',
    sortOrder: 0,
    isBasic: 0
  })
}

// 新增根节点
const handleAddRoot = () => {
  resetForm()
  formData.type = 'MODULE'
  formData.parentId = null
  dialogVisible.value = true
}

// 新增子节点
const handleAddChild = (data) => {
  resetForm()
  formData.parentId = data.id
  
  // 根据父节点类型设置子节点类型
  if (data.type === 'MODULE') {
    formData.type = 'MENU'
  } else if (data.type === 'MENU') {
    formData.type = 'ELEMENT'
  }
  
  dialogVisible.value = true
}

// 编辑
const handleEdit = (data) => {
  resetForm()
  Object.assign(formData, {
    id: data.id,
    name: data.name,
    code: data.code,
    type: data.type,
    parentId: data.parentId,
    permissionKey: data.permissionKey,
    uriPattern: data.uriPattern,
    method: data.method,
    icon: data.icon,
    path: data.path,
    component: data.component,
    sortOrder: data.sortOrder,
    isBasic: data.isBasic
  })
  dialogVisible.value = true
}

// 删除
const handleDelete = async (data) => {
  if (data.isBasic === 1) {
    ElMessage.warning('基本权限资源不可删除')
    return
  }
  
  const childCount = countChildren(data)
  const message = childCount > 0 
    ? `确定删除资源"${data.name}"及其 ${childCount} 个子资源吗？`
    : `确定删除资源"${data.name}"吗？`
  
  try {
    await ElMessageBox.confirm(message, '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await request.delete(`/api/resources/${data.id}`)
    ElMessage.success('删除成功')
    loadResourceTree()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
      console.error(error)
    }
  }
}

// 统计子节点数量
const countChildren = (node) => {
  let count = 0
  if (node.children) {
    count += node.children.length
    node.children.forEach(child => {
      count += countChildren(child)
    })
  }
  return count
}

// 提交表单
const handleSubmit = async () => {
  await formRef.value.validate()
  
  submitting.value = true
  try {
    if (formData.id) {
      // 编辑
      await request.put(`/api/resources/${formData.id}`, formData)
      ElMessage.success('更新成功')
    } else {
      // 新增
      await request.post('/api/resources', formData)
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    loadResourceTree()
  } catch (error) {
    ElMessage.error(formData.id ? '更新失败' : '创建失败')
    console.error(error)
  } finally {
    submitting.value = false
  }
}

// 初始化
onMounted(() => {
  loadResourceTree()
})
</script>

<style scoped lang="scss">
.resource-management {
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
    }
    
    .tree-node {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 4px 0;
      
      .node-info {
        display: flex;
        align-items: center;
        
        .node-name {
          font-weight: 500;
          margin-right: 8px;
        }
        
        .node-code {
          color: #909399;
          font-size: 12px;
          font-family: monospace;
        }
        
        .api-info {
          margin-left: 12px;
          display: flex;
          align-items: center;
          gap: 8px;
          
          .uri {
            color: #606266;
            font-size: 12px;
            font-family: monospace;
          }
        }
      }
      
      .node-actions {
        visibility: hidden;
      }
      
      &:hover .node-actions {
        visibility: visible;
      }
    }
  }
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>