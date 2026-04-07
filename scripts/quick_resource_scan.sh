#!/bin/bash

# 快速资源扫描脚本
# 第一阶段：数据库分析和简单扫描

echo "=== 资源扫描第一阶段：数据库分析 ==="

# 1. 分析数据库中的资源数据
echo "1. 分析数据库resource表结构..."
docker exec camera-postgres psql -U postgres -d camera_construction_db -c "
SELECT 
    COUNT(*) as total_resources,
    COUNT(CASE WHEN permission_key IS NULL OR permission_key = '' THEN 1 END) as missing_permission_key,
    COUNT(CASE WHEN required_permission IS NOT NULL AND required_permission != '' THEN 1 END) as has_required_permission,
    COUNT(CASE WHEN deleted = true THEN 1 END) as deleted_resources
FROM resource;
"

echo ""
echo "2. 按类型统计资源数量..."
docker exec camera-postgres psql -U postgres -d camera_construction_db -c "
SELECT 
    type,
    COUNT(*) as count,
    COUNT(CASE WHEN permission_key IS NULL OR permission_key = '' THEN 1 END) as missing_perm_key
FROM resource
GROUP BY type
ORDER BY type;
"

echo ""
echo "3. 检查重复的权限码..."
docker exec camera-postgres psql -U postgres -d camera_construction_db -c "
SELECT permission_key, COUNT(*) as duplicate_count
FROM resource 
WHERE permission_key IS NOT NULL AND permission_key != ''
GROUP BY permission_key
HAVING COUNT(*) > 1
ORDER BY duplicate_count DESC;
"

echo ""
echo "4. 检查孤立的资源（parent_id指向不存在的资源）..."
docker exec camera-postgres psql -U postgres -d camera_construction_db -c "
SELECT r.id, r.name, r.type, r.parent_id
FROM resource r
LEFT JOIN resource p ON r.parent_id = p.id
WHERE r.parent_id IS NOT NULL 
  AND p.id IS NULL
ORDER BY r.type, r.name;
"

echo ""
echo "5. 检查软删除字段使用情况..."
docker exec camera-postgres psql -U postgres -d camera_construction_db -c "
SELECT 
    COUNT(*) as total_deleted,
    MIN(deleted_at) as earliest_deletion,
    MAX(deleted_at) as latest_deletion,
    COUNT(DISTINCT deleted_by) as distinct_deleters
FROM resource
WHERE deleted = true;
"

echo ""
echo "=== 前端资源快速扫描 ==="

# 2. 扫描前端Vue文件
echo "6. 扫描前端Vue组件文件..."
find frontend/src -name "*.vue" | wc -l | xargs echo "找到Vue文件数量:"

echo ""
echo "7. 扫描页面组件..."
find frontend/src/views -name "*.vue" | head -20 | while read file; do
    filename=$(basename "$file")
    echo "  - $filename"
done

echo ""
echo "=== 后端资源快速扫描 ==="

# 3. 扫描后端Java文件
echo "8. 扫描后端Controller文件..."
find backend/src -name "*Controller.java" | wc -l | xargs echo "找到Controller文件数量:"

echo ""
echo "9. 列出所有Controller..."
find backend/src -name "*Controller.java" | head -20 | while read file; do
    filename=$(basename "$file")
    echo "  - $filename"
done

echo ""
echo "=== 生成初步报告 ==="

# 创建报告目录
mkdir -p ./scan_reports
report_file="./scan_reports/quick_scan_$(date +%Y%m%d_%H%M%S).txt"

{
    echo "资源快速扫描报告"
    echo "生成时间: $(date)"
    echo "================================"
    echo ""
    echo "1. 数据库资源统计:"
    docker exec camera-postgres psql -U postgres -d camera_construction_db -t -c "
    SELECT 
        '总资源数: ' || COUNT(*) as total,
        '缺少权限码: ' || COUNT(CASE WHEN permission_key IS NULL OR permission_key = '' THEN 1 END) as missing,
        '有required_permission字段: ' || COUNT(CASE WHEN required_permission IS NOT NULL AND required_permission != '' THEN 1 END) as has_req_perm,
        '已删除资源: ' || COUNT(CASE WHEN deleted = true THEN 1 END) as deleted
    FROM resource;"
    
    echo ""
    echo "2. 按类型统计:"
    docker exec camera-postgres psql -U postgres -d camera_construction_db -t -c "
    SELECT 
        type || ': ' || COUNT(*) || '个 (缺少权限码: ' || COUNT(CASE WHEN permission_key IS NULL OR permission_key = '' THEN 1 END) || ')'
    FROM resource
    GROUP BY type
    ORDER BY type;"
    
    echo ""
    echo "3. 前端资源:"
    echo "   Vue文件总数: $(find frontend/src -name "*.vue" | wc -l)"
    echo "   页面组件数: $(find frontend/src/views -name "*.vue" | wc -l)"
    
    echo ""
    echo "4. 后端资源:"
    echo "   Controller文件数: $(find backend/src -name "*Controller.java" | wc -l)"
    
    echo ""
    echo "5. 发现的问题:"
    echo "   - 重复权限码:"
    docker exec camera-postgres psql -U postgres -d camera_construction_db -t -c "
    SELECT '    ' || permission_key || ' (重复' || COUNT(*) || '次)'
    FROM resource 
    WHERE permission_key IS NOT NULL AND permission_key != ''
    GROUP BY permission_key
    HAVING COUNT(*) > 1
    ORDER BY COUNT(*) DESC
    LIMIT 10;"
    
    echo ""
    echo "   - 孤立资源:"
    docker exec camera-postgres psql -U postgres -d camera_construction_db -t -c "
    SELECT '    ID:' || r.id || ' ' || r.name || ' (' || r.type || ') - 父资源ID:' || r.parent_id
    FROM resource r
    LEFT JOIN resource p ON r.parent_id = p.id
    WHERE r.parent_id IS NOT NULL 
      AND p.id IS NULL
    ORDER BY r.type, r.name
    LIMIT 10;"
    
} > "$report_file"

echo "快速扫描完成！报告已保存到: $report_file"
echo ""
echo "下一步建议:"
echo "1. 查看报告文件: $report_file"
echo "2. 根据报告中的问题制定修复计划"
echo "3. 进行更详细的资源扫描"