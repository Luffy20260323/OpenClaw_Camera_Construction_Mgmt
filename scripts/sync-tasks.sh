#!/bin/bash
# 任务自动同步脚本
# 用法：./scripts/sync-tasks.sh [--dry-run] [--verbose]
# 功能：扫描机器人会话，自动同步任务状态到看板

set -e

TASK_BOARD="docs/TASK_BOARD.md"
TASK_LOG="docs/task-log.md"
SYNC_STATE_FILE=".sync-state.json"
DRY_RUN=false
VERBOSE=false

# 解析参数
while [[ $# -gt 0 ]]; do
    case $1 in
        --dry-run)
            DRY_RUN=true
            shift
            ;;
        --verbose)
            VERBOSE=true
            shift
            ;;
        *)
            echo "❌ 未知参数：$1"
            echo "用法：$0 [--dry-run] [--verbose]"
            exit 1
            ;;
    esac
done

log() {
    if [ "$VERBOSE" = true ]; then
        echo "$@"
    fi
}

info() {
    echo "$@"
}

# 检查任务看板是否存在
if [ ! -f "$TASK_BOARD" ]; then
    echo "❌ 任务看板不存在：$TASK_BOARD"
    exit 1
fi

info "🔄 开始任务同步..."

# 读取上次同步状态
LAST_SYNC_TIME=0
LAST_TASKS="{}"
if [ -f "$SYNC_STATE_FILE" ]; then
    LAST_SYNC_TIME=$(cat "$SYNC_STATE_FILE" | grep -o '"lastSyncTime":[0-9]*' | cut -d':' -f2 || echo "0")
fi

CURRENT_TIME=$(date +%s)
SYNC_INTERVAL=300  # 5 分钟同步间隔

# 检查是否需要同步
if [ $((CURRENT_TIME - LAST_SYNC_TIME)) -lt $SYNC_INTERVAL ]; then
    log "⏭️  距离上次同步不足 5 分钟，跳过"
    exit 0
fi

# 扫描当前活跃的机器人会话
log "📡 扫描机器人会话..."

# 获取当前工作目录中的机器人活动
ACTIVE_ROBOTS=()
TASKS_FOUND=()

# 方法 1：检查 sessions_list（如果有 openclaw 命令）
if command -v openclaw &> /dev/null; then
    log "  → 使用 openclaw 命令扫描会话"
    # 这里简化处理，实际应该调用 openclaw sessions_list
fi

# 方法 2：检查任务目录中的执行记录
log "  → 扫描任务文档中的执行记录..."
for task_file in docs/tasks/T*.md; do
    if [ -f "$task_file" ]; then
        task_id=$(basename "$task_file" .md)
        
        # 检查是否有最近的执行记录（24 小时内）
        if grep -q "$(date '+%Y-%m-%d')" "$task_file" 2>/dev/null; then
            # 提取负责人（改进的正则表达式）
            owner=$(grep -E "^\*\*负责人:\*\*" "$task_file" | head -1 | sed 's/.*负责人:\*\* *//;s/\*\*$//' || echo "")
            if [ -z "$owner" ]; then
                owner=$(grep -E "^负责人:" "$task_file" | head -1 | cut -d':' -f2 | sed 's/^[[:space:]]*//;s/[[:space:]]*$//' || echo "")
            fi
            
            if [ -n "$owner" ]; then
                # 清理负责人名称（只移除首尾空白）
                owner_clean=$(echo "$owner" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')
                ACTIVE_ROBOTS+=("$owner_clean")
                TASKS_FOUND+=("$task_id:$owner_clean")
                log "  ✓ 发现活跃任务：$task_id (负责人：$owner_clean)"
            fi
        fi
    fi
done

# 方法 3：检查 git 提交记录（过去 24 小时）
log "  → 扫描 git 提交记录..."
if [ -d ".git" ]; then
    RECENT_COMMITS=$(git log --since="24 hours ago" --format="%an" 2>/dev/null | sort -u || echo "")
    if [ -n "$RECENT_COMMITS" ]; then
        while IFS= read -r author; do
            if [ -n "$author" ] && [[ ! " ${ACTIVE_ROBOTS[@]} " =~ " ${author} " ]]; then
                ACTIVE_ROBOTS+=("$author")
                log "  ✓ 发现活跃开发者：$author"
            fi
        done <<< "$RECENT_COMMITS"
    fi
fi

# 同步任务状态到看板
log "📋 同步任务状态..."

SYNCED_COUNT=0

for task_info in "${TASKS_FOUND[@]}"; do
    task_id=$(echo "$task_info" | cut -d':' -f1)
    owner=$(echo "$task_info" | cut -d':' -f2)
    
    # 检查任务在看板中的当前状态
    current_status=$(grep "| $task_id |" "$TASK_BOARD" | head -1 || echo "")
    
    if [ -n "$current_status" ]; then
        # 任务已在看板中，检查状态是否需要更新
        if echo "$current_status" | grep -q "TODO"; then
            # TODO → WIP
            if [ "$DRY_RUN" = true ]; then
                info "  [DRY-RUN] 更新 $task_id: TODO → WIP ($owner)"
            else
                sed -i "s/| $task_id |\([^|]*\)| TODO | - |/| $task_id |\1| WIP | $owner |/" "$TASK_BOARD"
                info "  ✓ 更新 $task_id: TODO → WIP ($owner)"
                SYNCED_COUNT=$((SYNCED_COUNT + 1))
            fi
        elif echo "$current_status" | grep -q "WIP"; then
            # 已经是 WIP，更新负责人
            if ! echo "$current_status" | grep -q "$owner"; then
                if [ "$DRY_RUN" = true ]; then
                    info "  [DRY-RUN] 更新 $task_id 负责人：$owner"
                else
                    sed -i "s/| $task_id | WIP | [^|]* |/| $task_id | WIP | $owner |/" "$TASK_BOARD"
                    info "  ✓ 更新 $task_id 负责人：$owner"
                    SYNCED_COUNT=$((SYNCED_COUNT + 1))
                fi
            fi
        fi
    else
        # 任务不在看板中，可能是新任务，需要手动添加
        log "  ⚠️  任务 $task_id 不在看板中，需要手动添加"
    fi
done

# 检查是否有活跃机器人但没有任务记录
for robot in "${ACTIVE_ROBOTS[@]}"; do
    # 清理机器人名称（只移除首尾空白，保留中间空格）
    robot_clean=$(echo "$robot" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')
    
    # 检查这个机器人是否有 WIP 任务
    if grep -q "WIP.*${robot_clean}" "$TASK_BOARD"; then
        log "  ✓ 机器人 $robot_clean 有 WIP 任务"
    else
        info "  ⚠️  机器人 $robot_clean 活跃但没有 WIP 任务，请确认是否需要认领任务"
    fi
done

# 更新同步状态
if [ "$DRY_RUN" = false ]; then
    cat > "$SYNC_STATE_FILE" << EOF
{
    "lastSyncTime": $CURRENT_TIME,
    "lastSyncBy": "$(whoami)",
    "syncedTasks": $SYNCED_COUNT
}
EOF
fi

# 记录同步日志
if [ "$DRY_RUN" = false ] && [ $SYNCED_COUNT -gt 0 ]; then
    echo "- $(date '+%Y-%m-%d %H:%M') - 自动同步：更新 $SYNCED_COUNT 个任务状态" >> "$TASK_LOG"
fi

# 输出总结
echo ""
echo "✅ 同步完成"
echo "   活跃机器人：${#ACTIVE_ROBOTS[@]}"
echo "   同步任务：$SYNCED_COUNT"
echo "   下次同步：5 分钟后"

if [ "$DRY_RUN" = true ]; then
    echo ""
    echo "📝 这是预演模式，未实际修改文件"
fi
