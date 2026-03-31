<template>
  <AdminLayout>
    <div class="role-permission">
      <el-card class="header-card">
        <div class="page-header">
          <h2>角色权限配置</h2>
          <p class="description">为角色分配功能权限，系统管理员默认拥有所有权限且不可修改</p>
        </div>
      </el-card>

      <el-card class="content-card">
        <!-- 角色选择 -->
        <el-form :inline="true" class="role-select-form">
          <el-form-item label="选择角色">
            <el-select
              v-model="selectedRoleId"
              placeholder="请选择角色"
              filterable
              style="width: 300px"
              @change="loadRolePermissionTree"
            >
              <el-option
                v-for="role in roleList"
                :key="role.id"
                :label="`${role.role_name} (${role.role_code})`"
                :value="role.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadRolePermissionTree" :disabled="!selectedRoleId">
              加载权限树
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 角色信息展示 -->
        <div v-if="selectedRole" class="role-info">
          <el-descriptions title="角色信息" :column="3" border>
            <el-descriptions-item label="角色名称">{{ selectedRole.role_name }}</el-descriptions-item>
            <el-descriptions-item label="角色编码">{{ selectedRole.role_code }}</el-descriptions-item>
            <el-descriptions-item label="描述">{{ selectedRole.role_description || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 系统管理员提示 -->
        <el-alert
          v-if="isProtectedRole"
          title="系统管理员角色权限不可修改"
          type="warning"
          :closable="false"
          style="margin-top: 20px"
          show-icon
        >
          系统管理员角色默认拥有所有权限，不允许编辑。
        </el-alert>

        <!-- 权限状态图例 -->
        <el-alert
          title="权限状态说明"
          type="info"
          :closable="false"
          style="margin-top: 20px"
        >
          <div class="legend-container">
            <span class="legend-item">
              <el-tag type="danger" size="small">basic</el-tag> 基本权限（不可调整）
            </span>
            <span class="legend-item">
              <el-tag type="warning" size="small">default</el-tag> 缺省权限
            </span>
            <span class="legend-item">
              <el-tag type="success" size="small">added</el-tag> 已增加
            </span>
            <span class="legend-item">
              <el-tag type="info" size="small">removed</el-tag> 已移除
            </span>
            <span class="legend-item">
              <el-tag size="small">none</el-tag> 无权限
            </span>
          </div>
        </el-alert>

        <!-- 权限树 -->
        <div class="permission-tree-container" v-loading="loading">
          <el-tree
            ref="permissionTreeRef"
            :data="permissionTree"
            :props="treeProps"
            node-key="resourceId"
            default-expand-all
            :expand-on-click-node="false"
            :class="{ 'tree-disabled': isProtectedRole }"
          >
            <template #default="{ node, data }">
              <div class="tree-node" :class="getNodeClass(data.status)">
                <div class="node-content">
                  <!-- 节点图标 -->
                  <el-icon v-if="data.type" class="node-icon">
                    <component :is="getTypeIcon(data.type)" />
                  </el-icon>
                  
                  <!-- 节点信息 -->
                  <div class="node-info">
                    <span class="node-name">{{ data.name }}</span>
                    <span class="node-code" v-if="data.code">{{ data.code }}</span>
                    
                    <!-- 状态标签 -->
                    <el-tag 
                      :type="getStatusTagType(data.status)" 
                      size="small"
                      class="status-tag"
                    >
                      {{ data.status }}
                    </el-tag>
                    
                    <!-- 基本权限标记 -->
                    <el-tag 
                      v-if="data.status === 'basic'" 
                      type="danger" 
                      size="small"
                      class="basic-tag"
                    >
                      基本
                    </el-tag>
                  </div>
                </div>
                
                <!-- 操作按钮 -->
                <div class="node-actions" v-if="!isProtectedRole && data.status !== 'basic'">
                  <el-button
                    v-if="data.status === 'none' || data.status === 'removed'"
                    type="success"
                    size="small"
                    @click.stop="handleAdjustPermission(data, 'ADD')"
                    :loading="adjustingIds.includes(data.resourceId)"
                  >
                    <el-icon><Plus /></el-icon>
                    添加
                  </el-button>
                  
                  <el-button
                    v-if="data.status === 'default' || data.status === 'added'"
                    type="danger"
                    size="small"
                    @click.stop="handleAdjustPermission(data, 'REMOVE')"
                    :loading="adjustingIds.includes(data.resourceId)"
                  >
                    <el-icon><Minus /></el-icon>
                    移除
                  </el-button>
                </div>
                
                <!-- 基本权限锁定提示 -->
                <div class="node-locked" v-if="data.status === 'basic'">
                  <el-icon><Lock /></el-icon>
                  <span>基本权限</span>
                </div>
              </div>
            </template>
          </el-tree>
        </div>
      </el-card>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, Minus, Lock, Folder, Menu, Document, Setting, 
  Connection, User, Check, Close 
} from '@element-plus/icons-vue'
import AdminLayout from '@/layouts/AdminLayout.vue'
import { getAllRoles, getRolePermissionTree, adjustRolePermission } from '@/api/permission'

// 树组件引用
const permissionTreeRef = ref(null)

// 状态
const loading = ref(false)
const adjustingIds = ref([])
const roleList = ref([])
const selectedRoleId = ref(null)
const selectedRole = ref(null)
const permissionTree = ref([])

// 树配置
const treeProps = {
  children: 'children',
  label: 'name'
}

// 受保护的角色列表（不允许编辑权限）
const PROTECTED_ROLES = ['ROLE_SYSTEM_ADMIN']

// 是否为受保护角色
const isProtectedRole = computed(() => {
  return selectedRole.value && PROTECTED_ROLES.includes(selectedRole.value.role_code)
})

// 获取类型图标
const getTypeIcon = (type) => {
  const iconMap = {
    'MODULE': Folder,
    'MENU': Menu,
    'PAGE': Document,
    'ELEMENT': Setting,
    'API': Connection,
    'PERMISSION': Check
  }
  return iconMap[type] || Document
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  const typeMap = {
    'basic': 'danger',
    'default': 'warning',
    'added': 'success',
    'removed': 'info',
    'none': ''
  }
  return typeMap[status] || ''
}

// 获取节点 CSS 类
const getNodeClass = (status) => {
  return `node-status-${status}`
}

// 加载角色列表
const loadRoles = async () => {
  try {
    const res = await getAllRoles()
    roleList.value = res.data || []
  } catch (error) {
    console.error('加载角色列表失败:', error)
    ElMessage.error('加载角色列表失败')
  }
}

// 加载角色权限树
const loadRolePermissionTree = async () => {
  if (!selectedRoleId.value) return
  
  loading.value = true
  try {
    const role = roleList.value.find(r => r.id === selectedRoleId.value)
    selectedRole.value = role
    
    const res = await getRolePermissionTree(selectedRoleId.value)
    permissionTree.value = res.data || []
    
    ElMessage.success('权限树加载成功')
  } catch (error) {
    console.error('加载权限树失败:', error)
    ElMessage.error('加载权限树失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 处理权限调整
const handleAdjustPermission = async (data, action) => {
  if (!selectedRoleId.value) return
  
  const actionText = action === 'ADD' ? '添加' : '移除'
  
  try {
    await ElMessageBox.confirm(
      `确定要${actionText}权限"${data.name}"吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    adjustingIds.value.push(data.resourceId)
    
    await adjustRolePermission(selectedRoleId.value, {
      resourceId: data.resourceId,
      action: action
    })
    
    ElMessage.success(`${actionText}权限成功`)
    
    // 重新加载权限树
    await loadRolePermissionTree()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('调整权限失败:', error)
      ElMessage.error('调整权限失败：' + (error.message || '未知错误'))
    }
  } finally {
    adjustingIds.value = adjustingIds.value.filter(id => id !== data.resourceId)
  }
}

// 初始化
onMounted(() => {
  loadRoles()
})
</script>

<style scoped lang="scss">
.role-permission {
  .header-card {
    margin-bottom: 20px;
    
    .page-header {
      h2 {
        margin: 0 0 8px 0;
        font-size: 20px;
        color: #333;
      }
      
      .description {
        margin: 0;
        color: #666;
        font-size: 14px;
      }
    }
  }
  
  .content-card {
    .role-select-form {
      margin-bottom: 20px;
    }
    
    .role-info {
      margin-bottom: 20px;
    }
    
    .legend-container {
      display: flex;
      flex-wrap: wrap;
      gap: 16px;
      
      .legend-item {
        display: flex;
        align-items: center;
        gap: 8px;
      }
    }
    
    .permission-tree-container {
      margin-top: 20px;
      
      &.tree-disabled {
        opacity: 0.6;
        pointer-events: none;
      }
    }
  }
}

// 树节点样式
.tree-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  border-radius: 4px;
  margin: 2px 0;
  transition: all 0.2s;
  
  &:hover {
    background-color: #f5f7fa;
  }
  
  // 状态样式
  &.node-status-basic {
    background-color: #fef0f0;
    border-left: 3px solid #f56c6c;
  }
  
  &.node-status-default {
    background-color: #fdf6ec;
    border-left: 3px solid #e6a23c;
  }
  
  &.node-status-added {
    background-color: #f0f9eb;
    border-left: 3px solid #67c23a;
  }
  
  &.node-status-removed {
    background-color: #f4f4f5;
    border-left: 3px solid #909399;
  }
  
  &.node-status-none {
    background-color: #fff;
    border-left: 3px solid #dcdfe6;
  }
  
  .node-content {
    display: flex;
    align-items: center;
    flex: 1;
    
    .node-icon {
      margin-right: 8px;
      font-size: 16px;
      color: #606266;
    }
    
    .node-info {
      display: flex;
      align-items: center;
      gap: 8px;
      
      .node-name {
        font-weight: 500;
        color: #303133;
      }
      
      .node-code {
        color: #909399;
        font-size: 12px;
        font-family: monospace;
      }
      
      .status-tag {
        margin-left: 8px;
      }
      
      .basic-tag {
        margin-left: 4px;
      }
    }
  }
  
  .node-actions {
    display: flex;
    gap: 8px;
    
    .el-button {
      padding: 5px 10px;
      font-size: 12px;
    }
  }
  
  .node-locked {
    display: flex;
    align-items: center;
    gap: 4px;
    color: #f56c6c;
    font-size: 12px;
    
    .el-icon {
      font-size: 14px;
    }
  }
}

// 树禁用状态
.tree-disabled {
  .tree-node {
    pointer-events: none;
    opacity: 0.5;
  }
}
</style>
