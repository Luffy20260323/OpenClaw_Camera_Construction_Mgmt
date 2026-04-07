#!/usr/bin/env python3
"""
资源扫描脚本
用于扫描前端和后端文件，提取资源信息，并与数据库中的resource表进行比对
"""

import os
import re
import json
import psycopg2
from datetime import datetime
from pathlib import Path

class ResourceScanner:
    def __init__(self, db_config=None):
        self.db_config = db_config or {
            'host': 'localhost',
            'port': 5432,
            'database': 'camera_construction_db',
            'user': 'postgres',
            'password': 'postgres'
        }
        
        # 资源类型映射
        self.type_mapping = {
            'MODULE': '模块',
            'MENU': '菜单',
            'PAGE': '页面',
            'ELEMENT': '元素',
            'API': '接口',
            'PERMISSION': '权限'
        }
        
        # 扫描结果
        self.scan_results = {
            'frontend': [],
            'backend': [],
            'database': [],
            'issues': []
        }
        
    def connect_db(self):
        """连接数据库"""
        try:
            conn = psycopg2.connect(**self.db_config)
            return conn
        except Exception as e:
            print(f"数据库连接失败: {e}")
            return None
    
    def scan_frontend_resources(self, frontend_path):
        """扫描前端资源"""
        print("开始扫描前端资源...")
        
        # 扫描Vue组件文件
        vue_files = list(Path(frontend_path).rglob("*.vue"))
        print(f"找到 {len(vue_files)} 个Vue文件")
        
        for vue_file in vue_files:
            try:
                with open(vue_file, 'r', encoding='utf-8') as f:
                    content = f.read()
                
                # 提取组件信息
                component_info = self.extract_vue_component_info(vue_file, content)
                if component_info:
                    component_info['code_category'] = '前端'
                    self.scan_results['frontend'].append(component_info)
                    
            except Exception as e:
                print(f"处理文件 {vue_file} 时出错: {e}")
        
        # 扫描路由配置
        router_path = os.path.join(frontend_path, 'src/router/index.js')
        if os.path.exists(router_path):
            self.scan_router_config(router_path)
        
        print(f"前端资源扫描完成，找到 {len(self.scan_results['frontend'])} 个资源")
    
    def extract_vue_component_info(self, file_path, content):
        """从Vue组件中提取资源信息"""
        rel_path = str(file_path.relative_to(Path.cwd()))
        
        # 提取组件名称（从文件名）
        file_name = file_path.stem
        component_name = file_name
        
        # 尝试从template中提取按钮等元素
        elements = self.extract_vue_elements(content)
        
        # 确定资源类型
        resource_type = self.determine_frontend_resource_type(file_path, content)
        
        # 生成权限码（简化版，实际需要更复杂的逻辑）
        permission_key = self.generate_permission_key(file_path, resource_type)
        
        return {
            'file_path': rel_path,
            'name': component_name,
            'type': resource_type,
            'permission_key': permission_key,
            'elements': elements,
            'source_path': rel_path
        }
    
    def determine_frontend_resource_type(self, file_path, content):
        """确定前端资源类型"""
        path_str = str(file_path)
        
        # 根据路径判断
        if 'views/system' in path_str:
            if 'List.vue' in path_str or 'Management.vue' in path_str:
                return 'PAGE'
            elif 'Detail.vue' in path_str or 'Edit.vue' in path_str or 'Create.vue' in path_str:
                return 'PAGE'
            else:
                return 'PAGE'
        elif 'components/' in path_str:
            return 'ELEMENT'
        else:
            return 'PAGE'
    
    def extract_vue_elements(self, content):
        """从Vue模板中提取元素（按钮、表单等）"""
        elements = []
        
        # 查找按钮元素
        button_patterns = [
            r'<el-button[^>]*>([^<]+)</el-button>',
            r'<button[^>]*>([^<]+)</button>'
        ]
        
        for pattern in button_patterns:
            matches = re.findall(pattern, content, re.DOTALL)
            for match in matches:
                if match.strip():
                    elements.append({
                        'type': 'BUTTON',
                        'name': match.strip()[:50]
                    })
        
        return elements
    
    def scan_router_config(self, router_path):
        """扫描路由配置"""
        try:
            with open(router_path, 'r', encoding='utf-8') as f:
                content = f.read()
            
            # 简单提取路由信息（实际需要更复杂的解析）
            route_pattern = r"path:\s*['\"]([^'\"]+)['\"].*?component:\s*['\"]?([^,'\"]+)"
            matches = re.findall(route_pattern, content, re.DOTALL)
            
            for path, component in matches:
                self.scan_results['frontend'].append({
                    'code_category': '前端',
                    'type': 'PAGE',
                    'name': f"路由: {path}",
                    'web_path': path,
                    'component': component,
                    'permission_key': f"route:{path}:view"
                })
                
        except Exception as e:
            print(f"扫描路由配置时出错: {e}")
    
    def scan_backend_resources(self, backend_path):
        """扫描后端资源"""
        print("开始扫描后端资源...")
        
        # 扫描Java Controller文件
        java_files = list(Path(backend_path).rglob("*.java"))
        print(f"找到 {len(java_files)} 个Java文件")
        
        for java_file in java_files:
            try:
                with open(java_file, 'r', encoding='utf-8') as f:
                    content = f.read()
                
                # 检查是否是Controller
                if '@RestController' in content or '@Controller' in content:
                    api_resources = self.extract_java_controller_info(java_file, content)
                    for resource in api_resources:
                        resource['code_category'] = '后端'
                        self.scan_results['backend'].append(resource)
                        
            except Exception as e:
                print(f"处理文件 {java_file} 时出错: {e}")
        
        print(f"后端资源扫描完成，找到 {len(self.scan_results['backend'])} 个资源")
    
    def extract_java_controller_info(self, file_path, content):
        """从Java Controller中提取API信息"""
        resources = []
        
        # 提取类名和RequestMapping
        class_name_match = re.search(r'class\s+(\w+)\s*(?:extends|implements|\{)', content)
        class_name = class_name_match.group(1) if class_name_match else 'Unknown'
        
        # 提取类级别的RequestMapping
        class_mapping_match = re.search(r'@RequestMapping\(["\']([^"\']+)["\']\)', content)
        class_path = class_mapping_match.group(1) if class_mapping_match else ''
        
        # 提取方法级别的注解
        method_patterns = [
            (r'@GetMapping\(["\']([^"\']+)["\']\)', 'GET'),
            (r'@PostMapping\(["\']([^"\']+)["\']\)', 'POST'),
            (r'@PutMapping\(["\']([^"\']+)["\']\)', 'PUT'),
            (r'@DeleteMapping\(["\']([^"\']+)["\']\)', 'DELETE'),
            (r'@PatchMapping\(["\']([^"\']+)["\']\)', 'PATCH'),
            (r'@RequestMapping\(.*?value\s*=\s*["\']([^"\']+)["\'].*?method\s*=\s*RequestMethod\.(\w+)', None)  # 需要特殊处理
        ]
        
        for pattern, method in method_patterns:
            matches = re.findall(pattern, content)
            for match in matches:
                if isinstance(match, tuple):
                    path, method_type = match
                    method = method_type
                else:
                    path = match
                
                full_path = f"{class_path}{path}".replace('//', '/')
                
                # 提取方法名
                method_name_match = re.search(rf'public[^{{]+{re.escape(path)}[^{{]+(\w+)\s*\(', content)
                method_name = method_name_match.group(1) if method_name_match else 'unknown'
                
                resources.append({
                    'file_path': str(file_path.relative_to(Path.cwd())),
                    'class_name': class_name,
                    'method_name': method_name,
                    'type': 'API',
                    'name': f"{class_name}.{method_name}",
                    'uri_pattern': full_path,
                    'method': method,
                    'permission_key': self.generate_api_permission_key(class_name, method_name, full_path, method)
                })
        
        return resources
    
    def generate_permission_key(self, file_path, resource_type):
        """生成权限码"""
        path_str = str(file_path)
        
        # 根据文件路径生成权限码
        if resource_type == 'PAGE':
            # 页面权限码格式: module:page:action:page
            if 'system' in path_str:
                page_name = file_path.stem.lower()
                return f"system:{page_name}:view:page"
        elif resource_type == 'API':
            # API权限码格式: module:resource:action:api
            return "api:temp:view:api"
        elif resource_type == 'ELEMENT':
            # 元素权限码格式: module:element:action:element
            return "element:temp:view:element"
        
        return None
    
    def generate_api_permission_key(self, class_name, method_name, path, http_method):
        """生成API权限码"""
        # 简化版，实际需要根据业务规则生成
        module = class_name.replace('Controller', '').lower()
        action = method_name.lower()
        return f"{module}:{action}:{http_method.lower()}:api"
    
    def load_database_resources(self):
        """从数据库加载现有资源"""
        print("加载数据库中的资源...")
        
        conn = self.connect_db()
        if not conn:
            return
        
        try:
            cursor = conn.cursor()
            
            # 查询所有资源
            cursor.execute("""
                SELECT id, name, code, type, permission_key, parent_id, icon, 
                       path, component, module_code, file_path, required_permission,
                       deleted, deleted_at, deleted_by, is_top_level
                FROM resource
                ORDER BY type, name
            """)
            
            columns = [desc[0] for desc in cursor.description]
            for row in cursor.fetchall():
                resource = dict(zip(columns, row))
                resource['code_category'] = '数据库'
                self.scan_results['database'].append(resource)
            
            print(f"从数据库加载了 {len(self.scan_results['database'])} 个资源")
            
        except Exception as e:
            print(f"加载数据库资源时出错: {e}")
        finally:
            conn.close()
    
    def analyze_issues(self):
        """分析问题"""
        print("开始分析资源问题...")
        
        # 1. 检查缺失的权限码
        for resource in self.scan_results['database']:
            if not resource.get('permission_key'):
                self.scan_results['issues'].append({
                    'type': 'MISSING_PERMISSION_KEY',
                    'resource_id': resource.get('id'),
                    'resource_name': resource.get('name'),
                    'message': '资源缺少权限码'
                })
        
        # 2. 检查重复的权限码
        permission_keys = {}
        for resource in self.scan_results['database']:
            perm_key = resource.get('permission_key')
            if perm_key:
                if perm_key in permission_keys:
                    self.scan_results['issues'].append({
                        'type': 'DUPLICATE_PERMISSION_KEY',
                        'permission_key': perm_key,
                        'resource1': permission_keys[perm_key],
                        'resource2': resource.get('id'),
                        'message': f'权限码重复: {perm_key}'
                    })
                else:
                    permission_keys[perm_key] = resource.get('id')
        
        # 3. 检查孤立的资源（parent_id指向不存在的资源）
        resource_ids = {r['id'] for r in self.scan_results['database'] if r.get('id')}
        for resource in self.scan_results['database']:
            parent_id = resource.get('parent_id')
            if parent_id and parent_id not in resource_ids:
                self.scan_results['issues'].append({
                    'type': 'ORPHAN_RESOURCE',
                    'resource_id': resource.get('id'),
                    'resource_name': resource.get('name'),
                    'parent_id': parent_id,
                    'message': f'资源的父资源不存在: parent_id={parent_id}'
                })
        
        print(f"分析完成，发现 {len(self.scan_results['issues'])} 个问题")
    
    def generate_report(self):
        """生成扫描报告"""
        print("生成扫描报告...")
        
        report = {
            'scan_time': datetime.now().isoformat(),
            'summary': {
                'frontend_resources': len(self.scan_results['frontend']),
                'backend_resources': len(self.scan_results['backend']),
                'database_resources': len(self.scan_results['database']),
                'issues_found': len(self.scan_results['issues'])
            },
            'frontend_resources': self.scan_results['frontend'],
            'backend_resources': self.scan_results['backend'],
            'database_resources': self.scan_results['database'],
            'issues': self.scan_results['issues']
        }
        
        # 保存报告
        report_file = f"resource_scan_report_{datetime.now().strftime('%Y%m%d_%H%M%S')}.json"
        with open(report_file, 'w', encoding='utf-8') as f:
            json.dump(report, f, ensure_ascii=False, indent=2)
        
        print(f"报告已保存到: {report_file}")
        return report_file
    
    def generate_fix_scripts(self):
        """生成修复脚本"""
        print("生成修复脚本...")
        
        scripts = []
        
        # 生成SQL修复脚本
        sql_script = []
        sql_script.append("-- 资源数据修复脚本")
        sql_script.append("-- 生成时间: " + datetime.now().isoformat())
        sql_script.append("")
        
        # 修复缺失的权限码
        for issue in self.scan_results['issues']:
            if issue['type'] == 'MISSING_PERMISSION_KEY':
                resource_id = issue.get('resource_id')
                resource_name = issue.get('resource_name')
                
                # 生成权限码
                perm_key = self.generate_fix_permission_key(resource_name)
                if perm_key:
                    sql_script.append(f"-- 修复资源: {resource_name} (ID: {resource_id})")
                    sql_script.append(f"UPDATE resource SET permission_key = '{perm_key}' WHERE id = {resource_id};")
                    sql_script.append("")
        
        # 保存SQL脚本
        if len(sql_script) > 4:  # 不只是头部信息
            sql_file = f"fix_resources_{datetime.now().strftime('%Y%m%d_%H%M%S')}.sql"
            with open(sql_file, 'w', encoding='utf-8') as f:
                f.write('\n'.join(sql_script))
            scripts.append(sql_file)
            print(f"SQL修复脚本已保存到: {sql_file}")
        
        return scripts
    
    def generate_fix_permission_key(self, resource_name):
        """为缺失权限码的资源生成修复用的权限码"""
        # 简化版，实际需要根据资源类型和名称生成
        if not resource_name:
            return None
        
        # 移除特殊字符，转换为小写
        clean_name = re.sub(r'[^\w\s]', '', resource_name.lower())
        clean_name = re.sub(r'\s+', '_', clean_name)
        
        return f"fix:{clean_name}:view:temp"
    
    def run_scan(self, frontend_path, backend_path):
        """运行完整扫描"""
        print("=" * 60)
        print("开始资源扫描")
        print("=" * 60)
        
        # 扫描前端
        self.scan_frontend_resources(frontend_path)
        
        # 扫描后端
        self.scan_backend_resources(backend_path)
        
        # 加载数据库资源
        self.load_database_resources()
        
        # 分析问题
        self.analyze_issues()
        
        # 生成报告
        report_file = self.generate_report()
        
        # 生成修复脚本
        fix_scripts = self.generate_fix_scripts()
        
        print("=" * 60)
        print("扫描完成")
        print("=" * 60)
        
        return {
            'report_file': report_file,
            'fix_scripts': fix_scripts,
            'results': self.scan_results
        }

def main():
    """主函数"""
    # 配置路径
    base_dir = os.path.dirname(os.path.abspath(__file__))
    project_root = os.path.dirname(base_dir)
    
    frontend_path = os.path.join(project_root, 'frontend')
    backend_path = os.path.join(project_root, 'backend')
    
    # 检查路径是否存在
    if not os.path.exists(frontend_path):
        print(f"前端路径不存在: {frontend_path}")
        return
    
    if not os.path.exists(backend_path):
        print(f"后端路径不存在: {backend_path}")
        return
    
    # 创建扫描器并运行
    scanner = ResourceScanner()
    scanner