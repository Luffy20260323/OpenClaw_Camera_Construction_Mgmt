/**
 * 权限指令
 * 使用方式：v-permission="'user:create'" 或 v-permission="['user:create', 'user:edit']"
 * 
 * 支持的权限码格式：
 * - 传统格式：'user:create', 'user:edit'
 * - ELEMENT 格式：'system:user:create:button', 'system:user:list:page'
 * - 通配符格式：'system:user:*:button' (匹配该模块下所有 user 资源的 button 类型权限)
 */
import { useUserStore } from '@/stores/user';

export default {
  mounted(el, binding) {
    const { value } = binding;
    const userStore = useUserStore();
    const permissions = userStore.permissions || [];
    
    // 支持单个权限或权限数组
    const requiredPermissions = Array.isArray(value) ? value : [value];
    
    // 检查是否有任一权限（支持通配符匹配）
    const hasPermission = requiredPermissions.some(requiredPerm => {
      // 精确匹配
      if (permissions.includes(requiredPerm)) {
        return true;
      }
      
      // 通配符匹配（支持 *）
      if (requiredPerm.includes('*')) {
        const pattern = requiredPerm.replace(/\*/g, '.*');
        const regex = new RegExp(`^${pattern}$`);
        return permissions.some(perm => regex.test(perm));
      }
      
      // 层级权限匹配（兼容旧格式）
      // 例如：用户有 'system:user:create:button' 权限时，也应该匹配 'user:create'
      if (requiredPerm.split(':').length === 2) {
        // 旧格式：module:action
        const [module, action] = requiredPerm.split(':');
        return permissions.some(perm => {
          const parts = perm.split(':');
          if (parts.length >= 3) {
            // 新格式：module:resource:action:type
            return parts[0] === module && parts[2] === action;
          } else if (parts.length === 2) {
            // 旧格式：module:action
            return parts[0] === module && parts[1] === action;
          }
          return false;
        });
      }
      
      return false;
    });
    
    if (!hasPermission) {
      // 无权限，移除元素
      el.parentNode && el.parentNode.removeChild(el);
    }
  }
};
