const Lark = require("@larksuiteoapi/node-sdk");

const chatId = "oc_8b4c9d5a6bc96b56e91a4f2fa7c01ea0";
const appId = "cli_a93e6ea211b89cd2";
const appSecret = "ijzahjp7vEsDOEqxdWqVEdI8anBVFG7o";

const client = new Lark.Client({
  appId,
  appSecret,
  appType: Lark.AppType.SelfBuild,
  domain: Lark.Domain.Feishu,
});

async function getChatMembers() {
  try {
    // Get chat info
    const chatRes = await client.im.chat.get({ path: { chat_id: chatId } });
    console.log("Chat Info:", JSON.stringify(chatRes.data, null, 2));

    // Get members
    const membersRes = await client.im.chatMembers.get({
      path: { chat_id: chatId },
      params: { page_size: 100, member_id_type: "open_id" },
    });
    console.log("Members:", JSON.stringify(membersRes.data, null, 2));

    // Get bot info
    const botRes = await client.bot.getBotInfo();
    console.log("Bot Info:", JSON.stringify(botRes.data, null, 2));
  } catch (err) {
    console.error("Error:", err);
  }
}

getChatMembers();
