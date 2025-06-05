package com.habbashx.tcpserver.handler.console;

import com.habbashx.tcpserver.command.CommandSender;
import com.habbashx.tcpserver.event.ServerConsoleChatEvent;
import com.habbashx.tcpserver.socket.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * Handles server-side console input and command management.
 * <p>
 * The ServerConsoleHandler is responsible for processing input from the server console,
 * interpreting commands, and triggering events for non-command messages. This class
 * integrates with the server's command and event management systems.
 * <p>
 * It extends the {@code CommandSender} abstract class, enabling it to handle commands and
 * route messages appropriately while guaranteeing thread-safe operations provided by
 * its parent class. Additionally, it implements both the {@code Runnable} and {@code Closeable}
 * interfaces, allowing it to run as a separate thread and be safely closed when the server shuts down.
 */
public final class ServerConsoleHandler implements Runnable, CommandSender {

    private final Server server;

    public ServerConsoleHandler(Server server) {
        this.server = server;
    }

    /**
     * Continuously reads input from the server console, processes it as either a command
     * or a general message, and appropriately triggers the server's command or event management systems.
     * <p>
     * This method is the entry point for handling server-side console input and is invoked
     * when the {@code ServerConsoleHandler} instance is executed in its own thread.
     * <p>
     * Behavior:
     * - Commands (prefixed by "/") are passed to the server's command manager for execution.
     * - General messages (not prefixed by "/") are wrapped in a {@code ServerConsoleChatEvent}
     * and passed to the server's event manager.
     * <p>
     * Exceptions:
     * - If an {@code IOException} occurs while reading console input, the method will
     * rethrow it as a {@code RuntimeException}.
     * <p>
     * Thread-Safety:
     * - This method operates within a separate thread, ensuring asynchronous handling
     * of console input and management operations.
     */
    @Override
    public void run() {

        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {

            String message;
            while ((message = reader.readLine()) != null) {
                if (message.startsWith("/")) {
                    server.getCommandManager().executeCommand("Server", message, this);
                } else {
                    server.getEventManager().triggerEvent(new ServerConsoleChatEvent(message));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
