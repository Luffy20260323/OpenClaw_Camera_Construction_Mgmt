#!/usr/bin/env python3
import re
import os

test_dir = '/root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/e2e/tests'
frontend_url = 'http://localhost:3000'

for filename in os.listdir(test_dir):
    if not filename.endswith('.spec.ts'):
        continue
    
    filepath = os.path.join(test_dir, filename)
    print(f'修复 {filename}...')
    
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # 添加 FRONTEND_URL 变量
    if 'const baseURL' in content and 'const FRONTEND_URL' not in content:
        content = content.replace(
            "const baseURL = process.env.BASE_URL || 'http://localhost:8080';",
            f"const baseURL = process.env.BASE_URL || 'http://localhost:8080';\n  const FRONTEND_URL = process.env.FRONTEND_URL || '{frontend_url}';"
        )
    
    # 替换 page.goto 使用 FRONTEND_URL
    content = re.sub(r"page\.goto\(`\$\{baseURL\}/login`\)", f"page.goto(`${{FRONTEND_URL}}/login`)", content)
    content = re.sub(r"page\.goto\(`\$\{baseURL\}/register`\)", f"page.goto(`${{FRONTEND_URL}}/register`)", content)
    
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)
    
    print(f'✅ {filename} 修复完成')

print('\n所有文件修复完成！')
