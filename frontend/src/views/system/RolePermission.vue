<template>
    <div class="role-permission">
      <el-card class="header-card">
        <div class="page-header">
          <h2>角色权限配置</h2>
          <p class="description">为角色分配功能权限，受保护角色的权限不可调整</p>
        </div>
      </el-card>

      <el-card class="content-card">
        <!-- 角色选择区域 -->
        <div class="role-selection-area">
          <el-form :inline="true" class="role-select-form">
            <!-- 主角色选择 -->
            <el-form-item label="选择角色">
              <el-select
                v-model="selectedRoleId"
                placeholder="请选择角色"
                filterable
                style="width: 300px"
                @change="onSelectedRoleChange"
              >
                <el-option
                  v-for="role in roleList"
                  :key="role.id"
                  :label="`${role.role_name} (${role.role_code})`"
                  :value="role.id"
                />
              </el-select>
            </el-form-item>
            
            <!-- 参考角色选择 -->
            <el-form-item label="参考角色（可选）">
              <el-select
                v-model="referenceRoleId"
                placeholder="选择参考角色"
                filterable
                clearable
                style="width: 300px"
                @change="onReferenceRoleChange"
              >
                <el-option
                  v-for="role in roleList"
                  :key="role.id"
                  :label="`${role.role_name} (${role.role_code})`"
                  :value="role.id"
                  :disabled="role.id === selectedRoleId"
                />
              </el-select>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="loadRolePermissionData" :disabled="!selectedRoleId">
                加载权限数据
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 角色信息展示 - 左右布局 -->
        <div v-if="selectedRole || referenceRole" class="role-info-container">
          <div class="role-info-grid">
            <!-- 当前角色信息 - 左边 -->
            <div v-if="selectedRole" class="role-info-card current-role">
              <el-descriptions title="角色信息" :column="1" border size="small">
                <el-descriptions-item label="角色名称">{{ selectedRole.role_name }}</el-descriptions-item>
                <el-descriptions-item label="角色编码">{{ selectedRole.role_code }}</el-descriptions-item>
                <el-descriptions-item label="描述">{{ selectedRole.role_description || '-' }}</el-descriptions-item>
              </el-descriptions>
            </div>
            
            <!-- 参考角色信息 - 右边 -->
            <div v-if="referenceRole" class="role-info-card reference-role">
              <el-descriptions title="参考角色信息" :column="1" border size="small">
                <el-descriptions-item label="角色名称">{{ referenceRole.role_name }}</el-descriptions-item>
                <el-descriptions-item label="角色编码">{{ referenceRole.role_code }}</el-descriptions-item>
                <el-descriptions-item label="描述">{{ referenceRole.role_description || '-' }}</el-descriptions-item>
              </el-descriptions>
            </div>
          </div>
        </div>

        <!-- 受保护角色提示 -->
        <el-alert
          v-if="isSuperAdminRole"
          title="超级管理员角色权限不可调整"
          type="warning"
          :closable="false"
          style="margin-top: 20px"
          show-icon
        >
          超级管理员角色始终拥有系统全部权限，不允许调整。
        </el-alert>
        
        <el-alert
          v-else-if="isSystemAdminRole"
          title="系统管理员角色权限不可删除"
          type="info"
          :closable="false"
          style="margin-top: 20px"
          show-icon
        >
          系统管理员角色为系统保护角色，不允许删除。
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
              <el-checkbox disabled :model-value="true" /> 基本权限（系统自动授权）
            </span>
            <span class="legend-item">
              <el-checkbox disabled :model-value="true" /> 缺省权限（角色创建时默认拥有）
            </span>
            <span class="legend-item">
              <el-checkbox disabled :model-value="true" /> 生效权限（角色实际拥有的权限）
            </span>
            <span class="legend-item">
              <el-tag type="success" size="small">当前角色</el-tag> 可编辑
            </span>
            <span class="legend-item" v-if="referenceRoleId">
              <el-tag type="info" size="small">参考角色</el-tag> 只读
            </span>
          </div>
        </el-alert>

        <!-- 权限表格 - 带固定表头和滚动条 -->
        <div class="permission-table-container" v-loading="loading">
          <el-table
            :data="currentPageData"
            row-key="resourceId"
            :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
            style="width: 100%"
            border
            height="500px"
            :max-height="500"
          >
            <!-- 资源信息列 - 再缩短40% -->
            <el-table-column label="资源信息" min-width="144">
              <template #default="{ row }">
                <div class="resource-info" :style="{ paddingLeft: (row.level * 20) + 'px' }">
                  <el-icon v-if="row.type === 'MODULE'" class="resource-icon"><Folder /></el-icon>
                  <el-icon v-else-if="row.type === 'MENU'" class="resource-icon"><Menu /></el-icon>
                  <el-icon v-else-if="row.type === 'PAGE'" class="resource-icon"><Document /></el-icon>
                  <el-icon v-else-if="row.type === 'ELEMENT'" class="resource-icon"><Setting /></el-icon>
                  <el-icon v-else class="resource-icon"><Document /></el-icon>
                  
                  <div class="resource-details">
                    <div class="resource-name">{{ row.name }}</div>
                    <div class="resource-code">{{ row.code }} ({{ row.type }})</div>
                    <div v-if="row.permissionKey" class="permission-key">{{ row.permissionKey }}</div>
                  </div>
                </div>
              </template>
            </el-table-column>

            <!-- 当前角色权限列 -->
            <el-table-column label="当前角色权限" align="center" width="200">
              <template #header>
                <div class="column-header">
                  <div>当前角色权限</div>
                  <div v-if="selectedRole" class="role-name">{{ selectedRole.role_name }}</div>
                </div>
              </template>
              <template #default="{ row }">
                <div class="permission-checkboxes-horizontal" :class="{ 'disabled-checkboxes': isProtectedRole }">
                  <!-- 水平排列的三个复选框 -->
                  <div class="checkbox-row">
                    <!-- 是否生效权限 -->
                    <div class="checkbox-item">
                      <el-checkbox
                        v-model="row.is_effective"
                        :disabled="isProtectedRole"
                        @change="onPermissionChange(row, 'is_effective')"
                        size="small"
                      >
                        生效
                      </el-checkbox>
                    </div>
                    
                    <!-- 是否基本权限（只读，从状态推断） -->
                    <div class="checkbox-item">
                      <el-checkbox
                        :model-value="row.status === 'basic'"
                        disabled
                        size="small"
                      >
                        基本
                      </el-checkbox>
                    </div>
                    
                    <!-- 是否缺省权限（只读，从状态推断） -->
                    <div class="checkbox-item">
                      <el-checkbox
                        :model-value="row.status === 'default'"
                        disabled
                        size="small"
                      >
                        缺省
                      </el-checkbox>
                    </div>
                  </div>
                </div>
              </template>
            </el-table-column>

            <!-- 参考角色权限列 -->
            <el-table-column v-if="referenceRoleId" label="参考角色权限" align="center" width="200">
              <template #header>
                <div class="column-header">
                  <div>参考角色权限</div>
                  <div v-if="referenceRole" class="role-name">{{ referenceRole.role_name }}</div>
                </div>
              </template>
              <template #default="{ row }">
                <div class="permission-checkboxes-horizontal readonly">
                  <!-- 水平排列的三个复选框 -->
                  <div class="checkbox-row">
                    <!-- 参考角色是否生效权限 -->
                    <div class="checkbox-item">
                      <el-checkbox
                        :model-value="row.ref_is_effective"
                        disabled
                        size="small"
                      >
                        生效
                      </el-checkbox>
                    </div>
                    
                    <!-- 参考角色是否基本权限（从状态推断） -->
                    <div class="checkbox-item">
                      <el-checkbox
                        :model-value="row.ref_status === 'basic'"
                        disabled
                        size="small"
                      >
                        基本
                      </el-checkbox>
                    </div>
                    
                    <!-- 参考角色是否缺省权限（从状态推断） -->
                    <div class="checkbox-item">
                      <el-checkbox
                        :model-value="row.ref_status === 'default'"
                        disabled
                        size="small"
                      >
                        缺省
                      </el-checkbox>
                    </div>
                  </div>
                </div>
              </template>
            </el-table-column>

            <!-- 操作列 -->
            <el-table-column label="操作" width="100" align="center">
              <template #default="{ row }">
                <el-button
                  type="primary"
                  size="small"
                  @click="savePermission(row)"
                  :disabled="isProtectedRole || !hasPermissionChanged(row)"
                >
                  保存
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 分页 -->
        <div class="pagination-container" v-if="flattenedPermissions.length > 0">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="flattenedPermissions.length"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>

        <!-- 批量操作 -->
        <div class="batch-actions" v-if="!isProtectedRole && flattenedPermissions.length > 0">
          <el-button type="primary" @click="saveAllPermissions" :loading="savingAll">
            保存所有修改
          </el-button>
          <el-button @click="resetAllPermissions">
            重置所有修改
          </el-button>
          <span class="change-count">
            已修改: {{ changedPermissionCount }} 个权限
          </span>
        </div>
      </el-card>
    </div>
  </template>

  <script setup>
  import { ref, computed, onMounted, watch } from 'vue'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import {
    Folder,
    Menu,
    Document,
    Setting,
    Connection,
    User,
    Check,
    Close
  } from '@element-plus/icons-vue'
  import { getAllRoles, getRolePermissionTree, adjustRolePermission } from '@/api/permission'


  // 导入角色常量
  import { ROLE_SUPER_ADMIN, ROLE_SYSTEM_ADMIN } from '@/constants/roles'

  // 角色列表
  const roleList = ref([])
  
  // 当前选中的角色ID
  const selectedRoleId = ref(null)
  const selectedRole = ref(null)
  
  // 参考角色ID
  const referenceRoleId = ref(null)
  const referenceRole = ref(null)
  
  // 权限数据
  const permissionTree = ref([])
  const flattenedPermissions = ref([])
  
  // 加载状态
  const loading = ref(false)
  const savingAll = ref(false)
  
  // 分页相关
  const currentPage = ref(1)
  const pageSize = ref(20)
  
  // 当前页数据
  const currentPageData = computed(() => {
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    return flattenedPermissions.value.slice(start, end)
  })
  
  // 是否为超级管理员角色（完全保护，禁止调整权限）
  const isSuperAdminRole = computed(() => {
    return selectedRole.value && selectedRole.value.role_code === ROLE_SUPER_ADMIN
  })

  // 是否为系统管理员角色（部分保护，禁止删除）
  const isSystemAdminRole = computed(() => {
    return selectedRole.value && selectedRole.value.role_code === ROLE_SYSTEM_ADMIN
  })

  // 是否为受保护角色（向后兼容）
  const isProtectedRole = computed(() => {
    return isSuperAdminRole.value || isSystemAdminRole.value
  })

  // 已修改的权限数量
  const changedPermissionCount = computed(() => {
    return flattenedPermissions.value.filter(p => hasPermissionChanged(p)).length
  })

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

  // 选中的角色发生变化
  const onSelectedRoleChange = () => {
    if (selectedRoleId.value) {
      const role = roleList.value.find(r => r.id === selectedRoleId.value)
      selectedRole.value = role
      
      // 如果参考角色是当前选中的角色，清空参考角色
      if (referenceRoleId.value === selectedRoleId.value) {
        referenceRoleId.value = null
        referenceRole.value = null
      }
    } else {
      selectedRole.value = null
    }
  }

  // 参考角色发生变化
  const onReferenceRoleChange = () => {
    if (referenceRoleId.value) {
      const role = roleList.value.find(r => r.id === referenceRoleId.value)
      referenceRole.value = role
    } else {
      referenceRole.value = null
    }
  }

  // 加载权限数据
  const loadRolePermissionData = async () => {
    if (!selectedRoleId.value) return
    
    loading.value = true
    try {
      // 1. 加载当前角色的权限树
      const treeRes = await getRolePermissionTree(selectedRoleId.value)
      permissionTree.value = treeRes.data || []
      
      // 2. 如果选择了参考角色，加载参考角色的权限树
      let referencePermissionTree = []
      if (referenceRoleId.value) {
        const refRes = await getRolePermissionTree(referenceRoleId.value)
        referencePermissionTree = refRes.data || []
      }
      
      // 3. 合并数据并展平
      flattenedPermissions.value = flattenPermissionTree(
        permissionTree.value,
        referencePermissionTree
      )
      
      ElMessage.success('权限数据加载成功')
    } catch (error) {
      console.error('加载权限数据失败:', error)
      ElMessage.error('加载权限数据失败：' + (error.message || '未知错误'))
    } finally {
      loading.value = false
    }
  }

  // 在树中查找节点
  const findNodeInTree = (tree, resourceId) => {
    for (const node of tree) {
      if (node.id === resourceId) {
        return node
      }
      if (node.children && node.children.length > 0) {
        const found = findNodeInTree(node.children, resourceId)
        if (found) return found
      }
    }
    return null
  }

  // 展平权限树，合并权限数据
  const flattenPermissionTree = (tree, referenceTree = [], level = 0) => {
    const result = []
    
    for (const node of tree) {
      // 查找参考角色对应的节点
      const refNode = findNodeInTree(referenceTree, node.id)
      
      const flatNode = {
        resourceId: node.id,
        name: node.name,
        code: node.code,
        type: node.type,
        permissionKey: node.permissionKey,
        level: level,
        
        // 当前状态
        status: node.status || 'none',
        
        // 当前角色的权限数据（只使用is_effective）
        is_effective: node.status === 'basic' || node.status === 'default' || node.status === 'added',
        
        // 原始值（用于比较是否修改）
        original_is_effective: node.status === 'basic' || node.status === 'default' || node.status === 'added',
        
        // 参考角色的权限数据
        ref_status: refNode ? (refNode.status || 'none') : 'none',
        ref_is_effective: refNode ? (refNode.status === 'basic' || refNode.status === 'default' || refNode.status === 'added') : false,
        
        // 子节点
        children: node.children ? flattenPermissionTree(node.children, referenceTree, level + 1) : []
      }
      
      result.push(flatNode)
      
      // 如果有子节点，也添加到结果中
      if (flatNode.children && flatNode.children.length > 0) {
        result.push(...flatNode.children)
      }
    }
    
    return result
  }

  // 权限复选框发生变化
  const onPermissionChange = (row, field) => {
    // 如果状态是basic，不允许取消生效权限
    if (field === 'is_effective' && !row.is_effective && row.status === 'basic') {
      ElMessage.warning('基本权限不可取消')
      row.is_effective = true
    }
  }

  // 检查权限是否已修改
  const hasPermissionChanged = (row) => {
    return row.is_effective !== row.original_is_effective
  }

  // 保存单个权限
  const savePermission = async (row) => {
    if (!selectedRoleId.value || isProtectedRole.value) return
    
    try {
      await ElMessageBox.confirm(
        `确定要更新权限"${row.name}"吗？`,
        '确认操作',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      
      // 根据权限状态决定是添加还是移除
      const action = row.is_effective ? 'ADD' : 'REMOVE'
      await adjustRolePermission(selectedRoleId.value, {
        resourceId: row.resourceId,
        action: action
      })
      
      // 更新原始值
      row.original_is_effective = row.is_effective
      
      ElMessage.success('权限更新成功')
    } catch (error) {
      if (error !== 'cancel') {
        console.error('更新权限失败:', error)
        ElMessage.error('更新权限失败：' + (error.message || '未知错误'))
      }
    }
  }

  // 分页处理方法
  const handleSizeChange = (newSize) => {
    pageSize.value = newSize
    currentPage.value = 1 // 重置到第一页
  }
  
  const handleCurrentChange = (newPage) => {
    currentPage.value = newPage
  }

  // 保存所有修改的权限
  const saveAllPermissions = async () => {
    if (!selectedRoleId.value || isProtectedRole.value || changedPermissionCount.value === 0) return
    
    try {
      await ElMessageBox.confirm(
        `确定要更新 ${changedPermissionCount.value} 个权限吗？`,
        '确认批量操作',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      
      savingAll.value = true
      
      // 收集所有修改的权限
      const updates = flattenedPermissions.value
        .filter(p => hasPermissionChanged(p))
        .map(p => ({
          resourceId: p.resourceId,
          is_effective: p.is_effective
        }))
      
      // 批量更新（这里需要后端支持批量更新API）
      // 暂时使用循环单个更新
      let successCount = 0
      let errorCount = 0
      
      for (const updateData of updates) {
        try {
          const action = updateData.is_effective ? 'ADD' : 'REMOVE'
          await adjustRolePermission(selectedRoleId.value, {
            resourceId: updateData.resourceId,
            action: action
          })
          successCount++
        } catch (error) {
          console.error(`更新权限 ${updateData.resourceId} 失败:`, error)
          errorCount++
        }
      }
      
      // 重新加载数据
      await loadRolePermissionData()
      
      if (errorCount === 0) {
        ElMessage.success(`成功更新 ${successCount} 个权限`)
      } else {
        ElMessage.warning(`成功更新 ${successCount} 个权限，失败 ${errorCount} 个`)
      }
    } catch (error) {
      if (error !== 'cancel') {
        console.error('批量更新权限失败:', error)
        ElMessage.error('批量更新权限失败：' + (error.message || '未知错误'))
      }
    } finally {
      savingAll.value = false
    }
  }

  // 重置所有修改
  const resetAllPermissions = () => {
    if (changedPermissionCount.value === 0) return
    
    ElMessageBox.confirm(
      `确定要重置 ${changedPermissionCount.value} 个修改吗？`,
      '确认重置',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(() => {
      flattenedPermissions.value.forEach(p => {
        p.is_effective = p.original_is_effective
      })
      
      ElMessage.success('已重置所有修改')
    }).catch(() => {
      // 用户取消
    })
  }

  // 页面加载时初始化
  onMounted(() => {
    loadRoles()
  })

  // 监听选中的角色ID变化
  watch(selectedRoleId, (newVal) => {
    if (newVal) {
      loadRolePermissionData()
    } else {
      permissionTree.value = []
      flattenedPermissions.value = []
    }
  })

  // 监听参考角色ID变化
  watch(referenceRoleId, (newVal) => {
    if (selectedRoleId.value) {
      loadRolePermissionData()
    }
  })
  </script>

  <style lang="scss" scoped>
  .role-permission {
    padding: 20px;
  }

  .header-card {
    margin-bottom: 20px;
  }

  .page-header {
    h2 {
      margin: 0 0 8px 0;
      font-size: 24px;
      color: #303133;
    }

    .description {
      margin: 0;
      font-size: 14px;
      color: #909399;
    }
  }

  .role-selection-area {
    margin-bottom: 20px;
  }

  .role-select-form {
    display: flex;
    align-items: center;
    gap: 12px; /* 原16px，缩小 */
    flex-wrap: wrap;
    
    .el-form-item {
      margin-bottom: 0;
      
      .el-form-item__label {
        font-size: 13px; /* 原14px，缩小 */
      }
    }
  }

  .role-info-container {
    margin: 20px 0;
  }

  .role-info-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 16px;
  }

  .role-info-card {
    .el-descriptions {
      font-size: 12px;
    }
    
    .el-descriptions__title {
      font-size: 14px;
      margin-bottom: 8px;
    }
    
    .el-descriptions__label {
      width: 80px;
    }
  }

  .current-role {
    .el-descriptions__title {
      color: #409eff;
    }
  }

  .reference-role {
    .el-descriptions__title {
      color: #909399;
    }
  }

  .legend-container {
    display: flex;
    flex-wrap: wrap;
    gap: 12px; /* 原16px，缩小 */
    align-items: center;
    font-size: 12px; /* 整体字体缩小 */
  }

  .legend-item {
    display: flex;
    align-items: center;
    gap: 2px; /* 原4px，缩小 */
    font-size: 12px; /* 原14px，缩小 */
  }

  .permission-table-container {
    margin-top: 20px;
    
    /* 表格整体样式调整 - 行高缩小30%，字体同步调整 */
    .el-table {
      font-size: 12px;
      
      .el-table__row {
        height: 28px; /* 进一步缩小，因为复选框改为水平排列 */
      }
      
      .el-table__cell {
        padding: 4px 0;
      }
      
      .el-checkbox {
        font-size: 12px;
        
        .el-checkbox__label {
          font-size: 12px;
        }
      }
      
      .el-button {
        font-size: 12px;
        padding: 5px 10px;
        height: 28px;
      }
    }
  }

  .resource-info {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .resource-icon {
    font-size: 18px;
    color: #606266;
  }

  .resource-details {
    .resource-name {
      font-weight: 500;
      color: #303133;
      font-size: 12px; /* 进一步缩小，因为列宽缩小40% */
      line-height: 1.2;
    }

    .resource-code {
      font-size: 11px; /* 原12px，缩小 */
      color: #909399;
      line-height: 1.2;
    }

    .permission-key {
      font-size: 10px; /* 原12px，缩小 */
      color: #67c23a;
      background: #f0f9eb;
      padding: 1px 4px;
      border-radius: 3px;
      display: inline-block;
      margin-top: 1px;
    }
  }

  .column-header {
    text-align: center;
    font-size: 13px; /* 列标题字体缩小 */
    .role-name {
      font-size: 11px; /* 原12px，缩小 */
      color: #909399;
      margin-top: 2px; /* 原4px，缩小 */
    }
  }

  .permission-checkboxes {
    display: flex;
    flex-direction: column;
    gap: 4px; /* 原8px，缩小 */
    align-items: center;

    &.readonly {
      opacity: 0.7;
    }

    &.disabled-checkboxes {
      opacity: 0.5;
      pointer-events: none;
    }

    .checkbox-item {
      display: flex;
      align-items: center;
      gap: 2px; /* 原4px，缩小 */
    }
  }

  /* 水平排列的权限复选框 */
  .permission-checkboxes-horizontal {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100%;
    
    .checkbox-row {
      display: flex;
      gap: 8px;
      align-items: center;
      flex-wrap: wrap;
      justify-content: center;
      
      .checkbox-item {
        display: flex;
        align-items: center;
        gap: 2px;
        
        .el-checkbox {
          margin: 0;
          
          .el-checkbox__label {
            font-size: 11px;
            padding-left: 4px;
          }
        }
      }
    }
    
    &.readonly {
      opacity: 0.7;
    }
    
    &.disabled-checkboxes {
      opacity: 0.5;
      pointer-events: none;
    }
  }

  .batch-actions {
    margin-top: 16px; /* 原20px，缩小 */
    padding-top: 16px; /* 原20px，缩小 */
    border-top: 1px solid #ebeef5;
    display: flex;
    align-items: center;
    gap: 12px; /* 原16px，缩小 */
    
    .el-button {
      font-size: 13px; /* 按钮字体适当缩小 */
      padding: 7px 14px;
      height: 32px;
    }

    .change-count {
      margin-left: auto;
      color: #e6a23c;
      font-weight: 500;
      font-size: 13px; /* 字体缩小 */
    }
  }

  /* 分页容器 */
  .pagination-container {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
    
    .el-pagination {
      font-size: 12px;
      
      .el-pagination__total,
      .el-pagination__jump,
      .el-pagination__sizes {
        font-size: 12px;
      }
    }
  }

  // 树节点样式
  .tree-node {
    display: flex;
    align-items: center;
    padding: 8px 0;
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
        }
      }
    }
  }
  </style>
