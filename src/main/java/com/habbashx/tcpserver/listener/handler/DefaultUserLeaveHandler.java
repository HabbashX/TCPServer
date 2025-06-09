package com.habbashx.tcpserver.listener.handler;

import com.habbashx.tcpserver.event.handler.EventHandler;
import com.habbashx.tcpserver.event.UserLeaveEvent;

import com.habbashx.tcpserver.listener.Listener;
import com.habbashx.tcpserver.socket.server.Server;
import org.jetbrains.annotations.NotNull;


/**
 * Handles the {@link UserLeaveEvent} when a user disconnects from the system.
 * This handler is responsible for broadcasting the user's departure to the server
 * and logging the disconnection event.
 *
 * This class is designed to be a listener for the {@code UserLeaveEvent} using the
 * {@link EventHandler} annotation, making it part of the event-handling mechanism.
 *
 * Responsibilities:
 * - Broadcasts a message to the server notifying others of the user's departure.
 * - Logs the disconnection in the server logs for auditing or debugging purposes.
 */
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
