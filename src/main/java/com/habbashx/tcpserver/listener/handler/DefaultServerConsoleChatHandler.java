package com.habbashx.tcpserver.listener.handler;

import com.habbashx.tcpserver.event.handler.EventHandler;
import com.habbashx.tcpserver.event.ServerConsoleChatEvent;

import com.habbashx.tcpserver.listener.Listener;
import com.habbashx.tcpserver.socket.Server;
import org.jetbrains.annotations.NotNull;

import static com.habbashx.tcpserver.logger.ConsoleColor.BRIGHT_RED;
import static com.habbashx.tcpserver.logger.ConsoleColor.GRAY;
import static com.habbashx.tcpserver.logger.ConsoleColor.RESET;

/**
 * The DefaultServerConsoleChatHandler class handles the {@link ServerConsoleChatEvent} event
 * to process and broadcast messages sent through the server console. It uses the console
 * input to format and relay messages to the server and its clients.
 *
 * This class is annotated with {@code @EventHandler}, indicating it is a listener for
 * specified events, in this case, the {@code ServerConsoleChatEvent}. The annotation ensures
 * the handler is correctly registered and recognized by the event system.
 *
 * The handling logic within this implementation performs two main actions:
 * 1. Prints the formatted message to the standard output, representing the server console.
 * 2. Broadcasts the formatted message to the server, making it visible to all connected clients.
 *
 * This implementation ensures that all messages sent by the server through the console
 * are both logged locally and propagated across the server.
 *
 * Constructor Details:
 * - The constructor accepts a {@link Server} instance, which is used to access the server's
 *   broadcasting functionality.
 *
 * Event Handling Details:
 * - The {@code onEvent} method processes the {@code ServerConsoleChatEvent} by:
 *   a. Accessing the message content via the event's {@code getMessage} method.
 *   b. Formatting the message with color codes for display consistency.
 *   c. Printing the message to the server's console.
 *   d. Broadcasting the formatted message to all connected clients.
 *
 * Note that this implementation does not modify the event's cancellation status
 * and assumes all received events are valid for processing.
 */
@EventHandler
public final class DefaultServerConsoleChatHandler implements Listener<ServerConsoleChatEvent> {

    private final Server server;

    public DefaultServerConsoleChatHandler(Server server) {
        this.server = server;
    }

    @Override
    public void onEvent(@NotNull ServerConsoleChatEvent event) {
        final String message = event.getMessage();
        System.out.printf("%s[Server]%s: %s%s\n", BRIGHT_RED, GRAY, RESET, message);
        server.broadcast("%s[Server]%s: %s%s".formatted(BRIGHT_RED, GRAY, RESET, message));
    }
}
