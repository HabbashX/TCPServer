package com.habbashx.tcpserver.listener.handler;

import com.habbashx.tcpserver.event.Priority;
import com.habbashx.tcpserver.event.handler.EventHandler;
import com.habbashx.tcpserver.event.UserJoinEvent;
import com.habbashx.tcpserver.listener.Listener;
import com.habbashx.tcpserver.socket.Server;
import org.jetbrains.annotations.NotNull;

import static com.habbashx.tcpserver.logger.ConsoleColor.RED;
import static com.habbashx.tcpserver.logger.ConsoleColor.RESET;

@EventHandler(priority = Priority.HIGHEST)
public final class DefaultUserJoinHandler implements Listener<UserJoinEvent> {

    private final Server server;

    public DefaultUserJoinHandler(Server server) {
        this.server = server;
    }

    @Override
    public void onEvent(@NotNull UserJoinEvent userJoinEvent) {

        if (!server.getBanCommandManager().isUserBanned(userJoinEvent.getUsername())) {
            server.broadcast(userJoinEvent.getUsername() +" join the chat");
            server.getServerLogger().info(userJoinEvent.getUsername() + " connected to server");
        } else {
            userJoinEvent.setCancelled(true);
            userJoinEvent.getUser().sendMessage(RED+"you are banned from server"+RESET);
            userJoinEvent.getUser().getUserDetails().setUsername(null);
            server.getServerLogger().monitor("the user: "+userJoinEvent.getUsername() + " trying to join to server");
            userJoinEvent.getUser().shutdown();

        }

    }
}
