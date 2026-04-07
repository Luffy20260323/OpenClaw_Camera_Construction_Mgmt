<template>
    <div class="system-config">
      <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>系统配置管理</span>
          <el-button type="primary" @click="refreshConfigs">刷新</el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <!-- 侧边栏配置 -->
        <el-tab-pane label="侧边栏配置" name="sidebar">
          <el-form :model="sidebarConfig" label-width="200px" style="max-width: 600px">
            <el-form-item label="侧边栏位置">
              <el-select v-model="sidebarConfig.position" placeholder="请选择位置">
                <el-option label="左边" value="LEFT" />
                <el-option label="右边" value="RIGHT" />
              </el-select>
            </el-form-item>

            <el-form-item label="显示模式">
              <el-select v-model="sidebarConfig.mode" placeholder="请选择模式">
                <el-option label="一直显示" value="FIXED" />
                <el-option label="可隐藏" value="COLLAPSIBLE" />
              </el-select>
              <div class="form-tip">FIXED：侧边栏固定显示，不可折叠；COLLAPSIBLE：可通过按钮折叠/展开侧边栏</div>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="saveSidebarConfig" :loading="saving">保存配置</el-button>
              <el-button @click="loadSidebarConfigFromBackend">从服务器加载</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

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
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const activeTab = ref('sidebar')
const saving = ref(false)

const sidebarConfig = reactive({
  position: 'LEFT',
  mode: 'FIXED'
})

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

// 获取侧边栏配置（从 localStorage）
const loadSidebarConfig = () => {
  const savedConfig = localStorage.getItem('sidebarConfig')
  if (savedConfig) {
    try {
      const config = JSON.parse(savedConfig)
      sidebarConfig.position = config.position || 'LEFT'
      sidebarConfig.mode = config.mode || 'FIXED'
    } catch (e) {
      console.error('解析侧边栏配置失败:', e)
    }
  }
}

// 从后端加载侧边栏配置
const loadSidebarConfigFromBackend = async () => {
  try {
    const res = await request({
      url: '/system/config/sidebar',
      method: 'get'
    })
    if (res.data) {
      sidebarConfig.position = res.data.sidebarPosition || 'LEFT'
      sidebarConfig.mode = res.data.sidebarMode || 'FIXED'
      ElMessage.success('已从服务器加载配置')
    }
  } catch (error) {
    console.error('获取侧边栏配置失败:', error)
    ElMessage.warning('从服务器加载失败，使用本地配置')
  }
}

// 保存侧边栏配置
const saveSidebarConfig = async () => {
  saving.value = true
  try {
    // 保存到 localStorage
    localStorage.setItem('sidebarConfig', JSON.stringify({
      position: sidebarConfig.position,
      mode: sidebarConfig.mode
    }))

    // 同时保存到后端
    await request({
      url: '/system/config/sidebar/position',
      method: 'put',
      data: { position: sidebarConfig.position }
    })

    await request({
      url: '/system/config/sidebar/mode',
      method: 'put',
      data: { mode: sidebarConfig.mode }
    })

    ElMessage.success('侧边栏配置保存成功，刷新页面后生效')
  } catch (error) {
    console.error('保存侧边栏配置失败:', error)
    ElMessage.error('保存配置失败：' + error.message)
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  getCaptchaConfig()
  getAllConfigs()
  loadSidebarConfig()
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

  .form-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
    line-height: 1.5;
  }
}
</style>
