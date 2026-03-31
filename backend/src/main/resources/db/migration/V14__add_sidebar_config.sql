-- V14: 添加侧边栏配置
-- 说明：添加侧边栏位置和显示模式配置
-- 执行时机：系统配置功能扩展

-------------------------------------------------------------------------------
-- 1. 添加 system_configs 表的 config_type 字段（如果不存在）
-------------------------------------------------------------------------------
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'system_configs' AND column_name = 'config_type'
    ) THEN
        ALTER TABLE system_configs ADD COLUMN config_type VARCHAR(50) DEFAULT 'string';
    END IF;
END
$$;

-------------------------------------------------------------------------------
-- 2. 插入侧边栏配置初始值
-------------------------------------------------------------------------------
INSERT INTO system_configs (config_key, config_value, config_type, description) VALUES
    ('sidebar-position', 'LEFT', 'string', '侧边栏位置：LEFT-左边，RIGHT-右边'),
    ('sidebar-mode', 'FIXED', 'string', '侧边栏显示模式：FIXED-一直显示，COLLAPSIBLE-可隐藏')
ON CONFLICT (config_key) DO NOTHING;
