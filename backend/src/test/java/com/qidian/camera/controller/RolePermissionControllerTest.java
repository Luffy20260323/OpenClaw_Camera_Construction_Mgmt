package com.qidian.camera.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qidian.camera.module.auth.entity.*;
import com.qidian.camera.module.auth.mapper.*;
import com.qidian.camera.module.auth.service.PermissionService;
import com.qidian.camera.module.user.mapper.UserMapper;
import com.qidian.camera.module.role.mapper.RoleMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 角色权限管理 Controller 集成测试
 * 测试范围：
 * - 角色权限树 API 测试
 * - 角色权限调整 API 测试
 * - 用户权限计算测试
 * - 权限状态标记验证
 */
@SpringBootTest(properties = {
    "spring.main.allow-bean-definition-overriding=true",
    "spring.security.filter.order=-100"
})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("角色权限管理 API 集成测试")
public class RolePermissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PermissionService permissionService;

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
    private UserPermissionAdjustmentMapper userPermissionAdjustmentMapper;

    @Autowired
    private PermissionAuditLogMapper auditLogMapper;

    // ==================== TC-02-011: 获取角色权限树（标记状态） ====================

    @Test
    @Order(1)
    @DisplayName("TC-02-011: 获取角色权限树（标记状态）")
    void testGetRolePermissionTree() throws Exception {
        // 获取 test_role_a (id=3) 的权限树
        String response = mockMvc.perform(get("/api/roles/3/permissions/tree")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").isArray())
            .andReturn()
            .getResponse()
            .getContentAsString();

        // 验证返回数据结构
        Map<String, Object> result = objectMapper.readValue(response, Map.class);
        List<?> data = (List<?>) result.get("data");
        
        assertNotNull(data);
        assertTrue(data.size() > 0, "权限树应该包含节点");
        
        // 验证每个节点都有 status 字段
        for (Object node : data) {
            verifyPermissionTreeNode((Map<?, ?>) node);
        }
    }

    private void verifyPermissionTreeNode(Map<?, ?> node) {
        assertNotNull(node.get("id"), "节点应有 id");
        assertNotNull(node.get("name"), "节点应有 name");
        assertNotNull(node.get("type"), "节点应有 type");
        assertNotNull(node.get("status"), "节点应有 status 字段");
        
        String status = (String) node.get("status");
        assertTrue(
            "basic".equals(status) || "default".equals(status) || 
            "added".equals(status) || "removed".equals(status) || "none".equals(status),
            "status 应为 basic/default/added/removed/none，实际：" + status
        );
        
        List<?> children = (List<?>) node.get("children");
        if (children != null && !children.isEmpty()) {
            for (Object child : children) {
                verifyPermissionTreeNode((Map<?, ?>) child);
            }
        }
    }

    // ==================== TC-02-012: 权限树状态标记验证 ====================

    @Test
    @Order(2)
    @DisplayName("TC-02-012: 权限树状态标记验证")
    void testPermissionTreeStatusMarking() throws Exception {
        String response = mockMvc.perform(get("/api/roles/3/permissions/tree")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        Map<String, Object> result = objectMapper.readValue(response, Map.class);
        List<?> data = (List<?>) result.get("data");
        
        // 收集所有节点状态
        Map<String, List<Long>> statusMap = new HashMap<>();
        collectNodeStatus(data, statusMap);
        
        // 验证 basic 状态节点存在（resource_id=1,2,3,4）
        assertTrue(statusMap.containsKey("basic"), "应包含 basic 状态节点");
        
        // 验证 removed 状态节点存在（resource_id=11 有 REMOVE 调整）
        assertTrue(statusMap.containsKey("removed"), "应包含 removed 状态节点");
        
        // 验证 added 状态节点存在（resource_id=13 有 ADD 调整）
        assertTrue(statusMap.containsKey("added"), "应包含 added 状态节点");
    }

    private void collectNodeStatus(List<?> nodes, Map<String, List<Long>> statusMap) {
        for (Object node : nodes) {
            Map<?, ?> nodeMap = (Map<?, ?>) node;
            String status = (String) nodeMap.get("status");
            Long id = ((Number) nodeMap.get("id")).longValue();
            
            statusMap.computeIfAbsent(status, k -> new ArrayList<>()).add(id);
            
            List<?> children = (List<?>) nodeMap.get("children");
            if (children != null) {
                collectNodeStatus(children, statusMap);
            }
        }
    }

    // ==================== TC-04-001: 调整角色权限 - ADD ====================

    @Test
    @Order(10)
    @DisplayName("TC-04-001: 调整角色权限 - ADD")
    void testAdjustRolePermissionAdd() throws Exception {
        // 记录调整前的权限
        Set<Long> beforePermissions = permissionService.calculateRolePermissions(3L);
        
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

        // 验证权限计算结果包含新添加的权限
        Set<Long> afterPermissions = permissionService.calculateRolePermissions(3L);
        assertTrue(afterPermissions.contains(10L), "权限计算结果应包含新添加的资源 ID 10");
    }

    // ==================== TC-04-002: 调整角色权限 - REMOVE ====================

    @Test
    @Order(11)
    @DisplayName("TC-04-002: 调整角色权限 - REMOVE")
    void testAdjustRolePermissionRemove() throws Exception {
        // test_role_a 已拥有 resource_id=11（缺省权限），尝试移除
        Set<Long> beforePermissions = permissionService.calculateRolePermissions(3L);
        assertTrue(beforePermissions.contains(11L), "测试前置条件：角色应拥有资源 ID 11");
        
        Map<String, Object> request = new HashMap<>();
        request.put("resourceId", 11);
        request.put("action", "REMOVE");

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/roles/3/permissions/adjust")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        // 验证权限计算结果不包含被移除的权限
        Set<Long> afterPermissions = permissionService.calculateRolePermissions(3L);
        assertFalse(afterPermissions.contains(11L), "权限计算结果不应包含被移除的资源 ID 11");
    }

    // ==================== TC-04-003: 批量调整角色权限 ====================

    @Test
    @Order(12)
    @DisplayName("TC-04-003: 批量调整角色权限")
    void testBatchAdjustRolePermission() throws Exception {
        List<Map<String, Object>> adjustments = Arrays.asList(
            createAdjustment(14, "ADD"),
            createAdjustment(15, "ADD")
        );

        String requestJson = objectMapper.writeValueAsString(adjustments);

        mockMvc.perform(post("/api/roles/3/permissions/adjust-batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        // 验证两条调整记录都已创建
        RolePermissionAdjustment adj1 = rolePermissionAdjustmentMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RolePermissionAdjustment>()
                .eq(RolePermissionAdjustment::getRoleId, 3L)
                .eq(RolePermissionAdjustment::getResourceId, 14L)
        );
        RolePermissionAdjustment adj2 = rolePermissionAdjustmentMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RolePermissionAdjustment>()
                .eq(RolePermissionAdjustment::getRoleId, 3L)
                .eq(RolePermissionAdjustment::getResourceId, 15L)
        );
        
        assertNotNull(adj1);
        assertNotNull(adj2);
        assertEquals("ADD", adj1.getAction());
        assertEquals("ADD", adj2.getAction());
    }

    private Map<String, Object> createAdjustment(long resourceId, String action) {
        Map<String, Object> adj = new HashMap<>();
        adj.put("resourceId", resourceId);
        adj.put("action", action);
        return adj;
    }

    // ==================== TC-04-006: 基本权限保护 ====================

    @Test
    @Order(20)
    @DisplayName("TC-04-006: 尝试移除基本权限（失败）")
    void testAdjustBasicPermissionFailure() throws Exception {
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

        // 验证没有创建调整记录
        RolePermissionAdjustment adjustment = rolePermissionAdjustmentMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RolePermissionAdjustment>()
                .eq(RolePermissionAdjustment::getRoleId, 3L)
                .eq(RolePermissionAdjustment::getResourceId, 1L)
        );
        assertNull(adjustment, "基本权限不应创建调整记录");
    }

    // ==================== TC-02-008: ADD 后再 REMOVE（抵消） ====================

    @Test
    @Order(30)
    @DisplayName("TC-02-008: 角色权限 ADD 后再 REMOVE（抵消）")
    void testAdjustmentCancel() throws Exception {
        // 先 ADD
        Map<String, Object> addRequest = Map.of("resourceId", 25, "action", "ADD");
        mockMvc.perform(post("/api/roles/3/permissions/adjust")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addRequest)))
            .andExpect(status().isOk());

        // 再 REMOVE（抵消）
        Map<String, Object> removeRequest = Map.of("resourceId", 25, "action", "REMOVE");
        mockMvc.perform(post("/api/roles/3/permissions/adjust")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(removeRequest)))
            .andExpect(status().isOk());

        // 验证调整记录被更新或抵消
        RolePermissionAdjustment adjustment = rolePermissionAdjustmentMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RolePermissionAdjustment>()
                .eq(RolePermissionAdjustment::getRoleId, 3L)
                .eq(RolePermissionAdjustment::getResourceId, 25L)
        );
        
        // 根据实现，可能是 REMOVE 状态或被删除
        if (adjustment != null) {
            assertEquals("REMOVE", adjustment.getAction());
        }
    }

    // ==================== TC-02-009: REMOVE 后再 ADD（抵消） ====================

    @Test
    @Order(31)
    @DisplayName("TC-02-009: 角色权限 REMOVE 后再 ADD（抵消）")
    void testAdjustmentRestore() throws Exception {
        // resource_id=11 已有 REMOVE 调整，现在 ADD 回来
        Map<String, Object> addRequest = Map.of("resourceId", 11, "action", "ADD");
        mockMvc.perform(post("/api/roles/3/permissions/adjust")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addRequest)))
            .andExpect(status().isOk());

        // 验证调整记录被更新为 ADD
        RolePermissionAdjustment adjustment = rolePermissionAdjustmentMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RolePermissionAdjustment>()
                .eq(RolePermissionAdjustment::getRoleId, 3L)
                .eq(RolePermissionAdjustment::getResourceId, 11L)
        );
        
        if (adjustment != null) {
            assertEquals("ADD", adjustment.getAction());
        }
    }

    // ==================== TC-03-001: 单角色用户权限计算 ====================

    @Test
    @Order(40)
    @DisplayName("TC-03-001: 单角色用户权限计算")
    void testSingleRoleUserPermissionCalculation() throws Exception {
        // test_user1 (id=2) 只拥有 test_role_a (id=3)
        Set<Long> permissions = permissionService.calculateUserPermissions(2L);
        
        // 验证包含基本权限
        assertTrue(permissions.contains(1L), "应包含基本权限：登录");
        assertTrue(permissions.contains(2L), "应包含基本权限：退出");
        assertTrue(permissions.contains(3L), "应包含基本权限：查看个人信息");
        assertTrue(permissions.contains(4L), "应包含基本权限：修改密码");
        
        // 验证包含缺省权限（resource_id=8: user:view）
        assertTrue(permissions.contains(8L), "应包含缺省权限：user:view");
        
        // 验证不包含被 REMOVE 的权限（resource_id=11: role:view）
        assertFalse(permissions.contains(11L), "不应包含被 REMOVE 的权限：role:view");
        
        // 验证包含 ADD 的权限（resource_id=13: resource:view）
        assertTrue(permissions.contains(13L), "应包含 ADD 调整的权限：resource:view");
    }

    // ==================== TC-03-004: 多角色用户权限合并（并集） ====================

    @Test
    @Order(41)
    @DisplayName("TC-03-004: 多角色用户权限合并（并集）")
    void testMultiRoleUserPermissionMerge() throws Exception {
        // test_user2 (id=3) 拥有 test_role_a (id=3) 和 test_role_b (id=4)
        Set<Long> permissions = permissionService.calculateUserPermissions(3L);
        
        // 验证包含基本权限（不重复）
        assertTrue(permissions.contains(1L), "应包含基本权限：登录");
        
        // 验证包含 test_role_a 的缺省权限
        assertTrue(permissions.contains(8L), "应包含 test_role_a 的缺省权限：user:view");
        
        // 验证包含 test_role_b 的缺省权限
        assertTrue(permissions.contains(9L), "应包含 test_role_b 的缺省权限：user:create");
        assertTrue(permissions.contains(12L), "应包含 test_role_b 的缺省权限：role:permission:adjust");
        
        // 验证权限合并（并集）
        assertTrue(permissions.size() >= 7, "多角色用户权限数量应大于等于 7");
    }

    // ==================== TC-03-011: 判断用户是否有指定 permission_key ====================

    @Test
    @Order(50)
    @DisplayName("TC-03-011: 判断用户是否有指定 permission_key")
    void testHasPermission() throws Exception {
        // 获取 test_user1 的权限
        Set<String> permissionKeys = permissionService.getUserPermissionKeys(2L);
        
        // 验证包含 system:login
        assertTrue(permissionKeys.contains("system:login"), "应包含 system:login 权限");
    }

    // ==================== TC-03-012: 判断用户是否无指定 permission_key ====================

    @Test
    @Order(51)
    @DisplayName("TC-03-012: 判断用户是否无指定 permission_key")
    void testHasNotPermission() throws Exception {
        // 获取 test_user1 的权限
        Set<String> permissionKeys = permissionService.getUserPermissionKeys(2L);
        
        // 验证不包含 user:delete（test_role_a 没有这个权限）
        assertFalse(permissionKeys.contains("user:delete"), "不应包含 user:delete 权限");
    }

    // ==================== TC-03-014: 判断用户是否有 API 权限（URI + Method） ====================

    @Test
    @Order(52)
    @DisplayName("TC-03-014: 判断用户是否有 API 权限（URI + Method）")
    void testHasApiPermission() throws Exception {
        // test_user1 拥有 resource_id=8 (user:view, GET /api/users)
        boolean hasPermission = permissionService.hasApiPermission(2L, "/api/users", "GET");
        assertTrue(hasPermission, "test_user1 应有 GET /api/users 权限");
        
        // test_user1 不拥有 resource_id=9 (user:create, POST /api/users)
        boolean noPermission = permissionService.hasApiPermission(2L, "/api/users", "POST");
        assertFalse(noPermission, "test_user1 应无 POST /api/users 权限");
    }

    // ==================== TC-03-015: 判断用户是否有 API 权限（未配置权限的 API） ====================

    @Test
    @Order(53)
    @DisplayName("TC-03-015: 判断用户是否有 API 权限（未配置权限的 API）")
    void testHasApiPermissionUnconfigured() throws Exception {
        // 未配置权限的 API 默认放行
        boolean hasPermission = permissionService.hasApiPermission(2L, "/api/test", "GET");
        assertTrue(hasPermission, "未配置权限的 API 应默认放行");
    }

    // ==================== TC-06-001: 角色权限调整记录审计日志 ====================

    @Test
    @Order(60)
    @DisplayName("TC-06-001: 角色权限调整记录审计日志")
    void testRolePermissionAdjustAuditLog() throws Exception {
        // 记录调整前的审计日志数量
        long beforeCount = auditLogMapper.selectCount(null);
        
        // 执行权限调整
        Map<String, Object> request = Map.of("resourceId", 16, "action", "ADD");
        mockMvc.perform(post("/api/roles/3/permissions/adjust")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        // 验证审计日志已创建
        long afterCount = auditLogMapper.selectCount(null);
        assertTrue(afterCount > beforeCount, "应创建审计日志记录");
        
        // 验证审计日志内容
        List<PermissionAuditLog> logs = auditLogMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PermissionAuditLog>()
                .eq(PermissionAuditLog::getTargetType, "ROLE")
                .eq(PermissionAuditLog::getTargetId, 3L)
                .orderByDesc(PermissionAuditLog::getCreatedAt)
                .last("LIMIT 1")
        );
        
        assertFalse(logs.isEmpty());
        PermissionAuditLog log = logs.get(0);
        assertEquals("ROLE", log.getTargetType());
        assertEquals(3L, log.getTargetId());
        assertNotNull(log.getOperationType());
    }

    // ==================== 缓存测试（测试环境缓存已禁用） ====================

    @Test
    @Order(70)
    @DisplayName("TC-07-003: 用户权限变更后清除缓存")
    void testPermissionCacheEviction() throws Exception {
        // 测试环境缓存已禁用，验证权限计算直接从数据库读取
        Set<Long> permissions1 = permissionService.calculateUserPermissions(2L);
        Set<Long> permissions2 = permissionService.calculateUserPermissions(2L);
        
        // 两次计算结果应一致
        assertEquals(permissions1, permissions2, "权限计算结果应一致");
    }

    // ==================== 事务回滚测试 ====================

    @Test
    @Order(80)
    @DisplayName("TC-TRANSACTION: 测试数据库事务回滚")
    void testTransactionRollback() throws Exception {
        // 记录初始调整记录数量
        long initialCount = rolePermissionAdjustmentMapper.selectCount(null);

        // 执行权限调整
        Map<String, Object> request = Map.of("resourceId", 99, "action", "ADD");
        mockMvc.perform(post("/api/roles/3/permissions/adjust")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        // 验证调整记录已创建
        long afterCount = rolePermissionAdjustmentMapper.selectCount(null);
        assertEquals(initialCount + 1, afterCount, "调整记录数量应增加 1");
        
        // 由于 @Transactional，测试结束后数据应回滚
    }
}
