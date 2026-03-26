#!/bin/bash
# Feishu Chat Members Query Script
# Usage: ./list-chat-members.sh <chat_id> [account_id]

# Don't exit on error - we handle errors manually
set +e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OPENCLAW_DIR="${OPENCLAW_DIR:-$HOME/.openclaw}"

# Get parameters
CHAT_ID="$1"
ACCOUNT_ID="${2:-dev-1}"

if [ -z "$CHAT_ID" ]; then
    echo "❌ 错误：请提供 chat_id"
    echo "用法：./list-chat-members.sh <chat_id> [account_id]"
    exit 1
fi

echo "📋 查询群成员..."
echo "💬 Chat ID: $CHAT_ID"
echo "📋 账号：$ACCOUNT_ID"

# Read app credentials from openclaw.json
APP_ID=$(jq -r ".channels.feishu.accounts[\"$ACCOUNT_ID\"].appId" "$OPENCLAW_DIR/openclaw.json")
APP_SECRET=$(jq -r ".channels.feishu.accounts[\"$ACCOUNT_ID\"].appSecret" "$OPENCLAW_DIR/openclaw.json")

if [ "$APP_ID" = "null" ] || [ "$APP_SECRET" = "null" ]; then
    echo "❌ 错误：找不到账号 $ACCOUNT_ID 的配置"
    exit 1
fi

# Step 1: Get tenant access token
TOKEN_RESPONSE=$(curl -s -X POST "https://open.feishu.cn/open-apis/auth/v3/tenant_access_token/internal" \
  -H "Content-Type: application/json" \
  -d "{
    \"app_id\": \"$APP_ID\",
    \"app_secret\": \"$APP_SECRET\"
  }")

TENANT_ACCESS_TOKEN=$(echo "$TOKEN_RESPONSE" | jq -r '.tenant_access_token')
CODE=$(echo "$TOKEN_RESPONSE" | jq -r '.code')

if [ "$CODE" != "0" ]; then
    echo "❌ 获取令牌失败：$TOKEN_RESPONSE"
    exit 1
fi

# Step 2: Get chat info
CHAT_INFO=$(curl -s -X GET "https://open.feishu.cn/open-apis/im/v1/chats/$CHAT_ID" \
  -H "Authorization: Bearer $TENANT_ACCESS_TOKEN")

CODE=$(echo "$CHAT_INFO" | jq -r '.code')
if [ "$CODE" != "0" ]; then
    echo "❌ 查询群信息失败：$CHAT_INFO"
    exit 1
fi

# Extract chat info
CHAT_NAME=$(echo "$CHAT_INFO" | jq -r '.data.name')
OWNER_ID=$(echo "$CHAT_INFO" | jq -r '.data.owner_id')
BOT_COUNT=$(echo "$CHAT_INFO" | jq -r '.data.bot_count')
USER_COUNT=$(echo "$CHAT_INFO" | jq -r '.data.user_count')
TENANT_KEY=$(echo "$CHAT_INFO" | jq -r '.data.tenant_key')

# Step 3: Get current bot info
BOT_INFO=$(curl -s -X GET "https://open.feishu.cn/open-apis/bot/v3/info" \
  -H "Authorization: Bearer $TENANT_ACCESS_TOKEN")
CURRENT_BOT_OPEN_ID=$(echo "$BOT_INFO" | jq -r '.bot.open_id')
CURRENT_BOT_NAME=$(echo "$BOT_INFO" | jq -r '.bot.app_name')

# Step 4: Get member list
MEMBERS_RESPONSE=$(curl -s -X GET "https://open.feishu.cn/open-apis/im/v1/chats/$CHAT_ID/members" \
  -H "Authorization: Bearer $TENANT_ACCESS_TOKEN")

CODE=$(echo "$MEMBERS_RESPONSE" | jq -r '.code')
if [ "$CODE" != "0" ]; then
    echo "❌ 查询群成员失败：$MEMBERS_RESPONSE"
    exit 1
fi

# Parse members
TOTAL=$(echo "$MEMBERS_RESPONSE" | jq -r '.member_total // .data.member_total // .total // .data.total')
ITEMS=$(echo "$MEMBERS_RESPONSE" | jq -r '.items // .data.items')

echo "✅ 查询成功"
echo ""
echo "📊 群信息："
echo "  - 群名：$CHAT_NAME"
echo "  - 总人数：$TOTAL"
echo "  - 用户数：$USER_COUNT"
echo "  - 机器人数：$BOT_COUNT"
echo "  - 群主 ID: $OWNER_ID"
echo ""

# Step 5: Check each member to determine if bot or user
echo "👥 成员列表："
echo ""

MEMBER_COUNT=$(echo "$ITEMS" | jq 'length')
BOTS_FOUND=0
USERS_FOUND=0

for ((i=0; i<MEMBER_COUNT; i++)); do
    MEMBER=$(echo "$ITEMS" | jq -r ".[$i]")
    MEMBER_ID=$(echo "$MEMBER" | jq -r '.member_id')
    MEMBER_NAME=$(echo "$MEMBER" | jq -r '.name // .member_id')
    
    # Try to get user info - if fails with 41050, it's a bot
    USER_INFO=$(curl -s -X GET "https://open.feishu.cn/open-apis/contact/v3/users/$MEMBER_ID?user_id_type=open_id" \
      -H "Authorization: Bearer $TENANT_ACCESS_TOKEN")
    USER_CODE=$(echo "$USER_INFO" | jq -r '.code')
    
    # Determine member type
    if [ "$USER_CODE" = "41050" ]; then
        # It's a bot
        MEMBER_TYPE="🤖 机器人"
        ((BOTS_FOUND++))
    else
        # It's a user
        MEMBER_TYPE="👤 用户"
        ((USERS_FOUND++))
    fi
    
    # Check if owner
    ROLE=""
    if [ "$MEMBER_ID" = "$OWNER_ID" ]; then
        ROLE=" 👑 群主"
    fi
    
    echo "  - $MEMBER_NAME ($MEMBER_ID)$ROLE"
    echo "    类型：$MEMBER_TYPE"
done

echo ""

# Step 6: Get all bots from the same tenant
echo "🤖 同租户下的机器人（可能在此群中）："
echo ""

# Read all accounts from openclaw.json
ACCOUNTS=$(jq -r '.channels.feishu.accounts | keys[]' "$OPENCLAW_DIR/openclaw.json")

BOT_LIST="["
FIRST_BOT=true

for ACC in $ACCOUNTS; do
    ACC_APP_ID=$(jq -r ".channels.feishu.accounts[\"$ACC\"].appId" "$OPENCLAW_DIR/openclaw.json")
    ACC_APP_SECRET=$(jq -r ".channels.feishu.accounts[\"$ACC\"].appSecret" "$OPENCLAW_DIR/openclaw.json")
    
    # Get token for this account
    ACC_TOKEN_RESP=$(curl -s -X POST "https://open.feishu.cn/open-apis/auth/v3/tenant_access_token/internal" \
      -H "Content-Type: application/json" \
      -d "{\"app_id\": \"$ACC_APP_ID\", \"app_secret\": \"$ACC_APP_SECRET\"}")
    
    ACC_TOKEN=$(echo "$ACC_TOKEN_RESP" | jq -r '.tenant_access_token')
    ACC_CODE=$(echo "$ACC_TOKEN_RESP" | jq -r '.code')
    
    if [ "$ACC_CODE" != "0" ]; then
        continue
    fi
    
    # Get bot info
    ACC_BOT_INFO=$(curl -s -X GET "https://open.feishu.cn/open-apis/bot/v3/info" \
      -H "Authorization: Bearer $ACC_TOKEN")
    
    ACC_BOT_NAME=$(echo "$ACC_BOT_INFO" | jq -r '.bot.app_name')
    ACC_BOT_OPEN_ID=$(echo "$ACC_BOT_INFO" | jq -r '.bot.open_id')
    ACC_ACTIVATE_STATUS=$(echo "$ACC_BOT_INFO" | jq -r '.bot.activate_status')
    
    # Check if it's the current bot
    IS_CURRENT=""
    if [ "$ACC_BOT_OPEN_ID" = "$CURRENT_BOT_OPEN_ID" ]; then
        IS_CURRENT=" (当前)"
    fi
    
    # Check activation status
    STATUS_TEXT="未激活"
    if [ "$ACC_ACTIVATE_STATUS" = "2" ]; then
        STATUS_TEXT="已激活"
    fi
    
    echo "  - $ACC_BOT_NAME ($ACC_BOT_OPEN_ID)$IS_CURRENT"
    echo "    账号：$ACC | 状态：$STATUS_TEXT"
    
    # Add to JSON array
    if [ "$FIRST_BOT" = "true" ]; then
        FIRST_BOT=false
    else
        BOT_LIST="$BOT_LIST,"
    fi
    BOT_LIST="$BOT_LIST{\"account\":\"$ACC\",\"name\":\"$ACC_BOT_NAME\",\"open_id\":\"$ACC_BOT_OPEN_ID\",\"is_current\":$( [ "$ACC_BOT_OPEN_ID" = "$CURRENT_BOT_OPEN_ID" ] && echo "true" || echo "false" )}"
done

BOT_LIST="$BOT_LIST]"

echo ""
echo "📈 统计："
echo "  - 检测到的用户：$USERS_FOUND"
echo "  - 检测到的机器人：$BOTS_FOUND"
echo "  - 群配置机器人数：$BOT_COUNT"
echo "  - 同租户机器人总数：$(echo "$ACCOUNTS" | wc -w)"

# Return JSON for programmatic use
if [ "$3" = "--json" ]; then
    # Build members JSON
    MEMBERS_JSON="["
    for ((i=0; i<MEMBER_COUNT; i++)); do
        MEMBER=$(echo "$ITEMS" | jq -r ".[$i]")
        MEMBER_ID=$(echo "$MEMBER" | jq -r '.member_id')
        MEMBER_NAME=$(echo "$MEMBER" | jq -r '.name // .member_id')
        
        USER_INFO=$(curl -s -X GET "https://open.feishu.cn/open-apis/contact/v3/users/$MEMBER_ID?user_id_type=open_id" \
          -H "Authorization: Bearer $TENANT_ACCESS_TOKEN")
        USER_CODE=$(echo "$USER_INFO" | jq -r '.code')
        
        IS_BOT="false"
        if [ "$USER_CODE" = "41050" ]; then
            IS_BOT="true"
        fi
        
        IS_OWNER="false"
        if [ "$MEMBER_ID" = "$OWNER_ID" ]; then
            IS_OWNER="true"
        fi
        
        if [ $i -gt 0 ]; then
            MEMBERS_JSON="$MEMBERS_JSON,"
        fi
        MEMBERS_JSON="$MEMBERS_JSON{\"member_id\":\"$MEMBER_ID\",\"name\":\"$MEMBER_NAME\",\"is_bot\":$IS_BOT,\"is_owner\":$IS_OWNER}"
    done
    MEMBERS_JSON="$MEMBERS_JSON]"
    
    jq -n --arg chat_name "$CHAT_NAME" \
          --arg owner_id "$OWNER_ID" \
          --arg current_bot_id "$CURRENT_BOT_OPEN_ID" \
          --arg current_bot_name "$CURRENT_BOT_NAME" \
          --argjson bot_count "$BOT_COUNT" \
          --argjson user_count "$USER_COUNT" \
          --argjson total "$TOTAL" \
          --argjson members "$MEMBERS_JSON" \
          --argjson bots "$BOT_LIST" \
          '{
            chat_name: $chat_name,
            owner_id: $owner_id,
            bot_count: $bot_count,
            user_count: $user_count,
            total: $total,
            current_bot: {
              open_id: $current_bot_id,
              name: $current_bot_name
            },
            members: $members,
            tenant_bots: $bots
          }'
fi
