-- V3: 发布初始公司和作业区数据
-- 说明：发布 5 家初始公司及其作业区数据
--   - 国家管网集团华中分公司（甲方公司）+ 10 个作业区
--   - 国家管网集团云南分公司（甲方公司）+ 11 个作业区
--   - 云南仁邦信息工程有限公司（乙方公司）
--   - 陕西威远建设工程有限公司（乙方公司）
--   - 中移建设有限公司（乙方公司）
-- 执行时机：V2 初始数据之后

-------------------------------------------------------------------------------
-- 1. 公司数据
-------------------------------------------------------------------------------
-- 国家管网集团华中分公司（甲方公司，type_id=1）
INSERT INTO companies (id, company_name, type_id, status, is_system_protected) VALUES
    (4, '国家管网集团华中分公司', 1, 'active', false)
ON CONFLICT (id) DO UPDATE SET 
    company_name = EXCLUDED.company_name,
    type_id = EXCLUDED.type_id,
    status = EXCLUDED.status;

-- 国家管网集团云南分公司（甲方公司，type_id=1）
INSERT INTO companies (id, company_name, type_id, status, is_system_protected) VALUES
    (5, '国家管网集团云南分公司', 1, 'active', false)
ON CONFLICT (id) DO UPDATE SET 
    company_name = EXCLUDED.company_name,
    type_id = EXCLUDED.type_id,
    status = EXCLUDED.status;

-- 云南仁邦信息工程有限公司（乙方公司，type_id=2）
INSERT INTO companies (id, company_name, type_id, status, is_system_protected) VALUES
    (6, '云南仁邦信息工程有限公司', 2, 'active', false)
ON CONFLICT (id) DO UPDATE SET 
    company_name = EXCLUDED.company_name,
    type_id = EXCLUDED.type_id,
    status = EXCLUDED.status;

-- 陕西威远建设工程有限公司（乙方公司，type_id=2）
INSERT INTO companies (id, company_name, type_id, status, is_system_protected) VALUES
    (8, '陕西威远建设工程有限公司', 2, 'active', false)
ON CONFLICT (id) DO UPDATE SET 
    company_name = EXCLUDED.company_name,
    type_id = EXCLUDED.type_id,
    status = EXCLUDED.status;

-- 中移建设有限公司（乙方公司，type_id=2）
INSERT INTO companies (id, company_name, type_id, status, is_system_protected) VALUES
    (10, '中移建设有限公司', 2, 'active', false)
ON CONFLICT (id) DO UPDATE SET 
    company_name = EXCLUDED.company_name,
    type_id = EXCLUDED.type_id,
    status = EXCLUDED.status;

-------------------------------------------------------------------------------
-- 2. 作业区数据 - 国家管网集团华中分公司（company_id=4）
-------------------------------------------------------------------------------
INSERT INTO work_areas (id, work_area_name, work_area_code, company_id, max_capacity, description) VALUES
    (4, '武汉北作业区', 'Wuhanbei', 4, 1000, '华中分公司武汉北作业区'),
    (14, '武汉南作业区', 'Wuhannan', 4, 1000, '华中分公司武汉南作业区'),
    (15, '黄冈作业区', 'Huanggang', 4, 1000, '华中分公司黄冈作业区'),
    (16, '潜江作业区', 'Qianjiang', 4, 1000, '华中分公司潜江作业区'),
    (17, '洪湖作业区', 'Honghu', 4, 1000, '华中分公司洪湖作业区'),
    (18, '荆门作业区', 'Jingmen', 4, 1000, '华中分公司荆门作业区'),
    (19, '襄阳作业区', 'Xiangyang', 4, 1000, '华中分公司襄阳作业区'),
    (20, '枣阳作业区', 'Zaoyang', 4, 1000, '华中分公司枣阳作业区'),
    (21, '宜昌作业区', 'Yichang', 4, 1000, '华中分公司宜昌作业区'),
    (22, '恩施作业区', 'Enshi', 4, 1000, '华中分公司恩施作业区')
ON CONFLICT (id) DO UPDATE SET 
    work_area_name = EXCLUDED.work_area_name,
    work_area_code = EXCLUDED.work_area_code,
    company_id = EXCLUDED.company_id,
    max_capacity = EXCLUDED.max_capacity,
    description = EXCLUDED.description;

-------------------------------------------------------------------------------
-- 3. 作业区数据 - 国家管网集团云南分公司（company_id=5）
-------------------------------------------------------------------------------
INSERT INTO work_areas (id, work_area_name, work_area_code, company_id, max_capacity, description) VALUES
    (3, '德宏作业区', 'Dehong', 5, 1000, '云南分公司德宏作业区'),
    (5, '保山作业区', 'Baoshan', 5, 1000, '云南分公司保山作业区'),
    (6, '大理作业区', 'Dali', 5, 1000, '云南分公司大理作业区'),
    (7, '楚雄作业区', 'Chuxiong', 5, 1000, '云南分公司楚雄作业区'),
    (8, '昆明作业区', 'Kunming', 5, 1000, '云南分公司昆明作业区'),
    (9, '昆东作业区', 'Kundong', 5, 1000, '云南分公司昆东作业区'),
    (10, '曲靖作业区', 'Qujing', 5, 1000, '云南分公司曲靖作业区'),
    (11, '玉溪作业区', 'Yuxi', 5, 1000, '云南分公司玉溪作业区'),
    (12, '红河作业区', 'Honghe', 5, 1000, '云南分公司红河作业区'),
    (13, '文山作业区', 'Wenshan', 5, 1000, '云南分公司文山作业区')
ON CONFLICT (id) DO UPDATE SET 
    work_area_name = EXCLUDED.work_area_name,
    work_area_code = EXCLUDED.work_area_code,
    company_id = EXCLUDED.company_id,
    max_capacity = EXCLUDED.max_capacity,
    description = EXCLUDED.description;

-------------------------------------------------------------------------------
-- 数据验证（注释掉，仅用于调试）
-------------------------------------------------------------------------------
-- SELECT '公司' as table_name, COUNT(*) as count FROM companies WHERE id IN (4, 5, 6, 8, 10);
-- SELECT '华中分公司作业区' as table_name, COUNT(*) as count FROM work_areas WHERE company_id = 4;
-- SELECT '云南分公司作业区' as table_name, COUNT(*) as count FROM work_areas WHERE company_id = 5;
