<template>
  <div class="role-selector">
    <el-select
      v-model="selectedValue"
      filterable
      clearable
      :placeholder="placeholder"
      :disabled="disabled"
      :multiple="multiple"
      :collapse-tags="multiple"
      :collapse-tags-tooltip="true"
      style="width: 100%"
      @change="handleChange"
    >
      <el-option
        v-for="role in roles"
        :key="role.id"
        :label="role.roleName"
        :value="role.id"
        :disabled="role.disabled"
      >
        <div class="option-content">
          <span class="role-name">{{ role.roleName }}</span>
          <span class="role-code">{{ role.roleCode }}</span>
          <el-tag v-if="role.companyTypeName" :type="getCompanyTypeTag(role.companyTypeId)">
            {{ role.companyTypeName }}
          </el-tag>
          <el-tag v-if="role.isSystemProtected" type="warning" effect="plain">
            🔒
          </el-tag>
        </div>
      </el-option>
    </el-select>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: [Number, String, Array, null],
    default: null
  },
  // 角色列表
  roles: {
    type: Array,
    default: () => []
  },
  // 占位符
  placeholder: {
    type: String,
    default: '请选择角色'
  },
  // 是否禁用
  disabled: {
    type: Boolean,
    default: false
  },
  // 是否多选
  multiple: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const selectedValue = ref(props.modelValue)

watch(() => props.modelValue, (newVal) => {
  if (newVal !== selectedValue.value) {
    selectedValue.value = newVal
  }
})

// 获取公司类型标签颜色
const getCompanyTypeTag = (typeId) => {
  const tagMap = {
    1: 'primary',    // 总包单位
    2: 'success',    // 分包单位
    3: 'warning',    // 监理单位
    4: 'info'        // 建设单位
  }
  return tagMap[typeId] || 'info'
}

const handleChange = (value) => {
  emit('update:modelValue', value)
  emit('change', value)
}
</script>

<style scoped lang="scss">
.role-selector {
  width: 100%;
  
  .option-content {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .role-name {
      flex: 1;
      font-weight: 500;
    }
    
    .role-code {
      font-size: 12px;
      color: #909399;
      font-family: monospace;
    }
  }
}
</style>
