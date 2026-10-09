package com.habbashx.tcpserver.connection.console;

import com.habbashx.tcpserver.command.CommandSender;
import com.habbashx.tcpserver.event.ServerConsoleChatEvent;
import com.habbashx.tcpserver.socket.server.Server;
import org.jetbrains.annotations.Contract;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Handles server-side console input and command management.
 * <p>
 * The ServerConsoleHandler is responsible for processing input from the server console,
 * interpreting commands, and triggering events for non-command messages. This class
 * integrates with the server's command and event management systems.
 * <p>
 * It extends the {@code CommandSender} abstract class, enabling it to handle commands and
 * route messages appropriately while guaranteeing thread-safe operations provided by
 * its parent class. Additionally, it implements both the {@code Runnable}
 * interfaces, allowing it to run as a separate thread and be safely closed when the server shuts down.
 */

public final class ServerConsoleHandler extends ConsoleHandler implements CommandSender {

    private final Server server;

    /**
     * Stable per-handler guard. Commands dispatched from the console may run asynchronously
     * ({@code @Command(isAsync = true)}), so this has to be a single shared instance rather
     * than a lock minted per call.
     */
    private final ReentrantLock reentrantLock = new ReentrantLock();

    public ServerConsoleHandler(Server server) {
        this.server = server;
    }

    @Override
    public void run() {
        try (this) {
            String message;
            while ((message = getInput().readLine()) != null) {
                if (message.startsWith("/")) {
                    server.getCommandManager().executeCommand("Server", message, this);
                } else {
                    server.broadcast(message);
                    server.getEventManager().triggerEvent(new ServerConsoleChatEvent(message));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Contract(pure = true)
    @Override
    public boolean isConsole() {
        return true;
    }

    @Contract(pure = true)
    @Override
    public ReentrantLock getReentrantLock() {
        return reentrantLock;
    }
}
