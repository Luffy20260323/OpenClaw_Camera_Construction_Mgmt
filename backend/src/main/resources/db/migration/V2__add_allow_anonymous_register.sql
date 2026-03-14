-- 添加允许匿名注册字段
ALTER TABLE companies ADD COLUMN IF NOT EXISTS allow_anonymous_register BOOLEAN DEFAULT false;

-- 设置默认值：系统保护的公司不允许匿名注册
UPDATE companies SET allow_anonymous_register = false WHERE is_system_protected = true;

-- 添加注释
COMMENT ON COLUMN companies.allow_anonymous_register IS '是否允许匿名注册';
