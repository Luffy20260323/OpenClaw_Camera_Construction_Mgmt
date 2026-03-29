package com.qidian.camera.module.auth.aspect;

/**
 * 数据权限上下文（ThreadLocal）
 */
public class DataScopeContext {
    
    private static final ThreadLocal<String> WHERE_CLAUSE = new ThreadLocal<>();
    private static final ThreadLocal<String> TABLE_ALIAS = new ThreadLocal<>();
    
    /**
     * 设置 WHERE 条件
     */
    public static void setWhereClause(String whereClause) {
        WHERE_CLAUSE.set(whereClause);
    }
    
    /**
     * 获取 WHERE 条件
     */
    public static String getWhereClause() {
        return WHERE_CLAUSE.get();
    }
    
    /**
     * 设置表别名
     */
    public static void setTableAlias(String tableAlias) {
        TABLE_ALIAS.set(tableAlias);
    }
    
    /**
     * 获取表别名
     */
    public static String getTableAlias() {
        return TABLE_ALIAS.get();
    }
    
    /**
     * 清理 ThreadLocal
     */
    public static void clear() {
        WHERE_CLAUSE.remove();
        TABLE_ALIAS.remove();
    }
}
