<template>
    <div class="user-permission-page">
      <el-card class="header-card">
      <div class="page-header">
        <h2>用户权限配置</h2>
        <p class="description">系统管理员可在此页面为指定用户定制菜单权限，自定义权限优先级高于角色默认权限</p>
      </div>
    </el-card>

    <el-card class="content-card">
        <!-- 用户选择 -->
        <el-form :inline="true" class="user-select-form">
          <el-form-item label="选择用户">
            <el-select
              v-model="selectedUserId"
              placeholder="请选择用户"
              filterable
              style="width: 300px"
              @change="loadUserPermissions"
            >
              <el-option
                v-for="user in userList"
                :key="user.id"
                :label="`${user.realName} (${user.username})`"
                :value="user.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadUserPermissions" :disabled="!selectedUserId">
              加载权限
            </el-button>
          </el-form-item>
          <el-form-item>
            <el-button type="success" @click="savePermissions" :disabled="!selectedUserId || !hasChanges">
              保存修改
            </el-button>
          </el-form-item>
          <el-form-item>
            <el-button @click="resetPermissions" :disabled="!selectedUserId">
              重置为角色默认
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 用户信息展示 -->
        <div v-if="selectedUser" class="user-info">
          <el-descriptions title="用户信息" :column="3" border>
            <el-descriptions-item label="用户名">{{ selectedUser.username }}</el-descriptions-item>
            <el-descriptions-item label="姓名">{{ selectedUser.realName }}</el-descriptions-item>
            <el-descriptions-item label="角色">
              <el-tag v-for="role in selectedUser.roleNames" :key="role" size="small" style="margin-right: 4px;">
                {{ role }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="公司">{{ selectedUser.companyName }}</el-descriptions-item>
            <el-descriptions-item label="公司类型">{{ selectedUser.companyTypeName }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 菜单权限表格 -->
        <el-table 
          :data="permissionList" 
          border 
          style="width: 100%; margin-top: 20px"
          v-loading="loading"
        >
          <el-table-column prop="menuName" label="菜单名称" width="150" />
          <el-table-column prop="menuCode" label="菜单编码" width="150" />
          <el-table-column prop="menuPath" label="路径" width="200" />
          
          <el-table-column label="查看权限" width="120" align="center">
            <template #default="{ row }">
              <el-switch
                v-model="row.canView"
                :disabled="row.permissionSource === 'ROLE' && !hasCustomPermission(row.menuCode)"
                @change="markAsChanged(row)"
              />
            </template>
          </el-table-column>
          
          <el-table-column label="操作权限" width="120" align="center">
            <template #default="{ row }">
              <el-switch
                v-model="row.canOperate"
                :disabled="row.permissionSource === 'ROLE' && !hasCustomPermission(row.menuCode)"
                @change="markAsChanged(row)"
              />
            </template>
          </el-table-column>
          
          <el-table-column label="权限来源" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.permissionSource === 'CUSTOM' ? 'warning' : 'info'" size="small">
                {{ row.permissionSource === 'CUSTOM' ? '自定义' : '角色默认' }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column label="授权信息" width="180">
            <template #default="{ row }">
              <span v-if="row.permissionSource === 'CUSTOM'">
                {{ row.grantedAt ? new Date(row.grantedAt).toLocaleString() : '-' }}
              </span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button
                v-if="row.permissionSource === 'CUSTOM'"
                type="danger"
                size="small"
                text
                @click="removeCustomPermission(row)"
              >
                恢复默认
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 权限说明 -->
        <el-alert
          title="权限说明"
          type="info"
          :closable="false"
          style="margin-top: 20px"
        >
          <ul>
            <li><strong>角色默认权限：</strong>根据用户角色自动分配的菜单权限，不可直接修改</li>
            <li><strong>自定义权限：</strong>系统管理员手动配置的权限，优先级高于角色默认权限</li>
            <li><strong>恢复默认：</strong>删除自定义权限配置，恢复为角色默认权限</li>
            <li><strong>系统保护菜单：</strong>个人中心等基础菜单权限不可修改</li>
          </ul>
        </el-alert>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllMenus, getUserPermissions, updateUserPermission, deleteUserPermission } from '@/api/menu'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const loading = ref(false)
const userList = ref([])
const selectedUserId = ref(null)
const selectedUser = ref(null)
const allMenus = ref([])
const permissionList = ref([])
const originalPermissions = ref([]) // 用于检测变更

// 检查是否有未保存的修改
const hasChanges = computed(() => {
  if (originalPermissions.value.length === 0) return false
  
  return permissionList.value.some((perm, index) => {
    const original = originalPermissions.value[index]
    if (!original) return false
    return perm.canView !== original.canView || perm.canOperate !== original.canOperate
  })
})

// 加载用户列表
const loadUserList = async () => {
  try {
    const res = await request({
      url: '/user/list',
      method: 'get',
      params: { pageNum: 1, pageSize: 100 }
    })
    userList.value = res.data.records || []
  } catch (error) {
    console.error('加载用户列表失败:', error)
  }
}

// 加载所有菜单
const loadAllMenus = async () => {
  try {
    const res = await getAllMenus()
    allMenus.value = res.data || []
  } catch (error) {
    console.error('加载菜单列表失败:', error)
  }
}

// 加载用户权限
const loadUserPermissions = async () => {
  if (!selectedUserId.value) return
  
  loading.value = true
  try {
    // 获取用户详情
    const userRes = await request({
      url: `/user/${selectedUserId.value}`,
      method: 'get'
    })
    selectedUser.value = userRes.data
    
    // 获取用户菜单权限
    const permRes = await getUserPermissions(selectedUserId.value)
    permissionList.value = permRes.data || []
    
    // 保存原始状态用于检测变更
    originalPermissions.value = permissionList.value.map(p => ({
      menuCode: p.menuCode,
      canView: p.canView,
      canOperate: p.canOperate
    }))
    
    ElMessage.success('权限加载成功')
  } catch (error) {
    console.error('加载权限失败:', error)
    ElMessage.error('加载权限失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 检查是否有自定义权限
const hasCustomPermission = (menuCode) => {
  const perm = permissionList.value.find(p => p.menuCode === menuCode)
  return perm && perm.permissionSource === 'CUSTOM'
}

// 标记为已修改
const markAsChanged = (row) => {
  // 如果是角色默认权限且首次修改，自动创建自定义权限
  if (row.permissionSource === 'ROLE') {
    row.permissionSource = 'CUSTOM'
  }
}

// 保存权限修改
const savePermissions = async () => {
  if (!selectedUserId.value) return
  
  try {
    await ElMessageBox.confirm('确定要保存权限修改吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const changes = permissionList.value.filter((perm, index) => {
      const original = originalPermissions.value[index]
      if (!original) return false
      return perm.canView !== original.canView || perm.canOperate !== original.canOperate
    })
    
    for (const change of changes) {
      await updateUserPermission({
        userId: selectedUserId.value,
        menuId: change.menuId,
        canView: change.canView,
        canOperate: change.canOperate
      })
    }
    
    ElMessage.success('权限保存成功')
    await loadUserPermissions() // 重新加载
  } catch (error) {
    if (error !== 'cancel') {
      console.error('保存权限失败:', error)
      ElMessage.error('保存权限失败：' + error.message)
    }
  }
}

// 重置为角色默认
const resetPermissions = async () => {
  if (!selectedUserId.value) return
  
  try {
    await ElMessageBox.confirm('确定要重置所有自定义权限为角色默认吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const customPerms = permissionList.value.filter(p => p.permissionSource === 'CUSTOM')
    for (const perm of customPerms) {
      await deleteUserPermission(selectedUserId.value, perm.menuId)
    }
    
    ElMessage.success('权限已重置为角色默认')
    await loadUserPermissions()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重置权限失败:', error)
      ElMessage.error('重置权限失败：' + error.message)
    }
  }
}

// 删除单条自定义权限
const removeCustomPermission = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要恢复 "${row.menuName}" 为角色默认权限吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteUserPermission(selectedUserId.value, row.menuId)
    ElMessage.success('已恢复为角色默认权限')
    await loadUserPermissions()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除权限失败:', error)
      ElMessage.error('删除权限失败：' + error.message)
    }
  }
}

onMounted(() => {
  loadUserList()
  loadAllMenus()
})
</script>

<style scoped lang="scss">
.user-permission-page {
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
    .user-select-form {
      margin-bottom: 20px;
    }
    
    .user-info {
      margin-bottom: 20px;
    }
  }
}
</style>
