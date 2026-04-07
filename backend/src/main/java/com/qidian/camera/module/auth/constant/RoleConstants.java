package com.qidian.camera.module.auth.constant;

/**
 * 角色编码常量
 * 
 * 所有角色相关的编码都定义在这里，避免在代码中分散硬编码
 */
public final class RoleConstants {
    
    /**
     * 超级管理员角色编码
     * 拥有系统所有权限，仅 admin 用户
     */
    public static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";
    
    /**
     * 系统管理员角色编码
     * 日常运维角色，权限可配置
     */
    public static final String ROLE_SYSTEM_ADMIN = "ROLE_SYSTEM_ADMIN";
    
    /**
     * 甲方管理员角色编码
     */
    public static final String ROLE_JIAFANG_ADMIN = "ROLE_JIAFANG_ADMIN";
    
    /**
     * 乙方管理员角色编码
     */
    public static final String ROLE_YIFANG_ADMIN = "ROLE_YIFANG_ADMIN";
    
    /**
     * 监理方管理员角色编码
     */
    public static final String ROLE_JIANLIFANG_ADMIN = "ROLE_JIANLIFANG_ADMIN";
    
    /**
     * 私有构造函数，防止实例化
     */
    private RoleConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
