<template>
  <div class="not-found">
    <h1>404</h1>
    <p>页面未找到</p>
    <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
  </div>
</template>

<style scoped>
.not-found {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  
  h1 {
    font-size: 72px;
    color: #667eea;
    margin-bottom: 16px;
  }
  
  p {
    font-size: 20px;
    color: #666;
    margin-bottom: 32px;
  }
}
</style>
