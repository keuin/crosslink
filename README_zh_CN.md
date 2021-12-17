# CrossLink: 基于规则路由的 BungeeCord/Velocity 消息互联框架

CrossLink 的目的是连接Minecraft群组服务器和即时通讯软件。

CrossLink 将每个子服务器的聊天窗口视为独立的端点，将每个即时通讯软件的群聊也视为独立的端点。
服务器管理员编写配置文件，描述消息将如何在这些端点之间互相转发，从而让玩家无论在哪个子服务器
或者哪个即时通讯软件里，都可以在一起聊天。

路由策略可根据需求灵活配置。由于 CrossLink 的代码完全不关心消息该从哪出现、又要被发送到哪里，
因此转发操作可被用户完全控制。用户将`按来源过滤`、`按内容过滤`、`字符串替换`、`丢弃`、`转发`
这些**基本操作（action）** 组合成为**路由规则（rule）**，有限个路由规则的有序排列形成**路由表**。
CrossLink 按照路由表转发消息，基本操作的组合可实现自由而强大的消息转发，从而
提供高度定制化的 Minecraft 群组服务器消息互联方案。


# 运行环境

- Minecraft 反向代理服务器：
  - BungeeCord （未测试，理论上完全兼容）
  - Waterfall （在 `waterfall-1.18-470` 版本测试通过）
  - Velocity （在 `velocity-3.1.1-98` 版本测试通过）

- Java 运行时环境：
  - OpenJDK 17 （在 `OpenJDK 64-Bit Server VM (build 17.0.1+12-LTS, mixed mode, sharing)` 版本测试通过）


# 配置说明

1. 在插件目录 `plugins` 里放置插件 `.jar` 文件。
2. 在此目录下新建子目录 `crosslink`。
3. 在子目录 `crosslink` 下新建 `messaging.json` 文本文件和 `api.json` 文本文件。
4. 按照以下样例编写配置文件。

## `messaging.json` 示例

```json5
{
    "remotes": [
        {
          "type": "telegram",
          "id": "Telegram",                       // this endpoint is identified with "remote:Telegram"
          "enabled": true,                        // default: true, if set to false, this remote will be ignored
          "token": "======SECRET======",          // Telegram Bot token
          "chat_id": 123456789,                   // repeat to and from this chat
          "proxy": "socks://127.0.0.1:10809",     // connect to Telegram API using this proxy
          "api": "https://my-telegram-api.com"    // url to custom Telegram API
        },
         {
             "type": "psmb",            // psmb is a special case, since it is not an endpoint
             "id": "mypsmb",            // this stub endpoint is identified with "remote:mypsmb"
             "enabled": true,           // but it creates zero or one or more than one sub "virtual" endpoints
             "host": "1.onesmp.org",
             "port": 3456,
             "subscribe_to": "chat_.+", // psmb subscription pattern
                                        // for example: if you have topic chat_1 and chat_2
                                        // then they are identified with "remote:mypsmb:chat_1" and "remote:mypsmb:chat_2"
                                        // dispatching messages from "remote:mypsmb" to virtual endpoints
                                        // such as "remote:mypsmb:chat_1", is done by psmb stub endpoint
             "topics": [                // all topics this endpoint can actually "see"
                 "chat_qq",             // regexp in "subscribe_to" and action route "to"
                 "chat_wechat"          // will only match endpoints declared in this list
             ]
         },
         {
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
            "object": "chat_message", // match chat messages
            "from": "remote:.*",      // regexp matching source,
                                      // only messages with matched source will be
                                      // processed by this rule, otherwise this rule is skipped
            "actions": [{
                "type": "format",
                "color": "green"
            }, {                      // actions run sequentially
                "type": "route",      // route this message to matched destinations
                "to": "server:.*"     // regexp matching destination  
            }]
        },
        {
            // outbound messages (starting with '#', server -> all remotes)
            "object": "chat_message",
            "from": "server:.*",
            "actions": [{
                "type": "filter",     // filter the message using given regexp
                                      // if the message does not match given pattern,
                                      // it won't be passed into subsequent actions
                "pattern": "#.+"      // match all messages starts with char '#'
            }, {
                "type": "replace",    // replace the message, removing heading '#'
                "from": "^#(.*)",     // capture all chars after the heading '#'
                "to": "$1"            // and make them as the output
            }, {
                "type": "route",      // send the message to all remotes
                "to": "remote:.*"
            }]
        },
        {
            // cross-server messages (server -> all other servers)
            "object": "chat_message",
            "from": "server:.*",
            "actions": [{
                "type": "route",
                "to": "server:.*",
                "backflow": false      // do not repeat to sender, true by default
                                       // since the destination pattern will match the source,
                                       // we have to set backflow to false to prevent
                                       // players from seeing duplicate messages
            }]
        }
    ]
}
```

## `api.json` 示例

```json5
{
    "host": "127.0.0.1",        // which host to listen on
    "port": 8008,               // which port to listen on
    "token": "===secret==="     // if defined, only requests with GET parameter `token`
                                // which equals to this value will be processed
                                // invalid requests will only get a 403 error
}
```


# 开源声明

CrossLink 至今的所有版本均在 GPLv3 协议下开源，您可以在 [CrossLink 项目主页](https://github.com/keuin/crosslink) 上找到他的源代码。
CrossLink 基于许多开源组件，这些开源组件的许可协议和声明可以在他们的项目主页找到：

- [java-annotations](https://github.com/JetBrains/java-annotations)
- [junit-5](https://github.com/junit-team/junit5)
- [Time4J](https://github.com/MenoData/Time4J)
- [Guice](https://github.com/google/guice)
- [adventure](https://github.com/KyoriPowered/adventure)
- [jackson-core](https://github.com/FasterXML/jackson-core)
- [gson](https://github.com/google/gson)
- [java-telegram-bot-api](https://github.com/pengrad/java-telegram-bot-api)