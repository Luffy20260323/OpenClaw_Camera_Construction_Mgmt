<template>
    <div class="user-permission-detail-page">
      <!-- 头部卡片 -->
      <el-card class="header-card">
        <div class="page-header">
          <h2>用户权限查看</h2>
          <p class="description">查看用户的详细权限信息，包括角色继承和个人调整的权限</p>
        </div>
      </el-card>

      <!-- 用户选择卡片 -->
      <el-card class="content-card">
        <template #header>
          <div class="card-header">
            <span>用户选择</span>
          </div>
        </template>

        <el-form :inline="true" class="user-select-form">
          <el-form-item label="搜索用户">
            <el-input
              v-model="searchKeyword"
              placeholder="用户名/姓名/手机/邮箱"
              clearable
              style="width: 250px"
              @keyup.enter="loadUserList"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadUserList">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
          </el-form-item>
          <el-form-item>
            <el-button @click="resetSearch">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 用户列表 -->
        <el-table
          :data="userList"
          v-loading="userListLoading"
          style="width: 100%; margin-top: 10px"
          @row-click="selectUser"
          :row-class-name="tableRowClassName"
          highlight-current-row
        >
          <el-table-column prop="username" label="用户名" width="100" />
          <el-table-column prop="realName" label="姓名" width="80" />
          <el-table-column prop="email" label="邮箱" width="180" />
          <el-table-column prop="phone" label="手机号" width="120" />
          <el-table-column prop="companyName" label="公司" width="150" />
          <el-table-column prop="companyTypeName" label="公司类型" width="100" />
          <el-table-column label="角色" min-width="150">
            <template #default="{ row }">
              <el-tag
                v-for="role in row.roleNames"
                :key="role"
                size="small"
                style="margin-right: 4px; margin-bottom: 4px"
              >
                {{ role }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                {{ row.status === 1 ? '正常' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadUserList"
          @current-change="loadUserList"
          style="margin-top: 20px; justify-content: flex-end"
        />
      </el-card>

      <!-- 用户权限详情卡片 -->
      <el-card v-if="selectedUser" class="content-card permission-detail-card">
        <template #header>
          <div class="card-header">
            <span>权限详情 - {{ selectedUser.realName }} ({{ selectedUser.username }})</span>
            <el-button type="primary" size="small" @click="loadUserPermissions">
              <el-icon><Refresh /></el-icon>
              刷新权限
            </el-button>
          </div>
        </template>

        <!-- 用户基本信息 -->
        <el-descriptions title="用户信息" :column="3" border style="margin-bottom: 20px">
          <el-descriptions-item label="用户名">{{ selectedUser.username }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ selectedUser.realName }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ selectedUser.email || '-' }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ selectedUser.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="公司">{{ selectedUser.companyName }}</el-descriptions-item>
          <el-descriptions-item label="公司类型">{{ selectedUser.companyTypeName }}</el-descriptions-item>
          <el-descriptions-item label="角色" :span="3">
            <el-tag
              v-for="role in selectedUser.roleNames"
              :key="role"
              size="small"
              style="margin-right: 8px"
            >
              {{ role }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 权限统计 -->
        <el-row :gutter="16" style="margin-bottom: 20px">
          <el-col :span="6">
            <el-statistic title="总权限数" :value="permissionStats.total" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="角色权限" :value="permissionStats.fromRole" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="个人新增" :value="permissionStats.added" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="个人移除" :value="permissionStats.removed" />
          </el-col>
        </el-row>
        
        <!-- 权限来源说明 -->
        <el-alert
          title="权限来源说明"
          type="info"
          :closable="false"
          style="margin-bottom: 20px"
        >
          <ul style="margin: 8px 0 0 20px; padding: 0;">
            <li><strong>基本权限：</strong>角色配置的基础权限，来自 role_permissions 表</li>
            <li><strong>缺省权限：</strong>角色创建时的默认权限，来自 role_default_permissions 表</li>
            <li><strong>调整权限：</strong>用户个人权限调整，来自 user_permission_adjustments 表</li>
            <li><strong>优先级：</strong>调整权限 > 基本权限 > 缺省权限</li>
          </ul>
        </el-alert>

        <!-- 权限树 -->
        <div class="permission-tree-container" v-loading="permissionLoading">
          <el-tree
            ref="permissionTreeRef"
            :data="permissionTreeData"
            :props="treeProps"
            default-expand-all
            :expand-on-click-node="false"
          >
            <template #default="{ node, data }">
              <span class="permission-tree-node">
                <el-icon v-if="data.type === 'menu'" style="margin-right: 4px; color: #409eff">
                  <Menu />
                </el-icon>
                <el-icon v-else-if="data.type === 'permission'" style="margin-right: 4px; color: #67c23a">
                  <Check />
                </el-icon>
                <span :class="['node-label', getSourceClass(data.source)]">
                  {{ node.label }}
                </span>
                <el-tag
                  v-if="data.source"
                  :type="getSourceType(data.source)"
                  size="small"
                  style="margin-left: 8px"
                >
                  {{ getSourceText(data.source) }}
                </el-tag>
                <el-tag
                  v-if="data.adjustment"
                  :type="data.adjustment === 'ADD' ? 'success' : 'danger'"
                  size="small"
                  style="margin-left: 4px"
                >
                  {{ data.adjustment === 'ADD' ? '+ 新增' : '- 移除' }}
                </el-tag>
                <span v-if="data.sourceDetail" class="source-detail">
                  {{ data.sourceDetail }}
                </span>
              </span>
            </template>
          </el-tree>
        </div>

        <!-- 权限列表表格 -->
        <el-table
          :data="permissionList"
          border
          style="width: 100%; margin-top: 20px"
          v-loading="permissionLoading"
          :header-cell-style="{ background: '#f5f7fa', color: '#606266', fontWeight: 'bold' }"
        >
          <el-table-column prop="resourceName" label="权限名称" width="200" />
          <el-table-column prop="resourceCode" label="权限编码" width="150" />
          <el-table-column prop="permissionKey" label="权限标识" width="250" />
          <el-table-column label="资源类型" width="100">
            <template #default="{ row }">
              <el-tag :type="getResourceTypeTag(row.resourceType)" size="small">
                {{ getResourceTypeText(row.resourceType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="权限来源" width="120">
            <template #default="{ row }">
              <el-tag :type="getSourceType(row.permissionSource)" size="small">
                {{ getSourceText(row.permissionSource) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="来源详情" width="180">
            <template #default="{ row }">
              <span v-if="row.permissionSource === 'BASIC'">
                <el-tag size="small" effect="plain">{{ row.roleName || '-' }}</el-tag>
              </span>
              <span v-else-if="row.permissionSource === 'DEFAULT'">
                <el-tag size="small" effect="plain">{{ row.roleName || '-' }} (缺省)</el-tag>
              </span>
              <span v-else-if="row.permissionSource === 'ADJUSTMENT'">
                <el-tag size="small" :type="row.adjustmentType === 'ADD' ? 'success' : 'danger'" effect="plain">
                  {{ row.adjustmentType === 'ADD' ? '个人新增' : '个人移除' }}
                </el-tag>
              </span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.enabled ? 'success' : 'info'" size="small">
                {{ row.enabled ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 未选择用户提示 -->
      <el-card v-else class="content-card empty-card">
        <el-empty description="请从上方列表中选择一个用户查看权限详情" />
      </el-card>
    </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Menu, Check } from '@element-plus/icons-vue'
import request from '@/utils/request'

// 用户列表相关
const userListLoading = ref(false)
const userList = ref([])
const searchKeyword = ref('')
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 选中用户
const selectedUser = ref(null)

// 权限相关
const permissionLoading = ref(false)
const permissionList = ref([])
const permissionTreeData = ref([])
const permissionStats = reactive({
  total: 0,
  fromRole: 0,
  added: 0,
  removed: 0
})

const permissionTreeRef = ref(null)

const treeProps = {
  children: 'children',
  label: 'label',
  disabled: 'disabled'
}

// 表格行样式（高亮选中行）
const tableRowClassName = ({ row }) => {
  if (selectedUser.value && row.id === selectedUser.value.id) {
    return 'selected-row'
  }
  return ''
}

// 选择用户
const selectUser = (row) => {
  selectedUser.value = row
  loadUserPermissions()
}

// 加载用户列表
const loadUserList = async () => {
  userListLoading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }

    const res = await request({
      url: '/user/list',
      method: 'get',
      params
    })

    userList.value = res.data.records || []
    pagination.total = res.data.total || 0

    // 如果当前选中的用户不在列表中，清空选择
    if (selectedUser.value) {
      const exists = userList.value.find(u => u.id === selectedUser.value.id)
      if (!exists) {
        selectedUser.value = null
        permissionList.value = []
        permissionTreeData.value = []
      }
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
    ElMessage.error('加载用户列表失败：' + error.message)
  } finally {
    userListLoading.value = false
  }
}

// 重置搜索
const resetSearch = () => {
  searchKeyword.value = ''
  pagination.pageNum = 1
  loadUserList()
}

// 加载用户权限
const loadUserPermissions = async () => {
  if (!selectedUser.value) return

  permissionLoading.value = true
  try {
    // 获取用户权限详情（包含来源追溯）
    const permRes = await request({
      url: `/api/users/${selectedUser.value.id}/permissions/detail`,
      method: 'get'
    })

    permissionList.value = permRes.data || []

    // 统计权限来源
    permissionStats.total = permissionList.value.length
    permissionStats.fromRole = permissionList.value.filter(p => 
      p.permissionSource === 'BASIC' || p.permissionSource === 'DEFAULT'
    ).length
    permissionStats.added = permissionList.value.filter(p => 
      p.permissionSource === 'ADJUSTMENT' && p.adjustmentType === 'ADD'
    ).length
    permissionStats.removed = permissionList.value.filter(p => 
      p.permissionSource === 'ADJUSTMENT' && p.adjustmentType === 'REMOVE'
    ).length

    // 构建权限树
    buildPermissionTree()

    ElMessage.success('权限加载成功')
  } catch (error) {
    console.error('加载用户权限失败:', error)
    ElMessage.error('加载用户权限失败：' + error.message)
  } finally {
    permissionLoading.value = false
  }
}

// 构建权限树
const buildPermissionTree = () => {
  // 按权限类型和来源分组
  const menuPermissions = permissionList.value.filter(p => p.resourceType === 'MENU')
  const apiPermissions = permissionList.value.filter(p => p.resourceType === 'API')
  const buttonPermissions = permissionList.value.filter(p => p.resourceType === 'BUTTON')

  // 按来源分组
  const basicPermissions = permissionList.value.filter(p => p.permissionSource === 'BASIC')
  const defaultPermissions = permissionList.value.filter(p => p.permissionSource === 'DEFAULT')
  const adjustmentPermissions = permissionList.value.filter(p => p.permissionSource === 'ADJUSTMENT')

  // 构建树结构
  permissionTreeData.value = [
    {
      label: '菜单权限',
      type: 'category',
      id: 'menu-category',
      children: buildTreeByCategory(menuPermissions, '菜单')
    },
    {
      label: 'API 权限',
      type: 'category',
      id: 'api-category',
      children: buildTreeByCategory(apiPermissions, 'API')
    },
    {
      label: '按钮权限',
      type: 'category',
      id: 'button-category',
      children: buildTreeByCategory(buttonPermissions, '按钮')
    },
    {
      label: '按来源分类',
      type: 'category',
      id: 'source-category',
      children: [
        {
          label: `基本权限 (${basicPermissions.length})`,
          type: 'source',
          id: 'basic-source',
          children: basicPermissions.map(p => ({
            label: p.resourceName,
            type: 'permission',
            id: p.resourceId,
            source: p.permissionSource,
            sourceDetail: p.roleName,
            adjustment: null,
            enabled: p.enabled
          }))
        },
        {
          label: `缺省权限 (${defaultPermissions.length})`,
          type: 'source',
          id: 'default-source',
          children: defaultPermissions.map(p => ({
            label: p.resourceName,
            type: 'permission',
            id: p.resourceId,
            source: p.permissionSource,
            sourceDetail: p.roleName + ' (缺省)',
            adjustment: null,
            enabled: p.enabled
          }))
        },
        {
          label: `调整权限 (${adjustmentPermissions.length})`,
          type: 'source',
          id: 'adjustment-source',
          children: adjustmentPermissions.map(p => ({
            label: p.resourceName,
            type: 'permission',
            id: p.resourceId,
            source: p.permissionSource,
            sourceDetail: p.adjustmentType === 'ADD' ? '个人新增' : '个人移除',
            adjustment: p.adjustmentType,
            enabled: p.enabled
          }))
        }
      ]
    }
  ]
}

// 按类别构建树
const buildTreeByCategory = (permissions, categoryPrefix) => {
  const tree = []
  const groupMap = new Map()

  permissions.forEach(p => {
    // 使用权限名称的前缀作为分组
    const groupName = p.permissionName.split('-')[0] || '其他'
    if (!groupMap.has(groupName)) {
      groupMap.set(groupName, [])
    }
    groupMap.get(groupName).push(p)
  })

  groupMap.forEach((perms, groupName) => {
    tree.push({
      label: `${categoryPrefix} - ${groupName}`,
      type: 'group',
      id: `group-${groupName}`,
      children: perms.map(p => ({
        label: p.permissionName,
        type: 'permission',
        id: p.id,
        source: p.source,
        sourceDetail: p.roleName,
        adjustment: p.adjustment,
        enabled: p.enabled
      }))
    })
  })

  return tree
}

// 获取来源类型样式
const getSourceType = (source) => {
  const types = {
    'BASIC': 'primary',      // 基本权限 - 蓝色
    'DEFAULT': 'info',       // 缺省权限 - 灰色
    'ADJUSTMENT': 'warning'  // 调整权限 - 橙色
  }
  return types[source] || 'info'
}

// 获取来源文本
const getSourceText = (source) => {
  const texts = {
    'BASIC': '基本权限',
    'DEFAULT': '缺省权限',
    'ADJUSTMENT': '调整权限'
  }
  return texts[source] || source
}

// 获取来源样式类
const getSourceClass = (source) => {
  const classes = {
    'BASIC': 'source-basic',
    'DEFAULT': 'source-default',
    'ADJUSTMENT': 'source-adjustment'
  }
  return classes[source] || ''
}

// 获取资源类型标签样式
const getResourceTypeTag = (type) => {
  const types = {
    'MENU': 'primary',
    'API': 'success',
    'BUTTON': 'warning'
  }
  return types[type] || 'info'
}

// 获取资源类型文本
const getResourceTypeText = (type) => {
  const texts = {
    'MENU': '菜单',
    'API': 'API',
    'BUTTON': '按钮'
  }
  return texts[type] || type
}

onMounted(() => {
  loadUserList()
})
</script>

<style scoped lang="scss">
.user-permission-detail-page {
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
    margin-bottom: 20px;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 16px;
      font-weight: 600;
    }

    .user-select-form {
      margin-bottom: 10px;
    }

    .selected-row {
      background-color: #ecf5ff !important;
    }
  }

  .permission-detail-card {
    .permission-tree-container {
      border: 1px solid #e4e7ed;
      border-radius: 4px;
      padding: 16px;
      background: #fafafa;
      min-height: 300px;
      max-height: 500px;
      overflow-y: auto;
    }

    .permission-tree-node {
      display: flex;
      align-items: center;
      flex: 1;

      .node-label {
        font-size: 14px;

        &.source-basic {
          color: #409eff;
          font-weight: 500;
        }

        &.source-default {
          color: #909399;
        }

        &.source-adjustment {
          color: #e6a23c;
          font-weight: 500;
        }
      }

      .source-detail {
        margin-left: 8px;
        font-size: 12px;
        color: #909399;
      }
    }
  }

  .empty-card {
    text-align: center;
    padding: 60px 0;
  }
}
</style>
