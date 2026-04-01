-- ============================================================================
-- V20: 点位设备模型表结构
-- ============================================================================
-- 创建时间：2026-04-01
-- 说明：创建点位设备模型表及模型项表
-- ============================================================================

-- 1. 创建点位设备模型表
CREATE TABLE IF NOT EXISTS point_device_models (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    code VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. 创建点位设备模型项表（定义每种零部件的数量）
CREATE TABLE IF NOT EXISTS point_device_model_items (
    id BIGSERIAL PRIMARY KEY,
    model_id BIGINT NOT NULL REFERENCES point_device_models(id) ON DELETE CASCADE,
    component_type_id BIGINT NOT NULL REFERENCES component_types(id),
    quantity INT NOT NULL DEFAULT 1,
    sequence_no INT DEFAULT 10,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. 创建索引
CREATE INDEX IF NOT EXISTS idx_point_device_models_code ON point_device_models(code);
CREATE INDEX IF NOT EXISTS idx_point_device_models_is_active ON point_device_models(is_active);
CREATE INDEX IF NOT EXISTS idx_point_device_model_items_model_id ON point_device_model_items(model_id);
CREATE INDEX IF NOT EXISTS idx_point_device_model_items_component_type_id ON point_device_model_items(component_type_id);

-- 4. 添加表注释
COMMENT ON TABLE point_device_models IS '点位设备模型表：定义点位所需的设备配置模板';
COMMENT ON COLUMN point_device_models.id IS '模型 ID';
COMMENT ON COLUMN point_device_models.name IS '模型名称：如"标准监控点位模型"';
COMMENT ON COLUMN point_device_models.code IS '模型编码：如"STANDARD_MONITORING_POINT"';
COMMENT ON COLUMN point_device_models.description IS '描述';
COMMENT ON COLUMN point_device_models.is_active IS '是否启用';
COMMENT ON COLUMN point_device_models.created_by IS '创建人';
COMMENT ON COLUMN point_device_models.created_at IS '创建时间';
COMMENT ON COLUMN point_device_models.updated_at IS '更新时间';

COMMENT ON TABLE point_device_model_items IS '点位设备模型项表：定义模型中每种零部件的数量';
COMMENT ON COLUMN point_device_model_items.id IS '模型项 ID';
COMMENT ON COLUMN point_device_model_items.model_id IS '关联模型 ID';
COMMENT ON COLUMN point_device_model_items.component_type_id IS '零部件种类 ID';
COMMENT ON COLUMN point_device_model_items.quantity IS '数量';
COMMENT ON COLUMN point_device_model_items.sequence_no IS '显示序号，用于排序';
COMMENT ON COLUMN point_device_model_items.created_at IS '创建时间';
COMMENT ON COLUMN point_device_model_items.updated_at IS '更新时间';

-- 5. 插入示例数据：标准监控点位模型
INSERT INTO point_device_models (name, code, description, is_active) VALUES
    ('标准监控点位模型', 'STANDARD_MONITORING_POINT', '包含监控杆、摄像头、蓄电池、光伏板的标准配置', TRUE)
ON CONFLICT (code) DO NOTHING;

-- 获取刚插入的模型 ID 并插入模型项
DO $$
DECLARE
    model_id BIGINT;
BEGIN
    SELECT id INTO model_id FROM point_device_models WHERE code = 'STANDARD_MONITORING_POINT';
    
    IF model_id IS NOT NULL THEN
        INSERT INTO point_device_model_items (model_id, component_type_id, quantity, sequence_no)
        SELECT model_id, id, 1, seq * 10
        FROM component_types
        WHERE code IN ('MONITORING_POLE', 'CAMERA', 'BATTERY', 'SOLAR_PANEL')
        ORDER BY CASE code
            WHEN 'MONITORING_POLE' THEN 1
            WHEN 'CAMERA' THEN 2
            WHEN 'BATTERY' THEN 3
            WHEN 'SOLAR_PANEL' THEN 4
        END;
    END IF;
END $$;
