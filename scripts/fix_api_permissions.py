#!/usr/bin/env python3
"""
API权限码修复脚本
用于为缺少权限码的API资源生成权限码
"""

import re
import sys
import psycopg2
from typing import Dict, List, Tuple

class APIPermissionFixer:
    def __init__(self, db_config=None):
        self.db_config = db_config or {
            'host': 'localhost',
            'port': 5432,
            'database': 'camera_construction_db',
            'user': 'postgres',
            'password': 'postgres'
        }
        
        # URI模式到模块的映射
        self.uri_module_map = {
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
        self.method_action_map = {
            'GET': 'view',
            'POST': 'create',
            'PUT': 'update',
            'DELETE': 'delete',
            'PATCH': 'patch'
        }
        
    def connect_db(self):
        """连接数据库"""
        try:
            conn = psycopg2.connect(**self.db_config)
            return conn
        except Exception as e:
            print(f"数据库连接失败: {e}")
            return None
    
    def get_missing_permission_apis(self):
        """获取缺少权限码的API资源"""
        conn = self.connect_db()
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
    
    def generate_permission_key(self, uri_pattern: str, method: str) -> str:
        """根据URI模式和方法生成权限码"""
        
        # 确定模块
        module = 'general'
        for prefix, mod in self.uri_module_map.items():
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
        action = self.method_action_map.get(method, 'unknown')
        
        # 生成权限码
        permission_key = f"{module}:{resource}:{action}:api"
        
        return permission_key
    
    def validate_permission_key(self, permission_key: str) -> bool:
        """验证权限码格式"""
        # 基本格式检查: module:resource:action:type
        pattern = r'^[a-z][a-z0-9_]*:[a-z][a-z0-9_]*:[a-z][a-z0-9_]*:api$'
        return bool(re.match(pattern, permission_key))
    
    def check_duplicate_permission_keys(self, permission_keys: List[Tuple[int, str]]) -> List[Tuple[str, List[int]]]:
        """检查重复的权限码"""
        key_map = {}
        for resource_id, perm_key in permission_keys:
            if perm_key not in key_map:
                key_map[perm_key] = []
            key_map[perm_key].append(resource_id)
        
        # 返回重复的权限码及其对应的资源ID
        duplicates = [(key, ids) for key, ids in key_map.items() if len(ids) > 1]
        return duplicates
    
    def generate_fix_sql(self, fixes: List[Dict]) -> str:
        """生成修复SQL脚本"""
        sql_lines = []
        sql_lines.append("-- API权限码修复脚本")
        sql_lines.append("-- 生成时间: 2026-04-07")
        sql_lines.append("")
        sql_lines.append("BEGIN;")
        sql_lines.append("")
        
        permission_keys = []
        
        for fix in fixes:
            resource_id = fix['id']
            permission_key = fix['permission_key']
            
            sql_lines.append(f"-- {fix['name']}")
            sql_lines.append(f"-- URI: {fix['uri_pattern']} [{fix['method']}]")
            sql_lines.append(f"UPDATE resource SET permission_key = '{permission_key}' WHERE id = {resource_id};")
            sql_lines.append("")
            
            permission_keys.append((resource_id, permission_key))
        
        # 检查重复
        duplicates = self.check_duplicate_permission_keys(permission_keys)
        if duplicates:
            sql_lines.append("-- 警告：以下权限码重复，需要手动调整")
            for perm_key, resource_ids in duplicates:
                sql_lines.append(f"-- 权限码 '{perm_key}' 重复出现在资源: {', '.join(map(str, resource_ids))}")
            sql_lines.append("")
        
        sql_lines.append("COMMIT;")
        
        return '\n'.join(sql_lines)
    
    def run_fix(self, dry_run=True):
        """运行修复"""
        print("开始修复API权限码...")
        
        # 获取缺少权限码的API
        apis = self.get_missing_permission_apis()
        print(f"找到 {len(apis)} 个缺少权限码的API")
        
        if not apis:
            print("没有需要修复的API")
            return
        
        # 生成修复方案
        fixes = []
        for api in apis:
            permission_key = self.generate_permission_key(
                api['uri_pattern'], 
                api['method']
            )
            
            if self.validate_permission_key(permission_key):
                fixes.append({
                    **api,
                    'permission_key': permission_key
                })
            else:
                print(f"警告：无法为API生成有效的权限码: {api['name']} ({api['uri_pattern']})")
        
        print(f"成功为 {len(fixes)} 个API生成权限码")
        
        # 生成SQL脚本
        sql_script = self.generate_fix_sql(fixes)
        
        # 保存脚本
        sql_file = "fix_api_permissions.sql"
        with open(sql_file, 'w', encoding='utf-8') as f:
            f.write(sql_script)
        
        print(f"SQL脚本已保存到: {sql_file}")
        
        if dry_run:
            print("\n当前为干运行模式，不会实际执行SQL")
            print("请先检查生成的SQL脚本，确认无误后再执行")
        else:
            print("\n开始执行SQL脚本...")
            self.execute_sql_script(sql_script)
        
        return fixes
    
    def execute_sql_script(self, sql_script: str):
        """执行SQL脚本"""
        conn = self.connect_db()
        if not conn:
            print("无法连接数据库，跳过执行")
            return
        
        try:
            cursor = conn.cursor()
            cursor.execute(sql_script)
            conn.commit()
            print("SQL脚本执行成功")
            
            # 验证修复结果
            cursor.execute("""
                SELECT COUNT(*) 
                FROM resource 
                WHERE type = 'API' 
                  AND (permission_key IS NULL OR permission_key = '')
            """)
            remaining = cursor.fetchone()[0]
            print(f"修复后，仍有 {remaining} 个API缺少权限码")
            
        except Exception as e:
            conn.rollback()
            print(f"SQL脚本执行失败: {e}")
        finally:
            conn.close()
    
    def generate_report(self, fixes: List[Dict]):
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
        report_lines.append("详细修复列表:")
        for i, fix in enumerate(fixes, 1):
            report_lines.append(f"{i:3d}. {fix['name']}")
            report_lines.append(f"      URI: {fix['uri_pattern']} [{fix['method']}]")
            report_lines.append(f"      权限码: {fix['permission_key']}")
            report_lines.append("")
        
        # 保存报告
        report_file = "api_permission_fix_report.txt"
        with open(report_file, 'w', encoding='utf-8') as f:
            f.write('\n'.join(report_lines))
        
        print(f"修复报告已保存到: {report_file}")

def main():
    """主函数"""
    print("API权限码修复工具")
    print("=" * 60)
    
    # 创建修复器
    fixer = APIPermissionFixer()
    
    # 运行修复（干运行模式）
    fixes = fixer.run_fix(dry_run=True)
    
    if fixes:
        # 生成报告
        fixer.generate_report(fixes)
        
        print("\n下一步操作:")
        print("1. 检查生成的SQL脚本: fix_api_permissions.sql")
        print("2. 检查修复报告: api_permission_fix_report.txt")
        print("3. 确认无误后，修改脚本中的 dry_run=False 并重新运行")
        print("4. 或者手动执行SQL脚本")
    else:
        print("没有需要修复的API")

if __name__ == "__main__":
    main()