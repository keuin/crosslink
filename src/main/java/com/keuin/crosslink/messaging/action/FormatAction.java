package com.keuin.crosslink.messaging.action;

import com.keuin.crosslink.messaging.message.IMessage;
import com.keuin.crosslink.messaging.util.Messaging;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class FormatAction extends BaseReplaceAction {
    public FormatAction(TextColor color) {
        super((message) -> {
            var formatted = Messaging.duplicate(message.kyoriMessage());
            return IMessage.create(message.source(), message.sender(), formatted.color(color));
        });
    }
    public FormatAction(TextDecoration decor) {
        super((message) -> {
            // FIXME clear other decorations
            var formatted = Messaging.duplicate(message.kyoriMessage());
            return IMessage.create(message.source(), message.sender(), formatted.decorate(decor));
        });
    }
    public FormatAction(Style style) {
        super((message) -> {
            // FIXME clear other styles
            var formatted = Messaging.duplicate(message.kyoriMessage());
            return IMessage.create(message.source(), message.sender(), formatted.style(style));
        });
    }

    @Override
    public String toString() {
        return "FormatAction{}";
    }
}
