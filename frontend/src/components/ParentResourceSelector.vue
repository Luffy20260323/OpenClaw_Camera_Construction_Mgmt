<template>
  <div class="parent-resource-selector">
    <el-select
      v-model="selectedParentId"
      filterable
      clearable
      :placeholder="placeholder"
      :disabled="disabled"
      style="width: 100%"
      @change="handleParentChange"
    >
      <el-option
        v-for="option in filteredOptions"
        :key="option.id"
        :label="option.label"
        :value="option.id"
        :disabled="option.disabled"
      >
        <div class="option-content">
          <span :class="['option-label', option.disabled ? 'disabled' : '']">
            {{ option.prefix }}{{ option.label }}
          </span>
          <el-tag v-if="option.type" size="small" :type="getTypeTag(option.type)">
            {{ getTypeLabel(option.type) }}
          </el-tag>
          <el-icon v-if="option.id === currentResourceId" class="current-tag"><Location /></el-icon>
        </div>
      </el-option>
    </el-select>
    
    <!-- 循环引用警告 -->
    <el-alert
      v-if="showCycleWarning"
      type="warning"
      :title="cycleWarningMessage"
      :closable="false"
      show-icon
      class="cycle-warning"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  // 当前选中的父资源 ID
  modelValue: {
    type: [Number, String, null],
    default: null
  },
  // 当前资源 ID（用于循环引用检查）
  currentResourceId: {
    type: [Number, String],
    required: true
  },
  // 资源树形数据
  resourceTree: {
    type: Array,
    default: () => []
  },
  // 占位符
  placeholder: {
    type: String,
    default: '请选择父资源'
  },
  // 是否禁用
  disabled: {
    type: Boolean,
    default: false
  },
  // 是否允许选择自己
  allowSelf: {
    type: Boolean,
    default: false
  },
  // 最大层级深度
  maxDepth: {
    type: Number,
    default: 10
  }
})

const emit = defineEmits(['update:modelValue', 'change', 'cycle-detected'])

const selectedParentId = ref(props.modelValue)

// 监听外部值变化
watch(() => props.modelValue, (newVal) => {
  if (newVal !== selectedParentId.value) {
    selectedParentId.value = newVal
  }
})

// 扁平化资源树为选项列表
const flattenTree = (tree, prefix = '', depth = 0) => {
  const options = []
  
  for (const node of tree) {
    // 检查是否超过最大深度
    if (depth > props.maxDepth) {
      continue
    }
    
    const option = {
      id: node.id,
      label: node.name,
      type: node.type,
      prefix: prefix,
      disabled: false
    }
    
    // 循环引用检查：当前资源不能选择自己或其子资源作为父资源
    if (!props.allowSelf && node.id === props.currentResourceId) {
      option.disabled = true
    }
    
    options.push(option)
    
    // 递归处理子节点
    if (node.children && node.children.length > 0) {
      const childOptions = flattenTree(
        node.children,
        prefix + '├─ ',
        depth + 1
      )
      options.push(...childOptions)
    }
  }
  
  return options
}

// 计算过滤后的选项
const filteredOptions = computed(() => {
  const options = flattenTree(props.resourceTree)
  
  // 额外检查：如果当前资源已经有子资源，需要标记所有子资源为禁用
  if (props.currentResourceId) {
    const descendantIds = getDescendantIds(props.currentResourceId)
    options.forEach(option => {
      if (descendantIds.includes(option.id)) {
        option.disabled = true
      }
    })
  }
  
  return options
})

// 获取所有子资源 ID（用于循环引用检查）
const getDescendantIds = (resourceId, tree = props.resourceTree) => {
  const ids = []
  
  const findNode = (nodes) => {
    for (const node of nodes) {
      if (node.id === resourceId) {
        // 找到目标节点，收集所有子节点 ID
        collectChildrenIds(node.children, ids)
        return true
      }
      if (node.children && node.children.length > 0) {
        if (findNode(node.children)) {
          return true
        }
      }
    }
    return false
  }
  
  const collectChildrenIds = (children, idList) => {
    if (!children) return
    for (const child of children) {
      idList.push(child.id)
      if (child.children && child.children.length > 0) {
        collectChildrenIds(child.children, idList)
      }
    }
  }
  
  findNode(tree)
  return ids
}

// 检测循环引用
const checkCycle = (parentId) => {
  if (!parentId) {
    return { hasCycle: false, path: [] }
  }
  
  // 构建从当前节点到根节点的路径
  const path = [props.currentResourceId]
  let currentId = parentId
  
  while (currentId) {
    // 如果路径中已经包含当前 ID，说明有循环
    if (path.includes(currentId)) {
      path.push(currentId)
      return { hasCycle: true, path }
    }
    
    path.push(currentId)
    currentId = getParentId(currentId)
  }
  
  return { hasCycle: false, path }
}

// 获取父资源 ID
const getParentId = (resourceId, tree = props.resourceTree, parentId = null) => {
  for (const node of tree) {
    if (node.id === resourceId) {
      return parentId
    }
    if (node.children && node.children.length > 0) {
      const found = getParentId(resourceId, node.children, node.id)
      if (found !== null) {
        return found
      }
    }
  }
  return null
}

// 循环引用警告
const showCycleWarning = computed(() => {
  if (!selectedParentId.value) return false
  const { hasCycle } = checkCycle(selectedParentId.value)
  return hasCycle
})

const cycleWarningMessage = computed(() => {
  const { hasCycle, path } = checkCycle(selectedParentId.value)
  if (!hasCycle) return ''
  
  // 找到循环点
  const cycleStart = path[path.length - 1]
  const cyclePath = path.slice(path.indexOf(cycleStart)).join(' → ')
  
  return `检测到循环引用：${cyclePath}`
})

// 处理父资源变更
const handleParentChange = (value) => {
  // 再次检查循环引用
  if (value) {
    const { hasCycle, path } = checkCycle(value)
    
    if (hasCycle) {
      emit('cycle-detected', {
        hasCycle: true,
        path,
        message: cycleWarningMessage.value
      })
      
      // 阻止选择，清空值
      selectedParentId.value = null
      return
    }
  }
  
  emit('update:modelValue', value)
  emit('change', value)
}

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

// 暴露方法
const clearSelection = () => {
  selectedParentId.value = null
}

const validateSelection = () => {
  const { hasCycle } = checkCycle(selectedParentId.value)
  return !hasCycle
}

defineExpose({
  clearSelection,
  validateSelection
})
</script>

<style scoped lang="scss">
.parent-resource-selector {
  width: 100%;
  
  .option-content {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .option-label {
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      
      &.disabled {
        color: #c0c4cc;
      }
    }
    
    .current-tag {
      color: #409eff;
      font-size: 14px;
    }
  }
  
  .cycle-warning {
    margin-top: 8px;
    font-size: 13px;
  }
}
</style>
