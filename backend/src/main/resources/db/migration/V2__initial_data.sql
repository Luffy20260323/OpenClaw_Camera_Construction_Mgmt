-- V2: 初始数据
-- 说明：插入系统运行所需的基础数据
-- 执行时机：V1 表结构创建之后

-------------------------------------------------------------------------------
-- 1. 公司类型数据
-------------------------------------------------------------------------------
INSERT INTO company_types (id, type_name, description, is_system_protected) VALUES
    (1, '甲方', '建设单位', true),
    (2, '乙方', '施工单位', true),
    (3, '监理方', '监理单位', true),
    (4, '系统管理员', '系统管理', true)
ON CONFLICT (id) DO NOTHING;

-------------------------------------------------------------------------------
-- 2. 公司数据
-------------------------------------------------------------------------------
-- 系统管理员公司（必须存在，ID=1）
INSERT INTO companies (id, company_name, type_id, status, is_system_protected, allow_anonymous_register) VALUES
    (1, '北京其点技术服务有限公司', 4, 'active', true, false)
ON CONFLICT (id) DO NOTHING;

-- 示例公司（生产环境可根据需要删除或修改）
INSERT INTO companies (id, company_name, type_id, status, is_system_protected, allow_anonymous_register) VALUES
    (2, '示例甲方公司', 1, 'active', false, true),
    (3, '示例乙方公司', 2, 'active', false, true),
    (4, '示例监理公司', 3, 'active', false, true)
ON CONFLICT (id) DO NOTHING;

-------------------------------------------------------------------------------
-- 3. 角色数据
-------------------------------------------------------------------------------
-- 系统管理员角色
INSERT INTO roles (id, role_name, role_code, role_description, company_type_id, is_system_protected) VALUES
    (1, '系统管理员', 'ROLE_SYSTEM_ADMIN', '系统超级管理员', 4, true)
ON CONFLICT (id) DO NOTHING;

-- 甲方角色
INSERT INTO roles (id, role_name, role_code, role_description, company_type_id, is_system_protected) VALUES
    (2, '甲方管理员', 'ROLE_JIAFANG_ADMIN', '甲方公司管理员', 1, false),
    (3, '甲方项目经理', 'ROLE_JIAFANG_PM', '甲方项目经理', 1, false),
    (4, '甲方普通用户', 'ROLE_JIAFANG_USER', '甲方普通用户', 1, false)
ON CONFLICT (id) DO NOTHING;

-- 乙方角色
INSERT INTO roles (id, role_name, role_code, role_description, company_type_id, is_system_protected) VALUES
    (5, '乙方管理员', 'ROLE_YIFANG_ADMIN', '乙方公司管理员', 2, false),
    (6, '乙方项目经理', 'ROLE_YIFANG_PM', '乙方项目经理', 2, false),
    (7, '乙方普通用户', 'ROLE_YIFANG_USER', '乙方普通用户', 2, false)
ON CONFLICT (id) DO NOTHING;

-- 监理方角色
INSERT INTO roles (id, role_name, role_code, role_description, company_type_id, is_system_protected) VALUES
    (8, '监理方管理员', 'ROLE_JIANLIFANG_ADMIN', '监理方公司管理员', 3, false),
    (9, '监理工程师', 'ROLE_JIANLIFANG_ENGINEER', '监理工程师', 3, false),
    (10, '监理方普通用户', 'ROLE_JIANLIFANG_USER', '监理方普通用户', 3, false)
ON CONFLICT (id) DO NOTHING;

-------------------------------------------------------------------------------
-- 4. 用户数据
-------------------------------------------------------------------------------
-- 系统管理员用户
-- 用户名：admin
-- 密码：Admin@2026
-- BCrypt 哈希生成方法：BCrypt.hashpw("Admin@2026", BCrypt.gensalt(10))
INSERT INTO users (id, username, password_hash, real_name, email, phone, company_id, approval_status, status, is_system_protected) VALUES
    (1, 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '系统管理员', 'admin@qidian.com', '13800000000', 1, 1, 1, true)
ON CONFLICT (id) DO NOTHING;

-- 分配系统管理员角色
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1)
ON CONFLICT (user_id, role_id) DO NOTHING;

-------------------------------------------------------------------------------
-- 5. 系统配置数据
-------------------------------------------------------------------------------
INSERT INTO system_configs (config_key, config_value, description) VALUES
    ('captcha-type', 'image', '验证码类型：image 图片验证码，sms 短信验证码'),
    ('anonymous-register-enabled', 'true', '是否允许匿名注册'),
    ('system-version', 'v1.0.0', '系统版本号')
ON CONFLICT (config_key) DO NOTHING;

-------------------------------------------------------------------------------
-- 6. 作业区示例数据（可选）
-------------------------------------------------------------------------------
INSERT INTO work_areas (id, area_name, area_code, company_id, description, status) VALUES
    (1, '示例作业区 A', 'AREA-A', 2, '甲方示例作业区', 'active'),
    (2, '示例作业区 B', 'AREA-B', 3, '乙方示例作业区', 'active')
ON CONFLICT (id) DO NOTHING;

-------------------------------------------------------------------------------
-- 数据验证查询（注释掉，仅用于调试）
-------------------------------------------------------------------------------
-- SELECT '公司类型' as table_name, COUNT(*) as count FROM company_types;
-- SELECT '公司' as table_name, COUNT(*) as count FROM companies;
-- SELECT '角色' as table_name, COUNT(*) as count FROM roles;
-- SELECT '用户' as table_name, COUNT(*) as count FROM users;
-- SELECT '系统配置' as table_name, COUNT(*) as count FROM system_configs;
