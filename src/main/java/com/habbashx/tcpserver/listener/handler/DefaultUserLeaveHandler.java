package com.habbashx.tcpserver.listener.handler;

import com.habbashx.tcpserver.event.handler.EventHandler;
import com.habbashx.tcpserver.event.UserLeaveEvent;

import com.habbashx.tcpserver.listener.Listener;
import com.habbashx.tcpserver.socket.Server;
import org.jetbrains.annotations.NotNull;


@EventHandler
public final class DefaultUserLeaveHandler implements Listener<UserLeaveEvent> {

    private final Server server;

    public DefaultUserLeaveHandler(Server server) {
        this.server = server;
    }

    @Override
    public void onEvent(@NotNull UserLeaveEvent userLeaveEvent) {
        final String username = userLeaveEvent.getUser().getUserDetails().getUsername();
        server.broadcast(userLeaveEvent.getUsername() +" left the chat");
        server.getServerLogger().info(username+" disconnected from server");
    }
}
