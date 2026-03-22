#!/bin/bash
# 清理 7 天前的 GitHub Actions artifact

REPO="RichardQidian/OpenClaw_Camera_Construction_Mgmt"
DAYS_OLD=7

echo "🧹 开始清理 $DAYS_OLD 天前的 artifact..."

# 获取 7 天前的日期
CUTOFF_DATE=$(date -d "$DAYS_OLD days ago" +%Y-%m-%dT%H:%M:%SZ 2>/dev/null || date -v-${DAYS_OLD}d +%Y-%m-%dT%H:%M:%SZ)

echo "📅 清理截止日期：$CUTOFF_DATE"

# 获取所有 workflow runs
RUNS=$(gh run list --repo $REPO --limit 500 --json databaseId,createdAt --jq ".[] | select(.createdAt < \"$CUTOFF_DATE\") | .databaseId")

COUNT=0
for RUN_ID in $RUNS; do
    echo "🗑️  删除 workflow run #$RUN_ID"
    gh run delete $RUN_ID --repo $REPO
    COUNT=$((COUNT + 1))
    sleep 1
done

echo ""
echo "✅ 清理完成！共删除 $COUNT 个旧的 workflow runs"
echo "💡 提示：GitHub Actions 存储空间 = 500MB，每个 artifact 默认保留 90 天"
echo "💡 建议：设置 retention-days: 1-7 天，定期清理旧 artifact"
