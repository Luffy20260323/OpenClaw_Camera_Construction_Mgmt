-- ============================================================================
-- V4: 添加菜单权限管理系统
-- ============================================================================

-- 1. 菜单定义表
CREATE TABLE IF NOT EXISTS menus (
    id BIGSERIAL PRIMARY KEY,
    menu_code VARCHAR(50) NOT NULL UNIQUE,
    menu_name VARCHAR(100) NOT NULL,
    menu_path VARCHAR(255),
    parent_id BIGINT REFERENCES menus(id),
    sort_order INT DEFAULT 0,
    icon VARCHAR(50),
    is_visible BOOLEAN DEFAULT TRUE,
    required_permission VARCHAR(100),
    description VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_system_protected BOOLEAN DEFAULT FALSE
);

-- 2. 角色 - 菜单权限关联表（角色默认菜单权限）
CREATE TABLE IF NOT EXISTS role_menu_permissions (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    menu_id BIGINT NOT NULL REFERENCES menus(id) ON DELETE CASCADE,
    can_view BOOLEAN DEFAULT TRUE,
    can_operate BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(role_id, menu_id)
);

-- 3. 用户 - 菜单权限关联表（系统管理员可自定义）
CREATE TABLE IF NOT EXISTS user_menu_permissions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    menu_id BIGINT NOT NULL REFERENCES menus(id) ON DELETE CASCADE,
    can_view BOOLEAN DEFAULT TRUE,
    can_operate BOOLEAN DEFAULT TRUE,
    granted_by BIGINT REFERENCES users(id),
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, menu_id)
);

-- 4. 插入系统菜单
INSERT INTO menus (menu_code, menu_name, menu_path, parent_id, sort_order, icon, required_permission, is_system_protected) VALUES
    ('profile', '个人中心', '/user/profile', NULL, 1, 'User', NULL, TRUE),
    ('user_management', '用户管理', '/user/management', NULL, 2, 'Management', 'user:manage', FALSE),
    ('role_management', '角色管理', '/role', NULL, 3, 'Setting', 'role:manage', FALSE),
    ('workarea_management', '作业区管理', '/workarea', NULL, 4, 'Location', 'workarea:manage', FALSE),
    ('company_management', '公司管理', '/company', NULL, 5, 'Office', 'company:manage', FALSE),
    ('system_config', '系统管理', '/system/config', NULL, 6, 'Setting', 'system:config', FALSE)
ON CONFLICT (menu_code) DO NOTHING;

-- 5. 为系统管理员角色分配所有菜单权限
INSERT INTO role_menu_permissions (role_id, menu_id, can_view, can_operate)
SELECT 1, m.id, TRUE, TRUE
FROM menus m
ON CONFLICT (role_id, menu_id) DO NOTHING;

-- 6. 为甲方管理员角色分配用户管理权限
INSERT INTO role_menu_permissions (role_id, menu_id, can_view, can_operate)
SELECT 2, m.id, TRUE, TRUE
FROM menus m
WHERE m.menu_code IN ('profile', 'user_management')
ON CONFLICT (role_id, menu_id) DO NOTHING;

-- 7. 为乙方管理员角色分配用户管理权限
INSERT INTO role_menu_permissions (role_id, menu_id, can_view, can_operate)
SELECT 5, m.id, TRUE, TRUE
FROM menus m
WHERE m.menu_code IN ('profile', 'user_management')
ON CONFLICT (role_id, menu_id) DO NOTHING;

-- 8. 为监理方管理员角色分配用户管理权限
INSERT INTO role_menu_permissions (role_id, menu_id, can_view, can_operate)
SELECT 8, m.id, TRUE, TRUE
FROM menus m
WHERE m.menu_code IN ('profile', 'user_management')
ON CONFLICT (role_id, menu_id) DO NOTHING;

-- 9. 为其他角色（普通用户）仅分配个人中心权限
-- 甲方普通用户 (ROLE_JIAFANG_USER, role_id=4)
INSERT INTO role_menu_permissions (role_id, menu_id, can_view, can_operate)
SELECT 4, m.id, TRUE, FALSE
FROM menus m
WHERE m.menu_code = 'profile'
ON CONFLICT (role_id, menu_id) DO NOTHING;

-- 乙方普通用户 (ROLE_YIFANG_USER, role_id=7)
INSERT INTO role_menu_permissions (role_id, menu_id, can_view, can_operate)
SELECT 7, m.id, TRUE, FALSE
FROM menus m
WHERE m.menu_code = 'profile'
ON CONFLICT (role_id, menu_id) DO NOTHING;

-- 监理方普通用户 (ROLE_JIANLIFANG_USER, role_id=10)
INSERT INTO role_menu_permissions (role_id, menu_id, can_view, can_operate)
SELECT 10, m.id, TRUE, FALSE
FROM menus m
WHERE m.menu_code = 'profile'
ON CONFLICT (role_id, menu_id) DO NOTHING;

-- 甲方项目经理 (ROLE_JIAFANG_PM, role_id=3)
INSERT INTO role_menu_permissions (role_id, menu_id, can_view, can_operate)
SELECT 3, m.id, TRUE, FALSE
FROM menus m
WHERE m.menu_code = 'profile'
ON CONFLICT (role_id, menu_id) DO NOTHING;

-- 乙方项目经理 (ROLE_YIFANG_PM, role_id=6)
INSERT INTO role_menu_permissions (role_id, menu_id, can_view, can_operate)
SELECT 6, m.id, TRUE, FALSE
FROM menus m
WHERE m.menu_code = 'profile'
ON CONFLICT (role_id, menu_id) DO NOTHING;

-- 监理工程师 (ROLE_JIANLIFANG_ENGINEER, role_id=9)
INSERT INTO role_menu_permissions (role_id, menu_id, can_view, can_operate)
SELECT 9, m.id, TRUE, FALSE
FROM menus m
WHERE m.menu_code = 'profile'
ON CONFLICT (role_id, menu_id) DO NOTHING;

-- 乙方材料管理员 (ROLE_YIFANG_MATERIAL_ADMIN, role_id=18)
INSERT INTO role_menu_permissions (role_id, menu_id, can_view, can_operate)
SELECT 18, m.id, TRUE, FALSE
FROM menus m
WHERE m.menu_code = 'profile'
ON CONFLICT (role_id, menu_id) DO NOTHING;

-- 9. 创建索引
CREATE INDEX IF NOT EXISTS idx_role_menu_role_id ON role_menu_permissions(role_id);
CREATE INDEX IF NOT EXISTS idx_role_menu_menu_id ON role_menu_permissions(menu_id);
CREATE INDEX IF NOT EXISTS idx_user_menu_user_id ON user_menu_permissions(user_id);
CREATE INDEX IF NOT EXISTS idx_user_menu_menu_id ON user_menu_permissions(menu_id);

-- 10. 添加注释
COMMENT ON TABLE menus IS '系统菜单定义表';
COMMENT ON TABLE role_menu_permissions IS '角色菜单权限关联表';
COMMENT ON TABLE user_menu_permissions IS '用户自定义菜单权限表（系统管理员可配置）';
