package com.qidian.camera.module.auth.entity;

import com.qidian.camera.module.auth.constant.RoleConstants;
import lombok.Data;
import java.util.List;

/**
 * 当前登录用户上下文
 */
@Data
public class UserContext {
    private Long userId;
    private String username;
    private String realName;
    private Long companyId;
    private Integer companyTypeId;
    private List<String> roleCodes;
    private List<String> permissions;
    private List<Long> workAreaIds;
    private Long deptId;
    private String deptPath;
    
    /**
     * 判断是否有指定权限
     * 超级管理员拥有所有权限
     */
    public boolean hasPermission(String permissionCode) {
        if (permissions == null) {
            return false;
        }
        // 超级管理员拥有所有权限
        if (isSuperAdmin()) {
            return true;
        }
        return permissions.contains(permissionCode);
    }
    
    /**
     * 判断是否是超级管理员
     * 通过角色 Code 判断（使用常量，避免分散硬编码）
     */
    public boolean isSuperAdmin() {
        return roleCodes != null && roleCodes.contains(RoleConstants.ROLE_SUPER_ADMIN);
    }
    
    /**
     * 判断是否是系统管理员（保留方法，向后兼容）
     * @deprecated 使用 isSuperAdmin() 代替
     */
    @Deprecated
    public boolean isSystemAdmin() {
        return isSuperAdmin();
    }
    
    /**
     * 判断是否有指定角色
     */
    public boolean hasRole(String roleCode) {
        return roleCodes != null && roleCodes.contains(roleCode);
    }
}
