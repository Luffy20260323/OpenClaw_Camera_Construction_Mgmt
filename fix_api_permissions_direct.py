#!/usr/bin/env python3
"""
直接运行的API权限码修复脚本
"""

import re
import psycopg2

print("API权限码修复工具")
print("=" * 60)

# 数据库配置
DB_CONFIG = {
    'host': 'localhost',
    'port': 5432,
    'database': 'camera_construction_db',
    'user': 'postgres',
    'password': 'CameraSystem2026'
}

# URI模式到模块的映射
URI_MODULE_MAP = {
    '/auth/': 'auth',
    '/system/': 'system',
    '/user/': 'user',
    '/role/': 'role',
    '/permission/': 'permission',
    '/resource/': 'resource',
    '/menu/': 'menu',
    '/audit/': 'audit',
    '/log/': 'log',
    '/company/': 'company',
    '/workarea/': 'workarea',
    '/component/': 'component',
    '/point/': 'point',
    '/document/': 'document',
    '/element/': 'element',
    '/config/': 'config'
}

# HTTP方法到动作的映射
METHOD_ACTION_MAP = {
    'GET': 'view',
    'POST': 'create',
    'PUT': 'update',
    'DELETE': 'delete',
    'PATCH': 'patch'
}

def connect_db():
    """连接数据库"""
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        return conn
    except Exception as e:
        print(f"数据库连接失败: {e}")
        return None

def get_missing_permission_apis():
    """获取缺少权限码的API资源"""
    conn = connect_db()
    if not conn:
        return []
    
    try:
        cursor = conn.cursor()
        cursor.execute("""
            SELECT id, name, uri_pattern, method 
            FROM resource 
            WHERE type = 'API' 
              AND (permission_key IS NULL OR permission_key = '')
            ORDER BY uri_pattern, method
        """)
        
        apis = []
        for row in cursor.fetchall():
            apis.append({
                'id': row[0],
                'name': row[1],
                'uri_pattern': row[2],
                'method': row[3]
            })
        
        return apis
        
    except Exception as e:
        print(f"查询API数据失败: {e}")
        return []
    finally:
        conn.close()

def generate_permission_key(uri_pattern: str, method: str) -> str:
    """根据URI模式和方法生成权限码"""
    
    # 确定模块
    module = 'general'
    for prefix, mod in URI_MODULE_MAP.items():
        if uri_pattern.startswith(prefix):
            module = mod
            break
    
    # 确定资源名
    # 移除路径参数，如{id}、{roleId}等
    clean_uri = re.sub(r'\{[^}]+}', '', uri_pattern)
    # 移除首尾斜杠，分割路径
    parts = clean_uri.strip('/').split('/')
    
    # 构建资源名
    if len(parts) >= 2:
        # 使用最后两部分作为资源名，如: user/profile
        resource = '_'.join(parts[-2:])
    elif len(parts) == 1:
        resource = parts[0]
    else:
        resource = 'root'
    
    # 清理资源名中的特殊字符
    resource = re.sub(r'[^a-zA-Z0-9_]', '_', resource)
    resource = resource.lower()
    
    # 确定动作
    action = METHOD_ACTION_MAP.get(method, 'unknown')
    
    # 生成权限码
    permission_key = f"{module}:{resource}:{action}:api"
    
    return permission_key

def validate_permission_key(permission_key: str) -> bool:
    """验证权限码格式"""
    # 基本格式检查: module:resource:action:type
    pattern = r'^[a-z][a-z0-9_]*:[a-z][a-z0-9_]*:[a-z][a-z0-9_]*:api$'
    return bool(re.match(pattern, permission_key))

def generate_fix_sql(fixes):
    """生成修复SQL脚本"""
    sql_lines = []
    sql_lines.append("-- API权限码修复脚本")
    sql_lines.append("-- 生成时间: 2026-04-07")
    sql_lines.append("")
    sql_lines.append("BEGIN;")
    sql_lines.append("")
    
    for fix in fixes:
        resource_id = fix['id']
        permission_key = fix['permission_key']
        
        sql_lines.append(f"-- {fix['name']}")
        sql_lines.append(f"-- URI: {fix['uri_pattern']} [{fix['method']}]")
        sql_lines.append(f"UPDATE resource SET permission_key = '{permission_key}' WHERE id = {resource_id};")
        sql_lines.append("")
    
    sql_lines.append("COMMIT;")
    
    return '\n'.join(sql_lines)

def main():
    """主函数"""
    print("开始修复API权限码...")
    
    # 获取缺少权限码的API
    apis = get_missing_permission_apis()
    print(f"找到 {len(apis)} 个缺少权限码的API")
    
    if not apis:
        print("没有需要修复的API")
        return
    
    # 生成修复方案
    fixes = []
    for api in apis:
        permission_key = generate_permission_key(
            api['uri_pattern'], 
            api['method']
        )
        
        if validate_permission_key(permission_key):
            fixes.append({
                **api,
                'permission_key': permission_key
            })
        else:
            print(f"警告：无法为API生成有效的权限码: {api['name']} ({api['uri_pattern']})")
    
    print(f"成功为 {len(fixes)} 个API生成权限码")
    
    # 生成SQL脚本
    sql_script = generate_fix_sql(fixes)
    
    # 保存脚本
    sql_file = "fix_api_permissions_generated.sql"
    with open(sql_file, 'w', encoding='utf-8') as f:
        f.write(sql_script)
    
    print(f"SQL脚本已保存到: {sql_file}")
    
    # 生成报告
    generate_report(fixes)
    
    print("\n下一步操作:")
    print(f"1. 检查生成的SQL脚本: {sql_file}")
    print("2. 检查修复报告: api_permission_fix_report.txt")
    print("3. 确认无误后，手动执行SQL脚本")
    print("4. 或者联系管理员执行")

def generate_report(fixes):
    """生成修复报告"""
    report_lines = []
    report_lines.append("API权限码修复报告")
    report_lines.append("=" * 60)
    report_lines.append(f"修复时间: 2026-04-07")
    report_lines.append(f"修复数量: {len(fixes)}")
    report_lines.append("")
    
    # 按模块统计
    module_stats = {}
    for fix in fixes:
        module = fix['permission_key'].split(':')[0]
        if module not in module_stats:
            module_stats[module] = 0
        module_stats[module] += 1
    
    report_lines.append("按模块统计:")
    for module, count in sorted(module_stats.items()):
        report_lines.append(f"  {module}: {count}个")
    
    report_lines.append("")
    report_lines.append("详细修复列表（前20个）:")
    for i, fix in enumerate(fixes[:20], 1):
        report_lines.append(f"{i:3d}. {fix['name']}")
        report_lines.append(f"      URI: {fix['uri_pattern']} [{fix['method']}]")
        report_lines.append(f"      权限码: {fix['permission_key']}")
        report_lines.append("")
    
    if len(fixes) > 20:
        report_lines.append(f"... 还有 {len(fixes) - 20} 个API未在报告中显示")
    
    # 保存报告
    report_file = "api_permission_fix_report.txt"
    with open(report_file, 'w', encoding='utf-8') as f:
        f.write('\n'.join(report_lines))
    
    print(f"修复报告已保存到: {report_file}")

if __name__ == "__main__":
    main()