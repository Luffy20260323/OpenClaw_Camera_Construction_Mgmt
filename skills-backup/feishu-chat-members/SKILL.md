---
name: feishu-chat-members
description: |
  Feishu group chat member management. Activate when user sends /list-members or mentions group members, chat members, or wants to list/query members in a Feishu group.
---

# Feishu Chat Members Tool

Single tool `feishu_chat_members` for group member operations.

**Implementation:** TypeScript (Feishu plugin) - `/root/.openclaw/extensions/feishu/src/chat-members.ts`

**Legacy:** Shell script version at `skills/feishu-chat-members/list-members.sh` (deprecated)

## Trigger Conditions

Activate when user message contains:
- `/list-members` (with optional chat_id)
- `/list-members oc_xxxxxx`
- "这个群里有哪些人？"
- "群里有谁？"
- "列出群成员"
- "查询群成员"
- "群主是谁？"
- "群里有哪些机器人？"
- "群里有几个机器人？"

## Chat ID Extraction

From URL `https://xxx.feishu.cn/chat/oc_xxxxxx` → `chat_id` = `oc_xxxxxx`

From message context: Use `chat_id` from inbound metadata.

## Actions

### List Members (Basic)

```json
{ "action": "list", "chat_id": "oc_xxxxxx" }
```

Returns:
- `chat_name`: Group name
- `owner_id`: Owner's open_id
- `members`: Array of members with:
  - `member_id`: User's open_id
  - `name`: User's display name
  - `member_id_type`: ID type (open_id)
  - `tenant_key`: Tenant key
- `bot_count`: Number of bots in the group
- `user_count`: Number of human users
- `total`: Total member count
- `current_bot`: Current bot info with:
  - `open_id`: Bot's open_id
  - `name`: Bot name

### List Members (Detailed)

```json
{ "action": "list_detailed", "chat_id": "oc_xxxxxx" }
```

Returns all fields from `list` action, plus:
- `members`: Array with additional `is_bot` field for each member
- `tenant_bots`: Array of all bots configured in OpenClaw (same tenant) with:
  - `account`: Account ID from OpenClaw config
  - `name`: Bot name
  - `open_id`: Bot's open_id
  - `is_current`: true if this is the current bot
  - `activate_status`: Bot activation status (2 = activated)
  - `in_chat`: **true if this bot is actually in the group chat**, false if only configured but not in chat

**Important:** `tenant_bots` includes ALL bots from OpenClaw configuration, not just those in the chat. Check `in_chat` field to see which bots are actually group members.

## Example Output (list_detailed)

```json
{
  "chat_name": "团队 A 开发群",
  "owner_id": "ou_b204c0f15bdd51d183c3ce5a7d20fe2e",
  "bot_count": 5,
  "user_count": 2,
  "total": 7,
  "current_bot": {
    "open_id": "ou_0900e4a9853a4369b7010352d36d8a6c",
    "name": "OCT10-开发 1"
  },
  "members": [
    {
      "member_id": "ou_xxx",
      "name": "张三",
      "member_id_type": "open_id",
      "tenant_key": "xxxxx",
      "is_bot": false
    },
    {
      "member_id": "ou_yyy",
      "name": "李四",
      "member_id_type": "open_id",
      "tenant_key": "xxxxx",
      "is_bot": false
    },
    {
      "member_id": "ou_zzz",
      "name": "机器人 A",
      "member_id_type": "open_id",
      "tenant_key": "xxxxx",
      "is_bot": true
    }
  ],
  "tenant_bots": [
    {
      "account": "default",
      "name": "Emp10@HWC",
      "open_id": "ou_bfb219bf456b785c142da81b10fd9b5f",
      "is_current": false,
      "activate_status": 2,
      "in_chat": false
    },
    {
      "account": "dev-1",
      "name": "OCT10-开发 1",
      "open_id": "ou_0900e4a9853a4369b7010352d36d8a6c",
      "is_current": true,
      "activate_status": 2,
      "in_chat": true
    },
    {
      "account": "dev-2",
      "name": "OCT10-开发 2",
      "open_id": "ou_92f5b82758f1622edc38a48e14082dc4",
      "is_current": false,
      "activate_status": 2,
      "in_chat": false
    }
  ]
}
```

**Note:** `in_chat` field indicates whether the bot is actually a member of this group chat:
- `in_chat: true` → Bot is in the group
- `in_chat: false` → Bot is configured in OpenClaw but NOT in this group

## Human-Readable Output

```
📊 群信息：
  - 群名：团队 A 开发群
  - 总人数：7
  - 用户数：2
  - 机器人数：5
  - 群主 ID: ou_xxx

👥 成员列表：
  - 张三 (ou_xxx)
    类型：👤 用户
  - 李四 (ou_yyy) 👑 群主
    类型：👤 用户
  - 机器人 A (ou_zzz)
    类型：🤖 机器人

🤖 OpenClaw 配置的机器人（同租户）：
  - Emp10@HWC (ou_xxx)
    账号：default | 状态：已激活 | ❌ 不在此群
  - OCT10-开发 1 (ou_xxx) (当前)
    账号：dev-1 | 状态：已激活 | ✅ 在此群
  - OCT10-开发 2 (ou_xxx)
    账号：dev-2 | 状态：已激活 | ❌ 不在此群

📈 统计：
  - 检测到的用户：2
  - 检测到的机器人：1
  - 群配置机器人数：5
  - OpenClaw 配置机器人总数：3
  - 在此群的配置机器人：1
```

## Configuration

```yaml
channels:
  feishu:
    tools:
      chat_members: true # default: false
```

## Permissions

Required: `im:chat` or `im:chat:readonly`

## Usage Examples

**User:** "这个群里有哪些人？"
**Tool:** `{ "action": "list", "chat_id": "oc_xxx" }`

**User:** "详细列出群成员，区分用户和机器人"
**Tool:** `{ "action": "list_detailed", "chat_id": "oc_xxx" }`

**User:** "群主是谁？"
**Tool:** `{ "action": "list", "chat_id": "oc_xxx" }` → filter by owner_id

**User:** "群里有哪些机器人？"
**Tool:** `{ "action": "list_detailed", "chat_id": "oc_xxx" }` → Returns `tenant_bots` array

**User:** "群里有几个机器人？"
**Tool:** `{ "action": "count", "chat_id": "oc_xxx" }` → Returns `bot_count` from chat info

## Notes

- Feishu's member list API returns all members (users + bots) in the same list
- Bot detection: Call `contact.user.get` API - error code 41050 means it's a bot
- The `bot_count` from chat info shows how many bots are in the group
- `tenant_bots` shows **all bots configured in OpenClaw** (same tenant), NOT just bots in this chat
- Use `in_chat` field to distinguish:
  - `in_chat: true` → Bot is actually in this group chat
  - `in_chat: false` → Bot is configured in OpenClaw but NOT in this group
- `list_detailed` action is slower than `list` because it checks each member individually
- For large groups (>100 members), consider using `list` action for better performance
