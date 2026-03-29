package com.qidian.camera.module.auth.entity;

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
    
    /**
     * 判断是否有指定权限
     */
    public boolean hasPermission(String permissionCode) {
        if (permissions == null) {
            return false;
        }
        // 系统管理员拥有所有权限
        if (roleCodes != null && roleCodes.contains("ROLE_SYSTEM_ADMIN")) {
            return true;
        }
        return permissions.contains(permissionCode);
    }
    
    /**
     * 判断是否是系统管理员
     */
    public boolean isSystemAdmin() {
        return roleCodes != null && roleCodes.contains("ROLE_SYSTEM_ADMIN");
    }
    
    /**
     * 判断是否有指定角色
     */
    public boolean hasRole(String roleCode) {
        return roleCodes != null && roleCodes.contains(roleCode);
    }
}
