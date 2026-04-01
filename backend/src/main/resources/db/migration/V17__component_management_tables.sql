-- ============================================================================
-- V17: 零部件管理表结构
-- ============================================================================
-- 创建时间：2026-04-01
-- 说明：创建零部件种类表及初始数据
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

-- 2. 创建索引
CREATE INDEX IF NOT EXISTS idx_component_types_code ON component_types(code);
CREATE INDEX IF NOT EXISTS idx_component_types_is_active ON component_types(is_active);
CREATE INDEX IF NOT EXISTS idx_component_types_sequence_no ON component_types(sequence_no);

-- 3. 添加表注释
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

-- 4. 插入初始数据（17 种零部件）
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
