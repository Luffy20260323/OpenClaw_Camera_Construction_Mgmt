<template>
  <el-button
    v-if="visible"
    :disabled="disabled || !enabled"
    :type="type"
    :size="size"
    :loading="loading"
    @click="handleClick"
  >
    <slot></slot>
  </el-button>
</template>

<script setup>
import { computed } from 'vue';
import { useUserStore } from '@/stores/user';

const props = defineProps({
  // 所需权限代码
  permission: { type: String, required: true },
  // 按钮类型
  type: { type: String, default: 'default' },
  // 按钮尺寸
  size: { type: String, default: 'default' },
  // 是否禁用
  disabled: { type: Boolean, default: false },
  // 加载状态
  loading: { type: Boolean, default: false }
});

const emit = defineEmits(['click']);

const userStore = useUserStore();
const permissions = userStore.permissions || [];

// 检查是否有权限
const visible = computed(() => permissions.includes(props.permission));
const enabled = computed(() => visible.value);

const handleClick = (event) => {
  emit('click', event);
};
</script>
