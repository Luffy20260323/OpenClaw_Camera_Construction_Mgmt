package com.qidian.camera.module.auth.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API 权限注解
 * 用于标记需要权限控制的 API 接口
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiPermission {
    /**
     * 权限代码，对应 permissions 表的 permission_code
     */
    String value();
    
    /**
     * 权限描述
     */
    String description() default "";
}
