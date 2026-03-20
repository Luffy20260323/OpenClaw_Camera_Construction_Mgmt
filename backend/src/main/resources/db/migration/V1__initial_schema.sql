-- V1: 初始数据库表结构
-- 说明：仅创建表结构和索引，不插入数据
-- 执行时机：数据库首次初始化

-------------------------------------------------------------------------------
-- 1. 公司类型表
-------------------------------------------------------------------------------
CREATE TABLE company_types (
    id BIGSERIAL PRIMARY KEY,
    type_name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    is_system_protected BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-------------------------------------------------------------------------------
-- 2. 公司表
-------------------------------------------------------------------------------
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

-------------------------------------------------------------------------------
-- 3. 角色表
-------------------------------------------------------------------------------
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    role_name VARCHAR(100) NOT NULL,
    role_code VARCHAR(100) NOT NULL UNIQUE,
    role_description TEXT,
    company_type_id BIGINT NOT NULL REFERENCES company_types(id),
    is_system_protected BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-------------------------------------------------------------------------------
-- 4. 用户表
-------------------------------------------------------------------------------
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

-------------------------------------------------------------------------------
-- 5. 用户角色关联表
-------------------------------------------------------------------------------
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-------------------------------------------------------------------------------
-- 6. 作业区表
-------------------------------------------------------------------------------
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

-------------------------------------------------------------------------------
-- 7. 系统配置表
-------------------------------------------------------------------------------
CREATE TABLE system_configs (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-------------------------------------------------------------------------------
-- 8. 索引创建
-------------------------------------------------------------------------------
CREATE INDEX idx_users_company ON users(company_id);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_companies_type ON companies(type_id);
CREATE INDEX idx_roles_company_type ON roles(company_type_id);
CREATE INDEX idx_work_areas_company ON work_areas(company_id);
