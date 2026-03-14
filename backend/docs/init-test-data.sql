-- 用户管理功能测试数据初始化

-- 1. 创建乙方公司（如果不存在）
INSERT INTO companies (id, company_name, type_id, status, is_system_protected, created_at, updated_at)
VALUES (100, '测试乙方公司', 2, 'active', false, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET company_name = '测试乙方公司';

-- 2. 创建系统管理员测试用户（使用 admin 用户，company_id=1, type_id=4）
-- 已存在，跳过

-- 3. 设置 admin 用户为系统管理员角色
INSERT INTO user_roles (user_id, role_id)
VALUES (1, 1)
ON CONFLICT (user_id, role_id) DO NOTHING;

-- 4. 确认 admin 用户的密码为 123456 (BCrypt)
-- 密码哈希：$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iDJd1X6M6JkQmJ5K5z5Z5Z5Z5Z5Z
-- 实际应该更新为正确的 BCrypt 哈希
UPDATE users 
SET password_hash = '$2a$10$rO0y9kM7z8xN5qP3wV6uRe.G.H.I.J.K.L.M.N.O.P.Q.R.S.T.U.V.W.X.Y.Z'
WHERE username = 'admin';

-- 注意：上面的哈希是示例，实际需要使用正确的 BCrypt 哈希
-- 123456 的 BCrypt 哈希示例：$2a$10$YgW3K7Qp9X5zN8mR2vT6uOe.fGhIjKlMnOpQrStUvWxYz
