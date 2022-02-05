# i18n README

| Language             | File                               |
|----------------------|------------------------------------|
| English (US)         | [README.md](README.md)             |
| Chinese (Simplified) | [README_zh_CN.md](README_zh_CN.md) |


# 1. CrossLink: rule-based intercommunicating framework for BungeeCord and Velocity

CrossLink aims at bridging Minecraft chats and instant messaging platforms.
CrossLink is a refactor of legacy [BungeeCross](https://github.com/hit-mc/BungeeCross).
Heavy rewrite has been made to improve software quality.

CrossLink treat every chat area in sub-servers of a Minecraft server group as separate *endpoints*. Different instant
messaging software are different endpoints, too. Server operator create configuration files, which describes how the
messages should be *routed* among distinct endpoints. This enables people from different Minecraft sub-server chats, even
different instant messaging platforms, together, in a tidy way.

The routing is highly configurable. CrossLink does not care where the message comes from,
and where it will ultimately go. The forwarding process is entirely controlled and defined by the server operator.
Server operator combines **actions** like `filter by source`, `filter by content`, `replace by regexp`, `discard`
and `forward` into **rules**. Finite rules line up a routing table. CrossLink forward every message according to
the routing table. The combination of **actions** is versatile to help build a highly configurable 
message forwarding system among Minecraft grouped servers and instant messaging platforms.

Currently, only Telegram is supported. If you wish to link to other instant messaging platforms,
implementing that protocol in a *standalone* program is encouraged, which could be linked to CrossLink via `psmb` protocol.


# 2. Environment Requirement

- Minecraft reverse proxy server：
    - BungeeCord (not tested, compatible in theory)
    - Waterfall (tested in `waterfall-1.18-470`)
    - Velocity (tested in `velocity-3.1.1-98`)

- Java Runtime：
    - OpenJDK 17 (tested in `OpenJDK 64-Bit Server VM (build 17.0.1+12-LTS, mixed mode, sharing)`)


# 3. Configuration

1. Copy plugin's `.jar` file to `plugins` folder.
2. Create subdirectory `crosslink` in that folder.
3. Create `messaging.json` and `api.json` in the folder created in previous step.
4. Copy and edit configuration like the examples below.

## 3.1 Example `messaging.json`

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

## 3.2 Example `api.json`

```json5
{
    "host": "127.0.0.1",        // which host to listen on
    "port": 8008,               // which port to listen on
    "token": "===secret==="     // if defined, only requests with GET parameter `token`
                                // which equals to this value will be processed
                                // invalid requests will only get a 403 error
}
```


# 4. Open-source notice

CrossLink is a free software and licensed under GPLv3.
You can find the source code in [CrossLink's Homepage](https://github.com/keuin/crosslink).

CrossLink is made with the help of many open-source libraries. You can obtain the licenses and notices from their homepage:

- [java-annotations](https://github.com/JetBrains/java-annotations)
- [junit-5](https://github.com/junit-team/junit5)
- [Time4J](https://github.com/MenoData/Time4J)
- [Guice](https://github.com/google/guice)
- [adventure](https://github.com/KyoriPowered/adventure)
- [jackson-core](https://github.com/FasterXML/jackson-core)
- [java-telegram-bot-api](https://github.com/pengrad/java-telegram-bot-api)
- [bson](https://mvnrepository.com/artifact/org.mongodb/bson)