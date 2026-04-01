package com.qidian.camera.module.auth.aspect;

import com.qidian.camera.module.auth.annotation.DataScope;
import com.qidian.camera.module.auth.entity.UserContext;
import com.qidian.camera.module.auth.service.impl.PermissionServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 数据权限 AOP 切面
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class DataScopeAspect {
    
    private final PermissionServiceImpl permissionService;
    
    /**
     * 环绕通知，处理数据权限
     */
    @Around("@annotation(dataScope)")
    public Object around(ProceedingJoinPoint pjp, DataScope dataScope) throws Throwable {
        // 1. 获取当前用户
        UserContext user;
        try {
            user = permissionService.getCurrentUser();
        } catch (IllegalStateException e) {
            // 用户未登录，跳过数据权限过滤
            return pjp.proceed();
        }
        
        // 2. 系统管理员不需要数据过滤
        if (user.isSystemAdmin()) {
            log.debug("系统管理员，跳过数据权限过滤：userId={}", user.getUserId());
            return pjp.proceed();
        }
        
        // 3. 根据 scopeType 生成 WHERE 条件
        String whereClause = buildWhereClause(dataScope, user);
        
        if (whereClause != null && !whereClause.isEmpty()) {
            // 4. 将 WHERE 条件注入到 ThreadLocal（供 MyBatis 拦截器使用）
            DataScopeContext.setWhereClause(whereClause);
            DataScopeContext.setTableAlias(dataScope.tableAlias());
            log.debug("注入数据权限条件：{}", whereClause);
        }
        
        try {
            return pjp.proceed();
        } finally {
            // 5. 清理 ThreadLocal
            DataScopeContext.clear();
        }
    }
    
    /**
     * 构建 WHERE 条件
     */
    private String buildWhereClause(DataScope dataScope, UserContext user) {
        String alias = dataScope.tableAlias();
        if (alias != null && !alias.isEmpty()) {
            alias += ".";
        } else {
            alias = "";
        }
        
        return switch (dataScope.scopeType()) {
            case ALL -> {
                // 全部数据（不应该到这里，因为系统管理员已经提前返回）
                yield null;
            }
            case DEPT -> {
                // 本部门数据
                Long deptId = user.getDeptId();
                if (deptId == null) {
                    yield "1=0";  // 没有部门 ID，无法访问任何数据
                }
                yield alias + "dept_id = " + deptId;
            }
            case DEPT_AND_SUB -> {
                // 本部门及下级部门
                String deptPath = user.getDeptPath();
                if (deptPath == null || deptPath.isEmpty()) {
                    yield "1=0";
                }
                yield alias + "dept_path LIKE '" + deptPath + "%'";
            }
            case SELF -> {
                // 仅本人创建的数据
                yield alias + "created_by = " + user.getUserId();
            }
            case CUSTOM -> {
                // 自定义条件（通过 user_work_areas 表）
                if (user.getWorkAreaIds() == null || user.getWorkAreaIds().isEmpty()) {
                    yield "1=0";
                }
                String ids = user.getWorkAreaIds().stream()
                    .map(String::valueOf)
                    .collect(java.util.stream.Collectors.joining(","));
                yield alias + "id IN (" + ids + ")";
            }
        };
    }
}
