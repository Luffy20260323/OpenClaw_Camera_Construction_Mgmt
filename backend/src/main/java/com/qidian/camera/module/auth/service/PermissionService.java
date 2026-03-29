package com.qidian.camera.module.auth.service;

import com.qidian.camera.module.auth.entity.UserContext;
import com.qidian.camera.module.auth.entity.PermissionInfo;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 权限服务接口
 */
@Service
public interface PermissionService {
    
    /**
     * 获取当前登录用户上下文
     */
    UserContext getCurrentUser();
    
    /**
     * 检查当前用户是否有指定权限
     */
    boolean hasPermission(String permissionCode);
    
    /**
     * 检查指定用户是否有指定权限
     */
    boolean hasPermission(Long userId, String permissionCode);
    
    /**
     * 获取用户的所有权限代码
     */
    List<String> getUserPermissions(Long userId);
    
    /**
     * 获取用户的所有角色代码
     */
    List<String> getUserRoles(Long userId);
    
    /**
     * 获取所有权限列表
     */
    List<PermissionInfo> getAllPermissions();
}
