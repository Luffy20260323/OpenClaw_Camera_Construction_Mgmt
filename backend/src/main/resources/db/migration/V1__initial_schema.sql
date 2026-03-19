-- V1: 初始数据库表结构

-- 公司类型表
CREATE TABLE company_types (
    id BIGSERIAL PRIMARY KEY,
    type_name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    is_system_protected BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 公司表
CREATE TABLE companies (
    id BIGSERIAL PRIMARY KEY,
    company_name VARCHAR(200) NOT NULL,
    type_id BIGINT NOT NULL REFERENCES company_types(id),
    unified_social_credit_code VARCHAR(50),
    contact_person VARCHAR(50),
    contact_phone VARCHAR(20),
    contact_email VARCHAR(100),
    address TEXT,
    status VARCHAR(20) DEFAULT 'active',
    description TEXT,
    is_system_protected BOOLEAN DEFAULT FALSE,
    allow_anonymous_register BOOLEAN DEFAULT FALSE,
    created_by BIGINT,
    updated_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 角色表
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    role_name VARCHAR(100) NOT NULL,
    role_code VARCHAR(100) NOT NULL UNIQUE,
    role_description TEXT,
    company_type_id BIGINT NOT NULL REFERENCES company_types(id),
    is_system_protected BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 用户表
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    real_name VARCHAR(50),
    gender INTEGER DEFAULT 0,
    company_id BIGINT REFERENCES companies(id),
    approval_status INTEGER DEFAULT 0,
    approved_by BIGINT,
    approved_at TIMESTAMP,
    rejection_reason TEXT,
    status INTEGER DEFAULT 0,
    is_system_protected BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 用户角色关联表
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- 作业区表
CREATE TABLE work_areas (
    id BIGSERIAL PRIMARY KEY,
    area_name VARCHAR(100) NOT NULL,
    area_code VARCHAR(50),
    company_id BIGINT REFERENCES companies(id),
    description TEXT,
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 系统配置表
CREATE TABLE system_configs (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX idx_users_company ON users(company_id);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_companies_type ON companies(type_id);
CREATE INDEX idx_roles_company_type ON roles(company_type_id);
CREATE INDEX idx_work_areas_company ON work_areas(company_id);

-- 插入初始数据

-- 公司类型
INSERT INTO company_types (id, type_name, description, is_system_protected) VALUES
    (1, '甲方', '建设单位', true),
    (2, '乙方', '施工单位', true),
    (3, '监理方', '监理单位', true),
    (4, '系统管理员', '系统管理', true);

-- 系统保护公司
INSERT INTO companies (id, company_name, type_id, status, is_system_protected, allow_anonymous_register) VALUES
    (1, '北京其点技术服务有限公司', 4, 'active', true, false);

-- 系统角色
INSERT INTO roles (id, role_name, role_code, role_description, company_type_id, is_system_protected) VALUES
    (1, '系统管理员', 'ROLE_SYSTEM_ADMIN', '系统超级管理员', 4, true),
    (2, '甲方管理员', 'ROLE_JIAFANG_ADMIN', '甲方公司管理员', 1, false),
    (3, '甲方项目经理', 'ROLE_JIAFANG_PM', '甲方项目经理', 1, false),
    (4, '甲方普通用户', 'ROLE_JIAFANG_USER', '甲方普通用户', 1, false),
    (5, '乙方管理员', 'ROLE_YIFANG_ADMIN', '乙方公司管理员', 2, false),
    (6, '乙方项目经理', 'ROLE_YIFANG_PM', '乙方项目经理', 2, false),
    (7, '乙方普通用户', 'ROLE_YIFANG_USER', '乙方普通用户', 2, false),
    (8, '监理方管理员', 'ROLE_JIANLIFANG_ADMIN', '监理方公司管理员', 3, false),
    (9, '监理工程师', 'ROLE_JIANLIFANG_ENGINEER', '监理工程师', 3, false),
    (10, '监理方普通用户', 'ROLE_JIANLIFANG_USER', '监理方普通用户', 3, false);

-- 系统管理员用户 (密码：Admin@2026)
INSERT INTO users (id, username, password_hash, real_name, email, phone, company_id, approval_status, status, is_system_protected) VALUES
    (1, 'admin', '$2a$10$rKOxQlPg5Z5Z5Z5Z5Z5Z5.5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5', '系统管理员', 'admin@qidian.com', '13800000000', 1, 1, 1, true);

-- 分配系统管理员角色
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);

-- 系统配置
INSERT INTO system_configs (config_key, config_value, description) VALUES
    ('captcha-type', 'image', '验证码类型：image 图片验证码，sms 短信验证码'),
    ('anonymous-register-enabled', 'true', '是否允许匿名注册'),
    ('system-version', 'v1.1.0', '系统版本号');
