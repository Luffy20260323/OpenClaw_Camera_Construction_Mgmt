import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 创建 axios 实例
const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('accessToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    // 如果是 blob 类型（文件下载），直接返回
    if (response.config.responseType === 'blob') {
      return response.data
    }
    
    const res = response.data
    
    // 如果返回的状态码不是 200，说明接口有错误
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      
      // 401: 未授权，跳转到登录页
      if (res.code === 401) {
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
        localStorage.removeItem('userInfo')
        router.push('/login')
      }
      
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    
    return res
  },
  error => {
    console.error('请求错误:', error)
    
    // 处理 HTTP 状态码错误
    if (error.response) {
      const status = error.response.status
      if (status === 401) {
        // 401: 未授权，Token 过期或无效
        ElMessage.error('登录已过期，请重新登录')
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
        localStorage.removeItem('userInfo')
        router.push('/login')
      } else if (status === 403) {
        // 403: 无权限访问（但登录有效）
        ElMessage.error('无权限访问该功能')
        // 不清除 Token，因为用户已登录，只是没有该功能的权限
      } else if (status === 404) {
        ElMessage.error('请求的资源不存在')
      } else if (status === 500) {
        ElMessage.error('服务器错误')
      } else {
        ElMessage.error('请求失败：' + status)
      }
    } else {
      ElMessage.error('网络错误：' + error.message)
    }
    
    return Promise.reject(error)
  }
)

export default request
