#!/bin/bash
#===============================================================================
# 读取最近 N 天的记忆文件
# 用法：./read-memory.sh [天数]
# 示例：./read-memory.sh 7   # 读取最近 7 天
#===============================================================================

DAYS=${1:-7}  # 默认 7 天

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MEMORY_DIR="$SCRIPT_DIR/memory"

echo "📖 读取最近 ${DAYS} 天的记忆文件"
echo "============================================="
echo ""

# 获取今天日期
TODAY=$(date +%Y-%m-%d)

# 读取最近 N 天的记忆文件
for i in $(seq 0 $((DAYS - 1))); do
    # 计算日期
    DATE=$(date -d "$TODAY - $i days" +%Y-%m-%d)
    FILE="$MEMORY_DIR/${DATE}.md"
    
    if [ -f "$FILE" ]; then
        echo "✅ ${DATE} (${i}天前)"
        echo "---"
        head -20 "$FILE"  # 显示前 20 行
        echo ""
        echo "..."
        echo ""
    else
        echo "⚠️  ${DATE} - 文件不存在"
        echo ""
    fi
done

echo "============================================="
echo "✨ 记忆读取完成"
echo ""
echo "提示：可以使用以下命令查看完整内容"
echo "  cat memory/2026-03-*.md | less"
echo ""
