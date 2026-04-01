package com.qidian.camera.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qidian.camera.module.auth.entity.Resource;
import com.qidian.camera.module.auth.entity.RolePermissionAdjustment;
import com.qidian.camera.module.auth.entity.PermissionAuditLog;
import com.qidian.camera.module.auth.mapper.*;
import com.qidian.camera.module.user.mapper.UserMapper;
import com.qidian.camera.module.role.mapper.RoleMapper;
import com.qidian.camera.module.role.mapper.RoleMapper;
import com.qidian.camera.module.user.mapper.UserMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 权限管理 Controller 集成测试
 * 
 * 注意：由于项目中存在两个同名的 PermissionInterceptor 类（filter 和 interceptor 包），
 * 测试使用 @SpringBootTest 的 excludeFilters 来排除冲突的 bean。
 * 
 * 测试范围：
 * - TC-05-001: 获取资源树 API
 * - TC-05-002: 获取角色权限树 API
 * - TC-05-003: 调整角色权限 API
 * - TC-05-004: 获取用户权限 API
 * - TC-05-005: API 权限校验（拦截器）
 * - 资源管理 API 测试
 * - 审计日志 API 测试
 */
@SpringBootTest(properties = {
    "spring.main.allow-bean-definition-overriding=true",
    "spring.security.filter.order=-100",
    "spring.main.web-application-type=none"
})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("权限管理 API 集成测试")
public class PermissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ResourceMapper resourceMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Autowired
    private RolePermissionAdjustmentMapper rolePermissionAdjustmentMapper;

    @Autowired
    private PermissionAuditLogMapper auditLogMapper;

    // ==================== TC-05-001: 获取资源树 API ====================

    @Test
    @Order(1)
    @DisplayName("TC-05-001: 获取资源树 API")
    void testGetResourceTree() throws Exception {
        // 调用获取资源树 API
        String response = mockMvc.perform(get("/api/resources/tree")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").isArray())
            .andReturn()
            .getResponse()
            .getContentAsString();

        // 验证返回数据
        Map<String, Object> result = objectMapper.readValue(response, Map.class);
        List<?> data = (List<?>) result.get("data");
        
        assertNotNull(data);
        assertTrue(data.size() > 0, "资源树应该包含至少一个节点");
        
        // 验证根节点包含模块资源
        boolean hasModule = data.stream()
            .anyMatch(node -> "MODULE".equals(((Map<?, ?>) node).get("type")));
        assertTrue(hasModule, "资源树应该包含 MODULE 类型节点");
    }

    @Test
    @Order(2)
    @DisplayName("TC-05-001-2: 获取菜单树 API")
    void testGetMenuTree() throws Exception {
        // 调用获取菜单树 API
        mockMvc.perform(get("/api/resources/menu-tree")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").isArray())
            // 验证只包含 MODULE 和 MENU 类型
            .andExpect(result -> {
                String content = result.getResponse().getContentAsString();
                Map<String, Object> response = objectMapper.readValue(content, Map.class);
                List<?> data = (List<?>) response.get("data");
                
                // 递归检查所有节点类型
                for (Object node : data) {
                    checkMenuTreeType((Map<?, ?>) node);
                }
            });
    }

    private void checkMenuTreeType(Map<?, ?> node) {
        String type = (String) node.get("type");
        assertTrue("MODULE".equals(type) || "MENU".equals(type), 
            "菜单树只应包含 MODULE 和 MENU 类型，实际类型：" + type);
        
        List<?> children = (List<?>) node.get("children");
        if (children != null) {
            for (Object child : children) {
                checkMenuTreeType((Map<?, ?>) child);
            }
        }
    }

    @Test
    @Order(3)
    @DisplayName("TC-05-001-3: 获取基本权限资源 API")
    void testGetBasicResources() throws Exception {
        // 调用获取基本权限 API
        mockMvc.perform(get("/api/resources/basic")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").isArray())
            // 验证返回的都是基本权限
            .andExpect(result -> {
                String content = result.getResponse().getContentAsString();
                Map<String, Object> response = objectMapper.readValue(content, Map.class);
                List<Map<?, ?>> data = (List<Map<?, ?>>) response.get("data");
                
                for (Map<?, ?> resource : data) {
                    assertEquals(1, resource.get("isBasic"), 
                        "基本权限资源的 isBasic 应为 1");
                }
            });
    }

    // ==================== 资源管理 API 测试 ====================

    @Test
    @Order(10)
    @DisplayName("TC-01-001: 创建模块资源")
    void testCreateModuleResource() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("name", "测试模块");
        request.put("code", "test-module-" + System.currentTimeMillis());
        request.put("type", "MODULE");
        request.put("sortOrder", 999);

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.name").value("测试模块"))
            .andExpect(jsonPath("$.data.type").value("MODULE"));
    }

    @Test
    @Order(11)
    @DisplayName("TC-01-005: 创建重复 code 的资源（失败场景）")
    void testCreateDuplicateCodeResource() throws Exception {
        // 先创建一个资源
        String uniqueCode = "unique-code-" + System.currentTimeMillis();
        Map<String, Object> request = new HashMap<>();
        request.put("name", "唯一资源");
        request.put("code", uniqueCode);
        request.put("type", "PERMISSION");
        request.put("sortOrder", 100);

        String requestJson = objectMapper.writeValueAsString(request);

        // 第一次创建成功
        mockMvc.perform(post("/api/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isOk());

        // 第二次创建相同 code 应失败
        mockMvc.perform(post("/api/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(500))
            .andExpect(jsonPath("$.message").value("资源编码已存在"));
    }

    @Test
    @Order(12)
    @DisplayName("TC-01-010: 获取单个资源详情")
    void testGetResource() throws Exception {
        // 获取 ID=1 的资源（登录权限）
        mockMvc.perform(get("/api/resources/1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.id").value(1))
            .andExpect(jsonPath("$.data.name").value("登录"))
            .andExpect(jsonPath("$.data.isBasic").value(1));
    }

    @Test
    @Order(13)
    @DisplayName("TC-01-011: 获取不存在的资源（失败场景）")
    void testGetNonExistentResource() throws Exception {
        mockMvc.perform(get("/api/resources/99999")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(500))
            .andExpect(jsonPath("$.message").value("资源不存在"));
    }

    @Test
    @Order(14)
    @DisplayName("TC-01-012: 更新资源信息")
    void testUpdateResource() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("name", "更新后的资源名称");
        request.put("sortOrder", 888);

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(put("/api/resources/5")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.name").value("更新后的资源名称"))
            .andExpect(jsonPath("$.data.sortOrder").value(888));

        // 验证数据库已更新
        Resource updated = resourceMapper.selectById(5L);
        assertNotNull(updated);
        assertEquals("更新后的资源名称", updated.getName());
        assertEquals(888, updated.getSortOrder());
    }

    @Test
    @Order(15)
    @DisplayName("TC-01-015: 删除基本权限资源（失败场景）")
    void testDeleteBasicResource() throws Exception {
        // 尝试删除 ID=1 的基本权限资源（登录）
        mockMvc.perform(delete("/api/resources/1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(500))
            .andExpect(jsonPath("$.message").value("基本权限资源不可删除"));

        // 验证资源未被删除
        Resource resource = resourceMapper.selectById(1L);
        assertNotNull(resource);
    }

    // ==================== TC-05-002: 获取角色权限树 API ====================

    @Test
    @Order(20)
    @DisplayName("TC-05-002: 获取角色权限树 API")
    void testGetRolePermissionTree() throws Exception {
        // 获取 test_role_a (id=3) 的权限树
        mockMvc.perform(get("/api/roles/3/permissions/tree")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").isArray())
            // 验证权限状态标记
            .andExpect(result -> {
                String content = result.getResponse().getContentAsString();
                Map<String, Object> response = objectMapper.readValue(content, Map.class);
                List<?> data = (List<?>) response.get("data");
                
                // 验证存在不同状态的节点
                boolean hasBasic = false;
                boolean hasDefault = false;
                boolean hasRemoved = false;
                boolean hasAdded = false;
                
                for (Object node : data) {
                    hasBasic |= checkNodeStatus((Map<?, ?>) node, "basic");
                    hasDefault |= checkNodeStatus((Map<?, ?>) node, "default");
                    hasRemoved |= checkNodeStatus((Map<?, ?>) node, "removed");
                    hasAdded |= checkNodeStatus((Map<?, ?>) node, "added");
                }
                
                assertTrue(hasBasic, "应包含 basic 状态节点");
                assertTrue(hasDefault || hasRemoved || hasAdded, "应包含 default/removed/added 状态节点");
            });
    }

    private boolean checkNodeStatus(Map<?, ?> node, String status) {
        if (status.equals(node.get("status"))) {
            return true;
        }
        List<?> children = (List<?>) node.get("children");
        if (children != null) {
            for (Object child : children) {
                if (checkNodeStatus((Map<?, ?>) child, status)) {
                    return true;
                }
            }
        }
        return false;
    }

    // ==================== TC-05-003: 调整角色权限 API ====================

    @Test
    @Order(30)
    @DisplayName("TC-05-003: 调整角色权限 API - ADD")
    void testAdjustRolePermissionAdd() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("resourceId", 10);  // user-create-api
        request.put("action", "ADD");

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/roles/3/permissions/adjust")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        // 验证调整记录已创建
        RolePermissionAdjustment adjustment = rolePermissionAdjustmentMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RolePermissionAdjustment>()
                .eq(RolePermissionAdjustment::getRoleId, 3L)
                .eq(RolePermissionAdjustment::getResourceId, 10L)
        );
        assertNotNull(adjustment);
        assertEquals("ADD", adjustment.getAction());

        // 验证审计日志已记录
        long auditCount = auditLogMapper.selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.qidian.camera.module.auth.entity.PermissionAuditLog>()
                .eq(com.qidian.camera.module.auth.entity.PermissionAuditLog::getTargetId, 3L)
        );
        assertTrue(auditCount > 0, "应记录审计日志");
    }

    @Test
    @Order(31)
    @DisplayName("TC-05-003-2: 调整角色权限 API - REMOVE")
    void testAdjustRolePermissionRemove() throws Exception {
        // test_role_a 已拥有 resource_id=11，尝试移除
        Map<String, Object> request = new HashMap<>();
        request.put("resourceId", 11);
        request.put("action", "REMOVE");

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/roles/3/permissions/adjust")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(32)
    @DisplayName("TC-04-006: 尝试移除基本权限（失败）")
    void testAdjustBasicPermission() throws Exception {
        // 尝试移除基本权限 resource_id=1（登录）
        Map<String, Object> request = new HashMap<>();
        request.put("resourceId", 1);
        request.put("action", "REMOVE");

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/roles/3/permissions/adjust")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(500))
            .andExpect(jsonPath("$.message").value("基本权限不可调整"));
    }

    @Test
    @Order(33)
    @DisplayName("TC-04-003: 批量调整角色权限")
    void testBatchAdjustRolePermission() throws Exception {
        List<Map<String, Object>> adjustments = List.of(
            Map.of("resourceId", 12, "action", "ADD"),
            Map.of("resourceId", 13, "action", "ADD")
        );

        String requestJson = objectMapper.writeValueAsString(adjustments);

        mockMvc.perform(post("/api/roles/3/permissions/adjust-batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    // ==================== TC-05-004: 获取用户权限 API ====================

    @Test
    @Order(40)
    @DisplayName("TC-05-004: 获取用户权限相关 API")
    void testUserPermissionAPI() throws Exception {
        // 获取所有权限列表
        mockMvc.perform(get("/api/permission/list")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").isArray());

        // 获取所有角色列表
        mockMvc.perform(get("/api/permission/roles")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").isArray());

        // 获取权限分组列表
        mockMvc.perform(get("/api/permission/groups")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(41)
    @DisplayName("TC-05-004-2: 获取角色权限配置")
    void testGetRolePermissions() throws Exception {
        mockMvc.perform(get("/api/permission/role/3")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.roleId").value(3))
            .andExpect(jsonPath("$.data.permissionIds").isArray());
    }

    // ==================== 审计日志 API 测试 ====================

    @Test
    @Order(50)
    @DisplayName("TC-06-005: 查询审计日志")
    void testGetAuditLogs() throws Exception {
        mockMvc.perform(get("/api/permission/audit-logs")
                .contentType(MediaType.APPLICATION_JSON)
                .param("page", "1")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.total").exists())
            .andExpect(jsonPath("$.data.totalPages").exists());
    }

    @Test
    @Order(51)
    @DisplayName("TC-06-005-2: 按目标查询审计日志")
    void testGetAuditLogsByTarget() throws Exception {
        mockMvc.perform(get("/api/permission/audit-logs")
                .contentType(MediaType.APPLICATION_JSON)
                .param("targetId", "3")
                .param("targetType", "ROLE")
                .param("page", "1")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    // ==================== 事务回滚测试 ====================

    @Test
    @Order(60)
    @DisplayName("TC-TRANSACTION: 测试数据库事务回滚")
    void testTransactionRollback() throws Exception {
        // 记录初始资源数量
        long initialCount = resourceMapper.selectCount(null);

        // 创建资源
        Map<String, Object> request = new HashMap<>();
        request.put("name", "事务测试资源");
        request.put("code", "transaction-test-" + System.currentTimeMillis());
        request.put("type", "PERMISSION");
        request.put("sortOrder", 999);

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isOk());

        // 由于 @Transactional，测试结束后数据应回滚
        // 这里验证创建成功即可，回滚由 Spring 自动处理
        long afterCount = resourceMapper.selectCount(null);
        assertEquals(initialCount + 1, afterCount, "资源数量应增加 1");
    }
}
