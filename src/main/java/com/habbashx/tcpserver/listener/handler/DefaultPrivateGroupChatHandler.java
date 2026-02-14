package com.habbashx.tcpserver.listener.handler;

import com.habbashx.tcpserver.event.PrivateGroupChatEvent;
import com.habbashx.tcpserver.event.handler.EventHandler;
import com.habbashx.tcpserver.listener.Listener;
import org.jetbrains.annotations.NotNull;

import static com.habbashx.tcpserver.logger.ConsoleColor.BRIGHT_RED;
import static com.habbashx.tcpserver.logger.ConsoleColor.WHITE;

@EventHandler
public final class DefaultPrivateGroupChatHandler implements Listener<PrivateGroupChatEvent> {


    @Override
    public void onEvent(@NotNull PrivateGroupChatEvent event) {

        final String rolePrefix = event.getUserHandler().getUserDetails().getUserRole().getPrefix();
        final String username = event.getUserHandler().getUserDetails().getUsername();
        final String privateGroupPrefix = BRIGHT_RED + "[Private-Group] ";

        event.getPrivateGroup().broadcast(privateGroupPrefix + rolePrefix + username + ": " +WHITE+ event.getMessage());
    }
}
