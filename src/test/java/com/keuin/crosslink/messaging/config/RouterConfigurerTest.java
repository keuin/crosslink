package com.keuin.crosslink.messaging.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keuin.crosslink.messaging.config.router.RouterConfigurer;
import com.keuin.crosslink.messaging.rule.IRule;
import com.keuin.crosslink.messaging.rule.ObjectType;
import com.keuin.crosslink.testable.FakeRouter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RouterConfigurerTest {

    private static final String config = """
            [
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
                    "actions": [{             // actions run sequentially
                        "type": "route",      // route this message to matched destinations
                        "to": "server:.*"     // regexp matching destination \s
                    }, {
                        "type": "format",
                        "color": "green"
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
                        "from": "^#\\\\(.*\\\\)", // capture all chars after the heading '#'
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
                        "backflow": false  // do not repeat to sender, true by default
                                           // since the destination pattern will match the source,
                                           // we have to set backflow to false to prevent
                                           // players from seeing duplicate messages
                    }]
                }
            ]""";

    @Test
    void decode() throws ConfigSyntaxError, JsonProcessingException {
        var router = new FakeRouter();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        var rc = new RouterConfigurer(mapper.readTree(config));
        rc.configure(router);
        var chain = router.getRules();

        IRule r;
        assertEquals(3, chain.size());
        r = chain.get(0);
        assertEquals(r.object(), ObjectType.CHAT_MESSAGE);
        r = chain.get(1);
        assertEquals(r.object(), ObjectType.CHAT_MESSAGE);
        r = chain.get(2);
        assertEquals(r.object(), ObjectType.CHAT_MESSAGE);
        // TODO
    }
}