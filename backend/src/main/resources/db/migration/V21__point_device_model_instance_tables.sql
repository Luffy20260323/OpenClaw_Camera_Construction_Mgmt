-- ============================================================================
-- V21: 点位设备模型实例表结构
-- ============================================================================
-- 创建时间：2026-04-01
-- 说明：创建点位设备模型实例表及实例项表，支持从模型实例化并关联具体零部件实例
-- ============================================================================

-- 1. 创建点位设备模型实例表
CREATE TABLE IF NOT EXISTS point_device_model_instances (
    id BIGSERIAL PRIMARY KEY,
    model_id BIGINT NOT NULL REFERENCES point_device_models(id),
    name VARCHAR(200) NOT NULL,
    code VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. 创建点位设备模型实例项表（关联具体零部件实例）
CREATE TABLE IF NOT EXISTS point_device_model_instance_items (
    id BIGSERIAL PRIMARY KEY,
    instance_id BIGINT NOT NULL REFERENCES point_device_model_instances(id) ON DELETE CASCADE,
    component_type_id BIGINT NOT NULL REFERENCES component_types(id),
    component_instance_id BIGINT NOT NULL,
    sequence_no INT DEFAULT 10,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. 创建索引
CREATE INDEX IF NOT EXISTS idx_point_device_model_instances_model_id ON point_device_model_instances(model_id);
CREATE INDEX IF NOT EXISTS idx_point_device_model_instances_code ON point_device_model_instances(code);
CREATE INDEX IF NOT EXISTS idx_point_device_model_instances_is_active ON point_device_model_instances(is_active);
CREATE INDEX IF NOT EXISTS idx_point_device_model_instance_items_instance_id ON point_device_model_instance_items(instance_id);
CREATE INDEX IF NOT EXISTS idx_point_device_model_instance_items_component_type_id ON point_device_model_instance_items(component_type_id);
CREATE INDEX IF NOT EXISTS idx_point_device_model_instance_items_component_instance_id ON point_device_model_instance_items(component_instance_id);

-- 4. 添加表注释
COMMENT ON TABLE point_device_model_instances IS '点位设备模型实例表：从点位设备模型实例化而来，包含具体的零部件实例 ID';
COMMENT ON COLUMN point_device_model_instances.id IS '实例 ID';
COMMENT ON COLUMN point_device_model_instances.model_id IS '关联的模型 ID';
COMMENT ON COLUMN point_device_model_instances.name IS '实例名称';
COMMENT ON COLUMN point_device_model_instances.code IS '实例编码';
COMMENT ON COLUMN point_device_model_instances.description IS '描述';
COMMENT ON COLUMN point_device_model_instances.is_active IS '是否启用';
COMMENT ON COLUMN point_device_model_instances.created_by IS '创建人';
COMMENT ON COLUMN point_device_model_instances.created_at IS '创建时间';
COMMENT ON COLUMN point_device_model_instances.updated_at IS '更新时间';

COMMENT ON TABLE point_device_model_instance_items IS '点位设备模型实例项表：关联具体的零部件实例';
COMMENT ON COLUMN point_device_model_instance_items.id IS '实例项 ID';
COMMENT ON COLUMN point_device_model_instance_items.instance_id IS '关联实例 ID';
COMMENT ON COLUMN point_device_model_instance_items.component_type_id IS '零部件种类 ID';
COMMENT ON COLUMN point_device_model_instance_items.component_instance_id IS '零部件实例 ID（具体的设备实例）';
COMMENT ON COLUMN point_device_model_instance_items.sequence_no IS '显示序号，用于排序';
COMMENT ON COLUMN point_device_model_instance_items.created_at IS '创建时间';
COMMENT ON COLUMN point_device_model_instance_items.updated_at IS '更新时间';

-- 5. 插入权限数据
INSERT INTO permissions (permission_code, permission_name, permission_type, resource_type, parent_id, sequence_no, is_active) VALUES
    ('system:point-device-model-instance:view', '点位设备模型实例查看', 'api', 'menu', 
     (SELECT id FROM permissions WHERE permission_code = 'system:point-device-model'), 10, TRUE),
    ('system:point-device-model-instance:create', '点位设备模型实例创建', 'api', 'button', 
     (SELECT id FROM permissions WHERE permission_code = 'system:point-device-model-instance:view'), 20, TRUE),
    ('system:point-device-model-instance:edit', '点位设备模型实例编辑', 'api', 'button', 
     (SELECT id FROM permissions WHERE permission_code = 'system:point-device-model-instance:view'), 30, TRUE),
    ('system:point-device-model-instance:delete', '点位设备模型实例删除', 'api', 'button', 
     (SELECT id FROM permissions WHERE permission_code = 'system:point-device-model-instance:view'), 40, TRUE)
ON CONFLICT (permission_code) DO NOTHING;
