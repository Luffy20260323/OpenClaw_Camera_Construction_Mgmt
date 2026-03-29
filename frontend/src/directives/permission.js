/**
 * 权限指令
 * 使用方式：v-permission="'user:create'" 或 v-permission="['user:create', 'user:edit']"
 */
import { useUserStore } from '@/stores/user';

export default {
  mounted(el, binding) {
    const { value } = binding;
    const userStore = useUserStore();
    const permissions = userStore.permissions || [];
    
    // 支持单个权限或权限数组
    const requiredPermissions = Array.isArray(value) ? value : [value];
    
    // 检查是否有任一权限
    const hasPermission = requiredPermissions.some(perm => permissions.includes(perm));
    
    if (!hasPermission) {
      // 无权限，移除元素
      el.parentNode && el.parentNode.removeChild(el);
    }
  }
};
