#!/bin/bash
# 认领任务脚本
# 用法：./scripts/claim-task.sh <任务 ID> <机器人名称>

TASK=$1
ROBOT_NAME=$2
TASK_BOARD="docs/TASK_BOARD.md"

if [ -z "$TASK" ] || [ -z "$ROBOT_NAME" ]; then
    echo "❌ 用法：$0 <任务 ID> <机器人名称>"
    echo "示例：$0 T001 柳生"
    exit 1
fi

if [ ! -f "$TASK_BOARD" ]; then
    echo "❌ 任务看板不存在：$TASK_BOARD"
    exit 1
fi

# 更新任务状态为 WIP
sed -i "s/| $TASK | TODO | - |/| $TASK | WIP | $ROBOT_NAME |/" "$TASK_BOARD"
sed -i "s/| $TASK | todo | - |/| $TASK | WIP | $ROBOT_NAME |/" "$TASK_BOARD"

# 记录认领日志
echo "- $(date '+%Y-%m-%d %H:%M') - $ROBOT_NAME 认领任务 $TASK" >> docs/task-log.md

echo "✅ 任务 $TASK 已认领 by $ROBOT_NAME"
echo "📋 请阅读 docs/tasks/$TASK.md 了解任务详情"
