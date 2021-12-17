package com.keuin.crosslink.messaging.action;

import com.keuin.crosslink.messaging.message.IMessage;

import java.util.regex.Pattern;

// regexp replace
public class Re2placeAction extends BaseReplaceAction {
    public Re2placeAction(Pattern from, String to) {
        super((message) -> {
            // FIXME keep color information
            var content = from.matcher(message.pureString()).replaceAll(to);
            return IMessage.create(message.source(), message.sender(), content);
        });
    }

    @Override
    public String toString() {
        return "Re2placeAction{}";
    }
}
