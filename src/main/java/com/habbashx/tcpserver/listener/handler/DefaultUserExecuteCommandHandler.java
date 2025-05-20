package com.habbashx.tcpserver.listener.handler;

import com.habbashx.tcpserver.event.handler.EventHandler;
import com.habbashx.tcpserver.event.UserExecuteCommandEvent;

import com.habbashx.tcpserver.listener.Listener;
import com.habbashx.tcpserver.socket.Server;
import org.jetbrains.annotations.NotNull;

/**
 * Handles user command execution events in an asynchronous manner.
 * This class listens for {@link UserExecuteCommandEvent}, logs information
 * about the user and the command executed, and provides integration with the server's logger.
 *
 * Implements the {@link Listener} interface to handle events of type {@link UserExecuteCommandEvent}.
 * The event handler operation is asynchronous, as indicated by {@code @EventHandler(isAsync = true)}.
 *
 * Responsibilities:
 * - Captures user command execution events.
 * - Logs the username and executed command to the server's logging mechanism.
 *
 * Constructor:
 * - Accepts a {@link Server} instance to establish communication with the server
 *   and access the server's logging utility.
 *
 * Methods:
 * - {@code onEvent(UserExecuteCommandEvent event)}:
 *   Invoked when a {@link UserExecuteCommandEvent} occurs. Logs a message detailing
 *   the username and the command executed.
 *
 * Thread-safety:
 * - The handler is designated for asynchronous operations, ensuring better performance
 *   and non-blocking behavior during event handling.
 */
@EventHandler(isAsync = true)
public final class DefaultUserExecuteCommandHandler implements Listener<UserExecuteCommandEvent> {

    private final Server server;

    public DefaultUserExecuteCommandHandler(Server server) {
        this.server = server;
    }

    @Override
    public void onEvent(@NotNull UserExecuteCommandEvent event) {
        server.getServerLogger().monitor("the user: "+event.getUsername() +" executed the command "+event.getCommandExecutor());
    }
}
