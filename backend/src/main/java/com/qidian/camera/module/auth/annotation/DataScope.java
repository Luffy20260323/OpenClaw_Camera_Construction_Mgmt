package com.qidian.camera.module.auth.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据权限注解
 * 用于标记需要数据范围控制的查询方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataScope {
    /**
     * 表别名（用于 SQL）
     */
    String tableAlias() default "";
    
    /**
     * 数据范围类型
     */
    ScopeType scopeType() default ScopeType.COMPANY;
    
    enum ScopeType {
        ALL,            // 全部数据（系统管理员）
        COMPANY,        // 本公司数据
        WORK_AREA,      // 负责的作业区
        SELF,           // 仅本人
        CUSTOM          // 自定义
    }
}
