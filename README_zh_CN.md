# i18n README

| Language             | File                               |
|----------------------|------------------------------------|
| English (US)         | [README.md](README.md)             |
| Chinese (Simplified) | [README_zh_CN.md](README_zh_CN.md) |


# 1. CrossLink: 基于规则路由的 BungeeCord/Velocity 消息互联框架

CrossLink 的目的是连接Minecraft群组服务器和即时通讯软件。
CrossLink 是 [BungeeCross](https://github.com/hit-mc/BungeeCross) 的重构版本，大量代码被重写以改善软件质量。

CrossLink 将每个子服务器的聊天窗口视为独立的端点，将每个即时通讯软件的群聊也视为独立的端点。 服务器管理员编写配置文件，描述消息将如何在这些端点之间互相转发，从而让玩家无论在哪个子服务器 或者哪个即时通讯软件里，都可以在一起聊天。

路由策略可根据需求灵活配置。由于 CrossLink 的代码完全不关心消息该从哪出现、又要被发送到哪里， 因此转发操作可被用户完全控制。用户将`按来源过滤`、`按内容过滤`、`字符串替换`、`丢弃`、`转发`
这些**基本操作（action）** 组合成为**路由规则（rule）**，有限个路由规则的有序排列形成**路由表**。 CrossLink 按照路由表转发消息，基本操作的组合可实现自由而强大的消息转发，从而 提供高度定制化的
Minecraft 群组服务器消息互联方案。

目前 CrossLink 仅内置对 Telegram 的支持。如果您需要连接诸如微信、QQ 等即时通讯平台，建议为其编写独立的程序，并通过`psmb`协议连接到 CrossLink。


# 2. 运行环境

- Minecraft 反向代理服务器：
  - BungeeCord （未测试，理论上完全兼容）
  - Waterfall （在 `waterfall-1.18-470` 版本测试通过）
  - Velocity （在 `velocity-3.1.1-98` 版本测试通过）

- Java 运行时环境：
  - OpenJDK 17 （在 `OpenJDK 64-Bit Server VM (build 17.0.1+12-LTS, mixed mode, sharing)` 版本测试通过）


# 3. 配置说明

1. 在插件目录 `plugins` 里放置插件 `.jar` 文件。
2. 在此目录下新建子目录 `crosslink`。
3. 在子目录 `crosslink` 下新建 `messaging.json` 文本文件和 `api.json` 文本文件。
4. 按照以下样例编写配置文件。

## 3.1 `messaging.json` 示例

```json5
{
  "remotes": [
    {
      "type": "telegram",
      // this endpoint is identified with "remote:Telegram"
      "id": "Telegram",
      // default: true, if set to false, this remote will be ignored
      "enabled": true,
      // Telegram Bot token
      "token": "======SECRET======",
      // repeat to and from this chat
      "chat_id": 123456789,
      // connect to Telegram API using this proxy
      "proxy": "socks://127.0.0.1:10809",
      // url to custom Telegram API
      "api": "https://my-telegram-api.com"
    },
    {
      "type": "psmb",
      // this endpoint is identified as "remote:mypsmb"
      "id": "mypsmb",
      // but it creates zero or one or more than one sub "virtual" endpoints
      "enabled": true,
      "host": "1.onesmp.org",
      "port": 3456,
      // messages sent to this endpoint will be published to the psmb topic whose id is 'subscribe_to'
      "publish_to": "chat_mc",
      // messages from topics matched by this pattern will be present on this endpoint
      "subscribe_from": "chat_im*",
      // the unique subscription client id required by psmb protocol
      "subscriber_id": 1314,
      // send keep alive packet in every 20 seconds
      // if the value is ignored or not positive, keepalive will be disabled
      "keepalive": 20000
    },
    {
      // not implemented yet, may be added in future version
      "type": "json-rpc",
      "id": "rpc",
      "enabled": true,
      "listen": ["127.0.0.1", 8008],
      "methods": {
        "get": "getMessage",
        "put": "sendMessage"
      }
    }
  ],
  "routing": [
    // all rules are processed sequentially
    // a message may match multiple rules and thus may be duplicate in your case
    // if the message is dropped in an action in one rule,
    // (the action type is just "drop" and it does not have any argument)
    // all subsequent rules will NOT see this message
    {
      // inbound chat messages (remote -> all servers)
      "object": "chat_message",
      // match chat messages
      "from": "remote:.*",
      // regexp matching source,
      // only messages with matched source will be
      // processed by this rule, otherwise this rule is skipped
      "actions": [
        // actions run sequentially
        {
          "type": "format",
          "color": "green"
        },
        {
          // route this message to matched destinations
          "type": "route",
          // the regexp matching destinations
          "to": "server:.*"
        }
      ]
    },
    {
      // outbound messages (starting with '#', server -> all remotes)
      "object": "chat_message",
      "from": "server:.*",
      "actions": [
        {
          // filter the message using given regexp
          // if the message does not match given pattern,
          // it won't be passed into subsequent actions
          "type": "filter",
          // match all messages starts with char '#'
          "pattern": "#.+"
        },
        {
          // replace the message
          "type": "replace",
          // removing heading '#' capture all chars after the heading '#'
          // and make them as the output
          "from": "^#(.*)",
          "to": "$1"
        },
        {
          // send the message to all remotes
          "type": "route",
          "to": "remote:.*"
        }
      ]
    },
    {
      // cross-server messages (server -> all other servers)
      "object": "chat_message",
      "from": "server:.*",
      "actions": [
        {
          "type": "route",
          "to": "server:.*",
          // do not repeat to sender, true by default
          // since the destination pattern will match the source,
          // setting backflow to false will prevent
          // players from seeing duplicate messages
          "backflow": false
        }
      ]
    }
  ]
}
```

## 3.2 `api.json` 示例

```json5
{
    "host": "127.0.0.1",        // which host to listen on
    "port": 8008,               // which port to listen on
    "token": "===secret==="     // if defined, only requests with GET parameter `token`
                                // which equals to this value will be processed
                                // invalid requests will only get a 403 error
}
```


# 4. 开源声明

CrossLink 至今的所有版本均在 GPLv3 协议下开源，您可以在 [CrossLink 项目主页](https://github.com/keuin/crosslink) 上找到他的源代码。
CrossLink 基于许多开源组件，这些开源组件的许可协议和声明可以在他们的项目主页找到：

- [java-annotations](https://github.com/JetBrains/java-annotations)
- [junit-5](https://github.com/junit-team/junit5)
- [Time4J](https://github.com/MenoData/Time4J)
- [Guice](https://github.com/google/guice)
- [adventure](https://github.com/KyoriPowered/adventure)
- [jackson-core](https://github.com/FasterXML/jackson-core)
- [java-telegram-bot-api](https://github.com/pengrad/java-telegram-bot-api)
- [bson](https://mvnrepository.com/artifact/org.mongodb/bson)