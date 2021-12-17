package com.keuin.crosslink.messaging.action;

import java.util.regex.Pattern;

public class ReFilterAction extends BaseFilterAction {
    public ReFilterAction(Pattern pattern) {
        super((message) -> pattern.matcher(message.pureString()).matches());
    }

    @Override
    public String toString() {
        return "ReFilterAction{}";
    }
}
