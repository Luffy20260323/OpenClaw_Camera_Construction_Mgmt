# Feishu Chat Members Skill

查询飞书群聊成员列表的技能，支持识别群主和列出所有机器人。

## 功能

- ✅ 列出群聊中的所有人类成员
- ✅ 识别群主（👑 标记）
- ✅ 列出同租户下的所有机器人
- ✅ 显示机器人详细信息（名称、OpenID、账号、状态）
- ✅ 显示群基本信息（群名、总人数、用户数、机器人数）
- ✅ 支持 JSON 格式输出

## 使用方法

### 方式 1：发送指令（推荐）

在飞书聊天中发送以下任一指令：

```
/list-members
/list-members oc_xxxxxx
查询群成员
列出群成员
这个群有哪些人？
群主是谁？
群里有哪些机器人？
```

机器人会自动查询并返回结果。

### 方式 2：命令行测试

```bash
cd ~/.openclaw/extensions/feishu/skills/feishu-chat-members
./list-members.sh <chat_id> dev-1
```

带 JSON 输出：
```bash
./list-members.sh <chat_id> dev-1 --json
```

### 配置启用

在 `~/.openclaw/openclaw.json` 中添加：

```json
{
  "channels": {
    "feishu": {
      "tools": {
        "chat_members": true
      }
    }
  }
}
```

## 权限要求

需要在飞书开发者后台添加以下权限：
- `im:chat` 或 `im:chat:readonly`

## API 端点

- [获取群成员列表](https://open.feishu.cn/document/ukTMukTMukTMu4zN2YjN1MjN0YjN)
- [获取群详情](https://open.feishu.cn/document/ucTMzUjL2UDO44TNzUjN14SN1YSN)
- [获取机器人信息](https://open.feishu.cn/document/uUzLzUjL3UDM04SNzQjN1MjMzYjM)

## 输出示例

```
📊 群信息：
  - 群名：团队 A 开发群
  - 总人数：2
  - 用户数：2
  - 机器人数：5
  - 群主 ID: ou_b204c0f15bdd51d183c3ce5a7d20fe2e

👥 成员列表：
  - 严骏 (ou_73c017eac550e5ce63a1d3d3726d0769)
    类型：👤 用户
  - 柳生 (ou_b204c0f15bdd51d183c3ce5a7d20fe2e) 👑 群主
    类型：👤 用户

🤖 同租户下的机器人：
  - Emp10@HWC (ou_bfb219bf456b785c142da81b10fd9b5f)
    账号：default | 状态：已激活
  - OCT10-开发 1 (ou_0900e4a9853a4369b7010352d36d8a6c) (当前)
    账号：dev-1 | 状态：已激活
  - OCT10-开发 2 (ou_92f5b82758f1622edc38a48e14082dc4)
    账号：dev-2 | 状态：已激活
  - OCT10-文档 1 (ou_1166101a303d1239118a8c813857f31d)
    账号：doc-1 | 状态：已激活
  - OCT10-管理 1 (ou_bf8a6a30f3ccad4eba4a8515f35d16bb)
    账号：mgr-1 | 状态：已激活
  - OCT10-测试 1 (ou_8eb05909fa616aa165bcdcf76cda0e9e)
    账号：test-1 | 状态：已激活

📈 统计：
  - 检测到的用户：2
  - 群配置机器人数：5
  - 同租户机器人总数：6
```

## 注意事项

- 飞书 API 的成员列表不包含机器人，机器人通过查询租户配置获取
- `bot_count` 表示群中配置的机器人数量
- `tenant_bots` 列出同租户下的所有机器人（可能包含未在此群的机器人）
- 机器人状态：已激活 (2) / 未激活 (其他值)

## 文件结构

```
feishu-chat-members/
├── SKILL.md          # 技能定义
├── README.md         # 使用说明
└── list-members.sh   # 实现脚本
```
