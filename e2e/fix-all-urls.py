#!/usr/bin/env python3
import re
import os

test_dir = '/root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/e2e/tests'

for filename in os.listdir(test_dir):
    if not filename.endswith('.spec.ts'):
        continue
    
    filepath = os.path.join(test_dir, filename)
    print(f'修复 {filename}...')
    
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # 替换所有 `/api 为 `${baseURL}/api
    content = re.sub(r'`/api/', r'`${baseURL}/api/', content)
    
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)
    
    print(f'✅ {filename} 修复完成')

print('\n所有文件修复完成！')
