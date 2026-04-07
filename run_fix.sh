#!/bin/bash

echo "=== API权限码修复工具 ==="
echo ""

# 先备份数据库
echo "1. 备份数据库..."
docker exec camera-postgres pg_dump -U postgres camera_construction_db > db_backup_$(date +%Y%m%d_%H%M%S).sql
echo "数据库备份完成"

echo ""
echo "2. 分析缺少权限码的API..."
docker exec camera-postgres psql -U postgres -d camera_construction_db -c "
SELECT 
    COUNT(*) as total_apis,
    COUNT(CASE WHEN permission_key IS NULL OR permission_key = '' THEN 1 END) as missing_permission_key
FROM resource 
WHERE type = 'API';
"

echo ""
echo "3. 查看缺少权限码的API示例..."
docker exec camera-postgres psql -U postgres -d camera_construction_db -c "
SELECT id, name, uri_pattern, method 
FROM resource 
WHERE type = 'API' 
  AND (permission_key IS NULL OR permission_key = '')
ORDER BY uri_pattern, method
LIMIT 10;
"

echo ""
echo "4. 生成修复SQL脚本..."
cat > fix_api_permissions_manual.sql << 'EOF'
-- API权限码修复脚本
-- 生成时间: 2026-04-07
-- 注意：这是一个示例脚本，需要根据实际情况调整

BEGIN;

-- 示例：为认证相关API添加权限码
UPDATE resource SET permission_key = 'auth:login:post:api' WHERE uri_pattern = '/auth/login' AND method = 'POST';
UPDATE resource SET permission_key = 'auth:refresh:post:api' WHERE uri_pattern = '/auth/refresh' AND method = 'POST';
UPDATE resource SET permission_key = 'auth:captcha_config:get:api' WHERE uri_pattern = '/auth/captcha/config' AND method = 'GET';
UPDATE resource SET permission_key = 'auth:captcha_image:get:api' WHERE uri_pattern = '/auth/captcha/image' AND method = 'GET';
UPDATE resource SET permission_key = 'auth:captcha:get:api' WHERE uri_pattern = '/auth/captcha' AND method = 'GET';
UPDATE resource SET permission_key = 'auth:captcha_sms:post:api' WHERE uri_pattern = '/auth/captcha/sms' AND method = 'POST';

-- 示例：为系统相关API添加权限码
UPDATE resource SET permission_key = 'system:config:view:api' WHERE uri_pattern = '/system/config' AND method = 'GET';
UPDATE resource SET permission_key = 'system:config:update:api' WHERE uri_pattern = '/system/config' AND method = 'PUT';

-- 示例：为用户相关API添加权限码
UPDATE resource SET permission_key = 'user:profile:view:api' WHERE uri_pattern = '/user/profile' AND method = 'GET';
UPDATE resource SET permission_key = 'user:profile:update:api' WHERE uri_pattern = '/user/profile' AND method = 'PUT';

-- 示例：为角色相关API添加权限码
UPDATE resource SET permission_key = 'role:list:view:api' WHERE uri_pattern = '/role' AND method = 'GET';
UPDATE resource SET permission_key = 'role:create:create:api' WHERE uri_pattern = '/role' AND method = 'POST';
UPDATE resource SET permission_key = 'role:detail:view:api' WHERE uri_pattern = '/role/{id}' AND method = 'GET';
UPDATE resource SET permission_key = 'role:detail:update:api' WHERE uri_pattern = '/role/{id}' AND method = 'PUT';
UPDATE resource SET permission_key = 'role:detail:delete:api' WHERE uri_pattern = '/role/{id}' AND method = 'DELETE';

COMMIT;
EOF

echo "修复SQL脚本已生成: fix_api_permissions_manual.sql"
echo ""
echo "5. 查看生成的SQL脚本..."
head -30 fix_api_permissions_manual.sql

echo ""
echo "=== 下一步操作 ==="
echo "1. 检查生成的SQL脚本是否合理"
echo "2. 根据需要调整权限码格式"
echo "3. 执行SQL脚本: docker exec -i camera-postgres psql -U postgres -d camera_construction_db < fix_api_permissions_manual.sql"
echo "4. 验证修复结果"
echo ""
echo "注意：建议先修复少量API进行测试，确认无误后再批量修复"