#!/bin/bash
# 认领任务脚本（增强版）
# 用法：./scripts/claim-task.sh <任务 ID> [任务名称] [优先级]
# 示例：./scripts/claim-task.sh T007 "Bug 定位与修复" P0

set -e

TASK=$1
TASK_NAME=$2
PRIORITY=${3:-P2}
TASK_BOARD="docs/TASK_BOARD.md"
TASKS_DIR="docs/tasks"
TASK_FILE="$TASKS_DIR/$TASK.md"

# 获取当前机器人名称（从环境变量或参数）
ROBOT_NAME="${ROBOT_NAME:-$(whoami)}"

# 用法检查
if [ -z "$TASK" ]; then
    echo "❌ 用法：$0 <任务 ID> [任务名称] [优先级]"
    echo ""
    echo "参数说明:"
    echo "  任务 ID     - 如 T007"
    echo "  任务名称   - 任务简短描述（可选，如 \"Bug 定位与修复\"）"
    echo "  优先级     - P0/P1/P2/P3（可选，默认 P2）"
    echo ""
    echo "示例:"
    echo "  $0 T007 \"Bug 定位与修复\" P0"
    echo "  $0 T007 零部件管理 P1"
    echo "  $0 T007  # 简单认领已有任务"
    exit 1
fi

# 检查任务看板
if [ ! -f "$TASK_BOARD" ]; then
    echo "❌ 任务看板不存在：$TASK_BOARD"
    echo "💡 请先创建任务看板或确认项目已初始化"
    exit 1
fi

# 检查任务目录
if [ ! -d "$TASKS_DIR" ]; then
    echo "📁 创建任务目录：$TASKS_DIR"
    mkdir -p "$TASKS_DIR"
fi

echo "🔍 检查任务 $TASK ..."

# 检查任务是否已在看板中
if grep -q "| $TASK |" "$TASK_BOARD" 2>/dev/null; then
    # 任务已存在，检查状态
    current_status=$(grep "| $TASK |" "$TASK_BOARD" | head -1)
    
    if echo "$current_status" | grep -q "WIP"; then
        current_owner=$(echo "$current_status" | awk -F'|' '{print $4}' | tr -d ' ')
        if [ "$current_owner" = "$ROBOT_NAME" ]; then
            echo "ℹ️  任务 $TASK 已在你的进行中"
            echo "📋 任务详情：$TASK_FILE"
            exit 0
        else
            echo "⚠️  任务 $TASK 已被 $current_owner 认领"
            echo "💡 请选择其他任务或联系 $current_owner 协作"
            exit 1
        fi
    elif echo "$current_status" | grep -q "DONE"; then
        echo "⚠️  任务 $TASK 已完成，无需认领"
        exit 1
    fi
    
    # 更新状态为 WIP
    echo "📋 更新任务状态：TODO → WIP"
    sed -i "s/| $TASK |\([^|]*\)| TODO | - |/| $TASK |\1| WIP | $ROBOT_NAME |/" "$TASK_BOARD"
    
    # 如果任务文档不存在，创建它
    if [ ! -f "$TASK_FILE" ]; then
        echo "📝 创建任务文档：$TASK_FILE"
        cat > "$TASK_FILE" << EOF
# $TASK - ${TASK_NAME:-待补充任务名称}

**状态:** WIP  
**负责人:** $ROBOT_NAME  
**优先级:** $PRIORITY  
**创建时间:** $(date '+%Y-%m-%d %H:%M')  
**完成时间:** -  

---

## 📋 任务说明

_待补充_

## 🎯 完成标准

- [ ] _待补充_

## 📝 执行记录

- $(date '+%Y-%m-%d %H:%M') - $ROBOT_NAME 认领任务并开始执行

---

**最后更新:** $(date '+%Y-%m-%d %H:%M')
EOF
    fi
    
    echo "✅ 任务 $TASK 已认领"
    
else
    # 任务不存在，创建新任务
    echo "🆕 创建新任务：$TASK"
    
    # 如果没有提供任务名称，提示用户
    if [ -z "$TASK_NAME" ]; then
        echo "⚠️  未提供任务名称，使用默认名称"
        TASK_NAME="待补充任务名称"
    fi
    
    # 添加到任务看板（在表头后第一行）
    echo "📋 更新任务看板..."
    
    # 找到表格开始位置，插入新行
    awk -v task="$TASK" -v name="$TASK_NAME" -v owner="$ROBOT_NAME" -v priority="$PRIORITY" '
    /^\| ID \|/ {
        print
        getline  # 跳过分隔行
        print "| " task " | " name " | WIP | " owner " | " priority " | 新创建任务 |"
        print
        next
    }
    { print }
    ' "$TASK_BOARD" > "$TASK_BOARD.tmp" && mv "$TASK_BOARD.tmp" "$TASK_BOARD"
    
    # 创建任务文档
    echo "📝 创建任务文档：$TASK_FILE"
    cat > "$TASK_FILE" << EOF
# $TASK - $TASK_NAME

**状态:** WIP  
**负责人:** $ROBOT_NAME  
**优先级:** $PRIORITY  
**创建时间:** $(date '+%Y-%m-%d %H:%M')  
**完成时间:** -  

---

## 📋 任务说明

_待补充_

## 🎯 完成标准

- [ ] _待补充_

## 📝 执行记录

- $(date '+%Y-%m-%d %H:%M') - $ROBOT_NAME 创建并认领任务

---

**最后更新:** $(date '+%Y-%m-%d %H:%M')
EOF

    echo "✅ 新任务 $TASK 已创建并认领"
fi

# 记录日志
if [ -f "docs/task-log.md" ]; then
    echo "- $(date '+%Y-%m-%d %H:%M') - $ROBOT_NAME 认领任务 $TASK: $TASK_NAME" >> docs/task-log.md
fi

echo ""
echo "📋 下一步:"
echo "  1. 阅读任务详情：cat $TASK_FILE"
echo "  2. 补充任务说明和完成标准"
echo "  3. 开始执行任务"
echo ""
echo "💡 完成任务后运行:"
echo "  ./scripts/complete-task.sh $TASK \"完成说明\""
