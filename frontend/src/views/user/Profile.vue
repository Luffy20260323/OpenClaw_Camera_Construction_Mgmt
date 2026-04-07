<template>
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>个人中心</span>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <!-- 基本信息 -->
        <el-tab-pane label="基本信息" name="basic">
          <el-form :model="profile" :rules="rules" ref="profileFormRef" label-width="100px" style="max-width: 500px">
            <el-form-item label="用户名">
              <el-input v-model="profile.username" disabled />
            </el-form-item>

            <el-form-item label="姓名" prop="realName">
              <el-input v-model="profile.realName" placeholder="请输入姓名" />
            </el-form-item>

            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="profile.gender">
                <el-radio :label="0">未知</el-radio>
                <el-radio :label="1">男</el-radio>
                <el-radio :label="2">女</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="邮箱" prop="email">
              <el-input v-model="profile.email" placeholder="请输入邮箱" />
            </el-form-item>

            <el-form-item label="手机号" prop="phone">
              <el-input v-model="profile.phone" placeholder="请输入手机号" maxlength="11" />
            </el-form-item>

            <el-form-item label="公司名称">
              <el-input v-model="profile.companyName" disabled />
            </el-form-item>

            <el-form-item label="公司类型">
              <el-input v-model="profile.companyType" disabled />
            </el-form-item>

            <!-- 作业区信息（仅甲方公司显示） -->
            <el-form-item label="作业区" v-if="profile.companyTypeId === 1">
              <div v-if="profile.workAreas && profile.workAreas.length > 0">
                <el-tag 
                  v-for="area in profile.workAreas" 
                  :key="area.id" 
                  :type="area.isPrimary ? 'primary' : 'info'"
                  size="small" 
                  style="margin-right: 8px; margin-bottom: 8px;"
                >
                  {{ area.workAreaName }} {{ area.isPrimary ? '(主要)' : '' }}
                </el-tag>
              </div>
              <el-tag v-else type="info" size="small">空</el-tag>
            </el-form-item>

            <el-form-item label="角色">
              <el-tag v-for="role in profile.roles" :key="role" size="small" style="margin-right: 8px;">
                {{ role }}
              </el-tag>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="updateProfile" :loading="saving">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 修改密码 -->
        <el-tab-pane label="修改密码" name="password">
          <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px" style="max-width: 500px">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" show-password />
            </el-form-item>

            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
              <div class="form-tip">密码长度 6-20 位，建议包含大小写字母、数字和特殊字符</div>
            </el-form-item>

            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="changePassword" :loading="changingPassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const activeTab = ref('basic')
const saving = ref(false)
const changingPassword = ref(false)

const profileFormRef = ref(null)
const passwordFormRef = ref(null)

const profile = reactive({
  userId: 0,
  username: '',
  realName: '',
  gender: 0,
  email: '',
  phone: '',
  companyName: '',
  roles: []
})

const rules = {
  realName: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度必须在 6-20 位之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 获取用户信息
const getProfile = async () => {
  try {
    const res = await request({
      url: '/user/profile',
      method: 'get'
    })
    Object.assign(profile, res.data)
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  }
}

// 更新用户信息
const updateProfile = async () => {
  if (!profileFormRef.value) return
  
  await profileFormRef.value.validate(async (valid) => {
    if (valid) {
      saving.value = true
      try {
        await request({
          url: '/user/profile',
          method: 'put',
          data: {
            realName: profile.realName,
            gender: profile.gender,
            email: profile.email,
            phone: profile.phone
          }
        })
        ElMessage.success('保存成功')
      } catch (error) {
        console.error('保存失败:', error)
      } finally {
        saving.value = false
      }
    }
  })
}

// 修改密码
const changePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      changingPassword.value = true
      try {
        await request({
          url: '/user/password',
          method: 'put',
          data: {
            oldPassword: passwordForm.oldPassword,
            newPassword: passwordForm.newPassword,
            confirmPassword: passwordForm.confirmPassword
          }
        })
        ElMessage.success('密码修改成功，请重新登录')
        
        // 清空表单
        passwordForm.oldPassword = ''
        passwordForm.newPassword = ''
        passwordForm.confirmPassword = ''
        
        // 退出登录
        setTimeout(() => {
          localStorage.removeItem('accessToken')
          localStorage.removeItem('refreshToken')
          localStorage.removeItem('userInfo')
          window.location.href = '/login'
        }, 1500)
      } catch (error) {
        console.error('修改密码失败:', error)
        ElMessage.error(error.response?.data?.message || error.message || '修改密码失败')
      } finally {
        changingPassword.value = false
      }
    }
  })
}

onMounted(() => {
  getProfile()
})
</script>

<style scoped lang="scss">
.profile-container {
  padding: 20px;

  .box-card {
    max-width: 800px;
    margin: 0 auto;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .form-tip {
    font-size: 12px;
    color: #999;
    margin-top: 4px;
  }
}

.footer {
  background: #fff;
  border-top: 1px solid #e4e7ed;
  padding: 12px 24px;
  text-align: center;
  color: #909399;
  font-size: 13px;
}

.footer p {
  margin: 0;
}
</style>
