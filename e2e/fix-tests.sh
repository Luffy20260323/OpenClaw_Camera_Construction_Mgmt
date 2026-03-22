#!/bin/bash
# 批量修复 E2E 测试文件的 baseURL 问题

cd /root/.openclaw/workspace/OpenClaw_Camera_Construction_Mgmt/e2e/tests

for file in *.spec.ts; do
    echo "修复 $file..."
    
    # 在 import 后添加 baseURL 配置
    if ! grep -q "const baseURL" "$file"; then
        sed -i "/import { test, expect } from '@playwright\/test';/a\\
\\
\/\/ 配置 baseURL\\
const baseURL = process.env.BASE_URL || 'http://localhost:8080';" "$file"
    fi
    
    # 替换 request.post('/api 为 request.post(\`\${baseURL}/api
    sed -i "s|request\.post('/api|request.post(\`\${baseURL}/api|g" "$file"
    sed -i "s|request\.get('/api|request.get(\`\${baseURL}/api|g" "$file"
    sed -i "s|request\.put('/api|request.put(\`\${baseURL}/api|g" "$file"
    sed -i "s|request\.delete('/api|request.delete(\`\${baseURL}/api|g" "$file"
    
    # 替换 page.goto('/ 为 page.goto(\`\${baseURL}/
    sed -i "s|page\.goto('/|page.goto(\`\${baseURL}/|g" "$file"
    
    echo "✅ $file 修复完成"
done

echo ""
echo "所有文件修复完成！"
