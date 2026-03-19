<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <div class="logo">
          <el-icon :size="40"><VideoPlay /></el-icon>
        </div>
        <h1>视频监控点位施工项目管理系统</h1>
        <p>Camera Lifecycle Management System</p>
      </div>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            size="large"
            prefix-icon="User"
            clearable
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        
        <!-- 图形验证码 -->
        <el-form-item 
          v-if="captchaType === 'image'" 
          prop="captcha"
        >
          <el-row :gutter="10">
            <el-col :span="14">
              <el-input
                v-model="loginForm.captcha"
                placeholder="请输入验证码"
                size="large"
                prefix-icon="Key"
                clearable
                maxlength="4"
              />
            </el-col>
            <el-col :span="10">
              <div class="captcha-image" @click="refreshCaptcha">
                <img :src="captchaImage" alt="验证码" />
              </div>
            </el-col>
          </el-row>
        </el-form-item>
        
        <!-- 手机验证码 -->
        <el-form-item 
          v-if="captchaType === 'sms'" 
          prop="captcha"
        >
          <el-row :gutter="10">
            <el-col :span="14">
              <el-input
                v-model="loginForm.captcha"
                placeholder="请输入短信验证码"
                size="large"
                prefix-icon="Key"
                clearable
                maxlength="6"
              />
            </el-col>
            <el-col :span="10">
              <el-button 
                type="primary" 
                size="large"
                :disabled="smsCountdown > 0"
                @click="sendSmsCode"
                style="width: 100%"
              >
                {{ smsCountdown > 0 ? `${smsCountdown}秒后重试` : '获取验证码' }}
              </el-button>
            </el-col>
          </el-row>
          <!-- 显示验证码（开发环境） -->
          <div v-if="smsCaptchaCode" class="sms-captcha-tip">
            <el-alert 
              title="开发环境验证码" 
              type="info" 
              :closable="false"
              show-icon
            >
              <template #default>
                <strong>{{ smsCaptchaCode }}</strong>
                <span style="margin-left: 10px; color: #999; font-size: 12px;">（点击复制）</span>
              </template>
            </el-alert>
          </div>
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-footer">
        <p class="register-tip">
          还没有账号？
          <el-link type="primary" @click="showRegisterDialog">立即注册</el-link>
        </p>
        <p v-if="captchaType !== 'none'" class="captcha-tip">
          验证码类型：{{ captchaType === 'image' ? '图形验证码' : '手机验证码' }}
        </p>
        <p class="security-tip">
          <el-icon><Lock /></el-icon>
          注册后需要等待管理员审批
        </p>
      </div>
    </div>

    <!-- 注册对话框 -->
    <el-dialog v-model="registerDialogVisible" title="用户注册" width="600px">
      <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="registerForm.username" placeholder="请输入用户名（3-50 字符）" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="请输入密码（至少 6 位）" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请再次输入密码" show-password />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="registerForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="registerForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机" prop="phone">
          <el-input v-model="registerForm.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        
        <!-- 公司选择 -->
        <el-form-item label="公司" prop="companyId">
          <el-select 
            v-model="registerForm.companyId" 
            placeholder="请选择公司" 
            filterable
            style="width: 100%"
            @change="onCompanyChange"
          >
            <el-option
              v-for="company in companyList"
              :key="company.id"
              :label="company.companyName"
              :value="company.id"
            >
              <span>{{ company.companyName }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">{{ company.typeName }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        
        <!-- 角色选择 -->
        <el-form-item label="角色" prop="roleIds" v-if="companyList.length > 0">
          <el-select 
            v-model="registerForm.roleIds" 
            placeholder="请选择角色" 
            multiple
            filterable
            style="width: 100%"
            :disabled="!registerForm.companyId"
            @change="onRoleChange"
          >
            <el-option
              v-for="role in filteredRoleList"
              :key="role.id"
              :label="role.roleName"
              :value="role.id"
            />
          </el-select>
          <div class="form-tip" v-if="!registerForm.companyId">请先选择公司</div>
        </el-form-item>
        
        <!-- 作业区选择（仅当公司为甲方且角色包含作业区角色时显示） -->
        <el-form-item label="作业区" prop="workAreaIds" v-if="showWorkAreaSelect">
          <el-select 
            v-model="registerForm.workAreaIds" 
            placeholder="请选择作业区" 
            multiple
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="area in workAreaList"
              :key="area.id"
              :label="area.workAreaName"
              :value="area.id"
            />
          </el-select>
        </el-form-item>
        
      </el-form>
      <template #footer>
        <el-button @click="registerDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRegister" :loading="registering">注册</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { VideoPlay, Lock, User, Key } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref(null)
const registerFormRef = ref(null)
const loading = ref(false)
const registering = ref(false)
const registerDialogVisible = ref(false)

const captchaType = ref('none') // none, image, sms
const captchaImage = ref('')
const captchaId = ref('')
const smsCountdown = ref(0)
const smsCaptchaCode = ref('') // 显示验证码（开发环境）

// 公司列表
const companyList = ref([])
// 角色列表
const roleList = ref([])
// 作业区列表
const workAreaList = ref([])

const loginForm = reactive({
  username: '',
  password: '',
  captcha: '',
  captchaId: '',
  captchaType: ''
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少 6 位', trigger: 'blur' }
  ],
  captcha: [
    { 
      required: () => captchaType.value !== 'none', 
      message: '请输入验证码', 
      trigger: 'blur' 
    }
  ]
}

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  email: '',
  phone: '',
  companyId: null,
  roleIds: [],
  workAreaIds: []
})

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度必须在 3-50 之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  companyId: [
    { required: true, message: '请选择公司', trigger: 'change' }
  ],
  roleIds: [
    { required: true, message: '请至少选择一个角色', trigger: 'change', type: 'array' }
  ]
}

// 计算属性：是否显示作业区选择
// 当公司为甲方（typeId=1）且选择的角色包含作业区角色时显示
const showWorkAreaSelect = computed(() => {
  if (!registerForm.companyId) return false
  
  const company = companyList.value.find(c => c.id === registerForm.companyId)
  if (!company || company.typeId !== 1) return false
  
  // 检查是否选择了作业区相关的角色（角色名称包含"作业区"）
  const selectedRoles = roleList.value.filter(r => registerForm.roleIds.includes(r.id))
  const hasWorkAreaRole = selectedRoles.some(r => r.roleName && r.roleName.includes('作业区'))
  
  return hasWorkAreaRole
})

// 计算属性：过滤后的角色列表（根据选择的公司类型过滤）
const filteredRoleList = computed(() => {
  if (!registerForm.companyId) return []
  
  const company = companyList.value.find(c => c.id === registerForm.companyId)
  if (!company || !company.typeId) return roleList.value
  
  // 过滤出与公司类型匹配的角色
  return roleList.value.filter(role => role.companyTypeId === company.typeId)
})

// 加载公司列表（仅状态正常且允许匿名注册的公司）
const loadCompanies = async () => {
  try {
    const res = await request({
      url: '/company',
      method: 'get',
      params: { pageNum: 1, pageSize: 100, status: 'active', allowAnonymousRegister: true }
    })
    // 过滤掉系统保护的公司（如北京其点），确保只显示允许匿名注册的公司
    companyList.value = (res.data.records || []).filter(c => !c.isSystemProtected && c.allowAnonymousRegister === true)
  } catch (error) {
    console.error('加载公司列表失败:', error)
  }
}

// 加载角色列表
const loadRoles = async () => {
  try {
    const res = await request({
      url: '/role',
      method: 'get',
      params: { pageNum: 1, pageSize: 100 }
    })
    roleList.value = res.data.records || []
  } catch (error) {
    console.error('加载角色列表失败:', error)
  }
}

// 加载作业区列表
const loadWorkAreas = async (companyId) => {
  try {
    const res = await request({
      url: '/workarea',
      method: 'get',
      params: { pageNum: 1, pageSize: 100, companyId }
    })
    workAreaList.value = res.data.records || []
  } catch (error) {
    console.error('加载作业区列表失败:', error)
  }
}

// 公司改变时触发
const onCompanyChange = (companyId) => {
  // 清空选择
  registerForm.roleIds = []
  registerForm.workAreaIds = []
  workAreaList.value = []
  
  // 加载该公司的作业区（仅甲方公司）
  if (companyId) {
    const company = companyList.value.find(c => c.id === companyId)
    if (company && company.typeId === 1) {
      loadWorkAreas(companyId)
    }
  }
}

// 角色改变时触发
const onRoleChange = (roleIds) => {
  // 清空作业区选择
  registerForm.workAreaIds = []
  
  // 检查是否选择了作业区角色
  const company = companyList.value.find(c => c.id === registerForm.companyId)
  if (company && company.typeId === 1) {
    const selectedRoles = roleList.value.filter(r => roleIds.includes(r.id))
    const hasWorkAreaRole = selectedRoles.some(r => r.roleName && r.roleName.includes('作业区'))
    
    // 如果有作业区角色且作业区列表为空，加载作业区
    if (hasWorkAreaRole && workAreaList.value.length === 0) {
      loadWorkAreas(registerForm.companyId)
    }
  }
}

// 显示注册对话框
const showRegisterDialog = async () => {
  registerDialogVisible.value = true
  
  // 加载公司列表（仅状态正常且允许匿名注册的公司）
  if (companyList.value.length === 0) {
    await loadCompanies()
  }
  
  // 加载角色列表（不需要登录）
  if (roleList.value.length === 0) {
    try {
      const res = await request({
        url: '/role',
        method: 'get',
        params: { pageNum: 1, pageSize: 100 }
      })
      roleList.value = res.data.records || []
    } catch (error) {
      console.error('加载角色列表失败:', error)
    }
  }
}

// 处理注册
const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      if (registerForm.password !== registerForm.confirmPassword) {
        ElMessage.error('两次输入的密码不一致')
        return
      }
      
      registering.value = true
      try {
        await request({
          url: '/user/register',
          method: 'post',
          data: {
            username: registerForm.username,
            password: registerForm.password,
            confirmPassword: registerForm.confirmPassword,
            realName: registerForm.realName,
            email: registerForm.email,
            phone: registerForm.phone,
            companyId: registerForm.companyId,
            roleIds: registerForm.roleIds || [],
            workAreaIds: registerForm.workAreaIds || []
          }
        })
        
        ElMessage.success('注册成功，请等待管理员审批')
        registerDialogVisible.value = false
        
        // 自动填充登录表单
        loginForm.username = registerForm.username
        
        // 清空注册表单
        Object.assign(registerForm, {
          username: '',
          password: '',
          confirmPassword: '',
          realName: '',
          email: '',
          phone: '',
          companyId: null,
          roleIds: [],
          workAreaIds: []
        })
      } catch (error) {
        ElMessage.error('注册失败：' + error.message)
      } finally {
        registering.value = false
      }
    }
  })
}

// 加载验证码配置
const loadCaptchaConfig = async () => {
  try {
    const res = await request({
      url: '/auth/captcha/config',
      method: 'get'
    })
    captchaType.value = res.data.captchaType || 'none'
    
    if (captchaType.value === 'image') {
      await refreshCaptcha()
    }
  } catch (error) {
    console.error('加载验证码配置失败:', error)
  }
}

// 刷新图形验证码
const refreshCaptcha = async () => {
  try {
    const res = await request({
      url: '/auth/captcha',
      method: 'get'
    })
    
    // 使用 base64 显示验证码图片
    captchaImage.value = res.data.imageBase64
    captchaId.value = res.data.captchaId
    loginForm.captchaId = res.data.captchaId
    loginForm.captchaType = 'image'
    
    console.log('刷新验证码成功:', captchaId.value)
  } catch (error) {
    console.error('刷新验证码失败:', error)
  }
}

// 发送短信验证码
const sendSmsCode = async () => {
  try {
    // 开发环境：模拟发送成功，直接显示验证码
    if (process.env.NODE_ENV === 'development') {
      smsCaptchaCode.value = '123456'
      ElMessage.success('验证码已生成（开发环境）')
      console.log('【开发环境】短信验证码：123456')
      startCountdown()
      return
    }
    
    // 生产环境：调用真实接口
    await request({
      url: '/auth/captcha/sms',
      method: 'post',
      data: { phone: '13800138000' }
    })
    
    ElMessage.success('验证码已发送')
    startCountdown()
  } catch (error) {
    ElMessage.error('发送失败：' + error.message)
  }
}

// 开始倒计时
const startCountdown = () => {
  smsCountdown.value = 60
  const timer = setInterval(() => {
    smsCountdown.value--
    if (smsCountdown.value <= 0) {
      clearInterval(timer)
      smsCaptchaCode.value = '' // 清除验证码
    }
  }, 1000)
}

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await request({
          url: '/auth/login',
          method: 'post',
          data: {
            username: loginForm.username,
            password: loginForm.password,
            captcha: loginForm.captcha,
            captchaId: loginForm.captchaId,
            captchaType: loginForm.captchaType || captchaType.value
          }
        })
        
        // 保存 token
        localStorage.setItem('accessToken', res.data.accessToken)
        localStorage.setItem('refreshToken', res.data.refreshToken)
        
        // 保存用户信息
        userStore.token = res.data.accessToken
        userStore.refreshToken = res.data.refreshToken
        userStore.userInfo = res.data.userInfo
        localStorage.setItem("userInfo", JSON.stringify(res.data.userInfo))
        
        ElMessage.success('登录成功')
        
        // 跳转到首页
        router.push('/')
      } catch (error) {
        console.error('登录失败:', error)
        
        // 验证码错误时刷新验证码
        if (error.message && error.message.includes('验证码')) {
          loginForm.captcha = ''
          if (captchaType.value === 'image') {
            await refreshCaptcha()
          }
        }
      } finally {
        loading.value = false
      }
    }
  })
}

onMounted(() => {
  loadCaptchaConfig()
})
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  background: white;
  border-radius: 16px;
  padding: 40px;
  width: 100%;
  max-width: 420px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  margin-bottom: 16px;
  color: white;
}

.login-header h1 {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px 0;
}

.login-header p {
  color: #999;
  font-size: 14px;
  margin: 0;
}

.login-form {
  margin-bottom: 20px;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.login-btn:hover {
  opacity: 0.9;
}

.login-footer {
  text-align: center;
  margin-top: 20px;
}

.register-tip,
.captcha-tip,
.security-tip {
  margin: 8px 0;
  font-size: 14px;
  color: #666;
}

.security-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.captcha-image {
  width: 100%;
  height: 40px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
}

.sms-captcha-tip {
  margin-top: 10px;
  
  :deep(.el-alert) {
    padding: 8px 12px;
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    }
  }
}

.captcha-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

:deep(.el-dialog__body) {
  max-height: 70vh;
  overflow-y: auto;
}
</style>
