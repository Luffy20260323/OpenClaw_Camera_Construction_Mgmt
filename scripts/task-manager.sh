#!/bin/bash
# 任务管理器 - 对话驱动的任务创建与执行
# 用法：./scripts/task-manager.sh <动作> [参数]

set -e

ACTION=$1
shift

TASK_BOARD="TASK_BOARD.md"
TASKS_DIR="tasks"
TASK_LOG="docs/task-log.md"

# 确保目录存在
mkdir -p "$TASKS_DIR"
mkdir -p "docs"

# 创建任务日志文件
if [ ! -f "$TASK_LOG" ]; then
    echo "# 任务执行日志" > "$TASK_LOG"
    echo "" >> "$TASK_LOG"
fi

# 获取当前时间
get_timestamp() {
    date '+%Y-%m-%d %H:%M'
}

# 获取下一个任务 ID
get_next_task_id() {
    local last_id=$(grep -oE '\| T[0-9]+ \|' "$TASK_BOARD" 2>/dev/null | tail -1 | grep -oE '[0-9]+' || echo "000")
    local next_num=$(printf "%03d" $((10#$last_id + 1)))
    echo "T$next_num"
}

# 从描述中识别优先级
identify_priority() {
    local desc="$1"
    if echo "$desc" | grep -qiE '紧急 | 马上 | 立刻 | 立即|P0'; then
        echo "P0"
    elif echo "$desc" | grep -qiE 'BUG|修复 | 缺陷 | 错误 | 故障|P1'; then
        echo "P1"
    elif echo "$desc" | grep -qiE '优化 | 改进 | 建议|P3'; then
        echo "P3"
    else
        echo "P2"
    fi
}

# 从描述中识别任务类型
identify_type() {
    local desc="$1"
    if echo "$desc" | grep -qiE 'BUG|修复 | 缺陷'; then
        echo "BUG 修复"
    elif echo "$desc" | grep -qiE '功能 | 开发 | 实现'; then
        echo "功能开发"
    elif echo "$desc" | grep -qiE '测试 | 验证'; then
        echo "测试"
    elif echo "$desc" | grep -qiE '文档 | 说明'; then
        echo "文档"
    else
        echo "其他"
    fi
}

# 创建任务
create_task() {
    local description="$1"
    local assignee="${2:-dev-2}"
    
    if [ -z "$description" ]; then
        echo "❌ 请提供任务描述"
        echo "用法：$0 create \"任务描述\" [责任人]"
        exit 1
    fi
    
    local task_id=$(get_next_task_id)
    local priority=$(identify_priority "$description")
    local task_type=$(identify_type "$description")
    local timestamp=$(get_timestamp)
    local task_file="$TASKS_DIR/${task_id}.md"
    
    # 创建任务详情文件
    cat > "$task_file" << EOF
# $task_id - $description

**状态:** TODO  
**责任人:** $assignee  
**优先级:** $priority  
**任务类型:** $task_type  
**创建时间:** $timestamp  
**完成时间:** -  

---

## 📋 任务说明

$description

## 🎯 完成标准

- [ ] _待补充_

## 🧩 子任务列表

_待分析拆解_

## 📝 执行记录

- $timestamp - 任务创建

---

**最后更新:** $timestamp
EOF

    # 添加到任务看板（在表头后第一行）
    local temp_file=$(mktemp)
    awk -F'|' -v id="$task_id" -v name="$description" -v owner="$assignee" -v priority="$priority" -v time="$timestamp" '
    BEGIN { OFS="|" }
    /^\| ID \|/ {
        print
        getline  # 跳过分隔行
        print "| " id " | " name " | TODO | " owner " | " priority " | " time " | - | - |"
        print
        next
    }
    { print }
    ' "$TASK_BOARD" > "$temp_file" && mv "$temp_file" "$TASK_BOARD"
    
    # 记录日志
    echo "- $timestamp - 创建任务 $task_id: $description (优先级:$priority, 责任人:$assignee)" >> "$TASK_LOG"
    
    # 输出汇报信息
    echo ""
    echo "📋 新任务已创建"
    echo ""
    echo "任务 ID: #$task_id"
    echo "任务名称：$description"
    echo "优先级：$priority ($(get_priority_name $priority))"
    echo "责任人：$assignee"
    echo "创建时间：$timestamp"
    echo ""
    echo "任务文件：$task_file"
    echo ""
    
    # 如果需要自动开始执行
    if [ "$AUTO_START" = "true" ]; then
        echo "🚀 自动开始执行..."
        claim_task "$task_id" "$assignee"
    fi
}

# 获取优先级名称
get_priority_name() {
    case "$1" in
        P0) echo "紧急" ;;
        P1) echo "高" ;;
        P2) echo "正常" ;;
        P3) echo "低" ;;
        *) echo "未知" ;;
    esac
}

# 认领任务
claim_task() {
    local task_id="$1"
    local assignee="${2:-$(whoami)}"
    local timestamp=$(get_timestamp)
    
    if [ -z "$task_id" ]; then
        echo "❌ 请提供任务 ID"
        exit 1
    fi
    
    local task_file="$TASKS_DIR/${task_id}.md"
    
    if [ ! -f "$task_file" ]; then
        echo "❌ 任务文件不存在：$task_file"
        exit 1
    fi
    
    # 更新任务状态
    sed -i "s/\*\*状态:\*\* TODO/\*\*状态:\*\* WIP/" "$task_file"
    sed -i "s/\*\*责任人:\*\*.*/\*\*责任人:\*\* $assignee/" "$task_file"
    sed -i "s/| $task_id | .* | TODO |/| $task_id | | WIP | $assignee |/" "$TASK_BOARD"
    
    # 添加执行记录
    echo "- $timestamp - $assignee 认领任务并开始执行" >> "$task_file"
    echo "" >> "$task_file"
    echo "**最后更新:** $timestamp" >> "$task_file"
    
    # 记录日志
    echo "- $timestamp - $assignee 认领任务 $task_id" >> "$TASK_LOG"
    
    echo ""
    echo "🚀 任务 $task_id 已开始执行"
    echo "责任人：$assignee"
    echo "开始时间：$timestamp"
    echo ""
}

# 汇报进度
report_progress() {
    local task_id="$1"
    local progress="$2"
    local details="$3"
    
    if [ -z "$task_id" ]; then
        echo "❌ 请提供任务 ID"
        exit 1
    fi
    
    local task_file="$TASKS_DIR/${task_id}.md"
    local timestamp=$(get_timestamp)
    
    if [ ! -f "$task_file" ]; then
        echo "❌ 任务文件不存在：$task_file"
        exit 1
    fi
    
    # 添加进度记录
    echo "- $timestamp - 进度汇报：$progress% - $details" >> "$task_file"
    echo "" >> "$task_file"
    echo "**最后更新:** $timestamp" >> "$task_file"
    
    echo ""
    echo "📊 任务进度汇报 #$task_id"
    echo ""
    echo "当前进度：$progress%"
    echo "详情：$details"
    echo "时间：$timestamp"
    echo ""
}

# 完成任务
complete_task() {
    local task_id="$1"
    local note="$2"
    
    if [ -z "$task_id" ]; then
        echo "❌ 请提供任务 ID"
        exit 1
    fi
    
    local task_file="$TASKS_DIR/${task_id}.md"
    local timestamp=$(get_timestamp)
    
    if [ ! -f "$task_file" ]; then
        echo "❌ 任务文件不存在：$task_file"
        exit 1
    fi
    
    # 更新任务状态为 REVIEW
    sed -i "s/\*\*状态:\*\* WIP/\*\*状态:\*\* REVIEW/" "$task_file"
    sed -i "s/\*\*完成时间:\*\* -/\*\*完成时间:\*\* $timestamp/" "$task_file"
    sed -i "s/| $task_id | .* | WIP |/| $task_id | | REVIEW | |/" "$TASK_BOARD"
    
    # 添加完成记录
    echo "- $timestamp - 任务完成：${note:-无}" >> "$task_file"
    echo "" >> "$task_file"
    echo "**最后更新:** $timestamp" >> "$task_file"
    
    # 记录日志
    echo "- $timestamp - 完成任务 $task_id: ${note:-无}" >> "$TASK_LOG"
    
    echo ""
    echo "✅ 任务 $task_id 已完成"
    echo "完成时间：$timestamp"
    echo "说明：${note:-无}"
    echo "状态：REVIEW (等待验收)"
    echo ""
}

# 拆解任务为子任务
decompose_task() {
    local task_id="$1"
    shift
    local subtasks=("$@")
    
    if [ -z "$task_id" ]; then
        echo "❌ 请提供任务 ID"
        exit 1
    fi
    
    local task_file="$TASKS_DIR/${task_id}.md"
    local timestamp=$(get_timestamp)
    
    if [ ! -f "$task_file" ]; then
        echo "❌ 任务文件不存在：$task_file"
        exit 1
    fi
    
    # 更新子任务列表
    local subtask_section="## 🧩 子任务列表\n\n"
    local i=1
    for subtask in "${subtasks[@]}"; do
        subtask_section+="- [ ] T${task_id}-$i: $subtask\n"
        ((i++))
    done
    
    # 使用 sed 更新子任务部分（简化处理）
    echo "- $timestamp - 任务拆解为 ${#subtasks[@]} 个子任务" >> "$task_file"
    
    echo ""
    echo "🧩 任务 $task_id 已拆解为 ${#subtasks[@]} 个子任务"
    echo ""
    for i in "${!subtasks[@]}"; do
        echo "  T${task_id}-$((i+1)): ${subtasks[$i]}"
    done
    echo ""
}

# 显示帮助
show_help() {
    cat << EOF
任务管理器 - 对话驱动的任务创建与执行

用法：$0 <动作> [参数]

动作:
  create "描述" [责任人]     创建新任务
  claim <任务 ID> [责任人]   认领任务
  progress <任务 ID> <进度%> "详情"  汇报进度
  complete <任务 ID> "说明"  完成任务
  decompose <任务 ID> "子任务 1" "子任务 2" ...  拆解任务
  list                      列出所有任务
  help                      显示帮助

示例:
  $0 create "修复登录页面 BUG" dev-1
  $0 claim T001 dev-2
  $0 progress T001 50 "前端修复完成"
  $0 complete T001 "已修复所有问题"
  $0 decompose T001 "前端修复" "后端修复" "测试验证"

EOF
}

# 主逻辑
case "$ACTION" in
    create)
        create_task "$@"
        ;;
    claim)
        claim_task "$@"
        ;;
    progress)
        report_progress "$@"
        ;;
    complete)
        complete_task "$@"
        ;;
    decompose)
        decompose_task "$@"
        ;;
    list)
        cat "$TASK_BOARD"
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        echo "❌ 未知动作：$ACTION"
        show_help
        exit 1
        ;;
esac
