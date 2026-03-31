# API 权限拦截器使用指南

## 概述

本项目提供了两种方式的 API 权限控制：

1. **注解方式**：使用 `@RequirePermission` 注解标记需要权限控制的接口
2. **URL 匹配方式**：自动匹配 `resource` 表中配置的 API 资源，无需注解

## 一、注解方式

### 1.1 注解定义

```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    String value();        // permission_key，对应 resource 表的 permission_key
    String description() default "";
}
```

### 1.2 使用示例

#### 方法级别注解

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping("/{id}")
    @RequirePermission("user:read")
    public Result<User> getUser(@PathVariable Long id) {
        // ...
    }
    
    @PostMapping
    @RequirePermission("user:create")
    public Result<Void> createUser(@RequestBody User user) {
        // ...
    }
}
```

#### 类级别注解（作用于所有方法）

```java
@RestController
@RequestMapping("/api/admin")
@RequirePermission("admin:access")
public class AdminController {
    
    @GetMapping("/stats")
    public Result<Stats> getStats() {
        // 需要 admin:access 权限
    }
    
    @PostMapping("/config")
    public Result<Void> updateConfig(@RequestBody Config config) {
        // 需要 admin:access 权限
    }
}
```

#### 方法注解优先于类注解

```java
@RestController
@RequestMapping("/api/reports")
@RequirePermission("report:read")
public class ReportController {
    
    @GetMapping
    public Result<List<Report>> list() {
        // 需要 report:read 权限（类级别）
    }
    
    @PostMapping
    @RequirePermission("report:create")
    public Result<Void> create(@RequestBody Report report) {
        // 需要 report:create 权限（方法级别，优先）
    }
}
```

## 二、URL 匹配方式

### 2.1 配置 API 资源

在 `resource` 表中配置 API 资源：

```sql
INSERT INTO resource (name, code, type, uri_pattern, method, permission_key, status) 
VALUES 
('查询用户列表', 'user:list', 'API', '/api/users', 'GET', 'user:list', 1),
('创建用户', 'user:create', 'API', '/api/users', 'POST', 'user:create', 1),
('查询用户详情', 'user:detail', 'API', '/api/users/{id}', 'GET', 'user:read', 1),
('删除用户', 'user:delete', 'API', '/api/users/**', 'DELETE', 'user:delete', 1);
```

### 2.2 URL 匹配规则

#### 精确匹配

```
URI 模式：/api/users
匹配：    /api/users
不匹配：  /api/users/1
```

#### 路径变量匹配

```
URI 模式：/api/users/{id}
匹配：    /api/users/1
匹配：    /api/users/abc
不匹配：  /api/users/1/orders
```

#### 通配符匹配

```
URI 模式：/api/users/**
匹配：    /api/users
匹配：    /api/users/1
匹配：    /api/users/1/orders
匹配：    /api/users/1/orders/2/items
```

```
URI 模式：/api/users/*
匹配：    /api/users/1
不匹配：  /api/users/1/orders
```

### 2.3 优先级规则

1. **注解优先**：如果方法或类上有 `@RequirePermission` 注解，优先使用注解的 `permission_key`
2. **URL 匹配**：没有注解时，自动匹配 `resource` 表中的 API 资源
3. **默认放行**：既没有注解，也没有匹配的 API 资源，默认放行

## 三、实现原理

### 3.1 拦截器（PermissionInterceptor）

- 拦截所有 HTTP 请求
- 检查 `@RequirePermission` 注解
- 通过 URL + Method 匹配 `resource` 表
- 验证用户权限

### 3.2 AOP 切面（PermissionAspect）

- 作为拦截器的补充
- 在方法执行前进行权限检查
- 抛出 `BusinessException` 异常

### 3.3 URL 匹配器（UrlPatternMatcher）

- 支持 `**` 通配符（匹配任意路径）
- 支持 `*` 通配符（匹配单级路径）
- 支持 `{xxx}` 路径变量

## 四、错误响应

### 4.1 未登录（401）

```json
{
    "code": 401,
    "message": "未登录，请先登录",
    "data": null,
    "timestamp": 1712000000000
}
```

### 4.2 权限不足（403）

```json
{
    "code": 403,
    "message": "无权限访问",
    "data": null,
    "timestamp": 1712000000000
}
```

## 五、配置说明

### 5.1 拦截器配置

在 `WebMvcConfig` 中注册拦截器：

```java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    private final PermissionInterceptor permissionInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                    "/auth/**",        // 认证接口
                    "/error",          // 错误页面
                    "/v3/api-docs/**", // Swagger 文档
                    "/swagger-ui/**"   // Swagger UI
                );
    }
}
```

### 5.2 排除路径

默认排除以下路径：
- `/auth/**` - 认证相关接口
- `/error` - 错误页面
- `/favicon.ico` - 网站图标
- `/v3/api-docs/**` - OpenAPI 文档
- `/swagger-ui/**` - Swagger UI
- `/webjars/**` - Webjars 静态资源
- `/actuator/**` - 健康检查

## 六、最佳实践

### 6.1 推荐方式

- **优先使用注解**：代码更清晰，权限意图更明确
- **URL 匹配作为补充**：适用于无法修改代码的接口

### 6.2 权限设计

- 使用有意义的 `permission_key`，如 `user:read`、`order:create`
- 保持 `permission_key` 与 `resource` 表一致
- 定期审计 `resource` 表中的 API 配置

### 6.3 测试建议

```java
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testUnauthorized() throws Exception {
        // 未登录，应返回 401
        mockMvc.perform(get("/api/users/1"))
               .andExpect(status().isUnauthorized());
    }
    
    @Test
    void testForbidden() throws Exception {
        // 登录但无权限，应返回 403
        String token = loginAs("normal_user");
        mockMvc.perform(get("/api/admin/stats")
                        .header("Authorization", "Bearer " + token))
               .andExpect(status().isForbidden());
    }
    
    @Test
    void testAuthorized() throws Exception {
        // 登录且有权限，应返回 200
        String token = loginAs("admin");
        mockMvc.perform(get("/api/admin/stats")
                        .header("Authorization", "Bearer " + token))
               .andExpect(status().isOk());
    }
}
```

## 七、文件清单

```
src/main/java/com/qidian/camera/module/auth/
├── annotation/
│   └── RequirePermission.java       # 权限注解
├── aspect/
│   └── PermissionAspect.java        # AOP 切面
├── interceptor/
│   └── PermissionInterceptor.java   # 拦截器
├── util/
│   └── UrlPatternMatcher.java       # URL 匹配工具
└── config/
    └── WebMvcConfig.java            # Web MVC 配置（已更新）
```
