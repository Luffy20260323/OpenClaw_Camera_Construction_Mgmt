<template>
  <div class="resource-selector">
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
        v-for="option in options"
        :key="option.id"
        :label="option.name"
        :value="option.id"
        :disabled="option.disabled"
      >
        <div class="option-content">
          <span class="option-name">{{ option.name }}</span>
          <span class="option-code">{{ option.code }}</span>
          <el-tag v-if="option.type" size="small" :type="getTypeTag(option.type)">
            {{ getTypeLabel(option.type) }}
          </el-tag>
        </div>
      </el-option>
    </el-select>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: [Number, String, Array, null],
    default: null
  },
  // 资源列表
  resources: {
    type: Array,
    default: () => []
  },
  // 占位符
  placeholder: {
    type: String,
    default: '请选择资源'
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
  },
  // 资源类型过滤
  resourceType: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const selectedValue = ref(props.modelValue)

watch(() => props.modelValue, (newVal) => {
  if (newVal !== selectedValue.value) {
    selectedValue.value = newVal
  }
})

// 过滤后的选项
const options = computed(() => {
  if (!props.resourceType) {
    return props.resources
  }
  return props.resources.filter(r => r.type === props.resourceType)
})

// 获取类型标签
const getTypeLabel = (type) => {
  const labelMap = {
    MODULE: '模块',
    MENU: '菜单',
    PAGE: '页面',
    ELEMENT: '元素',
    API: 'API',
    PERMISSION: '权限'
  }
  return labelMap[type] || type
}

// 获取类型标签颜色
const getTypeTag = (type) => {
  const tagMap = {
    MODULE: 'warning',
    MENU: 'success',
    PAGE: 'primary',
    ELEMENT: 'info',
    API: 'danger',
    PERMISSION: 'warning'
  }
  return tagMap[type] || 'info'
}

const handleChange = (value) => {
  emit('update:modelValue', value)
  emit('change', value)
}
</script>

<style scoped lang="scss">
.resource-selector {
  width: 100%;
  
  .option-content {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .option-name {
      flex: 1;
      font-weight: 500;
    }
    
    .option-code {
      font-size: 12px;
      color: #909399;
      font-family: monospace;
    }
  }
}
</style>
