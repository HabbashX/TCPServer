package com.habbashx.tcpserver.listener.handler;

import com.habbashx.tcpserver.event.handler.EventHandler;
import com.habbashx.tcpserver.event.Priority;
import com.habbashx.tcpserver.event.UserChatEvent;

import com.habbashx.tcpserver.listener.Listener;
import com.habbashx.tcpserver.socket.Server;
import org.jetbrains.annotations.NotNull;

import static com.habbashx.tcpserver.logger.ConsoleColor.RED;
import static com.habbashx.tcpserver.logger.ConsoleColor.RESET;

@EventHandler(priority = Priority.LOW )
public final class DefaultChatHandler implements Listener<UserChatEvent> {

    private final Server server;

    public DefaultChatHandler(Server server) {
        this.server = server;
    }

    @Override
    public void onEvent(@NotNull UserChatEvent userChatEvent) {

        if (!userChatEvent.getMessage().isEmpty()) {
            if (userChatEvent.getUser().hasPermission(0X07)) {
                sendMessage(userChatEvent);
            } else {
                if (!userChatEvent.isOnCooldown()) {
                    sendMessage(userChatEvent);
                } else {
                    final int remainingCoolDown = (int) userChatEvent.getRemainingCooldown();
                    userChatEvent.getUser().sendMessage(RED + "you`re on cooldown for " + remainingCoolDown + " second" + RESET);
                }
            }
        } else {
            userChatEvent.getUser().sendMessage(RED+"you cannot send empty message"+RESET);
        }
    }

    private void sendMessage(@NotNull UserChatEvent userChatEvent) {

            String prefix = userChatEvent.getUser().getUserDetails().getUserRole().getPrefix();
            String username = userChatEvent.getUsername();

            server.broadcast(prefix +username + RESET+": "+userChatEvent.getMessage());
            System.out.println(prefix +username + RESET+": "+userChatEvent.getMessage());
            userChatEvent.applyCooldown();

    }
}
