package com.habbashx.tcpserver.connection.console;

import com.habbashx.tcpserver.command.CommandSender;
import com.habbashx.tcpserver.connection.UserHandler;
import com.habbashx.tcpserver.connection.packet.TextPacket;
import com.habbashx.tcpserver.connection.packet.factory.PacketFactory;
import com.habbashx.tcpserver.event.ServerConsoleChatEvent;
import com.habbashx.tcpserver.socket.server.Server;

import java.io.IOException;


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
                    TextPacket packet = PacketFactory.createText(message);

                    for (var handler : server.getConnectionHandlers()) {
                        if (handler instanceof UserHandler user) {
                            try {
                                user.sendPacket(packet);
                            } catch (Exception e) {
                                user.shutdown();
                            }
                        }
                    }

                    server.getEventManager().triggerEvent(new ServerConsoleChatEvent(message));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
