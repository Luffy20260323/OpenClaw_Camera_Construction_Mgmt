package com.qidian.camera.module.auth.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API 权限注解
 * 用于标记需要权限控制的 API 接口
 * 
 * @author qidian
 * @since 1.0.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    
    /**
     * 权限标识，对应 resource 表的 permission_key
     */
    String value();
    
    /**
     * 权限描述
     */
    String description() default "";
}
