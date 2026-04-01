package com.qidian.camera.module.auth.service;

import com.qidian.camera.module.auth.entity.UserContext;

/**
 * 数据权限服务接口
 * 负责用户和角色的数据权限查询与管理
 */
public interface DataPermissionService {
    
    /**
     * 获取用户的数据权限范围类型
     * 
     * @param userId 用户 ID
     * @return 数据范围类型：ALL, DEPT, DEPT_AND_SUB, SELF
     */
    String getUserDataScopeType(Long userId);
    
    /**
     * 获取用户的数据权限部门 ID
     * 
     * @param userId 用户 ID
     * @return 部门 ID，如果没有则返回 null
     */
    Long getUserDataDeptId(Long userId);
    
    /**
     * 获取用户的数据权限部门 ID 列表
     * 
     * @param userId 用户 ID
     * @return 部门 ID 列表（逗号分隔的字符串）
     */
    String getUserDataDeptIds(Long userId);
    
    /**
     * 获取用户的数据权限 SQL 条件
     * 
     * @param userId 用户 ID
     * @param entityType 实体类型（表名或业务类型）
     * @return SQL WHERE 条件（不包含 WHERE 关键字）
     */
    String getUserDataPermissionSql(Long userId, String entityType);
    
    /**
     * 获取角色的数据权限范围类型
     * 
     * @param roleId 角色 ID
     * @return 数据范围类型
     */
    String getRoleDataScopeType(Long roleId);
    
    /**
     * 获取角色的数据权限部门 ID
     * 
     * @param roleId 角色 ID
     * @return 部门 ID
     */
    Long getRoleDataDeptId(Long roleId);
    
    /**
     * 获取角色的数据权限部门 ID 列表
     * 
     * @param roleId 角色 ID
     * @return 部门 ID 列表
     */
    String getRoleDataDeptIds(Long roleId);
    
    /**
     * 设置用户数据权限
     * 
     * @param userId 用户 ID
     * @param scopeType 数据范围类型
     * @param deptId 部门 ID（当 scopeType 为 DEPT 时）
     * @param deptIds 部门 ID 列表（当 scopeType 为 DEPT_AND_SUB 时）
     * @param operatorId 操作人 ID
     */
    void setUserDataPermission(Long userId, String scopeType, Long deptId, String deptIds, Long operatorId);
    
    /**
     * 设置角色数据权限
     * 
     * @param roleId 角色 ID
     * @param scopeType 数据范围类型
     * @param deptId 部门 ID
     * @param deptIds 部门 ID 列表
     */
    void setRoleDataPermission(Long roleId, String scopeType, Long deptId, String deptIds);
    
    /**
     * 刷新用户数据权限缓存
     * 
     * @param userId 用户 ID
     */
    void refreshUserCache(Long userId);
    
    /**
     * 构建数据权限 WHERE 条件
     * 
     * @param userContext 用户上下文
     * @param scopeType 数据范围类型
     * @param tableAlias 表别名
     * @return WHERE 条件
     */
    String buildWhereClause(UserContext userContext, String scopeType, String tableAlias);
}
