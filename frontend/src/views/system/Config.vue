<template>
  <AdminLayout>
    <div class="system-config">
      <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>系统配置管理</span>
          <el-button type="primary" @click="refreshConfigs">刷新</el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <!-- 验证码配置 -->
        <el-tab-pane label="验证码配置" name="captcha">
          <el-form :model="captchaConfig" label-width="200px" style="max-width: 600px">
            <el-form-item label="验证码类型">
              <el-select v-model="captchaConfig.captchaType" placeholder="请选择">
                <el-option label="不用验证码" value="none" />
                <el-option label="图形验证码" value="image" />
                <el-option label="手机验证码" value="sms" />
              </el-select>
            </el-form-item>

            <el-form-item label="图形验证码长度">
              <el-input-number v-model="captchaConfig.captchaLength" :min="4" :max="8" />
            </el-form-item>

            <el-form-item label="验证码过期时间（分钟）">
              <el-input-number v-model="captchaConfig.captchaExpireMinutes" :min="1" :max="30" />
            </el-form-item>

            <el-form-item label="短信发送间隔（秒）">
              <el-input-number v-model="captchaConfig.smsIntervalSeconds" :min="30" :max="300" />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="saveCaptchaConfig" :loading="saving">保存配置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 所有配置项 -->
        <el-tab-pane label="全部配置" name="all">
          <el-table :data="allConfigs" style="width: 100%" border>
            <el-table-column prop="configKey" label="配置键" width="200" />
            <el-table-column prop="configValue" label="配置值" />
            <el-table-column prop="configType" label="类型" width="100" />
            <el-table-column prop="description" label="描述" />
            <el-table-column label="操作" width="150">
              <template #default="scope">
                <el-button
                  size="small"
                  @click="editConfig(scope.row)"
                >
                  编辑
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
  </AdminLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import AdminLayout from '@/layouts/AdminLayout.vue'

const activeTab = ref('captcha')
const saving = ref(false)

const captchaConfig = reactive({
  captchaType: 'none',
  captchaLength: 4,
  captchaExpireMinutes: 5,
  smsIntervalSeconds: 60
})

const allConfigs = ref([])

// 获取验证码配置
const getCaptchaConfig = async () => {
  try {
    const res = await request({
      url: '/system/config/captcha/all',
      method: 'get'
    })
    Object.assign(captchaConfig, res.data)
  } catch (error) {
    console.error('获取验证码配置失败:', error)
  }
}

// 获取所有配置
const getAllConfigs = async () => {
  try {
    const res = await request({
      url: '/system/config',
      method: 'get'
    })
    allConfigs.value = res.data
  } catch (error) {
    console.error('获取所有配置失败:', error)
  }
}

// 保存验证码配置
const saveCaptchaConfig = async () => {
  saving.value = true
  try {
    await request({
      url: '/system/config/captcha-type',
      method: 'put',
      data: {
        configValue: captchaConfig.captchaType,
        description: '验证码类型：none-不用验证码，image-图形验证码，sms-手机验证码'
      }
    })

    await request({
      url: '/system/config/captcha-length',
      method: 'put',
      data: {
        configValue: String(captchaConfig.captchaLength),
        description: '图形验证码长度'
      }
    })

    await request({
      url: '/system/config/captcha-expire-minutes',
      method: 'put',
      data: {
        configValue: String(captchaConfig.captchaExpireMinutes),
        description: '验证码过期时间（分钟）'
      }
    })

    await request({
      url: '/system/config/sms-interval-seconds',
      method: 'put',
      data: {
        configValue: String(captchaConfig.smsIntervalSeconds),
        description: '短信验证码发送间隔（秒）'
      }
    })

    ElMessage.success('配置保存成功')
    getCaptchaConfig()
    getAllConfigs()
  } catch (error) {
    console.error('保存配置失败:', error)
    ElMessage.error('保存配置失败：' + error.message)
  } finally {
    saving.value = false
  }
}

// 编辑配置
const editConfig = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入新的配置值', '编辑配置', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: row.configValue
    })

    await request({
      url: `/system/config/${row.configKey}`,
      method: 'put',
      data: {
        configValue: value,
        description: row.description
      }
    })

    ElMessage.success('配置更新成功')
    getAllConfigs()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('更新配置失败:', error)
      ElMessage.error('更新配置失败：' + error.message)
    }
  }
}

const refreshConfigs = () => {
  getCaptchaConfig()
  getAllConfigs()
  ElMessage.success('配置已刷新')
}

onMounted(() => {
  getCaptchaConfig()
  getAllConfigs()
})
</script>

<style scoped lang="scss">
.system-config {
  padding: 20px;

  .box-card {
    max-width: 1200px;
    margin: 0 auto;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>
