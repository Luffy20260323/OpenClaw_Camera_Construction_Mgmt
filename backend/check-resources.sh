#!/bin/bash

# 资源完整性检查和修复脚本
# 用途：对比前端路由和数据库资源，补充缺失的资源记录

export PGPASSWORD=uBUu8wZ50COo/1bhJwBpAIP/PKOFKutUnqdGH1dp3yw=
DB_HOST=localhost
DB_PORT=5432
DB_NAME=camera_construction_db
DB_USER=postgres

echo "========================================"
echo "资源完整性检查"
echo "========================================"
echo ""

# 1. 检查前端路由中定义的 menuCode
echo "1. 前端路由中的 menuCode 列表:"
grep -o "menuCode: '[^']*'" /root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/frontend/src/router/index.js | \
  sed "s/menuCode: '//g" | sed "s/'//g" | sort -u | while read code; do
    # 检查数据库中是否存在
    exists=$(psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c \
      "SELECT COUNT(*) FROM resource WHERE code = '$code' AND status = 1;")
    
    if [ "$exists" -eq 0 ]; then
        echo "  ❌ 缺失：$code"
    else
        echo "  ✅ 存在：$code"
    fi
done

echo ""
echo "2. 检查前端路由路径对应的资源:"

# 提取所有系统路由路径
grep "path: '/system" /root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/frontend/src/router/index.js | \
  sed "s/.*path: '//g" | sed "s/'.*//g" | while read path; do
    
    # 从路径推断 code
    code=$(echo $path | sed 's/\/system\///g' | sed 's/\//_/g')
    
    # 检查是否存在
    exists=$(psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c \
      "SELECT COUNT(*) FROM resource WHERE path = '$path' AND status = 1;")
    
    if [ "$exists" -eq 0 ]; then
        echo "  ❌ 缺失路径：$path (推断 code: $code)"
    else
        echo "  ✅ 存在路径：$path"
    fi
done

echo ""
echo "========================================"
echo "数据库资源统计"
echo "========================================"

psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "
SELECT 
    type,
    COUNT(*) as total,
    COUNT(CASE WHEN status=1 THEN 1 END) as active,
    COUNT(CASE WHEN status=0 THEN 1 END) as inactive
FROM resource
GROUP BY type
ORDER BY type;
"

echo ""
echo "========================================"
echo "模块资源分布"
echo "========================================"

psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "
SELECT 
    COALESCE(module_code, '(无模块)') as module,
    COUNT(*) as total,
    COUNT(DISTINCT type) as type_count
FROM resource
WHERE status = 1
GROUP BY module_code
ORDER BY module_code;
"

echo ""
echo "========================================"
echo "孤儿资源（无父资源的 MODULE/MENU）"
echo "========================================"

psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "
SELECT id, name, code, type, module_code, sort_order
FROM resource
WHERE type IN ('MODULE', 'MENU')
AND (parent_id IS NULL OR parent_id = 0)
AND status = 1
ORDER BY module_code, sort_order;
"

echo ""
echo "========================================"
echo "检查完成"
echo "========================================"
