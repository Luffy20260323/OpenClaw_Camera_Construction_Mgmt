-- 用户管理功能测试 SQL 脚本
-- 用于初始化测试数据

-- 1. 插入公司类型（如果不存在）
INSERT INTO company_types (id, type_name, description, is_system_protected, created_at, updated_at)
VALUES 
    (1, '甲方', '建设单位', true, NOW(), NOW()),
    (2, '乙方', '施工单位', true, NOW(), NOW()),
    (3, '监理方', '监理单位', true, NOW(), NOW()),
    (4, '系统管理员', '系统管理', true, NOW(), NOW())
ON DUPLICATE KEY UPDATE type_name = VALUES(type_name);

-- 2. 插入测试公司
INSERT INTO companies (id, company_name, type_id, status, is_system_protected, created_at, updated_at)
VALUES 
    (1, '甲方测试公司', 1, 'active', false, NOW(), NOW()),
    (2, '乙方测试公司', 2, 'active', false, NOW(), NOW()),
    (3, '监理方测试公司', 3, 'active', false, NOW(), NOW()),
    (4, '系统管理', 4, 'active', true, NOW(), NOW())
ON DUPLICATE KEY UPDATE company_name = VALUES(company_name);

-- 3. 插入测试角色
INSERT INTO roles (id, role_name, role_code, role_description, company_type_id, is_system_protected, created_at)
VALUES 
    -- 系统管理员角色
    (1, '系统管理员', 'ROLE_SYSTEM_ADMIN', '系统超级管理员', 4, true, NOW()),
    
    -- 甲方角色
    (2, '甲方管理员', 'ROLE_JIAFANG_ADMIN', '甲方公司管理员', 1, false, NOW()),
    (3, '甲方项目经理', 'ROLE_JIAFANG_PM', '甲方项目经理', 1, false, NOW()),
    (4, '甲方普通用户', 'ROLE_JIAFANG_USER', '甲方普通用户', 1, false, NOW()),
    
    -- 乙方角色
    (5, '乙方管理员', 'ROLE_YIFANG_ADMIN', '乙方公司管理员', 2, false, NOW()),
    (6, '乙方项目经理', 'ROLE_YIFANG_PM', '乙方项目经理', 2, false, NOW()),
    (7, '乙方普通用户', 'ROLE_YIFANG_USER', '乙方普通用户', 2, false, NOW()),
    
    -- 监理方角色
    (8, '监理方管理员', 'ROLE_JIANLIFANG_ADMIN', '监理方公司管理员', 3, false, NOW()),
    (9, '监理工程师', 'ROLE_JIANLIFANG_ENGINEER', '监理工程师', 3, false, NOW()),
    (10, '监理方普通用户', 'ROLE_JIANLIFANG_USER', '监理方普通用户', 3, false, NOW())
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name);

-- 4. 创建系统管理员用户（用于测试）
-- 密码：123456 (BCrypt 加密)
INSERT INTO users (id, username, password_hash, real_name, email, phone, company_id, approval_status, status, is_system_protected, created_at, updated_at)
VALUES 
    (1, 'system_admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iDJd1X6M6JkQmJ5K5z5Z5Z5Z5Z5Z', '系统管理员', 'system@example.com', '13800000000', 4, 1, 1, true, NOW(), NOW())
ON DUPLICATE KEY UPDATE username = VALUES(username);

-- 5. 给系统管理员分配角色
INSERT INTO user_roles (user_id, role_id)
VALUES (1, 1)
ON DUPLICATE KEY UPDATE user_id = VALUES(user_id);

-- 6. 测试场景 SQL

-- 场景 1：系统管理员创建乙方管理员
-- 使用系统管理员登录后，调用 POST /user 接口
-- 请求体：
-- {
--   "username": "yifang_admin",
--   "password": "123456",
--   "confirmPassword": "123456",
--   "realName": "乙方管理员",
--   "email": "yifang_admin@example.com",
--   "companyId": 2,
--   "roleIds": [5],
--   "autoApprove": true
-- }

-- 场景 2：乙方管理员创建乙方普通用户
-- 使用乙方管理员登录后，调用 POST /user 接口
-- 请求体：
-- {
--   "username": "yifang_user",
--   "password": "123456",
--   "confirmPassword": "123456",
--   "realName": "乙方用户",
--   "email": "yifang_user@example.com",
--   "companyId": 2,
--   "roleIds": [7],
--   "autoApprove": true
-- }

-- 场景 3：用户自主注册
-- 调用 POST /user/register 接口
-- 请求体：
-- {
--   "username": "zizhu_user",
--   "password": "123456",
--   "confirmPassword": "123456",
--   "realName": "自主注册用户",
--   "email": "zizhu@example.com",
--   "companyId": 2
-- }

-- 场景 4：乙方管理员审批待审批用户
-- 使用乙方管理员登录后，调用 POST /user/approve 接口
-- 请求体：
-- {
--   "userId": <刚注册的用户 ID>,
--   "approved": true
-- }

-- 场景 5：批量导入用户
-- 使用管理员登录后，调用 POST /user/batch-import 接口
-- 请求体：
-- {
--   "autoApprove": true,
--   "users": [
--     {
--       "username": "batch_user1",
--       "password": "123456",
--       "realName": "批量用户 1",
--       "email": "batch1@example.com",
--       "companyId": 2,
--       "roleIds": [7]
--     },
--     {
--       "username": "batch_user2",
--       "password": "123456",
--       "realName": "批量用户 2",
--       "email": "batch2@example.com",
--       "companyId": 2,
--       "roleIds": [7]
--     }
--   ]
-- }

-- 查询语句

-- 查询所有待审批用户
SELECT u.id, u.username, u.real_name, u.email, c.company_name, ct.type_name, u.approval_status, u.status
FROM users u
LEFT JOIN companies c ON u.company_id = c.id
LEFT JOIN company_types ct ON c.type_id = ct.id
WHERE u.approval_status = 0
ORDER BY u.created_at DESC;

-- 查询所有正常用户
SELECT u.id, u.username, u.real_name, u.email, c.company_name, ct.type_name, u.status
FROM users u
LEFT JOIN companies c ON u.company_id = c.id
LEFT JOIN company_types ct ON c.type_id = ct.id
WHERE u.status = 1
ORDER BY u.created_at DESC;

-- 查询用户及其角色
SELECT u.id, u.username, u.real_name, r.role_name, r.role_code
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.id
WHERE u.status = 1
ORDER BY u.id;

-- 清理测试数据（谨慎使用）
-- DELETE FROM user_roles WHERE user_id > 1;
-- DELETE FROM users WHERE id > 1;
-- DELETE FROM companies WHERE id > 4;
