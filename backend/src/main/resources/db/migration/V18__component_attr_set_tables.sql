-- =====================================================
-- V18__component_attr_set_tables.sql
-- 零部件属性集表结构
-- 创建时间：2026-04-01
-- =====================================================

-- -----------------------------------------------------
-- 零部件属性集表 (component_attr_sets)
-- -----------------------------------------------------
CREATE TABLE component_attr_sets (
    id BIGSERIAL PRIMARY KEY,
    component_type_id BIGINT NOT NULL,          -- 关联零部件种类
    name VARCHAR(100) NOT NULL,                 -- 属性集名称
    code VARCHAR(50) NOT NULL,                  -- 属性集编码
    description TEXT,                            -- 描述
    sequence_no INT DEFAULT 10,                 -- 显示序号
    is_active BOOLEAN DEFAULT true,             -- 是否启用
    created_by BIGINT,                          -- 创建人
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_attr_set_type FOREIGN KEY (component_type_id) 
        REFERENCES component_types(id) ON DELETE CASCADE
);

COMMENT ON TABLE component_attr_sets IS '零部件属性集表';
COMMENT ON COLUMN component_attr_sets.id IS '属性集 ID';
COMMENT ON COLUMN component_attr_sets.component_type_id IS '关联零部件种类 ID';
COMMENT ON COLUMN component_attr_sets.name IS '属性集名称：如"9 米监控杆属性集"';
COMMENT ON COLUMN component_attr_sets.code IS '属性集编码';
COMMENT ON COLUMN component_attr_sets.description IS '描述';
COMMENT ON COLUMN component_attr_sets.sequence_no IS '显示序号，用于前端排序';
COMMENT ON COLUMN component_attr_sets.is_active IS '是否启用';
COMMENT ON COLUMN component_attr_sets.created_by IS '创建人 ID';
COMMENT ON COLUMN component_attr_sets.created_at IS '创建时间';
COMMENT ON COLUMN component_attr_sets.updated_at IS '更新时间';

-- 创建索引
CREATE INDEX idx_attr_sets_type ON component_attr_sets(component_type_id);
CREATE INDEX idx_attr_sets_code ON component_attr_sets(code);
CREATE INDEX idx_attr_sets_active ON component_attr_sets(is_active);

-- -----------------------------------------------------
-- 属性集属性定义表 (component_attr_set_attrs)
-- -----------------------------------------------------
CREATE TABLE component_attr_set_attrs (
    id BIGSERIAL PRIMARY KEY,
    attr_set_id BIGINT NOT NULL,                -- 关联属性集
    attr_name VARCHAR(100) NOT NULL,            -- 属性名称
    attr_code VARCHAR(50) NOT NULL,             -- 属性编码（数据库字段名）
    attr_type VARCHAR(20) NOT NULL,             -- 属性类型：TEXT/NUMBER/DATE/SELECT/MULTI_SELECT/FILE
    unit VARCHAR(50),                            -- 单位
    options JSONB,                               -- 选项值（用于 SELECT 类型）
    default_value TEXT,                          -- 默认值
    sequence_no INT DEFAULT 10,                 -- 显示序号
    is_required BOOLEAN DEFAULT false,          -- 是否必填
    validation_rule TEXT,                        -- 验证规则
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_attr_set FOREIGN KEY (attr_set_id) 
        REFERENCES component_attr_sets(id) ON DELETE CASCADE
);

COMMENT ON TABLE component_attr_set_attrs IS '属性集属性定义表';
COMMENT ON COLUMN component_attr_set_attrs.id IS '属性定义 ID';
COMMENT ON COLUMN component_attr_set_attrs.attr_set_id IS '关联属性集 ID';
COMMENT ON COLUMN component_attr_set_attrs.attr_name IS '属性名称：如"厂商"、"高度"';
COMMENT ON COLUMN component_attr_set_attrs.attr_code IS '属性编码：如"manufacturer"、"height"';
COMMENT ON COLUMN component_attr_set_attrs.attr_type IS '属性类型：TEXT/NUMBER/DATE/SELECT/MULTI_SELECT/FILE';
COMMENT ON COLUMN component_attr_set_attrs.unit IS '单位：如"米"、"kg"、"cm"';
COMMENT ON COLUMN component_attr_set_attrs.options IS '选项值 JSON：用于 SELECT 类型，如["选项 1","选项 2"]';
COMMENT ON COLUMN component_attr_set_attrs.default_value IS '默认值';
COMMENT ON COLUMN component_attr_set_attrs.sequence_no IS '显示序号';
COMMENT ON COLUMN component_attr_set_attrs.is_required IS '是否必填';
COMMENT ON COLUMN component_attr_set_attrs.validation_rule IS '验证规则';
COMMENT ON COLUMN component_attr_set_attrs.created_at IS '创建时间';

-- 创建索引
CREATE INDEX idx_attr_set_attrs_set ON component_attr_set_attrs(attr_set_id);
CREATE INDEX idx_attr_set_attrs_code ON component_attr_set_attrs(attr_code);

-- -----------------------------------------------------
-- 权限资源配置
-- -----------------------------------------------------

-- 插入权限资源
INSERT INTO permission_resources (resource_code, resource_name, resource_type, parent_code, sequence_no, is_active)
VALUES 
    ('component-attr-set', '零部件属性集管理', 'MENU', 'system', 30, true)
ON CONFLICT (resource_code) DO NOTHING;

-- 插入权限资源子项
INSERT INTO permission_resources (resource_code, resource_name, resource_type, parent_code, sequence_no, is_active)
VALUES 
    ('component-attr-set:view', '查看属性集', 'API', 'component-attr-set', 10, true),
    ('component-attr-set:create', '创建属性集', 'API', 'component-attr-set', 20, true),
    ('component-attr-set:edit', '编辑属性集', 'API', 'component-attr-set', 30, true),
    ('component-attr-set:delete', '删除属性集', 'API', 'component-attr-set', 40, true)
ON CONFLICT (resource_code) DO NOTHING;

-- 为系统管理员角色分配权限（role_code='ADMIN'）
INSERT INTO role_permissions (role_id, permission_resource_id)
SELECT r.id, pr.id
FROM roles r, permission_resources pr
WHERE r.role_code = 'ADMIN' 
  AND pr.resource_code IN ('component-attr-set', 'component-attr-set:view', 'component-attr-set:create', 'component-attr-set:edit', 'component-attr-set:delete')
ON CONFLICT DO NOTHING;
