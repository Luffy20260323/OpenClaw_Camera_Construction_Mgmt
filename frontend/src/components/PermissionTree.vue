<template>
  <div class="permission-tree">
    <el-tree
      ref="treeRef"
      :data="treeData"
      :props="props"
      :default-checked-keys="checkedKeys"
      :default-expanded-keys="expandedKeys"
      :expand-on-click-node="false"
      show-checkbox
      node-key="id"
      :load="loadNode"
      lazy
      @check="handleCheck"
      @node-expand="handleNodeExpand"
    >
      <template #default="{ node, data }">
        <span class="tree-node">
          <el-icon :class="['node-icon', getTypeIcon(data.type)]">
            <component :is="getTypeIconComponent(data.type)" />
          </el-icon>
          <span class="node-label">{{ node.label }}</span>
          <span v-if="data.code" class="node-code">{{ data.code }}</span>
          <el-tag v-if="data.type" size="small" :type="getTypeTag(data.type)" class="node-type">
            {{ getTypeLabel(data.type) }}
          </el-tag>
        </span>
      </template>
    </el-tree>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  // 树形数据
  treeData: {
    type: Array,
    default: () => []
  },
  // 已选中的权限 ID 列表
  checkedKeys: {
    type: Array,
    default: () => []
  },
  // 默认展开的节点 ID
  expandedKeys: {
    type: Array,
    default: () => []
  },
  // 是否只读
  readonly: {
    type: Boolean,
    default: false
  },
  // 是否显示类型标签
  showTypeTag: {
    type: Boolean,
    default: true
  },
  // 是否显示编码
  showCode: {
    type: Boolean,
    default: true
  },
  // 是否禁用复选框
  checkStrictly: {
    type: Boolean,
    default: false
  },
  // 自定义节点内容
  renderContent: {
    type: Function,
    default: null
  }
})

const emit = defineEmits(['check', 'node-click', 'update:checkedKeys'])

const treeRef = ref(null)

// 树配置
const treeProps = {
  children: 'children',
  label: 'name',
  isLeaf: (data) => !data.children || data.children.length === 0
}

// 获取类型图标
const getTypeIcon = (type) => {
  const iconMap = {
    MODULE: 'folder',
    MENU: 'menu',
    PAGE: 'document',
    ELEMENT: 'component',
    API: 'link',
    PERMISSION: 'lock'
  }
  return iconMap[type] || 'document'
}

// 获取类型图标组件
const getTypeIconComponent = (type) => {
  const componentMap = {
    MODULE: 'Folder',
    MENU: 'Menu',
    PAGE: 'Document',
    ELEMENT: 'Component',
    API: 'Link',
    PERMISSION: 'Lock'
  }
  return componentMap[type] || 'Document'
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

// 懒加载节点
const loadNode = (node, resolve) => {
  if (node.level === 0) {
    return resolve(props.treeData)
  }
  if (node.data.children) {
    return resolve(node.data.children)
  }
  resolve([])
}

// 处理节点选中
const handleCheck = (checkedKeys) => {
  emit('check', checkedKeys)
  emit('update:checkedKeys', checkedKeys.checkedKeys)
}

// 处理节点展开
const handleNodeExpand = (data) => {
  emit('node-expand', data)
}

// 处理节点点击
const handleNodeClick = (data) => {
  emit('node-click', data)
}

// 暴露方法
const getCheckedKeys = () => {
  return treeRef.value?.getCheckedKeys() || { checkedKeys: [], halfCheckedKeys: [] }
}

const getCheckedNodes = () => {
  return treeRef.value?.getCheckedNodes() || []
}

const setCheckedKeys = (keys) => {
  treeRef.value?.setCheckedKeys(keys)
}

const setChecked = (key, checked, recursively) => {
  treeRef.value?.setChecked(key, checked, recursively)
}

const toggleExpanded = (key) => {
  treeRef.value?.toggleExpanded(key)
}

const expandAll = () => {
  const expandNode = (nodes) => {
    nodes.forEach(node => {
      if (node.id) {
        treeRef.value?.expandNode(node.id)
      }
      if (node.children && node.children.length > 0) {
        expandNode(node.children)
      }
    })
  }
  expandNode(props.treeData)
}

const collapseAll = () => {
  treeRef.value?.collapseAll()
}

// 暴露给父组件
defineExpose({
  getCheckedKeys,
  getCheckedNodes,
  setCheckedKeys,
  setChecked,
  toggleExpanded,
  expandAll,
  collapseAll,
  treeRef
})
</script>

<style scoped lang="scss">
.permission-tree {
  width: 100%;
  
  :deep(.el-tree) {
    background: transparent;
    
    .el-tree-node__content {
      height: auto;
      padding: 4px 0;
      border-radius: 4px;
      
      &:hover {
        background-color: #f5f7fa;
      }
    }
  }
}

.tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  
  .node-icon {
    font-size: 16px;
    color: #606266;
  }
  
  .node-label {
    font-size: 14px;
    color: #303133;
    font-weight: 500;
  }
  
  .node-code {
    font-size: 12px;
    color: #909399;
    font-family: monospace;
    background: #f5f7fa;
    padding: 2px 6px;
    border-radius: 3px;
  }
  
  .node-type {
    margin-left: 4px;
  }
}
</style>
