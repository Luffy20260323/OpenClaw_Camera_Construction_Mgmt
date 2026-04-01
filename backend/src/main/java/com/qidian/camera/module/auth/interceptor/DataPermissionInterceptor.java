package com.qidian.camera.module.auth.interceptor;

import com.qidian.camera.module.auth.aspect.DataScopeContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * MyBatis 数据权限拦截器
 * 自动在 SQL 中注入数据权限 WHERE 条件
 * 支持 ALL/COMPANY/WORKAREA/SELF 四种数据范围
 */
@Slf4j
@Component
@Intercepts({
    @Signature(
        type = Executor.class,
        method = "query",
        args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}
    )
})
public class DataPermissionInterceptor implements Interceptor {
    
    /**
     * 数据范围类型：全部数据
     */
    private static final String SCOPE_ALL = "ALL";
    
    /**
     * 数据范围类型：本公司数据
     */
    private static final String SCOPE_COMPANY = "COMPANY";
    
    /**
     * 数据范围类型：本作业区数据
     */
    private static final String SCOPE_WORKAREA = "WORKAREA";
    
    /**
     * 数据范围类型：仅个人数据
     */
    private static final String SCOPE_SELF = "SELF";
    
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
        
        // 只处理 SELECT 语句
        if (ms.getSqlCommandType() != SqlCommandType.SELECT) {
            return invocation.proceed();
        }
        
        // 获取数据权限 WHERE 条件
        String whereClause = DataScopeContext.getWhereClause();
        
        if (whereClause != null && !whereClause.isEmpty()) {
            BoundSql boundSql = ms.getBoundSql(invocation.getArgs()[1]);
            String sql = boundSql.getSql();
            
            // 跳过已包含数据权限条件的 SQL
            if (sql.toUpperCase().contains("DATA_PERMISSION_FILTER")) {
                return invocation.proceed();
            }
            
            // 在 SQL 中注入 WHERE 条件
            String newSql = appendWhereClause(sql, whereClause);
            
            if (!newSql.equals(sql)) {
                log.debug("数据权限 SQL 改写：{} -> {}", sql, newSql);
                
                // 替换 SQL
                MetaObject metaObject = org.apache.ibatis.reflection.SystemMetaObject.forObject(boundSql);
                metaObject.setValue("sql", newSql);
            }
        }
        
        return invocation.proceed();
    }
    
    /**
     * 在 SQL 中追加 WHERE 条件
     */
    private String appendWhereClause(String sql, String whereClause) {
        // 简单的 SQL 解析，处理 SELECT 语句
        String upperSql = sql.toUpperCase().trim();
        
        // 检查是否已有 WHERE 条件
        int whereIndex = upperSql.indexOf("WHERE");
        
        if (whereIndex == -1) {
            // 没有 WHERE，直接添加
            // 检查是否有 ORDER BY
            int orderIndex = upperSql.indexOf("ORDER BY");
            if (orderIndex != -1) {
                return sql.substring(0, orderIndex) + " WHERE " + whereClause + " " + sql.substring(orderIndex);
            }
            // 检查是否有 GROUP BY
            int groupIndex = upperSql.indexOf("GROUP BY");
            if (groupIndex != -1) {
                return sql.substring(0, groupIndex) + " WHERE " + whereClause + " " + sql.substring(groupIndex);
            }
            // 都没有，添加到末尾
            return sql + " WHERE " + whereClause;
        } else {
            // 已有 WHERE，需要 AND 连接
            // 找到 WHERE 后面的第一个条件结束位置（简单处理，找到下一个关键字）
            int nextKeywordIndex = findNextKeyword(upperSql, whereIndex + 5);
            
            if (nextKeywordIndex != -1) {
                return sql.substring(0, nextKeywordIndex) + " AND (" + whereClause + ") " + sql.substring(nextKeywordIndex);
            } else {
                return sql + " AND (" + whereClause + ")";
            }
        }
    }
    
    /**
     * 查找下一个 SQL 关键字的位置
     */
    private int findNextKeyword(String sql, int startPos) {
        String[] keywords = {"ORDER BY", "GROUP BY", "HAVING", "LIMIT", "UNION", "INTERSECT", "EXCEPT"};
        int minIndex = -1;
        
        for (String keyword : keywords) {
            int index = sql.indexOf(keyword, startPos);
            if (index != -1) {
                if (minIndex == -1 || index < minIndex) {
                    minIndex = index;
                }
            }
        }
        
        return minIndex;
    }
    
    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }
    
    @Override
    public void setProperties(Properties properties) {
        // 无需配置
    }
}
