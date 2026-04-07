<template>
  <div class="data-scope-selector">
    <el-radio-group v-model="selectedScope" :disabled="disabled" @change="handleScopeChange">
      <el-radio
        v-for="scope in scopeOptions"
        :key="scope.value"
        :label="scope.value"
        border
        class="scope-radio"
      >
        <div class="scope-content">
          <div class="scope-title">{{ scope.label }}</div>
          <div class="scope-desc">{{ scope.description }}</div>
        </div>
      </el-radio>
    </el-radio-group>
    
    <!-- 部门选择器（当选择本部门或本部门及下级时显示） -->
    <div v-if="showDeptSelector" class="dept-selector">
      <el-divider>指定部门</el-divider>
      
      <el-select
        v-if="scopeType === 'DEPT'"
        v-model="selectedDeptId"
        filterable
        placeholder="请选择部门"
        style="width: 100%"
        @change="handleDeptChange"
      >
        <el-option
          v-for="dept in deptOptions"
          :key="dept.id"
          :label="dept.name"
          :value="dept.id"
        />
      </el-select>
      
      <el-tree-select
        v-if="scopeType === 'DEPT_AND_SUB'"
        v-model="selectedDeptIds"
        :data="deptTree"
        :props="deptProps"
        multiple
        check-strictly
        placeholder="请选择部门（可多选）"
        style="width: 100%"
        @change="handleDeptChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  // 数据范围类型
  scopeType: {
    type: String,
    default: 'SELF'
  },
  // 部门 ID（单个）
  deptId: {
    type: [Number, String, null],
    default: null
  },
  // 部门 ID 列表（多个）
  deptIds: {
    type: Array,
    default: () => []
  },
  // 部门选项
  deptOptions: {
    type: Array,
    default: () => []
  },
  // 部门树
  deptTree: {
    type: Array,
    default: () => []
  },
  // 是否禁用
  disabled: {
    type: Boolean,
    default: false
  },
  // 自定义范围选项
  customScopeOptions: {
    type: Array,
    default: null
  }
})

const emit = defineEmits(['update:scopeType', 'update:deptId', 'update:deptIds', 'change'])

const selectedScope = ref(props.scopeType)
const selectedDeptId = ref(props.deptId)
const selectedDeptIds = ref(props.deptIds || [])

watch(() => props.scopeType, (newVal) => {
  selectedScope.value = newVal
})

watch(() => props.deptId, (newVal) => {
  selectedDeptId.value = newVal
})

watch(() => props.deptIds, (newVal) => {
  selectedDeptIds.value = newVal || []
})

// 默认范围选项
const defaultScopeOptions = [
  {
    value: 'SELF',
    label: '仅本人数据',
    description: '只能查看/操作自己创建的数据'
  },
  {
    value: 'DEPT',
    label: '本部门数据',
    description: '可查看/操作本部门的数据'
  },
  {
    value: 'DEPT_AND_SUB',
    label: '本部门及下级',
    description: '可查看/操作本部门及下级部门的数据'
  },
  {
    value: 'ALL',
    label: '全部数据',
    description: '可查看/操作所有数据'
  }
]

const scopeOptions = computed(() => {
  return props.customScopeOptions || defaultScopeOptions
})

// 是否显示部门选择器
const showDeptSelector = computed(() => {
  return ['DEPT', 'DEPT_AND_SUB'].includes(selectedScope.value)
})

// 部门树配置
const deptProps = {
  value: 'id',
  label: 'name',
  children: 'children'
}

// 处理范围变更
const handleScopeChange = (value) => {
  emit('update:scopeType', value)
  
  // 清空部门选择
  if (value === 'SELF' || value === 'ALL') {
    selectedDeptId.value = null
    selectedDeptIds.value = []
    emit('update:deptId', null)
    emit('update:deptIds', [])
  }
  
  emit('change', {
    scopeType: value,
    deptId: selectedDeptId.value,
    deptIds: selectedDeptIds.value
  })
}

// 处理部门变更
const handleDeptChange = () => {
  if (selectedScope.value === 'DEPT') {
    emit('update:deptId', selectedDeptId.value)
  } else if (selectedScope.value === 'DEPT_AND_SUB') {
    emit('update:deptIds', selectedDeptIds.value)
  }
  
  emit('change', {
    scopeType: selectedScope.value,
    deptId: selectedDeptId.value,
    deptIds: selectedDeptIds.value
  })
}
</script>

<style scoped lang="scss">
.data-scope-selector {
  width: 100%;
  
  .scope-radio {
    display: block;
    height: auto;
    margin-bottom: 12px;
    padding: 12px;
    
    :deep(.el-radio__label) {
      width: 100%;
    }
  }
  
  .scope-content {
    display: flex;
    flex-direction: column;
    gap: 4px;
    
    .scope-title {
      font-weight: 600;
      font-size: 14px;
      color: #303133;
    }
    
    .scope-desc {
      font-size: 12px;
      color: #909399;
    }
  }
  
  .dept-selector {
    margin-top: 16px;
    padding: 16px;
    background: #f5f7fa;
    border-radius: 4px;
    
    :deep(.el-divider__text) {
      font-weight: 600;
      font-size: 14px;
    }
  }
}
</style>
