-- ============================================================================
-- V19: 零部件属性集实例表结构
-- ============================================================================
-- 创建时间：2026-04-01
-- 说明：创建零部件属性集实例表和零部件实例表，支持零部件属性管理和实例化
-- ============================================================================

-- 1. 创建零部件种类表
CREATE TABLE IF NOT EXISTS component_types (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '种类名称',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '种类编码',
    description TEXT COMMENT '描述',
    sequence_no INT DEFAULT 10 COMMENT '显示序号',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    created_by BIGINT COMMENT '创建人',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 2. 创建零部件属性集表
CREATE TABLE IF NOT EXISTS component_attr_sets (
    id BIGSERIAL PRIMARY KEY,
    component_type_id BIGINT NOT NULL COMMENT '关联零部件种类',
    name VARCHAR(100) NOT NULL COMMENT '属性集名称',
    code VARCHAR(50) NOT NULL COMMENT '属性集编码',
    description TEXT COMMENT '描述',
    sequence_no INT DEFAULT 10 COMMENT '显示序号',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    created_by BIGINT COMMENT '创建人',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT fk_attr_set_type FOREIGN KEY (component_type_id) 
        REFERENCES component_types(id) ON DELETE CASCADE
);

-- 3. 创建属性集属性定义表
CREATE TABLE IF NOT EXISTS component_attr_set_attrs (
    id BIGSERIAL PRIMARY KEY,
    attr_set_id BIGINT NOT NULL COMMENT '关联属性集',
    attr_name VARCHAR(100) NOT NULL COMMENT '属性名称',
    attr_code VARCHAR(50) NOT NULL COMMENT '属性编码（数据库字段名）',
    attr_type VARCHAR(20) NOT NULL COMMENT '属性类型：TEXT/NUMBER/DATE/SELECT/MULTI_SELECT/FILE',
    unit VARCHAR(50) COMMENT '单位',
    options JSONB COMMENT '选项值（用于 SELECT 类型）',
    default_value TEXT COMMENT '默认值',
    sequence_no INT DEFAULT 10 COMMENT '显示序号',
    is_required BOOLEAN DEFAULT FALSE COMMENT '是否必填',
    validation_rule TEXT COMMENT '验证规则',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    CONSTRAINT fk_attr_set FOREIGN KEY (attr_set_id) 
        REFERENCES component_attr_sets(id) ON DELETE CASCADE
);

-- 4. 创建零部件属性集实例表
CREATE TABLE IF NOT EXISTS component_attr_set_instances (
    id BIGSERIAL PRIMARY KEY,
    attr_set_id BIGINT NOT NULL COMMENT '关联属性集',
    name VARCHAR(200) NOT NULL COMMENT '实例名称',
    attr_values JSONB NOT NULL COMMENT '属性值 JSON',
    created_by BIGINT COMMENT '创建人',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT fk_attr_set_instance FOREIGN KEY (attr_set_id) 
        REFERENCES component_attr_sets(id) ON DELETE CASCADE
);

-- 5. 创建零部件实例表
CREATE TABLE IF NOT EXISTS component_instances (
    id BIGSERIAL PRIMARY KEY,
    component_type_id BIGINT NOT NULL COMMENT '零部件种类',
    attr_set_instance_id BIGINT COMMENT '属性集实例（可选）',
    serial_number VARCHAR(100) COMMENT '序列号',
    status VARCHAR(20) DEFAULT 'normal' COMMENT '状态：normal-正常使用，replaced-已更换，scrapped-已报废',
    created_by BIGINT COMMENT '创建人',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT fk_instance_type FOREIGN KEY (component_type_id) 
        REFERENCES component_types(id),
    CONSTRAINT fk_instance_attr FOREIGN KEY (attr_set_instance_id) 
        REFERENCES component_attr_set_instances(id)
);

-- 6. 创建索引
CREATE INDEX IF NOT EXISTS idx_component_types_code ON component_types(code);
CREATE INDEX IF NOT EXISTS idx_component_types_is_active ON component_types(is_active);
CREATE INDEX IF NOT EXISTS idx_attr_sets_component_type_id ON component_attr_sets(component_type_id);
CREATE INDEX IF NOT EXISTS idx_attr_sets_is_active ON component_attr_sets(is_active);
CREATE INDEX IF NOT EXISTS idx_attr_set_attrs_attr_set_id ON component_attr_set_attrs(attr_set_id);
CREATE INDEX IF NOT EXISTS idx_attr_set_instances_attr_set_id ON component_attr_set_instances(attr_set_id);
CREATE INDEX IF NOT EXISTS idx_component_instances_component_type_id ON component_instances(component_type_id);
CREATE INDEX IF NOT EXISTS idx_component_instances_attr_set_instance_id ON component_instances(attr_set_instance_id);
CREATE INDEX IF NOT EXISTS idx_attr_set_instances_attr_values ON component_attr_set_instances USING GIN (attr_values);

-- 7. 添加表注释
COMMENT ON TABLE component_types IS '零部件种类表';
COMMENT ON COLUMN component_types.id IS '种类 ID';
COMMENT ON COLUMN component_types.name IS '种类名称：监控杆、摄像头、光伏板等';
COMMENT ON COLUMN component_types.code IS '种类编码：MONITORING_POLE、CAMERA 等';
COMMENT ON COLUMN component_types.description IS '描述';
COMMENT ON COLUMN component_types.sequence_no IS '显示序号，用于前端排序';
COMMENT ON COLUMN component_types.is_active IS '是否启用';
COMMENT ON COLUMN component_types.created_by IS '创建人';
COMMENT ON COLUMN component_types.created_at IS '创建时间';
COMMENT ON COLUMN component_types.updated_at IS '更新时间';

COMMENT ON TABLE component_attr_sets IS '零部件属性集表';
COMMENT ON COLUMN component_attr_sets.id IS '属性集 ID';
COMMENT ON COLUMN component_attr_sets.component_type_id IS '关联零部件种类';
COMMENT ON COLUMN component_attr_sets.name IS '属性集名称：如"9 米监控杆属性集"';
COMMENT ON COLUMN component_attr_sets.code IS '属性集编码';
COMMENT ON COLUMN component_attr_sets.description IS '描述';
COMMENT ON COLUMN component_attr_sets.sequence_no IS '显示序号';
COMMENT ON COLUMN component_attr_sets.is_active IS '是否启用';
COMMENT ON COLUMN component_attr_sets.created_by IS '创建人';
COMMENT ON COLUMN component_attr_sets.created_at IS '创建时间';
COMMENT ON COLUMN component_attr_sets.updated_at IS '更新时间';

COMMENT ON TABLE component_attr_set_attrs IS '属性集属性定义表';
COMMENT ON COLUMN component_attr_set_attrs.id IS '属性定义 ID';
COMMENT ON COLUMN component_attr_set_attrs.attr_set_id IS '关联属性集';
COMMENT ON COLUMN component_attr_set_attrs.attr_name IS '属性名称';
COMMENT ON COLUMN component_attr_set_attrs.attr_code IS '属性编码（数据库字段名）';
COMMENT ON COLUMN component_attr_set_attrs.attr_type IS '属性类型：TEXT/NUMBER/DATE/SELECT/MULTI_SELECT/FILE';
COMMENT ON COLUMN component_attr_set_attrs.unit IS '单位';
COMMENT ON COLUMN component_attr_set_attrs.options IS '选项值（用于 SELECT 类型）';
COMMENT ON COLUMN component_attr_set_attrs.default_value IS '默认值';
COMMENT ON COLUMN component_attr_set_attrs.sequence_no IS '显示序号';
COMMENT ON COLUMN component_attr_set_attrs.is_required IS '是否必填';
COMMENT ON COLUMN component_attr_set_attrs.validation_rule IS '验证规则';
COMMENT ON COLUMN component_attr_set_attrs.created_at IS '创建时间';

COMMENT ON TABLE component_attr_set_instances IS '零部件属性集实例表';
COMMENT ON COLUMN component_attr_set_instances.id IS '实例 ID';
COMMENT ON COLUMN component_attr_set_instances.attr_set_id IS '关联属性集';
COMMENT ON COLUMN component_attr_set_instances.name IS '实例名称';
COMMENT ON COLUMN component_attr_set_instances.attr_values IS '属性值 JSON：{"厂商":"海康威视","高度":"9 米","材质":"热镀锌钢"}';
COMMENT ON COLUMN component_attr_set_instances.created_by IS '创建人';
COMMENT ON COLUMN component_attr_set_instances.created_at IS '创建时间';
COMMENT ON COLUMN component_attr_set_instances.updated_at IS '更新时间';

COMMENT ON TABLE component_instances IS '零部件实例表';
COMMENT ON COLUMN component_instances.id IS '零部件实例 ID';
COMMENT ON COLUMN component_instances.component_type_id IS '零部件种类';
COMMENT ON COLUMN component_instances.attr_set_instance_id IS '属性集实例（可选）';
COMMENT ON COLUMN component_instances.serial_number IS '序列号';
COMMENT ON COLUMN component_instances.status IS '状态：normal-正常使用，replaced-已更换，scrapped-已报废';
COMMENT ON COLUMN component_instances.created_by IS '创建人';
COMMENT ON COLUMN component_instances.created_at IS '创建时间';
COMMENT ON COLUMN component_instances.updated_at IS '更新时间';

-- 8. 插入零部件种类初始数据
INSERT INTO component_types (name, code, description, sequence_no, is_active) VALUES
    ('监控杆', 'MONITORING_POLE', '支撑摄像头的杆体', 10, TRUE),
    ('摄像头', 'CAMERA', '视频采集设备', 20, TRUE),
    ('光伏板', 'SOLAR_PANEL', '太阳能发电板', 30, TRUE),
    ('蓄电池', 'BATTERY', '储能设备', 40, TRUE),
    ('太阳能控制器', 'SOLAR_CONTROLLER', '光伏充电控制', 50, TRUE),
    ('通信设备', 'COMMUNICATION_DEVICE', '通信设备统称', 60, TRUE),
    ('AI 边缘盒子', 'AI_EDGE_BOX', '具备通信、存储、AI 能力', 70, TRUE),
    ('存储网关', 'STORAGE_GATEWAY', '具备通信、存储能力', 80, TRUE),
    ('路由器', 'ROUTER', '网络路由设备', 90, TRUE),
    ('稳压器', 'VOLTAGE_STABILIZER', '电压稳定设备', 100, TRUE),
    ('变压器', 'TRANSFORMER', '电压转换设备', 110, TRUE),
    ('逆变器', 'INVERTER', 'DC/AC 转换设备', 120, TRUE),
    ('扩音器', 'LOUDSPEAKER', '音频输出设备', 130, TRUE),
    ('拾音器', 'MICROPHONE', '音频采集设备', 140, TRUE),
    ('声光报警器', 'ALARM_DEVICE', '报警设备', 150, TRUE),
    ('警示牌', 'WARNING_SIGN', '安全警示标识', 160, TRUE),
    ('风向标', 'WIND_VANE', '风向指示设备', 170, TRUE)
ON CONFLICT (code) DO NOTHING;
