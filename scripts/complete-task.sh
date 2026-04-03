#!/bin/bash
# 完成任务脚本
# 用法：./scripts/complete-task.sh <任务 ID> <完成说明>

TASK=$1
NOTE=$2
TASK_BOARD="docs/TASK_BOARD.md"

if [ -z "$TASK" ]; then
    echo "❌ 用法：$0 <任务 ID> [完成说明]"
    echo "示例：$0 T001 \"完成 API 开发\""
    exit 1
fi

if [ ! -f "$TASK_BOARD" ]; then
    echo "❌ 任务看板不存在：$TASK_BOARD"
    exit 1
fi

# 更新任务状态为 REVIEW
sed -i "s/| $TASK | WIP | [^|]* |/| $TASK | REVIEW | - |/" "$TASK_BOARD"
sed -i "s/| $TASK | wip | [^|]* |/| $TASK | REVIEW | - |/" "$TASK_BOARD"

# 记录完成日志
echo "- $(date '+%Y-%m-%d %H:%M') - 完成任务 $TASK: ${NOTE:-无}" >> docs/task-log.md

echo "✅ 任务 $TASK 已标记为 REVIEW"
echo "📝 完成说明：${NOTE:-无}"
echo "⏳ 等待验收后改为 DONE"
