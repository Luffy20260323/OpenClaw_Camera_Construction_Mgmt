#!/bin/bash
# 权限系统测试脚本

BASE_URL="http://localhost:8080/api"

echo "========================================"
echo "权限系统功能测试"
echo "========================================"
echo ""

# 测试 1：获取权限矩阵（无需认证，测试是否返回 403）
echo "测试 1: 获取权限矩阵（未认证）"
echo "----------------------------------------"
curl -s -o /dev/null -w "HTTP Status: %{http_code}\n" \
  "$BASE_URL/permission/matrix"
echo ""

# 测试 2：获取所有权限（未认证）
echo "测试 2: 获取所有权限（未认证）"
echo "----------------------------------------"
curl -s -o /dev/null -w "HTTP Status: %{http_code}\n" \
  "$BASE_URL/permission/list"
echo ""

# 测试 3：获取所有角色（未认证）
echo "测试 3: 获取所有角色（未认证）"
echo "----------------------------------------"
curl -s -o /dev/null -w "HTTP Status: %{http_code}\n" \
  "$BASE_URL/permission/roles"
echo ""

# 测试 4：健康检查
echo "测试 4: 后端健康检查"
echo "----------------------------------------"
curl -s "$BASE_URL/../actuator/health" 2>/dev/null | head -20 || echo "Actuator 未启用"
echo ""

# 测试 5：获取验证码配置
echo "测试 5: 获取验证码配置"
echo "----------------------------------------"
curl -s "$BASE_URL/auth/captcha/config" | head -50
echo ""

# 测试 6：检查数据库表
echo "测试 6: 检查数据库表"
echo "----------------------------------------"
docker exec camera-postgres psql -U postgres -d camera_construction_db -c \
  "SELECT tablename FROM pg_tables WHERE schemaname = 'public' AND tablename LIKE '%permission%' ORDER BY tablename;" 2>&1
echo ""

# 测试 7：检查权限数量
echo "测试 7: 检查权限数量"
echo "----------------------------------------"
docker exec camera-postgres psql -U postgres -d camera_construction_db -c \
  "SELECT COUNT(*) as permission_count FROM permissions;" 2>&1
echo ""

# 测试 8：检查角色数量
echo "测试 8: 检查角色数量"
echo "----------------------------------------"
docker exec camera-postgres psql -U postgres -d camera_construction_db -c \
  "SELECT COUNT(*) as role_count FROM roles;" 2>&1
echo ""

# 测试 9：检查系统管理员权限
echo "测试 9: 检查系统管理员权限配置"
echo "----------------------------------------"
docker exec camera-postgres psql -U postgres -d camera_construction_db -c \
  "SELECT r.role_name, COUNT(rp.permission_id) as perm_count 
   FROM roles r 
   LEFT JOIN role_permissions rp ON r.id = rp.role_id 
   WHERE r.role_code = 'system_admin'
   GROUP BY r.id, r.role_name;" 2>&1
echo ""

# 测试 10：检查审计日志表
echo "测试 10: 检查审计日志表结构"
echo "----------------------------------------"
docker exec camera-postgres psql -U postgres -d camera_construction_db -c \
  "\d permission_audit_log" 2>&1 | head -30
echo ""

echo "========================================"
echo "测试完成"
echo "========================================"
