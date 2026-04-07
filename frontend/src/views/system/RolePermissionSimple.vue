<template>
  <div class="role-permission-simple">
    <h1>角色权限配置（测试版）</h1>
    <p>这是一个测试页面，用于验证路由是否正常工作。</p>
    
    <el-card>
      <template #header>
        <span>功能说明</span>
      </template>
      <div>
        <p>1. 选择角色设置权限</p>
        <p>2. 可选参考角色进行对比</p>
        <p>3. 编辑权限并保存</p>
      </div>
    </el-card>
    
    <el-button type="primary" @click="testClick">测试按钮</el-button>
    <p>点击次数: {{ clickCount }}</p>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const clickCount = ref(0)

const testClick = () => {
  clickCount.value++
  ElMessage.success(`点击了 ${clickCount.value} 次`)
}
</script>

<style scoped>
.role-permission-simple {
  padding: 20px;
}
</style>