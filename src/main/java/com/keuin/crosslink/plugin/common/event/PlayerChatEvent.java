package com.keuin.crosslink.plugin.common.event;

@Deprecated
public interface PlayerChatEvent extends EventHandler {
    void onPlayerChat(String player, String server, String message);
}
