<template>
  <el-button
    v-permission="'system:menu:refresh:button'"
    :loading="loading"
    @click="handleRefresh"
    :title="tooltip"
  >
    <el-icon><Refresh /></el-icon>
    {{ showText ? '刷新菜单' : '' }}
  </el-button>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useResourceStore } from '@/stores/resource'

const props = defineProps({
  // 是否显示文字
  showText: {
    type: Boolean,
    default: false
  },
  // 提示文字
  tooltip: {
    type: String,
    default: '刷新菜单缓存'
  },
  // 刷新后是否自动跳转
  redirectAfterRefresh: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['refresh-success', 'refresh-error'])

const userStore = useUserStore()
const resourceStore = useResourceStore()

const loading = ref(false)

// 刷新菜单
const handleRefresh = async () => {
  if (loading.value) return
  
  loading.value = true
  try {
    // 1. 刷新资源缓存
    await resourceStore.refreshResourceCache()
    
    // 2. 刷新用户菜单
    await userStore.refreshMenus()
    
    ElMessage.success('菜单刷新成功')
    
    emit('refresh-success')
    
    // 3. 如果需要，刷新当前页面
    if (props.redirectAfterRefresh) {
      setTimeout(() => {
        window.location.reload()
      }, 500)
    }
  } catch (error) {
    console.error('刷新菜单失败:', error)
    ElMessage.error('刷新菜单失败：' + error.message)
    emit('refresh-error', error)
  } finally {
    loading.value = false
  }
}

// 暴露方法
defineExpose({
  refresh: handleRefresh
})
</script>
