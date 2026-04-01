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
     * @return 数据范围类型：ALL, COMPANY, WORKAREA, SELF, CUSTOM
     */
    String getUserDataScopeType(Long userId);
    
    /**
     * 获取用户的数据权限公司 ID
     * 
     * @param userId 用户 ID
     * @return 公司 ID，如果没有则返回 null
     */
    Long getUserDataCompanyId(Long userId);
    
    /**
     * 获取用户的数据权限作业区 ID 列表
     * 
     * @param userId 用户 ID
     * @return 作业区 ID 列表（逗号分隔的字符串）
     */
    String getUserDataWorkAreaIds(Long userId);
    
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
     * 获取角色的数据权限公司 ID
     * 
     * @param roleId 角色 ID
     * @return 公司 ID
     */
    Long getRoleDataCompanyId(Long roleId);
    
    /**
     * 获取角色的数据权限作业区 ID 列表
     * 
     * @param roleId 角色 ID
     * @return 作业区 ID 列表
     */
    String getRoleDataWorkAreaIds(Long roleId);
    
    /**
     * 设置用户数据权限
     * 
     * @param userId 用户 ID
     * @param scopeType 数据范围类型
     * @param companyId 公司 ID（当 scopeType 为 COMPANY 时）
     * @param workAreaIds 作业区 ID 列表（当 scopeType 为 WORKAREA 时）
     * @param operatorId 操作人 ID
     */
    void setUserDataPermission(Long userId, String scopeType, Long companyId, String workAreaIds, Long operatorId);
    
    /**
     * 设置角色数据权限
     * 
     * @param roleId 角色 ID
     * @param scopeType 数据范围类型
     * @param companyId 公司 ID
     * @param workAreaIds 作业区 ID 列表
     */
    void setRoleDataPermission(Long roleId, String scopeType, Long companyId, String workAreaIds);
    
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
