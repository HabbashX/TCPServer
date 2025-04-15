package com.habbashx.tcpserver.listener.handler;

import com.habbashx.tcpserver.command.manager.MuteCommandManager;
import com.habbashx.tcpserver.event.handler.EventHandler;
import com.habbashx.tcpserver.event.Priority;
import com.habbashx.tcpserver.event.UserChatEvent;

import com.habbashx.tcpserver.listener.Listener;
import org.jetbrains.annotations.NotNull;

import static com.habbashx.tcpserver.logger.ConsoleColor.RED;
import static com.habbashx.tcpserver.logger.ConsoleColor.RESET;

@EventHandler(priority = Priority.HIGHEST)
public final class DefaultMutedUserHandler implements Listener<UserChatEvent> {

    private final MuteCommandManager muteCommandManager;

    public DefaultMutedUserHandler(MuteCommandManager muteCommandManager) {
        this.muteCommandManager = muteCommandManager;
    }

    @Override
    public void onEvent(@NotNull UserChatEvent userChatEvent) {
        if (muteCommandManager.isUserMuted(userChatEvent.getUsername())) {
            userChatEvent.setCancelled(true);
            userChatEvent.getUser().sendMessage(RED+"you`re muted"+RESET);
        }

    }
}
