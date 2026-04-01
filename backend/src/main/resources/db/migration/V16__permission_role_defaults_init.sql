-- ============================================================================
-- V15: 角色权限初始化 - 基本权限与缺省权限配置
-- ============================================================================
-- 创建时间：2026-04-01 10:15
-- 说明：为所有角色分配基本权限，并配置各角色的缺省权限
-- 设计文档：docs/permission-design-spec.md
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 步骤 1：定义基本权限资源（确保存在）
-- ----------------------------------------------------------------------------
-- 基本权限：所有角色必须具备的最小权限集合

INSERT INTO resource (name, code, type, permission_key, is_basic, status, created_at) VALUES
    ('登录', 'system:login', 'API', 'system:login', 1, 1, NOW()),
    ('退出登录', 'system:logout', 'API', 'system:logout', 1, 1, NOW()),
    ('查看个人资料', 'profile:view', 'API', 'profile:view', 1, 1, NOW()),
    ('修改个人资料', 'profile:edit', 'API', 'profile:edit', 1, 1, NOW()),
    ('修改密码', 'profile:password', 'API', 'profile:password', 1, 1, NOW())
ON CONFLICT (code) DO UPDATE SET 
    is_basic = 1, 
    status = 1,
    updated_at = NOW();

-- ----------------------------------------------------------------------------
-- 步骤 2：为所有角色分配基本权限
-- ----------------------------------------------------------------------------
-- 基本权限是所有角色的基础，不可删除

DO $$
DECLARE
    r RECORD;
    basic_resource_ids BIGINT[];
BEGIN
    -- 获取基本权限资源 ID 列表
    SELECT ARRAY_AGG(id) INTO basic_resource_ids 
    FROM resource 
    WHERE is_basic = 1;
    
    -- 为每个角色分配基本权限
    FOR r IN SELECT id FROM roles LOOP
        FOR i IN 1..ARRAY_LENGTH(basic_resource_ids, 1) LOOP
            INSERT INTO role_permission (role_id, resource_id, permission_type, created_by)
            VALUES (r.id, basic_resource_ids[i], 'basic', 0)
            ON CONFLICT (role_id, resource_id) DO UPDATE SET
                permission_type = 'basic',
                updated_at = NOW();
        END LOOP;
    END LOOP;
    
    RAISE NOTICE '基本权限分配完成：所有角色已分配 % 个基本权限', array_length(basic_resource_ids, 1);
END $$;

-- ----------------------------------------------------------------------------
-- 步骤 3：定义角色缺省权限配置
-- ----------------------------------------------------------------------------
-- 缺省权限：角色创建时预设的权限集合
-- 不同角色类型有不同的缺省权限

-- 3.1 系统管理员角色缺省权限（拥有所有管理权限）
DO $$
DECLARE
    v_role_id BIGINT;
BEGIN
    SELECT id INTO v_role_id FROM roles WHERE role_code = 'ROLE_SYSTEM_ADMIN';
    
    IF v_role_id IS NOT NULL THEN
        -- 系统管理员拥有所有模块的查看和管理权限
        INSERT INTO role_permission (role_id, resource_id, permission_type, created_by)
        SELECT v_role_id, r.id, 'default', 0
        FROM resource r
        WHERE r.type IN ('MODULE', 'MENU', 'ELEMENT', 'API')
          AND r.status = 1
        ON CONFLICT (role_id, resource_id) DO UPDATE SET
            permission_type = 'default',
            updated_at = NOW();
        
        RAISE NOTICE '系统管理员缺省权限配置完成';
    END IF;
END $$;

-- 3.2 甲方管理员角色缺省权限
DO $$
DECLARE
    v_role_id BIGINT;
BEGIN
    SELECT id INTO v_role_id FROM roles WHERE role_code = 'ROLE_JIAFANG_ADMIN';
    
    IF v_role_id IS NOT NULL THEN
        -- 甲方管理员：查看和管理公司、作业区、用户
        INSERT INTO role_permission (role_id, resource_id, permission_type, created_by)
        SELECT v_role_id, r.id, 'default', 0
        FROM resource r
        WHERE r.code IN (
            -- 模块权限
            'module-company', 'module-workarea', 'module-user',
            -- 菜单权限
            'menu-company-list', 'menu-workarea-list', 'menu-user-list', 'menu-user-pending',
            -- 元素权限 - 公司
            'element-company-create', 'element-company-edit', 'element-company-delete',
            -- 元素权限 - 作业区
            'element-workarea-create', 'element-workarea-edit', 'element-workarea-delete',
            -- 元素权限 - 用户
            'element-user-create', 'element-user-edit', 'element-user-delete', 'element-user-approve',
            -- API 权限
            'api-company-list', 'api-company-detail', 'api-company-create', 'api-company-update',
            'api-workarea-list', 'api-workarea-detail', 'api-workarea-create', 'api-workarea-update',
            'api-user-list', 'api-user-detail', 'api-user-create', 'api-user-update', 'api-user-approve',
            'api-user-pending'
        )
        ON CONFLICT (role_id, resource_id) DO UPDATE SET
            permission_type = 'default',
            updated_at = NOW();
        
        RAISE NOTICE '甲方管理员缺省权限配置完成';
    END IF;
END $$;

-- 3.3 甲方项目经理角色缺省权限
DO $$
DECLARE
    v_role_id BIGINT;
BEGIN
    SELECT id INTO v_role_id FROM roles WHERE role_code = 'ROLE_JIAFANG_PM';
    
    IF v_role_id IS NOT NULL THEN
        -- 甲方项目经理：查看公司、作业区、用户，审批用户
        INSERT INTO role_permission (role_id, resource_id, permission_type, created_by)
        SELECT v_role_id, r.id, 'default', 0
        FROM resource r
        WHERE r.code IN (
            -- 模块权限
            'module-company', 'module-workarea', 'module-user',
            -- 菜单权限
            'menu-company-list', 'menu-workarea-list', 'menu-user-list', 'menu-user-pending',
            -- 元素权限 - 查看和审批
            'element-user-approve',
            -- API 权限
            'api-company-list', 'api-company-detail',
            'api-workarea-list', 'api-workarea-detail',
            'api-user-list', 'api-user-detail', 'api-user-approve', 'api-user-pending'
        )
        ON CONFLICT (role_id, resource_id) DO UPDATE SET
            permission_type = 'default',
            updated_at = NOW();
        
        RAISE NOTICE '甲方项目经理缺省权限配置完成';
    END IF;
END $$;

-- 3.4 甲方普通用户角色缺省权限
DO $$
DECLARE
    v_role_id BIGINT;
BEGIN
    SELECT id INTO v_role_id FROM roles WHERE role_code = 'ROLE_JIAFANG_USER';
    
    IF v_role_id IS NOT NULL THEN
        -- 甲方普通用户：仅查看权限
        INSERT INTO role_permission (role_id, resource_id, permission_type, created_by)
        SELECT v_role_id, r.id, 'default', 0
        FROM resource r
        WHERE r.code IN (
            -- 模块权限
            'module-company', 'module-workarea',
            -- 菜单权限
            'menu-company-list', 'menu-workarea-list',
            -- API 权限
            'api-company-list', 'api-company-detail',
            'api-workarea-list', 'api-workarea-detail',
            'api-user-profile', 'api-user-profile-update', 'api-user-password'
        )
        ON CONFLICT (role_id, resource_id) DO UPDATE SET
            permission_type = 'default',
            updated_at = NOW();
        
        RAISE NOTICE '甲方普通用户缺省权限配置完成';
    END IF;
END $$;

-- 3.5 乙方管理员角色缺省权限
DO $$
DECLARE
    v_role_id BIGINT;
BEGIN
    SELECT id INTO v_role_id FROM roles WHERE role_code = 'ROLE_YIFANG_ADMIN';
    
    IF v_role_id IS NOT NULL THEN
        -- 乙方管理员：查看和管理公司、作业区、用户
        INSERT INTO role_permission (role_id, resource_id, permission_type, created_by)
        SELECT v_role_id, r.id, 'default', 0
        FROM resource r
        WHERE r.code IN (
            -- 模块权限
            'module-company', 'module-workarea', 'module-user',
            -- 菜单权限
            'menu-company-list', 'menu-workarea-list', 'menu-user-list', 'menu-user-pending',
            -- 元素权限
            'element-company-create', 'element-company-edit', 'element-company-delete',
            'element-workarea-create', 'element-workarea-edit', 'element-workarea-delete',
            'element-user-create', 'element-user-edit', 'element-user-delete', 'element-user-approve',
            -- API 权限
            'api-company-list', 'api-company-detail', 'api-company-create', 'api-company-update',
            'api-workarea-list', 'api-workarea-detail', 'api-workarea-create', 'api-workarea-update',
            'api-user-list', 'api-user-detail', 'api-user-create', 'api-user-update', 'api-user-approve',
            'api-user-pending'
        )
        ON CONFLICT (role_id, resource_id) DO UPDATE SET
            permission_type = 'default',
            updated_at = NOW();
        
        RAISE NOTICE '乙方管理员缺省权限配置完成';
    END IF;
END $$;

-- 3.6 乙方项目经理角色缺省权限
DO $$
DECLARE
    v_role_id BIGINT;
BEGIN
    SELECT id INTO v_role_id FROM roles WHERE role_code = 'ROLE_YIFANG_PM';
    
    IF v_role_id IS NOT NULL THEN
        -- 乙方项目经理：查看公司、作业区、用户，审批用户
        INSERT INTO role_permission (role_id, resource_id, permission_type, created_by)
        SELECT v_role_id, r.id, 'default', 0
        FROM resource r
        WHERE r.code IN (
            -- 模块权限
            'module-company', 'module-workarea', 'module-user',
            -- 菜单权限
            'menu-company-list', 'menu-workarea-list', 'menu-user-list', 'menu-user-pending',
            -- 元素权限
            'element-user-approve',
            -- API 权限
            'api-company-list', 'api-company-detail',
            'api-workarea-list', 'api-workarea-detail',
            'api-user-list', 'api-user-detail', 'api-user-approve', 'api-user-pending'
        )
        ON CONFLICT (role_id, resource_id) DO UPDATE SET
            permission_type = 'default',
            updated_at = NOW();
        
        RAISE NOTICE '乙方项目经理缺省权限配置完成';
    END IF;
END $$;

-- 3.7 乙方普通用户角色缺省权限
DO $$
DECLARE
    v_role_id BIGINT;
BEGIN
    SELECT id INTO v_role_id FROM roles WHERE role_code = 'ROLE_YIFANG_USER';
    
    IF v_role_id IS NOT NULL THEN
        -- 乙方普通用户：仅查看权限
        INSERT INTO role_permission (role_id, resource_id, permission_type, created_by)
        SELECT v_role_id, r.id, 'default', 0
        FROM resource r
        WHERE r.code IN (
            -- 模块权限
            'module-company', 'module-workarea',
            -- 菜单权限
            'menu-company-list', 'menu-workarea-list',
            -- API 权限
            'api-company-list', 'api-company-detail',
            'api-workarea-list', 'api-workarea-detail',
            'api-user-profile', 'api-user-profile-update', 'api-user-password'
        )
        ON CONFLICT (role_id, resource_id) DO UPDATE SET
            permission_type = 'default',
            updated_at = NOW();
        
        RAISE NOTICE '乙方普通用户缺省权限配置完成';
    END IF;
END $$;

-- 3.8 监理方管理员角色缺省权限
DO $$
DECLARE
    v_role_id BIGINT;
BEGIN
    SELECT id INTO v_role_id FROM roles WHERE role_code = 'ROLE_JIANLIFANG_ADMIN';
    
    IF v_role_id IS NOT NULL THEN
        -- 监理方管理员：查看和管理公司、作业区、用户
        INSERT INTO role_permission (role_id, resource_id, permission_type, created_by)
        SELECT v_role_id, r.id, 'default', 0
        FROM resource r
        WHERE r.code IN (
            -- 模块权限
            'module-company', 'module-workarea', 'module-user',
            -- 菜单权限
            'menu-company-list', 'menu-workarea-list', 'menu-user-list', 'menu-user-pending',
            -- 元素权限
            'element-company-create', 'element-company-edit', 'element-company-delete',
            'element-workarea-create', 'element-workarea-edit', 'element-workarea-delete',
            'element-user-create', 'element-user-edit', 'element-user-delete', 'element-user-approve',
            -- API 权限
            'api-company-list', 'api-company-detail', 'api-company-create', 'api-company-update',
            'api-workarea-list', 'api-workarea-detail', 'api-workarea-create', 'api-workarea-update',
            'api-user-list', 'api-user-detail', 'api-user-create', 'api-user-update', 'api-user-approve',
            'api-user-pending'
        )
        ON CONFLICT (role_id, resource_id) DO UPDATE SET
            permission_type = 'default',
            updated_at = NOW();
        
        RAISE NOTICE '监理方管理员缺省权限配置完成';
    END IF;
END $$;

-- 3.9 监理工程师角色缺省权限
DO $$
DECLARE
    v_role_id BIGINT;
BEGIN
    SELECT id INTO v_role_id FROM roles WHERE role_code = 'ROLE_JIANLIFANG_ENGINEER';
    
    IF v_role_id IS NOT NULL THEN
        -- 监理工程师：查看公司、作业区、用户，审批用户
        INSERT INTO role_permission (role_id, resource_id, permission_type, created_by)
        SELECT v_role_id, r.id, 'default', 0
        FROM resource r
        WHERE r.code IN (
            -- 模块权限
            'module-company', 'module-workarea', 'module-user',
            -- 菜单权限
            'menu-company-list', 'menu-workarea-list', 'menu-user-list', 'menu-user-pending',
            -- 元素权限
            'element-user-approve',
            -- API 权限
            'api-company-list', 'api-company-detail',
            'api-workarea-list', 'api-workarea-detail',
            'api-user-list', 'api-user-detail', 'api-user-approve', 'api-user-pending'
        )
        ON CONFLICT (role_id, resource_id) DO UPDATE SET
            permission_type = 'default',
            updated_at = NOW();
        
        RAISE NOTICE '监理工程师缺省权限配置完成';
    END IF;
END $$;

-- 3.10 监理方普通用户角色缺省权限
DO $$
DECLARE
    v_role_id BIGINT;
BEGIN
    SELECT id INTO v_role_id FROM roles WHERE role_code = 'ROLE_JIANLIFANG_USER';
    
    IF v_role_id IS NOT NULL THEN
        -- 监理方普通用户：仅查看权限
        INSERT INTO role_permission (role_id, resource_id, permission_type, created_by)
        SELECT v_role_id, r.id, 'default', 0
        FROM resource r
        WHERE r.code IN (
            -- 模块权限
            'module-company', 'module-workarea',
            -- 菜单权限
            'menu-company-list', 'menu-workarea-list',
            -- API 权限
            'api-company-list', 'api-company-detail',
            'api-workarea-list', 'api-workarea-detail',
            'api-user-profile', 'api-user-profile-update', 'api-user-password'
        )
        ON CONFLICT (role_id, resource_id) DO UPDATE SET
            permission_type = 'default',
            updated_at = NOW();
        
        RAISE NOTICE '监理方普通用户缺省权限配置完成';
    END IF;
END $$;

-- ----------------------------------------------------------------------------
-- 步骤 4：记录权限审计日志
-- ----------------------------------------------------------------------------

INSERT INTO permission_audit_log (
    operator_id, operator_name, operation_type, target_type, target_id, 
    target_name, change_reason, source_type
) VALUES (
    0, 'SYSTEM', 'INITIALIZATION', 'ROLE_PERMISSION', 0, 
    'role_default_permissions', '角色缺省权限初始化 - V15 迁移', 'SYSTEM'
);

-- ----------------------------------------------------------------------------
-- 步骤 5：统计并输出结果
-- ----------------------------------------------------------------------------

DO $$
DECLARE
    v_role_count INTEGER;
    v_basic_count INTEGER;
    v_default_count INTEGER;
    v_total_count INTEGER;
BEGIN
    SELECT COUNT(DISTINCT role_id) INTO v_role_count FROM role_permission;
    SELECT COUNT(*) INTO v_basic_count FROM role_permission WHERE permission_type = 'basic';
    SELECT COUNT(*) INTO v_default_count FROM role_permission WHERE permission_type = 'default';
    SELECT COUNT(*) INTO v_total_count FROM role_permission;
    
    RAISE NOTICE '========================================';
    RAISE NOTICE ' 角色权限初始化完成！';
    RAISE NOTICE '========================================';
    RAISE NOTICE ' 配置角色数：%', v_role_count;
    RAISE NOTICE ' 基本权限记录数：%', v_basic_count;
    RAISE NOTICE ' 缺省权限记录数：%', v_default_count;
    RAISE NOTICE ' 总权限记录数：%', v_total_count;
    RAISE NOTICE '========================================';
END $$;
