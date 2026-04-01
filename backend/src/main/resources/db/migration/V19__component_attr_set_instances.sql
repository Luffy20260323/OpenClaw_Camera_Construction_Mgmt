-- ============================================================================
-- V19: 零部件属性集实例表结构
-- ============================================================================
-- 创建时间：2026-04-01
-- 说明：创建零部件属性集实例表和零部件实例表
-- ============================================================================

-- 1. 创建零部件属性集实例表
CREATE TABLE IF NOT EXISTS component_attr_set_instances (
    id BIGSERIAL PRIMARY KEY,
    attr_set_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    attr_values JSONB NOT NULL,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_attr_set_instance FOREIGN KEY (attr_set_id) 
        REFERENCES component_attr_sets(id) ON DELETE CASCADE
);

-- 2. 创建零部件实例表
CREATE TABLE IF NOT EXISTS component_instances (
    id BIGSERIAL PRIMARY KEY,
    component_type_id BIGINT NOT NULL,
    attr_set_instance_id BIGINT,
    serial_number VARCHAR(100),
    status VARCHAR(20) DEFAULT 'normal',
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_instance_type FOREIGN KEY (component_type_id) 
        REFERENCES component_types(id),
    CONSTRAINT fk_instance_attr FOREIGN KEY (attr_set_instance_id) 
        REFERENCES component_attr_set_instances(id)
);

-- 3. 创建索引
CREATE INDEX IF NOT EXISTS idx_attr_set_instances_attr_set_id ON component_attr_set_instances(attr_set_id);
CREATE INDEX IF NOT EXISTS idx_component_instances_component_type_id ON component_instances(component_type_id);
CREATE INDEX IF NOT EXISTS idx_component_instances_attr_set_instance_id ON component_instances(attr_set_instance_id);
CREATE INDEX IF NOT EXISTS idx_attr_set_instances_attr_values ON component_attr_set_instances USING GIN (attr_values);

-- 4. 添加表注释
COMMENT ON TABLE component_attr_set_instances IS '零部件属性集实例表';
COMMENT ON COLUMN component_attr_set_instances.id IS '实例 ID';
COMMENT ON COLUMN component_attr_set_instances.attr_set_id IS '关联属性集';
COMMENT ON COLUMN component_attr_set_instances.name IS '实例名称';
COMMENT ON COLUMN component_attr_set_instances.attr_values IS '属性值 JSON';
COMMENT ON COLUMN component_attr_set_instances.created_by IS '创建人';
COMMENT ON COLUMN component_attr_set_instances.created_at IS '创建时间';
COMMENT ON COLUMN component_attr_set_instances.updated_at IS '更新时间';

COMMENT ON TABLE component_instances IS '零部件实例表';
COMMENT ON COLUMN component_instances.id IS '零部件实例 ID';
COMMENT ON COLUMN component_instances.component_type_id IS '零部件种类';
COMMENT ON COLUMN component_instances.attr_set_instance_id IS '属性集实例（可选）';
COMMENT ON COLUMN component_instances.serial_number IS '序列号';
COMMENT ON COLUMN component_instances.status IS '状态：normal/replaced/scrapped';
COMMENT ON COLUMN component_instances.created_by IS '创建人';
COMMENT ON COLUMN component_instances.created_at IS '创建时间';
COMMENT ON COLUMN component_instances.updated_at IS '更新时间';

-- 5. 权限资源配置
INSERT INTO permission_resources (resource_code, resource_name, resource_type, parent_code, sequence_no, is_active)
VALUES 
    ('system:attr-instance', '属性集实例管理', 'MENU', 'system', 40, true)
ON CONFLICT (resource_code) DO NOTHING;

INSERT INTO permission_resources (resource_code, resource_name, resource_type, parent_code, sequence_no, is_active)
VALUES 
    ('system:attr-instance:view', '查看属性集实例', 'API', 'system:attr-instance', 10, true),
    ('system:attr-instance:create', '创建属性集实例', 'API', 'system:attr-instance', 20, true),
    ('system:attr-instance:edit', '编辑属性集实例', 'API', 'system:attr-instance', 30, true),
    ('system:attr-instance:delete', '删除属性集实例', 'API', 'system:attr-instance', 40, true)
ON CONFLICT (resource_code) DO NOTHING;